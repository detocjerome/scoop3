package fr.ifremer.scoop3.infra.undo_redo.metadata;

import fr.ifremer.scoop3.infra.undo_redo.data.DataValueChange;

public class MetadataValueChange extends DataValueChange {

    public enum QC_TO_UPDATE {
	DATASET_END_DATE, // Means it is a Metadata in the Dataset to update
	DATASET_METADATA, // The code of the first Platform
	DATASET_PTF_CODE, // The label of the first Platform
	DATASET_PTF_LABEL, // Dataset End date ...
	DATASET_REFERENCE, // Dataset reference ...
	DATASET_START_DATE, // Dataset Start date ...
	OBS_METADATA, // Means it is a Metadata in the Obs to update
	OBS_OCEAN_DEPTH, // QC to update is the attribute OceanDepth of the Obs
	OBS_SENSOR_DIST_FROM_BOTTOM, // QC to update is the attribute "NominalDepth" of the Sensor linked to the Obs
	OBS_SENSOR_NOMINAL_DEPTH, // QC to update is the "DistanceFromBottom" of the Sensor linked to the Obs
	OBS_SENSOR_SAMPLING_RATE, // QC to update is the "SamplingRateInSeconds" of the Sensor linked to the Obs
    }

    private final String errorMessageSuffix;
    private String metadata = null;
    private String newValue = null;
    private String oldValue = null;
    private final QC_TO_UPDATE qcToUpdate;

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final int observationIndex,
	    final String obsId, final String errorMessage) {
	super(observationIndex, 0, obsId, null, null, null, null, errorMessage);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.errorMessageSuffix = null;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final String errorMessageSuffix,
	    final int observationIndex, final String obsId, final int oldQC, final int newQC) {
	super(observationIndex, 0, oldQC, newQC, obsId);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.errorMessageSuffix = errorMessageSuffix;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final String errorMessageSuffix,
	    final String obsId, final String oldValue, final String newValue) {
	super(-1, 0, NO_NEW_QC, NO_NEW_QC, obsId, null, null, null, null);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.newValue = newValue;
	this.oldValue = oldValue;
	this.errorMessageSuffix = errorMessageSuffix;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final int observationIndex,
	    final int observationLevel, final String obsId, final String errorMessage) {
	super(observationIndex, observationLevel, obsId, null, null, null, null, errorMessage);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.errorMessageSuffix = null;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final String errorMessageSuffix,
	    final int observationIndex, final int observationLevel, final String obsId, final int oldQC,
	    final int newQC) {
	super(observationIndex, observationLevel, oldQC, newQC, obsId);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.errorMessageSuffix = errorMessageSuffix;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final String errorMessageSuffix,
	    final int observationIndex, final int observationLevel, final String obsId, final int oldQC,
	    final int newQC, final String refValueStr) {
	super(observationIndex, observationLevel, oldQC, newQC, obsId, refValueStr);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.errorMessageSuffix = errorMessageSuffix;
    }

    public MetadataValueChange(final QC_TO_UPDATE qcToUpdate, final String metadata, final String errorMessageSuffix,
	    final int observationLevel, final String obsId, final String oldValue, final String newValue) {
	super(-1, observationLevel, NO_NEW_QC, NO_NEW_QC, obsId, null, null, null, null);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
	this.newValue = newValue;
	this.oldValue = oldValue;
	this.errorMessageSuffix = errorMessageSuffix;
    }

    /**
     * @return the errorMessageSuffix
     */
    public String getErrorMessageSuffix() {
	if (errorMessageSuffix == null) {
	    return "";
	}
	return " (" + errorMessageSuffix + ")";
    }

    /**
     * @return the errorMessageSuffix
     */
    public String getErrorMessageSuffixOnly() {
	if (errorMessageSuffix == null) {
	    return "";
	}
	return errorMessageSuffix;
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
	return metadata;
    }

    /**
     * @return the newValue
     */
    public String getNewValue() {
	return newValue;
    }

    /**
     * @return the oldValue
     */
    public String getOldValue() {
	return oldValue;
    }

    public void setOldValue(final String oldValue) {
	this.oldValue = oldValue;
    }

    /**
     * @return the qcToUpdate
     */
    public QC_TO_UPDATE getQcToUpdate() {
	return qcToUpdate;
    }

    public String getVariableName() {
	return null;
    }

    public boolean isUpdateOnDatasetMetadata() {
	return false;
    }

    @Override
    public void resetNewAndOldQC() {
	super.resetNewAndOldQC();
    }
}
