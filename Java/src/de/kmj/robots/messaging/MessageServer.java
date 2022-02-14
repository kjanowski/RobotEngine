package de.kmj.robots.messaging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A UDP server which receives CommandMessages and returns StatusMessages to the
 * last known sender.
 *
 * @see de.kmj.robots.messaging.MessageClient
 * @author Kathrin Janowski
 */
public class MessageServer extends Thread {

    private static final Logger cLogger = Logger.getLogger(MessageServer.class.getName());
    
    /**
     * Handles all incoming commands.
     */
    private final CommandMessageHandler mCommandHandler;

    /**
     * Socket for connecting to the control application.
     */
    private DatagramSocket mServerSocket;

    /**
     * IP address of the machine on which the RobotEngine is running.
     */
    private final String mLocalIP;
    /**
     * Port on which the RobotEngine application is waiting for commands.
     */
    private final int mLocalPort;
    /**
     * Local socket address for connecting to the control application.
     */
    private final SocketAddress mLocalAddr;

    /**
     * Remote socket address for connecting to the control application.
     */
    private TreeMap<String, SocketAddress> mRemoteAddrs;

    /**
     * Size of the message buffer.
     */
    private final int mBufferSize;

    /**
     * Run flag.
     */
    private boolean mRunning;

    public MessageServer(CommandMessageHandler handler, String bufferSize,
            String localIP, String localPort)
            throws IllegalArgumentException {
        mCommandHandler = handler;

        //----------------------------------------------------------------------
        // validate the parameters
        //----------------------------------------------------------------------
        mLocalIP = localIP;

        try {
            mLocalPort = Integer.parseInt(localPort);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("can't parse localPort as an integer: " + localPort);
        }

        try {
            mBufferSize = Integer.parseInt(bufferSize);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("can't parse bufferSize as an integer: " + bufferSize);
        }

        //----------------------------------------------------------------------
        // The UDP server connection
        mLocalAddr = new InetSocketAddress(mLocalIP, mLocalPort);
        mRemoteAddrs = new TreeMap<String, SocketAddress>();

        mRunning = false;
    }

    public MessageServer(CommandMessageHandler handler, int bufferSize,
            String localIP, int localPort) {
        mCommandHandler = handler;

        mLocalIP = localIP;
        mLocalPort = localPort;

        mBufferSize = bufferSize;

        //----------------------------------------------------------------------
        // The UDP server connection
        mLocalAddr = new InetSocketAddress(mLocalIP, mLocalPort);
        mRemoteAddrs = new TreeMap<String, SocketAddress>();

        mRunning = false;
    }

    /**
     * Starts listening for command messages on the socket.
     */
    @Override
    public void start() {
        try {

            // create the server socket
            mServerSocket = new DatagramSocket(mLocalAddr);

            cLogger.log(Level.INFO, "server socket ready: local address {0}",
                            mLocalAddr);

            // Start the server thread
            super.start();
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "error in socket connection: {0}",
                            e.toString());
        }
    }

    /**
     * Stops the socket thread and closes the connection.
     */
    public void abort() {
        cLogger.log(Level.INFO, "closing server socket...");
        interrupt();

        // close the socket 
        if (mServerSocket != null && !mServerSocket.isClosed()) {
            mServerSocket.close();
        }

    }

    public void setLogLevel(Level level){
        try{
            cLogger.setLevel(level);
        }catch(SecurityException se){
            cLogger.log(Level.SEVERE, "could not change log level: "+se.getMessage());
        }
    }
    
    public void addLogHandler(Handler handler)
    {
        cLogger.addHandler(handler);
    }
    
    /**
     * Sends a status message to the control application.
     *
     * @param status the status message to send
     * @return true on success, false on failure
     */
    public boolean sendStatusMessage(StatusMessage status) {
        
        //get the receiver of that message -------------------------------------
        if (mRemoteAddrs.isEmpty()) {
            cLogger.log(Level.WARNING, "could not send because there are no known clients");
            return false;
        }

        SocketAddress addr = mRemoteAddrs.get(status.getTaskID());
        if(addr == null)
        {
            cLogger.log(Level.SEVERE, "no client for task ID \"{0}\"", status.getTaskID());
            return false;
        }
        
        //send the UDP packet --------------------------------------------------
        String message = status.toString();
        try {
            // create the UDP packet
            final byte[] buffer = message.getBytes("UTF-8");
            final DatagramPacket packet
                    //        = new DatagramPacket(buffer, buffer.length);
                    = new DatagramPacket(buffer, buffer.length, addr);

            // send the UDP packet
            cLogger.log(Level.INFO, "sending: {0}", message);
            mServerSocket.send(packet);
            cLogger.log(Level.INFO, "message sent");
        } catch (IOException e) {
            cLogger.log(Level.SEVERE, "could not send message: {0}", e.toString());
            return false;
        } catch(IllegalArgumentException e){
            cLogger.log(Level.SEVERE, "could not send message: {0}", e.toString());
            cLogger.log(Level.SEVERE, "destination address: ", addr.toString());
            
            return false;
        }
        
        //clear client address if the status is "finished" or "rejected"
        String st = status.getStatus();
        if(st.equals("finished")||st.equals("rejected"))
            mRemoteAddrs.remove(status.getTaskID());
        return true;
    }


    /**
     * Receives a command message over the socket.
     *
     * @return the message string, or null on failure
     */
    private CommandMessage recvCommand() {
        
        final byte[] buffer = new byte[mBufferSize];
        SocketAddress addr;
        String message;
        
        //receive from socket --------------------------------------------------
        try {
            final DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length);
            mServerSocket.receive(packet);

            
            addr = packet.getSocketAddress();
            cLogger.log(Level.INFO, "received packet from address {0}",
                            addr.toString());

            // convert to string
            message = new String(buffer, 0, packet.getLength(), "UTF-8");

        } catch (final IOException e) {
            cLogger.log(Level.SEVERE, "could not receive string message: {0}",
                            e.toString());
            return null;
        }

        //parse the command message --------------------------------------------
        CommandMessage command;
        try {
            command = new CommandMessage(message);
        } catch (IllegalArgumentException iae) {
            cLogger.log(Level.SEVERE, "could not parse command message: {0}"
                                + "\nraw message string: [{1}]",
                                new Object[]{iae.toString(), message});
            return null;
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "could not receive command message: {0}",
                            e.toString());
            return null;
        }

        
        //store client address for status message association
        mRemoteAddrs.put(command.getTaskID(), addr);
        
        return command;
    }

    /**
     * Listens for incoming CommandMessages.
     */
    @Override
    public final void run() {
        mRunning = true;

        while (mRunning) {
            // receive a new message
            final CommandMessage cmd = recvCommand();
            if (cmd != null) {
                // handle the event --------------------------------------------
                mCommandHandler.handleCommandMessage(cmd);
            }

            if (interrupted()) {
                mRunning = false;
            }
        }

        cLogger.log(Level.INFO, "stopped");
    }

}
