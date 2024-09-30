package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class LatitudeParameter extends CoordinateParameter<Double> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7681878187522412116L;

    public LatitudeParameter(final String name, final int dimension) {
	super(name, dimension);
	// FIXME
	// this.codeP01 = "ALATZZ01";
	setUnit("DEGN");
    }

    /**
     * Normalize angle
     *
     * @param angle
     */
    public Double normalize(final Double angle) {
	Double newAngle = angle;
	if (angle != null) {
	    if ((angle < -90.0) || (angle > 90.0)) {
		newAngle = Double.NaN;
	    }
	}
	return newAngle;
    }

    /**
     * Add a new record to the parameter
     *
     * @param value
     * @param qcValue
     */
    @Override
    public void addRecord(final Double value, final QCValues qcValue) {
	super.values.add(normalize(value));
	super.qcValues.add(qcValue);
    }

    /**
     * Add a new record to the parameter at the desired index
     *
     * @param value
     * @param qcValue
     * @param index
     */
    @Override
    public void addRecordAtIndex(final int index, final Double value, final QCValues qcValue) {
	super.values.add(index, normalize(value));
	super.qcValues.add(index, qcValue);
    }
}
