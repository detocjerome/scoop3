package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;

public class JTextAreaTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = -4094451231410246447L;
    private final JTextArea jTextArea;

    public JTextAreaTableCellEditor() {
	jTextArea = new JTextArea();
	jTextArea.setWrapStyleWord(true);
	jTextArea.setLineWrap(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
	return jTextArea.getText().trim();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean,
     * int, int)
     */
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
	    final int row, final int column) {
	// FIXME : ne fonctionne pas correctement si la première colonne est plus fine

	// SC3Logger.LOGGER.debug("getTableCellEditorComponent(...)");
	jTextArea.setText(String.valueOf(value));
	// SC3Logger.LOGGER.debug("getTableCellEditorComponent(...) 1: " + jTextArea.getPreferredSize().height + " / "
	// + table.getRowHeight(row));

	// set the JTextArea to the width of the table column
	final Dimension dim = new Dimension(table.getColumnModel().getColumn(column).getWidth(),
		table.getRowHeight(row));
	jTextArea.setSize(dim);
	jTextArea.setPreferredSize(dim);
	jTextArea.setMinimumSize(dim);

	// SC3Logger.LOGGER.debug("getTableCellEditorComponent(...) 2: " + jTextArea.getPreferredSize().height + " / "
	// + table.getRowHeight(row) + " - " + dim);
	table.setRowHeight(row, jTextArea.getPreferredSize().height);
	// SC3Logger.LOGGER.debug("getTableCellEditorComponent(...) 3: " + jTextArea.getPreferredSize().height + " / "
	// + table.getRowHeight(row));
	return jTextArea;
    }
}
