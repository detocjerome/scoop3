package fr.ifremer.scoop3.model;

import java.io.Serializable;

public class BoundingBox implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7447390926980852331L;
    /**
     * The maximum of the Latitude Dimension
     */
    private Double latitudeMax;
    /**
     * The minimum of the Latitude Dimension
     */
    private Double latitudeMin;
    /**
     * The maximum of the Longitude Dimension
     */
    private Double longitudeMax;
    /**
     * The minimum of the Longitude Dimension
     */
    private Double longitudeMin;
    /**
     * The maximum of the Z Dimension
     */
    private Double zMax;
    /**
     * The minimum of the Z Dimension
     */
    private Double zMin;

    public BoundingBox(final Double latitudeMax, final Double latitudeMin, final Double longitudeMax,
	    final Double longitudeMin) {
	this(latitudeMax, latitudeMin, longitudeMax, longitudeMin, null, null);
    }

    public BoundingBox(final Double latitudeMax, final Double latitudeMin, final Double longitudeMax,
	    final Double longitudeMin, final Double zMin, final Double zMax) {
	this.latitudeMax = latitudeMax;
	this.latitudeMin = latitudeMin;
	this.longitudeMax = longitudeMax;
	this.longitudeMin = longitudeMin;
	this.zMax = zMax;
	this.zMin = zMin;
    }

    /**
     * @return the latitudeMax
     */
    public Double getLatitudeMax() {
	return latitudeMax;
    }

    /**
     * @return the latitudeMin
     */
    public Double getLatitudeMin() {
	return latitudeMin;
    }

    /**
     * @return the longitudeMax
     */
    public Double getLongitudeMax() {
	return longitudeMax;
    }

    /**
     * @return the longitudeMin
     */
    public Double getLongitudeMin() {
	return longitudeMin;
    }

    /**
     * @return the zMax
     */
    public Double getzMax() {
	return zMax;
    }

    /**
     * @return the zMin
     */
    public Double getzMin() {
	return zMin;
    }

    /**
     * @param latitudeMax
     *            the latitudeMax to set
     */
    public void setLatitudeMax(final Double latitudeMax) {
	this.latitudeMax = latitudeMax;
    }

    /**
     * @param latitudeMin
     *            the latitudeMin to set
     */
    public void setLatitudeMin(final Double latitudeMin) {
	this.latitudeMin = latitudeMin;
    }

    /**
     * @param longitudeMax
     *            the longitudeMax to set
     */
    public void setLongitudeMax(final Double longitudeMax) {
	this.longitudeMax = longitudeMax;
    }

    /**
     * @param longitudeMin
     *            the longitudeMin to set
     */
    public void setLongitudeMin(final Double longitudeMin) {
	this.longitudeMin = longitudeMin;
    }

    /**
     * @param zMax
     *            the zMax to set
     */
    public void setzMax(final Double zMax) {
	this.zMax = zMax;
    }

    /**
     * @param zMin
     *            the zMin to set
     */
    public void setzMin(final Double zMin) {
	this.zMin = zMin;
    }

}
