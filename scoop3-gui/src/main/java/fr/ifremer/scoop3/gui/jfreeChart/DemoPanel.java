package fr.ifremer.scoop3.gui.jfreeChart;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;

public class DemoPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 3931944515480845513L;
    List charts;

    /**
     * Creates a new demo panel with the specified layout manager.
     *
     * @param layout
     *            the layout manager.
     */
    public DemoPanel(final LayoutManager layout) {
	super(layout);
	this.charts = new java.util.ArrayList();
    }

    /**
     * Records a chart as belonging to this panel. It will subsequently be returned by the getCharts() method.
     *
     * @param chart
     *            the chart.
     */
    public void addChart(final JFreeChart chart) {
	this.charts.add(chart);
    }

    /**
     * Returns an array containing the charts within this panel.
     *
     * @return The charts.
     */
    public JFreeChart[] getCharts() {
	final int chartCount = this.charts.size();
	final JFreeChart[] charts = new JFreeChart[chartCount];
	for (int i = 0; i < chartCount; i++) {
	    charts[i] = (JFreeChart) this.charts.get(i);
	}
	return charts;
    }

}
