import sys
import time

from aisoy.config import Config
from aisoy.aisoy1KiKEngine import Aisoy1KiKEngine
from robotEngine import EngineConfig
from messaging import MessageServer, CommandMessage


debugInfo = True

####################################
# Main-file to start Robot-Control #
####################################

if __name__ == '__main__':       

    if debugInfo:
        print "Length of args:", len(sys.argv)
        print "Args:", sys.argv

    ##############################################################################
    # configuration
    ##############################################################################
    appConfig = EngineConfig(sys.argv)
    
    robotConfig = Config(appConfig.ROBOT_CONFIG)
    engine = Aisoy1KiKEngine(robotConfig)
    engine.connect()
       
    ##############################################################################
    # socket communication
    ##############################################################################

    ip = appConfig.SERVER_IP
    port = appConfig.SERVER_PORT
    buffersize = appConfig.BUFFERSIZE
    
    server = MessageServer(ip, port, buffersize, engine)    
    server.start()
    engine.setServer(server)

    ############################################################################
    # wait for exit command
    ############################################################################
    command=""
    while(command!="exit"):
        command = raw_input(">>> ")
        try:
            cmd = CommandMessage(None, None)
            cmd.parse(command)
            engine.execute(cmd)
        except Exception as exc:
            print "unrecognized command message"

    print "closing AisoyEngine..."
    server.stop()

    print "server stopped."
    time.sleep(0.5)
    sys.exit(0)
    
