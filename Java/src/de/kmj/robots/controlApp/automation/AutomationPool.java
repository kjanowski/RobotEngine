/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.automation;

import de.kmj.robots.messaging.CommandMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kathrin
 */
public class AutomationPool {
    private final Logger cLogger = Logger.getLogger(AutomationPool.class.getName());
    private final TreeMap<String, AutomationThread> mAutomations;
    private File mFile;
    
    public AutomationPool()
    {
        mAutomations = new TreeMap<String, AutomationThread>();
        mFile=null;
    }
    
    public void add(AutomationThread thread)
    {
        mAutomations.put(thread.getName(), thread);
    }
    
    public AutomationThread get(String name)
    {
        return mAutomations.get(name);
    }
    
    public void remove(String name)
    {
        mAutomations.remove(name);
    }
    
    public AutomationThread[] getThreads()
    {
        AutomationThread[] threads = new AutomationThread[mAutomations.size()];
        threads = mAutomations.values().toArray(threads);
        return threads;
    }
    
    public boolean saveToFile(File file){
        mFile = file;
        
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.newDocument();
            
            Element root = xml.createElement("Automations");
            //TODO: any metadata to add?
            xml.appendChild(root);
            
            //------------------------------------------------------------------            
            
            for(AutomationThread thread: mAutomations.values())
            {
                Element autoElem = xml.createElement("Automation");
                autoElem.setAttribute("name", thread.getName());
                autoElem.setAttribute("randomize", Boolean.toString(thread.getRandomize()));
                autoElem.setAttribute("minDelay", Integer.toString(thread.getMinDelay()));
                autoElem.setAttribute("maxDelay", Integer.toString(thread.getMaxDelay()));
                autoElem.setAttribute("port", Integer.toString(thread.getPort()));
                
                ArrayList<CommandMessage> cmds = thread.getCommands();
                for(int i=0; i<cmds.size(); i++)
                {
                    Element cmdElem = cmds.get(i).createElement(xml);
                    autoElem.appendChild(cmdElem);
                }
                
                root.appendChild(autoElem);
            }
            
            
            //------------------------------------------------------------------
            
            FileOutputStream fos = new FileOutputStream(mFile);
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            
            transformer.transform(new DOMSource(xml), new StreamResult(fos));
            
            fos.close();
        } catch (ParserConfigurationException e)
        {
            cLogger.log(Level.SEVERE, "could not save xml file: {0}", e.toString());
            return false;
        }
        catch(TransformerException e)
        {
            cLogger.log(Level.SEVERE, "could not save xml file: {0}", e.toString());
            return false;
        }
        catch(IOException e)
        {
            cLogger.log(Level.SEVERE, "could not save xml file: {0}", e.toString());
            return false;
        }
        
        return true;
    }

    
    public boolean loadFromFile(File file){
        if(file.exists())
            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xml = builder.parse(file);

                //--------------------------------------------------------------
                // stop and clear all threads
                //--------------------------------------------------------------
                for(AutomationThread thread: mAutomations.values())
                    thread.interrupt();
                mAutomations.clear();
                                        
                Element root = xml.getDocumentElement();
                if(root != null)
                {
                    NodeList autoNodes = root.getElementsByTagName("Automation");
                    
                    for (int i=0; i<autoNodes.getLength(); i++)
                    {
                        Element autoElem = (Element)autoNodes.item(i);
                        String name=autoElem.getAttribute("name");
                        AutomationThread thread = new AutomationThread(name);
                        
                        String randStr = autoElem.getAttribute("randomize");
                        if(randStr!=null)
                            thread.setRandomize(Boolean.parseBoolean(randStr));
                        
                        String minDelayStr = autoElem.getAttribute("minDelay");
                        if(minDelayStr!=null)
                        {
                            try{
                                int minDelay = Integer.parseInt(minDelayStr);
                                thread.setMinDelay(minDelay);
                            }catch(NumberFormatException nfe)
                            {
                                thread.setMinDelay(0);
                            }
                        }else
                            thread.setMinDelay(0);
                        
                        String maxDelayStr = autoElem.getAttribute("maxDelay");
                        if(maxDelayStr!=null)
                        {
                            try{
                                int maxDelay = Integer.parseInt(maxDelayStr);
                                thread.setMaxDelay(maxDelay);
                            }catch(NumberFormatException nfe)
                            {
                                thread.setMaxDelay(0);
                            }
                        }else
                            thread.setMaxDelay(0);

                        String portStr = autoElem.getAttribute("port");
                        if(portStr!=null)
                        {
                            try{
                                int port = Integer.parseInt(portStr);
                                thread.setPort(port);
                            }catch(NumberFormatException nfe)
                            {
                                int autoPort = 1000+i;
                                cLogger.log(Level.WARNING, "no port found for automation#"+i+" -> assigning automatically: "+autoPort);
                                thread.setPort(autoPort);
                            }
                        }else
                            thread.setMaxDelay(0);
                        

                        //parse the commands
                        NodeList cmdNodes = autoElem.getElementsByTagName("command");
                        for(int j=0; j<cmdNodes.getLength(); j++)
                        {
                            CommandMessage cmd = new CommandMessage(cmdNodes.item(j));
                            thread.addCommand(cmd);
                        }
                        
                        mAutomations.put(name, thread);
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
    
}
