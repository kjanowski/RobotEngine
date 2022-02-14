import sys
import time

from robotEngine import EngineConfig
from YOUR_ROBOT_HERE.config import Config
from YOUR_ROBOT_HERE.YOUR_ROBOT_ENGINE_HERE import YOUR_ENGINE
from messaging import MessageServer, CommandMessage


debugInfo = True

####################################
# Main-file to start Robot-Control #
####################################

if __name__ == '__main__':       

    if debugInfo:
        print("Length of args:", len(sys.argv))
        print("Args:", sys.argv)

    ##############################################################################
    # configuration
    ##############################################################################
    appConfig = EngineConfig(sys.argv)
    
    robotConfig = Config(appConfig.ROBOT_CONFIG)
    engine = YOUR_ROBOT_ENGINE_HERE(robotConfig)
    engine.connect()
    if (engine.ready == False):
        print("Failed to initialize the engine properly.")
        print("Please check the connection to the robot, then try again.")
        sys.exit("failed to connect to robot")
           
    ##############################################################################
    # socket communication
    ##############################################################################

    ip = appConfig.SERVER_IP
    port = appConfig.SERVER_PORT
    buffersize = appConfig.BUFFERSIZE
    timeout = appConfig.SOCKET_TIMEOUT
    
    server = MessageServer(ip, port, buffersize, timeout, engine)    
    server.start()
    engine.setServer(server)

    ############################################################################
    # wait for exit command
    ############################################################################
    command=""
    while(command!="exit"):
        command = input(">>> ")
        try:
            cmd = CommandMessage(None, None)
            cmd.parse(command)
            engine.execute(cmd)
        except Exception as exc:
            print("unrecognized command message")

    print("closing RobotEngine application.")
    server.stop()
    engine.disconnect()
    sys.exit(0)
