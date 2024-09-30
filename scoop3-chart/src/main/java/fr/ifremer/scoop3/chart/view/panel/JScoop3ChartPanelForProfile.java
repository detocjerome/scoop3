package fr.ifremer.scoop3.chart.view.panel;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfile;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfileOrdinateUp;
import fr.ifremer.scoop3.infra.properties.FileConfig;

public class JScoop3ChartPanelForProfile extends JScoop3ChartPanelAbstract {

    /**
     *
     */
    private static final long serialVersionUID = -7147241202160363954L;

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
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.profile-graph-heigth"));
	} catch (final NumberFormatException nfe) {
	    graphHeight = 600;
	}
	try {
	    graphWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.profile-graph-width"));
	} catch (final NumberFormatException nfe) {
	    graphWidth = 200;
	}
    }

    /**
     * @param abscissaphysicalVar
     * @param ordonneePhysicalVar
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    public JScoop3ChartPanelForProfile(final ChartPhysicalVariable abscissaphysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int firstObservationIndex,
	    final int lastObservationIndex) {
	super(abscissaphysicalVar, ordonneePhysicalVar, graphWidth, graphHeight, firstObservationIndex,
		lastObservationIndex, true, -1, false);
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
	    add(new JLabel(abscissaPhysicalVar), BorderLayout.NORTH);
	    add(new JLabel(transformStringToVerticalWithHTML(ordonneeLabel)), BorderLayout.WEST);
	}
	add(jScoop3ChartScrollPane, BorderLayout.CENTER);
    }

    @Override
    protected JScoop3ChartScrollPaneAbstract getJScoop3ChartScrollPane(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided) {
	// special graph for PSAL/TEMP (need to inverse y-axis)
	if ((abscissaPhysicalVar.getLabel().equals("PSAL") && ordonneePhysicalVar.getLabel().equals("TEMP"))
		|| (abscissaPhysicalVar.getLabel().equals("PSAL_ADJUSTED")
			&& ordonneePhysicalVar.getLabel().equals("TEMP_ADJUSTED"))) {
	    return new JScoop3ChartScrollPaneForProfileOrdinateUp(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth,
		    panelHeight, firstObservationIndex, lastObservationIndex);
	} else {
	    return new JScoop3ChartScrollPaneForProfile(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth,
		    panelHeight, firstObservationIndex, lastObservationIndex);
	}
    }
}
