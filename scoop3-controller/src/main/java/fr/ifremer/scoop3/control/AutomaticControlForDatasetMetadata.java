package fr.ifremer.scoop3.control;

import fr.ifremer.scoop3.model.Dataset;

public abstract class AutomaticControlForDatasetMetadata extends AutomaticControl {

    /**
     * Performs the control on the Dataset Metadata
     * 
     * @param dataset
     * 
     * @return return true if the control is OK
     */
    public abstract boolean performControl(final Dataset dataset);
}
