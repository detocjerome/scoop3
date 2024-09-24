package fr.ifremer.scoop3.infra.undo_redo.data;

import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;

public class QCValueChange extends UndoRedoAction {

    public static final int NO_NEW_QC = Integer.MIN_VALUE;
    /**
     * The QC after change
     */
    private int newQC;
    /**
     * The index of the Observation
     */
    private int observationIndex;
    /**
     * The "level" of the Observation
     */
    private final Integer observationLevel;
    /**
     * The Observation Id
     */
    private String obsId;
    /**
     * The old Manual Flag (for the Error message)
     */
    private int oldManualQC;
    /**
     * The QC before change
     */
    private int oldQC;
    /**
     * The parameter name
     */
    private String parameterName;
    /**
     * The parameter value
     */
    private final Number parameterValue;
    /**
     * The old parameter value
     */
    private Number oldParameterValue;
    /**
     * The parameter value to display
     */
    private final String parameterValueStr;
    /**
     * The value of the Reference parameter for this level
     */
    private final String refValueStr;
    /**
     * The code of the current platform
     */
    private final String ptfCode;

    /**
     * Default constructor
     *
     * @param observationIndex
     * @param observationLevel
     * @param oldQC
     * @param obsId
     * @param refValueStr
     * @param parameterName
     */
    public QCValueChange(final int observationIndex, final int observationLevel, final int oldQC, final int newQC,
	    final String obsId, final String parameterName, final Number parameterValue, final String parameterValueStr,
	    final String refValueStr, final String ptfCode) {
	this.observationIndex = observationIndex;
	this.observationLevel = observationLevel;
	this.oldQC = oldQC;
	this.newQC = newQC;
	this.obsId = obsId;
	this.parameterName = parameterName;
	this.parameterValue = parameterValue;
	this.oldParameterValue = parameterValue;
	this.parameterValueStr = parameterValueStr;
	this.refValueStr = refValueStr;
	this.ptfCode = ptfCode;
    }

    /**
     * @return the newQC
     */
    public int getNewQC() {
	return newQC;
    }

    /**
     * set the newQC value
     */
    public void setNewQC(final int newQC) {
	this.newQC = newQC;
    }

    /**
     * set the oldParameter value
     */
    public Number getOldParameterValue() {
	return this.oldParameterValue;
    }

    /**
     * set the oldParameter value
     */
    public void setOldParameterValue(final Number oldParameterValue) {
	this.oldParameterValue = oldParameterValue;
    }

    /**
     * @return the observationIndex
     */
    public int getObservationIndex() {
	return observationIndex;
    }

    /**
     * @return the observationLevel
     */
    public Integer getObservationLevel() {
	return observationLevel;
    }

    /**
     * @return the obsId
     */
    public String getObsId() {
	return obsId;
    }

    /**
     * @return the oldManualQC
     */
    public int getOldManualQC() {
	return oldManualQC;
    }

    /**
     * @return the oldQC
     */
    public int getOldQC() {
	return oldQC;
    }

    /**
     * @return the parameterName
     */
    public String getParameterName() {
	return parameterName;
    }

    /**
     * @return the parameterValue
     */
    public Number getParameterValue() {
	return parameterValue;
    }

    /**
     * @return the parameterValue
     */
    public String getParameterValueStr() {
	return parameterValueStr;
    }

    /**
     * @return the refValueStr
     */
    public String getRefValueStr() {
	return refValueStr;
    }

    public String getPlatformCode() {
	return this.ptfCode;
    }

    /**
     * @param observationIndex
     *            the observationIndex to set
     */
    public void setObservationIndex(final int observationIndex) {
	this.observationIndex = observationIndex;
    }

    /**
     * @param observationId
     *            the observationId to set
     */
    public void setObservationId(final String observationId) {
	this.obsId = observationId;
    }

    /**
     * /!\ -1 means NULL
     *
     * @param flagManual
     *            the oldManualQC to set
     */
    public void setOldManualQC(final int flagManual) {
	oldManualQC = flagManual;
    }

    public void setOldQC(final int oldQC) {
	this.oldQC = oldQC;
    }

    /**
     * Reset the old and the new QC
     */
    protected void resetNewAndOldQC() {
	setOldManualQC(-1);
	oldQC = NO_NEW_QC;
	newQC = NO_NEW_QC;
    }

    public void setParameterName(final String parameterName) {
	this.parameterName = parameterName;
    }

}
