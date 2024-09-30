package fr.ifremer.scoop3.gui.common.model;

import fr.ifremer.scoop3.model.QCValues;

public class SC3PropertyQCChangeEvent extends SC3PropertyChangeEvent {

    private static final long serialVersionUID = -6604843346258049455L;

    private final QCValues newQC;
    private final String obsRef;
    private final int refLevel;
    private final String variableName;

    public SC3PropertyQCChangeEvent(final String obsRef, final String variableName, final int refLevel,
	    final QCValues newQC) {
	super(variableName, EVENT_ENUM.CHANGE_QC);

	this.obsRef = obsRef;
	this.variableName = variableName;
	this.refLevel = refLevel;
	this.newQC = newQC;
    }

    /**
     * @return the newQC
     */
    public QCValues getNewQC() {
	return newQC;
    }

    /**
     * @return the obsRef
     */
    public String getObsRef() {
	return obsRef;
    }

    /**
     * @return the refLevel
     */
    public int getRefLevel() {
	return refLevel;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
	return variableName;
    }

}
