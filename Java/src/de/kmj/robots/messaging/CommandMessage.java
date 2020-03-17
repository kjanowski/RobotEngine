package de.kmj.robots.messaging;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * The data structure for messages sent from the control application to the
 * robot.
 *
 * Every command message contains at least a task identifier and a command type.
 * Additionally, it can hold an arbitrary set of parameters which are required
 * for executing the command.
 *
 * The actual type and parameters are only validated at execution time in order
 * to facilitate the addition of new command types or parameters.
 *
 * The message can be converted to and from an XML string which is used for
 * transmitting it over network sockets.
 *
 * @author Kathrin Janowski
 */
public class CommandMessage extends XMLMessage{

    /**
     * The task identifier.
     */
    private String mTaskID;

    /**
     * The command type.
     */
    private String mCommandType;

    /**
     * The optional set of command parameters.
     */
    private TreeMap<String, String> mCommandParams;

    /**
     * Creates a CommandMessage with the given task ID and command type.
     *
     * @param taskID the task identifier
     * @param type the command type
     * @throws IllegalArgumentException if the taskID or the command type is
     * null
     */
    public CommandMessage(String taskID, String type)
            throws IllegalArgumentException {
        super();
        // validate the message
        if (taskID == null || type == null) {
            throw new IllegalArgumentException(
                    "CommandMessage requires at least a task ID and command type");
        }

        mTaskID = taskID;
        mCommandType = type;

        mCommandParams = new TreeMap<String, String>();
    }

    /**
     * Creates a CommandMessage by parsing its XML representation.
     *
     * @param messageStr the XML string containing the message data
     * @throws IllegalArgumentException if the taskID or the command type is
     * missing
     */
    public CommandMessage(String messageStr) throws IllegalArgumentException {
        super();
        parseDocument(messageStr);
    }    

    /**
     * Creates a CommandMessage by parsing an individual XML node.
     *
     * @param messageNode the XML node containing the message data
     * @throws IllegalArgumentException if the taskID or the command type is
     * missing
     */
    public CommandMessage(Node messageNode){
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
            cLogger.log(Level.SEVERE, "Can't parse command content: The document was not created yet!");
            return;
        }
        
        
        NodeList cmdElements = mDocument.getElementsByTagName("command");
        if(cmdElements.getLength()==0)
        {
            cLogger.log(Level.SEVERE, "No <command> element in the XML message!");
            return;
        }
        
        //------------------------------------------------------------------
        // parse all attributes
        //------------------------------------------------------------------
        Element cmdNode = (Element)cmdElements.item(0);
        mCommandParams = new TreeMap<String, String>();

        NamedNodeMap attributes = cmdNode.getAttributes();
        int attrCount = attributes.getLength();
        for (int i = 0; i < attrCount; i++) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();

            if (attrName.equals("task")) {
                mTaskID = attrValue;
            } else if (attrName.equals("type")) {
                mCommandType = attrValue;
            } else //arbitrary command parameters
            {
                mCommandParams.put(attrName, attrValue);
            }
        }
        
        // validate the message
        if (mTaskID == null || mCommandType == null) {
            throw new IllegalArgumentException(
                    "CommandMessage requires at least a task ID and command type");
        }
    }

    @Override
    public Element createMessageElement(Document doc){
        
        Element cmdElem = doc.createElement("command");
        cmdElem.setAttribute("task", mTaskID);
        cmdElem.setAttribute("type", mCommandType);
            
        Set<Entry<String, String>> params = mCommandParams.entrySet();
        for (Entry<String, String> param : params)
            cmdElem.setAttribute(param.getKey(), param.getValue());
        
        return cmdElem;
    }
    
    
    /*==========================================================================
     *  getters
     *==========================================================================*/
    public String getTaskID() {
        return mTaskID;
    }

    public String getCommandType() {
        return mCommandType;
    }

    public TreeMap<String, String> getCommandParams() {
        return mCommandParams;
    }

    public String getParam(String paramName) {
        return mCommandParams.get(paramName);
    }

        /**
     * Adds the given command parameter. If the entry exists, its value is
     * replaced.
     *
     * @param paramName the parameter name
     * @param paramValue the parameter value
     */
    public void addParameter(String paramName, String paramValue) {
        mCommandParams.put(paramName, paramValue);
    }

    /**
     * Removes the given command parameter.
     *
     * @param paramName the parameter name
     */
    public void removeParameter(String paramName) {
        mCommandParams.remove(paramName);
    }

}
