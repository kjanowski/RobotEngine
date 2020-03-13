/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.commandPool;

import de.kmj.robots.messaging.CommandMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
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
import org.xml.sax.SAXException;

/**
 *
 * @author Kathrin
 */
public class CommandPool{
    private static final Logger cLogger = Logger.getLogger(CommandPool.class.getName());
    private Category mRootCategory;
    private File mFile;
    
    private final DefaultTreeModel mCategoryModel;
    private DefaultMutableTreeNode mCategoryRootNode;
    
    public CommandPool()
    {
        mRootCategory = new Category("all");
        mFile = null;
        mCategoryModel = new DefaultTreeModel(mRootCategory.getTreeNode());
        buildTree();
    }
            
    private boolean saveToXML() {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.newDocument();
            
            Element root = xml.createElement("playlist");
            //TODO: any metadata to add?
            xml.appendChild(root);
            
            //------------------------------------------------------------------
            
            Element rootCategory = mRootCategory.toElement(xml);
            xml.appendChild(rootCategory);
            
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
    
    public DefaultTreeModel getTreeModel()
    {
        return mCategoryModel;
    }
    
    protected final void buildTree()
    {
        mCategoryRootNode = mRootCategory.getTreeNode();
        mCategoryModel.setRoot(mCategoryRootNode);
        mCategoryModel.nodeStructureChanged(mCategoryRootNode);
    }

    void addCommand(CommandMessage cmd, TreePath selectionPath) {
        
        Category targetCat = null;
        int index = 0;
        DefaultMutableTreeNode selectedNode = null;
        
        
        if(selectionPath == null)
        {
            targetCat = mRootCategory;
            selectedNode = (DefaultMutableTreeNode)mCategoryModel.getRoot();
            index = targetCat.getSubcategoryCount()+targetCat.getCommandCount();
        }        
        else{
            //what is selected: a category or another command?
            Object selection = selectionPath.getLastPathComponent();
            selectedNode = (DefaultMutableTreeNode)selection;
            Object obj = selectedNode.getUserObject();
            if(obj instanceof Category)
            {
                targetCat = (Category)obj;
                index = targetCat.getSubcategoryCount()+targetCat.getCommandCount();
            }
            else if(obj instanceof CommandMessage)
            {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
                selectedNode = parent;
                targetCat = (Category)parent.getUserObject();
                index = targetCat.getSubcategoryCount();
            }
        }
        
        
        if(targetCat == null)
                return;
            
        //check for duplicates
        if(!targetCat.containsCommand(cmd))
        {
            targetCat.addCommand(cmd);
            mCategoryModel.nodesWereInserted(selectedNode, new int[]{index});
        }
        
    }
    

    //TODO insert commands?


    void remove(TreePath selectionPath) {
        
        Object selection = selectionPath.getLastPathComponent();
        if((selection != null)||(selectionPath.getPathCount()>1))
        {
            //what is selected: a category or another command?
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selection;
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
            
            if(parent == null)
                return; // don't delete the root node
            
            Category parentCat = (Category)parent.getUserObject();
                
            int index = parent.getIndex(selectedNode);
            
            Object obj = selectedNode.getUserObject();
            boolean removed=false;
            if(obj instanceof Category)
            {
                parentCat.removeCategory(index);
                removed=true;
            }
            else if(obj instanceof CommandMessage)
            {
                parentCat.removeCommand(index);
                removed=true;
            }
    
            if(removed)
                mCategoryModel.nodesWereRemoved(parent,
                                                new int[]{index},
                                                new Object[]{selectedNode});
        }
    }

    
    public void addCategory(Category subcat, TreePath selectionPath) {
        
        Category targetCat = null;
        DefaultMutableTreeNode selectedNode = null;
        
        if(selectionPath == null)
        {
            targetCat = mRootCategory;
            selectedNode = (DefaultMutableTreeNode)mCategoryModel.getRoot();
        }        
        else{
            //what is selected: a category or another command?
            Object selection = selectionPath.getLastPathComponent();
            selectedNode = (DefaultMutableTreeNode)selection;
            Object obj = selectedNode.getUserObject();
            if(obj instanceof Category)
            {
                targetCat = (Category)obj;
            }
            else if(obj instanceof CommandMessage)
            {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
                selectedNode=parent;
                targetCat = (Category)parent.getUserObject();
            }
        }
        
        if(targetCat!=null)
        {
            targetCat.addCategory(subcat);
            mCategoryModel.nodesWereInserted(selectedNode, new int[]{targetCat.getSubcategoryCount()-1});
        }
    }
    
    
    public boolean loadFromFile(File file)
    {
        if(file.exists())
            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xml = builder.parse(file);

                //------------------------------------------------------------------

                Element root = xml.getDocumentElement();
                if(root != null)
                {
                    mRootCategory.parseNode(root);
                    buildTree();
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
            
            Element root = mRootCategory.toElement(xml);
            xml.appendChild(root);
            
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

    void addTreeModelListener(TreeModelListener listener) {
        mCategoryModel.addTreeModelListener(listener);
    }
}
