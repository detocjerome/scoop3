package fr.ifremer.scoop3.control;

import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;

public abstract class AutomaticControlForStation extends AutomaticControl {

    public abstract boolean performControl(Observation obs1, Observation obs2, final Dataset dataset);
}
