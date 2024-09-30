package fr.ifremer.scoop3.chart.view.scrollpane;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollBar;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.chart.JScoop3Chart;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

public class JScoop3ChartScrollPaneForTimeserie extends JScoop3ChartScrollPaneAbstract {

    /**
     *
     */
    private static final long serialVersionUID = 9220165952365164610L;
    private int[] qcsToUse = null;
    private String varnameDisplayed = null;

    /**
     * @param abscissaPhysicalVar
     * @param ordonneePhysicalVar
     * @param panelWidth
     * @param panelHeight
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    public JScoop3ChartScrollPaneForTimeserie(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex, final int observationNumber,
	    final boolean timeserieDivided) {
	super(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth, panelHeight, firstObservationIndex,
		lastObservationIndex, observationNumber, timeserieDivided);
    }

    @Override
    public double computeMaxViewableOrdinateForColumnHeader() {
	return minOrdinatePhysVal
		+ (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			* (getDataAreaForZoomLevelOne().getHeight() - getDataAreaForZoomLevelCurrent().getMinY()));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#computeMaxViewableOrdonnee()
     */
    @Override
    public double computeMaxViewableOrdonnee() {
	final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	final double scrollBarValue = getVerticalScrollBar().getValue() * 0.9d;
	final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	return maxOrdinatePhysVal - (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
    }

    @Override
    public double computeMinViewableOrdinateForColumnHeader() {
	return minOrdinatePhysVal + (((maxOrdinatePhysVal - minOrdinatePhysVal)
		/ getDataAreaForZoomLevelOne().getHeight())
		* (getDataAreaForZoomLevelOne().getHeight()
			- (getDataAreaForZoomLevelCurrent().getMinY() + getDataAreaForZoomLevelCurrent().getHeight())));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#computeMinViewableOrdonnee()
     */
    @Override
    public double computeMinViewableOrdonnee() {
	final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	final double scrollBarValue = (getVerticalScrollBar().getValue()
		+ getVerticalScrollBar().getModel().getExtent()) * 1.1d;
	final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	return maxOrdinatePhysVal - (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getPhysicalValueForLevel(int)
     */
    @Override
    public double getPhysicalValueForLevel(final int level) {
	return getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation)[level];
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#getQCForCurrentStation(int)
     */
    @Override
    public int getQCForCurrentStation(final int index) {
	if ((qcsToUse != null) && (qcsToUse.length > index)) {
	    return qcsToUse[index];
	}
	return super.getQCForCurrentStation(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getReferenceScrollBar()
     */
    @Override
    public JScrollBar getReferenceScrollBar() {
	return getHorizontalScrollBar();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getVariableNameToUpdate()
     */
    @Override
    public String getVariableNameToUpdate() {
	if (varnameDisplayed != null) {
	    return varnameDisplayed;
	}
	return getOrdinatePhysicalVar().getLabel();
    }

    @Override
    public String getSecondVariableNameToUpdate() {
	return getAbscissaPhysicalVar().getLabel();
    }

    /**
     * @param qcsToUse
     *            the qcsToUse to set
     */
    public void setQCToUse(final String varnameDisplayed, final int[] qcsToUse) {
	this.varnameDisplayed = varnameDisplayed;
	this.qcsToUse = qcsToUse;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetPhysvalMax()
     */
    @Override
    public double updateQCsGetPhysvalMax() {
	return dataAreaPointToPhysVal(selectionAreaUpperLeft, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetPhysvalMin()
     */
    @Override
    public double updateQCsGetPhysvalMin() {
	return dataAreaPointToPhysVal(selectionAreaBottomRight, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetQCsList(boolean)
     */
    @Override
    public List<int[]> updateQCsGetQCsList(final boolean currentStationOnly, final String superposedModeEnum) {

	List<int[]> toReturn;
	if (qcsToUse != null) {
	    if (currentStationOnly) {
		toReturn = new ArrayList<int[]>();
		toReturn.add(qcsToUse);
	    } else {
		// FIXME - use qcsToUse
		toReturn = getQCsList();
	    }
	} else {
	    if (currentStationOnly) {
		toReturn = new ArrayList<int[]>();
		toReturn.add(getQCsList().get(currentStation));
	    } else {
		toReturn = getQCsList();
	    }
	}

	return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetRefvalMax()
     */
    @Override
    public double updateQCsGetRefvalMax() {
	return dataAreaPointToPhysValForRef(selectionAreaUpperLeft, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetRefvalMin()
     */
    @Override
    public double updateQCsGetRefvalMin() {
	return dataAreaPointToPhysValForRef(selectionAreaBottomRight, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetValuesList()
     */
    @Override
    public List<Double[]> updateQCsGetValuesList(final boolean currentStationOnly, final String superposedModeEnum) {
	List<Double[]> toReturn;
	if (currentStationOnly) {
	    toReturn = new ArrayList<Double[]>();
	    toReturn.add(getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation));
	} else {
	    toReturn = getOrdinatePhysicalVar().getPhysicalValuesByStation();
	}
	return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#computeBgPointsCurQC(fr.ifremer.scoop3.chart.
     * model.ChartPhysicalVariable, fr.ifremer.scoop3.chart.model.ChartPhysicalVariable, int, int)
     */
    @Override
    protected int computeBgPointsCurQC(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar, final int index, final int level) {
	int curQC = ordinatePhysicalVar.getQcValuesByStation().get(index)[level];

	// TODO a mieux coder jerome curQC != 9 pour ne pas afficher les points inexistant du parametre mais ajouter
	// pour avoir des série homogène
	if (/* (curQC != 9) && */ (qcsToUse != null) && (qcsToUse.length > level)) {
	    curQC = qcsToUse[level];
	}
	return curQC;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#computeBgPointsCurY(java.awt.Dimension,
     * double, double, float)
     */
    @Override
    protected int computeBgPointsCurY(final Dimension2D imageDim, final double minOrdinateToDisplay,
	    final double maxOrdinateToDisplay, final double ordonneeToPixelYFactor, final double ordinateValue) {
	int curY = (int) Math.round((ordinateValue - minOrdinateToDisplay) * ordonneeToPixelYFactor);
	curY = (int) Math.round(imageDim.getHeight()) - curY;
	return curY;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#computeNewRectangleToDisplay(java.util
     * .Map, java.lang.String)
     */
    @Override
    protected Rectangle2D computeNewRectangleToDisplay(final Map<String, double[]> minMaxForVariables,
	    final String variableName) {
	double newX = getDataAreaForZoomLevelCurrent().getMinX();
	double newY = getDataAreaForZoomLevelCurrent().getMinY();

	double newWidth = getDataAreaForZoomLevelCurrent().getWidth();
	double newHeigth = getDataAreaForZoomLevelCurrent().getHeight();

	double newMinFloat = minMaxForVariables.get(variableName)[0];
	double newMaxFloat = minMaxForVariables.get(variableName)[1];

	if (variableName.equals(getAbscissaPhysicalVar().getLabel())) {
	    if (newMinFloat < minAbscissaPhysVal) {
		if (allowsUpdateMinAndMaxValuesDisplayed) {
		    setLastAdditionalPointsImagePosition(null);
		    setLastImageDim(null);
		    minAbscissaPhysVal = newMinFloat;
		} else {
		    newMinFloat = minAbscissaPhysVal;
		}
	    }
	    if (newMaxFloat > maxAbscissaPhysVal) {
		if (allowsUpdateMinAndMaxValuesDisplayed) {
		    setLastAdditionalPointsImagePosition(null);
		    setLastImageDim(null);
		    maxAbscissaPhysVal = newMaxFloat;
		} else {
		    newMaxFloat = maxAbscissaPhysVal;
		}
	    }

	    final double newXPurcent = (newMinFloat - minAbscissaPhysVal) / (maxAbscissaPhysVal - minAbscissaPhysVal);
	    final float newXFloat = (float) (getDataAreaForZoomLevelOne().getMinX()
		    + (getDataAreaForZoomLevelOne().getWidth() * newXPurcent));
	    newX = newXFloat;

	    final double newWidthPurcent = (newMaxFloat - newMinFloat) / (maxAbscissaPhysVal - minAbscissaPhysVal);
	    final float newWidthFloat = (float) (getDataAreaForZoomLevelOne().getMinX()
		    + (getDataAreaForZoomLevelOne().getWidth() * newWidthPurcent));
	    newWidth = newWidthFloat;
	} else if (variableName.equals(getOrdinatePhysicalVar().getLabel())) {
	    if (newMinFloat < minOrdinatePhysVal) {
		if (allowsUpdateMinAndMaxValuesDisplayed) {
		    setLastAdditionalPointsImagePosition(null);
		    setLastImageDim(null);
		    minOrdinatePhysVal = newMinFloat;
		} else {
		    newMinFloat = minOrdinatePhysVal;
		}
	    }
	    if (newMaxFloat > maxOrdinatePhysVal) {
		if (allowsUpdateMinAndMaxValuesDisplayed) {
		    setLastAdditionalPointsImagePosition(null);
		    setLastImageDim(null);
		    maxOrdinatePhysVal = newMaxFloat;
		} else {
		    newMaxFloat = maxOrdinatePhysVal;
		}
	    }

	    newY = zoomForVariablesNewY(newMinFloat, newMaxFloat);

	    final double newHeigthPurcent = (newMaxFloat - newMinFloat) / (maxOrdinatePhysVal - minOrdinatePhysVal);
	    final float newHeigthFloat = (float) (getDataAreaForZoomLevelOne().getMinY()
		    + (getDataAreaForZoomLevelOne().getHeight() * newHeigthPurcent));
	    newHeigth = newHeigthFloat;
	} else {
	    return null;
	}
	try {
	    return new Rectangle2D(newX, newY, newWidth, newHeigth);
	} catch (final Exception e) {
	    return null;
	}
    }

    @Override
    protected double dataAreaPointToLevel(final Point p, final boolean min) {
	final double computed = (p.x / computeAbscissaToPixelXFactor());
	double abscissaPhysValue = Math.max(minAbscissaPhysVal, this.minAbscissaPhysVal + computed);
	abscissaPhysValue = Math.min(abscissaPhysValue, this.maxAbscissaPhysVal);

	int level = 0;
	for (final double value : getAbscissaPhysicalVar().getPhysicalValuesByStation().get(currentStation)) {
	    if (value > abscissaPhysValue) {
		if (min) {
		    return level;
		} else {
		    return (double) level - 1;
		}
	    }
	    level++;
	}
	return level;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToLevelWithNoRefParam(java.awt.Point
     * , boolean)
     */
    @Override
    protected double dataAreaPointToLevelWithNoRefParam(final Point p, final boolean min) {
	final double abscissaParamMinVal = updateQCsGetRefvalMin();
	final double abscissaParamMaxVal = updateQCsGetRefvalMax();

	final double ordinateParamMinVal = updateQCsGetPhysvalMin();
	final double ordinateParamMaxVal = updateQCsGetPhysvalMax();

	final int maxLevel = Math.max(getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation).length,
		getAbscissaPhysicalVar().getPhysicalValuesByStation().get(currentStation).length);

	int levelForMax = 0;
	for (int currentLevel = 0; currentLevel < maxLevel; currentLevel++) {
	    final double abscissaValue = getAbscissaPhysicalVar().getPhysicalValuesByStation()
		    .get(currentStation)[currentLevel];
	    final double ordinateValue = getOrdinatePhysicalVar().getPhysicalValuesByStation()
		    .get(currentStation)[currentLevel];

	    final boolean abscissaInRange = (abscissaValue >= abscissaParamMinVal)
		    && (abscissaValue <= abscissaParamMaxVal);
	    final boolean ordinateInRange = (ordinateValue >= ordinateParamMinVal)
		    && (ordinateValue <= ordinateParamMaxVal);

	    if (abscissaInRange && ordinateInRange) {
		if (min) {
		    // Return the first good level
		    return currentLevel;
		} else {
		    // Memorize the last good level
		    levelForMax = currentLevel;
		}
	    }
	}

	return levelForMax;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToPhysVal(java.awt.Point,
     * java.awt.Dimension)
     */
    @Override
    protected double dataAreaPointToPhysVal(final Point p, final Dimension2D imageDim) {
	final double ordonneeToPixelYFactor = computeOrdonneeToPixelYFactor();
	final double height = imageDim.getHeight() - p.y;
	final double value = height / ordonneeToPixelYFactor;
	return minOrdinatePhysVal + value;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToPhysValForRef(java.awt.Point,
     * java.awt.Dimension)
     */
    @Override
    protected double dataAreaPointToPhysValForRef(final Point p, final Dimension2D imageDim) {
	return minAbscissaPhysVal + (p.x / computeAbscissaToPixelXFactor());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#getJScoop3Chart(fr.ifremer.scoop3.chart.view.
     * scrollpane.AbstractChartScrollPane)
     */
    @Override
    protected JScoop3Chart getJScoop3Chart(final AbstractChartScrollPane jScoop3ScrollPane,
	    final ChartPhysicalVariable abscissaPhysicalVar, final ChartPhysicalVariable ordinatePhysicalVariable,
	    final int observationNumber, final boolean timeserieDivided) {
	return new JScoop3Chart(jScoop3ScrollPane, false, abscissaPhysicalVar, ordinatePhysicalVariable/* , false */,
		observationNumber, timeserieDivided);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#getQCForCurrentStationPoint(int, int)
     */
    @Override
    protected int getQCForCurrentStationPoint(final int currentStation, final int level) {
	if (qcsToUse != null) {
	    return qcsToUse[level];
	}
	return getOrdinatePhysicalVar().getQcValuesByStation().get(currentStation)[level];
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getQCsList()
     */
    @Override
    protected List<int[]> getQCsList() {
	final List<int[]> toReturn = new ArrayList<>(getOrdinatePhysicalVar().getQcValuesByStation());
	if (qcsToUse != null) {
	    toReturn.set(currentStation, qcsToUse);
	}
	return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getReferencePhysicalVar()
     */
    @Override
    protected ChartPhysicalVariable getReferencePhysicalVar() {
	return getAbscissaPhysicalVar();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#zoomForVariablesNewY(float, float)
     */
    @Override
    protected double zoomForVariablesNewY(final double newMinFloat, final double newMaxFloat) {
	final double newYPurcent = (maxOrdinatePhysVal - newMaxFloat) / (maxOrdinatePhysVal - minOrdinatePhysVal);
	return (float) (getDataAreaForZoomLevelOne().getMinY()
		+ (getDataAreaForZoomLevelOne().getHeight() * newYPurcent));
    }
}
