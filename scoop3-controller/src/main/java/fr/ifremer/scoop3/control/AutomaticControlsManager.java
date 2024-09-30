package fr.ifremer.scoop3.control;

import java.util.ArrayList;

import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.Sensor;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;
import fr.ifremer.scoop3.tools.ComputeSpeed;

public abstract class AutomaticControlsManager {

    /**
     * List of the automatic controls, check the presence of parameters
     */
    private final ArrayList<AutomaticControlCheckParametersPresence> automaticControlsCheckParametersPresence;

    /**
     * List of the automatic controls for Dataset Metadata
     */
    private final ArrayList<AutomaticControlForDatasetMetadata> automaticControlsForDatasetMetadata;

    /**
     * List of the automatic controls for Observation's Data
     */
    private final ArrayList<AutomaticControlForObservationData> automaticControlsForObservationData;

    /**
     * List of the automatic controls for Observation Metadata
     */
    private final ArrayList<AutomaticControlForObservationMetadata> automaticControlsForObservationMetadata;

    /**
     * List of the automatic controls for Observation's Reference Parameter
     */
    private final ArrayList<AutomaticControlForReferenceParameterData> automaticControlsForReferenceParameterData;

    /**
     * Default constructor (protected)
     */
    protected AutomaticControlsManager() {
	automaticControlsCheckParametersPresence = new ArrayList<>();
	automaticControlsForDatasetMetadata = new ArrayList<>();
	automaticControlsForObservationData = new ArrayList<>();
	automaticControlsForObservationMetadata = new ArrayList<>();
	automaticControlsForReferenceParameterData = new ArrayList<>();
    }

    /**
     * @return the automaticControlsCheckParametersPresence
     */
    public ArrayList<AutomaticControlCheckParametersPresence> getAutomaticControlsCheckParametersPresence() {
	return automaticControlsCheckParametersPresence;
    }

    /**
     * @return the automaticControlsForDataset
     */
    public ArrayList<AutomaticControlForDatasetMetadata> getAutomaticControlsForDatasetMetadata() {
	return automaticControlsForDatasetMetadata;
    }

    /**
     * @return the automaticControlsForObservationData
     */
    public ArrayList<AutomaticControlForObservationData> getAutomaticControlsForObservationData() {
	return automaticControlsForObservationData;
    }

    /**
     * @return the automaticControlsForStation
     */
    public ArrayList<AutomaticControlForObservationMetadata> getAutomaticControlsForObservationMetadata() {
	return automaticControlsForObservationMetadata;
    }

    /**
     * @return the automaticControlsForReferenceParameter
     */
    public ArrayList<AutomaticControlForReferenceParameterData> getAutomaticControlsForReferenceParameterData() {
	return automaticControlsForReferenceParameterData;
    }

    /**
     * Init all QCs
     *
     * @param dataset
     */
    public void initMetadataQCs(final Dataset dataset) {
	if (isInitMetadataQCsStartable()) {
	    initDatasetMetadataQCs(dataset);
	    initObservationsMetadataQCs(dataset);
	}
    }

    private void initDatasetMetadataQCs(final Dataset dataset) {
	// nothing to do here yet
	initSpecificDatasetMetadataQCs(dataset);
    }

    /**
     * Init observations metadata
     */
    private void initObservationsMetadataQCs(final Dataset dataset) {

	// for each Observations
	for (final Observation observation : dataset.getObservations()) {

	    initValueAndQC(observation.getOceanDepth());

	    initSpecificObservationMetadataQCs(observation);
	    initSpecificObservationQCs(observation);

	    initSensorMetadataQCs(observation.getSensor());
	}
    }

    /**
     * Init sensor metadata
     */
    private void initSensorMetadataQCs(final Sensor sensor) {
	initValueAndQC(sensor.getDistanceFromBottom());
	initValueAndQC(sensor.getNominalDepth());
    }

    /**
     * Add a new Automatic Control, check the parameters' presence
     *
     * @param accpp
     */
    protected void addAutomaticControlCheckParametersPresence(final AutomaticControlCheckParametersPresence accpp) {
	automaticControlsCheckParametersPresence.add(accpp);
    }

    /**
     * Add a new Automatic Control for Dataset
     *
     * @param acfdm
     */
    protected void addAutomaticControlForDatasetMetadata(final AutomaticControlForDatasetMetadata acfdm) {
	automaticControlsForDatasetMetadata.add(acfdm);
    }

    /**
     * Add a new Automatic Control for Data
     *
     * @param acfod
     */
    protected void addAutomaticControlForObservationData(final AutomaticControlForObservationData acfod) {
	automaticControlsForObservationData.add(acfod);
    }

    /**
     * Add a new Automatic Control for Observation
     *
     */
    protected void addAutomaticControlForObservationMetadata(final AutomaticControlForObservationMetadata acfom) {
	automaticControlsForObservationMetadata.add(acfom);
    }

    /**
     * Add a new Automatic Control for Observation's Reference Parameter
     *
     * @param acfrpd
     */
    protected void addAutomaticControlForReferenceParameterData(final AutomaticControlForReferenceParameterData acfrpd) {
	automaticControlsForReferenceParameterData.add(acfrpd);
    }

    /**
     * Clear all loaded controls
     */
    protected void clearControls() {
	ComputeSpeed.setPlatformSpeedLimit(null);
	automaticControlsCheckParametersPresence.clear();
	automaticControlsForDatasetMetadata.clear();
	automaticControlsForObservationData.clear();
	automaticControlsForObservationMetadata.clear();
	automaticControlsForReferenceParameterData.clear();
    }

    /**
     * Return the QC initialized
     *
     * @return
     */
    protected QCValues initQC(final QCValues qc) {
	QCValues toReturn = qc;
	if ((qc != null) && (qc != QCValues.QC_9)) {
	    toReturn = QCValues.QC_0;
	}
	return toReturn;
    }

    /**
     * Init specific dataset metadata. It depends on the data type (i.e. Medatlas data)
     *
     */
    protected abstract void initSpecificDatasetMetadataQCs(final Dataset dataset);

    /**
     * Init specific observation metadata. It depends on the data type (i.e. Medatlas data)
     *
     * @param observation
     */
    protected abstract void initSpecificObservationMetadataQCs(Observation observation);

    /**
     * Init specific Observation QCs. It depends on the data type (i.e. Medatlas data)
     *
     * @param observation
     */
    protected abstract void initSpecificObservationQCs(Observation observation);

    /**
     * Init a given ValueAndQC
     *
     * @param valueAndQC
     */
    protected void initValueAndQC(final ValueAndQC valueAndQC) {
	if (valueAndQC != null) {
	    valueAndQC.setQc(initQC(valueAndQC.getQc()));
	}
    }

    /**
     * @return true if the method initMetadataQCs do something. If false, the initMetadataQCs is ignored.
     */
    protected abstract boolean isInitMetadataQCsStartable();
}
