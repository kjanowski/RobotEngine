/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 * @author Kathrin
 */
public class Config {
    private static final Logger cLogger = Logger.getLogger(Config.class.getName());
    
    private File mFile;
    
    private final TreeMap<String, ConnectionSetting> mConnections;
    private String mBaseDir;
    
    public Config()
    {
        mConnections = new TreeMap<String, ConnectionSetting>();
        mBaseDir = "./res";
    }
        
    public boolean loadFromFile(String path)
    {
        File file = new File(path);
        if(file.exists())
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(file);
            
            //------------------------------------------------------------------
            
            Element root = xml.getDocumentElement();
            if(root != null)
            {
                if(root.getNodeName().equalsIgnoreCase("ControlAppConfig"))
                {
                    //----------------------------------------------------------
                    // parse the resource directory
                    //----------------------------------------------------------
                    NodeList baseDirNodes = root.getElementsByTagName("BaseDirectory");
                    if(baseDirNodes.getLength() > 1)
                        cLogger.log(Level.WARNING, "too many <BaseDirectory> nodes -> using only the first one");
                    
                    try{
                        Node baseDirNode = baseDirNodes.item(0);
                        NamedNodeMap attribs = baseDirNode.getAttributes();
                        mBaseDir = attribs.getNamedItem("path").getNodeValue();
                    }catch(Exception e)
                    {
                        mBaseDir="./res";
                        cLogger.log(Level.WARNING, "could not read \"path\" attribute from <BaseDirectory> node -> using \"{0}\" instead", mBaseDir);
                    }
                    
                    //----------------------------------------------------------
                    // parse the connection settings
                    //----------------------------------------------------------
                    mConnections.clear();
                    
                    NodeList connSettingNodes = root.getElementsByTagName("ConnectionSettings");
                    if(connSettingNodes.getLength() > 1)
                        cLogger.log(Level.WARNING, "too many <ConnectionSettings> nodes -> using only the first one");
                    
                    try{
                        Node connSettingsNode = connSettingNodes.item(0);
                        
                        NodeList connNodes = connSettingsNode.getChildNodes();
                        
                        for(int i=0; i<connNodes.getLength(); i++)
                        {
                            Node connNode = connNodes.item(i);
                            ConnectionSetting setting = new ConnectionSetting();
                            boolean success = setting.parseNode(connNode);
                            if(success)
                               mConnections.put(setting.getName(), setting);
                            //else
                            //   cLogger.log(Level.WARNING, "could not parse <Connection> node {0} -> skip it", i);
                        }                        
                    }catch(Exception e)
                    {
                        cLogger.log(Level.WARNING, "no stored connection settings found");
                    }
                    
                }
                else{
                    cLogger.log(Level.SEVERE, "root element must be \"ControlAppConfig\"");
                    return false;
                }
            }
            
            mFile = file;
            return true;
        }catch (ParserConfigurationException e)
        {
            cLogger.log(Level.SEVERE, "could not parse xml file: {0}", e.toString());
        }
        catch(SAXException e)
        {
            cLogger.log(Level.SEVERE, "could not parse xml file: {0}", e.toString());
        }
        catch(IOException e){
            cLogger.log(Level.SEVERE, "could not load xml file: {0}", e.toString());
        }
        
        return false;

    }
    
    public boolean saveToFile(File file)
    {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.newDocument();
            
            Element root = xml.createElement("ControlAppConfig");
            xml.appendChild(root);
            
            Element baseDir = xml.createElement("BaseDirectory");
            baseDir.setAttribute("path", mBaseDir);
            root.appendChild(baseDir);
            
            Element connections = xml.createElement("ConnectionSettings");
            root.appendChild(connections);
            
            for(ConnectionSetting setting: mConnections.values())
            {
                Element connSetting = setting.toElement(xml);
                connections.appendChild(connSetting);
            }
            
            //------------------------------------------------------------------
            
            mFile = file;
            mFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(mFile);
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            
            transformer.transform(new DOMSource(xml), new StreamResult(fos));
            
            fos.close();
            cLogger.log(Level.INFO, "saved config to file: {0}", mFile.getPath());
            return true;
        } catch (ParserConfigurationException e)
        {
            cLogger.log(Level.SEVERE, "could not parse xml file: {0}", e.toString());
        }
        catch(TransformerException e)
        {
            cLogger.log(Level.SEVERE, "could not transform xml file: {0}", e.toString());
        }
        catch(IOException e) {
            cLogger.log(Level.SEVERE, "could not save xml file: {0}", e.toString());
        }

        return false;        
    }

    public void saveToFile(String path)
    {
        File file = new File(path);
        saveToFile(file);
    }
    
    public void save()
    {
        if(mFile!=null)
            saveToFile(mFile);
    }

    public void addConnection(ConnectionSetting setting)
    {
        mConnections.put(setting.getName(), setting);
    }
    
    public ConnectionSetting getConnection(String name)
    {
        if(name==null)
            return null;
        if(mConnections.containsKey(name))
            return mConnections.get(name);
        else return null;
    }

    public ConnectionSetting removeConnection(String name)
    {
        if(name==null)
            return null;
        if(mConnections.containsKey(name))
            return mConnections.remove(name);
        else return null;
    }

    public Collection<ConnectionSetting> getConnections()
    {
        return mConnections.values();
    }
    
    public String getBaseDir()
    {
        return mBaseDir;
    }
}
