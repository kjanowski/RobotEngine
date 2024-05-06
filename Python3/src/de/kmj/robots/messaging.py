#########################################################################
# This module contains the required classes for the                     #
# RobotEngine messaging protocol.                                       #
#########################################################################

import socket
import threading
import sys
from xml.etree import ElementTree as ET


#########################################################################
# message format for commands sent from the control application         #
#########################################################################
class CommandMessage:

    def __init__(self, taskID, cmdType):
        self.taskID = taskID
        self.cmdType = cmdType
        self.params = {}

    def parse(self, msgString):
        elem = ET.fromstring(msgString)
        self.params={}
        for key in elem.keys():
            if key == "task":
                self.taskID = elem.attrib[key]
            if key == "type":
                self.cmdType = elem.attrib[key]
            else:
                self.params[key] = elem.attrib[key]

    def toxml(self):
        elem = ET.Element("command")
        elem.attrib["task"] = self.taskID
        elem.attrib["type"] = self.cmdType
        for key in self.params.keys():
            elem.attrib[key] = self.params[key]
        return elem

    def tostring(self):
        return ET.tostring(self.toxml(), "UTF-8")


#########################################################################
# message format for status updates sent from to control application    #
#########################################################################
class StatusMessage:

    def __init__(self, taskID, status):
        self.taskID = taskID
        self.status = status
        self.details = {}

    def parse(self, msgString):
        elem = ET.fromstring(msgString)
        self.details={}
        for key in elem.keys():
            if key == "task":
                self.taskID = elem.attrib[key]
            if key == "status":
                self.status = elem.attrib[key]
            else:
                self.details[key] = elem.attrib[key]

    def toxml(self):
        elem = ET.Element("status")
        elem.attrib["task"] = self.taskID
        elem.attrib["status"] = self.status
        for key in self.details.keys():
            elem.attrib[key] = self.details[key]
        return elem

    def tostring(self):
        return ET.tostring(self.toxml(), "UTF-8")


########################################################################
# UDP Server which communicates with the control application           #
########################################################################
class MessageServer(threading.Thread):

    #===================================================================
    # Constructor of Server 
    #===================================================================
    def __init__(self, local_host, local_port, buffer_size, timeout, engine):
        threading.Thread.__init__(self)
        self.engine = engine
        
        self.local_host = local_host
        self.local_port = local_port
        self.buffer_size = buffer_size
        self.timeout = timeout
        self.debugOutput = False
        
        self.__reset__() 
    
    #===================================================================
    # Reset local attributes
    #===================================================================
    def __reset__(self):   
        print("[MessageServer] Reset")
        self.serverSock = None
        self.serverAddr = None
        self.clientAddr = None
        self.clientData = None
        self.isRunning = None
        self.isWaiting = None
    

    #===================================================================
    # Sends a status message back to the control application.
    #===================================================================
    def send(self, status):
        if self.clientAddr != None:
            try:
                self.serverSock.sendto(status.tostring(), self.clientAddr)
                if self.debugOutput:
                    print("[MessageServer] Sending StatusMessage: ", status.tostring())
            except Exception as exc:
                print("[MessageServer] Cannot send StatusMessage: ", exc)
    
    #===================================================================
    # Stop the Server: Close the Socket and Reset.
    #===================================================================
    def stop(self):            
        # Close The Server Socket    
        if(self.serverSock): 
            self.serverSock.close()
            print("[MessageServer] Closing Server Socket")
        # Reset the Member Fields
        self.__reset__()
        print("[MessageServer] Stopping Server Thread")
        self._Thread__stop() 
     
    #===================================================================
    # Server.start(): The main method of the server thread.
    #===================================================================
    def run(self):          

        try:
            #--------------------------------------------------------------------
            # Create and bind the socket                                         
            #--------------------------------------------------------------------
            if self.debugOutput:
                print("[MessageServer] Creating Socket")
            self.serverSock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            if self.debugOutput:
                print("[MessageServer] Binding Server Socket to ", self.local_host ,":", self.local_port)
            self.serverSock.bind((self.local_host, self.local_port))

            
            # Set The Running Flag
            self.isRunning = True;     
            if self.debugOutput:
                print("[MessageServer] Running and waiting for message")

            #--------------------------------------------------------------------
            # Main loop where the communication happens.                         
            # Server is in infinite loop until stopped by the parent application.
            #--------------------------------------------------------------------
            while self.isRunning:
                try:
                    self.clientData, self.clientAddr = self.serverSock.recvfrom(self.buffer_size)
                    if self.debugOutput:
                        print("[MessageServer] received ", len(self.clientData), " from address ", self.clientAddr, ": ", self.clientData)
                    
                    cmd = CommandMessage("unknown", "unknown")
                    cmd.parse(self.clientData)                        
                    self.engine.execute(cmd)
                except Exception as exc:
                    print("[MessageServer] EXCEPTION: ", exc)
                    
        except Exception as exc:
            print("[MessageServer] Server Thread Exception ", exc)
            print("[MessageServer] Aborting Server Thread")
            self.isRunning = False
        

########################################################################
# UDP Client which communicates with the RobotEngine                   #
########################################################################
class MessageClient(threading.Thread):

    #===================================================================
    # Constructor of Client 
    #===================================================================
    def __init__(self, local_host, local_port, remote_host, remote_port, buffer_size, callback):
        threading.Thread.__init__(self)

        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.buffer_size = buffer_size
        self.local_host = local_host
        self.local_port = local_port
        self.remote_host = remote_host
        self.remote_port = remote_port
        self.callback = callback

        self.debugOutput = False
    

    #===================================================================
    # Sends a command message to the RobotEngine.
    #===================================================================
    def send(self, command):
        self.socket.sendto(command.tostring(), (self.remote_host, self.remote_port))
    
    #===================================================================
    # Stop the Client: Close the Socket and Reset.
    #===================================================================
    def stop(self):            
        # Close The Socket    
        if(self.socket): 
            self.socket.close()
            print("[MessageClient] Closing Socket")
        print("[MessageClient] Stopping Client Thread")
        self._Thread__stop() 
     
    #===================================================================
    # Client.start(): The main method of the client thread.
    #===================================================================
    def run(self):          

        try:
            #--------------------------------------------------------------------
            # Create and bind the socket                                         
            #--------------------------------------------------------------------
            if self.debugOutput:
                print("[MessageClient] Creating Socket")
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            if self.debugOutput:
                print("[MessageClient] Binding Client Socket to ", self.local_host ,":", self.local_port)
            self.socket.bind((self.local_host, self.local_port))

            
            # Set The Running Flag
            self.isRunning = True;     
            if self.debugOutput:
                print("[MessageClient] Running and waiting for message")

            #--------------------------------------------------------------------
            # Main loop where the communication happens.                         
            # Client is in infinite loop until stopped by the parent application.
            #--------------------------------------------------------------------
            while self.isRunning:
                try:
                    data = self.socket.recv(self.buffer_size)
                    
                    if self.debugOutput:
                        print("[MessageClient] received ", len(self.data), ": ", data)
                    
                    status = StatusMessage("unknown", "unknown")
                    status.parse(data)                        
                    self.callback(status)
                except Exception as exc:
                    print("[MessageClient] EXCEPTION: ", exc)
                    
        except Exception as exc:
            print("[MessageClient] Client Thread Exception ", exc)
            print("[MessageClient] Aborting Client Thread")
            self.isRunning = False
