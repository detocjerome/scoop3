package fr.ifremer.scoop3.events;

import fr.ifremer.scoop3.controller.worflow.StepCode;

public class GuiEventStartStep extends GuiEvent {

    private final StepCode stepToStart;

    public GuiEventStartStep(final StepCode stepToStart) {
	super(GuiEventEnum.START_STEP);
	this.stepToStart = stepToStart;
    }

    /**
     * @return the stepToStart
     */
    public StepCode getStepToStart() {
	return stepToStart;
    }

}
