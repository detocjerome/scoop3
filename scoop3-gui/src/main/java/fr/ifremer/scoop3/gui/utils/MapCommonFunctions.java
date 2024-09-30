package fr.ifremer.scoop3.gui.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Point;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.map.model.PointModel;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.QCValues;

public abstract class MapCommonFunctions {

    private static final HashMap<String, PointModel> pointModels = new HashMap<>();

    /**
     * Convert observations data into OMGraphicList needed by the map component
     *
     * @param listObservations
     *            The list of observations
     * @return The list of OpenMap graphic objects
     */
    public static OMGraphicList convertObservationsToOMPoints(final List<Observation> listObservations,
	    final String datasetType) {
	final OMGraphicList omList = new OMGraphicList();
	for (int i = 0; i < listObservations.size(); i++) {
	    omList.addAll(convertObservationToOMPoint(listObservations.get(i), datasetType));
	}

	return omList;
    }

    /**
     * Create the list of objects for the map from the dataset
     *
     * @param dataset
     *            The dataset
     * @return The list of map object
     */
    public static OMGraphicList createOMListFromDataset(final Dataset dataset) {
	final List<Observation> observations = dataset.getObservations();

	return convertObservationsToOMPoints(observations, dataset.getDatasetType().toString());
    }

    /**
     * Create the list of objects for the map from the observations list in parameter
     *
     * @param dataset
     *            The dataset
     * @param observations
     * @return The list of map object
     */
    public static OMGraphicList createOMListFromObservations(final Dataset dataset,
	    final List<Observation> observations) {
	return convertObservationsToOMPoints(observations, dataset.getDatasetType().toString());
    }

    /**
     * Create a geoJson string with the openmap points
     *
     * @param dataset
     * @return
     */
    public static String createGeoJsonFromDataset(final Dataset dataset) {
	// Get the OMPoint list
	final OMGraphicList omList = createOMListFromDataset(dataset);

	// Crate the Feature Collection GeoJson OBject
	final FeatureCollection featureCollection = new FeatureCollection();

	for (int i = 0; i < omList.size(); i++) {
	    final OMGraphic omGraphic = omList.get(i);

	    if (omGraphic instanceof OMPoint) {
		final OMPoint omPoint = (OMPoint) omGraphic;

		final Feature feature = new Feature();
		final GeoJsonObject geoJsonObject = new Point(omPoint.getLon(), omPoint.getLat());
		feature.setGeometry(geoJsonObject);

		featureCollection.add(feature);

	    } else {
		// nothing to do
	    }

	}
	// featureCollection.set

	// Create mapper
	final ObjectMapper mapper = new ObjectMapper();
	mapper.enable(SerializationFeature.INDENT_OUTPUT);

	// GeoJson Object to JSON in String
	String geoJsonString = "";
	try {
	    geoJsonString = mapper.writeValueAsString(featureCollection);
	} catch (final JsonProcessingException e) {
	    SC3Logger.LOGGER.debug("Unable to write geoJson");
	    e.printStackTrace();
	}

	return geoJsonString;
    }

    /**
     *
     * @param observation
     * @param nearestProfiles
     * @param colorForNearestProfiles
     * @return
     */
    public static OMGraphicList createOMListWithNearestProfiles(final Observation observation,
	    final List<Profile> nearestProfiles, final Color colorForNearestProfiles, final String datasetType) {
	final OMGraphicList omList = new OMGraphicList();

	// Add current Obs
	omList.addAll(convertObservationToOMPoint(observation, datasetType));

	// Add all nearest profiles (with the right color)
	for (final Profile profile : nearestProfiles) {
	    omList.addAll(convertObservationToOMPoint(profile, colorForNearestProfiles, datasetType));
	}

	return omList;
    }

