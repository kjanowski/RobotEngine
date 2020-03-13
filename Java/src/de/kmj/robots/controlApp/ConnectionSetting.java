package de.kmj.robots.controlApp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Kathrin Janowski
 */
public class ConnectionSetting {
    private String name;
    private String localIP;
    private int localPort;
    private String remoteIP;
    private int remotePort;

    
    public ConnectionSetting(String name, String localIP, int localPort,
                             String remoteIP, int remotePort)
    {
        this.name=name;
        this.localIP=localIP;
        this.localPort=localPort;
        this.remoteIP=remoteIP;
        this.remotePort=remotePort;
    }
    
    public ConnectionSetting()
    {
        name="connection";
        localIP="127.0.0.1";
        localPort=2012;
        remoteIP="127.0.0.1";
        remotePort=1241;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getName() {
        return name;
    }

    public String getLocalIP() {
        return localIP;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public int getRemotePort() {
        return remotePort;
    }
    
    public boolean parseNode(Node node)
    {
        boolean check = node.getNodeName().equalsIgnoreCase("connection");
        if(check)
        {
            NamedNodeMap attribs = node.getAttributes();
            
            name=attribs.getNamedItem("name").getNodeValue();
            
            localIP= attribs.getNamedItem("localIP").getNodeValue();
            
            String localPortStr = attribs.getNamedItem("localPort").getNodeValue();
            try{
                localPort = Integer.parseInt(localPortStr);
            }catch(NullPointerException e)
            {
                check=false;
            }catch(NumberFormatException e)
            {
                check=false;
            }
            
            remoteIP= attribs.getNamedItem("remoteIP").getNodeValue();
            
            String remotePortStr = attribs.getNamedItem("remotePort").getNodeValue();
            try{
                remotePort = Integer.parseInt(remotePortStr);
            }catch(NullPointerException e)
            {
                check=false;
            }catch(NumberFormatException e)
            {
                check=false;
            }
            
        }
        
        return check;
    }
    
    
    public Element toElement(Document doc)
    {
        Element elem = doc.createElement("Connection");
        elem.setAttribute("name", name);
        elem.setAttribute("localIP", localIP);
        elem.setAttribute("localPort", Integer.toString(localPort));
        elem.setAttribute("remoteIP", remoteIP);
        elem.setAttribute("remotePort", Integer.toString(remotePort));
    
        return elem;
    }
}
