package fr.ifremer.scoop3.chart.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;

public abstract class JScoop3ChartPanelAbstract extends JPanel {

    private static boolean displayVariableName = true;

    /**
     *
     */
    private static final long serialVersionUID = -415671939755210433L;

    /**
     * Reference on the JScoop3ChartScrollPane
     */
    private JScoop3ChartScrollPaneAbstract jScoop3ChartScrollPane;

    /**
     * @param displayVariableName
     *            the displayVariableName to set
     */
    public static void setDisplayVariableName(final boolean displayVariableName) {
	JScoop3ChartPanelAbstract.displayVariableName = displayVariableName;
    }

    /**
     *
     * @param strToTransform
     * @return
     */
    public static String transformStringToVerticalWithHTML(final String strToTransform) {
	String ans = "<html>";
	final String br = "<br />";
	final String[] lettersArr = strToTransform.split("");
	for (final String letter : lettersArr) {
	    ans += letter + br;
	}
	ans += "</html>";
	return ans;
    }

    /**
     * @return the displayVariableName
     */
    protected static boolean isDisplayVariableName() {
	return displayVariableName;
    }

    /**
     * @param panelWidth
     * @param panelHeight
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    protected JScoop3ChartPanelAbstract(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final boolean isProfile,
	    final int observationNumber, final boolean timeserieDivided) {
	jScoop3ChartScrollPane = getJScoop3ChartScrollPane(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth,
		panelHeight, firstObservationIndex, lastObservationIndex, observationNumber, timeserieDivided);

	setBackground(Color.WHITE);

	/*
	 * TODO remove 20150305 setLayout(new GridBagLayout());
	 */
	setBorder(BorderFactory.createLineBorder(Color.BLACK));
	setLayout(new BorderLayout());

	/* Composant gerant le graphique */
	String abscissaLabel = Messages.getMessage(abscissaPhysicalVar.getLabel());
	if (abscissaPhysicalVar.getUnit() != null) {
	    abscissaLabel += " (" + abscissaPhysicalVar.getUnit() + ")";
	}
	String ordonneeLabel = Messages.getMessage(ordonneePhysicalVar.getLabel());
	if (ordonneePhysicalVar.getUnit() != null) {
	    ordonneeLabel += " (" + ordonneePhysicalVar.getUnit() + ")";
	}
	initPanel(abscissaLabel, ordonneeLabel, jScoop3ChartScrollPane);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
     */
    @Override
    public synchronized void addMouseListener(final MouseListener l) {
	jScoop3ChartScrollPane.addMouseListener(l);
    }

    /**
     * @return the jScoop3ChartScrollPane
     */
    public JScoop3ChartScrollPaneAbstract getjScoop3ChartScrollPane() {
	return jScoop3ChartScrollPane;
    }

    /**
     * @return the name of the variable to update
     */
    public String getVariableNameToUpdate() {
	return jScoop3ChartScrollPane.getVariableNameToUpdate();
    }

    /**
     * @return the name of the reference variable
     */
    public String getSecondVariableNameToUpdate() {
	return jScoop3ChartScrollPane.getSecondVariableNameToUpdate();
    }

    /**
     * @return true is the jScoop3ChartScrollPane has an active Selection Box
     */
    public boolean isSelectionBoxActive() {
	return jScoop3ChartScrollPane.isSelectionBoxActive();
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	jScoop3ChartScrollPane.prepareForDispose();
	removeAll();
	jScoop3ChartScrollPane = null;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDisposeChartOnly() {
	jScoop3ChartScrollPane.prepareForDisposeChartOnly();
	removeAll();
	jScoop3ChartScrollPane = null;
    }

    /**
     * QCs have changed for this variable in an other Panel
     *
     * @param qcsChanged
     */
    public void qcsChange(final List<QCValueChange> qcsChanged) {
	jScoop3ChartScrollPane.qcsChange(qcsChanged);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Component#removeMouseListener(java.awt.event.MouseListener)
     */
    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
	jScoop3ChartScrollPane.removeMouseListener(l);
    }

    /**
     * Repaint chart
     */
    public void repaintChart() {
	jScoop3ChartScrollPane.paintChart();
    }

    /**
     * Revert QCs
     *
     * @param qcsChanged
     */
    public void revertQC(final List<QCValueChange> qcsChanged) {
	jScoop3ChartScrollPane.revertQC(qcsChanged);
    }

    /**
     * Add additional series to display.
     *
     * @param additionalGraphs
     */
    public void setAdditionalSeriesToDisplay(final List<AdditionalGraph> additionalGraphs) {
	jScoop3ChartScrollPane.setAdditionalSeriesToDisplay(additionalGraphs);
    }

    public void setCurrentLevel(final int levelIndex) {
	jScoop3ChartScrollPane.setCurrentLevel(levelIndex);
	jScoop3ChartScrollPane.repaint();
    }

    /**
     * Update the QC in the jScoop3ChartScrollPane
     *
     * @param obsId
     *
     * @param currentStationOnly
     *            true means only for Current Station, false means for all Stations
     * @param qcToSet
     *            the QCValues to set
     * @param secondParameterIsRef
     * @return the list of the modified QC
     */
    public List<QCValueChange> updateQCs(final List<String> obsIds, final boolean currentStationOnly, final int qcToSet,
	    final boolean secondParameterIsRef, final List<List<? extends Number>> referenceValues,
	    final String superposedModeEnum, final boolean isBPCVersion) {

	double refValMin = jScoop3ChartScrollPane.updateQCsGetRefvalMin();
	double refValMax = jScoop3ChartScrollPane.updateQCsGetRefvalMax();

	// For Coriolis ... if the Z parameter is inverted ...
	if ((refValMin != -1) && (refValMax != -1) && (refValMin > refValMax)) {
	    final double tmp = refValMax;
	    refValMax = refValMin;
	    refValMin = tmp;
	}
	return jScoop3ChartScrollPane.updateQCs(obsIds, currentStationOnly, qcToSet, refValMin, refValMax,
		jScoop3ChartScrollPane.updateQCsGetPhysvalMin(), jScoop3ChartScrollPane.updateQCsGetPhysvalMax(),
		jScoop3ChartScrollPane.getVariableNameToUpdate(), referenceValues,
		(superposedModeEnum != null ? superposedModeEnum : "CURRENT_OBSERVATION_ONLY"), isBPCVersion);
    }

    /**
     * @param abscissaPhysicalVar
     * @param ordonneePhysicalVar
     * @param panelWidth
     * @param panelHeight
     * @param firstObservationIndex
     * @param lastObservationIndex
     * @return
     */
    protected abstract JScoop3ChartScrollPaneAbstract getJScoop3ChartScrollPane(
	    ChartPhysicalVariable abscissaPhysicalVar, ChartPhysicalVariable ordonneePhysicalVar, int panelWidth,
	    int panelHeight, int firstObservationIndex, int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided);

    /**
     * Init Panel, the order depends on Data type (Profile or TimeSerie)
     *
     * @param abscissaLabel
     * @param ordonneeLabel
     * @param jScoop3ChartScrollPane
     */
    protected abstract void initPanel(final String abscissaLabel, final String ordonneeLabel,
	    final JScoop3ChartScrollPaneAbstract jScoop3ChartScrollPane);
}
