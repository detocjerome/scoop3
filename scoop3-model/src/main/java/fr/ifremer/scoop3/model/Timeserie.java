package fr.ifremer.scoop3.model;

import java.io.Serializable;

import fr.ifremer.scoop3.model.parameter.LatitudeParameter;
import fr.ifremer.scoop3.model.parameter.LongitudeParameter;
import fr.ifremer.scoop3.model.parameter.SpatioTemporalParameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;
import fr.ifremer.scoop3.model.parameter.ZParameter;

public class Timeserie extends Observation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6492199169315580094L;

    /**
     * @param id
     */
    public Timeserie(final String id) {
	super(id);
    }

    /**
     * @param id
     * @param size
     */
    public Timeserie(final String id, final int size) {
	super(id, size);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.model.Observation#getReferenceParameter()
     */
    @Override
    public SpatioTemporalParameter<? extends Number> getReferenceParameter() {
	return getTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.model.Observation#getEmptyObservation()
     */
    @Override
    protected Observation getEmptyObservation() {
	return new Timeserie(getId());
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
	setTime(new TimeParameter(TIME_VAR_NAME, size));
	setZ(new ZParameter(Z_VAR_NAME, 1));
    }
}
