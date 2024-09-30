package fr.ifremer.scoop3.map.tools;

import java.util.ListIterator;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.DataBounds;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.map.model.PointModel;

public final class CommonFunctions {

    /**
     * Add the label to a point
     *
     * @param point
     *            The point
     */
    public static void addLabelToPoint(final OMPoint point) {
	if (point.getAppObject() != null) {
	    final PointModel pointModel = (PointModel) point.getAppObject();
	    point.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler(pointModel.getLabelToDisplay()));
	}
    }

    /**
     * Get the bounds of the data
     *
     * @return The data bounds
     */
    public static DataBounds getDataBounds(final OMGraphicList objects) {
	double latMin = 200;
	double latMax = -200;
	double lonMin = 200;
	double lonMax = -200;
	double currentLat;
	double currentLon;

	final OMGraphicList omg = getOMPointFromList(objects);

	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    if (graphic instanceof OMPoint) {
		currentLat = ((OMPoint) graphic).getLat();
		currentLon = ((OMPoint) graphic).getLon();

		if (currentLat > latMax) {
		    latMax = currentLat;
		}
		if (currentLat < latMin) {
		    latMin = currentLat;
		}
		if (currentLon > lonMax) {
		    lonMax = currentLon;
		}
		if (currentLon < lonMin) {
		    lonMin = currentLon;
		}

	    }
	}

	// In case of there is only one point
	latMax = latMax + 1;
	latMin = latMin - 1;
	lonMax = lonMax + 1;
	lonMin = lonMin - 1;

	SC3Logger.LOGGER.trace("getDataBounds : lonMin " + lonMin + " ; latMin " + latMin + " ; lonMax " + lonMax
		+ "  ; latMax " + latMax);

	return new DataBounds(lonMin, latMin, lonMax, latMax);
    }

    /**
     * Get the bounds of the data
     *
     * @return The data bounds
     */
    public static DataBounds getDataBoundsAround180(final OMGraphicList objects) {
	double latMin = 200;
	double latMax = -200;
	double lonMin = 200;
	double lonMax = -200;
	double currentLat;
	double currentLon;

	final OMGraphicList omg = getOMPointFromList(objects);

	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    if (graphic instanceof OMPoint) {
		currentLat = ((OMPoint) graphic).getLat();
		currentLon = ((OMPoint) graphic).getLon();

		if (currentLat > latMax) {
		    latMax = currentLat;
		}
		if (currentLat < latMin) {
		    latMin = currentLat;
		}
		if ((currentLon > lonMax) && (currentLon < 0)) {
		    lonMax = currentLon;
		}
		if ((currentLon < lonMin) && (currentLon > 0)) {
		    lonMin = currentLon;
		}

	    }
	}

	// In case of there is only one point
	latMax = latMax + 1;
	latMin = latMin - 1;
	lonMax = lonMax + 1;
	lonMin = lonMin - 1;

	SC3Logger.LOGGER.trace("getDataBounds : lonMin " + lonMin + " ; latMin " + latMin + " ; lonMax " + lonMax
		+ "  ; latMax " + latMax);

	return new DataBounds(lonMin, latMin, lonMax, latMax);
    }

    /**
     * Convert any OMGraphicList of graphics in a OMGraphicList of OMPoint
     *
     * @param omg
     *            A list of OMGraphic
     *
     * @return An List of OMPoint
     */
    public static OMGraphicList getOMPointFromList(final OMGraphicList omg) {
	OMGraphicList omglist = new OMGraphicList();

	final ListIterator<? extends OMGraphic> iterator = omg.listIteratorCopy();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    if (graphic instanceof OMPoint) {
		// SC3Logger.LOGGER.debug(((OMPoint) graphic).getAppObject());
		omglist.add(graphic);
	    } else if (graphic instanceof OMGraphicList) {
		omglist = getOMPointFromList((OMGraphicList) graphic);
	    }
	}

	return omglist;
    }

    /**
     * Remove a label to a point
     *
     * @param point
     *            The point
     */
    public static void removeLabelToPoint(final OMPoint point) {
	point.removeAttribute(OMGraphicConstants.LABEL);
    }

    /**
     * Zoom on the datas of the observation layer
     *
     * @param map
     *            The map component
     */
    public static void zoomToDatas(final MapBean map, final OMGraphicList objects) {
	final Proj proj = getInitialZoomProjection(map, objects);
	if (proj != null) {
	    map.setProjection(proj);
	}
    }

    /**
     * Get the initial zoom projection on map
     *
     * @param proj
     * @param bounds
     * @return
     */
    public static Proj getInitialZoomProjection(final MapBean map, final OMGraphicList objects) {
	if (map != null) {
	    final Proj proj = (Proj) map.getProjection();
	    final DataBounds bounds = getDataBounds(objects);

	    if (bounds != null) {
		final java.awt.geom.Point2D center;
		final DataBounds boundsAround180 = getDataBoundsAround180(objects);
		if ((bounds.getMin().getX() <= -170) && (bounds.getMax().getX() >= 170)
			&& (Math.abs(boundsAround180.getMin().getX() - boundsAround180.getMax().getX()) > 90)) {
		    center = bounds.getCenter();
		    final double differenceRealBounds = (180 + boundsAround180.getMin().getX() + 180)
			    - boundsAround180.getMax().getX();
		    double realCenterX;
		    if ((boundsAround180.getMax().getX() + (differenceRealBounds / 2)) < 180) {
			realCenterX = boundsAround180.getMax().getX() + (differenceRealBounds / 2);
		    } else {
			realCenterX = boundsAround180.getMin().getX() - (differenceRealBounds / 2);
		    }
		    center.setLocation(realCenterX, center.getY());
		} else {
		    center = bounds.getCenter();
		}
		if (center != null) {
		    proj.setCenter(center.getY(), center.getX());
		    final LatLonPoint llp1 = new LatLonPoint.Double(bounds.getMax().getY(), bounds.getMin().getX());
		    final LatLonPoint llp2 = new LatLonPoint.Double(bounds.getMin().getY(), bounds.getMax().getX());
		    float scale = ProjMath.getScale(llp1, llp2, proj);
		    proj.setScale(scale);
		    final java.awt.geom.Point2D ul = proj.getUpperLeft();
		    final java.awt.geom.Point2D lr = proj.getLowerRight();
		    final double factor1 = (bounds.getMax().getY() - bounds.getMin().getY()) / (ul.getY() - lr.getY());
		    final double factor2 = (bounds.getMax().getX() - bounds.getMin().getX()) / (lr.getX() - ul.getX());
		    // 1.1 buffers the edges for viewing a little, a little
		    // zoomed out.
		    scale *= Math.max(factor1, factor2);
		    proj.setScale(scale * 2f);
		    return proj;
		}
	    } else {
		SC3Logger.LOGGER.debug("No bounds for the data");
	    }
	}
	return null;
    }

    /**
     * The constructor
     */
    private CommonFunctions() {
    }

}
