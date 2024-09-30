package fr.ifremer.scoop3.control;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;

public abstract class AutomaticControlForObservationMetadata extends AutomaticControl {

    /**
     * Controls Metadata for a single observation (obs1 only) or a couple of Observations
     * 
     * @param obs1
     * @param obs2
     * @param dataset
     * @param abstractDataBaseManager
     * @param report
     * @return true if the Control is OK
     */
    public abstract boolean performControl(final Observation obs1, final Observation obs2, final Dataset dataset,
	    final AbstractDataBaseManager abstractDataBaseManager, Report report);
}
