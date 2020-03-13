package de.kmj.robots.util;

/**
 * Container for associating a task ID with a speech command.
 * 
 * @author Kathrin Janowski
 */
public class SpeechTask {
    
    /** The task ID associated with the speech command.*/
    private final String mTaskID;
    
    /** The input String which will be sent to the robot's Text-to-Speech engine.*/
    private final String mTTSCommand;
    
    /** The flag indicating whether lip movements should be used.*/
    private final boolean mUseLipSync;
    
    /**
     * Constructor.
     * @param taskID the task ID associated with the speech command
     * @param ttsCommand the input String which will be sent to the robot's Text-to-Speech engine
     * @param useLipSync true for enabling lip movements
     */
    public SpeechTask(String taskID, String ttsCommand, boolean useLipSync)
    {
        mTaskID = taskID;
        mTTSCommand = ttsCommand;
        mUseLipSync = useLipSync;
    }
    
    /**
     * Constructor.
     * @param taskID the task ID associated with the speech command
     * @param ttsCommand the input String which will be sent to the robot's Text-to-Speech engine
     */
    public SpeechTask(String taskID, String ttsCommand)
    {
        mTaskID = taskID;
        mTTSCommand = ttsCommand;
        mUseLipSync = true;
    }
    
    /**
     * @return the task ID associated with the speech command
     */
    public String getTaskID(){return mTaskID;}

    /**
     * @return the input String which will be sent to the robot's Text-to-Speech engine
     */
    public String getTTSCommand(){return mTTSCommand;}

    
    /**
     * @return true if lipSync is enabled for this speech task
     */
    public boolean getUseLipSync(){return mUseLipSync;}
    
    
    /**
     * @return a String containing the tupel (task ID, TTS command)
     */
    @Override
    public String toString(){
        return "("+mTaskID+", "+mTTSCommand+", "+mUseLipSync+")";
    }
}
