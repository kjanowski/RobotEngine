package de.kmj.robots.messaging;

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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
public class CommandMessage {

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
        try {
            //------------------------------------------------------------------
            // read the XML string
            //------------------------------------------------------------------
            final ByteArrayInputStream stream = new ByteArrayInputStream(messageStr.getBytes("UTF-8"));
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(stream);
            
            // get the root element
            final Element command = document.getDocumentElement();
            parseDOMNode(command);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("UTF-8 input not supported");
        } catch (SAXException ex) {
            throw new IllegalArgumentException("invalid XML syntax");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "invalid parser configuration", ex);
        } catch (IOException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "can't read message string", ex);
        }

    }
    
    public CommandMessage(Node cmdNode) throws IllegalArgumentException {
        if (!cmdNode.getNodeName().equals("command")) {
            throw new IllegalArgumentException("XML element must be of type \"command\"");
        }
        
        parseDOMNode(cmdNode);
    }
    
    
    private void parseDOMNode(Node cmdNode)
    {
        //------------------------------------------------------------------
        // parse all attributes
        //------------------------------------------------------------------
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

    
    public Element createElement(Document doc){
        Element command = doc.createElement("command");
        command.setAttribute("task", mTaskID);
        command.setAttribute("type", mCommandType);
            
        Set<Entry<String, String>> params = mCommandParams.entrySet();
        for (Entry<String, String> param : params)
            command.setAttribute(param.getKey(), param.getValue());
        
        return command;
    }
    
    public Document toDocument(){
        try {
            //------------------------------------------------------------------
            // create the XML element
            //------------------------------------------------------------------
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();
            
            // get the root element
            final Element command = createElement(document);
            document.appendChild(command);
            
            return document;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "invalid parser configuration: {0}", ex.toString());
        }
        
        return null;
    }
    
    /**
     * Creates the XML representation of the message. Used for transmitting it
     * between the robot engine and an external control application.
     *
     * @return an XML node describing the command message
     */
    @Override
    public String toString() {
        Document doc = toDocument();
        
        try{
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
            StringWriter writer = new StringWriter();
            DOMSource source = new DOMSource(doc);
            
            transformer.transform(source, new StreamResult(writer));            
            return writer.getBuffer().toString();
            
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "invalid transformer configuration: {0}", ex.toString());
        } catch (TransformerException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "could not create String from DOM node: {0}", ex.toString());
        }
        
        return null;
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

}
