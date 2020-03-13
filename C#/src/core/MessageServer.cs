using System;
using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.Collections.Generic;
using System.Text;

namespace RobotEngine.Messaging
{	
	public class MessageServer
	{
		private ICommandMessageHandler _commandHandler;
		
		private Socket _serverSocket;
		
		private IPAddress _localIP;
		
		private int _localPort;
		
		private IPEndPoint _localEndPoint;
		
		private Dictionary<string, EndPoint> _remoteEndPoints;
		
		private byte[] _buffer;
		
		private int _bufferSize;
		
		
		public MessageServer(ICommandMessageHandler handler, int bufferSize, string localIP, int localPort)
		{
			_commandHandler = handler;
			_remoteEndPoints = new Dictionary<string, EndPoint>();
			
			_bufferSize = bufferSize;
			_buffer=new byte[_bufferSize];
			
			_serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
			
			_localIP = IPAddress.Parse(localIP);
			_localPort = localPort;
			_localEndPoint = new IPEndPoint(_localIP, _localPort);			
		
			try{
				_serverSocket.Bind(_localEndPoint);
				_commandHandler.Log("Server running: "+_localEndPoint.ToString());
			}catch(Exception e)
			{
				_commandHandler.Log("Could not bind server socket: "+e.ToString());
			}
		
		}
		
		
		public void Receive()
		{
			EndPoint remoteEP = new IPEndPoint(IPAddress.Any, 0);
			int read = _serverSocket.ReceiveFrom(_buffer, _bufferSize, SocketFlags.None, ref remoteEP);
			
			if(read>0)
			{
				string msg = Encoding.UTF8.GetString(_buffer,0,read);
				
				_commandHandler.Log("received: "+msg);
					
				try{
					CommandMessage cmd = new CommandMessage(msg);
						
					//store the client socket for sending status messages later
					_remoteEndPoints.Add(cmd.GetTaskID(), remoteEP);
					
					_commandHandler.HandleCommandMessage(cmd);
				}catch(Exception e)
				{
					_commandHandler.Log("Could not process CommandMessage: "+e.ToString());					
				}	
			}
		}
		
		public void SendStatus(StatusMessage status){
			EndPoint remoteEndPoint;
			bool success = _remoteEndPoints.TryGetValue(status.GetTaskID(), out remoteEndPoint);
			if(success)
			{
				string msg = status.ToString();
				byte[] buffer = Encoding.UTF8.GetBytes(msg);
				
				_serverSocket.SendTo(_buffer, _bufferSize, SocketFlags.None, remoteEndPoint);
				
				//if this was the last status, remove the endpoint from the dictionary
				if(status.GetStatus().Equals("finished"))
				{
					_remoteEndPoints.Remove(status.GetTaskID());
				}
			}
		}

		~MessageServer()
		{
			_serverSocket.Close();
		}
		
	}
}