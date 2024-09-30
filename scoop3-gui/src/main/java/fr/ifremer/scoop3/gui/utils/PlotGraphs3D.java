package fr.ifremer.scoop3.gui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.chart.controllers.mouse.selection.AWTScatterMouseSelector;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Dimension;
import org.jzy3d.plot3d.builder.delaunay.DelaunayTessellator;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.providers.RegularTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DateTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;

import fr.ifremer.scoop3.gui.data.DataFrame3D;
import fr.ifremer.scoop3.infra.logger.SC3Logger;

/**
 * Manager pour la réalisation des tracés jzy3d dans un JPanel Swing.
 *
 */
public class PlotGraphs3D {

    private DelaunayTessellator delaunayTessellator = null;
    private DataFrame3D dataFrame = null;
    private Chart chart = null;
    private JPanel graphArea = null;
    private Shape shape = null;
    private SelectableScatter scatter = null;
    private Coord3d[] coordinates = null;
    private ArrayList<Coord3d> delauneyCoordinates = null;
    private Coord3d[] realScatterCoordinates = null;
    private Double[] realScatterColorValues = null;
    private ArrayList<Coord3d> realDelauneyCoordinates = null;
    private Color[] colors = null;
    private int coordSize = 0;
    private int realCoordinatesCpt = 0;
    private String xParameter;
    private String yParameter;
    private String zParameter;
    private float minRangeColor;
    private float maxRangeColor;
    private final String colorMapComboBox;
    private final String zComboBox;
    private JLabel colorBarLabel;
    private ITickRenderer renderer;
    private static final String DISTANT_PATH_COLOR_BAR = "//visi1-sih-web/home/www/test-bt/LastGraphOfCoriolisOnHtml/colorBarLegend.png";
    private static final String LOCAL_PATH_COLOR_BAR = System.getProperty("user.home")
	    + "/scoop3/legendFor3DGraphs/colorBarLegend.png";
    private static final int NEW_WIDTH = 143;
    private static final int DEFAULT_HEIGHT = 693;
    private static int newHeight;
    private static final int MAX_DURATION_TO_WAIT_BEFORE_CANCEL_3D_GRAPH = 30;

    public PlotGraphs3D(final JPanel aGraphArea, final String colorMapComboBox, final String zComboBox,
	    final Double panelHeight) {
	graphArea = aGraphArea;
	this.zComboBox = zComboBox;
	this.colorMapComboBox = colorMapComboBox;
	if (panelHeight != null) {
	    newHeight = panelHeight.intValue();
	} else {
	    newHeight = DEFAULT_HEIGHT;
	}
	delaunayTessellator = new DelaunayTessellator();
	graphArea.setLayout(new BorderLayout());
	chart = AWTChartComponentFactory.chart(Quality.Nicest, "awt");// Quality upgrades the render of scatter graphs
	// first of all decreasing the performance, but you don't necessarily feel it
	((Component) chart.getCanvas()).setPreferredSize(graphArea.getSize());
	graphArea.add((Component) chart.getCanvas(), BorderLayout.CENTER);
	// paintColorBarLegend();
    }

    /**
     * Charge l'ensemble des données à tracer
     *
     * @param aDataFrame
     *            l'ensemble des données à tracer
     */
    public void loadDataFrame(final DataFrame3D aDataFrame) {
	dataFrame = aDataFrame;
    }

    /**
     * Efface le tracé courant pour être prêt à en faire un nouveau
     *
     */
    public void clearPreviousPlot() {
	if (shape != null) {
	    chart.getScene().getGraph().remove(shape);
	    shape.dispose();
	    shape = null;
	}
	if (scatter != null) {
	    chart.getScene().getGraph().remove(scatter);
	    scatter.dispose();
	    scatter = null;
	}
    }

