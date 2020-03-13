#########################################################################
# This module contains the required classes for the                     #
# RobotEngine messaging protocol.                                       #
#########################################################################

import socket
import threading
import sys
from elementtree import ElementTree as ET


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
# UDP-Server which communicates with the control application           #
########################################################################
class MessageServer(threading.Thread):

    #===================================================================
    # Constructor of Server 
    #===================================================================
    def __init__(self, ip, port, buffersize, engine):
        threading.Thread.__init__(self)
        self.engine = engine
        
        self.proxy_ip = ip
        self.proxy_port = port
        self.buffersize = buffersize
        self.debugOutput = False
        
        self.__reset__() 
    
    #===================================================================
    # Reset local attributes
    #===================================================================
    def __reset__(self):   
        print "[MessageServer] Reset"         
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
                    print "[MessageServer] Sending StatusMessage: ", status.tostring()
            except Exception as exc:
                print "[MessageServer] Cannot send StatusMessage: ", exc
    #===================================================================
    # Stop the Server: Close the Socket and Reset.
    #===================================================================
    def stop(self):            
        # Close The Server Socket    
        if(self.serverSock): 
            self.serverSock.close()
            print "[MessageServer] Closing Server Socket"
        # Reset the Member Fields
        self.__reset__()
        print "[MessageServer] Aborting Server Thread"
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
                print "[MessageServer] Creating Socket"
            self.serverSock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            if self.debugOutput:
                print "[MessageServer] Binding Server Socket to ", self.proxy_ip ,":", self.proxy_port
            self.serverSock.bind((self.proxy_ip, self.proxy_port))

            
            # Set The Running Flag
            self.isRunning = True;     
            if self.debugOutput:
                print "[MessageServer] Running and waiting for message"

            #--------------------------------------------------------------------
            # Main loop where the communication happens.                         
            # Server is in infinite loop until stopped by the parent application.
            #--------------------------------------------------------------------
            while self.isRunning:
                try:
                    self.clientData, self.clientAddr = self.serverSock.recvfrom(self.buffersize)
                    if self.debugOutput:
                        print "[MessageServer] received ", len(self.clientData), " from address ", self.clientAddr, ": ", self.clientData
                    
                    cmd = CommandMessage("unknown", "unknown")
                    cmd.parse(self.clientData)                        
                    self.engine.execute(cmd)
                except Exception as exc:
                    "[MessageServer] EXCEPTION: ", exc
                    
        except Exception as exc:
            print "[MessageServer] Server Thread Exception ", exc
            print "[MessageServer] Stopping Server Thread"
            self.isRunning = False
        

