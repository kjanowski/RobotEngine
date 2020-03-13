package de.kmj.robots.controlApp;

import de.kmj.robots.controlApp.automation.AutomationPoolPanel;
import de.kmj.robots.controlApp.commandEditor.CommandEditorPanel;
import de.kmj.robots.controlApp.commandPool.CommandPoolPanel;
import de.kmj.robots.messaging.CommandMessage;
import de.kmj.robots.messaging.StatusMessage;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

/**
 * A Java Swing GUI for controling a remote RobotEngine.
 * 
 * @author Kathrin Janowski
 */
public class DefaultControlGUI extends JFrame implements ActionListener, WindowListener{
    
    private final DefaultControlApplication mApp;
    
    private final ConnectionEditorPanel mConnectPanel;
    private final CommandEditorPanel mCommandEditorPanel;
    private final StatusDisplayPanel mStatusPanel;
    private final CommandPoolPanel mCommandPoolPanel;
    private final AutomationPoolPanel mAutomationPanel;
    
    public DefaultControlGUI(DefaultControlApplication app)
    {
        super("RobotEngine Control Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(this);
        
        int minWidth = 600;
        setLocation(600, 0);    //TODO parameterize somehow... or center on the screen...
        mApp =app;
        
        //----------------------------------------------------------------------
        // connection settings
        //----------------------------------------------------------------------
        
        mConnectPanel = new ConnectionEditorPanel(mApp.getConfig());
        mConnectPanel.setActionCommand("connect");
        mConnectPanel.addActionListener(this);
        
        int connectPrefHeight = mConnectPanel.getPreferredSize().height;
        mConnectPanel.setMinimumSize(new Dimension(minWidth, connectPrefHeight));
        mConnectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                  connectPrefHeight));
        mConnectPanel.setSize(mConnectPanel.getPreferredSize());
        
        //----------------------------------------------------------------------
        // command input
        //----------------------------------------------------------------------
        
        mCommandEditorPanel = new CommandEditorPanel();
        mCommandEditorPanel.setActionCommand("command");
        mCommandEditorPanel.addActionListener(this);
        
        mCommandEditorPanel.setMinimumSize(new Dimension(minWidth, 300));
        mCommandEditorPanel.setSize(mCommandEditorPanel.getPreferredSize());
        
        //----------------------------------------------------------------------
        // command pool
        //----------------------------------------------------------------------
    
        mCommandPoolPanel = new CommandPoolPanel(mApp);
        mCommandPoolPanel.setMinimumSize(new Dimension(minWidth, 500));
        mCommandPoolPanel.setSize(mCommandPoolPanel.getPreferredSize());

        //----------------------------------------------------------------------
        // status output
        //----------------------------------------------------------------------
    
        mStatusPanel = new StatusDisplayPanel();
        mStatusPanel.setMinimumSize(new Dimension(minWidth, 110));
        mStatusPanel.setSize(mStatusPanel.getPreferredSize());

        //----------------------------------------------------------------------
        // automation
        //----------------------------------------------------------------------
        
        mAutomationPanel = new AutomationPoolPanel(mApp, mCommandEditorPanel);
        mAutomationPanel.setMinimumSize(new Dimension(minWidth, 200));
        mAutomationPanel.setSize(mAutomationPanel.getPreferredSize());
        
        //----------------------------------------------------------------------
        // frame layout
        //----------------------------------------------------------------------
        setLayout(new GridBagLayout());
        
        int connectH = mConnectPanel.getMinimumSize().height;
        int editorH = mCommandEditorPanel.getMinimumSize().height;
        int autoH = mAutomationPanel.getMinimumSize().height;
        int statusH= mStatusPanel.getMinimumSize().height;
        
        int totalHeight =   connectH + editorH + autoH + statusH;
        setMinimumSize(new Dimension(minWidth, totalHeight));

        int connectGridH = 1;
        int editorGridH = editorH/connectH;
        int autoGridH = autoH/connectH;
        int statusGridH = statusH/connectH;
        
