package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import fr.ifremer.scoop3.infra.logger.SC3Logger;

public class JTextAreaTableCellRenderer implements TableCellRenderer {
    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object,
     * boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	    final boolean hasFocus, final int row, final int column) {

	final JTextArea jtext = new JTextArea();
	jtext.setText(String.valueOf(value));
	jtext.setWrapStyleWord(true);
	jtext.setLineWrap(true);
	if (isSelected) {
	    jtext.setBackground((Color) UIManager.get("Table.selectionBackground"));
	}

	// Set the Row height as small as possible to compute the higher cell
	if (column == 0) {
	    table.setRowHeight(row, 1);
	}

	// set the JTextArea to the width of the table column
	try {
	    jtext.setSize(table.getColumnModel().getColumn(column).getWidth(), jtext.getPreferredSize().height);
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error("Erreur lors du changement de taille de la cellule : " + e);
	}
	final int height = jtext.getPreferredSize().height;

	// Check if the row has to be higher
	if (table.getRowHeight(row) < height) {
	    table.setRowHeight(row, height);
	}

	// table.setRowHeight(row, 50);

	return jtext;
    }
}
