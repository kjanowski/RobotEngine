/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.messaging;

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
 * A basic XML-based message for the communication between a RobotEngine
 * and a control application.
 * 
 * @author Kathrin Janowski
 */
public abstract class XMLMessage {

    protected static final Logger cLogger = Logger.getLogger(XMLMessage.class.getName());
    
    protected static final DocumentBuilderFactory cBuilderFactory = DocumentBuilderFactory.newInstance();
    protected static final TransformerFactory cTransformerFactory = TransformerFactory.newInstance();
    
    protected Document mDocument;
    
    public XMLMessage(){
        mDocument=null;
    }
    
    /**
     * Reads the XML document from a string.
     * @param messageStr contains the message data in XML syntax
     */
    protected void parseDocument(String messageStr){
        try {
            // read the XML string ----------------------------------
            final ByteArrayInputStream stream = new ByteArrayInputStream(messageStr.getBytes("UTF-8"));
            final DocumentBuilder builder = cBuilderFactory.newDocumentBuilder();
            mDocument = builder.parse(stream);
            
            // parse the content string -----------------------------
            parseMessageContent();
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
    
    
    /**
     * Creates the XML representation of the message data.
     */
    protected void createDocument(){
        try {
            final DocumentBuilder builder = cBuilderFactory.newDocumentBuilder();
            mDocument = builder.newDocument();            
            Element msgElem = createMessageElement(mDocument);
            mDocument.appendChild(msgElem);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CommandMessage.class.getName()).log(Level.SEVERE,
                    "invalid parser configuration: {0}", ex.toString());
        }
    }

    
    
    
    /**
     * Creates an XML-formatted string representation of the message data.
     * Used for transmission between a RobotEngine and a control application.
     * @return the message in XML syntax
     */
    @Override
    public String toString() {
        if(mDocument == null)
            createDocument();
        
        try{
            Transformer transformer = cTransformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            
            StringWriter writer = new StringWriter();
            DOMSource source = new DOMSource(mDocument);
            
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
     * Reads the message content from the XML document.
     */
    protected abstract void parseMessageContent();
    
    /**
     * Creates an XML Element from the message.
     * @param doc the parent Document
     * @return an Element containing the message data
     */
    public abstract Element createMessageElement(Document doc);
    
}
