package de.kmj.robots.controlApp;

import de.kmj.robots.messaging.MessageClient;
import de.kmj.robots.messaging.CommandMessage;
import de.kmj.robots.messaging.StatusMessage;
import de.kmj.robots.messaging.StatusMessageHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * A simple control application which connects to a remote RobotEngine.
 * 
 * Uses a Java Swing GUI.
 * 
 * @author Kathrin Janowski
 */
public class DefaultControlApplication implements StatusMessageHandler{
   
    private MessageClient mClient;
    private final DefaultControlGUI mGUI;
    private Config mConfig;
    
    
    public DefaultControlApplication(String configPath)
    {
        mClient = null;
        
        mConfig = new Config();
        if(configPath != null)
            mConfig.loadFromFile(configPath);
        else mConfig.saveToFile("./ControlApp.config");
        
        mGUI = new DefaultControlGUI(this);
        mGUI.displayConnectionStatus("not connected");
        mGUI.setVisible(true);
    }
    
    
    /**
     * Connects to an external RobotEngine.
     * 
     * @param localIP IP address of the machine running the control application
     * @param localPort port on which the control application listens for StatusMessages
     * @param remoteIP IP address of the machine running the RobotEngine
     * @param remotePort port on which the RobotEngine listens for CommandMessages
     */
    public void connectTo(String localIP, int localPort, String remoteIP, int remotePort)
    {
        if(mClient != null)
        {
            mClient.abort();
            try{
                mClient.join();
            }catch(InterruptedException ie)
            {
                Logger.getLogger(DefaultControlApplication.class.getName()).
                    log(Level.FINE,
                        "interrupted while closing previous client connection");
            }
        }
        
        mGUI.displayConnectionStatus("connecting");
        try{
            mClient = new MessageClient(this, 1024, localIP, localPort, remoteIP, remotePort);
            mClient.start();
        }catch(IllegalArgumentException iae)
        {
            Logger.getLogger(DefaultControlApplication.class.getName()).
                    log(Level.SEVERE,
                        "could not start new client connection: {0}",
                        iae.toString());
            mGUI.displayConnectionStatus("not connected");
        }

        boolean success = mClient.isConnected();
        if(success)
            mGUI.displayConnectionStatus("connected");
        else mGUI.displayConnectionStatus("not connected");
    }

    
    /**
     * Sends the given CommandMessage to the connected RobotEngine.
     * @param command 
     */
    public void sendCommand(CommandMessage command)
    {        
        addCommandToPool(command);
        
        if((mClient == null) || (command == null))
            return;
        
        if(mClient.isConnected())
        {
            boolean success = mClient.sendCommandMessage(command);
            if(success)
                mGUI.displayConnectionStatus("connected");
            else mGUI.displayConnectionStatus("unknown");
            
            
        } else mGUI.displayConnectionStatus("not connected");
    }
    
    
    public void copyCommand(CommandMessage command)
    {
        mGUI.copyCommand(command);
    }
    
    public void addCommandToPool(CommandMessage command)
    {
        mGUI.addCommandToPool(command);
    }
    
    @Override
    public void handleStatusMessage(StatusMessage message) {
        //TODO display the status message
        mGUI.displayStatus(message);
    }
    
    public void exit()
    {
        System.out.println("exiting DefaultControlApp...");
        mConfig.save();
        System.out.println("config saved.");
        mGUI.dispose();
    }
    
    
    /**
     * Launches the application.
     * @param args 
     */
    public static void main(String[] args)
    {
        final String configPath;
        if (args.length>0)
            configPath = args[0];
        else configPath = null;
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run()
            {
                DefaultControlApplication app = new DefaultControlApplication(configPath);
            }
        });
    }

    public Config getConfig() {
        return mConfig;
    }
}
