package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.Parameter;

public class DataTableCellRendererForObject extends DefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -8907079356387425169L;

    private final DataTableModel dataTableModel;

    protected DataTableCellRendererForObject(final DataTableModel dataTableModel) {
	this.dataTableModel = dataTableModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     * java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	    final boolean hasFocus, final int row, final int column) {
	final int modelRow = table.convertRowIndexToModel(row);
	final int modelColumn = table.convertColumnIndexToModel(column);

	final QCValues qcValue = dataTableModel.getQCValuesForCell(modelRow, modelColumn);

	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	if (value == Parameter.DOUBLE_EMPTY_VALUE) {
	    setBackground(Color.WHITE);
	    setForeground(Color.WHITE);
	} else {
	    if (qcValue != null) {
		Color backgroundColor = qcValue.getColor();
		final Color foregroundColor = qcValue.getForegroundColor();

		if (isSelected) {
		    backgroundColor = backgroundColor.darker();
		}
		setBackground(backgroundColor);
		setForeground(foregroundColor);
	    }
	}
	return this;
    }
}
