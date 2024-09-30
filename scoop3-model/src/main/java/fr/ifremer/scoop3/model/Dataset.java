package fr.ifremer.scoop3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import fr.ifremer.scoop3.model.parameter.Variable;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.LongValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.StringValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public class Dataset extends Observable implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -78533710385979795L;

    /**
     * Corruption type enum
     */
    public enum CorruptionType {
	CORRUPTED_DIM, //
	CORRUPTED_QC
    }

    /**
     * Bounding box of the Dataset
     */
    private BoundingBox boundingBox;
    /**
     * Type of the dataset (profile, trajectory or timeserie)
     */
    private DatasetType datasetType;

    /**
     * Dataset end date. The End Date is a LongValueAndQC for the MetadataTable.
     */
    private LongValueAndQC endDate;

    /**
     * Dataset metadata
     */
    private final HashMap<String, ValueAndQC> metadata;

    /**
     * List of the Platform(s) to which observations are associated
     */
    private final ArrayList<Platform> platforms;

    /**
     * The Dataset's reference. The reference is a StringValueAndQC for the MetadataTable.
     */
    private StringValueAndQC reference;

    /**
     * The source type of the dataset
     */
    private String sourceType = null;

    /**
     * Dataset start date. The Start Date is a LongValueAndQC for the MetadataTable.
     */
    private LongValueAndQC startDate;

    /**
     * Identifiant unique du dataset utile - A sa mise en cache local
     */
    private String uri;

    private static Dataset instance;

    private static boolean datasetLoaded = false;

    private static boolean instanceReseted = false;

    private final Map<Observation, CorruptionType> corruptedObservationMap;

    private Map<String, HashMap<String, String>> parameterDataModeMap;

    private boolean contextIdError = false;
    private boolean noPtfContextId = false;
    private boolean accessToAllPtf = false;
    private ArrayList<String> availablePtf = null;

    /**
     * Default constructor
     */
    public Dataset() {
	platforms = new ArrayList<>();
	metadata = new HashMap<>();
	corruptedObservationMap = new HashMap<Observation, CorruptionType>();
	parameterDataModeMap = new HashMap<String, HashMap<String, String>>();
    }

    /**
     * @return an instance of Dataset
     */
    public static synchronized Dataset getInstance() {
	if (instance == null) {
	    instance = new Dataset();
	}

	return instance;
    }

    public static synchronized void setInstance(final Dataset dataset) {
	instance = dataset;
    }

    public static void resetInstance() {
	if (!getDatasetLoaded()) {
	    setInstanceReseted(true);
	}
	instance = null;
    }

    /**
     * Add a new Metadata
     *
     * @param key
     * @param value
     *
     */
    public void addMetadata(final String key, final ValueAndQC value) {
	metadata.put(key, value);
    }

    /**
     * Add a profiles to platform
     *
     * @param newPlatform
     * @param profiles
     *
     */
    public void addProfilesPlatform(final Platform newPlatform, final List<Profile> profiles) throws Exception {
	addProfilesPlatform(newPlatform, new Sensor(), profiles);
    }

    /**
     * Add a profiles to platform
     *
     * @param newPlatform
     * @param newSensor
     * @param profiles
     *
     */
    public void addProfilesPlatform(final Platform newPlatform, final Sensor newSensor, final List<Profile> profiles)
	    throws Exception {
	addObservationsPlatform(newPlatform, newSensor, profiles);
	setDatasetType(DatasetType.PROFILE);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param latitude
     * @param longitude
     * @param z
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final List<Timeserie> timeseries,
	    final DoubleValueAndQC latitude, final DoubleValueAndQC longitude, final DoubleValueAndQC z)
	    throws Exception {
	addTimeSeriesPlatform(newPlatform, timeseries, latitude, longitude, z, null, null, null);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param latitude
     * @param longitude
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final List<Timeserie> timeseries,
	    final DoubleValueAndQC latitude, final DoubleValueAndQC longitude) throws Exception {
	addTimeSeriesPlatform(newPlatform, timeseries, latitude, longitude, null, null, null, null);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final List<Timeserie> timeseries) throws Exception {
	addTimeSeriesPlatform(newPlatform, timeseries, null, null, null, null, null, null);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param latitude
     * @param longitude
     * @param z
     * @param latitudeVariable
     * @param longitudeVariable
     * @param zVariable
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final List<Timeserie> timeseries,
	    final DoubleValueAndQC latitude, final DoubleValueAndQC longitude, final DoubleValueAndQC z,
	    final Variable<Double> latitudeVariable, final Variable<Double> longitudeVariable,
	    final Variable<Double> zVariable) throws Exception {
	addTimeSeriesPlatform(newPlatform, new Sensor(), timeseries, latitude, longitude, z, latitudeVariable,
		longitudeVariable, zVariable);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param latitude
     * @param longitude
     * @param z
     * @param latitudeVariable
     * @param longitudeVariable
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final List<Timeserie> timeseries,
	    final DoubleValueAndQC latitude, final DoubleValueAndQC longitude, final Variable<Double> latitudeVariable,
	    final Variable<Double> longitudeVariable) throws Exception {
	addTimeSeriesPlatform(newPlatform, new Sensor(), timeseries, latitude, longitude, null, latitudeVariable,
		longitudeVariable, null);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param sensor
     * @param latitude
     * @param longitude
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final Sensor sensor, final List<Timeserie> timeseries,
	    final DoubleValueAndQC latitude, final DoubleValueAndQC longitude) throws Exception {
	addTimeSeriesPlatform(newPlatform, sensor, timeseries, latitude, longitude, null, null, null, null);
    }

    /**
     * Add a time series
     *
     * @param newPlatform
     * @param timeseries
     * @param sensor
     * @param latitude
     * @param longitude
     * @param z
     * @param latitudeVariable
     * @param longitudeVariable
     * @param zVariable
     *
     */
    public void addTimeSeriesPlatform(final Platform newPlatform, final Sensor newSensor,
	    final List<Timeserie> timeseries, final DoubleValueAndQC latitude, final DoubleValueAndQC longitude,
	    final DoubleValueAndQC z, final Variable<Double> latitudeVariable, final Variable<Double> longitudeVariable,
	    final Variable<Double> zVariable) throws Exception {

	if (timeseries != null) {
	    setDatasetType(DatasetType.TIMESERIE);

	    Platform platform = getPlatform(newPlatform.getCode());
	    if (platform == null) {
		platform = newPlatform;
		// It is possible to have multiple sensor for 1 Platform
		// } else {
		// throw new Exception("The platform (" + platform.getCode() + ") already exists in the dataset");
	    }
	    addPlatform(platform);

	    final Sensor sensor = platform.getSensor(newSensor);
	    platform.addSensor(sensor);

	    if (!timeseries.isEmpty()) {

		for (int index = 0; index < timeseries.size(); index++) {
		    final Timeserie ts = timeseries.get(index);

		    // Update time serie latitude with given latitude
		    if (latitude != null) {
			ts.getLatitude().getValues().clear();
			ts.getLatitude().getQcValues().clear();
			ts.getLatitude().addRecord(latitude.getValueAsDouble(), latitude.getQc());
		    }
		    if (latitudeVariable != null) {
			ts.getLatitude().setVariable(latitudeVariable);
		    }

		    // Update time serie longitude with given longitude
		    if (longitude != null) {
			ts.getLongitude().getValues().clear();
			ts.getLongitude().getQcValues().clear();
			ts.getLongitude().addRecord(longitude.getValueAsDouble(), longitude.getQc());
		    }
		    if (longitudeVariable != null) {
			ts.getLongitude().setVariable(longitudeVariable);
		    }

		    if (z != null) {
			ts.getZ().getValues().clear();
			ts.getZ().getQcValues().clear();
			ts.getZ().addRecord(z.getValueAsDouble(), z.getQc());
		    }
		    if (zVariable != null) {
			ts.getZ().setVariable(zVariable);
		    }

		    // Check if the Time Serie is correct
		    ts.updateDimension();
		    ts.checkDimensions();

		    if (!ts.isCorrupted()) {
			// Link TS to the Sensor
			sensor.addObservation(ts);
		    } else {
			addCorruptedObservation(ts, CorruptionType.CORRUPTED_DIM);
		    }
		}
	    }
	} else {
	    throw new Exception("timeseries MUST be NOT null");
	}
    }

    /**
     * Add a time series
     *
     * @param timeserie
     * @param latitudeValQc
     * @param longitudeValQc
     * @param depthValQc
     *
     */
    public void addTimeSerie(final Timeserie timeSerie, final DoubleValueAndQC latitudeValQc,
	    final DoubleValueAndQC longitudeValQc, final DoubleValueAndQC depthValQc) throws Exception {
	if (timeSerie != null) {
	    if (latitudeValQc != null) {
		timeSerie.getLatitude().getValues().clear();
		timeSerie.getLatitude().getQcValues().clear();
		timeSerie.getLatitude().addRecord(latitudeValQc.getValueAsDouble(), latitudeValQc.getQc());
	    }
	    if (longitudeValQc != null) {
		timeSerie.getLongitude().getValues().clear();
		timeSerie.getLongitude().getQcValues().clear();
		timeSerie.getLongitude().addRecord(longitudeValQc.getValueAsDouble(), longitudeValQc.getQc());
	    }
	    if (depthValQc != null) {
		timeSerie.getZ().getValues().clear();
		timeSerie.getZ().getQcValues().clear();
		timeSerie.getZ().addRecord(depthValQc.getValueAsDouble(), depthValQc.getQc());

	    }
	    // Check if the Time Serie is correct
	    timeSerie.updateDimension();
	    timeSerie.checkDimensions();
	    // Link TS to the Sensor
	    final Sensor sensor = getPlatforms().get(0).getSensors().get(0);
	    sensor.addObservation(timeSerie);

	} else {
	    throw new Exception("timeserie MUST be NOT null");
	}

    }

    /**
     * Add a trajectory
     *
     * @param platform
     * @param trajectories
     *
     */
    public void addTrajectoriesPlatform(final Platform platform, final List<Trajectory> trajectories) throws Exception {
	addTrajectoriesPlatform(platform, new Sensor(), trajectories);
    }

    /**
     * Add a trajectory
     *
     * @param platform
     * @param newSensor
     * @param trajectories
     *
     */
    public void addTrajectoriesPlatform(final Platform newPlatform, final Sensor newSensor,
	    final List<Trajectory> trajectories) throws Exception {
	addObservationsPlatform(newPlatform, newSensor, trajectories);
	setDatasetType(DatasetType.TRAJECTORY);
    }

    /**
     * @return the boundingBox
     */
    public BoundingBox getBoundingBox() {
	return boundingBox;
    }

    /**
     * @return the datasetType
     */
    public DatasetType getDatasetType() {
	return datasetType;
    }

    /**
     * @return the endDate
     */
    public LongValueAndQC getEndDate() {
	return endDate;
    }

    /**
     * Get the metadata associated to the given key
     *
     * @param key
     * @return
     */
    public ValueAndQC getMetadata(final String key) {
	return metadata.get(key);
    }

    /**
     * Get the metadata associated to the given key
     *
     * @param key
     * @return
     */
    public HashMap<String, ValueAndQC> getMetadatas() {
	return metadata;
    }

    /**
     * @return a copy of this dataset, but without any Observation, Sensor and Platform
     */
    public Dataset getNewEmptyDataset() {
	final Dataset newDataset = new Dataset();

	// Copy info of the Dataset
	newDataset.setBoundingBox(getBoundingBox());
	newDataset.setDatasetType(getDatasetType());
	newDataset.setEndDate(new Date(getEndDate().getValueAsLong()));

	/*
	 * Copy Metadata
	 */
	for (final String metedataKey : metadata.keySet()) {
	    newDataset.addMetadata(metedataKey, getMetadata(metedataKey));
	}

	newDataset.setReference(getReference().getValueAsString());
	newDataset.setStartDate(new Date(getStartDate().getValueAsLong()));

	newDataset.setSourceType(getSourceType());

	return newDataset;
    }

    /**
     * @return all observations
     */
    public synchronized List<Observation> getObservations() {
	if (platforms.size() == 1) {
	    return platforms.get(0).getAllObservations();
	} else {
	    final List<Observation> observations = new ArrayList<>();
	    final ArrayList<Platform> tempPlatforms = new ArrayList<>(platforms);
	    for (final Platform platform : tempPlatforms) {
		observations.addAll(platform.getAllObservations());
	    }
	    return observations;
	}
    }

    /**
     *
     * @param platformCode
     * @return all observations linked to the platform
     */
    public synchronized List<Observation> getObservationsByPlatform(final String platformCode) {
	final Platform platform = getPlatform(platformCode);
	if (platform != null) {
	    return platform.getAllObservations();
	}
	return null;
    }

    /**
     * Get an observation in the dataset from its Id
     *
     * @param dataset
     * @param obsRef
     * @return
     */
    public synchronized Observation getObservationWithRef(final String obsRef, final String obsPtf) {
	for (final Observation observation : getObservations()) {
	    if (!obsPtf.equals("")) {
		if ((observation.getReference().equals(obsRef) || observation.getId().equals(obsRef))
			&& observation.getSensor().getPlatform().getCode().equals(obsPtf)) {
		    return observation;
		}
	    } else {
		if (observation.getReference().equals(obsRef) || observation.getId().equals(obsRef)) {
		    return observation;
		}
	    }
	}
	return null;
    }

    /**
     * @return the name of parameters contained in all dataset's observations
     */
    public synchronized List<String> getParametersNames() {
	final List<String> names = new ArrayList<String>();

	for (final Platform platform : platforms) {
	    for (final Observation obs : platform.getAllObservations()) {
		for (final String name : obs.getOceanicParameters().keySet()) {
		    if (!names.contains(/* obs.getOceanicParameters() */name)) {
			names.add(name);
		    }
		}
	    }
	}

	return names;
    }

    /**
     * @param paramName
     * @return all dataset's observations with contain a given parameter
     */
    public synchronized List<Observation> getObservationsByParameter(final String paramName) {
	final List<Observation> observations = new ArrayList<Observation>();
	for (final Observation obs : getObservations()) {
	    for (final String name : obs.getOceanicParameters().keySet()) {
		if (name.equals(paramName)) {
		    observations.add(obs);
		}
	    }
	}
	return observations;
    }

    /**
     * Get the Platform object from its code
     *
     * @param platformCode
     * @return the Platform
     */
    public synchronized Platform getPlatform(final String platformCode) {
	Platform platformToReturn = null;
	for (final Platform platform : platforms) {
	    if (platform.getCode().equals(platformCode)) {
		platformToReturn = platform;
		break;
	    }
	}
	return platformToReturn;
    }

    /**
     * @return all platforms
     */
    public List<Platform> getPlatforms() {
	return platforms;
    }

    /**
     * @return the reference
     */
    public StringValueAndQC getReference() {
	return reference;
    }

    /**
     * @return the sourceType
     */
    public String getSourceType() {
	return sourceType;
    }

    /**
     * @return the startDate
     */
    public LongValueAndQC getStartDate() {
	return startDate;
    }

    public String getURI() {
	return uri;
    }

    /**
     * @param boundingBox
     *            the boundingBox to set
     */
    public void setBoundingBox(final BoundingBox boundingBox) {
	this.boundingBox = boundingBox;
    }

    /**
     * @param datasetType
     *            the datasetType to set
     */
    public void setDatasetType(final DatasetType datasetType) {
	this.datasetType = datasetType;
    }

    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate(final Date endDate) {
	this.endDate = new LongValueAndQC(endDate.getTime(), QCValues.QC_0);
    }

    /**
     * @param reference
     *            the reference to set
     */
    public void setReference(final String reference) {
	if (this.reference == null) {
	    this.reference = new StringValueAndQC(reference, QCValues.QC_0);
	} else {
	    this.reference.setValue(reference);
	}
    }

    /**
     * @param sourceType
     *            the sourceType to set
     */
    public void setSourceType(final String sourceType) {
	this.sourceType = sourceType;
    }

    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate(final Date startDate) {
	this.startDate = new LongValueAndQC(startDate.getTime(), QCValues.QC_0);
    }

    public void setURI(final String datasetURI) {
	uri = datasetURI;

    }

    protected void addObservationsPlatform(final Platform newPlatform, final Sensor newSensor,
	    final List<? extends Observation> observations) throws Exception {
	if (observations != null) {
	    Platform platform = getPlatform(newPlatform.getCode());
	    if (platform == null) {
		platform = newPlatform;
		// It is possible to have multiple sensor for 1 Platform
		// } else {
		// throw new Exception("The platform (" + platform.getCode() + ") already exists in the dataset");
	    }
	    addPlatform(platform);

	    final Sensor sensor = platform.getSensor(newSensor);
	    platform.addSensor(sensor);

	    if (!observations.isEmpty()) {
		for (final Observation observation : observations) {
		    // add refrence to observations if equals null
		    if (observation.getReference() == null) {
			if (observation.getId() != null) {
			    observation.setReference(platform.getCode() + " - " + observation.getId());
			} else {
			    observation.setReference(platform.getCode());
			}
		    }
		    // Check if the Observation is correct
		    observation.updateDimension();
		    observation.checkDimensions();
		    if (!observation.isCorrupted()) {
			// Link Observation to the Sensor
			sensor.addObservation(observation);
		    } else {
			addCorruptedObservation(observation, CorruptionType.CORRUPTED_DIM);
		    }
		}

	    }
	} else {
	    throw new Exception("trajectories MUST be NOT null");
	}
    }

    /**
     * add an observation to the dataset
     *
     * @throws Exception
     */
    public void addPlatform(final Platform platform) {
	if (!platforms.contains(platform)) {
	    platforms.add(platform);
	}
    }

    /**
     * remove an observation from the dataset
     *
     * @param pObservation
     *            observation
     */
    public synchronized void removeObservation(final Observation pObservation) {
	for (final Platform platform : getPlatforms()) {
	    if (platform.getAllObservations().contains(pObservation)) {
		for (final Sensor sensor : platform.getSensors()) {
		    if (sensor.getObservations().contains(pObservation)) {
			sensor.removeObservation(pObservation);
		    }
		}
	    }
	}
    }

    public void setDatasetChanged() {
	setChanged();
    }

    public static boolean getDatasetLoaded() {
	return datasetLoaded;
    }

    public static void setDatasetLoaded(final boolean datasetLoaded) {
	Dataset.datasetLoaded = datasetLoaded;
    }

    public Map<String, HashMap<String, String>> getParameterDataModeMap() {
	return this.parameterDataModeMap;
    }

    public void setParameterDataModeMap(final Map<String, HashMap<String, String>> parameterDataModeMap) {
	this.parameterDataModeMap = parameterDataModeMap;
    }

    public Map<Observation, CorruptionType> getCorruptedObservationMap() {
	return this.corruptedObservationMap;
    }

    /**
     * Add observation to the corrupted obs map
     *
     * @param observation
     * @param corruptionType
     */
    public void addCorruptedObservation(final Observation observation, final CorruptionType corruptionType) {
	if (!corruptedObservationMap.containsKey(observation)) {
	    corruptedObservationMap.put(observation, corruptionType);
	}
    }

    public static boolean isInstanceReseted() {
	return instanceReseted;
    }

    public static void setInstanceReseted(final boolean instanceReseted) {
	Dataset.instanceReseted = instanceReseted;
    }

    public boolean getContextIdError() {
	return contextIdError;
    }

    public void setContextIdError(final boolean contextIdError) {
	this.contextIdError = contextIdError;
    }

    public boolean getNoPtfContextId() {
	return noPtfContextId;
    }

    public void setNoPtfContextId(final boolean noPtfContextId) {
	this.noPtfContextId = noPtfContextId;
    }

    public boolean getAccessToAllPtf() {
	return accessToAllPtf;
    }

    public void setAccessToAllPtf(final boolean accessToAllPtf) {
	this.accessToAllPtf = accessToAllPtf;
    }

    public ArrayList<String> getAvailablePtf() {
	return availablePtf;
    }

    public void setAvailablePtf(final ArrayList<String> availablePtf) {
	this.availablePtf = availablePtf;
    }
}
