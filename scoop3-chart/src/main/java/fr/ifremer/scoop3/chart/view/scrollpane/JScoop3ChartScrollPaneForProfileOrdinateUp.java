package fr.ifremer.scoop3.chart.view.scrollpane;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import javafx.geometry.Dimension2D;

public class JScoop3ChartScrollPaneForProfileOrdinateUp extends JScoop3ChartScrollPaneForProfile {

    /**
     *
     */
    private static final long serialVersionUID = -6761814039004268402L;

    public JScoop3ChartScrollPaneForProfileOrdinateUp(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordonneePhysicalVar, final int panelWidth, final int panelHeight,
	    final int firstObservationIndex, final int lastObservationIndex) {
	super(abscissaPhysicalVar, ordonneePhysicalVar, panelWidth, panelHeight, firstObservationIndex,
		lastObservationIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfile#computeBgPointsCurY(java.awt.Dimension,
     * double, double, float)
     */
    @Override
    protected int computeBgPointsCurY(final Dimension2D imageDim, final double minOrdinateToDisplay,
	    final double maxOrdinateToDisplay, final double ordonneeToPixelYFactor, final double ordinateValue) {
	return (int) Math.round((maxOrdinateToDisplay - ordinateValue) * ordonneeToPixelYFactor);
    }

    @Override
    public double computeMaxViewableOrdinateForColumnHeader() {
	double maxViewableOrdonnee = minOrdinatePhysVal
		+ (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			* (getDataAreaForZoomLevelCurrent().getMinY() + getDataAreaForZoomLevelCurrent().getHeight()));
	if (getOrdinatePhysicalVar().getLabel().equals("TEMP")
		|| getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")) {
	    maxViewableOrdonnee = minOrdinatePhysVal
		    + (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			    * (getDataAreaForZoomLevelOne().getHeight() - getDataAreaForZoomLevelCurrent().getMinY()));
	}
	return maxViewableOrdonnee;
    }

    /**
     * Renvoie la valeur maximale affichee de la mesure physique
     *
     * @return Le niveau maximal visible
     */
    @Override
    public double computeMaxViewableOrdonnee() {
	if (!getOrdinatePhysicalVar().getLabel().equals("TEMP")
		&& !getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")) {
	    final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	    final double scrollBarValue = (getVerticalScrollBar().getValue()
		    + getVerticalScrollBar().getModel().getExtent()) * 1.1d;
	    final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	    return this.minOrdinatePhysVal + (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
	} else {
	    final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	    final double scrollBarValue = getVerticalScrollBar().getValue() * 0.9d;
	    final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	    return maxOrdinatePhysVal - (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
	}
    }

    @Override
    public double computeMinViewableOrdinateForColumnHeader() {
	double minViewableOrdonnee = minOrdinatePhysVal
		+ (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			* (getDataAreaForZoomLevelCurrent().getMinY()));
	if (getOrdinatePhysicalVar().getLabel().equals("TEMP")
		|| getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")) {
	    minViewableOrdonnee = minOrdinatePhysVal
		    + (((maxOrdinatePhysVal - minOrdinatePhysVal) / getDataAreaForZoomLevelOne().getHeight())
			    * (getDataAreaForZoomLevelOne().getHeight() - (getDataAreaForZoomLevelCurrent().getMinY()
				    + getDataAreaForZoomLevelCurrent().getHeight())));
	}
	return minViewableOrdonnee;
    }

    /**
     * Renvoie la valeur minimale affichee de la mesure physique
     *
     * @return Le niveau minimale visible
     */
    @Override
    public double computeMinViewableOrdonnee() {
	if (!getOrdinatePhysicalVar().getLabel().equals("TEMP")
		&& !getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")) {
	    final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	    final double scrollBarValue = getVerticalScrollBar().getValue() * 0.9d;
	    final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	    return minOrdinatePhysVal + (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
	} else {
	    final double ordinateMaxMinusMin = (maxOrdinatePhysVal - minOrdinatePhysVal);

	    final double scrollBarValue = (getVerticalScrollBar().getValue()
		    + getVerticalScrollBar().getModel().getExtent()) * 1.1d;
	    final double scrollBarMaxValue = getVerticalScrollBar().getMaximum();

	    return maxOrdinatePhysVal - (ordinateMaxMinusMin * (scrollBarValue / scrollBarMaxValue));
	}
    }

}
