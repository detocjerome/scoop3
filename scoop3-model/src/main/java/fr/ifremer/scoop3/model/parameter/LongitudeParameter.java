package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class LongitudeParameter extends CoordinateParameter<Double> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6720153626407110864L;

    public LongitudeParameter(final String name, final int dimension) {
	super(name, dimension);
	// FIXME
	// this.codeP01 = "ALONZZ01";
	setUnit("DEGE");
    }

    /**
     * Normalize angle
     *
     * @param angle
     */
    public Double normalize(final Double angle) {
	Double newAngle = null;
	if (angle != null) {
	    newAngle = angle.doubleValue() % 360.0;

	    if (newAngle <= -180.0) {
		newAngle += 360.0;
	    } else if (newAngle > 180.0) {
		newAngle -= 360.0;
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
