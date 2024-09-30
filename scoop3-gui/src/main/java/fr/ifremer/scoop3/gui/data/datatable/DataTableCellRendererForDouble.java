package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JTable;

import fr.ifremer.scoop3.model.parameter.Parameter;

public class DataTableCellRendererForDouble extends DataTableCellRendererForObject {

    /**
     *
     */
    private static final long serialVersionUID = -2470467052778701862L;

    /**
     * The double formatter
     */
    private final DecimalFormat formatter;

    public DataTableCellRendererForDouble(final DataTableModel dataTableModel, final String formatForDouble) {
	super(dataTableModel);
	formatter = new DecimalFormat(formatForDouble);
	// Parse Double.NaN in String "NaN"
	final DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
	dfs.setNaN("");
	formatter.setDecimalFormatSymbols(dfs);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.data.datatable.DataTableCellRendererAbstract#getTableCellRendererComponent(javax.swing.
     * JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected,
	    final boolean hasFocus, final int row, final int column) {
	if ((Double) value == null) {
	    value = Double.NaN;
	}
	if (value != Parameter.DOUBLE_EMPTY_VALUE) {
	    // First format the cell value as required
	    value = formatter.format(value);
	}

	// And pass it on to parent class
	return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
