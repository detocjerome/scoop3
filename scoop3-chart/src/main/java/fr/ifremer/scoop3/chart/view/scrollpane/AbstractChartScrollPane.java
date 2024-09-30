package fr.ifremer.scoop3.chart.view.scrollpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import fr.ifremer.scoop3.chart.view.chart.JScoop3Chart;
import fr.ifremer.scoop3.chart.view.chart.JScoop3ChartCustomAspect;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

/**
 * Panneau d'affichage d'une variable physique
 *
 * @author jdetoc
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractChartScrollPane extends JScrollPane {

    public enum MouseMode {
	OTHER, // Used in Coriolis
	SELECTION, //
	ZOOM, //
	// ZOOM_WITH_PRECISION
    }

    /**
     * Mode 'Supprimer une mesure' activé ou non
     */
    protected static boolean removingMeasure = false;

    /**
     * If not empty, the points displayed are only for those QC
     */
    protected static List<Integer> qcToDisplay = new ArrayList<Integer>();

    /**
     * If not empty, the points excluded are only for those QC
     */
    protected static List<Integer> qcToExclude = new ArrayList<Integer>();

    /**
     * Utilisé par maxAbscissaPhysValBeforeCoefX
     */
    public static double lastMaxAbscissaPhysVal = Double.MAX_VALUE;

    /**
     * Utilisé par minAbscissaPhysValBeforeCoefX
     */
    public static double lastMinAbscissaPhysVal = Double.MIN_VALUE;
    /**
     * Action a effectuer suite a la selection de la souris sur le graphique
     */
    private static MouseMode mouseMode = MouseMode.SELECTION;
    /**
     * Valeur maximale de la mesure physique des abscisses a afficher
     */
    public double maxAbscissaPhysVal = Double.MAX_VALUE;
    /**
     * Sauvegarde de la valeur maxAbscissaPhysVal dans le cas où on affiche COEF_X
     */
    public double maxAbscissaPhysValBeforeCoefX = Double.MAX_VALUE;
    /**
     * Valeur maximale de la mesure physique des ordonnees a afficher
     */
    public double maxOrdinatePhysVal = Double.MAX_VALUE;
    /**
     * Valeur minimale de la mesure physique des abscisses a afficher
     */
    public double minAbscissaPhysVal = Double.MIN_VALUE;
    /**
     * Sauvegarde de la valeur minAbscissaPhysVal dans le cas où on affiche COEF_X
     */
    public double minAbscissaPhysValBeforeCoefX = Double.MIN_VALUE;
    /**
     * Valeur minimale de la mesure physique des ordonnees a afficher
     */
    public double minOrdinatePhysVal = Double.MIN_VALUE;
    /**
     * la valeur maximale réelle du paramètre en ordonnée (utilisé pour le paramètre de référence)
     */
    public double realMaxAbscissaPhysVal = maxAbscissaPhysVal;
    /**
     * la valeur maximale réelle du paramètre en ordonnée (utilisé pour le paramètre de référence)
     */
    public double realMaxOrdinatePhysVal = maxOrdinatePhysVal;
    /**
     * la valeur minimale réelle du paramètre en ordonnée (utilisé pour le paramètre de référence)
     */
    public double realMinAbscissaPhysVal = minAbscissaPhysVal;
    /**
     * la valeur minimale réelle du paramètre en ordonnée (utilisé pour le paramètre de référence)
     */
    public double realMinOrdinatePhysVal = minOrdinatePhysVal;
    /**
     * Rectangle nécessaire pour dessiner les courbes au niveau de zoom 1
     */

    protected transient Rectangle2D dataAreaForZoomLevelOne = null;

    private boolean errorDisplayed = false;

    /**
     * The additional graphs to draw on the graphic
     */
    protected transient List<AdditionalGraph> additionalGraphs;
    /**
     * Pixels a afficher constituant le nuages de points de l'ensembles des mesures bgPoints : Liste de tableaux de
     * coordonnees , chaque tableau represente un point tab[0]=X tab[1]=Y tab[2]=color
     */
    protected transient List<List<int[]>> bgPoints = new ArrayList<List<int[]>>();

    /**
     * Compososant graphique en charge de l'affichage du graphique
     */
    protected JScoop3Chart chart = null;

    /**
     * Niveau selectionne courant
     */
    protected int currentLevel = 0;

    /**
     * index of the current observation
     */
    protected int currentStation = 0;

    /**
     * Pixels à afficher constituant la polyligne du profil courant
     */
    protected transient List<int[]> currentStationsPoints = new ArrayList<int[]>();
    /**
     * Rectangle nécessaire pour dessiner les courbes au niveau de zoom courant
     */
    protected transient Rectangle2D dataAreaForZoomLevelCurrent = null;
    protected double dataAreaHeight;
    // Rectangle englobant la zone d'affichage des donnees
    protected double dataAreaWidth;
    /**
     * index of the first observation
     */
    protected int firstObservationIndex = 0;
    /**
     * En cours de dessin du rectangle de selection
     */
    protected boolean isDrawZoomArea = false;
    /**
     * index of the last observation
     */
    protected int lastObservationIndex = 0;

    /**
     * Valeur maximale du niveau à afficher
     */
    protected float maxViewableLevel = Float.MAX_VALUE;

    /**
     * Valeur minimale du niveau à afficher
     */
    protected float minViewableLevel = Float.MIN_VALUE;

    /**
     * Coin inferieur droit du rectangle des selection
     */
    protected Point selectionAreaBottomRight = null;

    /**
     * Point où la souris a été cliquée lors de la sélection
     */
    protected Point selectionAreaClickedPoint = null;

    /**
     * Coin superieur gauche du rectangle de selection
     */
    protected Point selectionAreaUpperLeft = null;

    protected boolean alert1 = false;
    protected boolean alert2 = false;
    protected boolean alert3 = false;

    /**
     * @return the mouseMode
     */
    public static MouseMode getMouseMode() {
	return mouseMode;
    }

    /**
     * @param mouseMode
     *            the mouseMode to set
     */
    public static void setMouseMode(final MouseMode mouseMode) {
	AbstractChartScrollPane.mouseMode = mouseMode;
    }

    /**
     * Constructeur
     *
     * @param abscissaPhysicalVar
     * @param ordinatePhysicalVar
     * @param panelWidth
     * @param panelHeight
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    protected AbstractChartScrollPane(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided) {

	super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	setBackground(Color.WHITE);

	// resetAvailableDataArea();
	this.firstObservationIndex = firstObservationIndex;
	this.lastObservationIndex = lastObservationIndex;
	this.currentStation = firstObservationIndex;

	SC3Logger.LOGGER.trace("AbstractChartPanel : first - last - current observation : " + firstObservationIndex
		+ " - " + lastObservationIndex + " - " + this.currentStation);

	/* Caracterisation d'affichage du panel */
	this.setPreferredSize(new Dimension(panelWidth, panelHeight));
	// this.setMinimumSize(new Dimension(panelWidth, panelHeight));

	// TODO jdetoc design graphs view
	chart = getJScoop3Chart(this, abscissaPhysicalVar, ordinatePhysicalVar, observationNumber, timeserieDivided);

	/* init the zoomAll DataArea and the currentZoom dataArea */
	resetAvailableDataArea();

    }

    /**
     * Renvoie Le facteur multiplicateur pour convertir une mesure physique en coordonnee X du rep�re
     * "Zone de donn�e totale"
     *
     * @return Le facteur multiplicateur pour convertir une mesure physique en X
     */
    public double computeAbscissaToPixelXFactor() {
	return getDataAreaForZoomLevelOne().getWidth()
		/ (computeMaxViewableAbscissaForColumnHeader() - computeMinViewableAbscissaForColumnHeader());
    }

    /**
     * Calcul du nuage de pixel de l'ensemble des points de mesures
     *
     * @param imageDim
     * @param imagePosition
     */
    public void computeBgPoints(final Dimension2D imageDim, final Point imagePosition) {

	if ((getAbscissaPhysicalVar().getPhysicalValuesByStation() != null)
		&& (getOrdinatePhysicalVar().getPhysicalValuesByStation() != null)) {

	    double minOrdinateToDisplay;
	    double maxOrdinateToDisplay;
	    double minAbscissaToDisplay;
	    double maxAbscissaToDisplay;

	    if (firstObservationIndex == lastObservationIndex) {
		// On va calculer tous les points, même ceux qui ne seront pas sur l'image de fond pour tracer les
		// lignes droites qui sortent ou rentrent dans l'image
		minOrdinateToDisplay = this.minOrdinatePhysVal;
		// TODO why + 1 ?
		maxOrdinateToDisplay = this.maxOrdinatePhysVal + 1;
		minAbscissaToDisplay = this.minAbscissaPhysVal;
		maxAbscissaToDisplay = this.maxAbscissaPhysVal;
	    } else {
		/* Calcul des valeurs minimale et maximale de level a afficher */
		minOrdinateToDisplay = computeMinViewableOrdonnee();
		maxOrdinateToDisplay = computeMaxViewableOrdonnee();
		/*
		 * Calcul des valeurs minimale et maximale de la valeur physique a afficher
		 */
		minAbscissaToDisplay = computeMinViewableAbscissa();
		maxAbscissaToDisplay = computeMaxViewableAbscissa();
	    }

	    /* Calcul du facteur valeur/pixel Y */
	    final double ordonneeToPixelYFactor = computeOrdonneeToPixelYFactor();

	    /* Calcul du facteur valeur/pixel X */
	    final double abscissaToPixelXFactor = computeAbscissaToPixelXFactor();

	    // SC3Logger.LOGGER.trace("computeBgPoints : abscissa min : {} - max : {}", minAbscissaToDisplay,
	    // maxAbscissaToDisplay);
	    // SC3Logger.LOGGER.trace("computeBgPoints : ordinate min : {} - max : {}", minOrdinateToDisplay,
	    // maxOrdinateToDisplay);
	    // SC3Logger.LOGGER.trace("computeBgPoints : pixel factor abscissa : {}", abscissaToPixelXFactor);
	    // SC3Logger.LOGGER.trace("computeBgPoints : pixel factor ordinate : {}", ordonneeToPixelYFactor);
	    // SC3Logger.LOGGER.trace("computeBgPoints : dataTotalArea.width : {}",
	    // this.chart.getDataTotalArea().width);
	    // SC3Logger.LOGGER.trace("computeBgPoints : dataTotalArea.height : {}",
	    // this.chart.getDataTotalArea().height);

	    /* Calcul du tableau de pixel a afficher */
	    int observationIndex;
	    int level;
	    int curX = -1;
	    int curY = -1;
	    int curQCToDisplay = -1;
	    int prevX = -1;
	    int prevY = -1;
	    int prevQC = -1;
	    int prevObsIndex = -1;
	    bgPoints.clear();

	    // SC3Logger.LOGGER.trace("computeBgPoints : abscissaPhysicalVar.getPhysicalValuesByStation().size() : {}",
	    // abscissaPhysicalVar.getPhysicalValuesByStation().size());
	    // SC3Logger.LOGGER.trace("computeBgPoints : abscissaPhysicalVar.getQcValuesByStation().size() : {}",
	    // abscissaPhysicalVar.getQcValuesByStation().size());
	    // SC3Logger.LOGGER.trace("computeBgPoints : ordinatePhysicalVar.getPhysicalValuesByStation().size() : {}",
	    // ordinatePhysicalVar.getPhysicalValuesByStation().size());
	    // SC3Logger.LOGGER.trace("computeBgPoints : ordinatePhysicalVar.getQcValuesByStation().size() : {}",
	    // ordinatePhysicalVar.getQcValuesByStation().size());

	    // For each observation
	    for (observationIndex = firstObservationIndex; observationIndex <= lastObservationIndex; observationIndex++) {
		final List<int[]> points = new ArrayList<int[]>();
		// For each level
		boolean prevAdded = true;
		boolean lastAbscissaDisplayable = true;
		boolean lastOrdinateDisplayable = true;
		for (level = 0; (level < this.getOrdinatePhysicalVar().getPhysicalValuesByStation()
			.get(observationIndex).length)
			&& (level < this.getAbscissaPhysicalVar().getPhysicalValuesByStation()
				.get(observationIndex).length); level++) {

		    final Double abscissaValue = getAbscissaPhysicalValueForStationForLevel(observationIndex, level);
		    final Double ordinateValue = getOrdinatePhysicalVar().getPhysicalValuesByStation()
			    .get(observationIndex)[level];

		    if ((abscissaValue != null) && (ordinateValue != null)) {
			final boolean abscissaDisplayable = (abscissaValue >= minAbscissaToDisplay)
				&& (abscissaValue <= maxAbscissaToDisplay);
			final boolean ordinateDisplayable = (ordinateValue >= minOrdinateToDisplay)
				&& (ordinateValue <= maxOrdinateToDisplay);

			computeAdditionalPoints(imageDim, imagePosition, observationIndex, level,
				abscissaToPixelXFactor, ordonneeToPixelYFactor);

			curX = computeBgPointsCurX(abscissaValue, minAbscissaPhysVal, abscissaToPixelXFactor)
				- imagePosition.x;

			curY = computeBgPointsCurY(imageDim, minOrdinatePhysVal, maxOrdinatePhysVal,
				ordonneeToPixelYFactor, ordinateValue) - imagePosition.y;

			if ((getAbscissaPhysicalVar().getLabel().equals("PSAL")
				&& getOrdinatePhysicalVar().getLabel().equals("TEMP"))
				|| (getAbscissaPhysicalVar().getLabel().equals("PSAL_ADJUSTED")
					&& getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED"))) {
			    curQCToDisplay = computeBgPointsCurQCPsalTemp(getAbscissaPhysicalVar(),
				    getOrdinatePhysicalVar(), observationIndex, level);
			} else {
			    curQCToDisplay = computeBgPointsCurQC(getAbscissaPhysicalVar(), getOrdinatePhysicalVar(),
				    observationIndex, level);
			}

			if (/* (curQCToDisplay != 9) && */ !Double.isInfinite(ordinateValue)
				&& !Double.isInfinite(abscissaValue)) {
			    if (abscissaDisplayable && ordinateDisplayable) {
				if ((curX != prevX) || (curY != prevY)) {
				    if (!prevAdded && (prevObsIndex == observationIndex)
					    && (lastAbscissaDisplayable && lastOrdinateDisplayable)) {
					points.add(new int[] { prevX, prevY, prevQC });
				    }
				    points.add(new int[] { curX, curY, curQCToDisplay });
				} else {
				    // On prend le QC le plus grand
				    if ((curQCToDisplay > prevQC) && !points.isEmpty()) {
					points.remove(points.size() - 1);
					points.add(new int[] { curX, curY, curQCToDisplay });
				    } else {
					// Used when displaying the selected level
					points.add(null);
				    }
				}
				prevAdded = true;
			    } else {
				prevAdded = false;
				points.add(null);
			    }
			    prevX = curX;
			    prevY = curY;
			    prevQC = curQCToDisplay;
			    prevObsIndex = observationIndex;

			    lastAbscissaDisplayable = abscissaDisplayable;
			    lastOrdinateDisplayable = ordinateDisplayable;
			} else {
			    // not displayable
			    if (!prevAdded && (prevObsIndex == observationIndex)) {
				points.add(new int[] { prevX, prevY, prevQC });
				prevAdded = true;
			    }
			    points.add(null);
			}
			// SC3Logger.LOGGER.trace("computeBgPoints : {} - value {} {} - x y qc : {} {} {}", level,
			// abscissaValue, ordinateValue, curX, curY, curQC);
		    }
		}
		// SC3Logger.LOGGER.trace("computeBgPoints : points.size() : {}", points.size());
		bgPoints.add(points);
	    }
	    // SC3Logger.LOGGER.trace("computeBgPoints : bgPoints.size() : {}", bgPoints.size());
	}
    }

    /**
     * Calcul du nuage de pixel des points de mesures de la station courante TODO
     */
    public void computeCurrentStationPoints(final boolean displayErrorMessageDialog) {

	double minAbscissaToDisplay;
	double maxAbscissaToDisplay;
	double minOrdinateToDisplay;
	double maxOrdinateToDisplay;
	// if (JScoop3Chart.IS_SCROLLABLE) {
	// Scrollable => On affiche toute l'etendue
	minAbscissaToDisplay = this.getMinAbscissaPhysVal();
	maxAbscissaToDisplay = this.getMaxAbscissaPhysVal();
	minOrdinateToDisplay = this.getMinOrdinatePhysVal();
	maxOrdinateToDisplay = this.getMaxOrdinatePhysVal();
	// } else {
	/*
	 * Calcul des valeurs minimale et maximale de la valeur physique a afficher
	 */
	// minAbscissaToDisplay = computeMinViewableAbscissa();
	// maxAbscissaToDisplay = computeMaxViewableAbscissa();
	// minOrdinateToDisplay = computeMinViewableOrdonnee();
	// maxOrdinateToDisplay = computeMaxViewableOrdonnee();
	// }

	/* Calcul du facteur valeur/pixel Y */
	final double ordinateYFactor = computeOrdonneeToPixelYFactor();

	/* Calcul du facteur valeur/pixel X */
	final double abscissaXFactor = computeAbscissaToPixelXFactor();

	/* On (re)calcul les points de la station courantes */
	currentStationsPoints.clear();

	try {
	    final int abscissaLength = getAbscissaPhysicalVar().getPhysicalValuesByStation().get(currentStation).length;
	    final int abscissaQCLength = getAbscissaPhysicalVar().getQcValuesByStation().get(currentStation).length;
	    final Dimension2D imageDimension = getImageDimension();
	    final int maxLevel = this.getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation).length;

	    for (int level = 0; level < maxLevel; level++) {
		if ((abscissaLength > level) && (abscissaQCLength > level)) {
		    final Double abscissaValue = getAbscissaPhysicalValueForStationForLevel(currentStation, level);
		    final Double ordinateValue = getOrdinatePhysicalVar().getPhysicalValuesByStation()
			    .get(currentStation)[level];
		    int abscissaQC;
		    if ((abscissaValue != null) && (ordinateValue != null)) {
			if ((getAbscissaPhysicalVar().getLabel().equals("PSAL")
				&& getOrdinatePhysicalVar().getLabel().equals("TEMP"))
				|| (getAbscissaPhysicalVar().getLabel().equals("PSAL_ADJUSTED")
					&& getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED"))) {
			    abscissaQC = computeBgPointsCurQCPsalTemp(getAbscissaPhysicalVar(),
				    getOrdinatePhysicalVar(), currentStation, level);
			} else {
			    abscissaQC = getQCForCurrentStationPoint(currentStation, level);
			}

			if (/* (abscissaQC != 9) && */(abscissaValue >= minAbscissaToDisplay)
				&& (abscissaValue <= maxAbscissaToDisplay) && (ordinateValue >= minOrdinateToDisplay)
				&& (ordinateValue <= maxOrdinateToDisplay)) {
			    final int curX = (int) Math.round((abscissaValue - minAbscissaToDisplay) * abscissaXFactor);
			    final int curY = computeBgPointsCurY(imageDimension, minOrdinateToDisplay,
				    maxOrdinateToDisplay, ordinateYFactor, ordinateValue);

			    currentStationsPoints.add(new int[] { curX, curY, abscissaQC });
			} else {
			    currentStationsPoints.add(null);
			}
		    }
		}
	    }
	} catch (final IndexOutOfBoundsException e) {
	    if (displayErrorMessageDialog && !errorDisplayed) {
		JOptionPane.showMessageDialog(null, Messages.getMessage("gui.waiting-dialog-dataset-loading"),
			"WAITING", JOptionPane.WARNING_MESSAGE);
		errorDisplayed = true;
	    }
	}
    }

    public Rectangle2D computeDataAreaForZoomLevelOne() {
	final int dataAreaMinX = getDataAreaMinX();
	final int dataAreaMaxX = getDataAreaMaxX();

	final int dataAreaMinY = getDataAreaMinY();
	final int dataAreaMaxY = getDataAreaMaxY();

	final int localDataAreaWidth = dataAreaMaxX - dataAreaMinX;
	final int localDataAreaHeight = dataAreaMaxY - dataAreaMinY;

	return new Rectangle2D(0, 0, localDataAreaWidth, localDataAreaHeight);
    }

    /**
     * Renvoie la valeur maximale affichee de la mesure physique
     *
     * @return La mesure physique maximale visible
     */
    public double computeMaxViewableAbscissa() {
	final double abscissaMaxMinusMin = (maxAbscissaPhysVal - minAbscissaPhysVal);

	final double scrollBarValue = (getHorizontalScrollBar().getValue()
		+ getHorizontalScrollBar().getModel().getExtent()) * 1.1d;
	final double scrollBarMaxValue = getHorizontalScrollBar().getMaximum();

	return this.minAbscissaPhysVal + (abscissaMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
    }

    public double computeMaxViewableAbscissaForColumnHeader() {
	return this.minAbscissaPhysVal
		+ (((maxAbscissaPhysVal - minAbscissaPhysVal) / getDataAreaForZoomLevelOne().getWidth())
			* (getDataAreaForZoomLevelCurrent().getMinX() + getDataAreaForZoomLevelCurrent().getWidth()));
    }

    public abstract double computeMaxViewableOrdinateForColumnHeader();

    /**
     * Renvoie la valeur maximale affichee de la mesure physique
     *
     * @return Le niveau maximal visible
     */
    public abstract double computeMaxViewableOrdonnee();

    /**
     * Renvoie la valeur minimale affichee de la mesure physique
     *
     * @return La mesure physique minimale visible
     */
    public double computeMinViewableAbscissa() {
	final double abscissaMaxMinusMin = (maxAbscissaPhysVal - minAbscissaPhysVal);

	final double scrollBarValue = getHorizontalScrollBar().getValue() * 0.9d;
	final double scrollBarMaxValue = getHorizontalScrollBar().getMaximum();

	return Math.floor(this.minAbscissaPhysVal + (abscissaMaxMinusMin * (scrollBarValue / scrollBarMaxValue)));
    }

    public double computeMinViewableAbscissaForColumnHeader() {
	return this.minAbscissaPhysVal
		+ (((maxAbscissaPhysVal - minAbscissaPhysVal) / getDataAreaForZoomLevelOne().getWidth())
			* (getDataAreaForZoomLevelCurrent().getMinX()));
    }

    public abstract double computeMinViewableOrdinateForColumnHeader();

    /**
     * Renvoie la valeur minimale affichee de la mesure physique
     *
     * @return Le niveau minimale visible
     */
    public abstract double computeMinViewableOrdonnee();

    /**
     * Calcule le facteur multiplicateur pour convertir une mesure physique en coordonnee Y du rep�re
     * "Zone de donn�e totale"
     *
     * @return Le facteur multiplicateur pour convertir un niveau en Y
     */
    public double computeOrdonneeToPixelYFactor() {
	return this.getDataAreaForZoomLevelOne().getHeight()
		/ (computeMaxViewableOrdinateForColumnHeader() - computeMinViewableOrdinateForColumnHeader());
    }

    /**
     * @param index
     * @param level
     * @return
     */
    public Double getAbscissaPhysicalValueForStationForLevel(final int index, final int level) {
	return getAbscissaPhysicalVar().getPhysicalValuesByStation().get(index)[level];
    }

    /**
     * @return the abscissaPhysicalVar
     */
    public ChartPhysicalVariable getAbscissaPhysicalVar() {
	return chart.getAbscissaPhysicalVar();
    }

    /**
     * @return the additionalGraphs
     */
    public List<AdditionalGraph> getAdditionalGraphs() {
	return additionalGraphs;
    }

    /**
     * @return the bgPoints
     */
    public List<List<int[]>> getBgPoints() {
	return bgPoints;
    }

    /**
     * @return the currentLevel
     */
    public int getCurrentLevel() {
	return currentLevel;
    }

    /**
     * @return the currentStation
     */
    public int getCurrentStation() {
	return currentStation;
    }

    /**
     * @return the currentStationsPoints
     */
    public List<int[]> getCurrentStationsPoints() {
	return currentStationsPoints;
    }

    /**
     * @return the dataAreaForZoomLevelCurrent
     */
    public Rectangle2D getDataAreaForZoomLevelCurrent() {
	return dataAreaForZoomLevelCurrent;
    }

    /**
     * @return the dataAreaForZoomLevelOne
     */
    public Rectangle2D getDataAreaForZoomLevelOne() {
	return dataAreaForZoomLevelOne;
    }

    /**
     * @return the firstObservationIndex
     */
    public int getFirstObservationIndex() {
	return firstObservationIndex;
    }

    /**
     * The Image Dimension
     */
    public Dimension2D getImageDimension() {
	return new Dimension2D(
		Math.round(((dataAreaWidth * (float) dataAreaForZoomLevelOne.getWidth())
			/ ((float) dataAreaForZoomLevelCurrent.getWidth()))),
		Math.round(((dataAreaHeight * (float) dataAreaForZoomLevelOne.getHeight())
			/ ((float) dataAreaForZoomLevelCurrent.getHeight()))));
    }

    /**
     * The Image position (depends on the ScrollBar)
     *
     * @return
     */
    public Point getImagePosition(final Dimension2D imageDimension) {
	final Point imagePosition = new Point();

	final double x = imageDimension.getWidth()
		* (((double) getHorizontalScrollBar().getValue()) / getHorizontalScrollBar().getMaximum());

	final double y = imageDimension.getHeight()
		* (((double) getVerticalScrollBar().getValue()) / getVerticalScrollBar().getMaximum());

	imagePosition.setLocation(x, y);

	return imagePosition;
    }

    /**
     * @return the lastObservationIndex
     */
    public int getLastObservationIndex() {
	return lastObservationIndex;
    }

    /**
     * @return the maxAbscissaPhysVal
     */
    public double getMaxAbscissaPhysVal() {
	return maxAbscissaPhysVal;
    }

    /**
     * @return the maxOrdinatePhysVal
     */
    public double getMaxOrdinatePhysVal() {
	return maxOrdinatePhysVal;
    }

    /**
     * @return the minAbscissaPhysVal
     */
    public double getMinAbscissaPhysVal() {
	return minAbscissaPhysVal;
    }

    /**
     * @return the minOrdinatePhysVal
     */
    public double getMinOrdinatePhysVal() {
	return minOrdinatePhysVal;
    }

    /**
     * @return the ordinatePhysicalVar
     */
    public ChartPhysicalVariable getOrdinatePhysicalVar() {
	return chart.getOrdinatePhysicalVar();
    }

    /**
     * @param index
     * @return the QC for the current point
     */
    public int getQCForCurrentStation(final int index) {
	return currentStationsPoints.get(index)[2];
    }

    /**
     * @return the realMaxAbscissaPhysVal
     */
    public double getRealMaxAbscissaPhysVal() {
	return realMaxAbscissaPhysVal;
    }

    /**
     * @return the realMinAbscissaPhysVal
     */
    public double getRealMinAbscissaPhysVal() {
	return realMinAbscissaPhysVal;
    }

    /**
     * @return the selectionAreaBottomRight
     */
    public Point getSelectionAreaBottomRight() {
	return selectionAreaBottomRight;
    }

    /**
     * @return the selectionAreaUpperLeft
     */
    public Point getSelectionAreaUpperLeft() {
	return selectionAreaUpperLeft;
    }

    /**
     * @return the isDrawZoomArea
     */
    public boolean isDrawZoomArea() {
	return isDrawZoomArea;
    }

    /**
     * @return TRUE if the Superposed Mode is ON.
     */
    public boolean isSuperposedMode() {
	return firstObservationIndex != lastObservationIndex;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	chart.prepareForDispose();
	prepareForDisposeChartOnly();
    }

    public void prepareForDisposeChartOnly() {
	chart.prepareForDisposeChartOnly();
	chart.removeAll();
	chart = null;
	bgPoints.clear();
	bgPoints = null;
	currentStationsPoints.clear();
	currentStationsPoints = null;
    }

    /**
     * Change de niveau courant
     *
     * @param currentLevel
     */
    public void setCurrentLevel(final int currentLevel) {
	this.currentLevel = currentLevel;
    }

    /*
     *
     */

    /**
     * Change de station courante
     *
     * @param currentStation
     */
    public void setCurrentStation(final int currentStation) {

	this.currentStation = currentStation;

	if (isSuperposedMode()) {
	    computeCurrentStationPoints(true);
	} else {
	    currentStationsPoints.clear();
	    firstObservationIndex = currentStation;
	    lastObservationIndex = currentStation;
	}
    }

    /**
     * @param newDataAreaForZoomLevelOne
     *            the dataAreaForZoomLevelOne to set
     */
    public void setDataAreaForZoomLevelOne(final Rectangle2D newDataAreaForZoomLevelOne) {

	if ((dataAreaForZoomLevelCurrent != null) && (dataAreaForZoomLevelOne != null)) {
	    // Compute ratio to keep the same zoom level

	    final float heightRatio = (float) (((float) newDataAreaForZoomLevelOne.getHeight())
		    / dataAreaForZoomLevelOne.getHeight());
	    final float widthRatio = (float) (((float) newDataAreaForZoomLevelOne.getWidth())
		    / dataAreaForZoomLevelOne.getWidth());

	    final double x = dataAreaForZoomLevelCurrent.getMinX() * widthRatio;
	    final double y = dataAreaForZoomLevelCurrent.getMinY() * heightRatio;

	    final double w = dataAreaForZoomLevelCurrent.getWidth() * widthRatio;
	    final double h = dataAreaForZoomLevelCurrent.getHeight() * heightRatio;

	    dataAreaForZoomLevelCurrent = new Rectangle2D(x, y, w, h);

	    dataAreaWidth = dataAreaWidth * widthRatio;
	    dataAreaHeight = dataAreaHeight * heightRatio;

	    getHorizontalScrollBar().setValue(Math.round(getHorizontalScrollBar().getValue() * widthRatio));
	    getHorizontalScrollBar().getModel()
		    .setExtent(Math.round(getHorizontalScrollBar().getModel().getExtent() * widthRatio));

	    getVerticalScrollBar().setValue(Math.round(getVerticalScrollBar().getValue() * heightRatio));
	    getVerticalScrollBar().getModel()
		    .setExtent(Math.round(getVerticalScrollBar().getModel().getExtent() * heightRatio));

	}

	this.dataAreaForZoomLevelOne = newDataAreaForZoomLevelOne;
    }

    /**
     * Update the DataViewableArea of the JScoop3Chart
     */
    public void updateDataAreaForZoomLevelCurrent() {
	Rectangle2D newDataViewableArea = getDataAreaForZoomLevelCurrent();
	final float newRatioVertical = ((float) getVerticalScrollBar().getValue())
		/ (float) getVerticalScrollBar().getMaximum();
	final float newRatioHorizontal = ((float) getHorizontalScrollBar().getValue())
		/ (float) getHorizontalScrollBar().getMaximum();

	final double newX = getDataAreaForZoomLevelOne().getWidth() * newRatioHorizontal;
	final double newY = getDataAreaForZoomLevelOne().getHeight() * newRatioVertical;

	newDataViewableArea = new Rectangle2D(newX, newY, newDataViewableArea.getWidth(),
		newDataViewableArea.getHeight());

	this.dataAreaForZoomLevelCurrent = newDataViewableArea;
    }

    /**
     * Affiche l'ensemble des mesures sur la partie vible du graphique
     */
    public abstract void zoomAll();

    /**
     * Zoom sur la partie selectionnee du graphique
     *
     * @return
     */
    public abstract boolean zoomOnDisplayArea();

    protected abstract void computeAdditionalPoints(Dimension2D imageDim, Point imagePosition, int observationIndex,
	    int level, double abscissaToPixelXFactor, double ordonneeToPixelYFactor);

    /**
     * @param index
     * @param level
     * @return
     */
    protected abstract int computeBgPointsCurQC(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar, final int index, final int level);

    protected int computeBgPointsCurQCPsalTemp(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar, final int index, final int level) {
	return Math.max(abscissaPhysicalVar.getQcValuesByStation().get(index)[level],
		ordinatePhysicalVar.getQcValuesByStation().get(index)[level]);
    }

    protected int computeBgPointsCurX(final double abscissaValue, final double minAbscissaPhysVal,
	    final double abscissaToPixelXFactor) {
	return (int) Math.round((abscissaValue - minAbscissaPhysVal) * abscissaToPixelXFactor);
    }

    /**
     * @param imageDim
     * @param minOrdinateToDisplay
     * @param ordonneeToPixelYFactor
     * @param ordinateValue
     * @return
     */
    protected abstract int computeBgPointsCurY(final Dimension2D imageDim, final double minOrdinateToDisplay,
	    final double maxOrdinateToDisplay, final double ordonneeToPixelYFactor, final double ordinateValue);

    /**
     * Calcule le point dans le repere "Zone totale de donnees" correspondant a l'origine de la zone visible
     *
     * @return Le point de la zone totale de donnees correspondant a l'origine de la zone visible
     */
    protected Point computeViewPosition() {
	return new Point((int) Math.round(this.getDataAreaForZoomLevelCurrent().getMinX() * computeZoomFactorX()),
		(int) Math.round((this.getDataAreaForZoomLevelCurrent().getMinY() * computeZoomFactorY())));
    }

    /**
     * Calcule le facteur de zoom applique sur l'axe des X
     *
     * @return Le facteur de zoom applique sur l'axe des X
     */
    protected double computeZoomFactorX() {
	return (getDataAreaForZoomLevelOne().getWidth() / getDataAreaForZoomLevelCurrent().getWidth());
    }

    /**
     * Calcule le facteur de zoom applique sur l'axe des Y
     *
     * @return Le facteur de zoom applique sur l'axe des Y
     */
    protected double computeZoomFactorY() {
	return (this.getDataAreaForZoomLevelOne().getHeight() / this.getDataAreaForZoomLevelCurrent().getHeight());
    }

    /**
     * Calcul le niveau correspondant au
     *
     * @param p
     *            Point(x,y) du graphique
     * @param min
     *            true if the level computed is for the Min
     * @return Le Level correspondant au point(x,y) du graphique
     */
    protected abstract double dataAreaPointToLevel(final Point p, boolean min);

    /**
     * Calcul le niveau correspondant au
     *
     * @param p
     *            Point(x,y) du graphique
     * @param min
     *            true if the level computed is for the Min
     * @return Le Level correspondant au point(x,y) du graphique
     */
    protected abstract double dataAreaPointToLevelWithNoRefParam(final Point p, boolean min);

    /**
     * @param p
     *            Point(x,y) du graphique
     * @return La mesure correspondant au point(x,y) du graphique
     */
    protected abstract double dataAreaPointToPhysVal(final Point p, final Dimension2D imageDim);

    /**
     * @param p
     *            Point(x,y) du graphique
     * @return La mesure correspondant au point(x,y) du graphique
     */
    protected abstract double dataAreaPointToPhysValForRef(final Point p, final Dimension2D imageDim);

    /**
     * @return the dataAreaMaxX
     */
    protected int getDataAreaMaxX() {
	return (this.getWidth() > 0 ? this.getWidth() : this.getPreferredSize().width)
		// Scrollbar width
		- 18;
    }

    /**
     * @return the dataAreaMaxY
     */
    protected int getDataAreaMaxY() {
	return (this.getHeight() > 0 ? this.getHeight() : this.getPreferredSize().height)
		// Scrollbar height
		- 18;
    }

    /**
     * @return the dataAreaMinX
     */
    protected int getDataAreaMinX() {
	return JScoop3ChartCustomAspect.graphAreaDataX;
    }

    /**
     * @return the dataAreaMinY
     */
    protected int getDataAreaMinY() {
	return JScoop3ChartCustomAspect.graphAreaDataY;
    }

    /**
     * @param abstractChartScrollPane
     * @return
     */
    protected abstract JScoop3Chart getJScoop3Chart(final AbstractChartScrollPane abstractChartScrollPane,
	    ChartPhysicalVariable abscissaPhysicalVar, ChartPhysicalVariable ordinatePhysicalVar,
	    final int observationNumber, final boolean timeserieDivided);

    /**
     * Get the QC to display
     *
     * @param currentStation
     * @param level
     * @return
     */
    protected abstract int getQCForCurrentStationPoint(int currentStation, int level);

    /**
     * Initialise les rectangles permettant de gérer les zooms* dataAreaForZoomLevelOne dataAreaForZoomLevelCurrent =
     * dataAreaForZoomLevelOne a l'initialisation
     */
    protected void resetAvailableDataArea() {
	dataAreaForZoomLevelOne = computeDataAreaForZoomLevelOne();
	dataAreaWidth = dataAreaForZoomLevelOne.getWidth();
	dataAreaHeight = dataAreaForZoomLevelOne.getHeight();
	dataAreaForZoomLevelCurrent = new Rectangle2D(dataAreaForZoomLevelOne.getMinX(),
		dataAreaForZoomLevelOne.getMinY(), dataAreaWidth, dataAreaHeight);

	// SC3Logger.LOGGER.debug("getHorizontalScrollBar().getMaximum() : " + getHorizontalScrollBar().getMaximum());

	// SC3Logger.LOGGER.debug(getAbscissaPhysicalVar().getLabel() + ": 1- " + getSize());
	setSize((int) dataAreaWidth //
		+ 18 // Scroll size
		+ JScoop3ChartCustomAspect.graphAreaDataX // header
		,
		(int) dataAreaHeight//
			+ 18 // Scroll size
			+ JScoop3ChartCustomAspect.graphAreaDataY // header
	);
	// SC3Logger.LOGGER.debug(getAbscissaPhysicalVar().getLabel() + ": 2- " + getSize());

	// getHorizontalScrollBar().revalidate();
	// getHorizontalScrollBar().validate();
	// getVerticalScrollBar().revalidate();
	// getVerticalScrollBar().validate();
	// SC3Logger.LOGGER.debug("getHorizontalScrollBar().getMaximum() : " + getHorizontalScrollBar().getMaximum());

    }

    public static void setRemoveMeasure(final boolean b) {
	removingMeasure = b;
    }

    public JScoop3Chart getJScoop3Chart() {
	return this.chart;
    }

    public void setLastObservationIndex(final int lastObservationIndex) {
	this.lastObservationIndex = lastObservationIndex;
    }

    public boolean getAlert1() {
	return this.alert1;
    }

    public boolean getAlert2() {
	return this.alert2;
    }

    public boolean getAlert3() {
	return this.alert3;
    }

    public static List<Integer> getQcToDisplay() {
	return qcToDisplay;
    }

    public static List<Integer> getQcToExclude() {
	return qcToExclude;
    }
}
