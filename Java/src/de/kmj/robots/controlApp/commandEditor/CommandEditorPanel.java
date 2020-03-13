package de.kmj.robots.controlApp.commandEditor;

import de.kmj.robots.controlApp.commandEditor.ParamPanel;
import de.kmj.robots.controlApp.commandEditor.StringParamPanel;
import de.kmj.robots.messaging.CommandMessage;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Map.Entry;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * GUI component for creating and sending CommandMessages.
 * <p>
 * Provides parameter templates for various known action types,
 * a special type "other..." for creating arbitrary CommandMessages
 * and a button that can be used to send the finished message.
 * <p>
 * To connect the button with a RobotEngine control application,
 * add a suitable {@link java.awt.event.ActionListener}
 * to the CommandEditorPanel. You can also set its action command as necessary.
 * 
 * @see de.kmj.robots.messaging.CommandMessage
 * @author Kathrin Janowski
 */
public class CommandEditorPanel extends JPanel implements ActionListener, KeyListener{
    private final DefaultCommands cDefaultCommands = new DefaultCommands();
    
    private int mTaskCounter;
    private final ArrayList<ParamPanel> mParameters;
    private String mCommandType;
    
    private final ComboBoxModel mTypeModel;
    private final JComboBox mTypeComboBox;
    
    private final StringParamPanel mTypePanel;
    private final JPanel mParameterPanel;
    private final JButton mSendButton;
            
    private final JPanel mCustomCommandsPanel;
    private final JButton mAddParameterBtn;
    
