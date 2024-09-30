package fr.ifremer.scoop3.control.impl;

import java.text.MessageFormat;

import fr.ifremer.scoop3.bathyClimato.BathyClimatologyManager;
import fr.ifremer.scoop3.bathyClimato.BathyException;
import fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;

public abstract class ACFOM_StationPositionInSeaAbstract extends AutomaticControlForObservationMetadata {

    private String automaticControlStatus;
    private String etopoFilename;

    protected ACFOM_StationPositionInSeaAbstract(final String etopoFilename) {
	if (BathyClimatologyManager.isValidEtopoFile(etopoFilename)) {
	    this.etopoFilename = etopoFilename;
	} else {
	    // Set ETOPO1 by default
	    this.etopoFilename = BathyClimatologyManager.ETOPO1_FILE;
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
	message.setStatus(automaticControlStatus);
	message.addParameter("etopoFilename", etopoFilename);
	return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata#performControl(fr.ifremer.scoop3.model.
     * Observation , fr.ifremer.scoop3.model.Observation, fr.ifremer.scoop3.model.Dataset)
     */
    @Override
    public boolean performControl(final Observation obs1, final Observation obs2, final Dataset dataset,
	    final AbstractDataBaseManager abstractDataBaseManager, final Report report) {
	boolean controlOK = true;

	automaticControlStatus = "OK";

	if (canPerformControl(obs1)) {
	    try {
		final DoubleValueAndQC latitude = getLatitude(obs1);
		final DoubleValueAndQC longitude = getLongitude(obs1);

		if ((latitude != null) && (longitude != null)) {
		    // Check latitude and Longitude
		    short bathymetry = BathyClimatologyManager.getSingleton().getBathymetry(etopoFilename,
			    latitude.getValueAsDouble(), longitude.getValueAsDouble());
		    if (!BathyClimatologyManager.getSingleton().isBathyNull()) {
			bathymetry = BathyClimatologyManager.getSingleton().getBathymetry(etopoFilename,
				latitude.getValueAsDouble(), longitude.getValueAsDouble());
		    } else {
			controlOK = false;
		    }

		    if (bathymetry > 0) {
			controlOK = false;

			String metadata = getMetadataToUpdate();
			CAMetadataErrorMessageItem messageToAdd = new CAMetadataErrorMessageItem(metadata, obs1.getId(),
				MessageFormat.format(Messages.getMessage(getErrorMessageName()) + " (" + metadata + ")",
					bathymetry),
				QCValues.QC_4);

			if (!report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA)
				.containsCAMetadataErrorMessageItem(messageToAdd)) {
			    // QC is set to 4
			    latitude.setQc(QCValues.getWorstQC(latitude.getQc(), QCValues.QC_4));
			    addErrorMessageItem(messageToAdd);
			}

			metadata = getMetadataToUpdate2();
			messageToAdd = new CAMetadataErrorMessageItem(metadata, obs1.getId(), MessageFormat
				.format(Messages.getMessage(getErrorMessageName()) + " (" + metadata + ")", bathymetry),
				QCValues.QC_4);
			if (!report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA)
				.containsCAMetadataErrorMessageItem(messageToAdd)) {
			    // QC is set to 4
			    longitude.setQc(QCValues.getWorstQC(longitude.getQc(), QCValues.QC_4));
			    addErrorMessageItem(messageToAdd);
			}

		    } else {
			// QC is set to 1
			latitude.setQc(QCValues.getWorstQC(latitude.getQc(), QCValues.QC_1));
			longitude.setQc(QCValues.getWorstQC(longitude.getQc(), QCValues.QC_1));

			// FAE 0055472
			if ((obs1.getLatitude().getQcValues().size() == 1)
				&& (obs1.getLongitude().getQcValues().size() == 1)) {
			    obs1.getLatitude().getQcValues().set(0,
				    QCValues.getWorstQC(latitude.getQc(), QCValues.QC_1));
			    obs1.getLongitude().getQcValues().set(0,
				    QCValues.getWorstQC(longitude.getQc(), QCValues.QC_1));
			}
		    }
		}
	    } catch (final BathyException e) {
		automaticControlStatus = "KO - bathy exception : " + e.getMessage();
	    }
	}

	return controlOK;
    }

    /**
     * @param obs
     * @return true if the test can be performed
     */
    protected abstract boolean canPerformControl(Observation obs);

    /**
     * @return the name of the error message for the Region test
     */
    protected abstract String getErrorMessageName();

    /**
     * @param obs
     * @return the latitude to use for the test
     */
    protected abstract DoubleValueAndQC getLatitude(Observation obs);

    /**
     * @param obs
     * @return the longitude to use for the test
     */
    protected abstract DoubleValueAndQC getLongitude(Observation obs);

    /**
     * Used to create the CAObsMetadataErrorMessageItem
     *
     * @return
     */
    protected abstract String getMetadataToUpdate();

    /**
     * Used to create the CAObsMetadataErrorMessageItem
     *
     * @return
     */
    protected abstract String getMetadataToUpdate2();
}
