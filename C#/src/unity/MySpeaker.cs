using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

using UnityEngine;
using UnityEngine.UI;

using Crosstales.RTVoice;
using Crosstales.RTVoice.Util;
using Crosstales.RTVoice.Model;

using RobotEngine.Messaging;

public class MySpeaker : MonoBehaviour
{
	[Space(10)]
	public Text textToSpeak;
	public AudioSource source;
	
	[Space(10)]
	public string bookmarkPattern = "\\$(\\d+)";
	public string voiceName = "Microsoft Hedda Desktop";
	[Range(0.0f, 2.0f)]
	public float rate = 1.0f;
	[Range(-500.0f, 500.0f)]
	public float pitch = 1.0f;
	[Range(0.0f, 2.0f)]
	public float volume = 1.0f;
	
	private Queue<CommandMessage> _commandBuffer;
	
	private Dictionary<int, string> _bookmarkIndices;
	private Wrapper _currentWrapper;
	private Wrapper _currentNativeWrapper;
	private string _currentTask;
	
    // Start is called before the first frame update
    void Start()
	{
		_commandBuffer = new Queue<CommandMessage>();
		_bookmarkIndices = new Dictionary<int, string>();
		_currentWrapper = null;
		_currentNativeWrapper = null;
		_currentTask = null;
		
		Speaker.Instance.OnSpeakStart += OnSpeechStarted;
		Speaker.Instance.OnSpeakComplete += OnSpeechFinished;
		Speaker.Instance.OnSpeakCurrentWord += OnWordSpoken;
    }

    // Update is called once per frame
    void Update()
	{
		if((_currentTask==null)&&(_commandBuffer.Count>0))
		{
			CommandMessage cmd = _commandBuffer.Dequeue();
			Speak(cmd.GetTaskID(), cmd.GetParam("text"));
		}
    }
    
    
	public void Speak(CommandMessage command)
	{
		_commandBuffer.Enqueue(command);
	}
    
    
	private void Speak(string taskID, string text)
	{
		if((text==null)||(text.Length==0))
			return;
			
		var voice = Speaker.Instance.VoiceForName(voiceName);
		if(voice==null)
		{
			Debug.LogError("no voice named \""+voiceName+"\" found!");
			return;
		}		
		
		string ssmlInput;
		if(pitch<0)
			ssmlInput="<prosody pitch=\""+pitch+"Hz\">"+text+"</prosody>";
		else ssmlInput="<prosody pitch=\"+"+pitch+"Hz\">"+text+"</prosody>";
		
		ssmlInput = FilterBookmarks(ssmlInput);
		
		_currentTask = taskID;
		_currentWrapper = new Wrapper(ssmlInput, voice, rate, 1.0f, volume, source, true, "", true);
		_currentNativeWrapper = new Wrapper(ssmlInput, voice, rate, 1.0f, 0.0f, null, true, "", false);
		Speaker.Instance.Speak(_currentWrapper);
		Speaker.Instance.SpeakNative(_currentNativeWrapper);
		
	}
	
	
	private void OnSpeechStarted(Wrapper wrapper)
	{
		if(wrapper.Equals(_currentWrapper))
		{
			StatusMessage status = new StatusMessage(_currentTask, "started");
			SendMessage("SendStatusMessage", status);	
		}
	}
	
	
	private void OnSpeechFinished(Wrapper wrapper)
	{
		if(wrapper.Equals(_currentNativeWrapper))
		{
			StatusMessage status = new StatusMessage(_currentTask, "finished");
			SendMessage("SendStatusMessage", status);	
			_currentWrapper = null;
			_currentTask = null;
		}
	}

	private void OnWordSpoken(Wrapper wrapper, string[] textArray, int index)
	{
		if(!wrapper.Equals(_currentNativeWrapper))
			return;
		
		//	Debug.Log("arrived at word "+index+": "+textArray[index]);
		
		string bookmarkID;
		bool success = _bookmarkIndices.TryGetValue(index, out bookmarkID);
		
		if(success)
		{
			StatusMessage status = new StatusMessage(_currentTask, "bookmark");
			status.AddDetail("id", bookmarkID);			
			
			SendMessage("SendStatusMessage", status);	
		}
	}
	
	private string FilterBookmarks(string text)
	{
		StringBuilder builder = new StringBuilder();
		Regex bookmarkRegex = new Regex("(\\S*)"+bookmarkPattern+"(\\S*)");
		_bookmarkIndices.Clear();
		

		int start=0;
		int end=text.IndexOf(' ');
		int wordIndex = 0;
		string word;
		bool checking = true;
		while(checking)
		{
			//parse the current word
			if(end>0)
				word = text.Substring(start, end-start);
			else word = text.Substring(start);
			
			//Debug.Log("parsing word \""+word+"\"");
			
			Match match = bookmarkRegex.Match(word);
			if((match!=null) && match.Success)
			{
				string predecessor = match.Groups[1].Value;
				string bookmarkID = match.Groups[2].Value;
				string successor = match.Groups[3].Value;
				
				if((predecessor!=null)&&(predecessor.Length>0))
				{
					builder.Append(predecessor);
					builder.Append(' ');
					wordIndex++;
					//Debug.Log("appended word \""+predecessor+"\"");
				}
				
				//store the bookmark and the word index
				int index=wordIndex-1;
				_bookmarkIndices.Add(index, bookmarkID);
				//Debug.Log("stored bookmark: ("+index+", "+bookmarkID+")");
					
				if((successor!=null)&&(successor.Length>0))
				{
					builder.Append(successor);
					builder.Append(' ');
					wordIndex++;
					//Debug.Log("appended word \""+successor+"\"");
				}
			}
			else
			{
				builder.Append(word);
				builder.Append(' ');
				wordIndex++;
				
				//Debug.Log("appended word \""+word+"\"");
			}
			

			//move on to the next word, skip the space
			if(end<0)
				checking=false;
			else{
				start=end+1;
				if(start<text.Length)
					end = text.IndexOf(' ', start);
				else checking=false;
			}
		}
		
		return builder.ToString();
	}
	
	public void StopSpeech()
	{
		if(_currentWrapper!=null){
			_commandBuffer.Clear();
			Speaker.Instance.Silence(_currentWrapper.Uid);
			Speaker.Instance.Silence(_currentNativeWrapper.Uid);
		}
	}
}
