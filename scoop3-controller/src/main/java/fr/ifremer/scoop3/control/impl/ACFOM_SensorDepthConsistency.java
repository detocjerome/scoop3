package fr.ifremer.scoop3.control.impl;

import java.text.MessageFormat;

import fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.Sensor;

public class ACFOM_SensorDepthConsistency extends AutomaticControlForObservationMetadata {

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(getClass()
		.getSimpleName());
	message.setStatus("OK");
	return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.control.AutomaticControlForObservationMetadata#performControl(fr.ifremer.scoop3.model.Observation
     * , fr.ifremer.scoop3.model.Observation, fr.ifremer.scoop3.model.Dataset,
     * fr.ifremer.scoop3.io.impl.AbstractDataBaseManager)
     */
    @Override
    public boolean performControl(final Observation obs1, final Observation obs2, final Dataset dataset,
	    final AbstractDataBaseManager abstractDataBaseManager, final Report report) {
	boolean controlOK = true;

	if (dataset.getDatasetType() == DatasetType.TIMESERIE || dataset.getDatasetType() == DatasetType.TRAJECTORY) {
	    final double bottomDepthDble = obs1.getOceanDepth().getValueAsDouble();

	    final Sensor sensor = obs1.getSensor();

	    if ((sensor.getNominalDepth() != null) && (sensor.getDistanceFromBottom() != null)) {

		final double nominalDepthDble = sensor.getNominalDepth().getValueAsDouble();
		final double distanceFromBottomDble = sensor.getDistanceFromBottom().getValueAsDouble();

		// Check if the 3 values are positives
		controlOK &= (bottomDepthDble > 0);
		controlOK &= (nominalDepthDble > 0);
		controlOK &= (distanceFromBottomDble > 0);

		// Check if the Sum is correct
		controlOK &= (bottomDepthDble == (nominalDepthDble + distanceFromBottomDble));

		if (!controlOK) {
		    CAMetadataErrorMessageItem errorMessageItem = new CAMetadataErrorMessageItem(
			    CAMetadataErrorMessageItem.QC_TO_UPDATE.OBS_OCEAN_DEPTH,
			    obs1.getId(),
			    MessageFormat.format(
				    Messages.getMessage("bpc-controller.automatic-control-for-observation.sensor-depth.consistency")
				    + " (" + Messages.getMessage("bpc-gui.obs-metadata.bottom-depth") + ")",
				    nominalDepthDble, distanceFromBottomDble, bottomDepthDble), QCValues.QC_3);
		    errorMessageItem.setBlockingError(false);
		    if (!report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).containsCAMetadataErrorMessageItem(
			    errorMessageItem)) {
			addErrorMessageItem(errorMessageItem);
			// QCs are set to 3
			obs1.getOceanDepth().setQc(QCValues.getWorstQC(obs1.getOceanDepth().getQc(), QCValues.QC_3));
		    }

		    errorMessageItem = new CAMetadataErrorMessageItem(
			    CAMetadataErrorMessageItem.QC_TO_UPDATE.SENSOR_NOMINAL_DEPTH,
			    obs1.getId(),
			    MessageFormat.format(
				    Messages.getMessage("bpc-controller.automatic-control-for-observation.sensor-depth.consistency")
				    + " ("
				    + Messages.getMessage("bpc-gui.obs-metadata.sensor-nominal-depth")
				    + ")", nominalDepthDble, distanceFromBottomDble, bottomDepthDble),
				    QCValues.QC_3);
		    errorMessageItem.setBlockingError(false);
		    if (!report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).containsCAMetadataErrorMessageItem(
			    errorMessageItem)) {
			addErrorMessageItem(errorMessageItem);
			// QCs are set to 3
			sensor.getNominalDepth().setQc(
				QCValues.getWorstQC(sensor.getNominalDepth().getQc(), QCValues.QC_3));
		    }

		    errorMessageItem = new CAMetadataErrorMessageItem(
			    CAMetadataErrorMessageItem.QC_TO_UPDATE.SENSOR_DIST_FROM_BOTTOM,
			    obs1.getId(),
			    MessageFormat.format(
				    Messages.getMessage("bpc-controller.automatic-control-for-observation.sensor-depth.consistency")
				    + " ("
				    + Messages.getMessage("bpc-gui.obs-metadata.sensor-distance-bottom")
				    + ")", nominalDepthDble, distanceFromBottomDble, bottomDepthDble),
				    QCValues.QC_3);
		    errorMessageItem.setBlockingError(false);
		    if (!report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).containsCAMetadataErrorMessageItem(
			    errorMessageItem)) {
			addErrorMessageItem(errorMessageItem);
			// QCs are set to 3
			sensor.getDistanceFromBottom().setQc(
				QCValues.getWorstQC(sensor.getDistanceFromBottom().getQc(), QCValues.QC_3));
		    }
		} else {
		    // QCs are set to 1
		    obs1.getOceanDepth().setQc(QCValues.getWorstQC(obs1.getOceanDepth().getQc(), QCValues.QC_1));
		    sensor.getNominalDepth()
		    .setQc(QCValues.getWorstQC(sensor.getNominalDepth().getQc(), QCValues.QC_1));
		    sensor.getDistanceFromBottom().setQc(
			    QCValues.getWorstQC(sensor.getDistanceFromBottom().getQc(), QCValues.QC_1));
		}
	    }
	}

	return controlOK;
    }

}
