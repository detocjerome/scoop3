package fr.ifremer.scoop3.gui.data.datatable.update;

import java.util.ArrayList;
import java.util.HashMap;

public class DataTableQCAndValueUpdate extends DataTableUpdateAbstract {

    private final HashMap<String, ArrayList<DataTableCellQCAndValueUpdate>> updatesForVariables;

    public DataTableQCAndValueUpdate(final HashMap<String, ArrayList<DataTableCellQCAndValueUpdate>> updatesForVariables) {
	super(DataTableUpdateType.QC_AND_VALUE_UPDATE);
	this.updatesForVariables = updatesForVariables;
    }

    /**
     * @return the updatesForVariables
     */
    public HashMap<String, ArrayList<DataTableCellQCAndValueUpdate>> getUpdatesForVariables() {
	return updatesForVariables;
    }

}
