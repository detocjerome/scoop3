package fr.ifremer.scoop3.map.model;

public class PointModel {

    private final String labelToDisplay;
    private final int level;
    private final String platformCode;
    private final String reference;
    private final String datasetType;
    private final Double latitude;
    private final Double longitude;

    private boolean zoomIn = true;

    public PointModel(final String reference, final String labelToDisplay, final String platformCode, final int level,
	    final String datasetType, final Double latitude, final Double longitude) {
	this.reference = reference;
	this.labelToDisplay = labelToDisplay;
	this.platformCode = platformCode;
	this.level = level;
	this.datasetType = datasetType;
	this.latitude = latitude;
	this.longitude = longitude;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if ((obj instanceof PointModel)) {
	    final PointModel pm2 = (PointModel) obj;
	    boolean equals = ((this.reference == null) && (pm2.getReference() == null))
		    || ((this.reference != null) && this.reference.equals(pm2.getReference()));
	    equals &= ((this.labelToDisplay == null) && (pm2.getLabelToDisplay() == null))
		    || ((this.labelToDisplay != null) && this.labelToDisplay.equals(pm2.getLabelToDisplay()));
	    equals &= ((this.platformCode == null) && (pm2.getPlatformCode() == null))
		    || ((this.platformCode != null) && this.platformCode.equals(pm2.getPlatformCode()));
	    // FAE 29201. Do NOT compare LEVEL
	    // equals &= (this.level == pm2.getLevel());
	    return equals;
	}
	// else
	return super.equals(obj);
    }

    /**
     * @return the labelToDisplay
     */
    public String getLabelToDisplay() {
	return labelToDisplay;
    }

    /**
     * @return the level
     */
    public int getLevel() {
	return level;
    }

    /**
     * @return the platformCode
     */
    public String getPlatformCode() {
	return platformCode;
    }

    /**
     * @return the reference
     */
    public String getReference() {
	return reference;
    }

    /**
     * @return the reference
     */
    public String getDatasetType() {
	return datasetType;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
	return latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
	return longitude;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return getClass().getSimpleName() + " : reference:" + reference + ", labelToDisplay:" + labelToDisplay
		+ ", platformCode:" + platformCode + ", level:" + level;
    }

    public void setZoomIn(final boolean b) {
	zoomIn = b;
    }

    public boolean getZoomIn() {
	return zoomIn;
    }
}
