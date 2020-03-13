/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.commandPool;

import de.kmj.robots.messaging.CommandMessage;
import java.awt.Component;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Kathrin Janowski
 */
public class CommandCellRenderer extends DefaultTreeCellRenderer{
     
    private final TreeMap<String, ImageIcon> mIcons;
    private final ImageIcon mEmptyFolderIcon;
    private final ImageIcon mOpenFolderIcon;
    private final ImageIcon mClosedFolderIcon;
    
    
    public CommandCellRenderer()
    {
        mIcons = new TreeMap<String, ImageIcon>();
        
        String[] commandTypes = {
            "anim", "pose", "gaze", "facs", "point", "led",
            "audio", "stopAudio", "speech", "stopSpeech", "setVoice",
            "move", "stopMove"
        };
        
        for(String type: commandTypes)
        {
            URL iconUrl = getClass().getResource("/icons/commands/"+type+".png");
            mIcons.put(type, new ImageIcon(iconUrl));
        }
   
        mEmptyFolderIcon = new ImageIcon(getClass().getResource("/icons/folder_empty.png"));
        mOpenFolderIcon = new ImageIcon(getClass().getResource("/icons/folder_open.png"));
        mClosedFolderIcon = new ImageIcon(getClass().getResource("/icons/folder_closed.png"));
    }
    
    
    @Override
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        Object obj = node.getUserObject();
        if (leaf) {
            
            if(obj instanceof CommandMessage)
            {
                CommandMessage cmd = (CommandMessage)obj;
                String type = cmd.getCommandType();
                
                //--------------------------------------------------------------
                // icons
                //--------------------------------------------------------------
                ImageIcon icon = mIcons.get(type);
                if(icon!=null)
                    setIcon(icon);
                //else
                    //TODO default command icon
                    
                    
                //--------------------------------------------------------------
                // labels
                //--------------------------------------------------------------
                String text;
                if(type.equals("speech"))
                {
                    text = "\""+cmd.getParam("text")+"\", lipSync: "+cmd.getParam("lipSync");
                }
                else if (type.equals("gaze"))
                {
                    text = "position: ("+cmd.getParam("x")+", "+cmd.getParam("y")+", "+cmd.getParam("z")+")";
                    String speed = cmd.getParam("speed");
                    String time = cmd.getParam("time");
                    
                    if(speed!=null) text += ", speed: "+speed;
                    if(time!=null) text += ", time: "+time;
                }  
                else if (type.equals("point"))
                {
                    text = "position: ("+cmd.getParam("x")+", "+cmd.getParam("y")+", "+cmd.getParam("z")+")";
                    String speed = cmd.getParam("speed");
                    String time = cmd.getParam("time");
                    
                    if(speed!=null) text += ", speed: "+speed;
                    if(time!=null) text += ", time: "+time;
                }  
                else if (type.equals("move"))
                {
                    text = "position: ("+cmd.getParam("x")+", "+cmd.getParam("y")+", "+cmd.getParam("z")+")";
                }  
                else if (type.equals("stopMove"))
                {
                    text = "stop move";
                }
                else if (type.equals("led"))
                {
                    String color = cmd.getParam("color");
                    
                    if(color!=null)
                        text = "color: "+color;
                    else
                        text = "color: ("+cmd.getParam("red")+", "+cmd.getParam("green")+", "+cmd.getParam("blue")+")";
                    
                    
                    String side = cmd.getParam("side");
                    String id = cmd.getParam("id");
                    
                    if(side!=null) text += ", side: "+side;
                    if(id!=null) text += ", id: "+id;
                }  
                else if (type.equals("stopSpeech"))
                {
                    text = "stop speech";
                }
                else if (type.equals("anim") || type.equals("animation"))
                {
                    text = cmd.getParam("name");
                }
                else if (type.equals("audio"))
                {
                    text = cmd.getParam("name");
                }
                else if (type.equals("stopAudio"))
                {
                    text = "stop audio";
                }
                else if (type.equals("setVoice")||type.equals("pose"))
                {
                    Set<Entry<String, String>> params = cmd.getCommandParams().entrySet();
                    text = params.toString();
                }
                else text = cmd.toString();
                
                setText(text);
            }
            else setIcon(mEmptyFolderIcon);
        } else {
            if(expanded)
                setIcon(mOpenFolderIcon);
            else
                setIcon(mClosedFolderIcon);
        } 

        return this;
    }
}
