package de.kmj.robots.messaging;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A UDP client which sends CommandMessages to the server and listens for
 * returning StatusMessages.
 *
 * @see de.kmj.robots.messaging.MessageServer
 * @author Kathrin Janowski
 */
public class MessageClient extends Thread {

    private static final Logger cLogger = Logger.getLogger(MessageClient.class.getName());

    /**
     * Handles all incoming status messages.
     */
    private final StatusMessageHandler mStatusHandler;

    /**
     * Socket for connecting to the RobotEngine application.
     */
    private DatagramSocket mClientSocket;
    private final Object mSocketLock = new Object();

    /**
     * IP address of the machine on which the control application is running.
     */
    private final String mLocalIP;
    /**
     * Port on which the control application is waiting for status messages.
     */
    private final int mLocalPort;
    /**
     * Local socket address for connecting to the RobotEngine application.
     */
    private final SocketAddress mLocalAddr;

    /**
     * IP address of the machine on which the RobotEngine application is
     * running.
     */
    private final String mRemoteIP;
    /**
     * Port on which the RobotEngine application is waiting for commands.
     */
    private final int mRemotePort;
    /**
     * Remote socket address for connecting to the RobotEngine application.
     */
    private SocketAddress mRemoteAddr;

    /**
     * Size of the message buffer.
     */
    private final int mBufferSize;

    /**
     * Run flag.
     */
    private boolean mRunning;

    public MessageClient(StatusMessageHandler handler, String bufferSize,
            String localIP, String localPort,
            String remoteIP, String remotePort
    )
            throws IllegalArgumentException {
        //----------------------------------------------------------------------
        // validate the integer parameters
        //----------------------------------------------------------------------

        try {
            mLocalPort = Integer.parseInt(localPort);
        } catch (Exception e) {
            throw new IllegalArgumentException("can't parse localPort as an integer: " + localPort);
        }

        try {
            mRemotePort = Integer.parseInt(remotePort);
        } catch (Exception e) {
            throw new IllegalArgumentException("can't parse remotePort as an integer: " + remotePort);
        }

        try {
            mBufferSize = Integer.parseInt(bufferSize);
        } catch (Exception e) {
            throw new IllegalArgumentException("can't parse bufferSize as an integer: " + bufferSize);
        }

        //----------------------------------------------------------------------
        // copy the remaining parameters
        //----------------------------------------------------------------------
        mStatusHandler = handler;
        mLocalIP = localIP;
        mRemoteIP = remoteIP;

        //----------------------------------------------------------------------
        // The UDP client connection
        //----------------------------------------------------------------------
        mLocalAddr = new InetSocketAddress(mLocalIP, mLocalPort);
        mRemoteAddr = new InetSocketAddress(mRemoteIP, mRemotePort);

        mRunning = false;
    }

    public MessageClient(StatusMessageHandler handler, int bufferSize,
            String localIP, int localPort,
            String remoteIP, int remotePort) {
        mStatusHandler = handler;

        mLocalIP = localIP;
        mLocalPort = localPort;

        mRemoteIP = remoteIP;
        mRemotePort = remotePort;
        mBufferSize = bufferSize;

        //----------------------------------------------------------------------
        // The UDP client connection
        //----------------------------------------------------------------------
        mLocalAddr = new InetSocketAddress(mLocalIP, mLocalPort);
        mRemoteAddr = new InetSocketAddress(mRemoteIP, mRemotePort);

        mRunning = false;
    }

    /**
     * Connect to the server and start listening for status messages on the
     * socket.
     */
    @Override
    public void start() {
        try {
            synchronized (mSocketLock) {
                // create the client socket
                mClientSocket = new DatagramSocket(mLocalAddr);

                cLogger.log(Level.FINE, "client socket ready: local address {0}",
                                mLocalAddr);

                mClientSocket.connect(mRemoteAddr);
            }
            // Start the server thread
            super.start();
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "error in socket connection: {0}",
                            e.toString());
        }
    }

    /**
     * @return true if the client is connected to the server, otherwise false
     */
    public boolean isConnected() {
        synchronized (mSocketLock) {
            if (mClientSocket == null) {
                return false;
            } else {
                return mClientSocket.isConnected();
            }
        }
    }

    /**
     * Stops the socket thread and closes the connection.
     */
    public void abort() {
        cLogger.log(Level.INFO, "closing client socket...");
        interrupt();

        // close the socket 
        if (mClientSocket != null && !mClientSocket.isClosed()) {
            mClientSocket.close();
        }
    }

    /**
     * Sends a message to the RobotEngine application.
     *
     * @param message the message to send
     * @return true on success, false on failure
     */
    private boolean sendString(final String message) {
        if (mRemoteAddr == null) {
            cLogger.log(Level.WARNING, "could not send because client is unknown");
            return false;
        }

        try {
            // create the UDP packet
            final byte[] buffer = message.getBytes("UTF-8");
            final DatagramPacket packet
                    //        = new DatagramPacket(buffer, buffer.length);
                    = new DatagramPacket(buffer, buffer.length, mRemoteAddr);

            // send the UDP packet
            cLogger.log(Level.FINE, "sending: {0}", message);
            mClientSocket.send(packet);
            cLogger.log(Level.FINE, "message sent");
            return true;
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "could not send message: {0}", e.toString());
            return false;
        }
    }

    /**
     * Sends a command message to the RobotEngine.
     *
     * @param command the command message to send
     * @return true on success, false on failure
     */
    public boolean sendCommandMessage(CommandMessage command) {
        return sendString(command.toString());
    }

    /**
     * Receives bytes over the socket.
     *
     * @return a byte array containing the message, or null on failure
     */
    private byte[] recvBytes() {
        try {
            final byte[] buffer = new byte[mBufferSize];
            final DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length);
            mClientSocket.receive(packet);

            mRemoteAddr = packet.getSocketAddress();
            cLogger.log(Level.FINER, "received packet from address {0}",
                            mRemoteAddr.toString());

            return Arrays.copyOf(buffer, packet.getLength());
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "could not receive byte message: {0}",
                            e.toString());
            return null;
        }
    }

    /**
     * Receives a string over the socket.
     *
     * @return the message string, or null on failure
     */
    private String recvString() {
        try {
            final byte[] buffer = recvBytes();
            // check the buffer content
            if (buffer != null) {
                // convert to string
                final String message
                        = new String(buffer, 0, buffer.length, "UTF-8");
                return message;
            }
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "could not receive String message: {0}",
                            e.toString());
        }
        return null;
    }

    /**
     * Receives a status message over the socket.
     *
     * @return the message string, or null on failure
     */
    private StatusMessage recvStatus() {
        try {
            final String msgString = recvString();

            try {
                StatusMessage status = new StatusMessage(msgString);
                return status;
            } catch (IllegalArgumentException iae) {
                cLogger.log(Level.SEVERE, "could not parse status message: {0}"
                                + "\nraw message string: {1}",
                                new Object[]{iae.toString(), msgString});
            }
        } catch (final Exception e) {
            cLogger.log(Level.SEVERE, "could not receive status message: {0}",
                            e.toString());
        }
        return null;
    }

    /**
     * Listens for incoming StatusMessages.
     */
    @Override
    public final void run() {
        mRunning = true;

        while (mRunning) {
            // receive a new message
            final StatusMessage status = recvStatus();
            if (status != null) {
                cLogger.log(Level.FINE, "received message: {0}", status.toString());

                // handle the event --------------------------------------------
                mStatusHandler.handleStatusMessage(status);
            }

            if (interrupted()) {
                mRunning = false;
            }
        }

        cLogger.log(Level.INFO, "stopped");
    }

}
