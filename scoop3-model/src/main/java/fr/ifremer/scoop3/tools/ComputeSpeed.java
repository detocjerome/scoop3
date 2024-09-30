package fr.ifremer.scoop3.tools;

import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.Trajectory;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.LongValueAndQC;

public class ComputeSpeed {

    private static Integer platformSpeedLimit = null;

    /**
     * Compute Speed between 2 Observations (if possible)
     *
     * @param obs1
     * @param obs2
     * @return
     */
    public static double computeCurrentSpeed(final Observation obs1, final LongValueAndQC obs1Time,
	    final DoubleValueAndQC obs1Latitude, final DoubleValueAndQC obs1Longitude, final Observation obs2,
	    final LongValueAndQC obs2Time, final DoubleValueAndQC obs2Latitude, final DoubleValueAndQC obs2Longitude) {
	double speedInKnot = 0;

	if ((obs2 != null) && (((obs1 instanceof Profile) && (obs2 instanceof Profile))
		|| ((obs1 instanceof Trajectory) && (obs2 instanceof Trajectory)))) {
	    // This control is only for Profiles and Trajectories
	    // 1 Knot = 1 Nautical Mile / 1 Hour

	    final double lat1 = obs1Latitude.getValueAsDouble();
	    final double lon1 = obs1Longitude.getValueAsDouble();
	    final double lat2 = obs2Latitude.getValueAsDouble();
	    final double lon2 = obs2Longitude.getValueAsDouble();

	    final double distanceInNauticalMiles = distance(lat1, lon1, lat2, lon2, "N");

	    // division by 3600.0d to avoid implicit cast in int
	    final double numberOfHours = (obs2Time.getValueAsLong() - obs1Time.getValueAsLong()) / 1000 / 3600.0d;

	    speedInKnot = (numberOfHours == 0) ? 0 : distanceInNauticalMiles / numberOfHours;
	}

	return speedInKnot;
    }

    /**
     * This routine calculates the distance between two points (given the latitude/longitude of those points). It is
     * being used to calculate the distance between two locations using GeoDataSource (TM) prodducts
     *
     * Definitions: South latitudes are negative, east longitudes are positive
     *
     * Passed to function: lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees) lat2, lon2 = Latitude and
     * Longitude of point 2 (in decimal degrees) unit = the unit you desire for results where: 'M' is statute miles 'K'
     * is kilometers (default) 'N' is nautical miles Worldwide cities and other features databases with latitude
     * longitude are available at http://www.geodatasource.com
     *
     * For enquiries, please contact sales@geodatasource.com
     *
     * Official Web site: http://www.geodatasource.com
     *
     * GeoDataSource.com (C) All Rights Reserved 2014
     *
     * system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
     *
     * system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
     *
     * system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param unit
     * @return
     */
    public static double distance(final double lat1, final double lon1, final double lat2, final double lon2,
	    final String unit) {
	final double theta = lon1 - lon2;
	double dist = (Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)))
		+ (Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta)));
	dist = Math.acos(dist);
	dist = rad2deg(dist);
	dist = dist * 60 * 1.1515;
	if (unit.equals("K")) {
	    dist = dist * 1.609344;
	} else if (unit.equals("N")) {
	    dist = dist * 0.8684;
	}
	return (dist);
    }

    /**
     * @return the platformSpeedLimit
     */
    public static Integer getPlatformSpeedLimit() {
	return platformSpeedLimit;
    }

    /**
     * @param platformSpeedLimit
     *            the platformSpeedLimit to set
     */
    public static void setPlatformSpeedLimit(final Integer platformSpeedLimit) {
	ComputeSpeed.platformSpeedLimit = platformSpeedLimit;
    }

    /**
     * This function converts decimal degrees to radians
     *
     * @param deg
     * @return
     */
    private static double deg2rad(final double deg) {
	return ((deg * Math.PI) / 180.0);
    }

    /**
     * This function converts radians to decimal degrees
     *
     * @param rad
     * @return
     */
    private static double rad2deg(final double rad) {
	return ((rad * 180) / Math.PI);
    }
}
