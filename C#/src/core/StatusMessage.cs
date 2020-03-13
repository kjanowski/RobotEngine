using System.Collections;
using System.Collections.Generic;
using UnityEngine;

using System.IO;
using System.Xml;

namespace RobotEngine.Messaging{
	public class StatusMessage
	{
		/// <summary>
		/// The task identifier.
		/// </summary>
		private string _taskID;
		
		/// <summary>
		/// The task's current status.
		/// </summary>
		private string _status;
		
		/// <summary>
		/// The optional set of status details.
		/// </summary>
		private Dictionary<string, string> _statusDetails;
		
		
		/// <summary>
		/// Creates a StatusMessage with the given task ID and status type.
		/// </summary>
		/// <param name="taskID">the task identifier</param>
		/// <param name="status">the task's current status</param>
		public StatusMessage(string taskID, string status)
		{
			if((taskID==null)||(status==null))
				throw new System.ArgumentNullException("StatusMessage requires at least a task ID and status label");
			
			_taskID = taskID;
			_status = status;
			_statusDetails = new Dictionary<string, string>();
		}

		/// <summary>
		/// Creates a StatusMessage by parsing its XML representation.
		/// </summary>
		/// <param name="msgString">the XML string containing the message data</param>
		public StatusMessage(string msgString)
		{
			XmlReader reader = XmlReader.Create(new StringReader(msgString));
			
			XmlDocument doc = new XmlDocument();
			doc.Load(reader);
			
			XmlNodeList statusNodes = doc.GetElementsByTagName("status");
			if(statusNodes.Count==0)
				throw new InvalidDataException("message string does not contain any <status> element!");
			
			_statusDetails = new Dictionary<string, string>();
			
			var iter = statusNodes[0].Attributes.GetEnumerator();
			while(iter.MoveNext())
			{
				XmlAttribute attr = (XmlAttribute)iter.Current;
				if(attr.Name.Equals("task"))
				{
					_taskID = attr.Value;
				}
				else if(attr.Name.Equals("status"))
				{
					_status = attr.Value;
				}
				else{
					//arbitrary command parameter
					_statusDetails.Add(attr.Name, attr.Value);
				}
			}
			
			//validate the message
			if((_taskID==null)||(_status==null))
				throw new System.ArgumentNullException("StatusMessage requires at least a task ID and status label");
		}


		public XmlDocument ToDocument(){
			XmlDocument doc = new XmlDocument();
			XmlElement statElem = doc.CreateElement("status");
			
			statElem.SetAttribute("task", _taskID);
			statElem.SetAttribute("status", _status);
			
			var iter = _statusDetails.GetEnumerator();
			while(iter.MoveNext())
			{
				KeyValuePair<string,string> entry = iter.Current;
				
				statElem.SetAttribute(entry.Key, entry.Value);
			}
			
			doc.AppendChild(statElem);
			
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
		
		public void AddDetail(string detailName, string detailValue)
		{
			_statusDetails.Add(detailName, detailValue);
		}
		
		public void RemoveDetail(string detailName)
		{
			_statusDetails.Remove(detailName);
		}
		
		public string GetTaskID(){
			return _taskID;
		}
		
		public string GetStatus(){
			return _status;
		}
		
		public Dictionary<string,string> GetStatusDetails(){
			return _statusDetails;
		}
		
		public string GetDetail(string detailName){
			string value;
			bool success = _statusDetails.TryGetValue(detailName, out value);
			if(success)
				return value;
			else return null;
		}
	}
}