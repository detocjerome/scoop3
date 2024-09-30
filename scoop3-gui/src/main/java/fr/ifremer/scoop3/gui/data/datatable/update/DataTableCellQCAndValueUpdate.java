package fr.ifremer.scoop3.gui.data.datatable.update;

import fr.ifremer.scoop3.model.QCValues;

public class DataTableCellQCAndValueUpdate {

    private final QCValues newQCValue;
    private final Number newValue;
    private final QCValues oldQCValue;
    private final Object oldValue;
    private final int rowIndex;

    /**
     * Default constructor
     * 
     * @param rowIndex
     * @param oldQCValue
     * @param newQCValue
     * @param oldValue
     * @param newValue
     */
    public DataTableCellQCAndValueUpdate(final int rowIndex, final QCValues oldQCValue, final QCValues newQCValue,
	    final Object oldValue, final Number newValue) {
	this.rowIndex = rowIndex;
	this.oldQCValue = oldQCValue;
	this.newQCValue = newQCValue;
	this.oldValue = oldValue;
	this.newValue = newValue;
    }

    /**
     * @return the newQCValue
     */
    public QCValues getNewQCValue() {
	return newQCValue;
    }

    /**
     * @return the newValue
     */
    public Number getNewValue() {
	return newValue;
    }

    /**
     * @return the oldQCValue
     */
    public QCValues getOldQCValue() {
	return oldQCValue;
    }

    /**
     * @return the oldValue
     */
    public Object getOldValue() {
	return oldValue;
    }

    /**
     * @return the rowIndex
     */
    public int getRowIndex() {
	return rowIndex;
    }

}
