package de.kmj.robots.messaging;

import static de.kmj.robots.messaging.XMLMessage.cLogger;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
public class StatusMessage extends XMLMessage{

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
        
        super();
        
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
        
        super();
        parseDocument(messageStr);

        // validate the message
        if (mTaskID == null || mStatus == null) {
            throw new IllegalArgumentException(
                    "StatusMessage requires at least a task ID and status label");
        }
    }

    /**
     * Creates a StatusMessage by parsing an individual XML node.
     *
     * @param messageNode the XML node containing the message data
     * @throws IllegalArgumentException if the taskID or the status label is
     * missing
     */
    public StatusMessage(Node messageNode){
        super();
        createDocument();
        mDocument.appendChild(messageNode);
        parseMessageContent();
    }
    
    
    @Override
    protected void parseMessageContent()
    {
        if(mDocument==null)
        {
            cLogger.log(Level.SEVERE, "Can't parse status content: The document was not created yet!");
            return;
        }
        
        
        NodeList statusElements = mDocument.getElementsByTagName("status");
        if(statusElements.getLength()==0)
        {
            cLogger.log(Level.SEVERE, "No <status> element in the XML message!");
            return;
        }
        
        //------------------------------------------------------------------
        // parse all attributes
        //------------------------------------------------------------------
        Element statusNode = (Element)statusElements.item(0);
        mStatusDetails = new TreeMap<String, String>();

        NamedNodeMap attributes = statusNode.getAttributes();
        int attrCount = attributes.getLength();
        for (int i = 0; i < attrCount; i++) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();

            if (attrName.equals("task")) {
                mTaskID = attrValue;
            } else if (attrName.equals("status")) {
                mStatus = attrValue;
            } else //arbitrary status details
            {
                mStatusDetails.put(attrName, attrValue);
            }
        }
        
        // validate the message
        if (mTaskID == null || mStatus == null) {
            throw new IllegalArgumentException(
                    "StatusMessage requires at least a task ID and status label");
        }
    }

    
    
    @Override
    public Element createMessageElement(Document doc){
        Element statusElem = doc.createElement("status");
        statusElem.setAttribute("task", mTaskID);
        statusElem.setAttribute("status", mStatus);
            
        Set<Entry<String, String>> params = mStatusDetails.entrySet();
        for (Entry<String, String> param : params)
            statusElem.setAttribute(param.getKey(), param.getValue());
        
        return statusElem;
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

}
