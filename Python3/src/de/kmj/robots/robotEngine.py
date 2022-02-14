from configparser import ConfigParser
from messaging import StatusMessage

debugInfo = True
debugMessage = True



class RobotEngine():
    def __init__(self, config):
        self.config = config
        self.server=None
        self.ready=False

    def setServer(self, server):
        self.server = server

    #=================================================================
    # Initializing the engine
    #=================================================================

    def connect(self):
        print("[RobotEngine] connecting to robot")
        self.ready = True


    #=================================================================
    # Shutting down the engine
    #=================================================================

    def disconnect(self):
        print("[RobotEngine] disconnecting from robot")
        self.ready = False
        
      
    #=================================================================
    # Parsing and executing a command message
    #=================================================================
    def execute(self, cmd):
        print("[RobotEngine] executing \"", cmd.cmdType, "\" command:\n\t", cmd.tostring())
                
    def sendStatus(self, status):
        self.server.send(status)

    def rejectCommand(self, taskID, reason):
        status = StatusMessage(taskID, "reject")
        status.details["reason"]=reason
        self.sendStatus(status)


##################################################################
# Holds general configuration data for a RobotEngine.
#
# Loads a configuration file or uses default values
# in case of failure.
##################################################################
class EngineConfig():

    def __init__(self, argv):        
        self.setDefaultParams()
        self.loadFile(argv[1])
    

    #=================================================================
    # default application parameters
    #=================================================================
    def setDefaultParams(self):
        self.BUFFERSIZE = 1024  	 # size of buffer for receiving

        self.SERVER_IP = "127.0.0.1" # IP of this machine
        self.SERVER_PORT = 1201      # Port of this machine
        
        self.SOCKET_TIMEOUT = 1.0	 # time that the server socket waits
        
        self.ROBOT_CONFIG = "../../res/config/robotConfig.cfg"

    #=================================================================
    # read configuration file
    #=================================================================
    def loadFile(self, filepath):
        # open the given file ----------------------------------------
        print("[EngineConfig] loading config file ", filepath)
        confParser = ConfigParser()
        confParser.read(filepath)
        
        # parse the parameters ---------------------------------------
        try:
            self.BUFFERSIZE = confParser.getint('messaging', 'buffersize')
        except Exception as e:
            print("[EngineConfig] error trying to parse BUFFERSIZE: ")
            print(str(e))
        print("BUFFERSIZE = ", self.BUFFERSIZE)

        self.SERVER_IP = confParser.get('messaging', 'server_ip')
        print("SERVER_IP = ", self.SERVER_IP)

        try:
            self.SERVER_PORT = confParser.getint('messaging', 'server_port')
        except Exception as e:
            print("[MessageConfig] error trying to parse SERVER_PORT: ")
            print(str(e))
        print("SERVER_PORT = ", self.SERVER_PORT)

        try:
            self.SOCKET_TIMEOUT = confParser.getfloat('messaging', 'socket_timeout')
        except Exception as e:
            print("[MessageConfig] error trying to parse SOCKET_TIMEOUT: ")
            print(str(e))
        print("SOCKET_TIMEOUT = ", self.SOCKET_TIMEOUT)

        self.ROBOT_CONFIG = confParser.get('robot', 'robot_config')
        print("ROBOT_CONFIG = ", self.ROBOT_CONFIG)
