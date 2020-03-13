/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.automation;

import de.kmj.robots.controlApp.ConnectionSetting;
import de.kmj.robots.messaging.CommandMessage;
import de.kmj.robots.messaging.MessageClient;
import de.kmj.robots.messaging.StatusMessage;
import de.kmj.robots.messaging.StatusMessageHandler;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kathrin
 */
public class AutomationThread extends Thread implements StatusMessageHandler{
    private static final Random sRandom = new Random();
    
    private final Logger mLogger;
    
    private MessageClient mClient;
    private ConnectionSetting mBaseConnection;
    private int mPort;
    private final ReentrantLock mClientLock = new ReentrantLock();
    private final Condition mClientCond = mClientLock.newCondition();
    
    private final ArrayList<CommandMessage> mCommands;
    
    private boolean mActive;
    private final ReentrantLock mActiveLock = new ReentrantLock(true);
    private final Condition mActiveCond = mActiveLock.newCondition();
    
    private boolean mPlaying;
    private final ReentrantLock mPlayingLock = new ReentrantLock(true);
    private final Condition mPlayingCond = mPlayingLock.newCondition();
    
    //play mode
    private boolean mRandomize;
    
    //delays
    private int mMinDelay;
    private int mMaxDelay;

    
    public AutomationThread(String name)
    {
        super(name);
        mLogger = Logger.getLogger("AutomationThread-"+this.getName());
        
        mCommands = new ArrayList<CommandMessage>();
        mClient = null;
        mBaseConnection=null;
        
        //default values
        mRandomize = false;
        
        mMinDelay = 1000;
        mMaxDelay = 1000;
        
        mActive=false;
        mPlaying=false;
    }

    public void setPort(int port){
        mPort = port;
        if(isConnected())
            reconnect();
    }

    public boolean isConnected()
    {
        try{
            mClientLock.lock();
            if(mClient == null)
                return false;
        
            return mClient.isConnected();
        }finally{
            if(mClientLock.isHeldByCurrentThread())
                mClientLock.unlock();
        }
    }

    public boolean connect(ConnectionSetting connection){
        try{
            mClientLock.lock();
        
            mBaseConnection = connection;
        
            return reconnect();
        }finally{
            if(mClientLock.isHeldByCurrentThread())
                mClientLock.unlock();
        }
    }
    
    public boolean reconnect(){
        try{
            mClientLock.lock();
            if(mClient!=null)
                mClient.abort();
            mClient = new MessageClient(this, 1024,
                            mBaseConnection.getLocalIP(), mPort,
                            mBaseConnection.getRemoteIP(), mBaseConnection.getRemotePort()
                            );
            mClient.start();
            mClientCond.signalAll();
            
            boolean success = (mClient!=null)&&mClient.isConnected();
            mLogger.log(Level.INFO, "connected: "+success);
            return success;
        }
        finally{
            if(mClientLock.isHeldByCurrentThread())
                mClientLock.unlock();
        }
    }

    public boolean addCommand(CommandMessage cmd){
        synchronized(mCommands)
        {
            return mCommands.add(cmd);
        }
    }

    public boolean removeCommand(int index){
        if(index<0)
            return false;
        
        synchronized(mCommands){
            if(index >= mCommands.size())
                return false;
            
            CommandMessage old = mCommands.remove(index);            
            return (old!=null);
        }
    }
    
    public ArrayList getCommands()
    {
        return mCommands;
    }
    
    public boolean isActive(){
        boolean active = false;
        try{
            mActiveLock.lock();
            active = mActive;
        }finally{
            if(mActiveLock.isHeldByCurrentThread())
                mActiveLock.unlock();
        }
        return active;
    }
    
    

    public void setRandomize(boolean randomize)
    {
        mRandomize = randomize;
    }
    
    public boolean getRandomize()
    {
        return mRandomize;
    }
    
    public int setMinDelay(int minDelay)
    {
        //limited to [0 sec; 5 min]
        minDelay = Math.max(0, minDelay);
        minDelay = Math.min(300000, minDelay);        
        mMinDelay = minDelay;
        return mMinDelay;
    }
    
    public int getMinDelay(){
        return mMinDelay;
    }
    
    public int setMaxDelay(int maxDelay)
    {
        //limited to [0 sec; 5 min]
        maxDelay = Math.max(0, maxDelay);
        maxDelay = Math.min(300000, maxDelay);        
        mMaxDelay = maxDelay;
        return mMaxDelay;
    }
    
    public int getMaxDelay(){
        return mMaxDelay;
    }
    
    public int getPort(){
        return mPort;
    }
        
