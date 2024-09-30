package fr.ifremer.scoop3.model;

import java.io.Serializable;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.parameter.LatitudeParameter;
import fr.ifremer.scoop3.model.parameter.LeveledParameter;
import fr.ifremer.scoop3.model.parameter.LongitudeParameter;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.SpatioTemporalParameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;

public class TimeserieImmLevel extends Timeserie implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -931156310682504266L;

    private static final char KEY_SEPARATOR = '_';

    /**
     * Constructor
     *
     * @param id
     */
    public TimeserieImmLevel(final String id) {
	super(id);
    }

    /**
     * Constructor
     *
     * @param id
     * @param size
     */
    public TimeserieImmLevel(final String id, final int size) {
	super(id, size);
    }

    /**
     * add a new leveled parameters
     *
     * @param key
     * @param value
     */
    public void addLeveledParameter(final LeveledParameter leveledParameter) throws Exception {

	// Check parameter dimension
	leveledParameter.checkDimension();
	final String parameterCode = leveledParameter.getCode();

	// Set code as 'CODE'_'IMM_LEVEL' (Ex : 9_0)
	leveledParameter.setCode(parameterCode + KEY_SEPARATOR + leveledParameter.getImmLevel().toString());

	// Store leveled parameter in OceanicParameter HASHMAP
	super.addOceanicParameter(leveledParameter);

	// Reset code to 'CODE'
	leveledParameter.setCode(parameterCode);
    }

    /**
     * Get leveled parameter
     *
     * @param parameterCode
     * @param immLevel
     *
     * @return OceanicParameter
     */
    public OceanicParameter getLeveledParameter(final String parameterCode, final Integer immLevel) throws Exception {
	String paramLevelKey = "";
	try {
	    // Get parameter by code as 'CODE'_'IMM_LEVEL' (Ex : 9_0)
	    paramLevelKey = parameterCode + KEY_SEPARATOR + immLevel.toString();

	    // Return leveled parameter in OceanicParameter HASHMAP
	    return super.getOceanicParameter(paramLevelKey);
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw new Exception("Leveled parameter " + paramLevelKey + " not found");
	}
    }

    /**
     * Check if TS have a given leveled parameter
     *
     * @param parameterCode
     * @param immLevel
     *
     * @return boolean
     */
    public boolean hasParameter(final String parameterCode, final Integer immLevel) {
	boolean hasParam = false;

	// Get parameter by code as 'CODE'_'IMM_LEVEL' (Ex : 9_0)
	final String paramLevelKey = parameterCode + KEY_SEPARATOR + immLevel.toString();

	if (this.getOceanicParameters().containsKey(paramLevelKey)) {
	    hasParam = true;
	}
	return hasParam;
    }

    /**
     * Check the parameter dimension
     */
    @Override
    public void checkDimensions() throws Exception {
	try {
	    getLongitude().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	try {
	    getLatitude().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	try {
	    getTime().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}

	Exception excToThrow = null;

	// For each leveled parameter
	for (final OceanicParameter lp : this.getOceanicParameters().values()) {
	    try {
		lp.checkDimension();
		if (lp.getDimension() != this.getReferenceParameter().getDimension()) {
		    throw new Exception(
			    lp.getCode() + " : dimension (" + lp.getDimension() + ") does not fit reference parameter "
				    + this.getReferenceParameter().getCode() + " : dimension ("
				    + getReferenceParameter().getDimension() + ") in the observation " + this.getId());
		} else {
		    SC3Logger.LOGGER.trace(lp.getCode() + " : dimension (" + lp.getDimension()
			    + ") fits reference parameter " + this.getReferenceParameter().getCode() + " : dimension ("
			    + getReferenceParameter().getDimension() + ")");
		}
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
		if (excToThrow == null) {
		    excToThrow = e;
		}
	    }
	}
	if (excToThrow != null) {
	    throw excToThrow;
	}
    }

    /**
     * Get referencd parameter
     */
    @Override
    public SpatioTemporalParameter<?> getReferenceParameter() {
	return getTime();
    }

    /**
     * Initialize spatio temporal parameter
     */
    @Override
    protected void initSpatioTemporalParameters(final int size) {
	setTime(new TimeParameter(TIME_VAR_NAME, size));
	setLongitude(new LongitudeParameter(LONGITUDE_VAR_NAME, size));
	setLatitude(new LatitudeParameter(LATITUDE_VAR_NAME, size));
    }

    /**
     * Create an empty time serie
     */
    @Override
    protected TimeserieImmLevel getEmptyObservation() {
	return new TimeserieImmLevel(getId());
    }

}