    /**
     * Get the PointModel for a given Observation.
     *
     * @param observation
     * @param level
     * @return
     */
    public static Object getPointModelForObs(final Observation observation, final int level, final String datasetType,
	    final boolean zoomIn) {
	String ref = "";
	if (datasetType.equals(DatasetType.PROFILE.toString())) {
	    ref = observation.getReference() + "_" + level + "_"
		    + observation.getFirstLatitudeClone().getValueAsDouble() + "_"
		    + observation.getFirstLongitudeClone().getValueAsDouble();
	} else {
	    if ((observation.getLatitude().getValues().size() > level)
		    && (observation.getLongitude().getValues().size() > level)) {
		ref = observation.getReference() + "_" + level + "_" + observation.getLatitude().getValues().get(level)
			+ "_" + observation.getLongitude().getValues().get(level);
	    } else {
		// timeserie with only one lat / lon
		ref = observation.getReference() + "_" + level + "_"
			+ observation.getFirstLatitudeClone().getValueAsDouble() + "_"
			+ observation.getFirstLongitudeClone().getValueAsDouble();
	    }
	}
	if (!pointModels.containsKey(ref)) {
	    if (datasetType.equals(DatasetType.PROFILE.toString())) {
		pointModels.put(ref,
			new PointModel(observation.getReference(), observation.getId(),
				observation.getSensor().getPlatform().getCode(), level, datasetType,
				observation.getFirstLatitudeClone().getValueAsDouble(),
				observation.getFirstLongitudeClone().getValueAsDouble()));
	    } else {
		if ((observation.getLatitude().getValues().size() > level)
			&& (observation.getLongitude().getValues().size() > level)) {
		    pointModels.put(ref,
			    new PointModel(observation.getReference(), observation.getId(),
				    observation.getSensor().getPlatform().getCode(), level, datasetType,
				    observation.getLatitude().getValues().get(level),
				    observation.getLongitude().getValues().get(level)));
		} else {
		    // timeserie with only one lat / lon
		    pointModels.put(ref,
			    new PointModel(observation.getReference(), observation.getId(),
				    observation.getSensor().getPlatform().getCode(), level, datasetType,
				    observation.getFirstLatitudeClone().getValueAsDouble(),
				    observation.getFirstLongitudeClone().getValueAsDouble()));
		}
	    }
	}
	if (pointModels.get(ref) != null) {
	    pointModels.get(ref).setZoomIn(zoomIn);
	}
	return pointModels.get(ref);
    }

    /**
     * Convert an observation into OMPoints needed for the map component
     *
     * @param observation
     *            An observation
     * @return A list of OMPoint
     */
    private static List<OMPoint> convertObservationToOMPoint(final Observation observation, final String datasetType) {
	return convertObservationToOMPoint(observation, null, datasetType);
    }

    /**
     * Convert an observation into OMPoints needed for the map component
     *
     * @param observation
     *            An observation
     * @param colorForNearestProfiles
     *            the color to use for the OMPoint. If null, compute the color with the Observation QCs.
     * @return A list of OMPoint
     */
    private static List<OMPoint> convertObservationToOMPoint(final Observation observation,
	    final Color colorForNearestProfiles, final String datasetType) {

	final List<OMPoint> omPoints = new ArrayList<>();

	int numberOfValues = observation.getLatitude().getValues().size();
	numberOfValues = Math.min(numberOfValues, observation.getLongitude().getValues().size());

	for (int level = 0; level < numberOfValues; level++) {

	    /* ********************************************************************************** */
	    /* *** getStartLatitude and getStartLongitude does no more exist in "observation" *** */
	    /* ********************************************************************************** */
	    // final double latitudeValue = ((index == 0) && (observation.getStartLatitude() != null)) ? observation
	    // .getStartLatitude().getValueAsDouble() : observation.getLatitude().getValues().get(index);
	    // final double longitudeValue = ((index == 0) && (observation.getStartLongitude() != null)) ? observation
	    // .getStartLongitude().getValueAsDouble() : observation.getLongitude().getValues().get(index);
	    /* ********************************************************************************** */
	    /* ********************************************************************************** */
	    /* ********************************************************************************** */

	    final Double latitudeValue = observation.getLatitude().getValues().get(level);
	    final Double longitudeValue = observation.getLongitude().getValues().get(level);

	    if ((latitudeValue != null) && (longitudeValue != null)) {
		final OMPoint point = new OMPoint(latitudeValue, longitudeValue, 3);

		if (colorForNearestProfiles == null) {
		    // Get the worst quality code
		    final QCValues pointQC;
		    if (numberOfValues == 1) {
			pointQC = observation.getWorstQCExcept9();
		    } else {
			pointQC = observation.getWorstQCExcept9ForIndex(level);
		    }

		    // Set the color depending of the quality code
		    final Color color = pointQC.getColor();

		    point.setFillPaint(color);
		} else {
		    point.setFillPaint(colorForNearestProfiles);
		}

		point.setAppObject(getPointModelForObs(observation, level, datasetType, true));

		omPoints.add(point);
	    }
	}

	return omPoints;
    }

    private MapCommonFunctions() {
    }

}
