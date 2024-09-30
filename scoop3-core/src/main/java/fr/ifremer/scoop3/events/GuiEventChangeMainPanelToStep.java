package fr.ifremer.scoop3.events;

import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;

public class GuiEventChangeMainPanelToStep extends GuiEvent {

    private final StepCode stepCode;
    private final SubStep subStep;

    // DO NOT USE THIS CONSTRUCTOR !!! NullPointerException !!!
    // public GuiEventChangeMainPanelToStep(final StepCode stepCode) {
    // this(stepCode, null);
    // }

    public GuiEventChangeMainPanelToStep(final StepCode stepCode, final SubStep subStep) {
	super(GuiEventEnum.CHANGE_MAIN_PANEL_TO_STEP);
	this.stepCode = stepCode;
	this.subStep = subStep;
    }

    /**
     * @return the stepCode
     */
    public StepCode getStep() {
	return stepCode;
    }

    /**
     * @return the subStep
     */
    public SubStep getSubStep() {
	return subStep;
    }
}
