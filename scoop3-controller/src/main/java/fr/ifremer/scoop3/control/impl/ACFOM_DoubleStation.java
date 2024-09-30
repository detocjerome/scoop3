package fr.ifremer.scoop3.control.impl;

import java.text.MessageFormat;

import fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem.QC_TO_UPDATE;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.tools.ComputeSpeed;

public class ACFOM_DoubleStation extends AutomaticControlForObservationMetadata {

    // By default, the positionDeltaInMiles is set to 1
    private static final int DEFAULT_VALUE_FOR_POSITION = 1;
    // By default, the timeDeltaInMinutes is set to 1
    private static final int DEFAULT_VALUE_FOR_TIME = 1;
    // By default, the immersionDeltaInDBar is set to 4
    private static final int DEFAULT_VALUE_FOR_IMM = 4;

    private int positionDeltaInMiles = DEFAULT_VALUE_FOR_POSITION;
    private int timeDeltaInMinutes = DEFAULT_VALUE_FOR_TIME;
    private int immersionDeltaInDBar = DEFAULT_VALUE_FOR_IMM;

    public ACFOM_DoubleStation(final String positionDeltaStr, final String timeDeltaStr,
	    final String immersionDeltaStr) {
	try {
	    positionDeltaInMiles = Integer.parseInt(positionDeltaStr);
	} catch (final NumberFormatException nfe) {
	    SC3Logger.LOGGER.error(nfe.getMessage(), nfe);
	}
	try {
	    timeDeltaInMinutes = Integer.parseInt(timeDeltaStr);
	} catch (final NumberFormatException nfe) {
	    SC3Logger.LOGGER.error(nfe.getMessage(), nfe);
	}
	try {
	    immersionDeltaInDBar = Integer.parseInt(immersionDeltaStr);
	} catch (final NumberFormatException nfe) {
	    SC3Logger.LOGGER.error(nfe.getMessage(), nfe);
	}
    }

    public ACFOM_DoubleStation() {
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
	message.addParameter("positionDeltaInMiles", positionDeltaInMiles);
	message.addParameter("timeDeltaInMinutes", timeDeltaInMinutes);
	message.addParameter("immersionDeltaInDBar", immersionDeltaInDBar);
	return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata#performControl(fr.ifremer.scoop3.model.
     * Observation , fr.ifremer.scoop3.model.Observation, fr.ifremer.scoop3.model.Dataset,
     * fr.ifremer.scoop3.io.impl.AbstractDataBaseManager)
     */
    @Override
    public boolean performControl(final Observation obs1, final Observation obs2, final Dataset dataset,
	    final AbstractDataBaseManager abstractDataBaseManager, final Report report) {
	boolean controlOK = true;

	if (obs2 != null) {

	    final double lat1 = obs1.getLatitude().getValues().get(0);
	    final double lon1 = obs1.getLongitude().getValues().get(0);
	    final double lat2 = obs2.getLatitude().getValues().get(0);
	    final double lon2 = obs2.getLongitude().getValues().get(0);

	    final double distanceInNauticalMiles = ComputeSpeed.distance(lat1, lon1, lat2, lon2, "N");

	    final long time1 = obs1.getTime().getValues().get(0);
	    final long time2 = obs2.getTime().getValues().get(0);

	    // division by 60.0d to avoid implicit cast in int
	    final double numberOfMinutes = Math.abs((time2 - time1) / 1000 / 60.0d);

	    // This control is only for Profiles and Trajectories
	    if ((dataset.getDatasetType() == DatasetType.PROFILE)
		    || (dataset.getDatasetType() == DatasetType.TRAJECTORY)) {

		if ((distanceInNauticalMiles < positionDeltaInMiles) && (numberOfMinutes < timeDeltaInMinutes)) {
		    controlOK = false;

		    final CAMetadataErrorMessageItem caMetadataErrorMessageItem = new CAMetadataErrorMessageItem(
			    QC_TO_UPDATE.NOTHING_TO_UPDATE, obs1.getId(),
			    MessageFormat.format(
				    Messages.getMessage(
					    "controller.automatic-control-for-observation.double-station.profile-or-trajectory"),
				    obs2.getId(), String.format("%.2f", distanceInNauticalMiles), positionDeltaInMiles,
				    String.format("%.2f", numberOfMinutes), timeDeltaInMinutes),
			    null);
		    caMetadataErrorMessageItem.setObs2Id(obs2.getId());
		    addErrorMessageItem(caMetadataErrorMessageItem);
		}
	    }
	    // This control is only for Timeseries
	    else if (dataset.getDatasetType() == DatasetType.TIMESERIE) {

		final double sensorDepth1 = obs1.getSensor().getNominalDepth().getValueAsDouble();
		final double sensorDepth2 = obs2.getSensor().getNominalDepth().getValueAsDouble();

		final double Zdelta = Math.abs(sensorDepth1 - sensorDepth2);

		if ((distanceInNauticalMiles < positionDeltaInMiles) && (numberOfMinutes < timeDeltaInMinutes)
			&& (Zdelta < immersionDeltaInDBar)) {
		    controlOK = false;

		    final CAMetadataErrorMessageItem caMetadataErrorMessageItem = new CAMetadataErrorMessageItem(
			    QC_TO_UPDATE.NOTHING_TO_UPDATE, obs1.getId(),
			    MessageFormat.format(
				    Messages.getMessage(
					    "controller.automatic-control-for-observation.double-station.timeserie"),
				    obs2.getId(), String.format("%.2f", distanceInNauticalMiles), positionDeltaInMiles,
				    String.format("%.2f", numberOfMinutes), timeDeltaInMinutes,
				    String.format("%.2f", Zdelta), immersionDeltaInDBar),
			    null);
		    caMetadataErrorMessageItem.setObs2Id(obs2.getId());
		    addErrorMessageItem(caMetadataErrorMessageItem);
		}
	    }

	}

	return controlOK;
    }
}
