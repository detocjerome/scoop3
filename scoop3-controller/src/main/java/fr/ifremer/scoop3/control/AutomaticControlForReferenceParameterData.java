package fr.ifremer.scoop3.control;

import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;

public abstract class AutomaticControlForReferenceParameterData extends AutomaticControl {

    /**
     * @return TRUE if this test is a Presence test
     */
    public abstract boolean isPresenceTest();

    /**
     * Controls the Reference parameter of the observation
     * 
     * @param obs
     * @param dataset
     * @return true if the Control is OK
     */
    public abstract boolean performControl(Observation obs, Dataset dataset);
}
