package de.kmj.robots.controlApp.commandEditor;

import java.awt.GridBagConstraints;
import javax.swing.JTextArea;

/**
 *
 * @author Kathrin Janowski
 */
public class CustomParamPanel extends ParamPanel{

    protected final JTextArea mNameField;
    protected final JTextArea mInputField;
    
    public CustomParamPanel(String paramName, String defaultValue, boolean required)
    {
        super(paramName, defaultValue, "\\S.*\\S", required);
        mLabel.setText("=");
        
        mNameField = new JTextArea(defaultValue);
        mNameField.setLineWrap(false);
        mNameField.setText(paramName);
        
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx=0;
        constr.gridy=0;
        constr.gridwidth=1;
        constr.gridheight=1;
        constr.anchor = GridBagConstraints.LINE_START;
        constr.fill = GridBagConstraints.NONE;
        
        add(mNameField, constr);
        
        
        
        mInputField = new JTextArea(defaultValue);
        mInputField.setLineWrap(true);
        mInputField.setWrapStyleWord(true);
        addInputComponent(mInputField);
    }

    @Override
    public String getParamName() {
        return mNameField.getText();
    }        
    
    @Override
    public String getStringValue() {
        return mInputField.getText();
    }    
}
