package fr.ifremer.scoop3.gui.data.datatable.update;

public abstract class DataTableUpdateAbstract {

    public enum DataTableUpdateType {
	QC_AND_VALUE_UPDATE, //
	QC_UPDATE, //
	ROW_DELETE, //
    }

    private final DataTableUpdateType dataTableUpdateType;

    protected DataTableUpdateAbstract(final DataTableUpdateType dataTableUpdateType) {
	this.dataTableUpdateType = dataTableUpdateType;
    }

    /**
     * @return the dataTableUpdateType
     */
    public DataTableUpdateType getDataTableUpdateType() {
	return dataTableUpdateType;
    }
}
