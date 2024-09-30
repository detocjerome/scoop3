package fr.ifremer.scoop3.gui.reference;

import java.util.List;
import java.util.Map;

import fr.ifremer.scoop3.chart.model.ChartDataset;
import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewModel;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;

public class ReferenceViewModel extends CommonViewModel {

    private ChartDataset chartDataset;
    private final Map<String, List<String>> parametersOrder;

    public ReferenceViewModel(final Dataset dataset, final Report report,
	    final Map<String, List<String>> parametersOrder) {
	super(dataset, report);
	this.parametersOrder = parametersOrder;
	convertScoop3ModelToChartModel();
    }

    /**
     * Convert the scoop3 model to chart model
     */
    public void convertScoop3ModelToChartModel() {
	chartDataset = new ChartDataset();

	// For each observation
	for (final Platform platform : getDataset().getPlatforms()) {
	    for (final Observation obs : platform.getAllObservations()) {

		// Stock ZParameter values into the unique ChartPhysicalVariable
		// for this parameter
		final Parameter<? extends Number> referenceParameter = obs.getReferenceParameter();

		ChartPhysicalVariable refPhysicalVariable;
		int index = chartDataset.indexOfPhysicalVariable(referenceParameter.getCode());
		if (index == -1) {
		    refPhysicalVariable = new ChartPhysicalVariable(referenceParameter.getCode(),
			    referenceParameter.getUnit());
		    refPhysicalVariable.setReferenceParameter(true);
		    refPhysicalVariable.setIsADate(referenceParameter instanceof TimeParameter);
		    chartDataset.addPhysicalVariable(refPhysicalVariable);
		} else {
		    refPhysicalVariable = chartDataset.getPhysicalVariable(index);
		    chartDataset.setPhysicalVariable(index, refPhysicalVariable);
		}
		refPhysicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(),
			Conversions.convertNumberListToDoubleArray(referenceParameter.getValues()),
			QCValues.convertQCValuesListToCharArray(referenceParameter.getQcValues()));

		// Stock level values into the unique ChartPhysicalVariable for
		// this parameter
		ChartPhysicalVariable levelPhysicalVariable;
		index = chartDataset.indexOfPhysicalVariable(CommonViewModel.MEASURE_NUMBER);
		if (index == -1) {
		    levelPhysicalVariable = new ChartPhysicalVariable(CommonViewModel.MEASURE_NUMBER, "");
		    levelPhysicalVariable.setLevelParameter(true);
		    chartDataset.addPhysicalVariable(levelPhysicalVariable);
		} else {
		    levelPhysicalVariable = chartDataset.getPhysicalVariable(index);
		    chartDataset.setPhysicalVariable(index, levelPhysicalVariable);
		}
		final int measureCount = referenceParameter.getValues().size();
		final Double[] measureNumbers = new Double[measureCount];
		for (int i = 0; i < measureCount; i++) {
		    measureNumbers[i] = (double) (i + 1);
		}
		levelPhysicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(), measureNumbers,
			QCValues.convertQCValuesListToCharArray(referenceParameter.getQcValues()));
	    }
	}
    }

    /**
     * @return the chartDataset
     */
    public ChartDataset getChartDataset() {
	return chartDataset;
    }

    /**
     * @return the parametersOrder
     */
    public Map<String, List<String>> getParametersOrder() {
	return parametersOrder;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewModel#getStepType()
     */
    @Override
    protected STEP_TYPE getStepType() {
	return STEP_TYPE.Q2_CONTROL_AUTO_DATA;
    }

    @Override
    protected void specificPrepareForDispose() {
	chartDataset.prepareForDispose();
	chartDataset = null;
    }
}
