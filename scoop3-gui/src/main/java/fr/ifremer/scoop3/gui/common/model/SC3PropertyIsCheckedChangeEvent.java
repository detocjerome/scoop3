package fr.ifremer.scoop3.gui.common.model;

public class SC3PropertyIsCheckedChangeEvent extends SC3PropertyChangeEvent {

    private static final long serialVersionUID = 2328996348138089946L;

    private final String errorMessage;
    private final Boolean newValue;
    private final String obsRef;
    private final Boolean oldValue;
    private final int refLevel;
    private final String refValStr;
    private final String variableName;
    private final Number variableValue;
    private final String variableValueStr;

    /**
     * @param variableName
     * @param refLevel
     * @param refValStr
     * @param newValue
     */
    public SC3PropertyIsCheckedChangeEvent(final String obsRef, final String variableName, final Number variableValue,
	    final String variableValueStr, final int refLevel, final String refValStr, final String errorMessage,
	    final Boolean newValue, final Boolean oldValue) {
	super(variableName, EVENT_ENUM.CHANGE_IS_CHECKED);
	this.obsRef = obsRef;
	this.variableName = variableName;
	this.variableValue = variableValue;
	this.variableValueStr = variableValueStr;
	this.refLevel = refLevel;
	this.refValStr = refValStr;
	this.errorMessage = errorMessage;
	this.newValue = newValue;
	this.oldValue = oldValue;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * @return the newValue
     */
    @Override
    public Boolean getNewValue() {
	return newValue;
    }

    /**
     * @return the obsRef
     */
    public String getObsRef() {
	return obsRef;
    }

    /**
     * @return the oldValue
     */
    @Override
    public Boolean getOldValue() {
	return oldValue;
    }

    /**
     * @return the refLevel
     */
    public int getRefLevel() {
	return refLevel;
    }

    /**
     * @return the refValStr
     */
    public String getRefValStr() {
	return refValStr;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
	return variableName;
    }

    /**
     * @return the variableValue
     */
    public Number getVariableValue() {
	return variableValue;
    }

    /**
     * @return the variableValueStr
     */
    public String getVariableValueStr() {
	return variableValueStr;
    }

}
