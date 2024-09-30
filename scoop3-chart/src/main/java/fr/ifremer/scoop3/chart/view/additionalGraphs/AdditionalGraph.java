package fr.ifremer.scoop3.chart.view.additionalGraphs;

import java.awt.BasicStroke;
import java.awt.Color;

public class AdditionalGraph {

    /**
     * Abscissa values to display (depends on the Zoom)
     */
    private int[] abscissaIntValues = null;
    /**
     * Abscissa values for the AdditionalGraph
     */
    private final Double[] abscissaValues;
    /**
     * Abscissa values for the AdditionalGraph
     */
    private final long[] timeAbscissaValues;
    /**
     * BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    private final BasicStroke basicStroke;
    /**
     * Color to use for the AdditionalGraph
     */
    private final Color color;
    /**
     * TRUE means this additional graph is used to compute max range for graph.
     */
    private boolean isUsedToComputeRange = true;
    /**
     * QCValues used for the AdditionalGraph
     */
    private final int[] listOfQCValues;
    /**
     * Ordinate values to display (depends on the Zoom)
     */
    private int[] ordinateIntValues = null;
    /**
     * Ordinate values for the AdditionalGraph
     */
    private final Double[] ordinateValues;

    /**
     * The BasicStroke is the default one.
     *
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph
     */
    public AdditionalGraph(final Double[] abscissaValues, final Double[] ordinateValues, final Color color) {
	this(abscissaValues, ordinateValues, color, new BasicStroke());
    }

    /**
     * The BasicStroke is the default one.
     *
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph
     */
    public AdditionalGraph(final long[] abscissaValues, final Double[] ordinateValues, final Color color) {
	this(abscissaValues, ordinateValues, color, new BasicStroke());
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph
     * @param basicStroke
     *            BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    public AdditionalGraph(final Double[] abscissaValues, final Double[] ordinateValues, final Color color,
	    final BasicStroke basicStroke) {
	this(abscissaValues, ordinateValues, color, null, basicStroke);
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph
     * @param basicStroke
     *            BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    public AdditionalGraph(final long[] abscissaValues, final Double[] ordinateValues, final Color color,
	    final BasicStroke basicStroke) {
	this(abscissaValues, ordinateValues, color, null, basicStroke);
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param listOfQCValues
     *            QCValues used for the AdditionalGraph
     */
    public AdditionalGraph(final Double[] abscissaValues, final Double[] ordinateValues, final int[] listOfQCValues) {
	this(abscissaValues, ordinateValues, listOfQCValues, new BasicStroke());
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param listOfQCValues
     *            QCValues used for the AdditionalGraph
     * @param basicStroke
     *            BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    public AdditionalGraph(final Double[] abscissaValues, final Double[] ordinateValues, final int[] listOfQCValues,
	    final BasicStroke basicStroke) {
	this(abscissaValues, ordinateValues, null, listOfQCValues, basicStroke);
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph (could be NULL)
     * @param listOfQCValues
     *            QCValues used for the AdditionalGraph (could be NULL)
     * @param basicStroke
     *            BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    private AdditionalGraph(final Double[] abscissaValues, final Double[] ordinateValues, final Color color,
	    final int[] listOfQCValues, final BasicStroke basicStroke) {
	this.abscissaValues = abscissaValues;
	this.timeAbscissaValues = null;
	this.ordinateValues = ordinateValues;
	this.color = color;
	this.listOfQCValues = listOfQCValues;
	this.basicStroke = basicStroke;
    }

    /**
     * @param abscissaValues
     *            Abscissa values for the AdditionalGraph
     * @param ordinateValues
     *            Ordinate values for the AdditionalGraph
     * @param color
     *            Color to use for the AdditionalGraph (could be NULL)
     * @param listOfQCValues
     *            QCValues used for the AdditionalGraph (could be NULL)
     * @param basicStroke
     *            BasicStroke to use for the AdditionalGraph (i.e. dashed line)
     */
    private AdditionalGraph(final long[] abscissaValues, final Double[] ordinateValues, final Color color,
	    final int[] listOfQCValues, final BasicStroke basicStroke) {
	this.abscissaValues = null;
	this.timeAbscissaValues = abscissaValues;
	this.ordinateValues = ordinateValues;
	this.color = color;
	this.listOfQCValues = listOfQCValues;
	this.basicStroke = basicStroke;
    }

    /**
     * @return the abscissaIntValues
     */
    public int[] getAbscissaIntValues() {
	return abscissaIntValues;
    }

    /**
     * @return the abscissaValues
     */
    public Double[] getAbscissaValues() {
	return abscissaValues;
    }

    /**
     * @return the timeAbscissaValues
     */
    public long[] getTimeAbscissaValues() {
	return timeAbscissaValues;
    }

    /**
     * @return the basicStroke
     */
    public BasicStroke getBasicStroke() {
	return basicStroke;
    }

    /**
     * @return the color
     */
    public Color getColor() {
	return color;
    }

    /**
     * @return the ordinateIntValues
     */
    public int[] getOrdinateIntValues() {
	return ordinateIntValues;
    }

    /**
     * @return the ordinateValues
     */
    public Double[] getOrdinateValues() {
	return ordinateValues;
    }

    /**
     * @param index
     * @return the listOfQCValues (if possible). -1 otherwise.
     */
    public int getQCValue(final int index) {
	if ((listOfQCValues != null) && (index < listOfQCValues.length)) {
	    return listOfQCValues[index];
	}
	return Integer.MAX_VALUE;
    }

    /**
     * @return the isUsedToComputeRange
     */
    public boolean isUsedToComputeRange() {
	return isUsedToComputeRange;
    }

    /**
     * @param abscissaIntValues
     *            the abscissaIntValues to set
     */
    public void setAbscissaIntValues(final int[] abscissaIntValues) {
	// SC3Logger.LOGGER.debug("setAbscissaIntValues");
	this.abscissaIntValues = abscissaIntValues;
    }

    /**
     * @param ordinateIntValues
     *            the ordinateIntValues to set
     */
    public void setOrdinateIntValues(final int[] ordinateIntValues) {
	// SC3Logger.LOGGER.debug("setOrdinateIntValues");
	this.ordinateIntValues = ordinateIntValues;
    }

    /**
     * @param isUsedToComputeRange
     *            the isUsedToComputeRange to set
     */
    public AdditionalGraph setUsedToComputeRange(final boolean isUsedToComputeRange) {
	this.isUsedToComputeRange = isUsedToComputeRange;
	return this;
    }

}
