/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.automation;

import de.kmj.robots.controlApp.commandEditor.CommandEditorPanel;
import de.kmj.robots.messaging.CommandMessage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Kathrin
 */
public class AutomationThreadPanel extends JPanel implements ActionListener, FocusListener, KeyListener{
    
    private final CommandEditorPanel mEditor;
    private Color mDefaultColor;
    private Color mHighlightColor;
    
    private AutomationThread mThread;
    
    private final ImageIcon mPlayIcon;
    private final ImageIcon mPauseIcon;
    
    private JButton mPlayPauseBtn;
    private boolean mPlaying;
    
    private JList mCommandList;
    private DefaultListModel mCommandModel;
    private JButton mAddCommandBtn;
    private JButton mDelCommandBtn;
    
    private JCheckBox mRandomizeCB;
    private JTextField mMinDelayTF;
    private JTextField mMaxDelayTF;
    
    public AutomationThreadPanel(CommandEditorPanel editor, AutomationThread thread)
    {
        super(new BorderLayout());
        
        mEditor = editor;
        
        mThread = thread;
        setBorder(new TitledBorder(new EtchedBorder(), mThread.getName()));
        
        mDefaultColor = Color.lightGray;
        mHighlightColor = mDefaultColor.brighter();
        
        //----------------------------------------------------------------------
        JPanel playPanel= new JPanel(new GridLayout(2,1));
        
        URL playURL = getClass().getResource("/icons/play.png");
        mPlayIcon = new ImageIcon(playURL);
        URL pauseURL = getClass().getResource("/icons/pause.png");
        mPauseIcon = new ImageIcon(pauseURL);
        
        mPlayPauseBtn = new JButton("");
        if(mThread.isActive())
            mPlayPauseBtn.setIcon(mPauseIcon);
        else mPlayPauseBtn.setIcon(mPlayIcon);
        
        mPlayPauseBtn.setActionCommand("togglePause");
        mPlayPauseBtn.addActionListener(this);
        playPanel.add(mPlayPauseBtn);
        
        mRandomizeCB = new JCheckBox("random", false);
        mRandomizeCB.addActionListener(this);
        mRandomizeCB.setSelected(mThread.getRandomize());
        playPanel.add(mRandomizeCB);
        add(playPanel, BorderLayout.WEST);
    
        //----------------------------------------------------------------------
        mCommandModel = new DefaultListModel();
        mCommandList = new JList(mCommandModel);
        mCommandList.setMinimumSize(new Dimension(500, 50));
        mCommandList.setMaximumSize(new Dimension(500, 100));
        
        ArrayList<CommandMessage> cmds = mThread.getCommands();
        for(int i=0; i<cmds.size(); i++)
            mCommandModel.addElement(cmds.get(i));
            
        JScrollPane scrollPane = new JScrollPane(mCommandList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel mCommandPanel = new JPanel(new BorderLayout());
        mCommandPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel mCmdButtonPanel = new JPanel(new GridLayout(2,1));
        mAddCommandBtn = new JButton("+");
        mAddCommandBtn.addActionListener(this);
        mCmdButtonPanel.add(mAddCommandBtn);
        mDelCommandBtn = new JButton("-");
        mDelCommandBtn.addActionListener(this);
        mCmdButtonPanel.add(mDelCommandBtn);
        mCommandPanel.add(mCmdButtonPanel, BorderLayout.WEST);
        
        add(mCommandPanel, BorderLayout.EAST);
        
        //----------------------------------------------------------------------
        JPanel timePanel = new JPanel(new GridLayout(2,2));
        
        JLabel minLabel = new JLabel("min:");
        timePanel.add(minLabel);
        mMinDelayTF = new JTextField(Integer.toString(mThread.getMinDelay()));
        mMinDelayTF.addFocusListener(this);
        mMinDelayTF.addKeyListener(this);
        timePanel.add(mMinDelayTF);
        
        JLabel maxLabel = new JLabel("max:");
        timePanel.add(maxLabel);
        mMaxDelayTF = new JTextField(Integer.toString(mThread.getMaxDelay()));
        mMaxDelayTF.addFocusListener(this);
        mMaxDelayTF.addKeyListener(this);
        timePanel.add(mMaxDelayTF);
        add(timePanel, BorderLayout.CENTER);
    }
    
    public void setDefaultColor(Color color){
        mDefaultColor = color;
    }
    
    public void setHighlightColor(Color color){
        mHighlightColor = color;
    }

    public AutomationThread getThread()
    {
        return mThread;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if(action.equals("+"))
        {
            addNewCommand();
        }else if(action.equals("-"))
        {
            deleteCommand();
        }else if(action.equals("togglePause"))
        {
            mPlaying = !mThread.isActive();
            mThread.setActive(mPlaying);
            
            //display current state
            if(mThread.isActive())
                mPlayPauseBtn.setIcon(mPauseIcon);
            else mPlayPauseBtn.setIcon(mPlayIcon);
        }else if(action.equals("random"))
        {
            mThread.setRandomize(mRandomizeCB.isSelected());
        }
    }
    
    
    private void addNewCommand(){
        CommandMessage cmd = mEditor.getCommand();
        boolean added = mThread.addCommand(cmd);
        
        if(added)
            mCommandModel.addElement(cmd);
    }
    
    private void deleteCommand(){
        int idx = mCommandList.getSelectedIndex();
        
        boolean removed = mThread.removeCommand(idx);
        if(removed)
            mCommandModel.remove(idx);
    }

    private void updateDelay(Object source){
        if(source == mMinDelayTF)
        {
            int value;
            try{
                value = Integer.parseInt(mMinDelayTF.getText());
                value = mThread.setMinDelay(value);
            }catch(NumberFormatException nfe)
            {
                value = mThread.getMinDelay(); //reset to current value
            }
            
            mMinDelayTF.setText(Integer.toString(value));
        }
        else if(source == mMaxDelayTF)
        {
            int value;
            try{
                value = Integer.parseInt(mMaxDelayTF.getText());
                value = mThread.setMaxDelay(value);
            }catch(NumberFormatException nfe)
            {
                value = mThread.getMaxDelay(); //reset to current value
            }
            
            mMaxDelayTF.setText(Integer.toString(value));
        }
    }
    
    public void highlight(boolean selected){
        if(selected)
            setBackground(mHighlightColor);
        else setBackground(mDefaultColor);
    }
    
    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(e.getSource() instanceof JTextField)
            updateDelay(e.getSource());
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
            if(e.getSource() instanceof JTextField)
                updateDelay(e.getSource());
    }
    
    
    
}
