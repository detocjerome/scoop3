package fr.ifremer.scoop3.controller;

import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;

public class ParameterStatisticsForTimeSerie {

    /**
     * Default value : 4
     */
    private static int statFacteurSt = 4;

    private final double average;
    private final double ecartType;

    /**
     * @return the statFacteurSt
     */
    public static int getStatFacteurSt() {
	return statFacteurSt;
    }

    /**
     * @param statFacteurSt
     *            the statFacteurSt to set
     */
    public static void setStatFacteurSt(final int statFacteurSt) {
	ParameterStatisticsForTimeSerie.statFacteurSt = statFacteurSt;
    }

    public ParameterStatisticsForTimeSerie(final OceanicParameter parameter) {
	final int indexMax = parameter.getValues().size();

	final double defaultValue = parameter.getFillValue() == null ? Double.NEGATIVE_INFINITY
		: parameter.getFillValue();

	double sum = 0;
	double sumSqrt = 0;

	int numberOfValues = 0;

	// Compute average value and variance.
	for (int index = 0; index < indexMax; index++) {
	    final double value = parameter.getValues().get(index);

	    // FAE 24265 : Do not keep default values ...
	    if ((value != defaultValue) && (value != Parameter.DOUBLE_EMPTY_VALUE)) {
		numberOfValues++;
		sum += value;
		sumSqrt += value * value;
	    }
	}

	if (numberOfValues != 0) {
	    average = sum / numberOfValues;
	} else {
	    average = 0;
	}
	final double variance = (sumSqrt / numberOfValues) - (average * average);
	ecartType = Math.sqrt(variance);
    }

    /**
     * @return the average
     */
    public double getAverage() {
	return average;
    }

    /**
     * @return the ecartType
     */
    public double getEcartType() {
	return ecartType;
    }

    /**
     * @return N x ecartType
     */
    public double getNXecartType() {
	return statFacteurSt * getEcartType();
    }

}
