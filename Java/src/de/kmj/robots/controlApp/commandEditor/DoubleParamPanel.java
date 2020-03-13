package de.kmj.robots.controlApp.commandEditor;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Kathrin Janowski
 */
public class DoubleParamPanel extends ParamPanel{

    protected final JSpinner mInputSpinner;
    protected final SpinnerNumberModel mInputModel;
    
    public DoubleParamPanel(String paramName, double defaultValue, double minValue, double maxValue, boolean required)
    {
        super(paramName, Double.toString(defaultValue), "\\d+\\.\\d+", required);
        
        mInputModel = new SpinnerNumberModel(defaultValue, minValue, maxValue, 0.1);
        mInputSpinner = new JSpinner(mInputModel);
        
        addInputComponent(mInputSpinner);
    }
    
    
    @Override
    public String getStringValue() {
        double value = mInputModel.getNumber().doubleValue();
        return Double.toString(value);
    }
}
