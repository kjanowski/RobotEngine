/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.commandPool;

import de.kmj.robots.messaging.CommandMessage;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Kathrin
 */
public class Category {
    
    private String mName;
    private final DefaultMutableTreeNode mTreeNode;
    private final ArrayList<CommandMessage> mCommands;
    private final ArrayList<Category> mSubcategories;
    
    public Category(String name)
    {
        mName = name;
        
        mCommands= new ArrayList<CommandMessage>();
        mSubcategories = new ArrayList<Category>();
        mTreeNode = new DefaultMutableTreeNode(this);
    }

    public Category(Node node)
    {
        mCommands= new ArrayList<CommandMessage>();
        mSubcategories = new ArrayList<Category>();
        
        mTreeNode = new DefaultMutableTreeNode(this);
        parseNode(node);
    }

    public String getName()
    {
        return mName;
    }
    
    public void setName(String name)
    {
        if((name!=null) && !name.isEmpty())
            mName = name;
        else mName="---";
    }
    
    public boolean containsCommand(CommandMessage cmd)
    {
        return mCommands.contains(cmd);
    }
    
    public boolean addCommand(CommandMessage cmd)
    {
        boolean success = mCommands.add(cmd);
        if(success)
        {
            DefaultMutableTreeNode cmdNode = new DefaultMutableTreeNode(cmd);
            mTreeNode.add(cmdNode);
        }
        
        return success;
    }
    
    public boolean addCommand(int index, CommandMessage cmd)
    {
        try{
            mCommands.add(index, cmd);
            
            int nodeIndex = mSubcategories.size()+index;
            
            mTreeNode.insert(mTreeNode, nodeIndex);
            return true;
        }catch(ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
    }
    
    public CommandMessage removeCommand(int index)
    {
        int cmdIndex = index - mSubcategories.size();
        
        try{
            CommandMessage removed = mCommands.remove(cmdIndex);
            if(removed != null)
                mTreeNode.remove(index);
            
            return removed;
        }catch(ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
    
    public CommandMessage[] getCommands()
    {
        CommandMessage[] result = new CommandMessage[mCommands.size()];
        mCommands.toArray(result);
        return result;
    }
    
    public int getCommandCount()
    {
        return mCommands.size();
    }
    
    public boolean addCategory(Category category)
    {
        boolean success = mSubcategories.add(category);
        if(success)
        {
            DefaultMutableTreeNode catNode = category.getTreeNode();
            mTreeNode.insert(catNode, mSubcategories.size()-1);
        }
        return success;
    }
    
    public boolean addCategory(int index, Category category)
    {
        try{
            mSubcategories.add(index, category);

            DefaultMutableTreeNode catNode = category.getTreeNode();
            mTreeNode.insert(catNode, index);
        }catch(ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
        return true;
    }
    
    public Category removeCategory(int index)
    {
        Category removed = mSubcategories.remove(index);
        if(removed != null)
            mTreeNode.remove(index);
        return removed;
    }

    
    public int getSubcategoryCount()
    {
        return mSubcategories.size();
    }
    
    public Category[] getSubcategories()
    {
        Category[] result = new Category[mSubcategories.size()];
        mSubcategories.toArray(result);
        return result;
    }

    
    public Element toElement(Document doc)
    {
        Element categoryElem = doc.createElement("category");
        categoryElem.setAttribute("name", mName);
        
        for(CommandMessage cmd: mCommands)
        {
            Element cmdElem = cmd.createElement(doc);
            categoryElem.appendChild(cmdElem);
        }
        
        for(Category subcat: mSubcategories)
        {
            Element subcatElem = subcat.toElement(doc);
            categoryElem.appendChild(subcatElem);
        }
        return categoryElem;
    }
    
    
    public boolean parseNode(Node node)
    {
        if(node.getNodeName().equalsIgnoreCase("category"))
        {
            mCommands.clear();
            mSubcategories.clear();
            mTreeNode.removeAllChildren();

            Node nameNode = node.getAttributes().getNamedItem("name");
            setName(nameNode.getNodeValue());
            
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++)
            {
                Node child=children.item(i);
                String type = child.getNodeName();
                if(type.equals("command"))
                {
                    CommandMessage cmd = new CommandMessage(child);
                    mCommands.add(cmd);
                }
                else if(type.equals("category"))
                {
                    Category subcategory = new Category(child);
                    mSubcategories.add(subcategory);
                }
                //otherwise ignore
            }
            
            updateTreeNode();
            
            return true;
        }
        
        return false;
    }

    public DefaultMutableTreeNode getTreeNode()
    {
        return mTreeNode;
    }
    
    public void updateTreeNode() {
        for(Category subCat: mSubcategories)
        {
            DefaultMutableTreeNode subCatNode = subCat.getTreeNode();
            mTreeNode.add(subCatNode);
        }
        
        for(CommandMessage cmd: mCommands)
        {
            DefaultMutableTreeNode cmdNode = new DefaultMutableTreeNode(cmd);
            mTreeNode.add(cmdNode);
        }
    }
    
    @Override
    public String toString()
    {return mName;}
}
