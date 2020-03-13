package de.kmj.robots.controlApp.commandEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 *
 * @author Kathrin Janowski
 */
public class BooleanParamPanel extends ParamPanel implements ActionListener{

    protected final JToggleButton mInputButton;
    
    public BooleanParamPanel(String paramName, boolean defaultValue, boolean required)
    {
        super(paramName, Boolean.toString(defaultValue), "(true)|(false)", required);
        
        mInputButton = new JToggleButton(mDefaultValue);
        mInputButton.setHorizontalAlignment(SwingConstants.LEADING);
        //actionPerformed(null);
        mInputButton.addActionListener(this);
        mInputButton.setSelected(defaultValue);
                
        addInputComponent(mInputButton);
    }
    
    @Override
    public String getStringValue() {
        return mInputButton.getText();
    }    

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = Boolean.toString(mInputButton.isSelected());
        mInputButton.setText(text);
    }
}
