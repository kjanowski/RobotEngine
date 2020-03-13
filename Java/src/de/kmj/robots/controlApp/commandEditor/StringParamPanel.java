package de.kmj.robots.controlApp.commandEditor;

import javax.swing.JTextArea;

/**
 *
 * @author Kathrin Janowski
 */
public class StringParamPanel extends ParamPanel{

    protected final JTextArea mInputField;
    
    public StringParamPanel(String paramName, String defaultValue, boolean required)
    {
        super(paramName, defaultValue, "\\S.*\\S", required);
        
        mInputField = new JTextArea(defaultValue);
        mInputField.setLineWrap(true);
        mInputField.setWrapStyleWord(true);
        addInputComponent(mInputField);
    }
    
    public void setStringValue(String value) {
        mInputField.setText(value);
    }    
    
    @Override
    public String getStringValue() {
        return mInputField.getText();
    }    
}
