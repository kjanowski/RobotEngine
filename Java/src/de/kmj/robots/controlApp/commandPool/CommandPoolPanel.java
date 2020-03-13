/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.commandPool;

import de.kmj.robots.controlApp.DefaultControlApplication;
import de.kmj.robots.messaging.CommandMessage;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Kathrin
 */
public class CommandPoolPanel extends JPanel implements ActionListener, MouseListener, TreeModelListener, TreeSelectionListener, KeyListener{
    private final DefaultControlApplication mApp;
    
    private JPanel mCommandButtonPanel;
    private JButton mBtn_send;
    private CommandPool mCommandPool;
    private JTree mCommandsTree;
    
    private JPanel mCategoryButtonPanel;
    private JTextField mCategoryNameField;
    private JFileChooser mFileChooser;
    
    
    public CommandPoolPanel(DefaultControlApplication app)
    {
        super(new BorderLayout());
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED),
                                   "previous command messages"));
        setDoubleBuffered(true);
        setMinimumSize(new Dimension(400, 500));

        mApp = app;
        
        mFileChooser = new JFileChooser();
        String baseDir = mApp.getConfig().getBaseDir();
        File baseDirFile = new File(baseDir);
        mFileChooser.setCurrentDirectory(baseDirFile);
        
        //----------------------------------------------------------------------
        mCommandButtonPanel = new JPanel(new GridLayout(1,3));
        mBtn_send = new JButton("send");
        mBtn_send.addActionListener(this);
        mCommandButtonPanel.add(mBtn_send);

        JButton copyButton = new JButton("copy");
        copyButton.addActionListener(this);
        mCommandButtonPanel.add(copyButton);

        JButton deleteButton = new JButton("delete");
        deleteButton.addActionListener(this);
        mCommandButtonPanel.add(deleteButton);

        add(mCommandButtonPanel, BorderLayout.NORTH);
        
        //----------------------------------------------------------------------
        
        mCategoryButtonPanel = new JPanel(new GridLayout(2,2));
        
        JButton addCatButton = new JButton("add category");
        addCatButton.addActionListener(this);
        mCategoryButtonPanel.add(addCatButton);

        mCategoryNameField = new JTextField("category");
        mCategoryButtonPanel.add(mCategoryNameField);
        
        JButton loadListButton = new JButton("load list");
        loadListButton.addActionListener(this);
        mCategoryButtonPanel.add(loadListButton);

        JButton saveListButton = new JButton("save list");
        saveListButton.addActionListener(this);
        mCategoryButtonPanel.add(saveListButton);
        
        
        add(mCategoryButtonPanel, BorderLayout.SOUTH);
        
        //----------------------------------------------------------------------
        mCommandPool = new CommandPool();
        mCommandPool.addTreeModelListener(this);
        
        mCommandsTree = new JTree(mCommandPool.getTreeModel());
        mCommandsTree.setRootVisible(true);
        mCommandsTree.setExpandsSelectedPaths(true);
        mCommandsTree.setCellRenderer(new CommandCellRenderer());
        mCommandsTree.addMouseListener(this);
        mCommandsTree.addKeyListener(this);
        JScrollPane treeScrollPane = new JScrollPane(mCommandsTree);
        add(treeScrollPane, BorderLayout.CENTER);
        
    }
    
    public void updateDisplay()
    {
        mCommandsTree.expandRow(1);
        repaint();
    }
    
    
    public void addCommand(CommandMessage cmd)
    {
        mCommandPool.addCommand(cmd, mCommandsTree.getSelectionPath());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        
        if(action.equals("load list"))
        {
            openFile();
            return;
        }
        else if(action.equals("save list"))
        {
            saveFile();
            return;
        }
        
        //----------------------------------------------------------------------
        
        TreePath selPath = mCommandsTree.getSelectionPath();
        
        if(selPath!=null)
        {
            DefaultMutableTreeNode lastNode =
                    (DefaultMutableTreeNode)(selPath.getLastPathComponent());

            if(lastNode.getUserObject() instanceof CommandMessage)
            {
                CommandMessage cmd = (CommandMessage)(lastNode.getUserObject());
                if(action.equals("send"))
                {
                    mApp.sendCommand(cmd);
                    return;
                }
                else if(action.equals("copy"))
                {
                    mApp.copyCommand(cmd);
                    return;
                }
            }

            if(action.equals("delete"))
            {
                mCommandPool.remove(selPath);
            }
            else if(action.equals("add category"))
            {
                Category newCat = new Category(mCategoryNameField.getText());
                mCommandPool.addCategory(newCat, selPath);
            }
        }
        else if(action.equals("add category"))
        {
            Category newCat = new Category(mCategoryNameField.getText());
            mCommandPool.addCategory(newCat, null);
        }
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        updateDisplay();
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        mCommandsTree.setSelectionPath(e.getTreePath());
        updateDisplay();
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        mCommandsTree.clearSelection();
        updateDisplay();
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        updateDisplay();
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        int selCount = mCommandsTree.getSelectionCount();
        
        mCommandButtonPanel.setEnabled(selCount == 1);
    }
    
    private boolean openFile()
    {
        int result = mFileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            File file = mFileChooser.getSelectedFile();
            
            if(file.exists())
            {
                boolean success = mCommandPool.loadFromFile(file);
                if(!success)
                {
                    JOptionPane.showMessageDialog(this, "Could not open the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return success;
            }
            else
            {
                JOptionPane.showMessageDialog(this, "The selected file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);    
                return false;
            }
        }
        else return false;
    }
    
    
    private boolean saveFile()
    {
        int result = mFileChooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            File file = mFileChooser.getSelectedFile();
            
            if(file.exists())
            {
                int overwrite = JOptionPane.showConfirmDialog(this, "Overwrite file \""+file.getName()+"\"?", "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
                if(overwrite == JOptionPane.NO_OPTION)
                    return false;
            }
            
            boolean success = mCommandPool.saveToFile(file);
            if(!success)
            {
                JOptionPane.showMessageDialog(this, "Could not save to the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;
            
        }
        else return false;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2)
            mBtn_send.doClick();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER)
            mBtn_send.doClick();
    }
    
}