    public CommandEditorPanel()
    {
        super(new GridBagLayout());
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED),
                                   "command message editor"));
        setDoubleBuffered(true);
        mParameters = new ArrayList<ParamPanel>();
        mTaskCounter = 0;
            
        mTypeModel = new DefaultComboBoxModel();
        mTypeComboBox = new JComboBox(mTypeModel);
        
        for(String key: cDefaultCommands.getKeys())
            mTypeComboBox.addItem(key);        
        mTypeComboBox.addItem("other");
        mTypeComboBox.addActionListener(this);
        
        mSendButton = new JButton("send command");
        
        mCustomCommandsPanel = new JPanel();
        mAddParameterBtn = new JButton("add parameter");
        mAddParameterBtn.addActionListener(this);
        mCustomCommandsPanel.add(mAddParameterBtn);
        mCustomCommandsPanel.setVisible(false);
        
        mTypePanel = new StringParamPanel("type", "other", true);
        mTypePanel.setEnabled(false);
        
        mParameterPanel = new JPanel();
        mParameterPanel.setLayout(new GridBagLayout());
        mParameterPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        JScrollPane scrollPane = new JScrollPane(mParameterPanel);        
        
        //----------------------------------------------------------------------
        
        GridBagConstraints comboConstr = new GridBagConstraints();
        comboConstr.gridx=0;
        comboConstr.gridy=0;
        comboConstr.gridwidth=1;
        comboConstr.gridheight=1;
        comboConstr.weightx=1.0;
        comboConstr.weighty=0.1;
        comboConstr.fill= GridBagConstraints.HORIZONTAL;
        comboConstr.anchor=GridBagConstraints.CENTER;
        add(mTypeComboBox, comboConstr);
        
        GridBagConstraints sendConstr = new GridBagConstraints();
        sendConstr.gridx=1;
        sendConstr.gridy=0;
        sendConstr.gridwidth=1;
        sendConstr.gridheight=1;
        sendConstr.weightx=1.0;
        sendConstr.weighty=0.1;
        sendConstr.fill= GridBagConstraints.HORIZONTAL;
        sendConstr.anchor=GridBagConstraints.CENTER;
        add(mSendButton, sendConstr);
        
        GridBagConstraints typeConstr = new GridBagConstraints();
        typeConstr.gridx=0;
        typeConstr.gridy=1;
        typeConstr.gridwidth=2;
        typeConstr.gridheight=1;
        typeConstr.weightx=1.0;
        typeConstr.weighty=0.1;
        typeConstr.fill= GridBagConstraints.BOTH;
        typeConstr.anchor=GridBagConstraints.CENTER;
        add(mTypePanel, typeConstr);

        GridBagConstraints paramConstr = new GridBagConstraints();
        paramConstr.gridx=0;
        paramConstr.gridy=2;
        paramConstr.gridwidth=2;
        paramConstr.gridheight=5;
        paramConstr.weightx=1.0;
        paramConstr.weighty=1.0;
        paramConstr.fill= GridBagConstraints.BOTH;
        paramConstr.anchor=GridBagConstraints.CENTER;
        add(scrollPane, paramConstr);
        
        
        GridBagConstraints customConstr = new GridBagConstraints();
        customConstr.gridx=0;
        customConstr.gridy=8;
        customConstr.gridwidth=2;
        customConstr.gridheight=1;
        customConstr.weightx=1.0;
        customConstr.weighty=0.1;
        customConstr.fill= GridBagConstraints.BOTH;
        customConstr.anchor=GridBagConstraints.CENTER;
        add(mCustomCommandsPanel, customConstr);
        
        //----------------------------------------------------------------------
        mTypeComboBox.setSelectedItem("speech");
    }
    
    /**
     * Sets the action command for the "send command" button.
     * @param actionCommand the new action command for the button
     */
    public void setActionCommand(String actionCommand)
    {
        mSendButton.setActionCommand(actionCommand);
    }

    /**
     * Sets the text for the "send command" button.
     * @param text the new text for the button
     */
    public void setButtonText(String text)
    {
        mSendButton.setText(text);
    }

    
    /**
     * Adds an ActionListener to the "send command" button.
     * @param listener the ActionListener for the button
     */
    public void addActionListener(ActionListener listener)
    {
        mSendButton.addActionListener(listener);
    }

    
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        for(Component c: getComponents())
            c.setEnabled(enabled);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton)
        {
            if(e.getActionCommand().equals("add parameter"))
            {
                ParamPanel newPanel = new CustomParamPanel("param", "value", false);
                newPanel.addKeyListener(this);

                GridBagConstraints constraints = new GridBagConstraints();
        
                constraints.gridx=0;
                constraints.gridy=mParameters.size();
                constraints.gridwidth=2;
                constraints.gridheight=1;
                constraints.weightx=1.0;
                constraints.weighty=0.1;
                constraints.anchor=GridBagConstraints.PAGE_START;
                constraints.fill=GridBagConstraints.HORIZONTAL;

                mParameters.add(newPanel);
                mParameterPanel.add(newPanel, constraints);
                mParameterPanel.setSize(mParameterPanel.getPreferredSize());
            }
        }
        else
        {
            String type = (String)mTypeComboBox.getSelectedItem();
            CommandMessage cmd = cDefaultCommands.get(type);
            setCommand(cmd);
        }
    }
        
    /**
     * Creates a {@link de.kmj.robots.messaging.CommandMessage}
     * with a procedurally generated task ID
     * from the selected action type and the currently editable parameters. 
     * 
     * @return the current CommandMessage
     */
    public final CommandMessage getCommand()
    {
        return getCommand("task"+mTaskCounter);
    }
    
    /**
     * Creates a {@link de.kmj.robots.messaging.CommandMessage}
     * with a procedurally generated task ID
     * from the selected action type and the currently editable parameters. 
     * 
     * @param taskID the task ID to assign to this command
     * @return the current CommandMessage
     */
    public final CommandMessage getCommand(String taskID)
    {
        CommandMessage cmd;
                
        if(mCommandType != null)
        {
            String type = mTypePanel.getStringValue();
            
            cmd = new CommandMessage(taskID, type);
            mTaskCounter++;

            for(ParamPanel param: mParameters)
            {
                String value=param.getStringValue();
                if(value != null)
                {
                    String name=param.getParamName();
                    cmd.addParameter(name, value);
                }
            }
        }
        else
        {
            //there is only one "parameter": the message string
            if(mParameters.size()>0)
            {
                ParamPanel msgPanel = mParameters.get(0);
                cmd = new CommandMessage(msgPanel.getStringValue());
            }
            else return null;
        }
        return cmd;
    }
    
    
    public void setCommand(CommandMessage cmd)
    {
        mParameterPanel.removeAll();
        mParameters.clear();

        if(cmd == null)
        {
            mCommandType = "other";
            mTypePanel.setEnabled(true);
            mCustomCommandsPanel.setVisible(true);
        }
        else
        {
            mCommandType = cmd.getCommandType();
            mTypePanel.setEnabled(false);
            mCustomCommandsPanel.setVisible(false);
            for(Entry<String, String> param: cmd.getCommandParams().entrySet())
            {
                ParamPanel newPanel = new StringParamPanel(param.getKey(), param.getValue(), false);
                newPanel.addKeyListener(this);
                mParameters.add(newPanel);
            }
        }
        
        //if(!mTypeComboBox.getSelectedItem().equals(mCommandType))
        mTypePanel.setStringValue(mCommandType);
                
        
        //----------------------------------------------------------------------
        // display the parameters
        //----------------------------------------------------------------------
     
        for(int i=0; i<mParameters.size(); i++)
        {
            GridBagConstraints constraints = new GridBagConstraints();
        
            constraints.gridx=0;
            constraints.gridy=i;
            constraints.gridwidth=2;
            constraints.gridheight=1;
            constraints.weightx=1.0;
            constraints.weighty=0.1;
            constraints.anchor=GridBagConstraints.PAGE_START;
            constraints.fill=GridBagConstraints.HORIZONTAL;
            
            mParameterPanel.add(mParameters.get(i), constraints);
        }
        
        mParameterPanel.setSize(mParameterPanel.getPreferredSize());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            mSendButton.doClick();
        }        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }
    
}
