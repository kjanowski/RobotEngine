package de.kmj.robots.controlApp.commandEditor;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Kathrin Janowski
 */
public class IntegerParamPanel extends ParamPanel{

    protected final JSpinner mInputSpinner;
    protected final SpinnerNumberModel mInputModel;
    
    public IntegerParamPanel(String paramName, int defaultValue, int minValue, int maxValue, boolean required)
    {
        super(paramName, Integer.toString(defaultValue), "\\d+", required);
        
        mInputModel = new SpinnerNumberModel(defaultValue, minValue, maxValue, 1);
        mInputSpinner = new JSpinner(mInputModel);
        
        addInputComponent(mInputSpinner);
    }
    
    
    @Override
    public String getStringValue() {
        int value = mInputModel.getNumber().intValue();
        return Integer.toString(value);
    }
}
