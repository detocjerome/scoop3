package fr.ifremer.scoop3.control;

import fr.ifremer.scoop3.model.Observation;

public abstract class AutomaticControlCheckParametersPresence extends AutomaticControl {

    /**
     * Controls the Parameters' presence for a single Observation
     * 
     * @param obs
     * @return true if the Control is OK
     */
    public abstract boolean performControl(Observation obs);
}
