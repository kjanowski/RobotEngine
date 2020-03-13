using System.Collections;
using System.Collections.Generic;
using UnityEngine;

using System.IO;
using System.Xml;

namespace RobotEngine.Messaging{
	public class CommandMessage
	{
		/// <summary>
		/// The task identifier.
		/// </summary>
		private string _taskID;
		
		/// <summary>
		/// The command type.
		/// </summary>
		private string _commandType;
		
		/// <summary>
		/// The optional set of command parameters.
		/// </summary>
		private Dictionary<string, string> _commandParams;
		
		
		/// <summary>
		/// Creates a CommandMessage with the given task ID and command type.
		/// </summary>
		/// <param name="taskID">the task identifier</param>
		/// <param name="type">the command type</param>
		public CommandMessage(string taskID, string type)
		{
			if((taskID==null)||(type==null))
				throw new System.ArgumentNullException("CommandMessage requires at least a task ID and command type");
			
			_taskID = taskID;
			_commandType = type;
			_commandParams = new Dictionary<string, string>();
		}

		/// <summary>
		/// Creates a CommandMessage by parsing its XML representation.
		/// </summary>
		/// <param name="msgString">the XML string containing the message data</param>
		public CommandMessage(string msgString)
		{
			XmlReader reader = XmlReader.Create(new StringReader(msgString));
			
			XmlDocument doc = new XmlDocument();
			doc.Load(reader);
			
			XmlNodeList cmdNodes = doc.GetElementsByTagName("command");
			if(cmdNodes.Count==0)
				throw new InvalidDataException("message string does not contain any <command> element!");
			
			_commandParams = new Dictionary<string, string>();
			
			var iter = cmdNodes[0].Attributes.GetEnumerator();
			while(iter.MoveNext())
			{
				XmlAttribute attr = (XmlAttribute)iter.Current;
				if(attr.Name.Equals("task"))
				{
					_taskID = attr.Value;
				}
				else if(attr.Name.Equals("type"))
				{
					_commandType = attr.Value;
				}
				else{
					//arbitrary command parameter
					_commandParams.Add(attr.Name, attr.Value);
				}
			}
			
			//validate the message
			if((_taskID==null)||(_commandType==null))
				throw new System.ArgumentNullException("CommandMessage requires at least a task ID and command type");
		}


		public XmlDocument ToDocument(){
			XmlDocument doc = new XmlDocument();
			XmlElement cmdElem = doc.CreateElement("command");
			
			cmdElem.SetAttribute("task", _taskID);
			cmdElem.SetAttribute("type", _commandType);
			
			var iter = _commandParams.GetEnumerator();
			while(iter.MoveNext())
			{
				KeyValuePair<string,string> entry = iter.Current;
				
				cmdElem.SetAttribute(entry.Key, entry.Value);
			}
			
			doc.AppendChild(cmdElem);
			
			return doc;
		}
		
		
		public override string ToString()
		{
			XmlDocument doc = ToDocument();
			
			StringWriter writer = new StringWriter();
			XmlWriter xmlWriter = XmlWriter.Create(writer);
			
			doc.WriteTo(xmlWriter);
			xmlWriter.Flush();
			
			return writer.ToString();
		}
		
		public void AddParameter(string paramName, string paramValue)
		{
			_commandParams.Add(paramName, paramValue);
		}
		
		public void RemoveParameter(string paramName)
		{
			_commandParams.Remove(paramName);
		}
		
		public string GetTaskID(){
			return _taskID;
		}
		
		public string GetCommandType(){
			return _commandType;
		}
		
		public Dictionary<string,string> GetCommandParams(){
			return _commandParams;
		}
		
		public string GetParam(string paramName){
			string value;
			bool success = _commandParams.TryGetValue(paramName, out value);
			if(success)
				return value;
			else return null;
		}
	}
}