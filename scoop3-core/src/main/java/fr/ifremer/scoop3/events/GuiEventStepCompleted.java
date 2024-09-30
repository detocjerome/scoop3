package fr.ifremer.scoop3.events;

import fr.ifremer.scoop3.controller.worflow.StepCode;

public class GuiEventStepCompleted extends GuiEvent {

    /**
     * TRUE if error has been detected
     */
    private boolean errorDetected = false;
    /**
     * If TRUE, the state if forced to be written in DB
     */
    private final boolean forceStateUpdateInDB;
    /**
     * The Step completed
     */
    private final StepCode stepCompleted;

    /**
     * 
     */
    public GuiEventStepCompleted(final StepCode stepCompleted) {
	this(stepCompleted, false);
    }

    public GuiEventStepCompleted(final StepCode stepCompleted, final boolean forceStateUpdateInDB) {
	super(GuiEventEnum.STEP_COMPLETED);
	this.stepCompleted = stepCompleted;
	this.forceStateUpdateInDB = forceStateUpdateInDB;
    }

    /**
     * @return the stepComplete
     */
    public StepCode getStepCompleted() {
	return stepCompleted;
    }

    /**
     * @return the errorDetected
     */
    public boolean isErrorDetected() {
	return errorDetected;
    }

    /**
     * @return the forceStateUpdateInDB
     */
    public boolean isForceStateUpdateInDB() {
	return forceStateUpdateInDB;
    }

    /**
     * @param errorDetected
     * @return
     */
    public GuiEventStepCompleted setErrorDetected(final boolean errorDetected) {
	this.errorDetected = errorDetected;
	return this;
    }
}
