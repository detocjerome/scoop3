package fr.ifremer.scoop3.gui.data.datatable.update;

import java.util.ArrayList;
import java.util.HashMap;

public class DataTableQCUpdate extends DataTableUpdateAbstract {

    private final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables;

    public DataTableQCUpdate(final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables) {
	super(DataTableUpdateType.QC_UPDATE);
	this.updatesForVariables = updatesForVariables;
    }

    /**
     * @return the updatesForVariables
     */
    public HashMap<String, ArrayList<DataTableCellQCUpdate>> getUpdatesForVariables() {
	return updatesForVariables;
    }

}
