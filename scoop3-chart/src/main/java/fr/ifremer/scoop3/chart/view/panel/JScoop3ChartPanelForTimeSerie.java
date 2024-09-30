package fr.ifremer.scoop3.chart.view.panel;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForTimeserie;
import fr.ifremer.scoop3.infra.properties.FileConfig;

public class JScoop3ChartPanelForTimeSerie extends JScoop3ChartPanelAbstract {

    /**
     *
     */
    private static final long serialVersionUID = 3979947728718995111L;

    /**
     * Height of 1 Graph
     */
    private static int graphHeight;
    /**
     * Width of 1 Graph
     */
    private static int graphWidth;

    static {
	try {
	    graphHeight = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.timeserie-graph-heigth"));
	} catch (final NumberFormatException nfe) {
	    graphHeight = 300;
	}
	try {
	    graphWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.timeserie-graph-width"));
	} catch (final NumberFormatException nfe) {
	    graphWidth = 300;
	}
    }

    /**
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    public JScoop3ChartPanelForTimeSerie(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int firstObservationIndex,
	    final int lastObservationIndex, final int observationNumber, final boolean timeserieDivided) {
	// Abscissa and Ordinate are inverted
	super(ordonneePhysicalVar, abscissaPhysicalVar, graphWidth, graphHeight, firstObservationIndex,
		lastObservationIndex, false, observationNumber, timeserieDivided);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.JScoop3ChartPanelAbstract#initPanel(java.lang.String, java.lang.String,
     * fr.ifremer.scoop3.chart.view.JScoop3ChartScrollPane)
     */
    @Override
    protected void initPanel(final String abscissaPhysicalVar, final String ordonneeLabel,
	    final JScoop3ChartScrollPaneAbstract jScoop3ChartScrollPane) {

	if (isDisplayVariableName()) {
	    add(new JLabel(abscissaPhysicalVar), BorderLayout.SOUTH);
	    add(new JLabel(transformStringToVerticalWithHTML(ordonneeLabel)), BorderLayout.WEST);
	}
	add(jScoop3ChartScrollPane, BorderLayout.CENTER);
    }

    @Override
    protected JScoop3ChartScrollPaneAbstract getJScoop3ChartScrollPane(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided) {
	return new JScoop3ChartScrollPaneForTimeserie(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth, panelHeight,
		firstObservationIndex, lastObservationIndex, observationNumber, timeserieDivided);
    }

    /**
     * @param qcsToUse
     *            the qcsToUse to use
     */
    public void setQCToUse(final String varnameDisplayed, final int[] qcsToUse) {
	((JScoop3ChartScrollPaneForTimeserie) getjScoop3ChartScrollPane()).setQCToUse(varnameDisplayed, qcsToUse);
    }
}
