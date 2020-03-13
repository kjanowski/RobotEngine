package de.kmj.robots;

import de.kmj.robots.messaging.StatusMessageHandler;
import de.kmj.robots.messaging.CommandMessage;
import de.kmj.robots.messaging.StatusMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic foundation for every robot engine.
 * 
 * Provides the basic infrastructure for receiving command messages and
 * sending feedback messages about their execution progress.
 * 
 * @author Kathrin Janowski
 */
public abstract class RobotEngine {
    /** The object which will receive and process the status messages. */ 
    protected StatusMessageHandler mStatusMessageHandler;
    
    /** The engine's configuration parameters. */
    protected Properties mEngineConfig;
    
    
    /**
     * Sets the output handler for the global/root Logger with name "".
     * 
     * By default, all child Loggers will use this <code>Handler</code>
     * and its <code>Formatter</code>.
     * 
     * @param handler the new output handler
     * @param removeExisting true for removing all previously existing handlers,
     *                       false for keeping them
     * @see java.util.logging.Logger
     * @see java.util.logging.Handler
     * @see java.util.logging.Formatter
     */
    protected final void setGlobalLogHandler(Handler handler, boolean removeExisting)
    {
        Logger globalLogger = Logger.getLogger("");
        
        if(removeExisting)
        {
            Handler[] globalHandlers = globalLogger.getHandlers();
            for(Handler h: globalHandlers)
                globalLogger.removeHandler(h);
        }
        
        globalLogger.addHandler(handler);        
    }

    /**
     * Sets the minimum level for the global/root Logger with name "".
     * 
     * By default, all child Loggers will use this <code>Level</code>.
     * 
     * @param level the minimum global log level
     * @see java.util.logging.Logger
     * @see java.util.logging.Level
     */
    protected final void setGlobalLogLevel(Level level)
    {
        Logger globalLogger = Logger.getLogger("");
        globalLogger.setLevel(level);
    }

    /**
     * Sets the formatter for the global/root Logger with name "".
     * 
     * By default, all child Loggers will use this <code>Formatter</code>.
     * 
     * @param formatter the default Formatter
     * @see java.util.logging.Logger
     * @see java.util.logging.Handler
     * @see java.util.logging.Formatter
     */
    protected final void setGlobalLogFormatter(Formatter formatter)
    {
        Logger globalLogger = Logger.getLogger("");
        
        //apply to all existing handlers
        Handler[] globalHandlers = globalLogger.getHandlers();
            for(Handler h: globalHandlers)
                h.setFormatter(formatter);
        
    }

    
    
    //==========================================================================
    // starting and stopping the engine
    //==========================================================================
    
    /**
     * Loads the engine configuration from the given file.
     * @param configPath path to the .properties file which contains the engine's configuration parameters
     * @return true on success, false if the file could not be loaded
     */
    protected final boolean loadConfig(String configPath)
    {
        if (configPath == null || configPath.isEmpty())
        {
            Logger.getLogger(RobotEngine.class.getName()).
                log(Level.SEVERE, "could not load engine configuration: no path given");
            return false;
        }
        
        Logger.getLogger(RobotEngine.class.getName()).
                log(Level.INFO, "loading engine configuration: {0}", configPath);
        
        //----------------------------------------------------------------------
        // load the engine config
        //----------------------------------------------------------------------
        mEngineConfig = new Properties();
        
        try{
            File file = new File(configPath);
            
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                mEngineConfig.load(stream);
                stream.close();
            }
            else {
                Logger.getLogger(RobotEngine.class.getName()).
                    log(Level.SEVERE,
                        "could not load engine configuration: file \"{0}\" not found",
                        configPath);
                return false;
            }            
        }
        catch (IOException exc) {
            Logger.getLogger(RobotEngine.class.getName()).
                log(Level.SEVERE, "could not load engine configuration: {0}",
                        exc.getMessage());
            return false;
        }
        
        //----------------------------------------------------------------------
        // successfully loaded
        //----------------------------------------------------------------------
        
        Logger.getLogger(RobotEngine.class.getName()).
                log(Level.INFO, "Successfully loaded engine configuration: {0}", mEngineConfig.toString());

        return true;
    }
    
    /**
     * Starts the robot engine.
     * Initializes important resources and starts optional helper threads.
     * 
     * @param configPath path to the .properties file which contains the engine's configuration parameters
     */
    public abstract void start(String configPath);
    
    /**
     * Stops the robot engine.
     * Aborts all threads and actions that are still running.
     */
    public abstract void stop();

    
    //==========================================================================
    // command message handling
    //==========================================================================

    /**
     * Executes a command from an external application.
     * 
     * If the command is supported and valid, the appropriate method is called.
     * Otherwise, the command is rejected and a warning is printed to the console.
     * 
     * @param command the command message 
     */
    public abstract void executeCommand(CommandMessage command);


    /**
     * Rejects an action command.
     * 
     * Assembles a suitable StatusMessage
     * and sends it to the registered StatusMessageHandler.
     * @param command the command to reject
     * @deprecated use <code>rejectCommand(String taskID)</code> instead
     */
    protected final void rejectCommand(CommandMessage command)
    {
        rejectCommand(command.getTaskID());
    }

    /**
     * Rejects an action command with the given reason.
     * 
     * Assembles a suitable StatusMessage
     * and sends it to the registered StatusMessageHandler.
     * @param command the rejected command
     * @param reason the reason why the command was rejected
     * @deprecated use <code>rejectCommand(String taskID, String reason)</code> instead
     */
    protected final void rejectCommand(CommandMessage command, String reason)
    {
        rejectCommand(command.getTaskID(), reason);
    }
    
    
    /**
     * Rejects a task with the given reason.
     * 
     * Assembles a suitable StatusMessage
     * and sends it to the registered StatusMessageHandler.
     * @param taskID the ID of the rejected task
     * @param reason the reason why the command was rejected
     */
    protected final void rejectCommand(String taskID, String reason)
    {
        StatusMessage rejection = new StatusMessage(taskID, "rejected");
        rejection.addDetail("reason", reason);
        sendStatusMessage(rejection);
    }
    
    /**
     * Rejects a task.
     * 
     * Assembles a suitable StatusMessage
     * and sends it to the registered StatusMessageHandler.
     * @param taskID the ID of the rejected task
     */
    protected final void rejectCommand(String taskID)
    {
        StatusMessage rejection = new StatusMessage(taskID, "rejected");
        sendStatusMessage(rejection);
    }
    

    //==========================================================================
    // status message handling
    //==========================================================================
    
    /**
     * Tells the engine where to send the status messages.
     * @param handler the object which will receive and process the status messages
     */
    public final void setStatusMessageHandler(StatusMessageHandler handler)
    {
        mStatusMessageHandler = handler;
    }
    
    /**
     * @return the object which will receive and process the status messages
     */
    public final StatusMessageHandler getStatusMessageHandler()
    {
        return mStatusMessageHandler;
    }
        
    /**
     * Forwards a status message to the status message handler
     * @param status the status message
     */
    public final void sendStatusMessage(StatusMessage status)
    {
        if(mStatusMessageHandler != null)
            mStatusMessageHandler.handleStatusMessage(status);
    }

}
