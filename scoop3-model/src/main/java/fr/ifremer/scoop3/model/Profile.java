package fr.ifremer.scoop3.model;

import java.io.Serializable;
import java.util.ArrayList;

import fr.ifremer.scoop3.model.parameter.LatitudeParameter;
import fr.ifremer.scoop3.model.parameter.LongitudeParameter;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.SpatioTemporalParameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;
import fr.ifremer.scoop3.model.parameter.ZParameter;

/**
 *
 * @author Altran
 *
 */
public class Profile extends Observation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -969789762232841132L;

    /**
     * @param id
     */
    public Profile(final String id) {
	super(id);
    }

    /**
     * @param id
     * @param size
     */
    public Profile(final String id, final int size) {
	super(id, size);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.Observation#getReferenceParameter()
     */
    @Override
    public SpatioTemporalParameter<? extends Number> getReferenceParameter() {
	return getZ();
    }

    /*
     * (non-Javadoc)
     *
     * Get the list of physical parameters codes (oceanic + z)
     */
    public ArrayList<String> getPhysicalParameterCodes() {
	final ArrayList<String> physicalParams = new ArrayList<String>();
	for (final OceanicParameter op : this.getOceanicParameters().values()) {
	    physicalParams.add(op.getCode());
	}
	physicalParams.add(getZ().getCode());
	return physicalParams;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.Observation#getEmptyObservation()
     */
    @Override
    protected Observation getEmptyObservation() {
	return new Profile(getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.Observation#initSpatioTemporalParameters(int)
     */
    @Override
    protected void initSpatioTemporalParameters(final int size) {
	setLongitude(new LongitudeParameter(LONGITUDE_VAR_NAME, 1));
	setLatitude(new LatitudeParameter(LATITUDE_VAR_NAME, 1));
	setTime(new TimeParameter(TIME_VAR_NAME, 1));
	setZ(new ZParameter(Z_VAR_NAME, size));
    }
}
