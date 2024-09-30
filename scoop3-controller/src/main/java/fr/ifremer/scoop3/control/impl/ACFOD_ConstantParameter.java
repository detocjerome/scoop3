package fr.ifremer.scoop3.control.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fr.ifremer.scoop3.control.AutomaticControlForObservationData;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.gui.home.HomeViewController;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;

public class ACFOD_ConstantParameter extends AutomaticControlForObservationData {

    private double defaultDetla;
    private final HashMap<String, Double> paramsDelta = new HashMap<>();

    /**
     * Liste des paramêtres à ignorer pour le contrôle.
     */
    private static final List<String> IGNORING_PARAMETER_CODE_LIST = Arrays.asList("DENS");

    public ACFOD_ConstantParameter() {
	this("0", null);
    }

    public ACFOD_ConstantParameter(final String delatStr) {
	this(delatStr, null);
    }

    /**
     * Check if oceanic parameters are constant comparing the min and max values to a given Delta
     *
     * @param delatStr
     *            the default delta to use if there is no specific delta
     * @param specificParametersDelta
     *            Other params works by couple. i.e : TEMP,0.5;PSAL,1 means the delta for TEMP is 0.5 et for PSAL is 1
     */
    public ACFOD_ConstantParameter(final String delatStr, final String specificParametersDelta) {

	try {
	    defaultDetla = Double.valueOf(delatStr);
	} catch (final NumberFormatException nfe) {
	    defaultDetla = 0d;
	}

	if (specificParametersDelta != null) {
	    for (final String specificParameterDelta : specificParametersDelta.trim().split(";")) {
		final String param = specificParameterDelta.split(",")[0];
		final String deltaStr = specificParameterDelta.split(",")[1];

		double detla;
		try {
		    detla = Float.valueOf(deltaStr.replace(",", "."));
		} catch (final NumberFormatException nfe) {
		    detla = defaultDetla;
		}
		paramsDelta.put(param, detla);
	    }
	}
    }

    /*
     * De manière générale, les contrôles automatiques ne doivent pas passer sur les données en valeur par défaut (et
     * donc avec un flag 9). (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.control.AutomaticControlForObservationData#performControl(fr.ifremer.scoop3.model.Observation,
     * fr.ifremer.scoop3.io.impl.AbstractDataBaseManager, fr.ifremer.scoop3.gui.home.HomeViewController)
     */
    @Override
    public boolean performControl(final Observation obs, final AbstractDataBaseManager abstractDataBaseManager,
	    final HomeViewController homeViewController) {
	boolean controlOK = true;

	for (final String currentParamCode : obs.getOceanicParameters().keySet()) {
	    // ignore parameter if in the list
	    if (!IGNORING_PARAMETER_CODE_LIST.contains(currentParamCode)) {

		final OceanicParameter parameter = obs.getOceanicParameters().get(currentParamCode);
		final double delta = (paramsDelta.containsKey(currentParamCode)) ? paramsDelta.get(currentParamCode)
			: defaultDetla;

		double minValue = Double.MAX_VALUE;
		double maxValue = Double.NEGATIVE_INFINITY;

		int nbValues = 0;
		for (int i = 0; i < parameter.getValues().size(); i++) {

		    final Double value = parameter.getValues().get(i);
		    final QCValues qc = parameter.getQcValues().get(i);

		    // De manière générale, les contrôles automatiques ne doivent pas passer sur les données en valeur
		    // par
		    // défaut (et donc avec un flag 9).
		    if (AutomaticControlForObservationData.isValueToControl(qc)) {
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);
			nbValues++;
		    }
		}

		if ((minValue != Double.MAX_VALUE) && (maxValue != Double.NEGATIVE_INFINITY) && (nbValues > 1)
			&& ((maxValue - minValue) <= delta)) {
		    // The parameter is considered as constant

		    final String paramCode = parameter.getCode();
		    final QCValues flagAuto = QCValues.QC_4;
		    final boolean isErrorOnReferenceParameter = false;

		    // Check if all QC are the same and equal to QC6, QC7 or QCQ
		    final int nbQC = parameter.getQcValues().size();
		    boolean ignoreConstantParam = true;
		    QCValues firstQC = null;
		    for (int index = 0; index < nbQC; index++) {
			if ((firstQC == null) && AutomaticControlForObservationData
				.isValueToControl(parameter.getQcValues().get(index))) {
			    firstQC = parameter.getQcValues().get(index);
			    break;
			}
		    }

		    if ((firstQC != null) && !firstQC.equals(QCValues.QC_6) && !firstQC.equals(QCValues.QC_7)
			    && !firstQC.equals(QCValues.QC_Q)) {
			ignoreConstantParam = false;
		    }

		    if (ignoreConstantParam && (firstQC != null)) {
			for (int index = 0; index < nbQC; index++) {
			    if (ignoreConstantParam
				    && AutomaticControlForObservationData
					    .isValueToControl(parameter.getQcValues().get(index))
				    && !firstQC.equals(parameter.getQcValues().get(index))) {
				ignoreConstantParam = false;
				break;
			    }
			}
		    }

		    if (!ignoreConstantParam) {
			// Déprécier les QCs à 4 si nécessaire
			for (int index = 0; index < nbQC; index++) {

			    // De manière générale, les contrôles automatiques ne doivent pas passer sur les données en
			    // valeur par
			    // défaut (et donc avec un flag 9).
			    if (AutomaticControlForObservationData
				    .isValueToControl(parameter.getQcValues().get(index))) {

				parameter.getQcValues().set(index,
					QCValues.getWorstQC(parameter.getQcValues().get(index), QCValues.QC_4));

				final Number paramValue = parameter.getValues().get(index);
				final int referenceIndex = index;
				final String referenceValue = String
					.valueOf(obs.getReferenceParameter().getValues().get(index));

				// Add an error message
				addErrorMessageItem(new CADataLightErrorMessageItem(obs.getId(),
					MessageFormat.format(
						Messages.getMessage(
							"controller.automatic-control-for-observation.constant-parameter"),
						currentParamCode, delta),
					paramCode, referenceValue, referenceIndex, paramValue,
					String.valueOf(paramValue), flagAuto, isErrorOnReferenceParameter));
			    }

			}

			controlOK = false;

			persistError(abstractDataBaseManager, homeViewController, obs, currentParamCode, delta);
		    }
		}
	    }
	}
	return controlOK;
    }

    /**
     * Nothing to do here, but this method can be override if needed.
     *
     * @param abstractDataBaseManager
     * @param homeViewController
     * @param obs
     * @param currentParamCode
     * @param delta
     */
    protected void persistError(final AbstractDataBaseManager abstractDataBaseManager,
	    final HomeViewController homeViewController, final Observation obs, final String currentParamCode,
	    final double delta) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControl#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(
		getClass().getSimpleName());
	message.setStatus("OK");
	message.addParameter("defaultDetla", defaultDetla);

	for (final String paramCode : paramsDelta.keySet()) {
	    message.addParameter("detla_for_" + paramCode, paramsDelta.get(paramCode));
	}

	return message;
    }

}
