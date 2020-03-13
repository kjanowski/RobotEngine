package de.kmj.robots.controlApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * GUI component for establishing a network connection.
 * <p>
 * Provides input fields for the network connection settings,
 * a button that can be used to establish the connection
 * and a label for displaying the connection status.
 * <p>
 * To connect the button with a RobotEngine control application,
 * add a suitable {@link java.awt.event.ActionListener}
 * to the ConnectionEditorPanel. You can also set its action command as necessary.
 * 
 * @author Kathrin Janowski
 */
public class ConnectionEditorPanel extends JPanel implements ActionListener{
    
    private final Config mAppConfig;
    
    private final JComboBox mPresetsList;
    private final JButton mBtn_addPreset;
    private final JButton mBtn_delPreset;
    
    private final JTextField mTextField_localIP;
    private final JTextField mTextField_localPort;
    private final JTextField mTextField_remoteIP;
    private final JTextField mTextField_remotePort;
    private final JButton mConnectButton;
    private final JLabel mStatusLabel;
    
    public ConnectionEditorPanel(Config appConfig)
    {
        super(new BorderLayout());
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED),
                                   "connection settings"));
        
        mAppConfig = appConfig;
        JPanel presetsPanel = new JPanel(new BorderLayout());
        mPresetsList = new JComboBox();
        mPresetsList.setEditable(true);
        for(ConnectionSetting setting: appConfig.getConnections())
            mPresetsList.addItem(setting.getName());
        mPresetsList.addActionListener(this);
        mPresetsList.setSelectedItem(null);
        presetsPanel.add(mPresetsList, BorderLayout.CENTER);
        
        mBtn_addPreset = new JButton("+");
        mBtn_addPreset.setActionCommand("addPreset");
        mBtn_addPreset.addActionListener(this);
        
        mBtn_delPreset = new JButton("-");
        mBtn_delPreset.setActionCommand("delPreset");
        mBtn_delPreset.addActionListener(this);
        
        JPanel addDelPanel = new JPanel(new GridLayout(1,2));
        addDelPanel.add(mBtn_addPreset);
        addDelPanel.add(mBtn_delPreset);
        presetsPanel.add(addDelPanel, BorderLayout.EAST);
        
        //----------------------------------------------------------------------
        
        JPanel textFieldPanel = new JPanel(new GridLayout(4,2));
        JLabel label_lhost = new JLabel("local IP:");
        mTextField_localIP = new JTextField();
        JLabel label_lport = new JLabel("local port:");
        mTextField_localPort = new JTextField();
        JLabel label_rhost = new JLabel("remote IP:");
        mTextField_remoteIP = new JTextField();
        JLabel label_rport = new JLabel("remote port:");
        mTextField_remotePort = new JTextField();
        
        textFieldPanel.add(label_lhost);
        textFieldPanel.add(label_lport);
        textFieldPanel.add(mTextField_localIP);
        textFieldPanel.add(mTextField_localPort);
        textFieldPanel.add(label_rhost);
        textFieldPanel.add(label_rport);
        textFieldPanel.add(mTextField_remoteIP);
        textFieldPanel.add(mTextField_remotePort);
        
        //----------------------------------------------------------------------
        
        JPanel btnPanel = new JPanel(new GridLayout(1,2));
        
        mConnectButton = new JButton("connect");
        mStatusLabel = new JLabel();
        mStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mStatusLabel.setOpaque(true);
        btnPanel.add(mConnectButton);
        btnPanel.add(mStatusLabel);
        
        //----------------------------------------------------------------------
        
        add(presetsPanel, BorderLayout.NORTH);
        add(textFieldPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        
        displayConnectionStatus("unknown");
    }

    
    /**
     * Adds an ActionListener to the "connect" button.
     * @param listener the ActionListener for the button
     */
    public void addActionListener(ActionListener listener)
    {
        mConnectButton.addActionListener(listener);
    }
    
    /**
     * Sets the action command for the "connect" button.
     * @param actionCommand the new action command for the button
     */
    public void setActionCommand(String actionCommand)
    {
        mConnectButton.setActionCommand(actionCommand);
    }
    
    
    public final String getLocalIP()
    {
        return mTextField_localIP.getText();
    }

    public int getLocalPort()
    {
        int result;
        try{
            result = Integer.parseInt(mTextField_localPort.getText());
        }
        catch(NumberFormatException nfe){
            result = 0;
        }
        return result;
    }

    public final String getRemoteIP()
    {
        return mTextField_remoteIP.getText();
    }

    public int getRemotePort()
    {
        int result;
        try{
            result = Integer.parseInt(mTextField_remotePort.getText());
        }
        catch(NumberFormatException nfe){
            result = 0;
        }
        return result;
    }

    void displayConnectionStatus(String status) {
        mStatusLabel.setText(status);
        
        if(status.equals("not connected"))
        {
            mStatusLabel.setBackground(Color.red);
        }
        else if(status.equals("connected"))
        {
            mStatusLabel.setBackground(Color.green);
        }
        else if(status.equals("connecting"))
        {
            mStatusLabel.setBackground(Color.yellow);
        }
        else if(status.equals("unknown"))
        {
            mStatusLabel.setBackground(Color.orange);
        }
        else mStatusLabel.setBackground(Color.lightGray);
    }

    private void addPreset()
    {
        String name = (String)mPresetsList.getEditor().getItem();
        
        ConnectionSetting old = mAppConfig.getConnection(name);
        if(old!=null)
        {
            int answer = JOptionPane.showConfirmDialog(this, "Overwrite preset \""+name+"\"?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.NO_OPTION)
                return;
        }
        
        String localIP = mTextField_localIP.getText();
        String localPortStr = mTextField_localPort.getText();
        String remoteIP = mTextField_remoteIP.getText();
        String remotePortStr = mTextField_remotePort.getText();
        
        try{
            int localPort = Integer.parseInt(localPortStr);
            int remotePort = Integer.parseInt(remotePortStr);
            
            ConnectionSetting setting=new ConnectionSetting(name, localIP, localPort, remoteIP, remotePort);
            mAppConfig.addConnection(setting);
            mPresetsList.addItem(setting.getName());
        }catch(NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "Please enter valid port numbers.", "Could not addd preset", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void deletePreset()
    {
        String name = (String)mPresetsList.getSelectedItem();
        
        ConnectionSetting old = mAppConfig.getConnection(name);
        if(old!=null)
        {
            int answer = JOptionPane.showConfirmDialog(this, "Delete preset \""+name+"\"?", "Confirm removal", JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION)
            {
                mAppConfig.removeConnection(name);
                mPresetsList.addItem(name);
            }
        }
    }
    
    
    private void loadPreset(String name)
    {
        ConnectionSetting setting = mAppConfig.getConnection(name);
        if(setting != null)
        {
            mTextField_localIP.setText(setting.getLocalIP());
            mTextField_localPort.setText(Integer.toString(setting.getLocalPort()));
            mTextField_remoteIP.setText(setting.getRemoteIP());
            mTextField_remotePort.setText(Integer.toString(setting.getRemotePort()));
            displayConnectionStatus("not connected");
        }
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JComboBox)
        {
            loadPreset((String)(mPresetsList.getSelectedItem()));
        }else{
            String action = e.getActionCommand();
            if(action.equals("addPreset"))
                addPreset();
            else if(action.equals("delPreset"))
                deletePreset();
        }
    }
    
}

