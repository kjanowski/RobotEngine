/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.automation;

import de.kmj.robots.controlApp.ConnectionSetting;
import de.kmj.robots.controlApp.DefaultControlApplication;
import de.kmj.robots.controlApp.commandEditor.CommandEditorPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/**
 *
 * @author Kathrin
 */
public class AutomationPoolPanel extends JPanel implements ActionListener{
    private final DefaultControlApplication mApp;
    private final CommandEditorPanel mEditor;
    private ConnectionSetting mBaseConnection;
    
    private final AutomationPool mAutomationPool;
    private final TreeMap<String, AutomationThreadPanel> mThreadPanels;
    private final JPanel mThreadPanelsPanel;
    
    private final JButton mAddThreadBtn;
    private final JButton mDelThreadBtn;
    private final JButton mSaveThreadsBtn;
    private final JButton mLoadThreadsBtn;
    private final JTextField mNameTF;
    private final JTextField mPortTF;
    
    private final JFileChooser mFileChooser;
    
    public AutomationPoolPanel(DefaultControlApplication app, CommandEditorPanel editor)
    {
        super(new BorderLayout());
        setDoubleBuffered(true);
        
        mApp=app;
        setBorder(new TitledBorder(new EtchedBorder(), "Automation"));
        mEditor = editor;
        mAutomationPool = new AutomationPool();
        mThreadPanels = new TreeMap<String, AutomationThreadPanel>();
        mBaseConnection = null;
        mFileChooser = new JFileChooser();
        String baseDir = mApp.getConfig().getBaseDir();
        File baseDirFile = new File(baseDir);
        mFileChooser.setCurrentDirectory(baseDirFile);
        
        
        //----------------------------------------------------------------------
        JPanel creationPanel =new JPanel(new BorderLayout());
        
        JPanel infoPanel = new JPanel(new GridLayout(2,2));
        JLabel nameLabel = new JLabel("name:");
        infoPanel.add(nameLabel);     
        mNameTF = new JTextField("(unnamed)");
        infoPanel.add(mNameTF);
        JLabel portLabel = new JLabel("port:");
        infoPanel.add(portLabel);
        mPortTF = new JTextField("1234");
        infoPanel.add(mPortTF);
        
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        mAddThreadBtn = new JButton("add");
        mAddThreadBtn.addActionListener(this);
        buttonPanel.add(mAddThreadBtn);
        mDelThreadBtn = new JButton("delete");
        mDelThreadBtn.addActionListener(this);
        mDelThreadBtn.setEnabled(false);
        buttonPanel.add(mDelThreadBtn);
        mSaveThreadsBtn = new JButton("save...");
        mSaveThreadsBtn.addActionListener(this);
        buttonPanel.add(mSaveThreadsBtn);
        mLoadThreadsBtn = new JButton("load...");
        mLoadThreadsBtn.addActionListener(this);
        buttonPanel.add(mLoadThreadsBtn);
        
        creationPanel.add(infoPanel, BorderLayout.WEST);
        creationPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(creationPanel, BorderLayout.NORTH);
        
        //----------------------------------------------------------------------
        mThreadPanelsPanel = new JPanel();
        mThreadPanelsPanel.setLayout(new BoxLayout(mThreadPanelsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(mThreadPanelsPanel,
                                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if(action.equals("add")){
            addAutomation();
        }
        else if(action.equals("delete"))
        {
            deleteAutomation();
        }
        else if(action.equals("save...")){
            saveToFile();
        }
        else if(action.equals("load...")){
            loadFromFile();
        }
    }
    
    public void setBaseConnection(String localIP, int localPort, String remoteIP, int remotePort)
    {
        mBaseConnection = new ConnectionSetting(remoteIP, localIP, localPort, remoteIP, remotePort);
    
        for(AutomationThreadPanel panel: mThreadPanels.values())
        {
            AutomationThread thread = panel.getThread();
            if(thread != null) thread.connect(mBaseConnection);
        }
    }
    
    private void addAutomation(){
        if(mBaseConnection == null)
        {
            JOptionPane.showMessageDialog(this, "Please connect to an agent first.", "Not Connected", JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        
        int port;
        try{
            port = Integer.parseInt(mPortTF.getText());
        }catch(NumberFormatException nfe)
        {
            JOptionPane.showMessageDialog(this, "Please enter a valid port number.", "Illegal Port", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String name = mNameTF.getText();
        
        AutomationThread thread = new AutomationThread(name);
        thread.setPort(port);
        
        mAutomationPool.add(thread);
        createThreadPanel(thread);
        
        repaint();
    }
    
    private void deleteAutomation(){
        //TODO get selection
        
        //TODO remove the thread from the pool
        //TODO remove the corresponding panel
        //repaint
    }
    
    private boolean saveToFile(){
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
            
            boolean success = mAutomationPool.saveToFile(file);
            if(!success)
            {
                JOptionPane.showMessageDialog(this, "Could not save to the selected file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;
            
        }
        else return false;
    }
    
    private boolean loadFromFile(){
        
        if(mBaseConnection == null)
        {
            JOptionPane.showMessageDialog(this, "Please connect to an agent first.", "Not Connected", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        int result = mFileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            File file = mFileChooser.getSelectedFile();
            
            if(file.exists())
            {
                boolean success = mAutomationPool.loadFromFile(file);
                
                createThreadPanels();
                
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
    
    
    private void createThreadPanel(AutomationThread thread){
        thread.connect(mBaseConnection);
        thread.start();
        AutomationThreadPanel panel = new AutomationThreadPanel(mEditor, thread);
        mThreadPanels.put(thread.getName(), panel);
        mThreadPanelsPanel.add(panel);
    }
    
    private void createThreadPanels()
    {
        for(AutomationThreadPanel panel: mThreadPanels.values())
            mThreadPanelsPanel.remove(panel);
        
        mThreadPanels.clear();
        
        for(AutomationThread thread: mAutomationPool.getThreads())
            createThreadPanel(thread);
        
        repaint();
    }
}
