package de.kmj.robots.util;

// RobotEngine dependencies
import de.kmj.robots.RobotEngine;
import de.kmj.robots.messaging.StatusMessage;

// scheduling
import static java.lang.Thread.interrupted;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

//------------------------------------------------------------------------------
//TODO/notes:
//
//Similar speech scheduling might already be included in the ScenePlayer when
//Visual SceneMaker is used to control the robot.
//In that case the RobotEngine would be safe to assume that it would not receive
//a second speech command before it sent the "finished" message for one
//which is already running.
//
//In general, the message protocol for the RobotEngine should enforce that
//all of the speech scheduling (sequential dispatching of speech commands,
//canceling pending commands before it's too late) takes place in the control
//application to avoid unnecessary overhead.
//
//It would be better to re-design this helper class as a reusable scheduler
//for the control applications (Wizard-of-Oz GUIs, animation editors, etc.)
//rather than for the RobotEngine implementations.
//
//Also, the SpeechTask can probably be replaced by a CommandMessage.
//
//...Actually, come to think of it - this class could become a generic
//CommandMessage queue as a building block for quickly adding scheduling
//functionality to arbitrary control applications.
//------------------------------------------------------------------------------
/**
 * Ensures that arbitrary speech events are associated with the correct task by
 * managing all speech requests in an explicit "first in, first out" queue.
 * <p>
 * Primarily, this class solves the problem that several robots' speech events
 * contain no information for identifying the speech request that produced them.
 * Therefore, the RobotEngine would not be able to determine which of the
 * control application's tasks has to be notified of the execution process.
 * <p>
 * Robots with this problem include:
 * <ul>
 * <li>Robopec Reeti V1</li>
 * <li>Robopec Reeti V2</li>
 * <li>RoboKind R-50</li>
 * </ul>
 * <p>
 * At least one robot (RoboKind R-50) also requires explicit scheduling for
 * being able to abort pending speech requests. SpeechJobs sent to this robot's
 * TTS can not be aborted and canceled after they have been requested, and
 * therefore simultaneous requests from the control application would force the
 * robot to finish both sentences.
 * <p>
 * To use this class in your RobotEngine, do the following:
 * <ul>
 * <li> Implement the sendToTTS() method to execute the given SpeechTask on your
 * specific robot. </li>
 * <li> Implement an event handler for detecting when your robot has stopped
 * speaking. In this case, call clearFinishedTask().</li>
 * <li> When your RobotEngine processes a "stopSpeech" command, call the
 * clearBuffer() method.</li>
 * </ul>
 *
 * @author Kathrin Janowski
 */
public abstract class FIFOSpeechScheduler extends Thread {

    /**
     * The RobotEngine instance.
     */
    protected final RobotEngine mEngine;

    /**
     * The buffer which contains the requested utterances.
     */
    private final LinkedBlockingQueue<SpeechTask> mBuffer;

    /**
     * The lock which secures the buffer.
     */
    private final Object mBufferLock = new Object();

    /**
     * The currently active speech task.
     */
    private String mCurrentTaskID;

    /**
     * The lock which secures both the speech activity flag and the current task
     * ID.
     */
    private final Object mCurrentTaskLock = new Object();

    /**
     * The speech activity flag.
     */
    private boolean mSpeaking;

    //==========================================================================
    // construction
    //==========================================================================
    /**
     * Constructor.
     *
     * @param engine the surrounding RobotEngine instance
     * @param debug true for enabling debug messages, false for disabling it
     */
    public FIFOSpeechScheduler(RobotEngine engine, boolean debug) {
        super("FIFO SpeechScheduler");

        Logger logger = Logger.getLogger(FIFOSpeechScheduler.class.getName());
        if (debug) {
            logger.setLevel(Level.FINEST);
        } else {
            logger.setLevel(Level.INFO);
        }

        mEngine = engine;
        mBuffer = new LinkedBlockingQueue<SpeechTask>();
        mCurrentTaskID = "unknown";
        mSpeaking = false;
    }

    /**
     * Constructor. Debug output is disabled by default.
     *
     * @param engine the surrounding RobotEngine instance
     */
    public FIFOSpeechScheduler(RobotEngine engine) {
        this(engine, false);
    }

    //==========================================================================
    // scheduling
    //==========================================================================
    protected final String getCurrentTask() {
        synchronized (mCurrentTaskLock) {
            return mCurrentTaskID;
        }
    }

