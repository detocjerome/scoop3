package fr.ifremer.scoop3.chart.view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.SimpleDateFormat;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.tools.Conversions;

public class JScoop3ChartCustomAspect {

    /** SerialVersionUID **/

    private static final long serialVersionUID = 3406672077722950469L;

    public static long getSerialVersionUID() {
	return serialVersionUID;
    }

    /** SerialVersionUID **/

    /** RADIUS ET DIAMETER **/

    /**
     * Diameter of the displayed points
     */
    protected static int diameter;

    /**
     * Radius of a displayed point
     */
    protected static int radius;

    /** RADIUS ET DIAMETER **/

    /** TYPE DE LIGNE EN FONCTION DES COURBES **/

    /**
     * Type de ligne pour la courbe courante
     */
    protected static final BasicStroke CURRENT_CURB_STROKE = new BasicStroke(2.0f);

    /**
     * Type de ligne pour les autres courbes
     */
    protected static final BasicStroke OTHER_CURB_STROKE = new BasicStroke(2.0f);

    /** TYPE DE LIGNE EN FONCTION DES COURBES **/

    /** QUADRILLAGE **/

    /**
     * Stroke of the quadrillage
     */
    protected static final BasicStroke QUADRILLAGE_STROKE_DASHED = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
	    BasicStroke.JOIN_MITER, 10.0f, new float[] { 1, 4 }, 0.0f);

    /**
     * To enable/disable easily the "QUADRILLAGE". Initialisation false.
     */
    protected static boolean drawQuadrillage = true;

    /** QUADRILLAGE **/

    /** ZOOM **/

    /**
     * Diameter of the displayed points. Initialisation 6
     */
    protected static int diameterZoom;

    /**
     * Diameter of the displayed points for the selected level.
     */
    protected static int selectedLevelDiameterZoom;

    /**
     * Radius of a displayed point
     */
    protected static int radiusZoom;

    /**
     * Radius of a displayed point for the selected level
     */
    protected static int selectedLevelRadiusZoom;

    /**
     * Threshold to use the DIAMETER_ZOOM instead of DIAMETER. Initialisation 50.
     */
    protected static final int ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM = 50;

    /** ZOOM **/

    /** MARQUES SUR LES AXES **/

    /**
     * Longueur d'une marque sur les axes initialisé à 4.
     */
    protected static final int GRAPHAREA_TICK_LENGTH = 4;

    /**
     * Espacement entre deux marques sur les axes, initialisé à 25.
     */
    protected static final int GRAPHAREA_TICK_STEP = 25;

    /**
     * Abscisse du point d'origine des donnees du graphique
     */
    public static int graphAreaDataX;

    /**
     * Ordonnee du point d'origine des donnees du graphique
     */
    public static int graphAreaDataY;

    /**
     * Ordonnee des marques de l'axe X
     */
    protected static int graphAreaXTickLabelY;

    /**
     * Ordonnees des marques de l'axe X dans le cas d'une Date
     */
    protected static int graphAreaXTickLabelYForDate;

    /**
     * Abscisse des marques de l'axe Y
     */
    protected static int graphAreaYTickLabelX;

    /**
     * Abscisse des marques de l'axe Y dans le cas d'une Date
     */
    protected static int graphAreaYTickLabelXForDate;

    /** MARQUES SUR LES AXES **/

    /** FORMATS DATE ET HEURE **/

    /**
     * SimpleDateFormat used for TimeSeries
     */
    protected static final SimpleDateFormat DATE_SDF = Conversions.getSimpleDateFormat("dd/MM/yy");

    /**
     * SimpleDateFormat used for TimeSeries
     */
    protected static final SimpleDateFormat HOUR_SDF = Conversions.getSimpleDateFormat("HH:mm");

    /** FORMATS DATE ET HEURE **/

    /** COULEURS **/

    /**
     * Couleur du rectangle de zoom initialisée à cyan
     */
    protected static final Color ZOOMAREA_BORDER_COLOR = Color.cyan;

    /**
     * Alpha component of the color used for the curves on background. 0 means totally transparent. Max 100.
     * Initialisation 80.
     */
    protected static final int BG_IMAGE_ALPHA = 100;

    /**
     * Color of the background ...
     */
    protected static Color backgroundColor;

    /** COULEURS **/

    /**
     * If TRUE, a line is displayed to show the depth 0
     */
    protected static boolean add0DepthAndBathyLines;

    /**
     * Color of the "Quadrillage"
     */
    protected static Color quadrillageColor;

    /**
     * Initialisation bloc static
     *
     * @return
     */
    static {
	graphAreaYTickLabelX = 10;
	graphAreaDataX = graphAreaYTickLabelX + 35;

	graphAreaXTickLabelY = 10;
	graphAreaDataY = graphAreaXTickLabelY + 7;

	graphAreaYTickLabelXForDate = 2;
	graphAreaXTickLabelYForDate = 2;

	backgroundColor = null;
	try {
	    backgroundColor = Color
		    .decode("0x" + FileConfig.getScoop3FileConfig().getString("chart.background-color-for-charts"));
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	if (backgroundColor == null) {
	    // Bleu outremer (http://www.code-couleur.com/dictionnaire/couleur-b.html)
	    backgroundColor = Color.decode("0x" + "1B019B");
	}

	try {
	    diameter = Integer.parseInt(FileConfig.getScoop3FileConfig().getString("chart.point-diameter"));
	} catch (final Exception e) {
	    diameter = 4;
	}
	radius = diameter / 2;

	try {
	    diameterZoom = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("chart.point-diameter-for-zoom"));
	} catch (final Exception e) {
	    diameterZoom = 6;
	}
	selectedLevelDiameterZoom = diameterZoom + 4;
	radiusZoom = diameterZoom / 2;
	selectedLevelRadiusZoom = radiusZoom + 2;

	add0DepthAndBathyLines = "true"
		.equalsIgnoreCase(FileConfig.getScoop3FileConfig().getString("chart.draw-0-depth-and-bathy-lines"));

	quadrillageColor = null;
	try {
	    quadrillageColor = Color
		    .decode("0x" + FileConfig.getScoop3FileConfig().getString("chart.quadrillage-color"));
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	if (quadrillageColor == null) {
	    quadrillageColor = Color.GRAY;
	}
    }

    /**
     * @return the ADD_0_DEPTH_LINE
     */
    public static boolean add0DepthAndBathyLines() {
	return add0DepthAndBathyLines;
    }
}