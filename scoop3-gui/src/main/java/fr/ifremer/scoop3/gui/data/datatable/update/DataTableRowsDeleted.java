package fr.ifremer.scoop3.gui.data.datatable.update;

import java.util.List;

public class DataTableRowsDeleted extends DataTableUpdateAbstract {

    /**
     * List of the cells deleted in 1 Action
     */
    private final List<DataTableCellDeleted> cellsDeleted;

    /**
     * Default constructor
     * 
     * 
     */
    public DataTableRowsDeleted(final List<DataTableCellDeleted> cellsDeleted) {
	super(DataTableUpdateType.ROW_DELETE);
	this.cellsDeleted = cellsDeleted;
    }

    /**
     * @return the rowsDeleted
     */
    public List<DataTableCellDeleted> getCellsDeleted() {
	return cellsDeleted;
    }
}
