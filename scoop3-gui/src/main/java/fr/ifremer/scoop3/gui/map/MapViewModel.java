package fr.ifremer.scoop3.gui.map;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewModel;
import fr.ifremer.scoop3.model.Dataset;

public class MapViewModel extends CommonViewModel {

    public MapViewModel(final Dataset dataset, final Report report) {
	super(dataset, report);
    }

    @Override
    protected void specificPrepareForDispose() {
	// nothing to do here
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.gui.common.CommonViewModel#getStepType()
     */
    @Override
    protected STEP_TYPE getStepType() {
	return STEP_TYPE.Q1_CONTROL_AUTO_METADATA;
    }
}
