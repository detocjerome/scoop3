/**
 *
 */
package fr.ifremer.scoop3.chart.view.scrollpane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
//import fr.ifremer.scoop3.chart.view.chart.JScoop3Chart;
import fr.ifremer.scoop3.chart.view.chart.JScoop3ChartCustomAspect;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

/**
 * Panneau d'affichage d'une variable physique
 *
 * @author jdetoc
 *
 */
@SuppressWarnings("serial")
public abstract class JScoop3ChartScrollPaneAbstract extends AbstractChartScrollPane
	implements MouseListener, MouseMotionListener, MouseWheelListener {

    /**
     * Coefficient utilisé pour le décalage des profiles
     */
    private static float coefX = 0;
    /**
     * Facteur du coefficient utilisé pour le décalage des profiles
     */
    private static float coefXFactor;
    /**
     *
     */
    private static boolean displayCircle = false;
    /**
     *
     */
    private static boolean displayLine = true;
    /**
     *
     */
    private static boolean displayPoints = true;
    /**
     * Distance de deplacement de la souris a partir de laquelle on redessine le rectangle de zoom
     */
    private static final double DRAG_DISTANCE_TO_REPAINT = 5;

    /**
     * Facteur de zoom maximum autorise (Utile car l'image de fond est consomatrice en memoire)
     */
    private static float zoomFactorMax;
    /**
     * if TRUE the Min (or Max) displayed can be updated by a lowest (or highest) value when updating displayed
     * rectangle
     */
    protected static boolean allowsUpdateMinAndMaxValuesDisplayed = false;
    /**
     * The last Image position to avoid multiple calculation
     */
    private Point lastAdditionalPointsImagePosition = null;
    /**
     * The last Image dimension to avoid multiple calculation
     */
    private transient Dimension2D lastImageDim = null;
    /**
     * Composant souhaitant intercepte les evenements survenant sur le panel
     */
    private final EventListenerList listeners = new EventListenerList();

    /**
     * Mémorisation de la position du scroll horizontal
     */
    private float scrollHorizontal = -1;
    /**
     * Mémorisation de la position du scroll vertical
     */
    private float scrollVertical = -1;

    private final JToolTip mouseOverTooltip = new JToolTip();

    private boolean canResetSelectionBox = false;

    private Double keepBoundsMinY = null;
    private Double keepBoundsMaxY = null;

    private final int observationNumber;
    private final boolean timeserieDivided;

    static {
	try {
	    zoomFactorMax = Integer.parseInt(FileConfig.getScoop3FileConfig().getString("chart.maximum-zoom-factor"));
	} catch (final NumberFormatException nfe) {
	    zoomFactorMax = 100000;
	}
	try {
	    coefXFactor = Float
		    .parseFloat(FileConfig.getScoop3FileConfig().getString("chart.shift-factor").replace(",", "."));
	} catch (final NumberFormatException nfe) {
	    coefXFactor = 0.1f;
	}

    }

    /**
     * @return the cOEF_X
     */
    public static float getCoefX() {
	return coefX;
    }

    /**
     * @return the cOEF_X
     */
    public static float getCoefXWithoutFactor() {
	return coefX / coefXFactor;
    }

    /**
     * @return the displayCircle
     */
    public static boolean isDisplayCircle() {
	return displayCircle;
    }

    /**
     * @return the displayLine
     */
    public static boolean isDisplayLine() {
	return displayLine;
    }

    /**
     * @return the displayPoints
     */
    public static boolean isDisplayPoints(final int qc) {
	if (!qcToDisplay.isEmpty()) {
	    return displayPoints && (qcToDisplay.contains(qc));
	} else if (!qcToExclude.isEmpty()) {
	    return displayPoints && (!qcToExclude.contains(qc));
	} else {
	    return displayPoints;
	}
    }

    /**
     * @param cOEF_X
     *            the cOEF_X to set
     */
    public static void setCoefX(final int cOEF_X) {
	coefX = cOEF_X * coefXFactor;
    }

    /**
     * @param qcToDisplay
     *            the qcToDisplay to set
     */
    public static void setDisplayOnlyQCOnGraph(final List<Integer> qcToDisplay) {
	JScoop3ChartScrollPaneAbstract.qcToDisplay = qcToDisplay;
    }

    /**
     * @param qcToExclude
     *            the qcToDisplay to set
     */
    public static void setExcludeOnlyQCOnGraph(final List<Integer> qcToExclude) {
	JScoop3ChartScrollPaneAbstract.qcToExclude = qcToExclude;
    }

    /**
     * @param displayPoints
     *            the displayPoints to set
     * @param displayCircle
     *            the displayCircle to set
     */
    public static void setDisplayPointsAndCircle(final boolean displayLine, final boolean displayPoints,
	    final boolean displayCircle) {
	JScoop3ChartScrollPaneAbstract.displayLine = displayLine;
	JScoop3ChartScrollPaneAbstract.displayPoints = displayPoints;
	JScoop3ChartScrollPaneAbstract.displayCircle = displayCircle;
    }

    public static void setLastMinAndMaxAbscissaPhysVal(final double currentMinAbscissaPhysVal,
	    final double currentMaxAbscissaPhysVal) {
	lastMinAbscissaPhysVal = currentMinAbscissaPhysVal;
	lastMaxAbscissaPhysVal = currentMaxAbscissaPhysVal;
    }

    /**
     *
     * @param panelWidth
     *            Largeur preferentielle
     * @param panelHeight
     *            Hauteur preferentielle
     */
    protected JScoop3ChartScrollPaneAbstract(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided) {

	super(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth, panelHeight, firstObservationIndex,
		lastObservationIndex, observationNumber, timeserieDivided);

	this.observationNumber = observationNumber;
	this.timeserieDivided = timeserieDivided;

	/* Gestion des evenements */
	addMouseListener(this);
	addMouseMotionListener(this);
	addMouseWheelListener(this);

	// Le graphique sera initialement affiche au niveau de zoom global
	zoomAll();

	// Le graphique est le JComponent scrollable
	setViewportView(chart);
	setColumnHeaderView(chart.getColumnHeader());
	setRowHeaderView(chart.getRowHeader());

	if (lastMinAbscissaPhysVal != Double.MIN_VALUE) {
	    minAbscissaPhysValBeforeCoefX = lastMinAbscissaPhysVal;
	    lastMinAbscissaPhysVal = Double.MIN_VALUE;
	    maxAbscissaPhysValBeforeCoefX = lastMaxAbscissaPhysVal;
	    lastMaxAbscissaPhysVal = Double.MAX_VALUE;
	}
    }

    /**
     * Abonne un composant aux evenements survenant sur le panel
     */
    public void addJScoop3GraphPanelListener(final JScoop3GraphPanelListener listener) {
	listeners.add(JScoop3GraphPanelListener.class, listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.AbstractChartPanel# getAbscissaPhysicalValueForStationForLevel(int, int)
     */
    @Override
    public Double getAbscissaPhysicalValueForStationForLevel(final int index, final int level) {
	Double abscissaValue = super.getAbscissaPhysicalValueForStationForLevel(index, level);
	if (coefX != 0) {
	    final double min = (lastMinAbscissaPhysVal != Double.MIN_VALUE) ? lastMinAbscissaPhysVal
		    : minAbscissaPhysValBeforeCoefX;
	    final double max = (lastMaxAbscissaPhysVal != Double.MAX_VALUE) ? lastMaxAbscissaPhysVal
		    : maxAbscissaPhysValBeforeCoefX;
	    abscissaValue += (index + 1) * coefX * (max - min);
	}
	return abscissaValue;
    }

    public int getLevelMaxFromSelectionArea(final boolean secondParameterIsRef) {
	if (secondParameterIsRef) {
	    return (int) Math.round(dataAreaPointToLevel(selectionAreaBottomRight, false));
	} else {
	    return (int) Math.round(dataAreaPointToLevelWithNoRefParam(selectionAreaBottomRight, false));
	}
    }

    public int getLevelMinFromSelectionArea(final boolean secondParameterIsRef) {
	if (secondParameterIsRef) {
	    return (int) Math.round(dataAreaPointToLevel(selectionAreaUpperLeft, true));
	} else {
	    return (int) Math.round(dataAreaPointToLevelWithNoRefParam(selectionAreaBottomRight, true));
	}
    }

    /**
     * @return the maxAbscissaPhysValBeforeCoefX
     */
    public double getMaxAbscissaPhysValBeforeCoefX() {
	return maxAbscissaPhysValBeforeCoefX;
    }

    /**
     * @return the minAbscissaPhysValBeforeCoefX
     */
    public double getMinAbscissaPhysValBeforeCoefX() {
	return minAbscissaPhysValBeforeCoefX;
    }

    public abstract double getPhysicalValueForLevel(final int level);

    /**
     * @return the ScrollBar for the Reference Parameter
     */
    public abstract JScrollBar getReferenceScrollBar();

    /**
     * @return the name of the variable to update
     */
    public abstract String getVariableNameToUpdate();

    /**
     * @return the name of the reference variable
     */
    public abstract String getSecondVariableNameToUpdate();

    /**
     * @return true is there is an active Selection Box
     */
    public boolean isSelectionBoxActive() {
	return (selectionAreaUpperLeft != null) && (selectionAreaBottomRight != null);
    }

    @Override
    public void mouseClicked(final MouseEvent arg0) {

	if (SwingUtilities.isLeftMouseButton(arg0)) {
	    // double click bouton gauche
	    if ((arg0.getClickCount() == 1) && !removingMeasure) {
		isDrawZoomArea = false;

		/* Replacement dans le reperes des donnees */
		final Point p = arg0.getPoint();
		translatePanelPointInDataTotalArea(p);

		// Recherche de la station courante passant par ce point
		final String newCurrentStation = findSelectedStation(p, false);
		final String[] newCurrentStationPart = newCurrentStation.split(" / ");
		if (Integer.parseInt(newCurrentStationPart[0]) != Integer.MIN_VALUE) {
		    final int[] newCurrentStationTab = new int[2];
		    newCurrentStationTab[0] = Integer.parseInt(newCurrentStationPart[0]);
		    newCurrentStationTab[1] = Integer.parseInt(newCurrentStationPart[1]);
		    fireSelectCurrentStation(newCurrentStationTab);
		}
	    }
	} else if (SwingUtilities.isRightMouseButton(arg0)) {
	    canResetSelectionBox = true;
	}

	if ((mouseOverTooltip.getTipText() != null) && !mouseOverTooltip.getTipText().equals("")
		&& !isSuperposedMode()) {
	    mouseOverTooltip.setVisible(true);
	}
    }

    @Override
    public void mouseDragged(final MouseEvent arg0) {

	final Point p = arg0.getPoint();
	translatePanelPointInDataTotalArea(p);

	if (SwingUtilities.isLeftMouseButton(arg0)) {
	    if (isSelectionBoxActive() && canResetSelectionBox) {
		selectionAreaUpperLeft = null;
		selectionAreaBottomRight = null;
		selectionAreaClickedPoint = new Point(p);
		canResetSelectionBox = false;
		alert1 = true;
	    }

	    if (isDrawZoomArea /* && (selectionAreaClickedPoint.distance(p) > DRAG_DISTANCE_TO_REPAINT) */) {
		// SC3Logger.LOGGER.debug("**** Mouse Dragged ****");
		// SC3Logger.LOGGER.debug(p);

		/* Zoom dans les limite de la zone d'affichage du graphique */
		limitDataTotalAreaPointToVisibleArea(p);

		/* Mise a jour du rectangle de selection */
		updateSelectionAreaCorners(p);

		repaint();
	    }
	}
    }

    @Override
    public void mouseEntered(final MouseEvent arg0) {
	// empty method
    }

    @Override
    public void mouseExited(final MouseEvent arg0) {
	// empty method
    }

    @Override
    public void mouseMoved(final MouseEvent arg0) {
	/* Replacement dans le reperes des donnees */
	final Point p = arg0.getPoint();
	translatePanelPointInDataTotalArea(p);

	// Recherche de la station courante passant par ce point
	final String newCurrentStation = findSelectedStation(p, true);
	final String[] newCurrentStationPart = newCurrentStation.split(" / ");
	if ((Integer.parseInt(newCurrentStationPart[0]) != Integer.MIN_VALUE) && (newCurrentStationPart.length == 4)) {
	    final String infosPoint = "<html>Plateforme : " + newCurrentStationPart[2] + " <br> Station ID : "
		    + newCurrentStationPart[3] + "</html>";
	    mouseOverTooltip.setLocation(p);
	    mouseOverTooltip.setTipText(infosPoint);
	    if (isSuperposedMode()) {
		mouseOverTooltip.setVisible(true);
	    }
	    if (chart.getComponents().length == 0) {
		mouseOverTooltip.setEnabled(true);
		if (!getAbscissaPhysicalVar().isADate()) {
		    mouseOverTooltip.setSize(85, 67);
		} else {
		    mouseOverTooltip.setSize(
			    (int) (mouseOverTooltip.getFontMetrics(mouseOverTooltip.getFont()).stringWidth(infosPoint)
				    * 0.63),
			    35);
		}
		chart.add(mouseOverTooltip);
	    }
	} else {
	    mouseOverTooltip.setVisible(false);
	    mouseOverTooltip.setTipText("");
	}
    }

    /**
     */
    @Override
    public void mousePressed(final MouseEvent arg0) {

	// SC3Logger.LOGGER.debug("**** Mouse Pressed ****");
	final Point p = arg0.getPoint();
	/* Replacement dans le reperes des donnees */
	translatePanelPointInDataTotalArea(p);

	/* Clic gauche : Zoom elastique */
	if (SwingUtilities.isLeftMouseButton(arg0) && isDataTotalAreaPointVisible(p)
	// Do not zoom if SHIFT is in progress
		&& (JScoop3ChartScrollPaneAbstract.getCoefX() == 0)) {

	    /* Init zoom elastique */
	    selectionAreaUpperLeft = new Point(p);
	    selectionAreaBottomRight = new Point(p);
	    selectionAreaClickedPoint = new Point(p);
	    isDrawZoomArea = true;
	    alert1 = false;
	    alert2 = false;
	    alert3 = false;
	}
    }

    @Override
    public void mouseReleased(final MouseEvent arg0) {

	if ((selectionAreaBottomRight != null) && (selectionAreaUpperLeft != null)
		&& (arg0.getButton() == MouseEvent.BUTTON1) && isDrawZoomArea
		&& (selectionAreaBottomRight.distance(selectionAreaUpperLeft) > DRAG_DISTANCE_TO_REPAINT)) {
	    // SC3Logger.LOGGER.debug("**** Mouse Released ****");

	    // translateSelectionAreaInDataArea();

	    if (((getMouseMode() == MouseMode.ZOOM) /* || (getMouseMode() == MouseMode.ZOOM_WITH_PRECISION) */)
		    && (zoomOnDisplayArea())) {
		fireZoomOnDisplayArea(arg0.getSource().getClass().getName());
	    }

	    if (getMouseMode() == MouseMode.OTHER) {
		fireSelectionDoneWithOtherMouseMode();
	    }

	    if ((getMouseMode() == MouseMode.SELECTION) && removingMeasure) {
		fireSelectionDoneWithRemovalMode();
	    }
	}

	// /* RAZ zoom elastique */
	// isDrawZoomArea = false;
	// selectionAreaUpperLeft = null;
	// selectionAreaBottomRight = null;
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {

	/**
	 * SC3Logger.LOGGER.debug("**** Mouse Moved ****");
	 *
	 * int wheelRotation = e.getWheelRotation(); if (wheelRotation < 0) { // Zoom +
	 * getDataViewableArea()NewStartPoint = new Point( Math.min(chart.getDataViewableArea().x + 10,
	 * (int)chart.getDataTotalArea().width/2-20) , Math.min(chart.getDataViewableArea().y + 10,
	 * (int)chart.getDataTotalArea().height/2-20) ); getDataViewableArea()NewEndPoint = new Point(
	 * Math.max(chart.getDataViewableArea().x + chart.getDataViewableArea().width - 10,
	 * (int)chart.getDataTotalArea().width/2+20) , Math.max(chart.getDataViewableArea().y +
	 * chart.getDataViewableArea().height - 10, (int)chart.getDataTotalArea().height/2+20) );
	 *
	 *
	 * } else { // zoom - getDataViewableArea()NewStartPoint = new Point( Math.max(chart.getDataViewableArea().x -
	 * 10, 0) , Math.max(chart.getDataViewableArea().y - 10, 0) ); getDataViewableArea()NewEndPoint = new Point(
	 * Math.min(chart.getDataViewableArea().x + chart.getDataViewableArea().width + 10,
	 * (int)chart.getDataTotalArea().width) , Math.min(chart.getDataViewableArea().x +
	 * chart.getDataViewableArea().height + 10, (int)chart.getDataTotalArea().height) ); } zoomOnDisplayArea();
	 * fireZoomOnDisplayTotalArea();
	 **/
    }

    /**
     * Repaint chart
     */
    public void paintChart() {

	/* Sans les lignes suivantes le scrollpane ne se cadre pas sur la zone zoomée */

	// Dessin de la trame de fond
	chart.computeBgImage(this.getImageDimension(), getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		this.getAdditionalGraphs());
	// Dessin de la station et de l'echantillon courant
	setCurrentStation(currentStation);

	/* Fin Sans les lignes suivantes le scrollpane ne se cadre pas sur la zone zoomée */

	validate();
	repaint();

    }

    /**
     * QCs have changed for this variable in an other Panel
     *
     * @param qcsChanged
     */
    public void qcsChange(final List<QCValueChange> qcsChanged) {
	final List<int[]> qcsList = getQCsList();

	for (final QCValueChange qcValueChange : qcsChanged) {
	    final int newQC = qcValueChange.getNewQC();
	    final int observationIndex = qcValueChange.getObservationIndex();
	    final int observationLevel = qcValueChange.getObservationLevel();

	    if (newQC != QCValueChange.NO_NEW_QC) {
		if ((observationIndex < qcsList.size()) && (observationLevel < qcsList.get(observationIndex).length)) {
		    qcsList.get(observationIndex)[observationLevel] = newQC;
		} else {
		    SC3Logger.LOGGER.warn("Problem in JScoop3ChartScrollPaneAbstract.qcsChange(), observationIndex="
			    + observationIndex + " observationLevel=" + observationLevel);
		}
	    }
	}
	paintChart();
    }

    /**
     * DÃ©sabonne un composant aux evenements survenant sur le panel
     */
    public void removeJScoop3GraphPanelListener(final JScoop3GraphPanelListener listener) {
	listeners.remove(JScoop3GraphPanelListener.class, listener);
    }

    /**
     * Remove the Selection Box if exists
     */
    public boolean removeSelectionBox() {
	/* RAZ zoom elastique */
	isDrawZoomArea = false;
	selectionAreaUpperLeft = null;
	selectionAreaBottomRight = null;
	selectionAreaClickedPoint = null;
	alert2 = true;

	repaint();
	return true;
    }

    /**
     * Revert QCs
     *
     * @param qcsChanged
     */
    public void revertQC(final List<QCValueChange> qcsChanged) {
	final List<int[]> qcsList = getQCsList();

	for (final QCValueChange qcValueChange : qcsChanged) {
	    final int oldQC = qcValueChange.getOldQC();
	    final int observationIndex = qcValueChange.getObservationIndex();
	    final int observationLevel = qcValueChange.getObservationLevel();

	    if (oldQC != QCValueChange.NO_NEW_QC) {
		if ((observationIndex < qcsList.size()) && (observationLevel < qcsList.get(observationIndex).length)) {
		    qcsList.get(observationIndex)[observationLevel] = oldQC;
		} else {
		    SC3Logger.LOGGER.warn("Problem in JScoop3ChartScrollPaneAbstract.revertQC(), observationIndex="
			    + observationIndex + " observationLevel=" + observationLevel);
		}
	    }
	}
	paintChart();
    }

    /**
     * Add additional series to display.
     *
     * @param additionalGraphsAbscissaValues
     * @param additionalGraphsOrdinateValues
     * @param additionalGraphsColors
     */
    public void setAdditionalSeriesToDisplay(final List<AdditionalGraph> additionalGraphs) {
	this.additionalGraphs = additionalGraphs;
	// Used to compute range
	chart.setAdditionalSeriesToDisplay(additionalGraphs);

	lastAdditionalPointsImagePosition = null;
	// no need to compute ordinates to display the climatology
	chart.computeRange(true, this.observationNumber, this.timeserieDivided);

	/*
	 * FAE 26921. 9. Tracer un trait pour symboliser la profondeur 0
	 */
	if (JScoop3ChartCustomAspect.add0DepthAndBathyLines() && getOrdinatePhysicalVar().isReferenceParameter()
		&& !getOrdinatePhysicalVar().isADate()) {
	    if (this.additionalGraphs == null) {
		this.additionalGraphs = new ArrayList<>();
	    }
	    // final float diff = Math.abs((maxAbscissaPhysVal - minAbscissaPhysVal) / 2f);
	    // final float min = minAbscissaPhysVal - ((diff < 1) ? 1f : diff);
	    // final float max = maxAbscissaPhysVal + ((diff < 1) ? 1f : diff);
	    final float min = -10000f;
	    final float max = 10000f;
	    final double minD = min;
	    final double maxD = max;
	    this.additionalGraphs
		    .add(new AdditionalGraph(new Double[] { minD, maxD }, new Double[] { 0.0, 0.0 }, Color.GRAY// .darker()
	    ).setUsedToComputeRange(false));
	} else if (JScoop3ChartCustomAspect.add0DepthAndBathyLines() && getAbscissaPhysicalVar().isReferenceParameter()
		&& !getAbscissaPhysicalVar().isADate()) {
	    if (this.additionalGraphs == null) {
		this.additionalGraphs = new ArrayList<>();
	    }
	    // final float diff = Math.abs((maxOrdinatePhysVal - minOrdinatePhysVal) / 2f);
	    // final float min = minOrdinatePhysVal - ((diff < 1) ? 1f : diff);
	    // final float max = maxOrdinatePhysVal + ((diff < 1) ? 1f : diff);
	    final float min = -10000f;
	    final float max = 10000f;
	    final double minD = min;
	    final double maxD = max;
	    this.additionalGraphs
		    .add(new AdditionalGraph(new Double[] { 0.0, 0.0 }, new Double[] { minD, maxD }, Color.GRAY// .darker()
	    ).setUsedToComputeRange(false));
	}

	// Dessin de la station et de l'echantillon courant
	setCurrentStation(currentStation);

	validate();
	repaint();
    }

    /**
     * Translate un point du repere Panel au repere Zone de donnees totale
     *
     * @param p
     *            Point du repere panel
     */
    public void translatePanelPointInDataTotalArea(final Point p) {

	translatePanelPointInDataViewArea(p);
	translateViewAreaPointInDataTotalArea(p);

    }

    /**
     * Update the QC of the current Station (or all Stations) in the Selection Box
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
	    final double refValMin, final double refValMax, final double physValMin, final double physValMax,
	    final String variableNameToUpdate, final List<List<? extends Number>> referencesValues,
	    final String superposedModeEnum, final boolean isBPCVersion) {

	final List<QCValueChange> qcsChanged = new ArrayList<QCValueChange>();
	final List<int[]> qcsList = updateQCsGetQCsList(currentStationOnly, superposedModeEnum);
	final List<Double[]> valuesList = updateQCsGetValuesList(currentStationOnly, superposedModeEnum);

	List<Double[]> realValuesList = new ArrayList<Double[]>();
	List<int[]> realQcsList = new ArrayList<int[]>();

	// creates a HashMap with Index of Platform and their observations
	final HashMap<Integer, ArrayList<String>> observationsPtfHashMap = new HashMap<Integer, ArrayList<String>>();
	for (int index = 0; index < Dataset.getInstance().getPlatforms().size(); index++) {
	    final ArrayList<String> observationsList = new ArrayList<String>();
	    for (final Observation o : Dataset.getInstance().getPlatforms().get(index).getAllObservations()) {
		observationsList.add(o.getId());
	    }
	    observationsPtfHashMap.put(index, observationsList);
	}

	// if dataset type is timeSeries
	if (getAbscissaPhysicalVar().isADate()) {
	    // if superposed mode
	    if (!currentStationOnly) {
		final String currentPlatform = getOrdinatePhysicalVar().getPlatformsCodes().get(currentStation);
		final ArrayList<Integer> currentObsIds = new ArrayList<Integer>();
		// get the ids of different observations of this platform
		for (int index = 0; index < getOrdinatePhysicalVar().getPlatformsCodes().size(); index++) {
		    if (getOrdinatePhysicalVar().getPlatformsCodes().get(index).equals(currentPlatform)) {
			currentObsIds.add(index);
		    }
		}

		for (int index = currentObsIds.get(0); index <= currentObsIds.get(currentObsIds.size() - 1); index++) {
		    realValuesList.add(valuesList.get(index));
		}

		for (int index = currentObsIds.get(0); index <= currentObsIds.get(currentObsIds.size() - 1); index++) {
		    realQcsList.add(qcsList.get(index));
		}
	    } else {
		realValuesList = valuesList;
		realQcsList = qcsList;
	    }
	} else {
	    realValuesList = valuesList;
	    realQcsList = qcsList;
	}

	final int maxIndex = realValuesList.size();
	for (int index = 0; index < maxIndex; index++) {
	    final int observationIndex = (currentStationOnly) ? currentStation : index;
	    final Double[] values = realValuesList.get(index);
	    final int[] qcs = realQcsList.get(index);
	    // Liste des observations Chaque Observation Liste des valeurs du paramètre de référence
	    List<? extends Number> referenceValues = null;
	    try {
		referenceValues = referencesValues.get(index);
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(Messages.getMessage("coriolis-gui.critical-error-detected.message") + "\n"
			+ MessageFormat.format(Messages.getMessage("coriolis-gui.critical-error-detected.details"),
				// stationID
				obsIds.get(index),
				// platformCode
				getOrdinatePhysicalVar().getPlatformsCodes().get(currentStation)));
		JOptionPane.showMessageDialog(null, Messages.getMessage("coriolis-gui.critical-error-detected.message")
			+ "\n"
			+ MessageFormat.format(Messages.getMessage("coriolis-gui.critical-error-detected.details"),
				// stationID
				obsIds.get(index),
				// platformCode
				getOrdinatePhysicalVar().getPlatformsCodes().get(currentStation)),
			Messages.getMessage("coriolis-gui.critical-error-detected.title"), JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }

	    final int levelMax = Math.min(values.length, referenceValues.size());
	    for (int level = 0; level <= levelMax; level++) {
		if ((values.length > level) && (referenceValues.size() > level)) {
		    final Double value = values[level];
		    final double refValue = referenceValues.get(level).doubleValue();
		    // final float refValue = referenceValues.get(level).floatValue();
		    if (((value != null) && (value >= physValMin) && (value <= physValMax) && (refValue >= refValMin)
			    && (refValue <= refValMax))
			    && ((JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
				    && JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty())
				    || (!JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
					    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(qcs[level]))
				    || (!JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()
					    && !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(qcs[level])))
			    && (qcs[level] != qcToSet) && (!isBPCVersion || (isBPCVersion && (qcs[level] != 9)))) {
			// Backup old values
			final QCValueChange qcChanged = new QCValueChange(observationIndex, level, qcs[level], qcToSet,
				obsIds.get(index), variableNameToUpdate, value,
				(value == Float.POSITIVE_INFINITY) ? "-" : String.valueOf(value),
				String.valueOf(referenceValues.get(level)),
				/* getOrdinatePhysicalVar().getPlatformsCodes().get(currentStation) */getPlatformIndexFromObservation(
					observationsPtfHashMap, obsIds.get(index)));
			qcsChanged.add(qcChanged);
			qcs[level] = qcToSet;
		    }
		}
	    }
	}

	// Dessin de la trame de fond
	chart.computeBgImage(this.getImageDimension(), getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		this.getAdditionalGraphs());
	// Dessin de la station et de l'echantillon courant
	setCurrentStation(currentStation);

	if (!removeSelectionBox()) {
	    repaint();
	}

	return qcsChanged;
    }

    public String getPlatformIndexFromObservation(final HashMap<Integer, ArrayList<String>> observationsPtfHashMap,
	    final String observationId) {
	for (int index = 0; index < observationsPtfHashMap.size(); index++) {
	    if (observationsPtfHashMap.get(index).contains(observationId)) {
		// return getOrdinatePhysicalVar().getPlatformsCodes().get(index);
		return Dataset.getInstance().getPlatforms().get(index).getCode();
	    }
	}
	return null;
    }

    /**
     * @return
     */
    public abstract double updateQCsGetPhysvalMax();

    /**
     * @return
     */
    public abstract double updateQCsGetPhysvalMin();

    /**
     * @param currentStationOnly
     * @return
     */
    public abstract List<int[]> updateQCsGetQCsList(final boolean currentStationOnly, final String superposedModeEnum);

    /**
     * @return
     */
    public abstract double updateQCsGetRefvalMax();

    /**
     * @return
     */
    public abstract double updateQCsGetRefvalMin();

    /**
     * @param currentStationOnly
     * @return
     */
    public abstract List<Double[]> updateQCsGetValuesList(boolean currentStationOnly, final String superposedModeEnum);

    /**
     * Zoom afin d'afficher l'ensemble des mesures
     */
    @Override
    public void zoomAll() {
	resetAvailableDataArea();
	zoomToRectangle(this.getDataAreaForZoomLevelOne());
    }

    public void zoomForVariables(final Map<String, double[]> minMaxForVariables, final boolean zoomOnGraph,
	    final boolean reverseHorizontalScrollBar, final boolean precisionZoomOn/*
										    * , final int observationNumber
										    */) {

	scrollHorizontal = -1;
	scrollVertical = -1;

	for (final String variableName : minMaxForVariables.keySet()) {
	    final Rectangle2D newRect = computeNewRectangleToDisplay(minMaxForVariables, variableName);
	    // SC3Logger.LOGGER.debug("zoomForVariables: " + variableName + ", " + newRect);
	    if (newRect != null) {
		// if (!precisionZoomOn) {
		if (zoomOnGraph) {
		    if (reverseHorizontalScrollBar) {
			slideToRectangle(newRect, reverseHorizontalScrollBar);
		    } else {
			zoomToRectangle(newRect);
		    }
		} else {
		    slideToRectangle(newRect, reverseHorizontalScrollBar);
		}
		// } else {
		// if (!Double.isNaN(newRect.getMinX()) && !Double.isNaN(newRect.getMaxX())
		// && !Double.isNaN(newRect.getMinY()) && !Double.isNaN(newRect.getMaxY())
		// && !Double.isInfinite(newRect.getMinX()) && !Double.isInfinite(newRect.getMaxX())
		// && !Double.isInfinite(newRect.getMinY()) && !Double.isInfinite(newRect.getMaxY())) {
		// zoomWithPrecision(newRect, minMaxForVariables, observationNumber);
		// }
		// }
	    }
	}
    }

    /**
     * Zoom x2 (if possible)
     */
    public void zoomIn() {
	zoomIn(null);
    }

    /**
     * Zoom sur la zone selectionAreaUpperLeft/selectionAreaBottomRight
     *
     * @return Vrai si le zoom demande a pu etre applique
     */
    @Override
    public boolean zoomOnDisplayArea() {

	// verrue
	isDrawZoomArea = false;

	// Zone de donnees sur laquelle on veut zoomer
	// * 1f
	final float dataArea = (float) (getDataAreaForZoomLevelOne().getWidth()
		* getDataAreaForZoomLevelOne().getHeight());
	final float viewableAreaWidth = (float) ((((selectionAreaBottomRight.x - selectionAreaUpperLeft.x) * 1f)
		* getDataAreaForZoomLevelCurrent().getWidth()) / getDataAreaForZoomLevelOne().getWidth());
	final float viewableAreaHeight = (float) ((((selectionAreaBottomRight.y - selectionAreaUpperLeft.y) * 1f)
		* getDataAreaForZoomLevelCurrent().getHeight()) / getDataAreaForZoomLevelOne().getHeight());
	final float zoomFactor = dataArea / (viewableAreaWidth * viewableAreaHeight);

	SC3Logger.LOGGER.debug("zoomFactor : " + zoomFactor + " (max: " + zoomFactorMax + ")");
	// SC3Logger.LOGGER.debug("zoomFactor : " + zoomFactor + " (max: " + ZOOM_FACTOR_MAX + ") ");
	if (zoomFactor <= zoomFactorMax) {
	    final double newX = ((selectionAreaUpperLeft.x * getDataAreaForZoomLevelCurrent().getWidth())
		    / getDataAreaForZoomLevelOne().getWidth());
	    final double newY = ((selectionAreaUpperLeft.y * getDataAreaForZoomLevelCurrent().getHeight())
		    / getDataAreaForZoomLevelOne().getHeight());
	    double newWidth = ((selectionAreaBottomRight.x - selectionAreaUpperLeft.x)
		    * getDataAreaForZoomLevelCurrent().getWidth()) / getDataAreaForZoomLevelOne().getWidth();
	    if (newWidth == 0) {
		newWidth = 1;
	    }
	    double newHeigth = ((selectionAreaBottomRight.y - selectionAreaUpperLeft.y)
		    * getDataAreaForZoomLevelCurrent().getHeight()) / getDataAreaForZoomLevelOne().getHeight();
	    if (newHeigth == 0) {
		newHeigth = 1;
	    }

	    zoomToRectangle(new Rectangle2D(newX, newY, newWidth, newHeigth));

	    // Supprime la sélection courante (FAE 24913)
	    selectionAreaUpperLeft = null;
	    selectionAreaBottomRight = null;
	    alert3 = true;

	    // compute bounds to keep
	    keepBoundsMinY = computeKeepBoundsMinY();
	    keepBoundsMaxY = computeKeepBoundsMaxY();

	    return true;
	} else {
	    // Dessin de la trame de fond
	    repaint();
	    return false;
	}
    }

    /**
     * Zoom * 0.5 (if possible)
     */
    public void zoomOut() {
	// Check if it is possible to Zoom out
	if ((getDataAreaForZoomLevelCurrent().getWidth() < getDataAreaForZoomLevelOne().getWidth())
		|| (getDataAreaForZoomLevelCurrent().getHeight() < getDataAreaForZoomLevelOne().getHeight())) {

	    final double newX = (getDataAreaForZoomLevelOne().getMinX() + getDataAreaForZoomLevelCurrent().getMinX())
		    - (getDataAreaForZoomLevelCurrent().getWidth() / 2);
	    final double newY = (getDataAreaForZoomLevelOne().getMinY() + getDataAreaForZoomLevelCurrent().getMinY())
		    - (getDataAreaForZoomLevelCurrent().getHeight() / 2);

	    double newWidth = getDataAreaForZoomLevelCurrent().getWidth() * 2;
	    if (newWidth > getDataAreaForZoomLevelOne().getWidth()) {
		newWidth = getDataAreaForZoomLevelOne().getWidth();
	    }
	    double newHeigth = getDataAreaForZoomLevelCurrent().getHeight() * 2;
	    if (newHeigth > getDataAreaForZoomLevelOne().getHeight()) {
		newHeigth = getDataAreaForZoomLevelOne().getHeight();
	    }

	    zoomToRectangle(new Rectangle2D(newX, newY, newWidth, newHeigth));
	}
    }

    /**
     * Set Zoom to the given rectangle for the JScoop3ChartPanel (real zoom)
     */
    public void zoomToRectangle(final Rectangle2D zoomRect) {

	updateSelectionBoxLocation(getDataAreaForZoomLevelCurrent(), zoomRect);

	this.dataAreaForZoomLevelCurrent = zoomRect;
	paintChart();

	scrollHorizontal = ((float) (zoomRect.getMinX() - getDataAreaForZoomLevelOne().getMinX()))
		/ (float) getDataAreaForZoomLevelOne().getWidth();
	scrollVertical = ((float) (zoomRect.getMinY() - getDataAreaForZoomLevelOne().getMinY()))
		/ (float) getDataAreaForZoomLevelOne().getHeight();

	updateScrollPosition();
    }

    /**
     * Set Zoom to the given rectangle for the JScoop3ChartPanel (slide mode)
     */
    public void slideToRectangle(final Rectangle2D zoomRect, final boolean reverseHorizontalScrollBar) {

	updateSelectionBoxLocation(getDataAreaForZoomLevelCurrent(), zoomRect);

	this.dataAreaForZoomLevelCurrent = zoomRect;
	paintChart();

	if (reverseHorizontalScrollBar) {
	    scrollHorizontal = (float) (((float) ((getDataAreaForZoomLevelOne().getWidth() - zoomRect.getWidth())
		    - (zoomRect.getMinX() - getDataAreaForZoomLevelOne().getMinX())))
		    / getDataAreaForZoomLevelOne().getWidth());
	} else {
	    scrollHorizontal = ((float) (zoomRect.getMinX() - getDataAreaForZoomLevelOne().getMinX()))
		    / (float) getDataAreaForZoomLevelOne().getWidth();
	}
	scrollVertical = ((float) (zoomRect.getMinY() - getDataAreaForZoomLevelOne().getMinY()))
		/ (float) getDataAreaForZoomLevelOne().getHeight();

	updateScrollPosition();
    }

    /**
     * Set Zoom to the given rectangle for the JScoop3ChartPanel (slide mode)
     */
    // public void zoomWithPrecision(final Rectangle2D zoomRect, final Map<String, double[]> minMaxForVariables,
    // final int observationNumber) {
    //
    // updateSelectionBoxLocation(getDataAreaForZoomLevelCurrent(), zoomRect);
    //
    // this.dataAreaForZoomLevelOne = zoomRect;
    // this.dataAreaForZoomLevelCurrent = zoomRect;
    //
    // Double minOrdinate = null;
    // Double maxOrdinate = null;
    // Double minAbscissa = null;
    // Double maxAbscissa = null;
    //
    // final ChartPhysicalVariable abscissaPhysicalVar = chart.getAbscissaPhysicalVar();
    // final ChartPhysicalVariable ordinatePhysicalVar = chart.getOrdinatePhysicalVar();
    //
    // // for profiles
    // if ((minMaxForVariables.keySet().contains("PRES") || minMaxForVariables.keySet().contains("DEPH"))
    // && !minMaxForVariables.keySet().contains("Time")) {
    // if (minMaxForVariables.get("PRES") != null) {
    // for (final String param : minMaxForVariables.keySet()) {
    // if (param.equals("PRES")) {
    // minOrdinate = minMaxForVariables.get(param)[0];
    // maxOrdinate = minMaxForVariables.get(param)[1];
    // } else {
    // minAbscissa = minMaxForVariables.get(param)[0];
    // maxAbscissa = minMaxForVariables.get(param)[1];
    // }
    // }
    // }
    // if (minMaxForVariables.get("DEPH") != null) {
    // for (final String param : minMaxForVariables.keySet()) {
    // if (param.equals("DEPH")) {
    // minOrdinate = minMaxForVariables.get(param)[0];
    // maxOrdinate = minMaxForVariables.get(param)[1];
    // } else {
    // minAbscissa = minMaxForVariables.get(param)[0];
    // maxAbscissa = minMaxForVariables.get(param)[1];
    // }
    // }
    // }
    // }
    // // for timeseries
    // else if (minMaxForVariables.keySet().contains("Time")) {
    // for (final String param : minMaxForVariables.keySet()) {
    // if (param.equals("Time")) {
    // minAbscissa = minMaxForVariables.get(param)[0];
    // maxAbscissa = minMaxForVariables.get(param)[1];
    // } else {
    // minOrdinate = minMaxForVariables.get(param)[0];
    // maxOrdinate = minMaxForVariables.get(param)[1];
    // }
    // }
    // }
    //
    // // // make a copy of abscissaPhysicalVar within zoom range
    // // final Double[] abscissaPhysicalVarValues = abscissaPhysicalVar.getPhysicalValuesByStation()
    // // .get(observationNumber);
    // // final int[] abscissaPhysicalVarQcValues = abscissaPhysicalVar.getQcValuesByStation().get(observationNumber);
    // //
    // // final ArrayList<Double> abscissaPhysicalVarValuesList = new ArrayList<Double>();
    // // final ArrayList<Integer> abscissaPhysicalVarQcValuesList = new ArrayList<Integer>();
    // // for (int i = 0; i < (abscissaPhysicalVarValues.length - 1); i++) {
    // // if ((abscissaPhysicalVarValues[i] <= maxAbscissa) && (abscissaPhysicalVarValues[i] >= minAbscissa)) {
    // // abscissaPhysicalVarValuesList.add(abscissaPhysicalVarValues[i]);
    // // abscissaPhysicalVarQcValuesList.add(abscissaPhysicalVarQcValues[i]);
    // // }
    // // }
    // //
    // // abscissaPhysicalVar.getPhysicalValuesByStation().set(observationNumber,
    // // abscissaPhysicalVarValuesList.toArray(new Double[abscissaPhysicalVarValuesList.size()]));
    // // abscissaPhysicalVar.getQcValuesByStation().set(observationNumber,
    // // convertIntegers(abscissaPhysicalVarQcValuesList));
    // //
    // // // make a copy of ordinatePhysicalVar within zoom range
    // // final Double[] ordinatePhysicalVarValues = ordinatePhysicalVar.getPhysicalValuesByStation()
    // // .get(observationNumber);
    // // final int[] ordinatePhysicalVarQcValues = ordinatePhysicalVar.getQcValuesByStation().get(observationNumber);
    // //
    // // final ArrayList<Double> ordinatePhysicalVarValuesList = new ArrayList<Double>();
    // // final ArrayList<Integer> ordinatePhysicalVarQcValuesList = new ArrayList<Integer>();
    // // for (int i = 0; i < (ordinatePhysicalVarValues.length - 1); i++) {
    // // if ((ordinatePhysicalVarValues[i] <= maxOrdinate) && (ordinatePhysicalVarValues[i] >= minOrdinate)) {
    // // ordinatePhysicalVarValuesList.add(ordinatePhysicalVarValues[i]);
    // // ordinatePhysicalVarQcValuesList.add(ordinatePhysicalVarQcValues[i]);
    // // }
    // // }
    // //
    // // ordinatePhysicalVar.getPhysicalValuesByStation().set(observationNumber,
    // // ordinatePhysicalVarValuesList.toArray(new Double[ordinatePhysicalVarValuesList.size()]));
    // // ordinatePhysicalVar.getQcValuesByStation().set(observationNumber,
    // // convertIntegers(ordinatePhysicalVarQcValuesList));
    //
    // // make a copy of abscissaPhysicalVar within zoom range
    // final Double[] abscissaPhysicalVarValues = abscissaPhysicalVar.getPhysicalValuesByStation()
    // .get(observationNumber);
    // final int[] abscissaPhysicalVarQcValues = abscissaPhysicalVar.getQcValuesByStation().get(observationNumber);
    // final Double[] ordinatePhysicalVarValues = ordinatePhysicalVar.getPhysicalValuesByStation()
    // .get(observationNumber);
    // final int[] ordinatePhysicalVarQcValues = ordinatePhysicalVar.getQcValuesByStation().get(observationNumber);
    //
    // final ArrayList<Double> abscissaPhysicalVarValuesList = new ArrayList<Double>();
    // final ArrayList<Integer> abscissaPhysicalVarQcValuesList = new ArrayList<Integer>();
    // final ArrayList<Double> ordinatePhysicalVarValuesList = new ArrayList<Double>();
    // final ArrayList<Integer> ordinatePhysicalVarQcValuesList = new ArrayList<Integer>();
    //
    // for (int i = 0; i < (abscissaPhysicalVarValues.length - 1); i++) {
    // if ((abscissaPhysicalVarValues[i] <= maxAbscissa) && (abscissaPhysicalVarValues[i] >= minAbscissa)
    // && (ordinatePhysicalVarValues[i] <= maxOrdinate) && (ordinatePhysicalVarValues[i] >= minOrdinate)) {
    // abscissaPhysicalVarValuesList.add(abscissaPhysicalVarValues[i]);
    // abscissaPhysicalVarQcValuesList.add(abscissaPhysicalVarQcValues[i]);
    // ordinatePhysicalVarValuesList.add(ordinatePhysicalVarValues[i]);
    // ordinatePhysicalVarQcValuesList.add(ordinatePhysicalVarQcValues[i]);
    // }
    // }
    //
    // abscissaPhysicalVar.getPhysicalValuesByStation().set(observationNumber,
    // abscissaPhysicalVarValuesList.toArray(new Double[abscissaPhysicalVarValuesList.size()]));
    // abscissaPhysicalVar.getQcValuesByStation().set(observationNumber,
    // convertIntegers(abscissaPhysicalVarQcValuesList));
    // ordinatePhysicalVar.getPhysicalValuesByStation().set(observationNumber,
    // ordinatePhysicalVarValuesList.toArray(new Double[ordinatePhysicalVarValuesList.size()]));
    // ordinatePhysicalVar.getQcValuesByStation().set(observationNumber,
    // convertIntegers(ordinatePhysicalVarQcValuesList));
    //
    // // timeserie
    // if (minMaxForVariables.keySet().contains("Time")) {
    // chart = new JScoop3Chart(chart.getJScoop3ScrollPane(), false, abscissaPhysicalVar, ordinatePhysicalVar,
    // true);
    // }
    // // profile
    // else {
    // chart = new JScoop3Chart(chart.getJScoop3ScrollPane(), true, abscissaPhysicalVar, ordinatePhysicalVar,
    // true);
    // }
    //
    // paintChart();
    // }

    /**
     *
     * @return Retourne la rectangle visble dans le repere zone totale de donnee
     */
    private Rectangle computeVisibleRectOfDataTotalArea() {
	final Point upperLeft = new Point(0, 0);
	translateViewAreaPointInDataTotalArea(upperLeft);
	final Point bottomRight = new Point(getDataAreaMaxX(), getDataAreaMaxY());
	translateViewAreaPointInDataTotalArea(bottomRight);

	return new Rectangle(upperLeft.x, upperLeft.y, bottomRight.x - upperLeft.x, bottomRight.y - upperLeft.y);
    }

    /**
     * Retourne La station et le niveau correspondant au point
     *
     * @param clickedPoint
     *            Point du repere DataTotalArea
     * @return int[0] : indice de la station, int[1] indice du niveau
     */
    private String findSelectedStation(final Point clickedPoint, final boolean isMouseOver) {

	// final int diffLevelToFind = 50;

	boolean findStation = false;
	int newCurrentStation = -1;
	int newCurrentLevel = -1;
	String newCurrentPlatform = "";
	String newCurrentStationID = "";

	// Point correct ?
	if (isDataTotalAreaPointVisible(clickedPoint)) {

	    final Dimension2D imageDim = getImageDimension();
	    final Point imagePosition = getImagePosition(imageDim);
	    final Point viewPosition = getViewport().getViewPosition();

	    /* Calcul du facteur valeur/pixel Y */
	    final double ordonneeToPixelYFactor = computeOrdonneeToPixelYFactor();

	    /* Calcul du facteur valeur/pixel X */
	    final double abscissaToPixelXFactor = computeAbscissaToPixelXFactor();

	    // final double levelToFind = dataAreaPointToLevel(clickedPoint, true);
	    // final int levelToFindInt = (int) Math.round(levelToFind);

	    double distanceBetweenPointAndClickedPoint;
	    if (!isMouseOver) {
		distanceBetweenPointAndClickedPoint = Double.MAX_VALUE;
	    } else {
		distanceBetweenPointAndClickedPoint = 5.0;
	    }

	    List<Double[]> valuesForStations;
	    if (getAbscissaPhysicalVar().isReferenceParameter()) {
		valuesForStations = getOrdinatePhysicalVar().getPhysicalValuesByStation();
	    } else {
		valuesForStations = getAbscissaPhysicalVar().getPhysicalValuesByStation();
	    }
	    for (int stationIndex = 0; stationIndex < valuesForStations.size(); stationIndex++) {
		if (((firstObservationIndex != lastObservationIndex) && (firstObservationIndex <= stationIndex)
			&& (stationIndex <= lastObservationIndex)) || (stationIndex == currentStation)) {
		    final Double[] values = valuesForStations.get(stationIndex);
		    /*
		     * for (int level = Math.max(levelToFindInt - diffLevelToFind, 0); level <= Math .min(levelToFindInt
		     * + diffLevelToFind, values.length - 1); level++) {
		     */
		    // issue with the graph PSAL/TEMP. Better go through all levels one by one
		    for (int level = 0; level <= (values.length - 1); level++) {

			final Double abcissaValue = getAbscissaPhysicalValueForStationForLevel(stationIndex, level);
			Double ordinateValue = 0.0;
			// check if levels exists for this stationIndex
			if (getOrdinatePhysicalVar().getPhysicalValuesByStation().get(stationIndex).length > level) {

			    ordinateValue = getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(stationIndex)[level];

			    if ((ordinateValue != null) && !Double.isInfinite(ordinateValue) && (abcissaValue != null)
				    && !Double.isInfinite(abcissaValue)) {
				/*
				 * Get the point on the chart
				 */
				int curX = (int) Math
					.round((abcissaValue - minAbscissaPhysVal) * abscissaToPixelXFactor)
					- imagePosition.x;
				int curY = computeBgPointsCurY(imageDim, minOrdinatePhysVal, maxOrdinatePhysVal,
					ordonneeToPixelYFactor, ordinateValue) - imagePosition.y;
				curX += viewPosition.x;
				curY += viewPosition.y;

				final Point currentPoint = new Point(curX, curY);

				final int a = currentPoint.x - clickedPoint.x;
				final int b = currentPoint.y - clickedPoint.y;
				// pythagoras ...
				final double curDistanceBetweenPointAndClickedPoint = Math
					.sqrt((double) (a * a) + (b * b));

				// Check the difference of the distance between "clicked point" and "current point"
				if (curDistanceBetweenPointAndClickedPoint < distanceBetweenPointAndClickedPoint) {
				    distanceBetweenPointAndClickedPoint = curDistanceBetweenPointAndClickedPoint;
				    findStation = true;
				    if (!isMouseOver) {
					newCurrentLevel = level;
					newCurrentStation = stationIndex;
				    } else {
					try {
					    newCurrentPlatform = getOrdinatePhysicalVar().getPlatformsCodes()
						    .get(stationIndex);
					    newCurrentStationID = getOrdinatePhysicalVar().getObervationsList()
						    .get(stationIndex);
					} catch (final IndexOutOfBoundsException e) {
					    SC3Logger.LOGGER.error("The station or platform targeted is not reachable");
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	} else {
	    // SC3Logger.LOGGER.debug("Le point selectionne n'est pas compris dans la zone du graphique");
	}

	if (findStation) {
	    return newCurrentStation + " / " + newCurrentLevel + " / " + newCurrentPlatform + " / "
		    + newCurrentStationID;
	} else {
	    return Integer.MIN_VALUE + " / " + Integer.MIN_VALUE + " / null / null";
	}

    }

    /**
     * Retourne Vrai si le point du repere zone totale de donnees est dans la zone visible des donnees
     *
     * @param p
     *            Point dans le repere Panel
     * @return
     */
    private boolean isDataTotalAreaPointVisible(final Point p) {

	final Rectangle visibleArea = computeVisibleRectOfDataTotalArea();
	return (visibleArea.contains(p));
    }

    /**
     * Les coordonneees p.x et p.y sont positionnees aux limites de la zone de donnees si elles n'en font pas partie
     *
     * @param p
     */
    private void limitDataTotalAreaPointToVisibleArea(final Point p) {

	if (!isDataTotalAreaPointVisible(p)) {
	    final Rectangle dataArea = computeVisibleRectOfDataTotalArea();

	    if (p.x < dataArea.x) {
		p.x = dataArea.x;
	    } else if (p.x > (dataArea.x + dataArea.width)) {
		p.x = dataArea.x + dataArea.width;
	    }

	    if (p.y < dataArea.y) {
		p.y = dataArea.y;
	    } else if (p.y > (dataArea.y + dataArea.height)) {
		p.y = dataArea.y + dataArea.height;
	    }
	}
    }

    /**
     * Translate un point du repere Panel au repere Zone de donnees visible
     *
     * @param p
     *            Point du reopere panel
     */
    private void translatePanelPointInDataViewArea(final Point p) {

	p.x -= getDataAreaMinX();
	p.y -= getDataAreaMinY();
    }

    /**
     * Translate un point du repere zone visible au repere zone totale
     *
     * @param p
     *            Point dont les coordonnees sont dans le repere zone visible
     */
    private void translateViewAreaPointInDataTotalArea(final Point p) {
	final Point viewPosition = getViewport().getViewPosition();
	p.x += viewPosition.x;
	p.y += viewPosition.y;
    }

    /**
     * @param zoomRect
     *
     */
    private void updateScrollPosition() {
	final Runnable updateScrollPosition = () -> {
	    // SC3Logger.LOGGER.debug("updateScrollPosition : " + scrollHorizontal + ", " + scrollVertical);
	    if (scrollHorizontal != -1f) {
		// Compute Scroll position
		scrollHorizontal = scrollHorizontal * getHorizontalScrollBar().getModel().getMaximum();
		getHorizontalScrollBar().setValue((int) Math.floor(scrollHorizontal));
		getHorizontalScrollBar().validate();
		// scrollHorizontal = -1f;
	    }
	    if (scrollVertical != -1f) {
		// Compute Scroll position
		scrollVertical = scrollVertical * getVerticalScrollBar().getModel().getMaximum();
		getVerticalScrollBar().setValue((int) Math.floor(scrollVertical));
		// scrollVertical = -1f;
	    }
	    // SC3Logger.LOGGER.debug("updateScrollPosition : " + scrollHorizontal + ", " + scrollVertical);
	    scrollHorizontal = -1f;
	    scrollVertical = -1f;
	};
	SwingUtilities.invokeLater(updateScrollPosition);
    }

    /**
     * Met a jour les coins du rectangle de selection suite a une modification du rectangle
     *
     * @param p
     *            Coin du rectangle de selection qui a change
     */
    private void updateSelectionAreaCorners(final Point p) {

	/* Mise à jour des coins supérieur gauche et inférieur droit */
	if (p.x > selectionAreaClickedPoint.x) {
	    if (p.y > selectionAreaClickedPoint.y) {
		selectionAreaUpperLeft = selectionAreaClickedPoint;
		selectionAreaBottomRight = p;
	    } else {
		selectionAreaUpperLeft = new Point(selectionAreaClickedPoint.x, p.y);
		selectionAreaBottomRight = new Point(p.x, selectionAreaClickedPoint.y);
	    }
	} else {
	    if (p.y > selectionAreaClickedPoint.y) {
		selectionAreaUpperLeft = new Point(p.x, selectionAreaClickedPoint.y);
		selectionAreaBottomRight = new Point(selectionAreaClickedPoint.x, p.y);
	    } else {
		selectionAreaUpperLeft = p;
		selectionAreaBottomRight = selectionAreaClickedPoint;
	    }
	}

	alert1 = false;
	alert2 = false;
	alert3 = false;
    }

    /**
     * Update the location of the Selection Box (if exists)
     *
     * @param oldDataViewableArea
     * @param newDataViewableArea
     */
    private void updateSelectionBoxLocation(final Rectangle2D oldDataViewableArea,
	    final Rectangle2D newDataViewableArea) {
	if ((selectionAreaUpperLeft != null) && (selectionAreaBottomRight != null) && (newDataViewableArea != null)
		&& (newDataViewableArea.getWidth() != 0) && (newDataViewableArea.getHeight() != 0)) {
	    final double xRatio = (oldDataViewableArea.getWidth()) / (newDataViewableArea.getWidth());
	    final double yRatio = (oldDataViewableArea.getHeight()) / (newDataViewableArea.getHeight());

	    selectionAreaUpperLeft.x *= xRatio;
	    selectionAreaUpperLeft.y *= yRatio;

	    selectionAreaBottomRight.x *= xRatio;
	    selectionAreaBottomRight.y *= yRatio;
	}
    }

    private void zoomIn(final Point newCenterPoint) {
	final float dataArea = (float) (getDataAreaForZoomLevelOne().getWidth()
		* getDataAreaForZoomLevelOne().getHeight());
	final float wishedviewableArea = ((float) (getDataAreaForZoomLevelCurrent().getWidth()
		* getDataAreaForZoomLevelCurrent().getHeight())) / 4;
	final float wishedZoomFactor = dataArea / wishedviewableArea;

	// Check if the wished factor is acceptable
	if (wishedZoomFactor <= zoomFactorMax) {
	    double newX = getDataAreaForZoomLevelCurrent().getMinX()
		    + (getDataAreaForZoomLevelCurrent().getWidth() / 4);
	    double newY = getDataAreaForZoomLevelCurrent().getMinY()
		    + (getDataAreaForZoomLevelCurrent().getHeight() / 4);
	    final double newWidth = getDataAreaForZoomLevelCurrent().getWidth() / 2;
	    final double newHeight = getDataAreaForZoomLevelCurrent().getHeight() / 2;

	    if (newCenterPoint != null) {
		newX = (getDataAreaForZoomLevelCurrent().getMinX()
			+ ((newCenterPoint.x * getDataAreaForZoomLevelCurrent().getWidth())
				/ getDataAreaForZoomLevelOne().getWidth()))
			- (newWidth / 2);
		if (newX < 0) {
		    newX = 0;
		}
		newY = (getDataAreaForZoomLevelCurrent().getMinY()
			+ ((newCenterPoint.y * getDataAreaForZoomLevelCurrent().getHeight())
				/ getDataAreaForZoomLevelOne().getHeight()))
			- (newHeight / 2);
		if (newY < 0) {
		    newY = 0;
		}
	    }
	    zoomToRectangle(new Rectangle2D(newX, newY, newWidth, newHeight));
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#computeAdditionalPoints(java.awt.Dimension,
     * java.awt.Point, int, int, double, double)
     */
    @Override
    protected void computeAdditionalPoints(final Dimension2D imageDim, final Point imagePosition,
	    final int observationIndex, final int level, final double abscissaToPixelXFactor,
	    final double ordonneeToPixelYFactor) {
	if ((additionalGraphs != null) && ((lastAdditionalPointsImagePosition == null)
		|| !(lastAdditionalPointsImagePosition.equals(imagePosition) && lastImageDim.equals(imageDim)))) {

	    lastAdditionalPointsImagePosition = imagePosition;
	    setLastImageDim(imageDim);

	    // int graphId = 0;
	    for (final AdditionalGraph additionalGraph : additionalGraphs) {

		/*
		 * Compute abscissa value (depends on the final image size)
		 */
		int indexGraphMax;
		if (additionalGraph.getAbscissaValues() != null) {
		    indexGraphMax = additionalGraph.getAbscissaValues().length;
		} else {
		    indexGraphMax = additionalGraph.getTimeAbscissaValues().length;
		}
		final int[] additionalGraphAbscissaIntValues = new int[indexGraphMax];

		for (int index = 0; index < indexGraphMax; index++) {
		    if (additionalGraph.getAbscissaValues() != null) {
			additionalGraphAbscissaIntValues[index] = (int) Math
				.round((additionalGraph.getAbscissaValues()[index] - minAbscissaPhysVal)
					* abscissaToPixelXFactor)
				- imagePosition.x;
		    } else {
			additionalGraphAbscissaIntValues[index] = (int) Math
				.round((additionalGraph.getTimeAbscissaValues()[index] - minAbscissaPhysVal)
					* abscissaToPixelXFactor)
				- imagePosition.x;
		    }
		}
		additionalGraph.setAbscissaIntValues(additionalGraphAbscissaIntValues);

		/*
		 * Compute ordinate value (depends on the final image size)
		 */
		indexGraphMax = additionalGraph.getOrdinateValues().length;
		final int[] additionalGraphOrdinateIntValues = new int[indexGraphMax];

		for (int index = 0; index < indexGraphMax; index++) {
		    additionalGraphOrdinateIntValues[index] = computeBgPointsCurY(imageDim, minOrdinatePhysVal,
			    maxOrdinatePhysVal, ordonneeToPixelYFactor, additionalGraph.getOrdinateValues()[index])
			    - imagePosition.y;
		}
		additionalGraph.setOrdinateIntValues(additionalGraphOrdinateIntValues);

		// SC3Logger.LOGGER.debug("computeAdditionalPoints [" + graphId + "] ordinate : "
		// + additionalGraph.getOrdinateValues()[0]);
		// graphId++;
	    }
	}
    }

    /**
     * @param minMaxForVariables
     * @param variableName
     * @return
     */
    protected abstract Rectangle2D computeNewRectangleToDisplay(final Map<String, double[]> minMaxForVariables,
	    final String variableName);

    /**
     * Propage un evenement "selection d'une nouvelle station courante"
     *
     */
    protected void fireSelectCurrentStation(final int[] newCurrentStation) {

	for (final JScoop3GraphPanelListener listener : listeners.getListeners(JScoop3GraphPanelListener.class)) {
	    listener.changeCurrentStation(newCurrentStation);
	}

    }

    protected void fireSelectionDoneWithOtherMouseMode() {
	for (final JScoop3GraphPanelListener listener : listeners.getListeners(JScoop3GraphPanelListener.class)) {
	    listener.selectionDoneWithOtherMouseMode(getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		    selectionAreaUpperLeft, selectionAreaBottomRight);
	}
    }

    protected void fireSelectionDoneWithRemovalMode() {
	for (final JScoop3GraphPanelListener listener : listeners.getListeners(JScoop3GraphPanelListener.class)) {
	    listener.selectionDoneWithRemovalMode(getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		    selectionAreaUpperLeft, selectionAreaBottomRight);
	}
    }

    /**
     * Propage un evenement "zoom global"
     */
    protected void fireZoomAll() {

	for (final JScoop3GraphPanelListener listener : listeners.getListeners(JScoop3GraphPanelListener.class)) {
	    listener.zoomAll();
	}

    }

    /**
     * Propage evenement "zoom elastique"
     */
    protected void fireZoomOnDisplayArea(final String sourceClass) {
	for (final JScoop3GraphPanelListener listener : listeners.getListeners(JScoop3GraphPanelListener.class)) {
	    listener.zoomOnDisplayArea(getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		    selectionAreaUpperLeft, selectionAreaBottomRight, true, sourceClass);
	}
    }

    /**
     * @return the QCs List
     */
    protected abstract List<int[]> getQCsList();

    /**
     *
     * @return the Reference Parameter
     */
    protected abstract ChartPhysicalVariable getReferencePhysicalVar();

    @Override
    protected void paintComponent(final Graphics g) {

	// Dessin de la trame de fond
	chart.computeBgImage(this.getImageDimension(), getDataAreaForZoomLevelOne(), getDataAreaForZoomLevelCurrent(),
		this.getAdditionalGraphs());
	// Dessin de la station et de l'echantillon courant
	setCurrentStation(currentStation);

	super.paintComponent(g);
    }

    /**
     * @param newMinFloat
     * @param newMaxFloat
     * @return
     */
    protected abstract double zoomForVariablesNewY(double newMinFloat, double newMaxFloat);

    public void setMinAbscissaPhysVal(final double minAbscissaPhysVal) {
	this.minAbscissaPhysVal = minAbscissaPhysVal;
    }

    public void setMaxAbscissaPhysVal(final double maxAbscissaPhysVal) {
	this.maxAbscissaPhysVal = maxAbscissaPhysVal;
    }

    public void setMinOrdinatePhysVal(final double minOrdinatePhysVal) {
	this.minOrdinatePhysVal = minOrdinatePhysVal;
    }

    public void setMaxOrdinatePhysVal(final double maxOrdinatePhysVal) {
	this.maxOrdinatePhysVal = maxOrdinatePhysVal;
    }

    public static void allowsUpdateMinAndMaxValuesDisplayed(final boolean allowsUpdateMinAndMaxValuesDisplayed) {
	JScoop3ChartScrollPaneAbstract.allowsUpdateMinAndMaxValuesDisplayed = allowsUpdateMinAndMaxValuesDisplayed;
    }

    /**
     * @param lastAdditionalPointsImagePosition
     *            the lastAdditionalPointsImagePosition to set
     */
    protected void setLastAdditionalPointsImagePosition(final Point lastAdditionalPointsImagePosition) {
	this.lastAdditionalPointsImagePosition = lastAdditionalPointsImagePosition;
    }

    /**
     * @param lastImageDim
     *            the lastImageDim to set
     */
    protected void setLastImageDim(final Dimension2D lastImageDim) {
	this.lastImageDim = lastImageDim;
    }

    public Double getKeepBoundsMinY() {
	return this.keepBoundsMinY;
    }

    public Double getKeepBoundsMaxY() {
	return this.keepBoundsMaxY;
    }

    public Double computeKeepBoundsMinY() {
	return minOrdinatePhysVal
		+ ((this.getDataAreaForZoomLevelCurrent().getMinY() / this.getDataAreaForZoomLevelOne().getHeight())
			* (maxOrdinatePhysVal - minOrdinatePhysVal));
    }

    public Double computeKeepBoundsMaxY() {
	return minOrdinatePhysVal
		+ ((this.getDataAreaForZoomLevelCurrent().getMaxY() / this.getDataAreaForZoomLevelOne().getHeight())
			* (maxOrdinatePhysVal - minOrdinatePhysVal));
    }

    // public static int[] convertIntegers(final List<Integer> integers) {
    // final int[] ret = new int[integers.size()];
    // for (int i = 0; i < ret.length; i++) {
    // ret[i] = integers.get(i).intValue();
    // }
    // return ret;
    // }
}
