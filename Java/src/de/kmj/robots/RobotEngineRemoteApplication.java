package de.kmj.robots;


import de.kmj.robots.messaging.StatusMessageHandler;
import de.kmj.robots.messaging.MessageServer;
import java.lang.reflect.Constructor;
import de.kmj.robots.messaging.CommandMessage;
import de.kmj.robots.messaging.CommandMessageHandler;
import de.kmj.robots.messaging.StatusMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This application provides access to an arbitrary {@link de.kmj.robots.RobotEngine} via network
 * or console input.
 * <p>
 * To run this application, a configuration file with the following parameters
 * is required:
 * <ul>
 * <li>engine.class: the full name of the RobotEngine implementation</li>
 * <li>engine.config: the path to the engine-specific configuration file</li>
 * <li>network.localIP: the IP address of the computer on which this application is running</li>
 * <li>network.localPort: the port on which this application receives commands for the engine</li>
 * <li>network.bufferSize: the size of the message buffer</li>
 * </ul>
 * 
 * @author Kathrin Janowski
 */
public class RobotEngineRemoteApplication
    implements StatusMessageHandler, CommandMessageHandler{

    /** Handles the remote connection to the control application. */
    MessageServer mMessageServer;

    /** The RobotEngine instance. */
    RobotEngine mEngine;

    /** The application configuration. */
    Properties mAppConfig;
    
    /**
     * Loads the application configuration.
     * 
     * @param appConfigPath path to the .properties file which contains the application's configuration parameters
     * @return true on success, false if the file could not be loaded
     */
    private boolean loadConfig(String appConfigPath)
    {        
        if (appConfigPath == null || appConfigPath.isEmpty())
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load application configuration: no path given");
            return false;
        }

        Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.INFO, "loading application configuration: {0}", appConfigPath);

        
        //----------------------------------------------------------------------
        // load the application config
        //----------------------------------------------------------------------
        mAppConfig = new Properties();
        
        try{
            File file = new File(appConfigPath);
            
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                mAppConfig.load(stream);
                stream.close();
            }
            else {
                Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                    log(Level.SEVERE,
                        "could not load application configuration: file {0} not found",
                        appConfigPath);
                return false;
            }            
        }
        catch (IOException exc) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE,
                    "could not load application configuration: {0}",
                    exc.getMessage());
            return false;
        }
        
        //----------------------------------------------------------------------
        // successfully loaded
        //----------------------------------------------------------------------
        Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.INFO,
                    "successfully loaded application configuration: {0}",
                    mAppConfig.toString());
        
        return true;
    }
    
    /**
     * Launches the application.
     * 
     * Tries to create an instance of the {@link de.kmj.robots.RobotEngine}
     * implementation which is specified in the application configuration.
     * 
     * Afterwards, a {@link de.kmj.robots.messaging.MessageServer} is created
     * based on the network settings in the same configuration.
     * If the creation of the MessageServer fails, the application will issue
     * a warning and start without the option for network input. 
     * 
     * @param appConfigPath path to the .properties file which contains the application's configuration parameters
     * @return true on success, otherwise false
     */
    public boolean launch(String appConfigPath)
    {
        boolean check=loadConfig(appConfigPath);
        if(!check)
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE,
                    "could not load required application parameters -> abort launch");
            return false;
        }
        
        //----------------------------------------------------------------------
        // engine
        //----------------------------------------------------------------------
                
        String engineClass = mAppConfig.getProperty("engine.class");
        if((engineClass == null) || engineClass.isEmpty())
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "missing parameter in application configuration: engine.class");
            return false;
        }

        String engineConfig = mAppConfig.getProperty("engine.config");
        if((engineConfig == null) || engineConfig.isEmpty())
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "missing parameter in application configuration: engine.config");
            return false;
        }
        
        // start the robot engine
        mEngine = loadRobotEngine(engineClass);
        
        if(mEngine==null)
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load robot engine class: {0}",
                    engineClass);
            return false;
        }

        mEngine.setStatusMessageHandler(this);
        mEngine.start(engineConfig);
        
        //TODO: wait until the engine is ready before proceeding
        
        //----------------------------------------------------------------------
        // network connection
        //----------------------------------------------------------------------
        
        String localIP = mAppConfig.getProperty("network.localIP", "127.0.0.1");
        String localPort = mAppConfig.getProperty("network.localPort", "1241");
                
        String bufferSize = mAppConfig.getProperty("network.bufferSize", "4096");

        
        try{
            mMessageServer = new MessageServer(this, bufferSize,
                localIP, localPort);

            mMessageServer.start();
        }
        catch(IllegalArgumentException iae)
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.WARNING, "invalid network configuration: {0}"
                    + "\n\t-> only console input available", 
                    iae.toString()
                );
            mMessageServer = null;
        }        
        
        return true;
    }
   
    /**
     * Terminates the application.
     * 
     * Shuts down the RobotEngine and the MessageServer before exiting.
     */
    public void quit()
    {
        mEngine.stop();
        
        mMessageServer.abort();       
        try {
            mMessageServer.join();
        } catch (InterruptedException ie) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.FINE, "interrupted while stopping the message server");
        }

        Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.INFO, "shutdown complete");
        System.exit(0);
    }
    
    
    /**
     * Main method of the application.
     * @param args the command line arguments, containing only the path
     * to the application configuration file
     */
    public static void main(String[] args) {
        RobotEngineRemoteApplication theRemoteApp =
                new RobotEngineRemoteApplication();
                
        //start the application ---------------------------------------------------
        String configPath=args[0];
        
        theRemoteApp.launch(configPath);
                
        //----------------------------------------------------------------------
        // start checking for input on System.in
        //----------------------------------------------------------------------

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        
        while (true) {
            try {
                // Read STDIN Line 
                final String line = reader.readLine().trim();
                if (line != null) {
                    if (line.equals("exit")) {
                        // Exit Application
                        break;
                    } else {
                        //try to interpret it as a command message
                        try{
                            CommandMessage cmd = new CommandMessage(line);
                            theRemoteApp.handleCommandMessage(cmd);
                        }
                        catch(IllegalArgumentException ex)
                        {
                            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                                log(Level.SEVERE,
                                    "could not parse command: {0}", line);
                        }
                    }
                } else {
                    // Exit Application
                    break;
                }
            } catch (Exception exc) {
                break;
            }
        } 
        
        //exit application -----------------------------------------------------
        
        theRemoteApp.quit();
    }
    
    
    //==========================================================================
    // embedding the RobotEngine
    //==========================================================================

    /**
     * Creates an instace of a specific RobotEngine implementation.
     * 
     * @param className the name of a class which extends RobotEngine
     * @return an instance the requested class, if it is a valid subclass,
     *      otherwise null
     */
    public static RobotEngine loadRobotEngine(String className)
    {
        try{
            Class engineClass = Class.forName(className);
            
            if (RobotEngine.class.isAssignableFrom(engineClass))
            {
                //look for constructor
                Constructor constructor = engineClass.getConstructor();
                if (constructor != null)
                {
                    RobotEngine theEngine = (RobotEngine)constructor.newInstance();
                    return theEngine;
                }
                else{
                    Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                        log(Level.SEVERE, "{0} has no valid constructor",
                            className);
                    return null;
                }
            }
            else{
                Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                    log(Level.SEVERE, "{0} is not a valid RobotEngine subclass",
                        className);
                return null;
            }
        }
        //TODO: improve error messages
        catch(ClassNotFoundException e)
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "RobotEngine class {0} not found", className);
        }
        catch(NoClassDefFoundError e){
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE,
                    "no class definition found for RobotEngine class {0}",
                    className);
        }
        catch (NoSuchMethodException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        } catch (SecurityException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        } catch (InstantiationException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        } catch (IllegalAccessException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        } catch (IllegalArgumentException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        } catch (InvocationTargetException e) {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName()).
                log(Level.SEVERE, "could not load RobotEngine of class {0}: {1}",
                    new Object[]{className, e.toString()});
        }

        return null;
    }
    
    /**
     * Passes a command message over to the RobotEngine instance.
     * @param message the command to execute
     */
    @Override
    public void handleCommandMessage(CommandMessage message)
    {
        try{
            mEngine.executeCommand(message);
        }catch(Exception e)
        {
            Logger.getLogger(RobotEngineRemoteApplication.class.getName())
                    .log(Level.SEVERE, "could not handle command: {0}", e.toString());
        }
    }

    /**
     * Sends the status message to the external control application.
     * @param message the status message
     */
    @Override
    public void handleStatusMessage(StatusMessage message) {
        mMessageServer.sendStatusMessage(message);
    }
    
    
}
