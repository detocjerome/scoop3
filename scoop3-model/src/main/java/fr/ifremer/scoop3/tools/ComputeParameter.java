package fr.ifremer.scoop3.tools;

import java.io.Serializable;

import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.LatitudeParameter;
import fr.ifremer.scoop3.model.parameter.LongitudeParameter;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import fr.ifremer.scoop3.model.parameter.TimeParameter;

public abstract class ComputeParameter implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6459848908061835208L;

    private static double defaultValue = 99999;

    /**
     * Compute the Current Components EWCT and NSCT for the given parameters
     *
     * @param hcdtParameter
     * @param hcspParameter
     * @param ewctDefaultValue
     * @param ewctParameterName
     * @param nsctDefaultValue
     * @param nsctParameterName
     */
    public static OceanicParameter[] computeCurrentComponents(final OceanicParameter hcdtParameter,
	    final OceanicParameter hcspParameter, final double ewctDefaultValue, final String ewctParameterName,
	    final double nsctDefaultValue, final String nsctParameterName) {
	QCValues currentQCValue;
	double ewctValue;
	double nsctValue;

	final OceanicParameter ewctParameter = new OceanicParameter(ewctParameterName);
	ewctParameter.setFillValue(ewctDefaultValue);
	ewctParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_MODIFIABLE);

	final OceanicParameter nsctParameter = new OceanicParameter(nsctParameterName);
	nsctParameter.setFillValue(nsctDefaultValue);
	nsctParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_MODIFIABLE);

	final OceanicParameter[] toReturn = new OceanicParameter[] { ewctParameter, nsctParameter };

	final double hcdtDefaultValue = hcdtParameter.getFillValue();
	final double hcspDefaultValue = hcspParameter.getFillValue();

	// All parameters have the same dimension value
	final int numberOfValues = hcdtParameter.getValues().size();
	for (int index = 0; index < numberOfValues; index++) {

	    final double hcdtDegValue = hcdtParameter.getValues().get(index);
	    final double hcspValue = hcspParameter.getValues().get(index);
	    final QCValues hcdtQC = hcdtParameter.getQcValues().get(index);
	    final QCValues hcspQC = hcspParameter.getQcValues().get(index);

	    // Check if there is at least on input parameter which is equal to its default value
	    if ((hcdtDegValue != hcdtDefaultValue) && (hcspValue != hcspDefaultValue) && (hcdtQC != QCValues.QC_9)
		    && (hcspQC != QCValues.QC_9)) {
		final double hcdtRadValue = Conversions.convertAngleFromDegreeToRadian(hcdtDegValue);

		currentQCValue = QCValues.getWorstQC(hcdtQC, hcspQC);

		ewctValue = hcspValue * Math.sin(hcdtRadValue);
		nsctValue = hcspValue * Math.cos(hcdtRadValue);
	    } else {
		// at least one of the input parameters is "DEFAULT"
		currentQCValue = QCValues.QC_9;
		ewctValue = ewctDefaultValue;
		nsctValue = nsctDefaultValue;
	    }
	    ewctParameter.addRecord(ewctValue, currentQCValue);
	    nsctParameter.addRecord(nsctValue, currentQCValue);
	}

	return toReturn;
    }

    /**
     * Compute the Current Direction HCDT and Amplitude HCSP for the given parameters
     *
     * @param ewctParameter
     * @param nsctParameter
     * @param hcdtDefaultValue
     * @param hcdtParameterName
     * @param hcspDefaultValue
     * @param hcspParameterName
     * @return results in an array
     */
    public static OceanicParameter[] computeCurrentDirectionAndAmplitude(final OceanicParameter ewctParameter,
	    final OceanicParameter nsctParameter, final double hcdtDefaultValue, final String hcdtParameterName,
	    final double hcspDefaultValue, final String hcspParameterName) {
	QCValues currentQCValue;
	double hcspValue;
	double hcdtValue;

	final OceanicParameter hcdtParameter = new OceanicParameter(hcdtParameterName);
	hcdtParameter.setFillValue(hcdtDefaultValue);
	hcdtParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_MODIFIABLE);

	final OceanicParameter hcspParameter = new OceanicParameter(hcspParameterName);
	hcspParameter.setFillValue(hcspDefaultValue);
	hcspParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_MODIFIABLE);

	final OceanicParameter[] toReturn = new OceanicParameter[] { hcdtParameter, hcspParameter };

	final double ewctDefaultValue = hcdtParameter.getFillValue();
	final double nsctDefaultValue = hcspParameter.getFillValue();

	// All parameters have the same dimension value
	final int numberOfValues = ewctParameter.getValues().size();
	for (int index = 0; index < numberOfValues; index++) {
	    final double ewctValue = ewctParameter.getValues().get(index);
	    final double nsctValue = nsctParameter.getValues().get(index);
	    final QCValues ewctQC = ewctParameter.getQcValues().get(index);
	    final QCValues nsctQC = nsctParameter.getQcValues().get(index);

	    // Check if there is at least on input parameter which is equal to its default value
	    if ((ewctValue != ewctDefaultValue) && (nsctValue != nsctDefaultValue) && (ewctQC != QCValues.QC_9)
		    && (nsctQC != QCValues.QC_9)) {
		currentQCValue = QCValues.getWorstQC(ewctQC, nsctQC);

		hcspValue = Math.sqrt((ewctValue * ewctValue) + (nsctValue * nsctValue));

		if (nsctValue == 0d) {
		    if (ewctValue == 0d) {
			// Both nsctValue and ewctValue == 0 ==> angular : 0°
			hcdtValue = 0d;
		    } else if (ewctValue > 0) {
			// (nsctValue == 0) and (ewctValue > 0) ==> angular : 90°
			hcdtValue = 90d;
		    } else {
			// (nsctValue == 0) and (ewctValue < 0) ==> angular : -90°
			hcdtValue = -90d;
		    }
		} else {
		    hcdtValue = Math.atan(ewctValue / nsctValue) * (180.0d / Math.PI);
		}

		if (nsctValue < 0) {
		    hcdtValue += 180;
		} else if (ewctValue < 0) {
		    hcdtValue += 360;
		}
	    } else {
		// at least one of the input parameters is "DEFAULT"
		currentQCValue = QCValues.QC_9;
		hcspValue = hcspDefaultValue;
		hcdtValue = hcdtDefaultValue;
	    }

	    hcspParameter.addRecord(hcspValue, currentQCValue);
	    hcdtParameter.addRecord(hcdtValue, currentQCValue);
	}

	return toReturn;
    }

    /**
     * Compute the density for the given parameters
     *
     * @param presParameter
     *            pressure parameter
     * @param tempParameter
     *            temperature parameter
     * @param psalParameter
     *            salinity parameter
     * @param densityAnomalyDefaultValue
     */
    public static OceanicParameter computeDensityAnomaly(final Parameter<Double> presParameter,
	    final OceanicParameter tempParameter, final OceanicParameter psalParameter,
	    final double densityAnomalyDefaultValue, final String computedParameterName) {
	double densityValue;
	QCValues densityQCValue;

	final OceanicParameter densityParameter = new OceanicParameter(computedParameterName);
	densityParameter.setFillValue(densityAnomalyDefaultValue);
	densityParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_CONTROL);

	double presDefaultValue;
	double tempDefaultValue;
	double psalDefaultValue;
	try {
	    presDefaultValue = presParameter.getFillValue() == null ? Double.MIN_VALUE : presParameter.getFillValue();
	    tempDefaultValue = tempParameter.getFillValue() == null ? Double.MIN_VALUE : tempParameter.getFillValue();
	    psalDefaultValue = psalParameter.getFillValue() == null ? Double.MIN_VALUE : psalParameter.getFillValue();
	} catch (final Exception e) {
	    presDefaultValue = defaultValue;
	    tempDefaultValue = defaultValue;
	    psalDefaultValue = defaultValue;
	}

	// All parameters have the same dimension value
	final int numberOfValues = presParameter.getValues().size();
	for (int index = 0; index < numberOfValues; index++) {

	    final double presValue = presParameter.getValues().get(index);
	    final double tempValue = tempParameter.getValues().get(index);
	    final double psalValue = psalParameter.getValues().get(index);

	    // if the user use removingMeasure mode, the density value can't be computed and is NaN too
	    if (!Double.isNaN(tempValue) && !Double.isNaN(psalValue) && !Double.isNaN(presValue)
		    && !Double.isInfinite(tempValue) && !Double.isInfinite(psalValue)
		    && !Double.isInfinite(presValue)) {
		// Check if there is at least on input parameter which is equal to its default value
		if ((presValue != presDefaultValue) && (tempValue != tempDefaultValue)
			&& (psalValue != psalDefaultValue)) {
		    // (densityValue * 1000) - 1000 ==> to get the anomaly of the density
		    densityValue = (computeDensity(presValue, tempValue, psalValue) * 1000) - 1000;

		    // Round to 3 digits
		    densityValue = (int) (densityValue * 1000) / 1000d;

		    final QCValues presQC = presParameter.getQcValues().get(index);
		    final QCValues tempQC = tempParameter.getQcValues().get(index);
		    final QCValues psalQC = psalParameter.getQcValues().get(index);
		    if ((presQC != null) && (tempQC != null) && (psalQC != null)) {
			densityQCValue = QCValues.getWorstQC(presQC, tempQC);
			densityQCValue = QCValues.getWorstQC(densityQCValue, psalQC);
		    } else {
			densityQCValue = QCValues.QC_9;
		    }
		} else {
		    // at least one of the input parameters is "DEFAULT"
		    densityValue = densityAnomalyDefaultValue;
		    densityQCValue = QCValues.QC_9;
		}
		// FAE 27029
		if (Double.isNaN(densityValue)) {
		    densityValue = densityAnomalyDefaultValue;
		    densityQCValue = QCValues.QC_9;
		}
		densityParameter.addRecord(densityValue, densityQCValue);
	    } else {
		final QCValues presQC = presParameter.getQcValues().get(index);
		final QCValues tempQC = tempParameter.getQcValues().get(index);
		final QCValues psalQC = psalParameter.getQcValues().get(index);
		densityQCValue = QCValues.getWorstQC(presQC, tempQC);
		densityQCValue = QCValues.getWorstQC(densityQCValue, psalQC);
		densityParameter.addRecord(Double.NaN, densityQCValue);
	    }
	}

	return densityParameter;
    }

    /**
     * Compute the speed for the given parameters
     *
     * @param latParameter
     *            latitude parameter
     * @param lonParameter
     *            longitude parameter
     * @param speedDefaultValue
     */
    public static OceanicParameter computeSpeed(final LatitudeParameter latParameter,
	    final LongitudeParameter lonParameter, final TimeParameter timeParameter, final double speedDefaultValue,
	    final String computedParameterName) {
	double speedValue;
	QCValues speedQCValue;

	final OceanicParameter speedParameter = new OceanicParameter(computedParameterName);
	speedParameter.setFillValue(speedDefaultValue);
	speedParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_CONTROL);

	final double latDefaultValue = latParameter.getFillValue() == null ? Double.MIN_VALUE
		: latParameter.getFillValue();
	final double lonDefaultValue = lonParameter.getFillValue() == null ? Double.MIN_VALUE
		: lonParameter.getFillValue();
	final double timeDefaultValue = timeParameter.getFillValue() == null ? Double.MIN_VALUE
		: timeParameter.getFillValue();

	// All parameters have the same dimension value
	final int numberOfValues = latParameter.getValues().size();
	if (numberOfValues == 1) {
	    final QCValues latQC = latParameter.getQcValues().get(0);
	    final QCValues lonQC = lonParameter.getQcValues().get(0);
	    QCValues timeQC = timeParameter.getQcValues().get(0);
	    for (int index = 0; index < (timeParameter.getQcValues().size() - 1); index++) {
		timeQC = QCValues.getWorstQC(timeQC, timeParameter.getQcValues().get(index + 1));
	    }
	    speedQCValue = QCValues.getWorstQC(latQC, lonQC);
	    speedQCValue = QCValues.getWorstQC(speedQCValue, timeQC);
	    speedParameter.addRecord(0.0, speedQCValue);
	} else {
	    final QCValues firstLatQC = latParameter.getQcValues().get(0);
	    final QCValues firstLonQC = lonParameter.getQcValues().get(0);
	    final QCValues firstTimeQC = timeParameter.getQcValues().get(0);
	    QCValues firstSpeedQCValue = QCValues.getWorstQC(firstLatQC, firstLonQC);
	    firstSpeedQCValue = QCValues.getWorstQC(firstSpeedQCValue, firstTimeQC);
	    speedParameter.addRecord(0.0, firstSpeedQCValue);
	    for (int index = 1; index < numberOfValues; index++) {

		final Double lat1Value = latParameter.getValues().get(index - 1);
		final Double lon1Value = lonParameter.getValues().get(index - 1);
		final double time1Value = timeParameter.getValues().get(index - 1);
		final Double lat2Value = latParameter.getValues().get(index);
		final Double lon2Value = lonParameter.getValues().get(index);
		final double time2Value = timeParameter.getValues().get(index);

		// Check if there is at least on input parameter which is equal to its default value
		if ((lat1Value != null) && (lon1Value != null) && (lat2Value != null) && (lon2Value != null)
			&& (lat1Value != latDefaultValue) && (lon1Value != lonDefaultValue)
			&& (time1Value != timeDefaultValue) && (lat2Value != latDefaultValue)
			&& (lon2Value != lonDefaultValue) && (time2Value != timeDefaultValue)) {
		    speedValue = computeSpeedFunction(lat1Value, lon1Value, time1Value, lat2Value, lon2Value,
			    time2Value);

		    // Round to 3 digits
		    speedValue = (int) (speedValue * 1000) / 1000d;

		    final QCValues latQC = latParameter.getQcValues().get(index);
		    final QCValues lonQC = lonParameter.getQcValues().get(index);
		    final QCValues timeQC = timeParameter.getQcValues().get(index);
		    speedQCValue = QCValues.getWorstQC(latQC, lonQC);
		    speedQCValue = QCValues.getWorstQC(speedQCValue, timeQC);
		} else {
		    // at least one of the input parameters is "DEFAULT"
		    speedValue = speedDefaultValue;
		    speedQCValue = QCValues.QC_9;
		}
		// FAE 27029
		if (Double.isNaN(speedValue)) {
		    speedValue = speedDefaultValue;
		    speedQCValue = QCValues.QC_9;
		}
		speedParameter.addRecord(speedValue, speedQCValue);
	    }
	}
	return speedParameter;
    }

    public static OceanicParameter computeSalinity(final Parameter<Double> presParameter,
	    final OceanicParameter tempParameter, final OceanicParameter cndcParameter,
	    final double salinityDefaultValue, final String salinityParameterName) {
	double salinityValue;
	QCValues salinityQCValue;

	final OceanicParameter salinityParameter = new OceanicParameter(salinityParameterName);
	salinityParameter.setFillValue(salinityDefaultValue);
	salinityParameter.setLinkParamType(LINK_PARAM_TYPE.COMPUTED_MODIFIABLE);

	final double presDefaultValue = presParameter.getFillValue() == null ? Double.MIN_VALUE
		: presParameter.getFillValue();
	final double tempDefaultValue = tempParameter.getFillValue() == null ? Double.MIN_VALUE
		: tempParameter.getFillValue();
	final double cndcDefaultValue = cndcParameter.getFillValue() == null ? Double.MIN_VALUE
		: cndcParameter.getFillValue();

	// All parameters have the same dimension value
	final int numberOfValues = presParameter.getValues().size();
	for (int index = 0; index < numberOfValues; index++) {

	    final double presValue = presParameter.getValues().get(index);
	    final double tempValue = tempParameter.getValues().get(index);
	    final double cndcValue = cndcParameter.getValues().get(index);

	    // Check if there is at least on input parameter which is equal to its default value
	    if ((presValue != presDefaultValue) && (tempValue != tempDefaultValue) && (cndcValue != cndcDefaultValue)) {
		salinityValue = computeSalinity(presValue, tempValue, cndcValue);

		// Round to 3 digits
		salinityValue = (int) (salinityValue * 1000) / 1000d;

		final QCValues presQC = presParameter.getQcValues().get(index);
		final QCValues tempQC = tempParameter.getQcValues().get(index);
		final QCValues psalQC = cndcParameter.getQcValues().get(index);
		salinityQCValue = QCValues.getWorstQC(presQC, tempQC);
		salinityQCValue = QCValues.getWorstQC(salinityQCValue, psalQC);
	    } else {
		// at least one of the input parameters is "DEFAULT"
		salinityValue = salinityDefaultValue;
		salinityQCValue = QCValues.QC_9;
	    }
	    // FAE 27029
	    if (Double.isNaN(salinityValue)) {
		salinityValue = salinityDefaultValue;
		salinityQCValue = QCValues.QC_9;
	    }
	    salinityParameter.addRecord(salinityValue, salinityQCValue);
	}

	return salinityParameter;
    }

    /**
     * Compute a adiabatic temperature
     *
     * Source : CoDatRecord.cpp (used for Coriolis treatments)
     *
     * @param p_presValue
     * @param p_tempValue
     * @param p_psalValue
     * @return
     */
    private static double computeAdiabaticTempValue(final double p_presValue, // I : The pressure
	    final double p_tempValue, // I : The temperature
	    final double p_psalValue // I : The salinity
    ) {
	// ----------------------------------------------------------------------------
	// DONNEES LOCALES
	// ----------------------------------------------------------------------------

	double lP;
	double lT;
	double lS;
	double lDS;
	double lAdiabaticTemperature = 0.0;

	// ----------------------------------------------------------------------------
	// TRAITEMENT
	// ----------------------------------------------------------------------------

	// ********************************************************************
	// Bryden,H.,1973,DEEP-SEA RES.,20,401-408
	// Fofonoff,N.,1977,DEEP-SEA RES.,24,489-491
	//
	// Computes the adiabatic temperature gradient
	//
	// Units are:
	// PRESSURE P DECIBARS
	// TEMPERATURE T DEG CELSIUS (IPTS-68)
	// SALINITY S (IPSS-78)
	// ADIABATIC ATG DEG. C/DECIBAR
	//
	//
	// CHECKVALUE:
	// ATG = 3.255976E-4 C/DBAR
	// FOR S = 40 (IPSS-78), T = 40 DEG C, P0 = 10000 DECIBARS
	// ********************************************************************

	lP = p_presValue;
	lT = p_tempValue;
	lS = p_psalValue;

	lDS = lS - 35.0;

	lAdiabaticTemperature = (((((((-2.1687E-16 * lT) + 1.8676E-14) * lT) - 4.6206E-13) * lP)
		+ ((((2.7759E-12 * lT) - 1.1351E-10) * lDS)
			+ (((((-5.4481E-14 * lT) + 8.733E-12) * lT) - 6.7795E-10) * lT) + 1.8741E-8))
		* lP) + (((-4.2393E-8 * lT) + 1.8932E-6) * lDS)
		+ (((((6.6228E-10 * lT) - 6.836E-8) * lT) + 8.5258E-6) * lT) + 3.5803E-5;

	// ----------------------------------------------------------------------------
	// ERREURS ET RETOUR
	// ----------------------------------------------------------------------------
	return lAdiabaticTemperature;
    }

    /**
     * Compute the density for the given parameters values
     *
     * @param p_presValue
     * @param p_tempValue
     * @param p_psalValue
     * @return
     */
    private static double computeDensity(final double p_presValue, // I : The pressure
	    final double p_tempValue, // I : The temperature
	    final double p_psalValue // I : The salinity
    ) {
	final double l_referencePressure = 0.0; // The reference pressure value

	// ********************************************************************
	// Compute potential temperature
	// ********************************************************************

	final double l_potentialTemperature = computePotentialTempValue(p_presValue, p_tempValue, p_psalValue,
		l_referencePressure);

	// ********************************************************************
	// Compute and add the potential density measure
	// ********************************************************************

	return computeDensValue(l_referencePressure, l_potentialTemperature, p_psalValue);
    }

    /**
     * Compute the speed for the given parameters values
     *
     * @param p_latValue
     * @param p_lonValue
     * @return
     */
    private static double computeSpeedFunction(double pLat1Value, // I : The first latitude
	    double pLon1Value, // I : The first longitude
	    final double p_time1Value, // I : The first time parameter
	    double pLat2Value, // I : The second latitude
	    double pLon2Value, // I : The second longitude
	    final double p_time2Value // I : The second time parameter
    ) {

	if ((pLat1Value == pLat2Value) && (pLon1Value == pLon2Value)) {
	    return 0.0;
	} else {
	    // Convert degrees to radians
	    pLat1Value = (pLat1Value * Math.PI) / 180.0;
	    pLon1Value = (pLon1Value * Math.PI) / 180.0;

	    pLat2Value = (pLat2Value * Math.PI) / 180.0;
	    pLon2Value = (pLon2Value * Math.PI) / 180.0;

	    // radius of earth in metres
	    final double r = 6371000;

	    // Distance in metres
	    final double distance = r * Math.acos((Math.sin(pLat1Value) * Math.sin(pLat2Value))
		    + (Math.cos(pLat1Value) * Math.cos(pLat2Value) * Math.cos(pLon2Value - pLon1Value)));

	    final double time_s = (p_time2Value - p_time1Value) / 1000.0;
	    final double speed_mps = distance / time_s;
	    return speed_mps * 1.94384;
	}
    }

    /**
     * Compute a density using a pressure, a potential temperature and a salinity
     *
     * Source : CoDatRecord.cpp (used for Coriolis treatments)
     *
     * @param p_presValue
     * @param p_tempValue
     * @param p_psalValue
     * @return
     */
    private static double computeDensValue(final double p_presValue, // I : The pressure
	    final double p_tempValue, // I : The temperature
	    final double p_psalValue // I : The salinity
    ) {
	// ----------------------------------------------------------------------------
	// DONNEES LOCALES
	// ----------------------------------------------------------------------------

	final double l_a0 = 999.842594;
	final double l_a1 = 6.793952E-2;
	final double l_a2 = -9.09529E-3;
	final double l_a3 = 1.001685E-4;
	final double l_a4 = -1.120083E-6;
	final double l_a5 = 6.536332E-9;
	final double l_b0 = 0.824493;
	final double l_b1 = -4.0899E-3;
	final double l_b2 = 7.6438E-5;
	final double l_b3 = -8.2467E-7;
	final double l_b4 = 5.3875E-9;
	final double l_c0 = -5.72466E-3;
	final double l_c1 = 1.0227E-4;
	final double l_c2 = -1.6546E-6;
	final double l_d0 = 4.8314E-4;

	double lXp8 = 0.0;
	double lXt8 = 0.0;
	double lXs8 = 0.0;
	double lS2 = 0.0;
	double lS32 = 0.0;
	double lA = 0.0;
	double lB = 0.0;
	double lC = 0.0;
	double lD = 0.0;
	double lE = 0.0;
	double lSig = 0.0;
	double lVZero = 0.0;
	double lXkzw = 0.0;
	double lAw = 0.0;
	double lBw = 0.0;
	double lAa = 0.0;
	double lBb = 0.0;
	double lCc = 0.0;
	double lDd = 0.0;
	double lAx = 0.0;
	double lBx = 0.0;
	double lXkz = 0.0;
	double lXk = 0.0;
	double lVspe08 = 0.0;
	double lAlp = 0.0;
	double lDensite = 0.0;

	// ----------------------------------------------------------------------------
	// TRAITEMENT
	// ----------------------------------------------------------------------------

	// ********************************************************************
	//
	// function densite
	//
	// creation : Decembre 89 auteur : D.Jacolot
	//
	// objet : Calcul de l'anomalie de densite in situ
	//
	// description :
	// formule de f. j. millero et a. poisson
	// deep-sea research, vol 28a, 6, 625-629, 1981
	// working draft of s.c.o.r. (wg 81): eos 80
	//
	// valeurs de controle:
	// p = 10**4 decibars
	// t = 40 degres
	// s = 40 nsu
	// d = 59.820377 kg/m**3
	//
	// ********************************************************************

	lXp8 = p_presValue;
	lXt8 = p_tempValue;
	lXs8 = p_psalValue;
	lS2 = lXs8 * lXs8;
	lS32 = Math.sqrt(lS2 * lXs8);
	lXt8 = lXt8 * 1.00024;
	lA = (((((((((l_a5 * lXt8) + l_a4) * lXt8) + l_a3) * lXt8) + l_a2) * lXt8) + l_a1) * lXt8) + l_a0;
	lB = (((((((l_b4 * lXt8) + l_b3) * lXt8) + l_b2) * lXt8) + l_b1) * lXt8) + l_b0;
	lC = (((l_c2 * lXt8) + l_c1) * lXt8) + l_c0;
	lB = lB * lXs8;
	lC = lC * lS32;
	lD = l_d0 * lS2;
	lSig = lA + lB + lC + lD;
	lVZero = 1.0 / lSig;
	lXkzw = ((((-5.155288E-5 * lXt8) + 1.360477E-2) * lXt8) - 2.327105) * lXt8;
	lXkzw = ((lXkzw + 148.4206) * lXt8) + 19652.21;
	lAw = (((((-5.77905E-8 * lXt8) + 1.16092E-5) * lXt8) + 1.43713E-4) * lXt8) + 3.239908E-1;
	lBw = (((5.2787E-10 * lXt8) - 6.12293E-8) * lXt8) + 8.50935E-7;
	lAa = (((((-6.167E-5 * lXt8) + 1.09987E-2) * lXt8) - 0.603459) * lXt8) + 54.6746;
	lBb = (((-5.3009E-4 * lXt8) + 1.6483E-2) * lXt8) + 7.944E-2;
	lCc = (((-1.6078E-7 * lXt8) - 1.0981E-6) * lXt8) + 2.2838E-4;
	lDd = 1.91075E-5;
	lE = (((9.1697E-12 * lXt8) + 2.0816E-10) * lXt8) - 9.9348E-9;
	lAa = lAa * lXs8;
	lBb = lBb * lS32;
	lCc = lCc * lXs8;
	lDd = lDd * lS32;
	lE = lE * lXs8;
	lAx = lAw + lCc + lDd;
	lBx = lBw + lE;
	lXkz = lXkzw + lAa + lBb;
	lXk = lXkz + (lAx * lXp8) + (lBx * lXp8 * lXp8);
	lVspe08 = lVZero * (1.0 - ((0.1 * lXp8) / lXk));
	lAlp = lVspe08;
	lDensite = 1.0 / lAlp / 1000.0;

	// ----------------------------------------------------------------------------
	// ERREURS ET RETOUR
	// ----------------------------------------------------------------------------

	return lDensite;
    }

    /**
     * Compute a potential temperature
     *
     * Source : CoDatRecord.cpp (used for Coriolis treatments)
     *
     * @param p_presValue
     * @param p_tempValue
     * @param p_psalValue
     * @param p_referencePressure
     * @return
     */
    private static double computePotentialTempValue(final double p_presValue, // I : The pressure
	    final double p_tempValue, // I : The temperature
	    final double p_psalValue, // I : The salinity
	    final double p_referencePressure // I : The reference pressure
    ) {
	// ----------------------------------------------------------------------------
	// DONNEES LOCALES
	// ----------------------------------------------------------------------------

	double lP;
	double lT;
	double lS;
	double lH;
	double lXK;
	double lQ;
	double lPotentialTemperature = 0.0;

	// ----------------------------------------------------------------------------
	// TRAITEMENT
	// ----------------------------------------------------------------------------

	// ********************************************************************
	// Bryden,H.,1973,DEEP-SEA RES.,20,401-408
	// Fofonoff,N.,1977,DEEP-SEA RES.,24,489-491
	//
	// Computes the potential temperature from in-situ measurements
	// at a reference pressure (dbars) corresponding to the
	// salinity S (ppt) and temperature T (deg C) at pressure
	// P (dbars). The formula has been copied from the UNESCO
	// algorithms.
	//
	// TO COMPUTE LOCAL POTENTIAL TEMPERATURE AT PR
	// USING BRYDEN 1973 POLYNOMIAL FOR ADIABATIC LAPSE RATE
	// AND RUNGE-KUTTA 4-TH ORDER INTEGRATION ALGORITHM.
	// UNITS:
	// PRESSURE P DECIBARS
	// TEMPERATURE T DEG CELSIUS (IPTS-68)
	// SALINITY S (IPSS-78)
	// REFERENCE PRS PR DECIBARS
	// POTENTIAL TMP. THETA DEG CELSIUS
	//
	// CHECKVALUE:
	// THETA = 36.89073 C,
	// S = 40 (IPSS-78), T0 = 40 DEG C, P0 = 10000 DECIBARS, PR = 0 DECIBARS
	//
	// ********************************************************************

	lP = p_presValue;
	lT = p_tempValue * 1.00024;
	lS = p_psalValue;

	lH = p_referencePressure - lP;

	lXK = lH * computeAdiabaticTempValue(lP, lT, lS);

	lT = lT + (0.5 * lXK);

	lQ = lXK;

	lP = lP + (0.5 * lH);

	lXK = lH * computeAdiabaticTempValue(lP, lT, lS);

	lT = lT + (0.29289322 * (lXK - lQ));

	lQ = (0.58578644 * lXK) + (0.121320344 * lQ);

	lXK = lH * computeAdiabaticTempValue(lP, lT, lS);

	lT = lT + (1.707106781 * (lXK - lQ));

	lQ = (3.414213562 * lXK) - (4.121320344 * lQ);

	lP = lP + (0.5 * lH);

	lXK = lH * computeAdiabaticTempValue(lP, lT, lS);

	lPotentialTemperature = lT + ((lXK - (2.0 * lQ)) / 6.0);

	lPotentialTemperature = lPotentialTemperature / 1.00024;

	// ----------------------------------------------------------------------------
	// ERREURS ET RETOUR
	// ----------------------------------------------------------------------------

	return lPotentialTemperature;
    }

    /**
     * Compute a salinity using a pressure, a temperature and a conductivity
     *
     * Source : CoDatRecord.cpp (used for Coriolis treatments)
     *
     * @param p_presValue
     * @param p_tempValue
     * @param p_cndcValue
     * @return salinity value
     */
    private static double computeSalinity(final double p_presValue, // I : The pressure
	    final double p_tempValue, // I : The temperature
	    final double p_cndcValue // I : The conductivity
    ) {
	// ----------------------------------------------------------------------------
	// DONNEES LOCALES
	// ----------------------------------------------------------------------------

	final double l_CNO68 = 42.914;
	final double l_A0 = 0.008;
	final double l_A1 = -0.1692;
	final double l_A2 = 25.3851;
	final double l_A3 = 14.0941;
	final double l_A4 = -7.0261;
	final double l_A5 = 2.7081;
	final double l_B0 = 0.0005;
	final double l_B1 = -0.0056;
	final double l_B2 = -0.0066;
	final double l_B3 = -0.0375;
	final double l_B4 = 0.0636;
	final double l_B5 = -0.0144;
	final double l_C0 = 0.6766097;
	final double l_C1 = 2.00564E-2;
	final double l_C2 = 1.104259E-4;
	final double l_C3 = -6.9698E-7;
	final double l_C4 = 1.0031E-9;
	final double l_D1 = 3.426E-2;
	final double l_D2 = 4.464E-4;
	final double l_D3 = 4.215E-1;
	final double l_D4 = -3.107E-3;
	final double l_E1 = 2.07E-5;
	final double l_E2 = -6.37E-10;
	final double l_E3 = 3.989E-15;
	double lP = 0.0;
	double lT = 0.0;
	double lC = 0.0;
	double lR = 0.0;
	double lRTEMP = 0.0;
	double lCXP = 0.0;
	double lAXT = 0.0;
	double lBXT = 0.0;
	double lRP = 0.0;
	double lRT = 0.0;
	double lDS = 0.0;
	double lS = 0.0;

	// ----------------------------------------------------------------------------
	// TRAITEMENT
	// ----------------------------------------------------------------------------

	// ********************************************************************
	// METHOD 1 : Compute the salinity in accordance with pressure,
	// temperature and conductivity.
	// PRATICAL SALINITY SCALE 1978 : E.L. LEWIS - R.G. PERKIN DEEP-SEA
	// RESEARCH , VOL. 28A , NO 4 , PP. 307 -328 , 1981
	// "WORKING DRAFT OF S.C.R. (WG51)" RECOMMENDED METHOD
	// C(35.15.0) = CNO68 =42.914
	// DEEP-SEA RESEARCH , VOL. 23 , PP.157-165 , 1976
	// ********************************************************************
	//
	//
	// Last Change: 04/05/1993 by C. Lagadec
	// -------------
	// Change of the CNO68 value
	//
	// --------------------------------------------------------------------

	lP = p_presValue; // unit: deciBar
	lT = p_tempValue; // unit: degree
	lC = p_cndcValue * 10; // unit: ms/cm (convert from coriolis unit (S/m))

	lR = lC / l_CNO68;
	lRTEMP = (((((((l_C4 * lT) + l_C3) * lT) + l_C2) * lT) + l_C1) * lT) + l_C0;
	lCXP = ((((l_E3 * lP) + l_E2) * lP) + l_E1) * lP;
	lAXT = ((l_D4 * lT) + l_D3) * lR;
	lBXT = (((l_D2 * lT) + l_D1) * lT) + 1.0;
	lRP = (lCXP / (lAXT + lBXT)) + 1.0;
	lRT = lR / (lRP * lRTEMP);
	lDS = (lT - 15.0) / (((lT - 15.0) * 0.0162) + 1.0);
	lS = (((l_B5 * lDS) + l_A5) * Math.pow(lRT, 2.5)) + (((l_B4 * lDS) + l_A4) * lRT * lRT)
		+ (((l_B3 * lDS) + l_A3) * Math.pow(lRT, 1.5));
	lS = lS + (((l_B2 * lDS) + l_A2) * lRT) + (((l_B1 * lDS) + l_A1) * Math.pow(lRT, 0.5)) + ((l_B0 * lDS) + l_A0);

	// ----------------------------------------------------------------------------
	// ERREURS ET RETOUR
	// ----------------------------------------------------------------------------

	return lS;
    }
}
