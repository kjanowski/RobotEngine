using System.Collections;
using System.Collections.Generic;

using UnityEngine;
using UnityEngine.UI;

using RobotEngine.Messaging;


public class UnityBotEngine : MonoBehaviour, ICommandMessageHandler
{
	[Header("Agent Components")]
	public MySpeaker speaker;
	public HeadIK headIK;
	
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
	
	private MessageServer _server;	private Coroutine _serverCoroutine;
	
	
    // Start is called before the first frame update
    void Start()
    {
		//start the server
	    _server = new MessageServer(this, bufferSize, localIPAddress, localPort);
	    StartCoroutine("ListenForCommands");
    }
	

    // Update is called once per frame
    void Update()
    {
	    
    }

	IEnumerator ListenForCommands()
	{
		for(;;)
		{
			_server.Receive();
			yield return new WaitForSeconds(0.1f);
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
			Execute(cmd);
	}

	private void Execute(CommandMessage cmd)
	{
		if(cmd==null)
			return;
		
		switch(cmd.GetCommandType())
		{
			case "speech":
				{
					if(speaker!=null){
						string text = cmd.GetParam("text");
						if(text!=null)
						{
							bool started = speaker.Speak(text);
							if(started)
								SendStatus(cmd.GetTaskID(), "started");
						}else RejectCommand(cmd.GetTaskID(), "no 'text' attribute found");
					}
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
						double.TryParse(xStr, out x);
						double y = 0.0;
						double.TryParse(yStr, out y);
						double z = 0.0;
						double.TryParse(zStr, out z);
						double time = 0;
						double.TryParse(timeStr, out time);
						
						
						bool started = headIK.GazeAt(x,y,z,time);
						if(started)
							SendStatus(cmd.GetTaskID(), "started");
					}else RejectCommand(cmd.GetTaskID(), "gaze animation not supported");
					
					break;
				}
			default:
				{
					Debug.LogWarning("unknown command type: \""+cmd.GetCommandType()+"\"");
					break;
				}
		}	
	}
	

	public bool RejectCommand(string taskID, string reason)
	{
		StatusMessage msg = new StatusMessage(taskID, "rejected");
		msg.AddDetail("reason", reason);
		
		return SendStatus(msg);
	}
	

	public bool SendStatus(string taskID, string status)
	{
		StatusMessage msg = new StatusMessage(taskID, status);
		
		return SendStatus(msg);
	}
	
	public bool SendStatus(StatusMessage msg)
	{
		Debug.Log(msg.ToString());
		//TODO send over server socket
		
		return true;
	}
	
	public void Log(string logMessage){
		Debug.Log(logMessage);
	}
}
