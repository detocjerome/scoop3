package fr.ifremer.scoop3.gui.jfreeChart;

import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.editor.ChartEditor;

public class MyChartPanel extends ChartPanel {

    private static final long serialVersionUID = 6538823174186232645L;
    private transient ChartEditor editor;
    JFreeChart chart;

    public MyChartPanel(final JFreeChart chart, final ChartEditor editor) {
	super(chart);
	this.chart = chart;
	this.editor = editor;
    }

    public MyChartPanel(final JFreeChart chart, final ChartEditor editor, final boolean properties, final boolean save,
	    final boolean print, final boolean zoom, final boolean tooltips) {
	super(chart, properties, save, print, zoom, tooltips);
	this.chart = chart;
	this.editor = editor;
    }

    @Override
    public void doEditChartProperties() {
	final int result = JOptionPane.showConfirmDialog(this, editor,
		localizationResources.getString("Chart_Properties"), JOptionPane.OK_CANCEL_OPTION,
		JOptionPane.PLAIN_MESSAGE);
	if (result == JOptionPane.OK_OPTION) {
	    this.setChart(this.chart);
	    this.editor.updateChart(this.chart);
	    this.validate();
	    this.repaint();
	}
    }

}
