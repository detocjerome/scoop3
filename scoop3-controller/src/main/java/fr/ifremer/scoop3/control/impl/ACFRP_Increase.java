package fr.ifremer.scoop3.control.impl;

import fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public abstract class ACFRP_Increase extends AutomaticControlForReferenceParameterData {

    /**
     * By default, the numberOfValuesToCheckForInvertedProfile is set to 15
     */
    private static final int DEFAULT_VALUE = 15;

    /**
     * To check if a Profile is inverted, we check a given number of starting and ending values
     */
    private int numberOfValuesToCheckForInvertedProfile = DEFAULT_VALUE;

    /**
     * Default constructor
     */
    protected ACFRP_Increase() {
    }

    /**
     * Constructor with a given "numberOfValuesToCheckForInvertedProfile"
     */
    protected ACFRP_Increase(final String propertyValues) {
	try {
	    numberOfValuesToCheckForInvertedProfile = Integer.parseInt(propertyValues);
	} catch (final NumberFormatException nfe) {
	    SC3Logger.LOGGER.error(nfe.getMessage(), nfe);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(
		getClass().getSimpleName());
	message.setStatus("OK");
	message.addParameter("numberOfValuesToCheckForInvertedProfile", numberOfValuesToCheckForInvertedProfile);
	return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#isPresenceTest()
     */
    @Override
    public boolean isPresenceTest() {
	return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameter#performControl(fr.ifremer.scoop3.model.
     * Observation )
     */
    @Override
    public boolean performControl(final Observation obs, final Dataset dataset) {
	boolean controlOK;

	boolean alwaysDecrease = false;
	if (dataset.getDatasetType() == DatasetType.PROFILE) {
	    alwaysDecrease = checkReferenceParameterAlwaysDecrease(obs);
	}

	if (alwaysDecrease) {
	    controlOK = false;

	    // Remove all existing ErrorMessage to keep only this one
	    getErrorMessageItemsAndClear();

	    final String paramCode = obs.getReferenceParameter().getCode();
	    final String referenceValue = "-";
	    final int referenceIndex = -1;
	    final QCValues flagAuto = QCValues.QC_4;
	    final boolean isErrorOnReferenceParameter = true;
	    addErrorMessageItem(new CADataLightErrorMessageItem(obs.getId(),
		    Messages.getMessage("controller.automatic-control-for-reference-param.inverted"), paramCode,
		    referenceValue, referenceIndex, null, "-", flagAuto, isErrorOnReferenceParameter));

	    // Backup new QC
	    obs.setQc(QCValues.QC_4);
	} else {
	    controlOK = checkReferenceParameter(obs, dataset);
	}

	return controlOK;
    }

    /**
     * If the Reference Parameter is not strictly growing, its QCValue is set to QC_4
     *
     * @param obs
     * @param dataset
     */
    private boolean checkReferenceParameter(final Observation obs, final Dataset dataset) {
	boolean controlOK = true;
	QCValues qcToSet;

	final boolean isWaterBottle = observationComeFromWaterBottles(obs, dataset);

	Number previousValue = obs.getReferenceParameter().getValues().get(0);
	qcToSet = QCValues.getWorstQC(obs.getReferenceParameter().getQcValues().get(0), QCValues.QC_1);
	obs.getReferenceParameter().getQcValues().set(0, qcToSet);

	final int numberOfValues = obs.getReferenceParameter().getValues().size();
	for (int index = 1; index < numberOfValues; index++) {
	    final Number currentValue = obs.getReferenceParameter().getValues().get(index);

	    if (dataset.getDatasetType() == DatasetType.PROFILE) {
		// As we are in a Profile, the Reference Parameter is Z (Double)
		// si isWaterBottle && "non Croissance ou palier" => erreur
		if (isWaterBottle && (currentValue.doubleValue() < previousValue.doubleValue())) {
		    controlOK = false;
		    setErrorOnIndex(obs, index);
		} else
		// As we are in a Profile, the Reference Parameter is Z (Double)
		// si !isWaterBottle && "non Croissance stricte" => erreur
		if (!isWaterBottle && (currentValue.doubleValue() <= previousValue.doubleValue())) {
		    controlOK = false;
		    setErrorOnIndex(obs, index);
		} else {
		    previousValue = currentValue;
		    qcToSet = QCValues.getWorstQC(obs.getReferenceParameter().getQcValues().get(index), QCValues.QC_1);
		    obs.getReferenceParameter().getQcValues().set(index, qcToSet);
		}
	    } else if ((dataset.getDatasetType() == DatasetType.TIMESERIE)
		    || (dataset.getDatasetType() == DatasetType.TRAJECTORY)) {
		// As we are in a Timeserie or a Trajectory, the Reference Parameter is Time (Long)
		if (currentValue.longValue() < previousValue.longValue()) {
		    controlOK = false;
		    setErrorOnIndex(obs, index);
		} else {
		    previousValue = currentValue;
		    qcToSet = QCValues.getWorstQC(obs.getReferenceParameter().getQcValues().get(index), QCValues.QC_1);
		    obs.getReferenceParameter().getQcValues().set(index, qcToSet);
		}
	    }
	}

	return controlOK;
    }

    /**
     * Check if a Profile is inverted
     *
     * @param obs
     * @param controlOK
     * @return
     */
    private boolean checkReferenceParameterAlwaysDecrease(final Observation obs) {
	// Faux si le paramètre croit au moins entre deux valeurs de la plage controlée
	boolean alwaysDecrease = true;

	final int numberOfValues = obs.getReferenceParameter().getValues().size();

	// Une observation qui n'a qu'une mesure est obligatoirement OK
	if (numberOfValues < 2) {
	    alwaysDecrease = false;
	    return alwaysDecrease;
	}

	// Check starting values
	Number previousValue = obs.getReferenceParameter().getValues().get(0);
	for (int index = 1; alwaysDecrease && (index < numberOfValuesToCheckForInvertedProfile)
		&& (index < numberOfValues); index++) {
	    final Number currentValue = obs.getReferenceParameter().getValues().get(index);

	    // Except the TimeParameter, all parameters are Double
	    // As we are in a Profile, the Reference Parameter is Z (Double)
	    if (currentValue.doubleValue() >= previousValue.doubleValue()) {
		alwaysDecrease = false;
	    }

	    previousValue = currentValue;
	}

	// Check ending values
	final int previousValueIndex = numberOfValues - numberOfValuesToCheckForInvertedProfile;
	if (previousValueIndex > 0) {
	    for (int index = previousValueIndex; alwaysDecrease && (index < numberOfValues); index++) {
		final Number currentValue = obs.getReferenceParameter().getValues().get(index);

		// Except the TimeParameter, all parameters are Double
		// As we are in a Profile, the Reference Parameter is Z (a Double)
		if (currentValue.doubleValue() >= previousValue.doubleValue()) {
		    alwaysDecrease = false;
		}

		previousValue = currentValue;
	    }
	}

	return alwaysDecrease;
    }

    /**
     * An error has been detected. Set the QCValue and memorize the error message if needed.
     *
     * @param obs
     * @param index
     */
    private void setErrorOnIndex(final Observation obs, final int index) {
	obs.getReferenceParameter().getQcValues().set(index, QCValues.QC_4);

	final String paramCode = obs.getReferenceParameter().getCode();
	final int referenceIndex = index;
	final QCValues flagAuto = QCValues.QC_4;
	final boolean isErrorOnReferenceParameter = true;
	addErrorMessageItem(new CADataLightErrorMessageItem(obs,
		Messages.getMessage("controller.automatic-control-for-reference-param.not-always-growing"), paramCode,
		referenceIndex, flagAuto, isErrorOnReferenceParameter));
    }

    /**
     * If the observation has been done with Water Bottles (return true), the increase check is not performed.
     *
     * @param obs
     * @param dataset
     * @return
     */
    protected abstract boolean observationComeFromWaterBottles(Observation obs, Dataset dataset);
}