    /**
     * Enqueues a new SpeechTask.
     *
     * @param task the requested speech task
     */
    protected final void bufferTask(SpeechTask task) {
        Logger.getLogger(FIFOSpeechScheduler.class.getName()).
                log(Level.FINE, "enqueueing speech task {0}: \"{1}\"",
                        new Object[]{task.getTaskID(), task.getTTSCommand()});

        synchronized (mBufferLock) {
            //enqueue the speech task
            mBuffer.add(task);

            //inform waiting threads about the change
            mBufferLock.notify();
        }
    }

    protected final void updateSpeechState(boolean speaking) {
        StatusMessage statusMsg = null;

        synchronized (mCurrentTaskLock) {
            mSpeaking = speaking;

            //if speech has stopped, clean up
            if (mCurrentTaskID != null)
            {
                if(mSpeaking)
                {
                    //prepare status message
                    statusMsg = new StatusMessage(mCurrentTaskID, "started");
                
                }else
                {
                    //prepare status message
                    statusMsg = new StatusMessage(mCurrentTaskID, "finished");
                    //clear current task and inform scheduling loop
                    mCurrentTaskID = null;
                    mCurrentTaskLock.notifyAll();
                }
            }
        }

        //send status message?
        if (statusMsg != null) {
            mEngine.sendStatusMessage(statusMsg);
        }
    }

    /**
     * Removes the current task from the buffer and sends the final
     * StatusMessage.
     */
    protected final void clearFinishedTask() {
        String task;
        synchronized (mCurrentTaskLock) {
            task = mCurrentTaskID;

            //reset task data
            mCurrentTaskID = null;
            mCurrentTaskLock.notifyAll();
        }

        StatusMessage statusMsg = new StatusMessage(task, "finished");
        mEngine.sendStatusMessage(statusMsg);
    }

    /**
     * Removes all pending tasks from the buffer.
     */
    protected final void clearBuffer() {
        Logger.getLogger(FIFOSpeechScheduler.class.getName()).
                log(Level.FINE, "clearing buffer...");

        synchronized (mBufferLock) {
            //clear the buffer
            mBuffer.clear();
            //inform waiting threads about the change
            mBufferLock.notify();
        }

        Logger.getLogger(FIFOSpeechScheduler.class.getName()).
                log(Level.FINE, "buffer cleared");
    }

    /**
     * The scheduling loop.
     *
     * Issues the next speech command from the buffer after the robot has
     * finished speaking.
     */
    @Override
    public void run() {
        Logger logger = Logger.getLogger(FIFOSpeechScheduler.class.getName());

        logger.log(Level.FINE, "starting scheduling loop...");

        boolean running = true;
        SpeechTask nextTask;

        while (running) {
            //select the next utterance action
            synchronized (mBufferLock) {
                //wait until there are actions in the buffer
                while (running && mBuffer.isEmpty()) {
                    try {
                        mBufferLock.wait();
                    } catch (InterruptedException ex) {
                        running = false;
                    }
                }

                logger.log(Level.FINER, "#tasks in buffer: {0}", mBuffer.size());

                try {
                    nextTask = mBuffer.remove();
                } catch (NoSuchElementException nsee) {
                    nextTask = null;
                }
            }

            //send the utterance to the TTS 
            if (running && (nextTask != null)) {
                logger.log(Level.FINER, "speaking: {0}", nextTask.toString());

                synchronized (mCurrentTaskLock) {
                    mCurrentTaskID = nextTask.getTaskID();
                    mSpeaking = true;
                    sendToTTS(nextTask);
                }

                //then wait until that utterance has finished
                boolean speaking = true;
                while (speaking) {
                    try {
                        synchronized (mCurrentTaskLock) {
                            speaking = (mSpeaking);

                            if (speaking) {
                                mCurrentTaskLock.wait();
                            }
                        }
                    } catch (InterruptedException ex) {
                        //Scheduler has been interrupted
                        logger.log(Level.FINE, "stopped while speaking");

                        running = false;
                    }
                }
            }

            //check for interruption before looping
            if (interrupted()) {
                running = false;
            }
        }

        logger.log(Level.FINE, "stopped");
    }

    //==========================================================================
    // accessing the robot's TTS
    //==========================================================================
    /**
     * Executes the speech command for a specific robot API.
     *
     * @param task the speech task which is ready to be executed
     */
    protected abstract void sendToTTS(SpeechTask task);

}
