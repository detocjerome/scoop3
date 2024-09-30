package fr.ifremer.scoop3.gui.common.model;

public class SC3PropertyCommentChangeEvent extends SC3PropertyChangeEvent {

    private static final long serialVersionUID = 3409037761172287103L;

    private final String errorMessage;
    private final String newValue;
    private final String obsRef;
    private final String oldValue;
    private final int refLevel;
    private final String refValStr;
    private final String variableName;
    private final Number variableValue;
    private final String variableValueStr;

    public SC3PropertyCommentChangeEvent(final String obsRef, final String variableName, final Number variableValue,
	    final String variableValueStr, final int refLevel, final String refValStr, final String errorMessage,
	    final String newValue, final String oldValue) {
	super(variableName, EVENT_ENUM.CHANGE_COMMENT);

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
    public String getNewValue() {
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
    public String getOldValue() {
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
    public Number variableValue() {
	return variableValue;
    }

    /**
     * @return the variableValueStr
     */
    public String variableValueStr() {
	return variableValueStr;
    }

}
