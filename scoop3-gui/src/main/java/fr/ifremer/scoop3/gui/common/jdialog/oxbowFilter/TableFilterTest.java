package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.table.filter.TableRowFilterSupport;
import fr.ifremer.scoop3.infra.logger.SC3Logger;

/**
 * SOURCE : https://github.com/eugener/oxbow
 */
public class TableFilterTest implements Runnable {
    public static void main(final String[] args) {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	SwingUtilities.invokeLater(new TableFilterTest());
    }

    @Override
    public void run() {
	final JFrame f = new JFrame("Swing Table Filter Test");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setPreferredSize(new Dimension(1000, 600));
	final JPanel p = (JPanel) f.getContentPane();
	p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	final JTable table = buildTable();
	p.add(new JScrollPane(table));
	f.pack();
	f.setLocationRelativeTo(null);
	f.setVisible(true);
    }

    private JTable buildTable() {
	@SuppressWarnings("serial")
	final JTable theTable = new JTable() {
	    /*
	     * (non-Javadoc)
	     *
	     * @see javax.swing.JTable#getColumnClass(int)
	     */
	    @Override
	    public Class<?> getColumnClass(final int column) {
		if (column == 0) {
		    return Boolean.class;
		}
		return super.getColumnClass(column);
	    }
	};

	final JTable table = TableRowFilterSupport.forTable(theTable).actions(true).searchable(true)
		.useTableRenderers(true).apply();
	table.setModel(new DefaultTableModel(data, colNames));
	// table.getColumnModel().getColumn(0).setCellRenderer(new TestRenderer());
	return table;
    }

    private static final int ITEM_COUNT = 100;

    protected static Object[] colNames = { "A123", "B123", "C123" };

    protected static Object[][] sample = { { true, 123.2, "ccc333" }, { true, 88888888, null },
	    { false, 12344, "ccc222" }, { false, 67456.34534, "ccc111" }, { false, 78427.33, "ccc444" } };

    protected static Object[][] data = new Object[ITEM_COUNT][sample[0].length];
    static {
	for (int i = 0; i < ITEM_COUNT; i += sample.length) {
	    for (int j = 0; j < sample.length; j += 1) {
		data[i + j] = sample[j];
	    }
	}
    }

    @SuppressWarnings("serial")
    static class TestRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
		final boolean hasFocus, final int row, final int column) {
	    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    setText(getText());
	    return this;
	}
    }
}
