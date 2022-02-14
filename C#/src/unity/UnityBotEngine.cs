using System.Collections;
using System.Collections.Generic;
using System.Globalization;

using UnityEngine;
using UnityEngine.UI;

using RobotEngine.Messaging;


public class UnityBotEngine : MonoBehaviour, ICommandMessageHandler
{
	[Header("Agent Components")]
	public MySpeaker speaker;
	public HeadIK headIK;
	public Animator animator;
	public FACSAnimator facsAnimator;
	
	[Space(20)]
	[Header("Direct Input Field")]
	public Text cmdText;
	
	[Space(20)]
	[Header("Message Server")]
	public string localIPAddress = "127.0.0.1";
	[Range(1024,4096)]
	public int localPort = 1241;
	[Range(1024,4096)]
	public int bufferSize = 1024;
	
	private MessageServer _server;
	private Queue<CommandMessage> _cmdBuffer;
	
	private CommandMessage _currentAnim; //TODO replace with proper scheduler!!!
	
    // Start is called before the first frame update
    void Start()
	{
		_cmdBuffer = new Queue<CommandMessage>();
		_currentAnim = null;
		
	    //start the server
	    _server = new MessageServer(this, bufferSize, localIPAddress, localPort);
	}
	

    // Update is called once per frame
    void Update()
    {
	    while(_cmdBuffer.Count>0)
	    {
	    	CommandMessage cmd = _cmdBuffer.Dequeue();
	    	Execute(cmd);
	    }
    }

	public void ExecuteRawText(){
		try{
			CommandMessage cmd = new CommandMessage(cmdText.text);
			Execute(cmd);
		}catch(System.Exception e)
		{
			Debug.LogError("could not execute command: "+e.ToString());
		}
	}
	
	public void HandleCommandMessage(CommandMessage cmd){
		if(cmd!=null)
			_cmdBuffer.Enqueue(cmd);
	}

	private void Execute(CommandMessage cmd)
	{
		if(cmd==null)
			return;
		
		Debug.Log(gameObject.name+" received command "+cmd.ToString());
		
		switch(cmd.GetCommandType())
		{
			case "stopSpeech":
				{
					if(speaker!=null){
						speaker.StopSpeech();
					}
					else SendStatus(cmd.GetTaskID(), "finished");
					break;
				}
			case "speech":
				{
					if(speaker!=null){
						string text = cmd.GetParam("text");
						if(text!=null)
						{
							speaker.Speak(cmd);
						}else RejectCommand(cmd.GetTaskID(), "no 'text' attribute found");
					}else RejectCommand(cmd.GetTaskID(), "speech not supported");
					break;
				}
			case "gaze":
				{
					if(headIK!=null)
					{
						string xStr = cmd.GetParam("x");
						string yStr = cmd.GetParam("y");
						string zStr = cmd.GetParam("z");
						string timeStr = cmd.GetParam("time");
						
						double x = 0.0;
						double.TryParse(xStr, NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out x);
						double y = 0.0;
						double.TryParse(yStr, NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out y);
						double z = 0.0;
						double.TryParse(zStr, NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out z);
						double time = 0;
						double.TryParse(timeStr, NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out time);
						
						
						bool started = headIK.GazeAt(cmd.GetTaskID(), x,y,z,time);
						if(started)
							SendStatus(cmd.GetTaskID(), "started");
					}else RejectCommand(cmd.GetTaskID(), "gaze animation not supported");
					
					break;
				}
			case "anim":
			{
				if(animator!=null){
					PlayAnimation(cmd.GetTaskID(), cmd.GetParam("name"));
					
				}else RejectCommand(cmd.GetTaskID(), "body animation not supported");
				break;
			}
			case "facs":
			{
				if(facsAnimator!=null){
					facsAnimator.ShowFACS(cmd);
				}else RejectCommand(cmd.GetTaskID(), "FACS animation not supported");
				break;
			}
			default:
				{
					RejectCommand(cmd.GetTaskID(), "unsupported command type");
					break;
				}
		}	
	}
	

	public bool RejectCommand(string taskID, string reason)
	{
		StatusMessage msg = new StatusMessage(taskID, "rejected");
		msg.AddDetail("reason", reason);
		
		return SendStatusMessage(msg);
	}
	

	public bool SendStatus(string taskID, string status)
	{
		StatusMessage msg = new StatusMessage(taskID, status);
		
		return SendStatusMessage(msg);
	}
	
	public bool SendStatusMessage(StatusMessage msg)
	{
		bool success =	_server.SendStatus(msg);
		if(!success)
			Debug.Log("could not send status message: "+msg.ToString());
		
		return success;
	}	
		
	public void AnimationFinished(string taskID){
		SendStatus(taskID, "finished");
	}
	
	private void PlayAnimation(string taskID, string name){
		
		bool animRecognized = true;
		
		switch(name){
			case "arms/gesture":{
				animator.SetInteger("gestureIndex", 0);
				break;
			}
			case "arms/shrug":{
				animator.SetInteger("gestureIndex", 1);
				break;
			}
			case "arms/wave":{
				animator.SetInteger("gestureIndex", 2);
				break;
			}
			default:{
				animator.SetInteger("gestureIndex", -1);
				animRecognized=false;
				break;
			}
		}
		
		if(animRecognized){
			SendStatus(taskID, "started");
		}else RejectCommand(taskID, "unknown animation "+name);
	}
	
	//TODO track animation success

}
