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

public class JScoop3ChartScrollPaneForProfile extends JScoop3ChartScrollPaneAbstract {

    /**
     *
     */
    private static final long serialVersionUID = -5542201102307184122L;

    /**
     * @param ordonneePhysicalVar
     * @param panelWidth
     * @param panelHeight
     * @param firstObservationIndex
     * @param lastObservationIndex
     */
    public JScoop3ChartScrollPaneForProfile(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex) {
	super(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth, panelHeight, firstObservationIndex,
		lastObservationIndex, -1, false);
    }

    @Override
    public double computeMaxViewableOrdinateForColumnHeader() {
	return minOrdinatePhysVal
		+ (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			* (getDataAreaForZoomLevelCurrent().getMinY() + getDataAreaForZoomLevelCurrent().getHeight()));
    }

    /**
     * Renvoie la valeur maximale affichee de la mesure physique
     *
     * @return Le niveau maximal visible
     */
    @Override
    public double computeMaxViewableOrdonnee() {
	final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	final double scrollBarValue = (getVerticalScrollBar().getValue()
		+ getVerticalScrollBar().getModel().getExtent()) * 1.1d;
	final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	return this.minOrdinatePhysVal + (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
    }

    @Override
    public double computeMinViewableOrdinateForColumnHeader() {
	return minOrdinatePhysVal
		+ (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			* (getDataAreaForZoomLevelCurrent().getMinY()));
    }

    /**
     * Renvoie la valeur minimale affichee de la mesure physique
     *
     * @return Le niveau minimale visible
     */
    @Override
    public double computeMinViewableOrdonnee() {
	final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	final double scrollBarValue = getVerticalScrollBar().getValue() * 0.9d;
	final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	return minOrdinatePhysVal + (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getPhysicalValueForLevel(int)
     */
    @Override
    public double getPhysicalValueForLevel(final int level) {
	return getAbscissaPhysicalVar().getPhysicalValuesByStation().get(currentStation)[level];
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getReferenceScrollBar()
     */
    @Override
    public JScrollBar getReferenceScrollBar() {
	return getVerticalScrollBar();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getVariableNameToUpdate()
     */
    @Override
    public String getVariableNameToUpdate() {
	String toReturn = getAbscissaPhysicalVar().getLabel();
	if (toReturn.equals("measure_number")) {
	    toReturn = getOrdinatePhysicalVar().getLabel();
	}
	return toReturn;
    }

    @Override
    public String getSecondVariableNameToUpdate() {
	return getOrdinatePhysicalVar().getLabel();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetPhysvalMax()
     */
    @Override
    public double updateQCsGetPhysvalMax() {
	return dataAreaPointToPhysVal(selectionAreaBottomRight, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetPhysvalMin()
     */
    @Override
    public double updateQCsGetPhysvalMin() {
	return dataAreaPointToPhysVal(selectionAreaUpperLeft, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetQCsList(boolean)
     */
    @Override
    public List<int[]> updateQCsGetQCsList(final boolean currentStationOnly, final String superposedModeEnum) {
	List<int[]> toReturn;
	if (currentStationOnly) {
	    toReturn = new ArrayList<int[]>();
	    toReturn.add(getQCsList().get(currentStation));
	} else {
	    if (superposedModeEnum.equals("ALL_OBSERVATIONS_FROM_DATASET")) {
		toReturn = getQCsListADataset();
	    } else {
		toReturn = getQCsListAPlateform(currentStation);
	    }
	}
	return toReturn;
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
	    toReturn.add(getAbscissaPhysicalVar().getPhysicalValuesByStation().get(currentStation));
	} else {
	    if (superposedModeEnum.equals("ALL_OBSERVATIONS_FROM_DATASET")) {
		toReturn = getAbscissaPhysicalVar().getPhysicalValuesADataset();
	    } else {
		toReturn = getAbscissaPhysicalVar().getPhysicalValuesAPlatform(currentStation);
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
	return dataAreaPointToPhysValForRef(selectionAreaBottomRight, getImageDimension());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#updateQCsGetRefvalMin()
     */
    @Override
    public double updateQCsGetRefvalMin() {
	return dataAreaPointToPhysValForRef(selectionAreaUpperLeft, getImageDimension());
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
	return abscissaPhysicalVar.getQcValuesByStation().get(index)[level];
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
	return (int) Math.round((ordinateValue - minOrdinateToDisplay) * ordonneeToPixelYFactor);
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
	    // Gestion des graphiques PSAL/TEMP qui zoomaient par défaut
	    if ((newMinFloat > minOrdinatePhysVal) && (!variableName.equals("PRES") && !variableName.equals("DEPH"))
		    && (getDataAreaForZoomLevelOne() == getDataAreaForZoomLevelCurrent())) {
		newMinFloat = minOrdinatePhysVal;
	    }
	    if ((newMaxFloat < maxOrdinatePhysVal) && (!variableName.equals("PRES") && !variableName.equals("DEPH"))
		    && (getDataAreaForZoomLevelOne() == getDataAreaForZoomLevelCurrent())) {
		newMaxFloat = maxOrdinatePhysVal;
	    }

	    newY = zoomForVariablesNewY(newMinFloat, newMaxFloat);

	    final double newHeigthPurcent = (newMaxFloat - newMinFloat) / (maxOrdinatePhysVal - minOrdinatePhysVal);
	    final float newHeigthFloat = (float) (getDataAreaForZoomLevelOne().getMinY()
		    + (getDataAreaForZoomLevelOne().getHeight() * newHeigthPurcent));
	    newHeigth = newHeigthFloat;
	}

	// ugly hack for spinner prev / next profile
	if (newHeigth <= 0) {
	    newHeigth = getDataAreaForZoomLevelCurrent().getHeight();
	}
	if (newWidth <= 0) {
	    newWidth = getDataAreaForZoomLevelCurrent().getWidth();
	}

	return new Rectangle2D(newX, newY, newWidth, newHeigth);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToLevel(java.awt.Point,
     * boolean)
     */
    @Override
    protected double dataAreaPointToLevel(final Point p, final boolean min) {
	final double computed = (p.y / computeOrdonneeToPixelYFactor());
	double ordinatePhysValue = Math.max(minOrdinatePhysVal, this.minOrdinatePhysVal + computed);
	ordinatePhysValue = Math.min(ordinatePhysValue, this.maxOrdinatePhysVal);

	boolean inverted = false;
	while (getOrdinatePhysicalVar().getPhysicalValuesByStation().size() <= currentStation) {
	    try {
		Thread.sleep(1);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
	    }
	}
	if (getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation).length > 2) {
	    final double firstValue = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation)[0];
	    double otherValue = firstValue;
	    for (int index = 1; (index < getOrdinatePhysicalVar().getPhysicalValuesByStation()
		    .get(currentStation).length) && (otherValue == firstValue); index++) {
		otherValue = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation)[index];
	    }
	    if (otherValue < firstValue) {
		inverted = true;
	    }
	}

	int level = 0;
	int bestLevelForMax = -1;
	if (inverted) {
	    for (level = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation).length
		    - 1; level >= 0; level--) {
		final double value = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation)[level];
		if (value > ordinatePhysValue) {
		    if (min) {
			return level;
		    } else {
			return (double) level + 1;
		    }
		}
	    }
	} else {
	    for (final double value : getOrdinatePhysicalVar().getPhysicalValuesByStation().get(currentStation)) {
		if (value > ordinatePhysValue) {
		    if (min) {
			return level;
		    } else {
			if (bestLevelForMax == -1) {
			    bestLevelForMax = level;
			}
			// return level - 1;
		    }
		} else if (bestLevelForMax != -1) {
		    bestLevelForMax = -1;
		}
		level++;
	    }
	    if (bestLevelForMax != -1) {
		return (double) bestLevelForMax - 1;
	    }
	    if (!min) {
		return (double) level - 1;
	    }
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
	final double abscissaParamMinVal = updateQCsGetPhysvalMin();
	final double abscissaParamMaxVal = updateQCsGetPhysvalMax();

	final double ordinateParamMinVal = updateQCsGetRefvalMin();
	final double ordinateParamMaxVal = updateQCsGetRefvalMax();

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
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToPhysVal(java.awt.Point)
     */
    @Override
    protected double dataAreaPointToPhysVal(final Point p, final Dimension2D imageDim) {
	return minAbscissaPhysVal + (p.x / computeAbscissaToPixelXFactor());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#dataAreaPointToPhysValForRef(java.awt.Point,
     * java.awt.Dimension)
     */
    @Override
    protected double dataAreaPointToPhysValForRef(final Point p, final Dimension2D imageDim) {
	return minOrdinatePhysVal + (p.y / computeOrdonneeToPixelYFactor());
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
	    final ChartPhysicalVariable abscissaPhysicalVar, final ChartPhysicalVariable ordinatePhysicalVar,
	    final int observationNumber, final boolean timeserieDivided) {
	return new JScoop3Chart(jScoop3ScrollPane, true, abscissaPhysicalVar, ordinatePhysicalVar/* , false */, -1,
		false);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane#getQCForCurrentStationPoint(int, int)
     */
    @Override
    protected int getQCForCurrentStationPoint(final int currentStation, final int level) {
	return getAbscissaPhysicalVar().getQcValuesByStation().get(currentStation)[level];
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getQCsList()
     */
    @Override
    protected List<int[]> getQCsList() {
	return getAbscissaPhysicalVar().getQcValuesByStation();
    }

    protected List<int[]> getQCsListAPlateform(final int currentStation) {
	return getAbscissaPhysicalVar().getQcValuesAPlatform(currentStation);
    }

    protected List<int[]> getQCsListADataset() {
	return getAbscissaPhysicalVar().getQcValuesADataset();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#getReferencePhysicalVar()
     */
    @Override
    protected ChartPhysicalVariable getReferencePhysicalVar() {
	return getOrdinatePhysicalVar();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract#zoomForVariablesNewY(float, float)
     */
    @Override
    protected double zoomForVariablesNewY(final double newMinFloat, final double newMaxFloat) {
	final double newYPurcent = (newMinFloat - minOrdinatePhysVal) / (maxOrdinatePhysVal - minOrdinatePhysVal);
	return (float) (getDataAreaForZoomLevelOne().getMinY()
		+ (getDataAreaForZoomLevelOne().getHeight() * newYPurcent));
    }
}