    public void setActive(boolean active)
    {
        try{
            mActiveLock.lock();
            
            if(active)
                resetPlayState();
            
            mActive = active;
            mActiveCond.signalAll();            
        }
        finally{
            if(mActiveLock.isHeldByCurrentThread())
                mActiveLock.unlock();
        }
        
        if(!isConnected())
            reconnect();
    }
    
    public void resetPlayState()
    {
        try{
            mPlayingLock.lock();
            mPlaying = false;
            mPlayingCond.signalAll();
        }finally{
            if(mPlayingLock.isHeldByCurrentThread())
                mPlayingLock.unlock();
        }
    }
    
    
    @Override
    public void run(){
        long chosenDelay;
        int chosenIndex = 0;
        boolean active;
        boolean playing;
        boolean running = true;
        
        while(running){
            
            //wait until the thread is activated -------------------------------
            try{
                mActiveLock.lock();
                active = mActive;
                
                while(!active)
                {
                    mLogger.log(Level.INFO, "waiting for activation...");
                    mActiveCond.await();
                    active = mActive;
                }
            }catch(InterruptedException e)
            {
                running = false;
            }finally{
                if(mActiveLock.isHeldByCurrentThread())
                    mActiveLock.unlock();
            }
            
            
            if(!running)
                return;
            
            //------------------------------------------------------------------
            
            boolean connected = false;
            try{
                mClientLock.lock();
                connected = isConnected();
                
                while(!connected){
                    mLogger.log(Level.INFO, "waiting for connection ("
                            +mBaseConnection.getLocalIP()+":"+mPort+"->"
                            +mBaseConnection.getRemoteIP()+":"+mBaseConnection.getRemotePort()+") ...");
                    mClientCond.await();
                    connected = isConnected();
                }
            }catch(InterruptedException e){
                running = false;
            }
            finally{
                if(mClientLock.isHeldByCurrentThread())
                    mClientLock.unlock();
            }
            
            if(!running)
                return;
            
            if(connected){
                //choose the next command ------------------------------------------
                if(mRandomize)
                {
                    chosenIndex = sRandom.nextInt(mCommands.size());
                }else{
                    chosenIndex++;
                    if(chosenIndex >= mCommands.size())
                        chosenIndex=0;
                }

                //send the command -------------------------------------------------

                CommandMessage nextCmd;
                synchronized(mCommands){
                    if(mCommands.isEmpty())
                    {
                        nextCmd = null;
                    }else 
                        nextCmd = mCommands.get(chosenIndex);
                }

                if(nextCmd != null){
                    mClient.sendCommandMessage(mCommands.get(chosenIndex));

                    try{
                        mPlayingLock.lock();
                        mPlaying = true;
                    }
                    finally{
                        if(mPlayingLock.isHeldByCurrentThread())
                            mPlayingLock.unlock();
                    }

                    //wait for end
                    playing = true;            
                    try{
                        mPlayingLock.lock();
                        playing = mPlaying;

                        while(playing)
                        {
                            mLogger.log(Level.INFO, "waiting for task completion...");

                            mPlayingCond.await();
                            playing = mPlaying;
                        }
                    }catch(InterruptedException e)
                    {
                        running = false;
                    }
                    finally{
                        if(mPlayingLock.isHeldByCurrentThread())
                            mPlayingLock.unlock();
                    }

                    if(!running)
                        return;

                    //sleep until next command -----------------------------------------

                    if(mMaxDelay>mMinDelay)
                        chosenDelay = sRandom.nextInt(mMaxDelay-mMinDelay) + mMinDelay;
                    else chosenDelay = mMinDelay;
                    try{
                        mLogger.log(Level.INFO, "sleeping for "+chosenDelay+" ms...");
                        sleep(chosenDelay);
                    }catch(InterruptedException e)
                    {
                        running = false;
                    }

                }
            }
            
            if(isInterrupted())
                running=false;
        }        
    }
    
    @Override
    public void finalize() throws Throwable{
        if(mClient!=null)
            mClient.abort();
        super.finalize();
    }
    
    @Override
    public void handleStatusMessage(StatusMessage message) {
        String status = message.getStatus();
        
        if(status.equals("finished") || status.equals("rejected"))
        {
            try{
                mPlayingLock.lock();
                mPlaying = false;
                mPlayingCond.signalAll();
            }finally{
                if(mPlayingLock.isHeldByCurrentThread())
                    mPlayingLock.unlock();
            }
            
            if(status.equals("rejected"))
            {
                String reason = message.getDetail("reason");
                mLogger.log(Level.WARNING, "command rejected, reason: "+reason);
            }
        }
        //otherwise ignore
    }
    
    
    
}
