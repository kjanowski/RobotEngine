package de.kmj.robots.controlApp.commandEditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provides editing controls for a single
 * {@link de.hcm.robots.messaging.CommandMessage} parameter.
 * 
 * @author Kathrin Janowski
 */
public abstract class ParamPanel extends JPanel{
    
    protected String mParamName;
    protected JLabel mLabel;
    protected final String mDefaultValue;
    protected final Pattern mValidationPattern;
    protected final boolean mRequired;
    protected Component mInputComponent;
    
    public ParamPanel(String paramName, String defaultValue, String valiationPattern, boolean required)
    {
        super(new GridBagLayout());
        
        mParamName = paramName;
        mDefaultValue = defaultValue;
        mValidationPattern = Pattern.compile(valiationPattern);
        mRequired = required;
        
        String labelText = paramName+"=";
        if(mRequired)
            labelText="* "+labelText;
        mLabel = new JLabel(labelText);
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx=1;
        constr.gridy=0;
        constr.gridwidth=1;
        constr.gridheight=1;
        constr.anchor = GridBagConstraints.LINE_START;
        constr.fill = GridBagConstraints.NONE;
        
        add(mLabel, constr);
        
        mInputComponent=null;
    }
    
    protected final void addInputComponent(Component input)
    {
        mInputComponent=input;
        GridBagConstraints constr = new GridBagConstraints();
        constr.gridx=2;
        constr.gridy=0;
        constr.gridwidth=3;
        constr.gridheight=1;
        constr.weightx = 0.75;
        constr.anchor = GridBagConstraints.LINE_START;
        constr.fill = GridBagConstraints.HORIZONTAL;
        
        add(mInputComponent, constr);
    }
    
    @Override
    public void addKeyListener(KeyListener listener)
    {
       if(mInputComponent == null)
       {
           System.err.println("no suitable Input Component for adding the listener");
           return;
       }    
       
       mInputComponent.addKeyListener(listener);
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        for(Component c: getComponents())
            c.setEnabled(enabled);
    }

    
    public String getParamName()
    {
        return mParamName;
    }
    
    public final boolean isRequired()
    {
        return mRequired;
    }
    
    public final boolean isParameterValid()
    {
        String value = getStringValue();
        if(value == null)
        {
            return !mRequired;
        }
        else
        {
            Matcher matcher = mValidationPattern.matcher(value);
            return matcher.matches();
        }
    }
    
    public abstract String getStringValue();
}