        GridBagConstraints connectConstr = new GridBagConstraints();
        connectConstr.gridx=0;
        connectConstr.gridy=0;
        connectConstr.gridwidth=1;
        connectConstr.gridheight=connectGridH;
        connectConstr.weightx=1.0;
        connectConstr.weighty=1.0;
        connectConstr.fill=GridBagConstraints.HORIZONTAL;
        connectConstr.anchor=GridBagConstraints.PAGE_START;
        add(mConnectPanel, connectConstr);
                
        GridBagConstraints editorConstr = new GridBagConstraints();
        editorConstr.gridx=0;
        editorConstr.gridy=connectConstr.gridy+connectGridH;
        editorConstr.gridwidth=1;
        editorConstr.gridheight=editorGridH;
        editorConstr.weightx=1.0;
        editorConstr.weighty=1.0;  //use as much vertical space as possible
        editorConstr.fill=GridBagConstraints.BOTH;
        editorConstr.anchor=GridBagConstraints.PAGE_START;
        add(mCommandEditorPanel, editorConstr);
        
        GridBagConstraints autoConstr = new GridBagConstraints();
        autoConstr.gridx=0;
        autoConstr.gridy=editorConstr.gridy+editorGridH;
        autoConstr.gridwidth=1;
        autoConstr.gridheight=autoGridH;
        autoConstr.weightx=1.0;
        autoConstr.weighty=1.0; //use what is left
        autoConstr.fill=GridBagConstraints.BOTH;
        autoConstr.anchor=GridBagConstraints.PAGE_START;
        add(mAutomationPanel, autoConstr);
        
        GridBagConstraints poolConstr = new GridBagConstraints();
        poolConstr.gridx=1;
        poolConstr.gridy=0;
        poolConstr.gridwidth=1;
        poolConstr.gridheight=autoConstr.gridy+autoGridH;
        poolConstr.weightx=1.0;
        poolConstr.weighty=1.0;
        poolConstr.fill=GridBagConstraints.BOTH;
        poolConstr.anchor=GridBagConstraints.PAGE_START;
        add(mCommandPoolPanel, poolConstr);        

        GridBagConstraints statusConstr = new GridBagConstraints();
        statusConstr.gridx=0;
        statusConstr.gridy=autoConstr.gridy+autoConstr.gridheight;
        statusConstr.gridwidth=3;
        statusConstr.gridheight=statusGridH;
        statusConstr.weightx=1.0;
        statusConstr.weighty=1.0;
        statusConstr.fill=GridBagConstraints.BOTH;
        statusConstr.anchor=GridBagConstraints.PAGE_START;
        add(mStatusPanel, statusConstr);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if (cmd.equals("connect"))
        {
            mApp.connectTo(mConnectPanel.getLocalIP(),
                           mConnectPanel.getLocalPort(),
                           mConnectPanel.getRemoteIP(),
                           mConnectPanel.getRemotePort());
            mAutomationPanel.setBaseConnection(
                           mConnectPanel.getLocalIP(),
                           mConnectPanel.getLocalPort(),
                           mConnectPanel.getRemoteIP(),
                           mConnectPanel.getRemotePort());                 
        }
        else if(cmd.equals("command"))
        {
            CommandMessage msg = mCommandEditorPanel.getCommand();
            mApp.sendCommand(msg);
        }
    }
    
    public void displayConnectionStatus(String status)
    {
        mConnectPanel.displayConnectionStatus(status);
        
        boolean enabled = status.equals("connected");
        mCommandEditorPanel.setEnabled(enabled);
    }
    
    public void displayStatus(StatusMessage status)
    {
        mStatusPanel.showStatus(status);
    }
    
    public void copyCommand(CommandMessage command)
    {
        mCommandEditorPanel.setCommand(command);
    }
    
    public void addCommandToPool(CommandMessage command)
    {
        mCommandPoolPanel.addCommand(command);
    }

    //==========================================================================
    // window listener
    //==========================================================================
    
    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        mApp.exit();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
