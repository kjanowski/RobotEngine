package de.kmj.robots.messaging;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The data structure for messages sent from the robot to the control
 * application.
 *
 * Every status message contains at least a task identifier and a status label
 * which can be interpreted by the conrol application. Additionally, it can hold
 * an arbitrary set of details about the status change.
 *
 * The message can be converted to and from an XML string which is used for
 * transmitting it over network sockets.
 *
 * @author Kathrin Janowski
 */
public class StatusMessage {

    /**
     * The task identifier.
     */
    private String mTaskID;

    /**
     * The task's current status.
     */
    private String mStatus;

    /**
     * The optional set of status details.
     */
    private TreeMap<String, String> mStatusDetails;

    /**
     * Creates a StatusMessage with the given task ID and status label.
     *
     * @param taskID the task identifier
     * @param status the task's current status
     * @throws IllegalArgumentException if the taskID or the command type is
     * null
     */
    public StatusMessage(String taskID, String status)
            throws IllegalArgumentException {
        // validate the message
        if (taskID == null || status == null) {
            throw new IllegalArgumentException(
                    "StatusMessage requires at least a task ID and status label");
        }

        mTaskID = taskID;
        mStatus = status;

        mStatusDetails = new TreeMap<String, String>();
    }

    /**
     * Creates a StatusMessage by parsing its XML representation.
     *
     * @param messageStr the XML string containing the message data
     * @throws IllegalArgumentException if the taskID or the command type is
     * missing
     */
    public StatusMessage(String messageStr) throws IllegalArgumentException {
        try {
            //------------------------------------------------------------------
            // read the XML string
            //------------------------------------------------------------------
            final ByteArrayInputStream stream = new ByteArrayInputStream(messageStr.getBytes("UTF-8"));
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(stream);

            // get the root element
            final Element status = document.getDocumentElement();
            if (!status.getNodeName().equals("status")) {
                throw new IllegalArgumentException("XML root element must be of type \"status\"");
            }

            //------------------------------------------------------------------
            // parse all attributes
            //------------------------------------------------------------------
            mStatusDetails = new TreeMap<String, String>();

            NamedNodeMap attributes = status.getAttributes();
            int attrCount = attributes.getLength();
            for (int i = 0; i < attrCount; i++) {
                Node attr = attributes.item(i);
                String attrName = attr.getNodeName();
                String attrValue = attr.getNodeValue();

                if (attrName.equals("task")) {
                    mTaskID = attrValue;
                } else if (attrName.equals("status")) {
                    mStatus = attrValue;
                } else //arbitrary command parameters
                {
                    mStatusDetails.put(attrName, attrValue);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("UTF-8 input not supported");
        } catch (SAXException ex) {
            throw new IllegalArgumentException("invalid XML syntax");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(StatusMessage.class.getName()).log(Level.SEVERE,
                    "invalid parser configuration", ex);
        } catch (IOException ex) {
            Logger.getLogger(StatusMessage.class.getName()).log(Level.SEVERE,
                    "can't read message string", ex);
        }

        // validate the message
        if (mTaskID == null || mStatus == null) {
            throw new IllegalArgumentException(
                    "StatusMessage requires at least a task ID and status label");
        }
    }

    /**
     * Creates the XML representation of the message. Used for transmitting it
     * between the robot engine and an external control application.
     *
     * @return an XML-formatted status message
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<status task=\"");
        builder.append(mTaskID);
        builder.append("\" status=\"");
        builder.append(mStatus);
        builder.append("\"");

        Set<Entry<String, String>> details = mStatusDetails.entrySet();
        for (Entry detail : details) {
            builder.append(" ");
            builder.append(detail.getKey());
            builder.append("=\"");
            builder.append(detail.getValue());
            builder.append("\"");
        }

        builder.append("/>");

        return builder.toString();
    }

    /**
     * Adds the given status detail. If the entry exists, its value is replaced.
     *
     * @param detailName the detail name
     * @param detailValue the detail value
     */
    public void addDetail(String detailName, String detailValue) {
        mStatusDetails.put(detailName, detailValue);
    }

    /**
     * Removes the given status detail.
     *
     * @param detailName the parameter name
     */
    public void removeDetail(String detailName) {
        mStatusDetails.remove(detailName);
    }

    /*==========================================================================
     *  getters
     *==========================================================================*/
    public String getTaskID() {
        return mTaskID;
    }

    public String getStatus() {
        return mStatus;
    }

    public TreeMap<String, String> getStatusDetails() {
        return mStatusDetails;
    }

    public String getDetail(String detailName) {
        return mStatusDetails.get(detailName);
    }
}