    /**
     * Réalise le plot scatter (points non reliés)
     *
     * @param xParameter
     *            les données selon l'axe x
     * @param yParameter
     *            les données selon l'axe y
     * @param zParameter
     *            les données selon l'axe z
     * @throws PlotGraph3DException
     */
    public void plotScatter(final String xParameter, final String yParameter, final String zParameter,
	    final boolean useCycleNumber) throws PlotGraph3DException {
	this.xParameter = xParameter;
	this.yParameter = yParameter;
	this.zParameter = zParameter;
	clearPreviousPlot();
	final int count = dataFrame.getRowCount();
	coordinates = new Coord3d[count];
	double xFactor = 1.0;
	double yFactor = 1.0;
	double zFactor = 1.0;

	if ((xParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    xFactor = -1.0;
	}
	if ((yParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    yFactor = -1.0;
	}
	if ((zParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    zFactor = -1.0;
	}

	for (int i = 0; i < count; i++) {
	    coordinates[i] = new Coord3d(dataFrame.getValue(xParameter, i) * xFactor,
		    dataFrame.getValue(yParameter, i) * yFactor, dataFrame.getValue(zParameter, i) * zFactor);

	    scanScatterCoordinate(i, false); // Permet de déterminer la taille du jeu de données exact
	}

	if (coordSize != 0) {
	    realScatterCoordinates = new Coord3d[coordSize];
	    realScatterColorValues = new Double[coordSize];
	    colors = new Color[coordSize];
	    for (int j = 0; j < count; j++) {
		scanScatterCoordinate(j, true); // Permet d'éliminer les données tronquées
	    }
	    for (int k = 0; k < coordSize; k++) {
		setBlueColor(k); // Permet de créer un bleu aléatoire pour les points des graphs
	    }
	}

	// special case for cycle number variable if we display observation index instead
	chart.getAxeLayout().setXAxeLabel(
		xParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : xParameter);
	chart.getAxeLayout().setYAxeLabel(
		yParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : yParameter);
	chart.getAxeLayout().setZAxeLabel(
		zParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : zParameter);
	scanDate();

	try {
	    scatter = new SelectableScatter(realScatterCoordinates, colors);
	} catch (final NullPointerException e) {
	    JOptionPane.showMessageDialog(new JFrame(),
		    "Les paramètres sélectionnés ne permettent pas de créer un graph contenant suffisament de données",
		    "Erreur", JOptionPane.ERROR_MESSAGE);
	    throw new PlotGraph3DException(e);
	}
	scatter.setWidth(5);
	colorBarLegendAndPointsScatter();
	selectableRectangle();
	chart.getScene().getGraph().add(scatter);
    }

    /**
     * Réalise le plot "delauney", les données qui ne sont pas régulières son extrapolées pour dessiner une surface (par
     * triangulation delauney)
     *
     * @param xParameter
     *            les données selon l'axe x
     * @param yParameter
     *            les données selon l'axe y
     * @param zParameter
     *            les données selon l'axe z
     */
    public void plotDelaunayTessellator(final String xParameter, final String yParameter, final String zParameter,
	    final boolean useCycleNumber) {
	clearPreviousPlot();

	this.xParameter = xParameter;
	this.yParameter = yParameter;
	this.zParameter = zParameter;

	delauneyCoordinates = new ArrayList<Coord3d>();
	realDelauneyCoordinates = new ArrayList<Coord3d>();
	final int count = dataFrame.getRowCount();
	double xFactor = 1.0;
	double yFactor = 1.0;
	double zFactor = 1.0;
	if ((xParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (xParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    xFactor = -1.0;
	}
	if ((yParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (yParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    yFactor = -1.0;
	}
	if ((zParameter.compareTo(DataFrame3D.DEPTH_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.DEPTH_ADJUSTED_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.PRES_KEY) == 0)
		|| (zParameter.compareTo(DataFrame3D.PRES_ADJUSTED_KEY) == 0)) {
	    zFactor = -1.0;
	}

	for (int i = 0; i < count; i++) {
	    delauneyCoordinates.add(new Coord3d(dataFrame.getValue(xParameter, i) * xFactor,
		    dataFrame.getValue(yParameter, i) * yFactor, dataFrame.getValue(zParameter, i) * zFactor));
	    scanDelauneyCoordinate(i);
	}

	try {
	    realDelauneyCoordinates.get(1); // permet de détecter si realDelauneyCoordinates.size()=0, si oui, on
					    // provoque le catch IndexOutOfBoundsException, et on ne trace pas le
					    // graphique

	    // timeout of 30 sec for the colorBarLegend
	    // delaunayTessellator.build() ca be blocked by triangulation problems
	    final Duration timeout = Duration.ofSeconds(MAX_DURATION_TO_WAIT_BEFORE_CANCEL_3D_GRAPH);
	    final ExecutorService executor = Executors.newSingleThreadExecutor();

	    final Future<String> handler = executor.submit(new Callable() {
		@Override
		public Shape call() throws Exception {
		    shape = (Shape) delaunayTessellator.build(realDelauneyCoordinates);
		    return shape;
		}
	    });

	    try {
		handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
	    } catch (final TimeoutException | InterruptedException | ExecutionException e) {
		SC3Logger.LOGGER.error("Timeout happened while building the Delauney graph");
		handler.cancel(true);
	    }

	    executor.shutdownNow();
	} catch (final NullPointerException | StackOverflowError e) {
	    JOptionPane.showMessageDialog(new JFrame(),
		    "Une erreur de triangulation empêche le programme de tracer un graph 3D Delauney", "Erreur",
		    JOptionPane.ERROR_MESSAGE);
	} catch (final OutOfMemoryError e) {
	    JOptionPane.showMessageDialog(new JFrame(),
		    "Il n'y a pas assez de mémoire allouée pour le programme. Veuillez en allouer davantage dans les VM Arguments",
		    "Erreur", JOptionPane.ERROR_MESSAGE);
	} catch (final IndexOutOfBoundsException e) {
	    JOptionPane.showMessageDialog(new JFrame(),
		    "Les paramètres sélectionnés ne permettent pas de créer un graph contenant suffisament de données",
		    "Erreur", JOptionPane.ERROR_MESSAGE);
	}

	if (shape != null) {
	    final ColorMapper myColorMapper = new ColorMapper(new ColorMapRainbow(), shape.getBounds().getZmin(),
		    shape.getBounds().getZmax(), new Color(1, 1, 1, .5f));
	    shape.setColorMapper(myColorMapper);
	    scanDateLegendDelauney();
	    shape.setFaceDisplayed(true);
	    shape.setWireframeDisplayed(false);

	    // special case for cycle number variable if we display observation index instead
	    chart.getAxeLayout().setXAxeLabel(
		    xParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : xParameter);
	    chart.getAxeLayout().setYAxeLabel(
		    yParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : yParameter);
	    chart.getAxeLayout().setZAxeLabel(
		    zParameter.equals(DataFrame3D.CYCLE_NUMBER) && !useCycleNumber ? "OBSERVATION INDEX" : zParameter);
	    scanDate();// change le render d'un axe si c'est la date
	    if (zParameter.equals(DataFrame3D.TIME_KEY)) {
		renderer = new DateTickRenderer("dd/MM/YY HH:mm");
	    }
	    colorBarLegendDelauney(renderer);
	    chart.addController(new AWTCameraMouseController());
	    chart.getScene().getGraph().add(shape);
	}
    }

    /**
     * @param i
     * @param isSizeDone
     */
    private void scanScatterCoordinate(final int i, final Boolean isSizeDone) {

	Boolean falseCoordinate = false;

	// Des données tronquées peuvent prendre la valeur NaN qui n'est pas représentable sur un graph3d, on cherche
	// donc a les effacer des données
	if (Float.isNaN(coordinates[i].x) || Float.isNaN(coordinates[i].y) || Float.isNaN(coordinates[i].z)
		|| Double.isNaN(dataFrame.getValue(colorMapComboBox, i))) {
	    falseCoordinate = true;
	}

	// Des données tronquées peuvent prendre la valeur Infinity qui n'est pas représentable sur un graph3d, on
	// cherche donc a les effacer des données
	if ((coordinates[i].x == Float.POSITIVE_INFINITY) || (coordinates[i].y == Float.POSITIVE_INFINITY)
		|| (coordinates[i].z == Float.POSITIVE_INFINITY)
		|| (dataFrame.getValue(colorMapComboBox, i) == Double.POSITIVE_INFINITY)) {
	    falseCoordinate = true;
	}

	// Permet de déterminer la taille du tableau de coordonnées réellement tracable (sans Nan et sans Infinity)
	if (!falseCoordinate.booleanValue() && !isSizeDone.booleanValue()) {
	    coordSize++;
	}

	// Si la taille a été déterminée, on rempli le tableau avec les bonnes valeurs
	if (!falseCoordinate.booleanValue() && isSizeDone.booleanValue() && (realCoordinatesCpt < coordSize)) {
	    realScatterCoordinates[realCoordinatesCpt] = (coordinates[i]);
	    realScatterColorValues[realCoordinatesCpt] = (dataFrame.getValue(colorMapComboBox, i));
	    realCoordinatesCpt++;
	}
    }

    private void scanDelauneyCoordinate(final int i) {

	Boolean falseCoordinate = false;

	// Des données tronquées peuvent prendre la valeur NaN qui n'est pas représentable sur un graph3d, on cherche
	// donc a les effacer des données
	if (!colorMapComboBox.equals("NONE")) {
	    if (Float.isNaN(delauneyCoordinates.get(i).x) || Float.isNaN(delauneyCoordinates.get(i).y)
		    || Float.isNaN(delauneyCoordinates.get(i).z)
		    || Double.isNaN(dataFrame.getValue(colorMapComboBox, i))) {
		falseCoordinate = true;
	    }
	} else {
	    if (Float.isNaN(delauneyCoordinates.get(i).x) || Float.isNaN(delauneyCoordinates.get(i).y)
		    || Float.isNaN(delauneyCoordinates.get(i).z)) {
		falseCoordinate = true;
	    }
	}

	// Des données tronquées peuvent prendre la valeur Infinity qui n'est pas représentable sur un graph3d, on
	// cherche donc a les effacer des données
	if (!colorMapComboBox.equals("NONE")) {
	    if ((delauneyCoordinates.get(i).x == Float.POSITIVE_INFINITY)
		    || (delauneyCoordinates.get(i).y == Float.POSITIVE_INFINITY)
		    || (delauneyCoordinates.get(i).z == Float.POSITIVE_INFINITY)
		    || (dataFrame.getValue(colorMapComboBox, i) == Double.POSITIVE_INFINITY)) {
		falseCoordinate = true;
	    }
	} else {
	    if ((delauneyCoordinates.get(i).x == Float.POSITIVE_INFINITY)
		    || (delauneyCoordinates.get(i).y == Float.POSITIVE_INFINITY)
		    || (delauneyCoordinates.get(i).z == Float.POSITIVE_INFINITY)) {
		falseCoordinate = true;
	    }
	}

	if (!falseCoordinate.booleanValue()) {
	    realDelauneyCoordinates.add(delauneyCoordinates.get(i));
	}
    }

    /**
     * Applique une couleur bleue aux différents points composants le graph 3D scatter
     *
     * @param i
     */
    public void setBlueColor(final int i) {
	final Random rand = new Random();
	final float r = rand.nextFloat() / 2.5f;
	final float g = rand.nextFloat() / 1.2f;
	final float b = (rand.nextFloat() * 25) + 230;
	colors[i] = new Color(r, g, b, 0.8f);
    }

    /**
     * Colorie les points d'un scatter 3D en fonction du paramètre de la JComboBox color Parameter en un dégradé de
     * colorMapRainbow
     *
     * @param i
     */
    public void setColorMap() {
	final ColorMapRainbow colorMap = new ColorMapRainbow();
	for (int i = 0; i < coordSize; i++) {
	    if (DataFrame3D.DEPTH_KEY.equals(colorMapComboBox)
		    || DataFrame3D.DEPTH_ADJUSTED_KEY.equals(colorMapComboBox)
		    || DataFrame3D.PRES_KEY.equals(colorMapComboBox)
		    || DataFrame3D.PRES_ADJUSTED_KEY.equals(colorMapComboBox)) {
		// permet de colorier les points à l'envers car la profondeur est négative
		colors[i] = colorMap.getColor(realScatterCoordinates[i].x, realScatterCoordinates[i].y,
			-realScatterColorValues[i], -maxRangeColor, -minRangeColor);
	    } else {
		colors[i] = colorMap.getColor(realScatterCoordinates[i].x, realScatterCoordinates[i].y,
			realScatterColorValues[i], minRangeColor, maxRangeColor);
	    }
	}
    }

    /**
     * Scan les 3 axes pour trouver un paramètre Date, s'il est présent, le convertit de double au format dd/MM/YY HH:mm
     */
    private void scanDate() {
	if ((xParameter.equals(DataFrame3D.DATE_KEY)) || (xParameter.equals(DataFrame3D.TIME_KEY))) {
	    chart.getAxeLayout().setXTickRenderer(new DateTickRenderer("dd/MM/YY HH:mm"));
	}
	if ((yParameter.equals(DataFrame3D.DATE_KEY)) || (yParameter.equals(DataFrame3D.TIME_KEY))) {
	    chart.getAxeLayout().setYTickRenderer(new DateTickRenderer("dd/MM/YY HH:mm"));
	}
	if ((zParameter.equals(DataFrame3D.DATE_KEY)) || (zParameter.equals(DataFrame3D.TIME_KEY))) {
	    chart.getAxeLayout().setZTickRenderer(new DateTickRenderer("dd/MM/YY HH:mm"));
	}
    }

    /**
     * Affiche la color bar legend associée au paramètre de la JComboBox Z lorsqu'on trace un delauney 3D
     *
     * @param renderer
     */
    private void colorBarLegendDelauney(final ITickRenderer renderer) {
	final AWTColorbarLegend colorBarLegend = new AWTColorbarLegend(shape, new RegularTickProvider(10), renderer);
	colorBarLegend.setMinimumSize(new Dimension(130, colorBarLegend.getMinimumSize().height));
	shape.setLegend(colorBarLegend);
	shape.setLegendDisplayed(true);
    }

    /**
     * Colorie les points d'un scatter 3D en fonction du paramètre de la JComboBox color Parameter et affiche la color
     * bar legend associée au code couleur
     */
    private void colorBarLegendAndPointsScatter() throws PlotGraph3DException {
	if (!colorMapComboBox.equals("NONE")) {
	    minRangeColor = getMin(realScatterColorValues);
	    maxRangeColor = getMax(realScatterColorValues);
	    setColorMap();
	    saveColorBarLegend();
	    // paintColorBarLegend();
	    graphArea.add(colorBarLabel, BorderLayout.EAST);
	}
    }

    /**
     * Retourne le minimum d'un JComboBox.toString() utilisé pour calculer la plage de valeur que prend la color bar
     * legend
     *
     * @param Parameter
     * @return min
     */
    public float getMin(final Double[] parameter) {
	double min = parameter[0];
	for (int i = 1; i < parameter.length; i++) {
	    if (parameter[i] < min) {
		min = parameter[i];
	    }
	}
	return (float) min;
    }

    /**
     * Retourne le maximum d'un JComboBox.toString() utilisé pour calculer la plage de valeur que prend la color bar
     * legend
     *
     * @param Parameter
     * @return max
     */
    public float getMax(final Double[] parameter) {
	double max = parameter[0];
	for (int i = 1; i < parameter.length; i++) {
	    if (parameter[i] > max) {
		max = parameter[i];
	    }
	}
	return (float) max;
    }

    /**
     * Sauvegarde une image de la color bar legend en fonction de la JComboBox color Parameter lorsqu'on trace un
     * scatter 3D Le chemin de l'image peut etre modifié mais il faut une première image colorBarLegend dans le nouveau
     * chemin puisqu'on écrase l'image existante, on ne la crée pas
     */
    private void saveColorBarLegend() throws PlotGraph3DException {

	try {

	    final ArrayList<Coord3d> list = new ArrayList<Coord3d>();
	    for (int i = 0; i < coordSize; i++) {
		list.add(realScatterCoordinates[i]);
	    }

	    // timeout of 30 sec for the colorBarLegend
	    // delaunayTessellator.build() ca be blocked by triangulation problems
	    final Duration timeout = Duration.ofSeconds(MAX_DURATION_TO_WAIT_BEFORE_CANCEL_3D_GRAPH);
	    final ExecutorService executor = Executors.newSingleThreadExecutor();

	    final Future<String> handler = executor.submit(new Callable() {
		@Override
		public Shape call() throws Exception {
		    shape = (Shape) delaunayTessellator.build(list);
		    return shape;
		}
	    });

	    try {
		handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
	    } catch (final TimeoutException | InterruptedException | ExecutionException e) {
		SC3Logger.LOGGER.error("Timeout happened while building the color bar legend");
		handler.cancel(true);
	    }

	    executor.shutdownNow();

	    if (DataFrame3D.DEPTH_KEY.equals(colorMapComboBox)
		    || DataFrame3D.DEPTH_ADJUSTED_KEY.equals(colorMapComboBox)
		    || DataFrame3D.PRES_KEY.equals(colorMapComboBox)
		    || DataFrame3D.PRES_ADJUSTED_KEY.equals(colorMapComboBox)) {
		// permet de faire la légende à l'envers car la profondeur est négative
		final ColorMapper myColorMapper = new ColorMapper(new ColorMapRainbow(), -maxRangeColor, -minRangeColor,
			new Color(1, 1, 1, .5f));
		shape.setColorMapper(myColorMapper);
	    } else {
		final ColorMapper myColorMapper = new ColorMapper(new ColorMapRainbow(), minRangeColor, maxRangeColor,
			new Color(1, 1, 1, .5f));
		shape.setColorMapper(myColorMapper);
	    }

	} catch (final NullPointerException e) {
	    throw new PlotGraph3DException(e);
	}

	scanDateLegendScatter();

	final AWTColorbarLegend colorBarLegend = new AWTColorbarLegend(shape, new RegularTickProvider(10), renderer);

	final BufferedImage image = colorBarLegend.toImage(NEW_WIDTH, newHeight);
	colorBarLabel = new JLabel(new ImageIcon(image));

	// colorBarLegend.setImage(image, newWidth, newHeight);
	// try {
	//
	// // check if the directory is existing. If not, create it
	// if (!Files.exists(Paths.get(localPathColorBar))) {
	// final String[] directoryParts = localPathColorBar.split("/");
	// final String directoryPath = localPathColorBar.substring(0,
	// localPathColorBar.length() - directoryParts[directoryParts.length -
	// 1].length());
	// new File(directoryPath).mkdirs();
	// }
	//
	// colorBarLegend.saveImage(localPathColorBar/* distantPathColorBar */);
	// } catch (final IOException e1) {
	// SC3Logger.LOGGER.debug("Image non sauvegardée");
	// } catch (final IllegalArgumentException e) {
	// SC3Logger.LOGGER.debug("Image nulle " + e);
	// }

    }

    /**
     * Récupère l'image de la color bar legend sauvegardée pour l'afficher à droite du panel lorsqu'on trace un scatter
     * 3D
     */
    // private void paintColorBarLegend() {
    // BufferedImage myPicture = new BufferedImage(NEW_WIDTH, newHeight, BufferedImage.TYPE_INT_RGB);
    // try {
    // myPicture = ImageIO.read(new File(LOCAL_PATH_COLOR_BAR/* distantPathColorBar */));
    // } catch (final IOException e1) {
    // SC3Logger.LOGGER.debug("L'image ne peut pas etre lue " + e1);
    // }
    // // resize the colorBarLegend
    // final Image tmp = myPicture.getScaledInstance(NEW_WIDTH, newHeight, Image.SCALE_SMOOTH);
    // final BufferedImage dimg = new BufferedImage(NEW_WIDTH, newHeight, BufferedImage.TYPE_INT_ARGB);
    // final Graphics2D g2d = dimg.createGraphics();
    // g2d.drawImage(tmp, 0, 0, null);
    // g2d.dispose();
    // colorBarLabel = new JLabel(new ImageIcon(dimg));
    // }

    /**
     * Permet de renseigner le bon renderer pour la color bar legend lorsque la date est sélectionnée dans la JComboBox
     * color Parameter
     */
    private void scanDateLegendScatter() {
	if ((colorMapComboBox.equals(DataFrame3D.DATE_KEY)) || (colorMapComboBox.equals(DataFrame3D.TIME_KEY))) {
	    renderer = new DateTickRenderer("dd/MM/YY HH:mm");
	} else if ((!zComboBox.equals(DataFrame3D.DATE_KEY)) && (!zComboBox.equals(DataFrame3D.TIME_KEY))
		&& (!colorMapComboBox.equals(DataFrame3D.DATE_KEY))
		&& (!colorMapComboBox.equals(DataFrame3D.TIME_KEY))) {
	    renderer = chart.getView().getAxe().getLayout().getZTickRenderer();
	} else if (((zComboBox.equals(DataFrame3D.DATE_KEY)) || (zComboBox.equals(DataFrame3D.TIME_KEY)))
		&& ((!colorMapComboBox.equals(DataFrame3D.DATE_KEY))
			&& (!colorMapComboBox.equals(DataFrame3D.TIME_KEY)))) {
	    renderer = chart.getView().getAxe().getLayout().getYTickRenderer();
	}
    }

    /**
     * Permet de renseigner le bon renderer pour la color bar legend lorsque la date est sélectionnée dans la JComboBox
     * Z
     */
    private void scanDateLegendDelauney() {
	if (zComboBox.equals(DataFrame3D.DATE_KEY)) {
	    renderer = new DateTickRenderer("dd/MM/YY HH:mm");
	} else {
	    renderer = chart.getView().getAxe().getLayout().getZTickRenderer();
	}
    }

    private void selectableRectangle() {
	final AWTScatterMouseSelector selector = new AWTScatterMouseSelector(scatter);
	final AWTScatterDualModeMouseSelector mouse = new AWTScatterDualModeMouseSelector(chart, selector);
    }

    public String[] getListX() {
	final String[] x = new String[realScatterCoordinates.length];
	for (int i = 0; i < realScatterCoordinates.length; i++) {
	    x[i] = "xComboBox[" + (i + 1) + "] <- c(" + realScatterCoordinates[i].x + ")";
	}
	return x;
    }

    public String[] getListY() {
	final String[] y = new String[realScatterCoordinates.length];
	for (int i = 0; i < realScatterCoordinates.length; i++) {
	    y[i] = "yComboBox[" + (i + 1) + "] <- c(" + realScatterCoordinates[i].y + ")";
	}
	return y;
    }

    public String[] getListZ() {
	final String[] z = new String[realScatterCoordinates.length];
	for (int i = 0; i < realScatterCoordinates.length; i++) {
	    z[i] = "zComboBox[" + (i + 1) + "] <- c(" + realScatterCoordinates[i].z + ")";
	}
	return z;
    }

    public String[] getListColor() {
	final String[] color = new String[colors.length];
	for (int i = 0; i < colors.length; i++) {
	    color[i] = "rgb(" + colors[i].r + ", " + colors[i].g + ", " + colors[i].b + ", " + colors[i].a
		    + ", names = NULL, maxColorValue=1)";
	}
	return color;
    }

    public Integer getColorsSize() {
	return this.colors.length;
    }

    public Integer getCoordinatesSize() {
	return this.realScatterCoordinates.length;
    }

    public Coord3d[] getScatterCoordinates() {
	return this.realScatterCoordinates;
    }

    public ArrayList<Coord3d> getDelauneyCoordinates() {
	return this.realDelauneyCoordinates;
    }

    public Color[] getColors() {
	return this.colors;
    }

    public String getDistantPathColorBar() {
	return DISTANT_PATH_COLOR_BAR;
    }

    public String getLocalPathColorBar() {
	return LOCAL_PATH_COLOR_BAR;
    }

    public double[] getColorParameterValues(final String Parameter) {
	return this.dataFrame.getArray(Parameter);
    }

    public Chart getChart() {
	return this.chart;
    }

}
