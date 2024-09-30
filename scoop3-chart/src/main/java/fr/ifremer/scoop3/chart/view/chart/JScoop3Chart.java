package fr.ifremer.scoop3.chart.view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.valueAndQc.QCColor;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

/**
 * Graphique
 *
 * @author jdetoc
 *
 */
public class JScoop3Chart extends JComponent {

    /**
     * Composant SWING d'affichage de l'axe des X
     *
     * @author jdetoc
     *
     */
    public class JScoop3ChartColumnHeader extends JComponent {

	/**
	 *
	 */
	private static final long serialVersionUID = 5599268957524855035L;

	public JScoop3ChartColumnHeader() {
	    this.setOpaque(true);
	    this.setBackground(Color.WHITE);
	}

	/**
	 * Dessine le composant
	 *
	 * @param g
	 *            Environement graphique
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	// @Override
	@Override
	public void paintComponent(final Graphics g) {
	    super.paintComponent(g);
	    paintColumnHeader((Graphics2D) g);
	}
    }

    /**
     * Composant SWING d'affichage de l'axe des Y
     *
     * @author jdetoc
     *
     */
    public class JScoop3ChartRowHeader extends JComponent {

	/**
	 *
	 */
	private static final long serialVersionUID = -8813777057450102018L;

	public JScoop3ChartRowHeader() {
	    this.setOpaque(true);
	    this.setBackground(Color.WHITE);
	}

	/**
	 * Dessine le composant
	 *
	 * @param g
	 *            Environement graphique
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	// @Override
	@Override
	public void paintComponent(final Graphics g) {
	    super.paintComponent(g);
	    paintRowHeader((Graphics2D) g);
	}

    }

    /**
     * 1 day in milliseconds
     */
    private static final long ONE_DAY_IN_MS = 24 * 60 * 60 * 1000l;

    private static final long serialVersionUID = 6465634020965048864L;

    private static final int TIMESERIE_MIN_INTERVAL = 120000;

    // D�claration des attributs de JScoop3Chart

    /**
     * Axes des X
     */
    private JScoop3ChartColumnHeader columnHeader;

    /**
     * Image portant le nuage de mesures de l'ensemble des profils + les axes
     */
    private transient BufferedImage image = null;

    /**
     * Variable used for abscissa legend
     */
    private boolean isAbscissaADate;

    /**
     * Variable used for ordinate legend
     */
    private boolean isOrdinateADate;

    private final boolean isProfile;

    /**
     *
     */
    private final AbstractChartScrollPane jScoop3ScrollPane;

    /**
     * Axes des Y
     */
    private JScoop3ChartRowHeader rowHeader;

    /**
     * Used for the if JScoop3ChartCustomAspect.DRAW_QUADRILLAGE == TRUE
     */
    private final List<Integer> tickXPositions = new ArrayList<>();

    /**
     * Used for the if JScoop3ChartCustomAspect.DRAW_QUADRILLAGE == TRUE
     */
    private final List<Integer> tickYPositions = new ArrayList<>();

    /**
     * Variable physique a afficher en abscisse
     */
    protected transient ChartPhysicalVariable abscissaPhysicalVar = null;

    /**
     * Valeur de la marge de securite en abscisse d'un graphique de type profil
     */
    protected float securityMarginAbscissaProfile = 0.10f;

    /**
     * Valeur de la marge de securite en abscisse d'un graphique de type série temporelle
     */
    protected float securityMarginAbscissaTimeSerie = 0.05f;

    /**
     * Valeur de la marge de securite en ordonnee d'un graphique
     */
    protected float securityMarginOrdinate = 0.05f;

    protected transient JScoop3ChartComputeParameters computeParameters = null;

    /**
     * Variable physique a afficher en ordonnée
     */
    protected transient ChartPhysicalVariable ordinatePhysicalVar = null;

    // private final int defaultNbTicksNeeded = 7;

    /**
     *
     * @param jScoop3ScrollPane
     * @param chartWidth
     * @param chartHeight
     * @param isProfile
     * @param abscissaPhysicalVar
     */
    public JScoop3Chart(final AbstractChartScrollPane jScoop3ScrollPane, final boolean isProfile,
	    final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar/*
							    * , final boolean zoomWithPrecision
							    */, final int observationNumber,
	    final boolean timeserieDivided) {

	computeParameters = new JScoop3ChartComputeParameters();
	rowHeader = new JScoop3ChartRowHeader();
	columnHeader = new JScoop3ChartColumnHeader();
	this.jScoop3ScrollPane = jScoop3ScrollPane;
	this.isProfile = isProfile;

	setFirstObservationIndex(jScoop3ScrollPane.getFirstObservationIndex());
	setLastObservationIndex(jScoop3ScrollPane.getLastObservationIndex());

	setPhysicalVar(abscissaPhysicalVar, ordinatePhysicalVar);

	/* Calcul des bornes affichees du jeu de donnees */
	/*
	 * if (zoomWithPrecision) { computeRangeForZoomWithPrecision(false); } else {
	 */
	computeRange(false, observationNumber, timeserieDivided);
	// }
    }

    /**
     * Met a jour l'image de fond du graphique. L'image de fond represente l'ensemble des profils
     */
    public void computeBgImage(final Dimension2D imageDim, final Rectangle2D dataAreaForZoomLevelOne,
	    final Rectangle2D dataAreaForZoomLevelCurrent, final List<AdditionalGraph> additionalGraphs) {

	computeParameters.imageDimension = imageDim;
	computeParameters.imagePosition = jScoop3ScrollPane.getImagePosition(computeParameters.imageDimension);
	computeParameters.dataAreaForZoomLevelOne = dataAreaForZoomLevelOne;
	computeParameters.dataAreaForZoomLevelCurrent = dataAreaForZoomLevelCurrent;
	computeParameters.additionalGraphs = additionalGraphs;

	SC3Logger.LOGGER.trace("Abscisse:" + jScoop3ScrollPane.getAbscissaPhysicalVar().getLabel() + "|Dim:"
		+ computeParameters.imageDimension + "|Pos" + computeParameters.imagePosition);

	// Calcul les pixels a afficher
	jScoop3ScrollPane.computeBgPoints(computeParameters.imageDimension, computeParameters.imagePosition);

	this.setPreferredSize(new Dimension((int) Math.round(computeParameters.imageDimension.getWidth()),
		(int) Math.round(computeParameters.imageDimension.getHeight())));
	this.columnHeader.setPreferredSize(
		new Dimension(this.getPreferredSize().width, JScoop3ChartCustomAspect.graphAreaDataY));
	this.rowHeader.setPreferredSize(
		new Dimension(JScoop3ChartCustomAspect.graphAreaDataX, this.getPreferredSize().height));
	this.revalidate();

	/* Dessine l'image de trame de fond */
	// TODO pourquoi getDataAreaMaxX et pas getDataWidth
	// this.image = new BufferedImage(getDataAreaMaxX(), getDataAreaMaxY(), BufferedImage.TYPE_INT_ARGB);
	try {
	    this.image = new BufferedImage(
		    (int) Math.round(this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth()),
		    (int) Math.round(Math.max(1, this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight())),
		    BufferedImage.TYPE_INT_ARGB);
	} catch (final IllegalArgumentException e) {
	    // can happen when the user moves the frame on another screen
	    SC3Logger.LOGGER.error("Width and height of the frame can't be < 0 : " + e.getMessage());
	}

	final Graphics2D g2d = this.image.createGraphics();
	final RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	g2d.setRenderingHints(rh);

	// test
	// if (getAbscissaPhysicalVar().getLabel().equals("TEMP")) {
	// BufferedImage testImg = null;
	// try {
	// final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TEMP.PNG");
	// testImg = ImageIO.read(inputStream);
	// } catch (final IOException e1) {
	// e1.printStackTrace();
	// }
	// if (testImg != null) {
	// System.out.println("==> " + jScoop3ScrollPane.minAbscissaPhysVal + " , "
	// + jScoop3ScrollPane.maxAbscissaPhysVal + " / " + jScoop3ScrollPane.minOrdinatePhysVal + " , "
	// + jScoop3ScrollPane.maxOrdinatePhysVal);
	//
	// // compute correct position for the bigGraphics
	// final double tempMinX = 2.22;
	// final double tempMaxX = 24.75;
	// final double tempMinY = 0;
	// final double tempMaxY = 5098;
	//
	// final double minViewableX = jScoop3ScrollPane.computeMinViewableAbscissa();
	// final double maxViewableX = jScoop3ScrollPane.computeMaxViewableAbscissa();
	// final double minViewableY = jScoop3ScrollPane.computeMinViewableOrdonnee();
	// final double maxViewableY = jScoop3ScrollPane.computeMaxViewableOrdonnee();
	// System.out.println("=========> " + maxViewableX);
	//
	// if ((maxViewableX > tempMinX) && (minViewableX < tempMaxX) && (maxViewableY > tempMinY)) {
	//
	// System.out.println("=> " + jScoop3ScrollPane.getHorizontalScrollBar().getValue() + " / "
	// + jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getMinX());
	//
	// // pos X
	// final double posXD = Math.abs(tempMinX - minViewableX) / (maxViewableX - minViewableX);
	// final int posX = (int) Math
	// .round((posXD * this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth())
	// + this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getMinX());
	//
	// // width
	// final double ratioX = (tempMaxX - tempMinX) / (Math.abs(maxViewableX - minViewableX));
	// final int widthX = (int) Math
	// .round(this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() * ratioX);
	//
	// // pos Y
	// final double posYD = Math.abs(tempMinY - minViewableY) / (maxViewableY - minViewableY);
	// final int posY = (int) Math
	// .round((posYD * this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight())
	// + this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getMinY());
	//
	// // height
	// final double ratioY = (tempMaxY - tempMinY) / (Math.abs(maxViewableY - minViewableY));
	// final int heightY = (int) Math.round(
	// Math.max(1, this.jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() * ratioY));
	//
	// System.out.println("===> " + posX + " , " + posY + " / " + widthX + " , " + heightY);
	// g2d.drawImage(testImg, posX, posY, widthX, heightY, null);
	// }
	//
	// }
	// }

	// Compute wished factor for Width
	final float wishedFactorForWidth = (float) (1
		/ (((float) jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getWidth())
			/ jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() / 2));
	// Compute wished factor for Heigth
	final float wishedFactorForHeigth = (float) (1
		/ (((float) jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getHeight())
			/ jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() / 2));

	// Keep the max factor
	final float wishedZoomFactor = Math.max(wishedFactorForWidth, wishedFactorForHeigth);

	int xFrom = Integer.MAX_VALUE;
	int yFrom = Integer.MAX_VALUE;
	int colourFrom = Integer.MAX_VALUE;
	int xTo = Integer.MAX_VALUE;
	int yTo = Integer.MAX_VALUE;
	int colourTo = Integer.MAX_VALUE;
	int qcValueFrom;
	int qcValueTo;
	int worstQCValue;

	if (!jScoop3ScrollPane.isSuperposedMode()) {
	    g2d.setStroke(JScoop3ChartCustomAspect.CURRENT_CURB_STROKE);
	} else {
	    g2d.setStroke(JScoop3ChartCustomAspect.OTHER_CURB_STROKE);
	}

	for (int index = 0; index < jScoop3ScrollPane.getBgPoints().size(); index++) {
	    List<int[]> points = null;
	    try {
		points = jScoop3ScrollPane.getBgPoints().get(index);
		xFrom = Integer.MAX_VALUE;
		yFrom = Integer.MAX_VALUE;

		int currentLevel = 0;
		for (final int[] point : points) {
		    if (point == null) {
			// +1 because xFrom is not initialized
			if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
				&& (currentLevel == (jScoop3ScrollPane.getCurrentLevel() + 1))
				&& ((currentLevel == 0) || (points.get(currentLevel - 1) != null))) {
			    // SC3Logger.LOGGER.debug("310 :" + currentLevel + " [" + xFrom + ", " + yFrom + "]");
			    g2d.drawOval(xFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
				    yFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
				    JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
				    JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
			}
			currentLevel++;
			continue;
		    }
		    xTo = point[0];
		    yTo = point[1];
		    colourTo = point[2];
		    if ((xFrom != Integer.MAX_VALUE) && (xTo != Integer.MAX_VALUE)/* && (yTo >= yFrom) */) {
			if ((colourTo < QCColor.QC_COLOR_MAP.size()) && (colourFrom < QCColor.QC_COLOR_MAP.size())) {
			    if (!jScoop3ScrollPane.isSuperposedMode()) {
				// line color = worst qc
				g2d.setColor(QCColor.QC_COLOR_MAP.get(Math.max(colourTo, colourFrom)));
			    } else {
				Color color = QCColor.QC_COLOR_MAP.get(Math.max(colourTo, colourFrom));
				// the darker version of white is not enough dark
				if (color == Color.WHITE) {
				    color = new Color(230, 230, 230);
				}
				g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
					JScoop3ChartCustomAspect.BG_IMAGE_ALPHA).darker());
			    }
			    // Draw line before points ...
			    if (jScoop3ScrollPane.isSuperposedMode()
				    || JScoop3ChartScrollPaneAbstract.isDisplayLine()) {
				if (JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
					&& JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()) {
				    g2d.drawLine(xFrom, yFrom, xTo, yTo);
				} else {
				    if ((!JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
					    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(colourFrom)
					    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(colourTo))
					    || (!JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()
						    && !JScoop3ChartScrollPaneAbstract.getQcToExclude()
							    .contains(colourFrom)
						    && !JScoop3ChartScrollPaneAbstract.getQcToExclude()
							    .contains(colourTo))) {
					g2d.drawLine(xFrom, yFrom, xTo, yTo);
				    }
				}
			    }
			    // Only the current observation is displayed
			    if (JScoop3ChartScrollPaneAbstract.isDisplayPoints(colourFrom)) {
				// point color = qc
				final Color color = QCColor.QC_COLOR_MAP.get(colourFrom);
				g2d.setColor(jScoop3ScrollPane.isSuperposedMode() ? color.darker() : color);
				if (wishedZoomFactor > JScoop3ChartCustomAspect.ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM) {
				    g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radiusZoom,
					    yFrom - JScoop3ChartCustomAspect.radiusZoom,
					    JScoop3ChartCustomAspect.diameterZoom,
					    JScoop3ChartCustomAspect.diameterZoom);
				} else {
				    g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radius,
					    yFrom - JScoop3ChartCustomAspect.radius, JScoop3ChartCustomAspect.diameter,
					    JScoop3ChartCustomAspect.diameter);
				}
			    }
			    // +1 because xFrom is not initialized
			    if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
				    && (currentLevel == (jScoop3ScrollPane.getCurrentLevel() + 1))
				    && ((currentLevel == 0) || (points.get(currentLevel - 1) != null))) {
				// SC3Logger.LOGGER.debug("310 :" + currentLevel + " [" + xFrom + ", " + yFrom +
				// "]");
				g2d.drawOval(xFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
					yFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
					JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
					JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
			    }
			    // line color = worst qc
			    g2d.setColor(QCColor.QC_COLOR_MAP.get(Math.max(colourTo, colourFrom)));
			} else {
			    // draw line before points ...
			    if (JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
				    && JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()) {
				g2d.drawLine(xFrom, yFrom, xTo, yTo);
			    } else {
				if ((!JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
					&& JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(colourFrom)
					&& JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(colourTo))
					|| (!JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()
						&& !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(colourFrom)
						&& !JScoop3ChartScrollPaneAbstract.getQcToExclude()
							.contains(colourTo))) {
				    g2d.drawLine(xFrom, yFrom, xTo, yTo);
				}
			    }
			    if (!jScoop3ScrollPane.isSuperposedMode()) {
				// Only the current observation is displayed
				g2d.setColor(Color.lightGray);
				if (JScoop3ChartScrollPaneAbstract.isDisplayPoints(-1)) {
				    if (wishedZoomFactor > JScoop3ChartCustomAspect.ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM) {
					g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radiusZoom,
						yFrom - JScoop3ChartCustomAspect.radiusZoom,
						JScoop3ChartCustomAspect.diameterZoom,
						JScoop3ChartCustomAspect.diameterZoom);
				    } else {
					g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radius,
						yFrom - JScoop3ChartCustomAspect.radius,
						JScoop3ChartCustomAspect.diameter, JScoop3ChartCustomAspect.diameter);
				    }
				}
			    } else {
				g2d.setColor(Color.lightGray.darker());
			    }
			}
		    }
		    xFrom = xTo;
		    yFrom = yTo;
		    colourFrom = colourTo;
		    currentLevel++;
		}
	    } catch (final Exception e) {
		final UnhandledException exception = new UnhandledException(
			"Erreur dans le parcours des points, valeur de la liste points : " + points
				+ ", bgPoints size : " + jScoop3ScrollPane.getBgPoints().size(),
			e);
	    }
	    if ((xTo != Integer.MAX_VALUE) && JScoop3ChartScrollPaneAbstract.isDisplayPoints(colourTo)) {
		if (!jScoop3ScrollPane.isSuperposedMode()) {
		    // line color = worst qc
		    g2d.setColor(QCColor.QC_COLOR_MAP.get(colourTo));
		} else {
		    final Color color = QCColor.QC_COLOR_MAP.get(colourTo);
		    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
			    JScoop3ChartCustomAspect.BG_IMAGE_ALPHA).darker());
		}
		if (wishedZoomFactor > JScoop3ChartCustomAspect.ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM) {
		    g2d.fillOval(xTo - JScoop3ChartCustomAspect.radiusZoom, yTo - JScoop3ChartCustomAspect.radiusZoom,
			    JScoop3ChartCustomAspect.diameterZoom, JScoop3ChartCustomAspect.diameterZoom);
		} else {
		    g2d.fillOval(xTo - JScoop3ChartCustomAspect.radius, yTo - JScoop3ChartCustomAspect.radius,
			    JScoop3ChartCustomAspect.diameter, JScoop3ChartCustomAspect.diameter);
		}
	    }
	}

	/*
	 * Draw additional Graphs (if needed)
	 *
	 * /!\ Additional graphs are NOT displayed if the JScoop3ChartScrollPaneAbstract.getCOEF_X() != 0
	 */
	if ((jScoop3ScrollPane.getAdditionalGraphs() != null) && !jScoop3ScrollPane.getAdditionalGraphs().isEmpty()
		&& (JScoop3ChartScrollPaneAbstract.getCoefX() == 0)) {
	    final ArrayList<AdditionalGraph> tempAdditionalGraphs = new ArrayList<AdditionalGraph>(
		    jScoop3ScrollPane.getAdditionalGraphs());
	    for (final AdditionalGraph additionalGraph : tempAdditionalGraphs) {

		if (additionalGraph.getBasicStroke() != null) {
		    g2d.setStroke(additionalGraph.getBasicStroke());
		} else {
		    g2d.setStroke(new BasicStroke());
		}

		if (additionalGraph.getColor() != null) {
		    g2d.setColor(additionalGraph.getColor());
		}

		xFrom = Integer.MAX_VALUE;
		yFrom = Integer.MAX_VALUE;
		// qcValueFrom = -1;
		qcValueFrom = Integer.MAX_VALUE;
		xTo = Integer.MAX_VALUE;
		yTo = Integer.MAX_VALUE;
		// qcValueTo = -1;
		qcValueTo = Integer.MAX_VALUE;

		final int indexMax = ((additionalGraph.getAbscissaIntValues() == null)
			|| (additionalGraph.getOrdinateIntValues() == null)) ? 0
				: Math.min(additionalGraph.getAbscissaIntValues().length,
					additionalGraph.getOrdinateIntValues().length);
		for (int index = 0; index < indexMax; index++) {
		    xTo = additionalGraph.getAbscissaIntValues()[index];
		    yTo = additionalGraph.getOrdinateIntValues()[index];
		    qcValueTo = additionalGraph.getQCValue(index);
		    worstQCValue = Math.max(qcValueFrom, qcValueTo);
		    if ((worstQCValue < QCColor.QC_COLOR_MAP.size())) {
			g2d.setColor(QCColor.QC_COLOR_MAP.get(worstQCValue));
		    }

		    if ((xFrom != Integer.MAX_VALUE) && (yFrom != Integer.MAX_VALUE)) {
			if (JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
				&& JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()) {
			    g2d.drawLine(xFrom, yFrom, xTo, yTo);
			} else {
			    if ((!JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
				    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(qcValueFrom)
				    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(qcValueTo))
				    || (!JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()
					    && !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(qcValueFrom)
					    && !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(qcValueTo))) {
				g2d.drawLine(xFrom, yFrom, xTo, yTo);
			    } else if (g2d.getColor() == additionalGraph.getColor()) {
				g2d.drawLine(xFrom, yFrom, xTo, yTo);
			    }
			}
		    }
		    xFrom = xTo;
		    yFrom = yTo;
		    qcValueFrom = qcValueTo;
		}
	    }
	}

	final boolean isDefaultView = ((jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() == jScoop3ScrollPane
		.getDataAreaForZoomLevelCurrent().getWidth())
		&& (jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() == jScoop3ScrollPane
			.getDataAreaForZoomLevelCurrent().getHeight()));
	// if it's the default view, disable scrollbars to avoid bug in Argo version
	// if the user zoom, scrollbars are enable
	if (isDefaultView) {
	    jScoop3ScrollPane.getVerticalScrollBar().setEnabled(false);
	    jScoop3ScrollPane.getHorizontalScrollBar().setEnabled(false);
	} else {
	    jScoop3ScrollPane.getVerticalScrollBar().setEnabled(true);
	    jScoop3ScrollPane.getHorizontalScrollBar().setEnabled(true);
	}
    }

    /**
     * Calcule les bornes du jeu de donnees
     */
    public void computeRange(final boolean onlyAbscissa, final int observationNumber, final boolean timeserieDivided) {

	Double value;
	int qcValue;

	if (!onlyAbscissa) {
	    // Ordinate
	    jScoop3ScrollPane.minOrdinatePhysVal = Double.MAX_VALUE;
	    jScoop3ScrollPane.maxOrdinatePhysVal = Double.MIN_VALUE;
	    if (getOrdinatePhysicalVar().getPhysicalValuesByStation() != null) {
		if (!timeserieDivided) {
		    for (int index = getFirstObservationIndex(); index <= getLastObservationIndex(); index++) {
			// force sleep, waiting for the convertScoop3ModelToChartModel() method to finish making
			// chartDataset
			while (getOrdinatePhysicalVar().getPhysicalValuesByStation().size() <= index) {
			    try {
				Thread.sleep(1);
			    } catch (final InterruptedException e) {
				e.printStackTrace();
			    }
			}
			for (int level = 0; level < getOrdinatePhysicalVar().getPhysicalValuesByStation()
				.get(index).length; level++) {
			    value = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(index)[level];
			    qcValue = getOrdinatePhysicalVar().getQcValuesByStation().get(index)[level];

			    if ((value != null)
				    && ((AbstractChartScrollPane.getQcToDisplay().isEmpty()
					    && AbstractChartScrollPane.getQcToExclude().isEmpty())
					    || (!isProfile && ((!AbstractChartScrollPane.getQcToDisplay().isEmpty()
						    && AbstractChartScrollPane.getQcToDisplay().contains(qcValue))
						    || (!AbstractChartScrollPane.getQcToExclude().isEmpty()
							    && (!AbstractChartScrollPane.getQcToExclude()
								    .contains(qcValue)))))
					    || isProfile)
				    && ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY)
					    && (!Double.isNaN(value)))) {
				if ((value < jScoop3ScrollPane.minOrdinatePhysVal)
					|| (jScoop3ScrollPane.minOrdinatePhysVal == Double.MAX_VALUE)) {
				    jScoop3ScrollPane.minOrdinatePhysVal = value;
				}
				if ((value > jScoop3ScrollPane.maxOrdinatePhysVal)
					|| (jScoop3ScrollPane.maxOrdinatePhysVal == Double.MIN_VALUE)) {
				    jScoop3ScrollPane.maxOrdinatePhysVal = value;
				}
			    }
			}
		    }
		} else {
		    final int index = observationNumber;
		    for (int level = 0; level < getOrdinatePhysicalVar().getPhysicalValuesByStation()
			    .get(index).length; level++) {
			value = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(index)[level];
			qcValue = getOrdinatePhysicalVar().getQcValuesByStation().get(index)[level];

			if ((value != null)
				&& ((AbstractChartScrollPane.getQcToDisplay().isEmpty()
					&& AbstractChartScrollPane.getQcToExclude().isEmpty())
					|| (!isProfile && ((!AbstractChartScrollPane.getQcToDisplay().isEmpty()
						&& AbstractChartScrollPane.getQcToDisplay().contains(qcValue))
						|| (!AbstractChartScrollPane.getQcToExclude().isEmpty()
							&& (!AbstractChartScrollPane.getQcToExclude()
								.contains(qcValue)))))
					|| isProfile)
				&& ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY)
					&& (!Double.isNaN(value)))) {
			    if ((value < jScoop3ScrollPane.minOrdinatePhysVal)
				    || (jScoop3ScrollPane.minOrdinatePhysVal == Double.MAX_VALUE)) {
				jScoop3ScrollPane.minOrdinatePhysVal = value;
			    }
			    if ((value > jScoop3ScrollPane.maxOrdinatePhysVal)
				    || (jScoop3ScrollPane.maxOrdinatePhysVal == Double.MIN_VALUE)) {
				jScoop3ScrollPane.maxOrdinatePhysVal = value;
			    }
			}
		    }
		}
	    }

	    // No valid values ....
	    if ((jScoop3ScrollPane.minOrdinatePhysVal == Double.MAX_VALUE)
		    && (jScoop3ScrollPane.maxOrdinatePhysVal == Double.MIN_VALUE)) {
		jScoop3ScrollPane.minOrdinatePhysVal = 0;
		jScoop3ScrollPane.maxOrdinatePhysVal = 0;
	    }
	}

	// Abscissa
	jScoop3ScrollPane.minAbscissaPhysVal = Double.MAX_VALUE;
	jScoop3ScrollPane.maxAbscissaPhysVal = Double.MIN_VALUE;
	if (getAbscissaPhysicalVar().getPhysicalValuesByStation() != null) {
	    if (!timeserieDivided) {
		for (int index = getFirstObservationIndex(); index <= getLastObservationIndex(); index++) {
		    // force sleep, waiting for the convertScoop3ModelToChartModel() method to finish making
		    // chartDataset
		    while (getAbscissaPhysicalVar().getPhysicalValuesByStation().size() <= index) {
			try {
			    Thread.sleep(1);
			} catch (final InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		    for (int level = 0; level < getAbscissaPhysicalVar().getPhysicalValuesByStation()
			    .get(index).length; level++) {
			value = getAbscissaPhysicalValueForStationForLevel(index, level);
			qcValue = getAbscissaPhysicalVar().getQcValuesByStation().get(index)[level];

			if ((value != null)
				&& ((AbstractChartScrollPane.getQcToDisplay().isEmpty()
					&& AbstractChartScrollPane.getQcToExclude().isEmpty())
					|| (isProfile && ((!AbstractChartScrollPane.getQcToDisplay().isEmpty()
						&& AbstractChartScrollPane.getQcToDisplay()
							.contains(
								qcValue))
						|| (!AbstractChartScrollPane.getQcToExclude().isEmpty()
							&& (!AbstractChartScrollPane.getQcToExclude()
								.contains(qcValue)))
						|| (getAbscissaPhysicalVar().getLabel().equals("PSAL")
							&& getOrdinatePhysicalVar().getLabel().equals("TEMP"))))
					|| !isProfile)
				&& ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY))) {
			    if ((value < jScoop3ScrollPane.minAbscissaPhysVal)
				    || (jScoop3ScrollPane.minAbscissaPhysVal == Double.MAX_VALUE)
				    || Double.isNaN(jScoop3ScrollPane.minAbscissaPhysVal)) {
				jScoop3ScrollPane.minAbscissaPhysVal = value;
			    }
			    if ((value > jScoop3ScrollPane.maxAbscissaPhysVal)
				    || (jScoop3ScrollPane.maxAbscissaPhysVal == Double.MIN_VALUE)
				    || Double.isNaN(jScoop3ScrollPane.maxAbscissaPhysVal)) {
				jScoop3ScrollPane.maxAbscissaPhysVal = value;
			    }
			}
		    }
		}
	    } else {
		final int index = observationNumber;
		for (int level = 0; level < getAbscissaPhysicalVar().getPhysicalValuesByStation()
			.get(index).length; level++) {
		    value = getAbscissaPhysicalValueForStationForLevel(index, level);
		    qcValue = getAbscissaPhysicalVar().getQcValuesByStation().get(index)[level];

		    if ((value != null)
			    && ((AbstractChartScrollPane.getQcToDisplay().isEmpty()
				    && AbstractChartScrollPane.getQcToExclude().isEmpty())
				    || (isProfile && ((!AbstractChartScrollPane.getQcToDisplay().isEmpty()
					    && AbstractChartScrollPane.getQcToDisplay().contains(qcValue))
					    || (!AbstractChartScrollPane.getQcToExclude().isEmpty()
						    && (!AbstractChartScrollPane.getQcToExclude().contains(qcValue)))
					    || (getAbscissaPhysicalVar().getLabel().equals("PSAL")
						    && getOrdinatePhysicalVar().getLabel().equals("TEMP"))))
				    || !isProfile)
			    && ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY))) {
			if ((value < jScoop3ScrollPane.minAbscissaPhysVal)
				|| (jScoop3ScrollPane.minAbscissaPhysVal == Double.MAX_VALUE)
				|| Double.isNaN(jScoop3ScrollPane.minAbscissaPhysVal)) {
			    jScoop3ScrollPane.minAbscissaPhysVal = value;
			}
			if ((value > jScoop3ScrollPane.maxAbscissaPhysVal)
				|| (jScoop3ScrollPane.maxAbscissaPhysVal == Double.MIN_VALUE)
				|| Double.isNaN(jScoop3ScrollPane.maxAbscissaPhysVal)) {
			    jScoop3ScrollPane.maxAbscissaPhysVal = value;
			}
		    }
		}
	    }
	}

	// No valid values ....
	if ((jScoop3ScrollPane.minAbscissaPhysVal == Double.MAX_VALUE)
		&& (jScoop3ScrollPane.maxAbscissaPhysVal == Double.MIN_VALUE)) {
	    jScoop3ScrollPane.minAbscissaPhysVal = 0;
	    jScoop3ScrollPane.maxAbscissaPhysVal = 0;
	}

	// if min == max, decrease min and increase max to display the only point of observation
	if (jScoop3ScrollPane.minAbscissaPhysVal == jScoop3ScrollPane.maxAbscissaPhysVal) {
	    if (!getAbscissaPhysicalVar().getLabel().equals("Time")) {
		jScoop3ScrollPane.minAbscissaPhysVal -= 1;
		jScoop3ScrollPane.maxAbscissaPhysVal += 1;
	    } else {
		jScoop3ScrollPane.minAbscissaPhysVal -= 4.32e+7;
		jScoop3ScrollPane.maxAbscissaPhysVal += 4.32e+7;
	    }
	}

	if (!onlyAbscissa) {
	    jScoop3ScrollPane.realMinOrdinatePhysVal = jScoop3ScrollPane.minOrdinatePhysVal;
	    jScoop3ScrollPane.realMaxOrdinatePhysVal = jScoop3ScrollPane.maxOrdinatePhysVal;
	}

	jScoop3ScrollPane.realMinAbscissaPhysVal = jScoop3ScrollPane.minAbscissaPhysVal;
	jScoop3ScrollPane.realMaxAbscissaPhysVal = jScoop3ScrollPane.maxAbscissaPhysVal;

	if (!timeserieDivided) {
	    computeRangeWithAdditionalPoints(onlyAbscissa);
	}

	double diffAbscissa = 0;
	if ((jScoop3ScrollPane.minAbscissaPhysVal != Double.MAX_VALUE)
		&& (jScoop3ScrollPane.maxAbscissaPhysVal != Double.MIN_VALUE)) {
	    /*
	     * Add space at the left and the right of the graph
	     */

	    if (isAbscissaADate) {
		diffAbscissa = Math.abs(jScoop3ScrollPane.maxAbscissaPhysVal - jScoop3ScrollPane.minAbscissaPhysVal)
			* securityMarginAbscissaTimeSerie;
		if (diffAbscissa < TIMESERIE_MIN_INTERVAL) {
		    diffAbscissa = TIMESERIE_MIN_INTERVAL;
		}
	    } else {
		diffAbscissa = Math.abs(jScoop3ScrollPane.maxAbscissaPhysVal - jScoop3ScrollPane.minAbscissaPhysVal)
			* securityMarginAbscissaProfile;
	    }

	}

	// Round the Min and Max values
	if (isAbscissaADate) {
	    jScoop3ScrollPane.minAbscissaPhysVal -= diffAbscissa;
	    jScoop3ScrollPane.maxAbscissaPhysVal += diffAbscissa;

	    final Calendar calendarForMin = Conversions.getUTCCalendar();
	    calendarForMin.setTime(new Date((long) jScoop3ScrollPane.minAbscissaPhysVal));
	    // permet d'arrondir en abscisse au jour précédent à 00h00
	    /*
	     * calendarForMin.set(Calendar.HOUR_OF_DAY, 0); calendarForMin.set(Calendar.MINUTE, 0);
	     * calendarForMin.set(Calendar.SECOND, 0); calendarForMin.set(Calendar.MILLISECOND, 0);
	     */

	    final Calendar calendarForMax = Conversions.getUTCCalendar();
	    calendarForMax.setTime(new Date((long) jScoop3ScrollPane.maxAbscissaPhysVal));
	    // permet d'arrondir en abscisse au jour suivant à 00h00
	    /*
	     * calendarForMax.set(Calendar.HOUR_OF_DAY, 0); calendarForMax.set(Calendar.MINUTE, 0);
	     * calendarForMax.set(Calendar.SECOND, 0); calendarForMax.set(Calendar.MILLISECOND, 0);
	     * calendarForMax.add(Calendar.DATE, 1);
	     */ // Go to next day

	    jScoop3ScrollPane.minAbscissaPhysVal = calendarForMin.getTimeInMillis();
	    jScoop3ScrollPane.maxAbscissaPhysVal = calendarForMax.getTimeInMillis();
	} else {

	    double margin = diffAbscissa;

	    // Arrondir le margin à l'ordre de grandeur approprié
	    final double magnitude = Math.pow(10, Math.floor(Math.log10(margin)));
	    margin = Math.ceil(margin / magnitude) * magnitude;

	    jScoop3ScrollPane.minAbscissaPhysVal -= margin;
	    jScoop3ScrollPane.maxAbscissaPhysVal += margin;

	}

	if (!onlyAbscissa) {
	    double diffMinOrdinate = 0;
	    double diffMaxOrdinate = 0;
	    double diffOrdinate = 0;
	    if ((jScoop3ScrollPane.minOrdinatePhysVal != Double.MAX_VALUE)
		    && (jScoop3ScrollPane.maxOrdinatePhysVal != Double.MIN_VALUE)) {
		/*
		 * Add space at the top and the bottom of the graph
		 */
		diffOrdinate = Math.abs(jScoop3ScrollPane.maxOrdinatePhysVal - jScoop3ScrollPane.minOrdinatePhysVal)
			* securityMarginOrdinate;
		if (diffOrdinate < 0.5) {
		    diffOrdinate = 0.5f;
		} else if (diffOrdinate < 1) {
		    diffOrdinate = 1;
		}

		diffMinOrdinate = jScoop3ScrollPane.minOrdinatePhysVal - diffOrdinate;
		diffMaxOrdinate = jScoop3ScrollPane.maxOrdinatePhysVal + diffOrdinate;
	    }

	    // Round the Min and Max values
	    if (isOrdinateADate) {
		jScoop3ScrollPane.minOrdinatePhysVal -= diffOrdinate;
		jScoop3ScrollPane.maxOrdinatePhysVal += diffOrdinate;

		final Calendar calendarForMin = Conversions.getUTCCalendar();
		calendarForMin.setTime(new Date((long) jScoop3ScrollPane.minOrdinatePhysVal));
		calendarForMin.set(Calendar.DAY_OF_MONTH, 1);
		calendarForMin.set(Calendar.HOUR_OF_DAY, 0);
		calendarForMin.set(Calendar.MINUTE, 0);
		calendarForMin.set(Calendar.SECOND, 0);
		calendarForMin.set(Calendar.MILLISECOND, 0);

		final Calendar calendarForMax = Conversions.getUTCCalendar();
		calendarForMax.setTime(new Date((long) jScoop3ScrollPane.maxOrdinatePhysVal));
		calendarForMax.set(Calendar.DAY_OF_MONTH, 1);
		calendarForMax.add(Calendar.MONTH, 1);
		calendarForMax.set(Calendar.HOUR_OF_DAY, 0);
		calendarForMax.set(Calendar.MINUTE, 0);
		calendarForMax.set(Calendar.SECOND, 0);
		calendarForMax.set(Calendar.MILLISECOND, 0);

		jScoop3ScrollPane.minOrdinatePhysVal = calendarForMin.getTimeInMillis();
		jScoop3ScrollPane.maxOrdinatePhysVal = calendarForMax.getTimeInMillis();
	    } else {
		// Round at 0.5
		jScoop3ScrollPane.minOrdinatePhysVal = (float) (Math.floor(jScoop3ScrollPane.minOrdinatePhysVal * 2)
			/ 2);
		jScoop3ScrollPane.maxOrdinatePhysVal = (float) (Math.ceil(jScoop3ScrollPane.maxOrdinatePhysVal * 2)
			/ 2);

		// Add the security margin only if needed
		if (jScoop3ScrollPane.minOrdinatePhysVal > diffMinOrdinate) {
		    jScoop3ScrollPane.minOrdinatePhysVal -= diffOrdinate;
		}
		if (jScoop3ScrollPane.maxOrdinatePhysVal < diffMaxOrdinate) {
		    jScoop3ScrollPane.maxOrdinatePhysVal += diffOrdinate;
		}
	    }
	}

	SC3Logger.LOGGER.trace("computeRange    : abscissa min : {} - max : {}", jScoop3ScrollPane.minAbscissaPhysVal,
		jScoop3ScrollPane.maxAbscissaPhysVal);
	SC3Logger.LOGGER.trace("computeRange    : ordinate min : {} - max : {}", jScoop3ScrollPane.minOrdinatePhysVal,
		jScoop3ScrollPane.maxOrdinatePhysVal);
    }

    public void computeRangeWithAdditionalPoints(final boolean onlyAbscissa) {
	if (computeParameters.additionalGraphs != null) {
	    Double value;

	    for (final AdditionalGraph additionalGraph : computeParameters.additionalGraphs) {
		if (additionalGraph.isUsedToComputeRange()) {
		    int maxLevel;
		    if (!onlyAbscissa) {
			// Ordinate
			maxLevel = additionalGraph.getOrdinateValues().length;
			for (int level = 0; level < maxLevel; level++) {
			    value = additionalGraph.getOrdinateValues()[level];
			    if (value < jScoop3ScrollPane.minOrdinatePhysVal) {
				jScoop3ScrollPane.minOrdinatePhysVal = value;
			    }
			    if (value > jScoop3ScrollPane.maxOrdinatePhysVal) {
				jScoop3ScrollPane.maxOrdinatePhysVal = value;
			    }
			}
		    }
		    // Abscissa
		    if (additionalGraph.getAbscissaValues() != null) {
			maxLevel = additionalGraph.getAbscissaValues().length;
		    } else {
			maxLevel = additionalGraph.getTimeAbscissaValues().length;
		    }
		    for (int level = 0; level < maxLevel; level++) {
			if (additionalGraph.getAbscissaValues() != null) {
			    value = additionalGraph.getAbscissaValues()[level];
			} else {
			    value = (double) additionalGraph.getTimeAbscissaValues()[level];
			}
			if (value < jScoop3ScrollPane.minAbscissaPhysVal) {
			    jScoop3ScrollPane.minAbscissaPhysVal = value;
			}
			if (value > jScoop3ScrollPane.maxAbscissaPhysVal) {
			    jScoop3ScrollPane.maxAbscissaPhysVal = value;
			}
		    }
		}
	    }
	}
    }

    // /**
    // * Calcule les bornes du jeu de donnees
    // */
    // public void computeRangeForZoomWithPrecision(final boolean onlyAbscissa) {
    //
    // Double value;
    //
    // if (!onlyAbscissa) {
    // // Ordinate
    // jScoop3ScrollPane.minOrdinatePhysVal = Double.MAX_VALUE;
    // jScoop3ScrollPane.maxOrdinatePhysVal = Double.MIN_VALUE;
    // if (getOrdinatePhysicalVar().getPhysicalValuesByStation() != null) {
    // for (int index = getFirstObservationIndex(); index <= getLastObservationIndex(); index++) {
    // // force sleep, waiting for the convertScoop3ModelToChartModel() method to finish making
    // // chartDataset
    // while (getOrdinatePhysicalVar().getPhysicalValuesByStation().size() <= index) {
    // try {
    // Thread.sleep(1);
    // } catch (final InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // for (int level = 0; level < getOrdinatePhysicalVar().getPhysicalValuesByStation()
    // .get(index).length; level++) {
    // value = getOrdinatePhysicalVar().getPhysicalValuesByStation().get(index)[level];
    // if ((value != null)
    // && (!ChartDataset.PARAMETERS_DEFAULT_VALUES.contains(value)
    // && !ChartDataset.PARAMETERS_DEFAULT_VALUES
    // .contains(Float.parseFloat(value.toString())))
    // && ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY)
    // && (!Double.isNaN(value)))) {
    // if ((value < jScoop3ScrollPane.minOrdinatePhysVal)
    // || (jScoop3ScrollPane.minOrdinatePhysVal == Double.MAX_VALUE)) {
    // jScoop3ScrollPane.minOrdinatePhysVal = value;
    // }
    // if ((value > jScoop3ScrollPane.maxOrdinatePhysVal)
    // || (jScoop3ScrollPane.maxOrdinatePhysVal == Double.MIN_VALUE)) {
    // jScoop3ScrollPane.maxOrdinatePhysVal = value;
    // }
    // }
    // }
    // }
    // }
    //
    // // No valid values ....
    // if ((jScoop3ScrollPane.minOrdinatePhysVal == Double.MAX_VALUE)
    // && (jScoop3ScrollPane.maxOrdinatePhysVal == Double.MIN_VALUE)) {
    // jScoop3ScrollPane.minOrdinatePhysVal = 0;
    // jScoop3ScrollPane.maxOrdinatePhysVal = 0;
    // }
    // }
    //
    // // Abscissa
    // jScoop3ScrollPane.minAbscissaPhysVal = Double.MAX_VALUE;
    // jScoop3ScrollPane.maxAbscissaPhysVal = Double.MIN_VALUE;
    // if (getAbscissaPhysicalVar().getPhysicalValuesByStation() != null) {
    // for (int index = getFirstObservationIndex(); index <= getLastObservationIndex(); index++) {
    // // force sleep, waiting for the convertScoop3ModelToChartModel() method to finish making chartDataset
    // while (getAbscissaPhysicalVar().getPhysicalValuesByStation().size() <= index) {
    // try {
    // Thread.sleep(1);
    // } catch (final InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // for (int level = 0; level < getAbscissaPhysicalVar().getPhysicalValuesByStation()
    // .get(index).length; level++) {
    // value = getAbscissaPhysicalValueForStationForLevel(index, level);
    // if ((value != null)
    // && (!ChartDataset.PARAMETERS_DEFAULT_VALUES.contains(value)
    // && !ChartDataset.PARAMETERS_DEFAULT_VALUES
    // .contains(Float.parseFloat(value.toString())))
    // && ((value != Double.POSITIVE_INFINITY) && (value != Double.NEGATIVE_INFINITY))) {
    // if ((value < jScoop3ScrollPane.minAbscissaPhysVal)
    // || (jScoop3ScrollPane.minAbscissaPhysVal == Double.MAX_VALUE)
    // || Double.isNaN(jScoop3ScrollPane.minAbscissaPhysVal)) {
    // jScoop3ScrollPane.minAbscissaPhysVal = value;
    // }
    // if ((value > jScoop3ScrollPane.maxAbscissaPhysVal)
    // || (jScoop3ScrollPane.maxAbscissaPhysVal == Double.MIN_VALUE)
    // || Double.isNaN(jScoop3ScrollPane.maxAbscissaPhysVal)) {
    // jScoop3ScrollPane.maxAbscissaPhysVal = value;
    // }
    // }
    // }
    // }
    // }
    //
    // // No valid values ....
    // if ((jScoop3ScrollPane.minAbscissaPhysVal == Double.MAX_VALUE)
    // && (jScoop3ScrollPane.maxAbscissaPhysVal == Double.MIN_VALUE)) {
    // jScoop3ScrollPane.minAbscissaPhysVal = 0;
    // jScoop3ScrollPane.maxAbscissaPhysVal = 0;
    // }
    //
    // // if min == max, decrease min and increase max to display the only point of observation
    // if (jScoop3ScrollPane.minAbscissaPhysVal == jScoop3ScrollPane.maxAbscissaPhysVal) {
    // if (!getAbscissaPhysicalVar().getLabel().equals("Time")) {
    // jScoop3ScrollPane.minAbscissaPhysVal -= 1;
    // jScoop3ScrollPane.maxAbscissaPhysVal += 1;
    // } else {
    // jScoop3ScrollPane.minAbscissaPhysVal -= 4.32e+7;
    // jScoop3ScrollPane.maxAbscissaPhysVal += 4.32e+7;
    // }
    // }
    //
    // if (!onlyAbscissa) {
    // jScoop3ScrollPane.realMinOrdinatePhysVal = jScoop3ScrollPane.minOrdinatePhysVal;
    // jScoop3ScrollPane.realMaxOrdinatePhysVal = jScoop3ScrollPane.maxOrdinatePhysVal;
    // }
    //
    // jScoop3ScrollPane.realMinAbscissaPhysVal = jScoop3ScrollPane.minAbscissaPhysVal;
    // jScoop3ScrollPane.realMaxAbscissaPhysVal = jScoop3ScrollPane.maxAbscissaPhysVal;
    //
    // computeRangeWithAdditionalPoints(onlyAbscissa);
    //
    // // double diffMinAbscissa = 0;
    // // double diffMaxAbscissa = 0;
    // // double diffAbscissa = 0;
    // // if ((jScoop3ScrollPane.minAbscissaPhysVal != Double.MAX_VALUE)
    // // && (jScoop3ScrollPane.maxAbscissaPhysVal != Double.MIN_VALUE)) {
    // // /*
    // // * Add space at the left and the right of the graph
    // // */
    // //
    // // if (isAbscissaADate) {
    // // diffAbscissa = Math.abs(jScoop3ScrollPane.maxAbscissaPhysVal - jScoop3ScrollPane.minAbscissaPhysVal)
    // // * securityMarginAbscissaTimeSerie;
    // // if (diffAbscissa < timeserieMinInterval) {
    // // diffAbscissa = timeserieMinInterval;
    // // }
    // // } else {
    // // diffAbscissa = Math.abs(jScoop3ScrollPane.maxAbscissaPhysVal - jScoop3ScrollPane.minAbscissaPhysVal)
    // // * securityMarginAbscissaProfile;
    // // if (diffAbscissa < 0.5) {
    // // diffAbscissa = 0.5f;
    // // } else if (diffAbscissa < 1) {
    // // diffAbscissa = 1;
    // // }
    // // }
    // //
    // // diffMinAbscissa = jScoop3ScrollPane.minAbscissaPhysVal - diffAbscissa;
    // // diffMaxAbscissa = jScoop3ScrollPane.maxAbscissaPhysVal + diffAbscissa;
    // //
    // // }
    // //
    // // // Round the Min and Max values
    // // if (isAbscissaADate) {
    // // jScoop3ScrollPane.minAbscissaPhysVal -= diffAbscissa;
    // // jScoop3ScrollPane.maxAbscissaPhysVal += diffAbscissa;
    // //
    // // final Calendar calendarForMin = Conversions.getUTCCalendar();
    // // calendarForMin.setTime(new Date((long) jScoop3ScrollPane.minAbscissaPhysVal));
    // // // permet d'arrondir en abscisse au jour précédent à 00h00
    // // /*
    // // * calendarForMin.set(Calendar.HOUR_OF_DAY, 0); calendarForMin.set(Calendar.MINUTE, 0);
    // // * calendarForMin.set(Calendar.SECOND, 0); calendarForMin.set(Calendar.MILLISECOND, 0);
    // // */
    // //
    // // final Calendar calendarForMax = Conversions.getUTCCalendar();
    // // calendarForMax.setTime(new Date((long) jScoop3ScrollPane.maxAbscissaPhysVal));
    // // // permet d'arrondir en abscisse au jour suivant à 00h00
    // // /*
    // // * calendarForMax.set(Calendar.HOUR_OF_DAY, 0); calendarForMax.set(Calendar.MINUTE, 0);
    // // * calendarForMax.set(Calendar.SECOND, 0); calendarForMax.set(Calendar.MILLISECOND, 0);
    // // * calendarForMax.add(Calendar.DATE, 1);
    // // */ // Go to next day
    // //
    // // jScoop3ScrollPane.minAbscissaPhysVal = calendarForMin.getTimeInMillis();
    // // jScoop3ScrollPane.maxAbscissaPhysVal = calendarForMax.getTimeInMillis();
    // // } else {
    // // // Round at 0.5
    // // jScoop3ScrollPane.minAbscissaPhysVal = (float) (Math.floor(jScoop3ScrollPane.minAbscissaPhysVal * 2) / 2);
    // // jScoop3ScrollPane.maxAbscissaPhysVal = (float) (Math.ceil(jScoop3ScrollPane.maxAbscissaPhysVal * 2) / 2);
    // //
    // // // Add the security margin only if needed
    // // if (jScoop3ScrollPane.minAbscissaPhysVal > diffMinAbscissa) {
    // // jScoop3ScrollPane.minAbscissaPhysVal -= diffAbscissa;
    // // }
    // // if (jScoop3ScrollPane.maxAbscissaPhysVal < diffMaxAbscissa) {
    // // jScoop3ScrollPane.maxAbscissaPhysVal += diffAbscissa;
    // // }
    // //
    // // }
    // //
    // // if (!onlyAbscissa) {
    // // double diffMinOrdinate = 0;
    // // double diffMaxOrdinate = 0;
    // // double diffOrdinate = 0;
    // // if ((jScoop3ScrollPane.minOrdinatePhysVal != Double.MAX_VALUE)
    // // && (jScoop3ScrollPane.maxOrdinatePhysVal != Double.MIN_VALUE)) {
    // // /*
    // // * Add space at the top and the bottom of the graph
    // // */
    // // diffOrdinate = Math.abs(jScoop3ScrollPane.maxOrdinatePhysVal - jScoop3ScrollPane.minOrdinatePhysVal)
    // // * securityMarginOrdinate;
    // // if (diffOrdinate < 0.5) {
    // // diffOrdinate = 0.5f;
    // // } else if (diffOrdinate < 1) {
    // // diffOrdinate = 1;
    // // }
    // //
    // // diffMinOrdinate = jScoop3ScrollPane.minOrdinatePhysVal - diffOrdinate;
    // // diffMaxOrdinate = jScoop3ScrollPane.maxOrdinatePhysVal + diffOrdinate;
    // // }
    // //
    // // // Round the Min and Max values
    // // if (isOrdinateADate) {
    // // jScoop3ScrollPane.minOrdinatePhysVal -= diffOrdinate;
    // // jScoop3ScrollPane.maxOrdinatePhysVal += diffOrdinate;
    // //
    // // final Calendar calendarForMin = Conversions.getUTCCalendar();
    // // calendarForMin.setTime(new Date((long) jScoop3ScrollPane.minOrdinatePhysVal));
    // // calendarForMin.set(Calendar.DAY_OF_MONTH, 1);
    // // calendarForMin.set(Calendar.HOUR_OF_DAY, 0);
    // // calendarForMin.set(Calendar.MINUTE, 0);
    // // calendarForMin.set(Calendar.SECOND, 0);
    // // calendarForMin.set(Calendar.MILLISECOND, 0);
    // //
    // // final Calendar calendarForMax = Conversions.getUTCCalendar();
    // // calendarForMax.setTime(new Date((long) jScoop3ScrollPane.maxOrdinatePhysVal));
    // // calendarForMax.set(Calendar.DAY_OF_MONTH, 1);
    // // calendarForMax.add(Calendar.MONTH, 1);
    // // calendarForMax.set(Calendar.HOUR_OF_DAY, 0);
    // // calendarForMax.set(Calendar.MINUTE, 0);
    // // calendarForMax.set(Calendar.SECOND, 0);
    // // calendarForMax.set(Calendar.MILLISECOND, 0);
    // //
    // // jScoop3ScrollPane.minOrdinatePhysVal = calendarForMin.getTimeInMillis();
    // // jScoop3ScrollPane.maxOrdinatePhysVal = calendarForMax.getTimeInMillis();
    // // } else {
    // // // Round at 0.5
    // // jScoop3ScrollPane.minOrdinatePhysVal = (float) (Math.floor(jScoop3ScrollPane.minOrdinatePhysVal * 2)
    // // / 2);
    // // jScoop3ScrollPane.maxOrdinatePhysVal = (float) (Math.ceil(jScoop3ScrollPane.maxOrdinatePhysVal * 2)
    // // / 2);
    // //
    // // // Add the security margin only if needed
    // // if (jScoop3ScrollPane.minOrdinatePhysVal > diffMinOrdinate) {
    // // jScoop3ScrollPane.minOrdinatePhysVal -= diffOrdinate;
    // // }
    // // if (jScoop3ScrollPane.maxOrdinatePhysVal < diffMaxOrdinate) {
    // // jScoop3ScrollPane.maxOrdinatePhysVal += diffOrdinate;
    // // }
    // // }
    // // }
    //
    // SC3Logger.LOGGER.trace("computeRange : abscissa min : {} - max : {}", jScoop3ScrollPane.minAbscissaPhysVal,
    // jScoop3ScrollPane.maxAbscissaPhysVal);
    // SC3Logger.LOGGER.trace("computeRange : ordinate min : {} - max : {}", jScoop3ScrollPane.minOrdinatePhysVal,
    // jScoop3ScrollPane.maxOrdinatePhysVal);
    // }

    public Double getAbscissaPhysicalValueForStationForLevel(final int index, final int level) {
	Double abscissaValue = getAbscissaPhysicalVar().getPhysicalValuesByStation().get(index)[level];
	if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {

	    double min;
	    if (AbstractChartScrollPane.lastMinAbscissaPhysVal != Double.MIN_VALUE) {
		min = AbstractChartScrollPane.lastMinAbscissaPhysVal;
	    } else {
		min = jScoop3ScrollPane.minAbscissaPhysValBeforeCoefX;
	    }

	    double max;
	    if (AbstractChartScrollPane.lastMaxAbscissaPhysVal != Double.MAX_VALUE) {
		max = AbstractChartScrollPane.lastMaxAbscissaPhysVal;
	    } else {
		max = jScoop3ScrollPane.maxAbscissaPhysValBeforeCoefX;
	    }

	    abscissaValue += (index + 1) * JScoop3ChartScrollPaneAbstract.getCoefX() * (max - min);
	}
	return abscissaValue;
    }

    public ChartPhysicalVariable getAbscissaPhysicalVar() {
	return abscissaPhysicalVar;
    }

    /**
     * @return the columnHeader
     */
    public JScoop3ChartColumnHeader getColumnHeader() {
	return columnHeader;
    }

    public int getFirstObservationIndex() {
	return computeParameters.firstObservationIndex;
    }

    public int getLastObservationIndex() {
	return computeParameters.lastObservationIndex;
    }

    public ChartPhysicalVariable getOrdinatePhysicalVar() {
	return ordinatePhysicalVar;
    }

    /**
     * @return the rowHeader
     */
    public JScoop3ChartRowHeader getRowHeader() {
	return rowHeader;
    }

    public void prepareForDispose() {
	ordinatePhysicalVar.prepareForDispose();
	abscissaPhysicalVar.prepareForDispose();
	prepareForDisposeChartOnly();
    }

    public void prepareForDisposeChartOnly() {
	removeAll();
	image = null;
	rowHeader = null;
	columnHeader = null;
	ordinatePhysicalVar = null;
	abscissaPhysicalVar = null;
    }

    /**
     * @param additionalGraphs
     *            the additionalGraphs to set
     */
    public void setAdditionalSeriesToDisplay(final List<AdditionalGraph> additionalGraphs) {
	computeParameters.additionalGraphs = additionalGraphs;
    }

    public void setFirstObservationIndex(final int firstObservationIndex) {
	computeParameters.firstObservationIndex = firstObservationIndex;
    }

    public void setIsAbscissaADate(final boolean isAbscissaADate) {
	this.isAbscissaADate = isAbscissaADate;
    }

    public void setIsOrdinateADate(final boolean isOrdinateADate) {
	this.isOrdinateADate = isOrdinateADate;
    }

    public void setLastObservationIndex(final int lastObservationIndex) {
	computeParameters.lastObservationIndex = lastObservationIndex;
    }

    /**
     * Associe la variable physique au panneau d'affichage
     */
    private void setPhysicalVar(final ChartPhysicalVariable abscissaPhysicalVar,
	    final ChartPhysicalVariable ordinatePhysicalVar) {

	this.abscissaPhysicalVar = abscissaPhysicalVar;
	this.ordinatePhysicalVar = ordinatePhysicalVar;
	setIsAbscissaADate(abscissaPhysicalVar.isADate());
	setIsOrdinateADate(ordinatePhysicalVar.isADate());
    }

    /**
     * @param g2d
     * @param minAbscissaToDisplay
     */
    protected void paintColumnHeaderAbscissaIsADate(final Graphics2D g2d) {
	tickXPositions.clear();

	if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
	    return;
	}

	final long[] coefs = new long[] { 24 * 30 * ONE_DAY_IN_MS, // 2 years
		18 * 30 * ONE_DAY_IN_MS, // 1 year and half
		12 * 30 * ONE_DAY_IN_MS, // 1 year
		10 * 30 * ONE_DAY_IN_MS, // 10 months
		8 * 30 * ONE_DAY_IN_MS, // 8 months
		6 * 30 * ONE_DAY_IN_MS, // 6 months
		4 * 30 * ONE_DAY_IN_MS, // 4 months
		2 * 30 * ONE_DAY_IN_MS, // 2 months
		30 * ONE_DAY_IN_MS, // 1 month
		7 * ONE_DAY_IN_MS, // 1 week
		2 * ONE_DAY_IN_MS, // 2 day
		ONE_DAY_IN_MS, // 1 day
		ONE_DAY_IN_MS / 2l, // 1/2 day
		ONE_DAY_IN_MS / 4l, // 1/4 day
		ONE_DAY_IN_MS / 8l, // 3 hours
		60 * 60 * 1000l, // 1 hour
		30 * 60 * 1000l, // 1/2 hour
		15 * 60 * 1000l, // 1/4 hour
		5 * 60 * 1000l, // 5 minutes
		2 * 60 * 1000l, // 2 minutes
		60 * 1000l, // 1 minute
	};

	/*
	 * 0 means, first day of week. >0 means add month. <0 means : nothing to do
	 */
	final int[] firstDayOfMonthOrWeekIfCoefSelected = new int[] { 24, // 2 years
		18, // 1 year and half
		12, // 1 year
		10, // 10 months
		8, // 8 months
		6, // 6 months
		4, // 4 months
		2, // 2 months
		1, // 1 month
		0, // 1 week
		-1, // 2 days
		-1, // 1 day
		-1, // 1/2 day
		-1, // 1/4 day
		-1, // 3 hours
		-1, // 1 hour
		-1, // 1/2 hour
		-1, // 1/4 hour
		-1, // 5 minutes
		-1, // 2 minutes
		-1, // 1 minutes
	};

	// If there is more than 100 PX between 2 values when using this coef, keep it
	final int pxLimitForCoefSearching = 100;

	final double minViewable = jScoop3ScrollPane.computeMinViewableAbscissaForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableAbscissaForColumnHeader();

	// Abcissa tick
	g2d.setFont(new Font(null, Font.BOLD, 10));

	/*
	 * Compute Min and Max value for the "tick loop"
	 */
	final boolean isDefaultView = ((jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() == jScoop3ScrollPane
		.getDataAreaForZoomLevelCurrent().getWidth())
		&& (jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() == jScoop3ScrollPane
			.getDataAreaForZoomLevelCurrent().getHeight()));

	// Compute the precision
	final long minTickValue = (long) minViewable;
	final long maxTickValue = (long) maxViewable;
	final long diffMinMaxTickValue = maxTickValue - minTickValue;

	final List<String> tickLabels = new ArrayList<>();
	final double abscissaToPixelXFactor = jScoop3ScrollPane.computeAbscissaToPixelXFactor();

	int coefIndex = -1;

	if (isDefaultView) {
	    // Detect coef for 5 values
	    for (int index = 0; index < coefs.length; index++) {
		final long currentCoef = coefs[index];
		final long fiveTimesCoef = 5l * currentCoef;
		if (diffMinMaxTickValue >= fiveTimesCoef) {
		    coefIndex = index;
		    break;
		}
	    }
	} else {
	    /*
	     * Search the best coef to use ... it depends on the difference in PX between 2 values
	     */
	    for (int index = 0; index < coefs.length; index++) {
		final long currentCoef = coefs[index];
		final long twoTimesCoef = 2l * currentCoef;
		if (diffMinMaxTickValue >= twoTimesCoef) {
		    final int firstTickPos = (int) Math
			    .round((minTickValue - jScoop3ScrollPane.getMinAbscissaPhysVal()) * abscissaToPixelXFactor);
		    final int secondTickPos = (int) Math
			    .round(((minTickValue + currentCoef) - jScoop3ScrollPane.getMinAbscissaPhysVal())
				    * abscissaToPixelXFactor);

		    final int currentDiff = secondTickPos - firstTickPos;

		    if (coefIndex == -1) {
			coefIndex = index;
		    } else if (currentDiff >= pxLimitForCoefSearching) {
			coefIndex = index;
		    } else {
			break;
		    }
		}
	    }
	}

	if (coefIndex == -1) {
	    coefIndex = coefs.length - 1;
	}

	final boolean firstDayOfMonth = firstDayOfMonthOrWeekIfCoefSelected[coefIndex] > 0;
	final boolean firstDayOfWeek = firstDayOfMonthOrWeekIfCoefSelected[coefIndex] == 0;

	final Calendar calendarForCurrentTickValue = Conversions.getUTCCalendar();

	/*
	 * Compute ticks ...
	 */
	long currentTickValue = minTickValue;
	SimpleDateFormat sdf;
	int offset = 0;
	for (int index = 0; currentTickValue <= maxTickValue; index++) {
	    if (firstDayOfMonth) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue);
		calendarForCurrentTickValue.set(Calendar.DAY_OF_MONTH, 1);
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);
		if (index > 0) {
		    calendarForCurrentTickValue.add(Calendar.MONTH,
			    index * firstDayOfMonthOrWeekIfCoefSelected[coefIndex]);
		}

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
		sdf = Conversions.getSimpleDateFormat(Conversions.DATE_FORMAT_DDMMYYYY);
		offset = 25;
	    } else if (firstDayOfWeek) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue + (index * coefs[coefIndex]));
		calendarForCurrentTickValue.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
		sdf = Conversions.getSimpleDateFormat(Conversions.DATE_FORMAT_DDMMYYYY);
		offset = 25;
	    } else if (coefs[coefIndex] >= ONE_DAY_IN_MS) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue + (index * coefs[coefIndex]));
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);

		if ((coefs[coefIndex] == (2 * ONE_DAY_IN_MS))
			&& ((calendarForCurrentTickValue.get(Calendar.DAY_OF_MONTH) % 2) == 1)) {
		    // Only display odd days
		    calendarForCurrentTickValue.add(Calendar.DAY_OF_MONTH, 1);
		}

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
		sdf = Conversions.getSimpleDateFormat(Conversions.DATE_FORMAT_DDMMYYYY);
		offset = 25;
	    } else {
		currentTickValue = minTickValue + (index * coefs[coefIndex]);
		sdf = Conversions.getSimpleDateFormat(Conversions.DATE_FORMAT_DDMMYYYY_HHMM);
		offset = 42;
	    }
	    final int curTick = (int) Math
		    .round((currentTickValue - jScoop3ScrollPane.getMinAbscissaPhysVal()) * abscissaToPixelXFactor);
	    tickXPositions.add(curTick);

	    final Date date = new Date(currentTickValue);
	    tickLabels.add(sdf.format(date));
	}

	/*
	 * Display ticks
	 */
	final int nbIndexes = tickXPositions.size();
	for (int index = 0; index < nbIndexes; index++) {
	    final int curTick = tickXPositions.get(index);
	    final String toDisplay = tickLabels.get(index);

	    g2d.drawLine(curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH, curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY);

	    final int xPos = curTick;
	    g2d.drawString(toDisplay, xPos - offset, JScoop3ChartCustomAspect.graphAreaXTickLabelY);
	}

	if (!JScoop3ChartCustomAspect.drawQuadrillage) {
	    tickXPositions.clear();
	}
    }

    /**
     * @param g2d
     */
    protected void paintColumnHeaderAbscissaIsNotADate(final Graphics2D g2d) {
	tickXPositions.clear();

	if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
	    return;
	}

	final double[] coefs = new double[] { 500d, 200d, 100d, 50d, 20d, 10d, 5d, 2d, 1d, 0.5d, 0.2d, 0.1d, 0.05d,
		0.02d, 0.01d, 0.005d };
	// If there is more than 40 PX between 2 values when using this coef, keep it
	final int pxLimitForCoefSearching = 40;

	// Compute the precision
	// final double diffMaxMinViewableAbscissa = jScoop3ScrollPane.computeMaxViewableAbscissa()
	// - jScoop3ScrollPane.computeMinViewableAbscissa();
	String format;

	// Abcissa tick
	g2d.setFont(new Font(null, Font.BOLD, 10));

	/*
	 * Compute Min and Max value for the "tick loop"
	 */
	// final boolean isDefaultView = ((Math.abs(jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth()
	// - jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getWidth()) <= 0.001)
	// && (Math.abs(jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight()
	// - jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getHeight()) <= 0.001));
	// final int nbTicksNeeded;
	// final int nbTicksNeededMinusOne;
	// final int tickStepInPx;
	// final double valueToDisplay;

	final List<String> tickLabels = new ArrayList<>();
	final double abscissaToPixelXFactor = jScoop3ScrollPane.computeAbscissaToPixelXFactor();
	// final double diffMaxMinAbscissa;

	boolean addOffsetToFirstLabel = false;
	boolean subOffsetToLastLabel = false;

	// if (isDefaultView) {
	// // diffMaxMinViewableAbscissa is the difference between maxAbscissaViewable and minAbscissaViewable
	// if (diffMaxMinViewableAbscissa < 0.15d) {
	// format = "%.3f";
	// } else if (diffMaxMinViewableAbscissa < 20d) {
	// format = "%.2f";
	// } else if (diffMaxMinViewableAbscissa < 100d) {
	// format = "%.1f";
	// } else {
	// format = "%.0f";
	// }
	// // if it's the measure level/reference parameter we want to have integers in abscissa
	// if (jScoop3ScrollPane.getAbscissaPhysicalVar().isLevelParameter()) {
	// if (diffMaxMinViewableAbscissa < 4d) {
	// format = "%.1f";
	// } else {
	// format = "%.0f";
	// }
	// // set the max and min absissa value to integer
	// // jScoop3ScrollPane.maxAbscissaPhysVal = Math.ceil(jScoop3ScrollPane.getMaxAbscissaPhysVal());
	// // jScoop3ScrollPane.minAbscissaPhysVal = Math.floor(jScoop3ScrollPane.getMinAbscissaPhysVal());
	//
	// // nbTicksNeeded = (int) (jScoop3ScrollPane.maxAbscissaPhysVal - jScoop3ScrollPane.minAbscissaPhysVal) +
	// // 1;
	//
	// // if (nbTicksNeeded > defaultNbTicksNeeded) {
	// // // find the nearest divisor with defaultNbTicksNeeded (currently equals to 7)
	// // final Integer nearestDivisor = findTheNearestDivisor(nbTicksNeeded, defaultNbTicksNeeded);
	// // // if the nearest divisor is lower than 20 and not null, use this nearest divisor as the number of
	// // // ticks needed. 20 is the average limit of ticks which can be displayed on graph
	// // nbTicksNeeded = ((nearestDivisor == null) || (nearestDivisor >= 20) || (nearestDivisor <= 3))
	// // ? defaultNbTicksNeeded : nearestDivisor;
	// // }
	// // nbTicksNeeded = defaultNbTicksNeeded;
	// } // else {
	// nbTicksNeeded = defaultNbTicksNeeded;
	// // }
	// nbTicksNeededMinusOne = nbTicksNeeded - 1;
	// tickStepInPx = (int) Math.round(jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth())
	// / nbTicksNeededMinusOne;
	//
	// diffMaxMinAbscissa = jScoop3ScrollPane.getMaxAbscissaPhysVal() - jScoop3ScrollPane.getMinAbscissaPhysVal();
	//
	// addOffsetToFirstLabel = true;
	// subOffsetToLastLabel = true;
	//
	// for (int index = 0; index < nbTicksNeeded; index++) {
	// final int curTick = index * tickStepInPx;
	// tickXPositions.add(curTick);
	//
	// if (index == 0) {
	// valueToDisplay = jScoop3ScrollPane.getMinAbscissaPhysVal();
	// } else if (index == (nbTicksNeeded - 1)) {
	// valueToDisplay = jScoop3ScrollPane.getMaxAbscissaPhysVal();
	// } else {
	// valueToDisplay = jScoop3ScrollPane.getMinAbscissaPhysVal()
	// + ((diffMaxMinAbscissa * index) / nbTicksNeededMinusOne);
	// }
	// tickLabels.add(String.format(format, valueToDisplay));
	// }
	// } else {
	// min and max viewables on the graph
	final double minViewable = jScoop3ScrollPane.computeMinViewableAbscissaForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableAbscissaForColumnHeader();

	final double minTickValue = Math.floor(minViewable);
	final double maxTickValue = Math.ceil(maxViewable);

	final double diffMinMaxTickValue = /* maxTickValue - minTickValue; */maxViewable - minViewable;

	double firstTickValue;
	double lastTickValue;
	double tickStep;
	final double offsetLimit = 0.07d;

	/*
	 * Search the best coef to use ... it depends on the difference in PX between 2 values
	 */
	double bestCoef = Double.NEGATIVE_INFINITY;
	// go down through the list of coefs
	for (final double currentCoef : coefs) {
	    // if coef < difference between maxViewable and minViewable
	    if (diffMinMaxTickValue >= currentCoef) {
		final int firstTickPos = (int) Math
			.round((minTickValue - jScoop3ScrollPane.getMinAbscissaPhysVal()) * abscissaToPixelXFactor);
		final int secondTickPos = (int) Math
			.round(((minTickValue + currentCoef) - jScoop3ScrollPane.getMinAbscissaPhysVal())
				* abscissaToPixelXFactor);

		final int currentDiff = secondTickPos - firstTickPos;

		if (bestCoef == Double.NEGATIVE_INFINITY) {
		    bestCoef = currentCoef;
		    // stop the loop when we have secondTickPos - firstTickPos >= 40px. The goal is to have 40px
		    // between each tick
		} else if (currentDiff >= pxLimitForCoefSearching) {
		    bestCoef = currentCoef;
		} else {
		    break;
		}
	    }
	}
	if (bestCoef == Double.NEGATIVE_INFINITY) {
	    bestCoef = coefs[coefs.length - 1];
	}
	// if it's the measure level/reference parameter we want to have integers in abscissa
	if (jScoop3ScrollPane.getAbscissaPhysicalVar().isLevelParameter()) {
	    if (bestCoef >= 1) {
		firstTickValue = ((int) (Math.ceil(minTickValue) / bestCoef)) * bestCoef;
		lastTickValue = ((int) (Math.floor(maxTickValue) / bestCoef)) * bestCoef;
		format = "%.0f";
		tickStep = bestCoef;
	    } else {
		firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
		lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
		format = "%.1f";
		tickStep = bestCoef >= 0.1d ? bestCoef : 0.1d;
	    }
	} else {
	    if (bestCoef >= 1) {
		firstTickValue = ((int) (Math.ceil(minTickValue) / bestCoef)) * bestCoef;
		lastTickValue = ((int) (Math.floor(maxTickValue) / bestCoef)) * bestCoef;
		format = "%.0f";
	    } else if ((bestCoef >= 0.1) && (bestCoef < 1)) {
		firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
		lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
		format = "%.1f";
	    } else if ((bestCoef >= 0.01) && (bestCoef < 0.1)) {
		firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
		lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
		format = "%.2f";
	    } else {
		firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
		lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
		format = "%.3f";
	    }
	    tickStep = bestCoef;
	}

	addOffsetToFirstLabel = addOffsetToFirstLabel || (Math.abs(minTickValue - firstTickValue) < offsetLimit);
	subOffsetToLastLabel = subOffsetToLastLabel || (Math.abs(maxTickValue - lastTickValue) < offsetLimit);

	// Compute ticks
	double currentTickValue = firstTickValue;
	for (int index = 0; currentTickValue < lastTickValue; index++) {
	    currentTickValue = firstTickValue + (index * tickStep);
	    final int curTick = (int) Math
		    .round((currentTickValue - jScoop3ScrollPane.getMinAbscissaPhysVal()) * abscissaToPixelXFactor);
	    tickXPositions.add(curTick);

	    tickLabels.add(String.format(format, currentTickValue));
	}
	// }

	final int nbIndexes = tickXPositions.size();
	final int lastIndex = nbIndexes - 1;

	for (int index = 0; index < nbIndexes; index++) {
	    final int curTick = tickXPositions.get(index);
	    final String toDisplay = tickLabels.get(index);

	    g2d.drawLine(curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH, curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY);

	    int xPos;
	    if ((index == 0) && addOffsetToFirstLabel) {
		xPos = curTick;
	    } else if ((index == lastIndex) && subOffsetToLastLabel) {
		xPos = curTick - 25;
	    } else {
		xPos = curTick - 15;
	    }
	    g2d.drawString(toDisplay, xPos, JScoop3ChartCustomAspect.graphAreaXTickLabelY);
	}

	if (!JScoop3ChartCustomAspect.drawQuadrillage) {
	    tickXPositions.clear();
	}
    }

    /**
     * @param g2d
     * @param minAbscissaToDisplay
     * @deprecated
     */
    @Deprecated
    protected void paintColumnHeaderAbscissaIsNotRef(final Graphics2D g2d, final double minAbscissaToDisplay) {
	tickXPositions.clear();

	// Compute the precision
	String format;
	final double diffMAxMinAbscissa = jScoop3ScrollPane.computeMaxViewableAbscissa()
		- jScoop3ScrollPane.computeMinViewableAbscissa();
	if (diffMAxMinAbscissa < 3d) {
	    format = "%.3f";
	} else if (diffMAxMinAbscissa < 20d) {
	    format = "%.2f";
	} else if (diffMAxMinAbscissa < 100d) {
	    format = "%.1f";
	} else {
	    format = "%.0f";
	}

	// Abcissa tick
	g2d.setFont(new Font(null, Font.BOLD, 10));
	// Rotate String
	final AffineTransform atXLabelTick = new AffineTransform();
	atXLabelTick.setToRotation(Math.PI / 2.0);
	g2d.setFont(g2d.getFont().deriveFont(atXLabelTick));

	/*
	 * Compute Min and Max value for the "tick loop"
	 */
	final int nbTicksNeeded = (getPreferredSize().width / JScoop3ChartCustomAspect.GRAPHAREA_TICK_STEP);
	// nbTicksNeeded = (jScoop3ScrollPane.getDataAreaForZoomLevelOne().width / GRAPHAREA_TICK_STEP);
	final double maxMinusMinAbsissaPhysVal = jScoop3ScrollPane.getMaxAbscissaPhysVal()
		- jScoop3ScrollPane.getMinAbscissaPhysVal();
	final double minTick = (nbTicksNeeded * (jScoop3ScrollPane.computeMinViewableAbscissaForColumnHeader()
		- jScoop3ScrollPane.getMinAbscissaPhysVal())) / maxMinusMinAbsissaPhysVal;
	final double maxTick = (nbTicksNeeded * (jScoop3ScrollPane.computeMaxViewableAbscissaForColumnHeader()
		- jScoop3ScrollPane.getMinAbscissaPhysVal())) / maxMinusMinAbsissaPhysVal;

	final int diffForTicks = 5;
	final int minTickInt = Math.max(0, (int) Math.floor(minTick) - diffForTicks);
	// final int maxTickInt = Math.min(nbTicksNeeded, (int) Math.ceil(maxTick) + diffForTicks);
	final int maxTickInt = (int) Math.ceil(maxTick) + diffForTicks;

	String lastDateDisplayed = "";
	/*
	 * To avoid "infinite loop"
	 */
	final int maxTicks = 1000;
	int nbTicks = 0;
	final double abscissaToPixelXFactor = jScoop3ScrollPane.computeAbscissaToPixelXFactor();
	// for (int curTick = 0; (curTick < this.getPreferredSize().width) && (nbTicks < maxTicks); curTick +=
	// GRAPHAREA_TICK_STEP) {
	for (int curTickInt = minTickInt; (curTickInt < maxTickInt) && (nbTicks < maxTicks); curTickInt++) {
	    nbTicks++;
	    final int curTick = curTickInt * JScoop3ChartCustomAspect.GRAPHAREA_TICK_STEP;

	    if (JScoop3ChartCustomAspect.drawQuadrillage) {
		tickXPositions.add(curTick);
	    }

	    // FIXME
	    // g2d.drawLine(curTick, dataAreaMaxY - GRAPHAREA_TICK_LENGTH, curTick, dataAreaMaxY);
	    g2d.drawLine(curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH, curTick,
		    JScoop3ChartCustomAspect.graphAreaDataY);
	    if (JScoop3ChartScrollPaneAbstract.getCoefX() == 0) {
		String toDisplay;
		final double valueToDisplay = minAbscissaToDisplay + (curTick / abscissaToPixelXFactor);
		if (isAbscissaADate) {
		    final Date date = new Date((long) valueToDisplay);
		    final String dateStr = JScoop3ChartCustomAspect.DATE_SDF.format(date);
		    if (lastDateDisplayed.equals(dateStr)) {
			toDisplay = JScoop3ChartCustomAspect.HOUR_SDF.format(date);

		    } else {
			toDisplay = dateStr;
			lastDateDisplayed = dateStr;
		    }
		    g2d.drawString(toDisplay, curTick, JScoop3ChartCustomAspect.graphAreaXTickLabelYForDate);
		} else {
		    toDisplay = String.format(format, valueToDisplay);
		    g2d.drawString(toDisplay, curTick, JScoop3ChartCustomAspect.graphAreaXTickLabelY);
		}
	    }
	}
    }

    /**
     * @param g2d
     * @param minAbscissaToDisplay
     * @deprecated
     */
    @Deprecated
    protected void paintColumnHeaderAbscissaIsRef(final Graphics2D g2d, final double minAbscissaToDisplay) {
	tickXPositions.clear();

	final double realMinAbscissaToDisplay = Math.max(jScoop3ScrollPane.getRealMinAbscissaPhysVal(),
		minAbscissaToDisplay);
	final double realMaxAbscissaToDisplay = jScoop3ScrollPane.getRealMaxAbscissaPhysVal();

	final double minViewable = jScoop3ScrollPane.computeMinViewableAbscissaForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableAbscissaForColumnHeader();

	final long minTickValue = (long) Math.floor(Math.max(realMinAbscissaToDisplay, minViewable));
	final long maxTickValue = (long) Math.ceil(maxViewable);

	final long diffMinMaxTickValue = maxTickValue - minTickValue;

	// Print ticks 5% before and 5% after displayed values
	final long minTickValueToPrint = (long) Math
		.floor(Math.max(realMinAbscissaToDisplay, minTickValue - (diffMinMaxTickValue * 0.05d)));
	final long maxTickValueToPrint = (long) Math
		.ceil(Math.min(realMaxAbscissaToDisplay, maxTickValue + (diffMinMaxTickValue * 0.05d)));

	// 20 pixels between 2 ticks ...
	final int numberOfTicks = (image.getWidth() - JScoop3ChartCustomAspect.graphAreaDataX) / 20;
	final long stepBetweenTickValues = (diffMinMaxTickValue < numberOfTicks) ? 1
		: diffMinMaxTickValue / numberOfTicks;

	final double abscissaToPixelXFactor = jScoop3ScrollPane.computeAbscissaToPixelXFactor();

	if (JScoop3ChartScrollPaneAbstract.getCoefX() == 0) {
	    String lastDateDisplayed = "";

	    // Abcissa tick
	    g2d.setFont(new Font(null, Font.BOLD, 10));
	    final AffineTransform atXLabelTick = new AffineTransform();
	    atXLabelTick.setToRotation(Math.PI / 2.0);
	    g2d.setFont(g2d.getFont().deriveFont(atXLabelTick));

	    boolean printTick = true;
	    long curTickValue = minTickValueToPrint;
	    while (printTick) {
		printTick = false;

		final int curTick = (int) ((curTickValue - minAbscissaToDisplay) * abscissaToPixelXFactor);

		if (JScoop3ChartCustomAspect.drawQuadrillage) {
		    tickXPositions.add(curTick);
		}

		g2d.drawLine(curTick,
			JScoop3ChartCustomAspect.graphAreaDataY - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH,
			curTick, JScoop3ChartCustomAspect.graphAreaDataY);

		String toDisplay;
		final double valueToDisplay = curTickValue;
		if (isAbscissaADate) {
		    final Date date = new Date(curTickValue);
		    final String dateStr = JScoop3ChartCustomAspect.DATE_SDF.format(date);
		    if (lastDateDisplayed.equals(dateStr)) {
			toDisplay = JScoop3ChartCustomAspect.HOUR_SDF.format(date);
		    } else {
			toDisplay = dateStr;
			lastDateDisplayed = dateStr;
		    }

		    g2d.drawString(toDisplay, curTick, JScoop3ChartCustomAspect.graphAreaXTickLabelYForDate);
		} else {
		    toDisplay = String.valueOf((int) valueToDisplay);
		    g2d.drawString(toDisplay, curTick, JScoop3ChartCustomAspect.graphAreaXTickLabelY);
		}

		if (curTickValue <= maxTickValueToPrint) {
		    printTick = true;
		    curTickValue += stepBetweenTickValues;
		}
	    }
	}
    }

    /**
     * Dessine le composant
     *
     * @param g
     *            Environement graphique
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(final Graphics g) {
	final Rectangle2D newRect = jScoop3ScrollPane.computeDataAreaForZoomLevelOne();

	if (!jScoop3ScrollPane.getDataAreaForZoomLevelOne().equals(newRect)) {
	    jScoop3ScrollPane.setDataAreaForZoomLevelOne(newRect);

	    jScoop3ScrollPane.getColumnHeader().repaint();
	    jScoop3ScrollPane.getRowHeader().repaint();
	}

	computeBgImage(computeParameters.imageDimension, computeParameters.dataAreaForZoomLevelOne,
		computeParameters.dataAreaForZoomLevelCurrent, computeParameters.additionalGraphs);
	jScoop3ScrollPane.computeCurrentStationPoints(false);

	super.paintComponent(g);

	// Dessin de la station et de l'echantillon courant
	final Point imagePosition = jScoop3ScrollPane.getImagePosition(jScoop3ScrollPane.getImageDimension());

	/* Dessin de l'image de fond (Ensemble des stations + axes) */
	final Graphics2D g2d = (Graphics2D) g;
	g2d.setBackground(JScoop3ChartCustomAspect.backgroundColor);
	g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

	if (JScoop3ChartCustomAspect.drawQuadrillage) {
	    /* Dessin du quadrillage */
	    g2d.setColor(JScoop3ChartCustomAspect.quadrillageColor);
	    // g2d.setStroke(QUADRILLAGE_STROKE);
	    g2d.setStroke(JScoop3ChartCustomAspect.QUADRILLAGE_STROKE_DASHED);
	    if (!tickXPositions.isEmpty()) {
		// Draw each lines ...
		final int deltaX = (tickXPositions.get(tickXPositions.size() - 1) - tickXPositions.get(0)) / 2;
		int minX = tickXPositions.get(0) - deltaX;
		final int maxX = tickXPositions.get(tickXPositions.size() - 1) + deltaX;
		if (minX < 0) {
		    minX = -1;
		}
		// Draw rows ...
		for (final int tickYPosition : tickYPositions) {
		    g2d.drawLine(minX, tickYPosition, maxX, tickYPosition);
		}
	    }

	    if (!tickYPositions.isEmpty()) {
		final int deltaY;
		int minY;
		final int maxY;
		if (tickYPositions.get(0) < tickYPositions.get(tickYPositions.size() - 1)) {
		    deltaY = (tickYPositions.get(tickYPositions.size() - 1) - tickYPositions.get(0)) / 2;
		    minY = tickYPositions.get(0) - deltaY;
		    maxY = tickYPositions.get(tickYPositions.size() - 1) + deltaY;
		} else {
		    deltaY = (tickYPositions.get(0) - tickYPositions.get(tickYPositions.size() - 1)) / 2;
		    minY = tickYPositions.get(tickYPositions.size() - 1) - deltaY;
		    maxY = tickYPositions.get(0) + deltaY;
		}
		if (minY < 0) {
		    minY = -1;
		}
		// Draw columns ....
		for (final int tickXPosition : tickXPositions) {
		    g2d.drawLine(tickXPosition, minY, tickXPosition, maxY);
		}
	    }
	}

	g2d.drawImage(this.image, imagePosition.x, imagePosition.y, this);
	// g2d.drawImage(this.image, 0, 0, this);

	/* Dessin du profil courant */
	g2d.setStroke(JScoop3ChartCustomAspect.CURRENT_CURB_STROKE);
	int xFrom = Integer.MAX_VALUE;
	int yFrom = Integer.MAX_VALUE;
	int qcFrom = Integer.MAX_VALUE;
	int xTo = Integer.MAX_VALUE;
	int yTo = Integer.MAX_VALUE;
	int qcTo = Integer.MAX_VALUE;

	// Compute wished factor for Width
	final float wishedFactorForWidth = (float) (1
		/ (((float) jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getWidth())
			/ jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() / 2));
	// Compute wished factor for Heigth
	final float wishedFactorForHeigth = (float) (1
		/ (((float) jScoop3ScrollPane.getDataAreaForZoomLevelCurrent().getHeight())
			/ jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() / 2));

	// Keep the max factor
	final float wishedZoomFactor = Math.max(wishedFactorForWidth, wishedFactorForHeigth);

	g2d.setStroke(JScoop3ChartCustomAspect.CURRENT_CURB_STROKE);

	/* Dessin du profil courant dans le cas Mode Profils Superposés */
	int currentLevel = 0;
	for (int index = 0; index < jScoop3ScrollPane.getCurrentStationsPoints().size(); index++) {
	    int[] point = null;
	    try {
		point = jScoop3ScrollPane.getCurrentStationsPoints().get(index);
	    } catch (final IndexOutOfBoundsException e) {
		final UnhandledException exception = new UnhandledException(
			"index : " + index + " / jScoop3ScrollPane.getCurrentStationsPoints().size() : "
				+ jScoop3ScrollPane.getCurrentStationsPoints().size(),
			e);
	    }
	    if (point == null) {
		if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
			&& (currentLevel == (jScoop3ScrollPane.getCurrentLevel() + 1))
			&& (jScoop3ScrollPane.getCurrentStationsPoints().get(index - 1) != null)) {
		    // SC3Logger.LOGGER.debug("1385 :" + currentLevel + " [" + xFrom + ", " + yFrom + "]");
		    g2d.drawOval(xFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    yFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
		}
		currentLevel++;
		continue;
	    }
	    xTo = point[0];
	    yTo = point[1];
	    qcTo = jScoop3ScrollPane.getQCForCurrentStation(index);

	    // Draw line before points ...
	    if ((qcFrom != Integer.MAX_VALUE) && (qcTo < QCColor.QC_COLOR_MAP.size())) {
		g2d.setColor(QCColor.QC_COLOR_MAP.get(Math.max(qcFrom, qcTo)));
	    }

	    if (JScoop3ChartScrollPaneAbstract.isDisplayLine()) {
		if (JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
			&& JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()) {
		    if ((xFrom != Integer.MAX_VALUE) && (xTo != Integer.MAX_VALUE)) {
			g2d.setStroke(JScoop3ChartCustomAspect.CURRENT_CURB_STROKE);
			g2d.drawLine(xFrom, yFrom, xTo, yTo);
		    }
		} else {
		    if (((!JScoop3ChartScrollPaneAbstract.getQcToDisplay().isEmpty()
			    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(qcFrom)
			    && JScoop3ChartScrollPaneAbstract.getQcToDisplay().contains(qcTo))
			    || (!JScoop3ChartScrollPaneAbstract.getQcToExclude().isEmpty()
				    && !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(qcFrom)
				    && !JScoop3ChartScrollPaneAbstract.getQcToExclude().contains(qcTo)))
			    && ((xFrom != Integer.MAX_VALUE) && (xTo != Integer.MAX_VALUE))) {
			g2d.setStroke(JScoop3ChartCustomAspect.CURRENT_CURB_STROKE);
			g2d.drawLine(xFrom, yFrom, xTo, yTo);
		    }
		}
	    }

	    if (qcFrom != Integer.MAX_VALUE) {
		// Couleur du point
		if ((qcFrom < QCColor.QC_COLOR_MAP.size())) {
		    g2d.setColor(QCColor.QC_COLOR_MAP.get(qcFrom));
		} else {
		    g2d.setColor(Color.white);
		}
		if (JScoop3ChartScrollPaneAbstract.isDisplayPoints(qcFrom)) {
		    if ((wishedZoomFactor > JScoop3ChartCustomAspect.ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM)
			    && (jScoop3ScrollPane.isSuperposedMode())) {
			g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radiusZoom,
				yFrom - JScoop3ChartCustomAspect.radiusZoom, JScoop3ChartCustomAspect.diameterZoom,
				JScoop3ChartCustomAspect.diameterZoom);
		    } else {
			g2d.fillOval(xFrom - JScoop3ChartCustomAspect.radius, yFrom - JScoop3ChartCustomAspect.radius,
				JScoop3ChartCustomAspect.diameter, JScoop3ChartCustomAspect.diameter);
		    }
		}
		if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
			&& (currentLevel == (jScoop3ScrollPane.getCurrentLevel() + 1))
			&& (jScoop3ScrollPane.getCurrentStationsPoints().get(index - 1) != null)) {
		    // SC3Logger.LOGGER.debug("1385 :" + currentLevel + " [" + xFrom + ", " + yFrom + "]");
		    g2d.drawOval(xFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    yFrom - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
		}
	    } else {
		// Check the first point ...
		if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
			&& (currentLevel == (jScoop3ScrollPane.getCurrentLevel()))) {
		    // SC3Logger.LOGGER.debug("1395 :" + currentLevel + " [" + xTo + ", " + yTo + "]");
		    if ((qcTo < QCColor.QC_COLOR_MAP.size())) {
			g2d.setColor(QCColor.QC_COLOR_MAP.get(qcTo));
		    } else {
			g2d.setColor(Color.white);
		    }
		    g2d.drawOval(xTo - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    yTo - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
			    JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
		}
	    }

	    xFrom = xTo;
	    yFrom = yTo;

	    if ((qcTo < QCColor.QC_COLOR_MAP.size())) {
		qcFrom = qcTo;
	    }
	    currentLevel++;
	}

	// Couleur du point
	if ((qcTo < QCColor.QC_COLOR_MAP.size())) {
	    g2d.setColor(QCColor.QC_COLOR_MAP.get(qcTo));
	} else {
	    g2d.setColor(Color.white);
	}
	if (JScoop3ChartScrollPaneAbstract.isDisplayPoints(qcTo)) {
	    if ((wishedZoomFactor > JScoop3ChartCustomAspect.ZOOM_THRESHOLD_FOR_DIAMETER_ZOOM)
		    && (jScoop3ScrollPane.isSuperposedMode())) {
		g2d.fillOval(xTo - JScoop3ChartCustomAspect.radiusZoom, yTo - JScoop3ChartCustomAspect.radiusZoom,
			JScoop3ChartCustomAspect.diameterZoom, JScoop3ChartCustomAspect.diameterZoom);
	    } else {
		g2d.fillOval(xTo - JScoop3ChartCustomAspect.radius, yTo - JScoop3ChartCustomAspect.radius,
			JScoop3ChartCustomAspect.diameter, JScoop3ChartCustomAspect.diameter);
	    }
	}

	if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()
		&& (currentLevel == (jScoop3ScrollPane.getCurrentLevel() + 1))) {
	    // SC3Logger.LOGGER.debug("1435 :" + currentLevel + " [" + xTo + ", " + yTo + "]");
	    g2d.drawOval(xTo - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
		    yTo - JScoop3ChartCustomAspect.selectedLevelRadiusZoom,
		    JScoop3ChartCustomAspect.selectedLevelDiameterZoom,
		    JScoop3ChartCustomAspect.selectedLevelDiameterZoom);
	}

	/* Dessin du rectangle de zoom */
	if (jScoop3ScrollPane.isDrawZoomArea()) {
	    g2d.setColor(JScoop3ChartCustomAspect.ZOOMAREA_BORDER_COLOR);
	    try {
		g2d.drawRect(jScoop3ScrollPane.getSelectionAreaUpperLeft().x,
			jScoop3ScrollPane.getSelectionAreaUpperLeft().y,
			jScoop3ScrollPane.getSelectionAreaBottomRight().x
				- jScoop3ScrollPane.getSelectionAreaUpperLeft().x,
			jScoop3ScrollPane.getSelectionAreaBottomRight().y
				- jScoop3ScrollPane.getSelectionAreaUpperLeft().y);
	    } catch (final NullPointerException e) {
		final UnhandledException exception = new UnhandledException(
			"Dessin d'un rectangle, jScoop3ScrollPane.getSelectionAreaUpperLeft() : "
				+ jScoop3ScrollPane.getSelectionAreaUpperLeft()
				+ " / jScoop3ScrollPane.getSelectionAreaBottomRight() : "
				+ jScoop3ScrollPane.getSelectionAreaBottomRight() + " / alert1 : "
				+ jScoop3ScrollPane.getAlert1() + " / alert2 : " + jScoop3ScrollPane.getAlert2()
				+ " / alert3 : " + jScoop3ScrollPane.getAlert3(),
			e);
	    }
	}

    }

    /**
     * @param g2d
     * @param minAbscissaToDisplay
     */
    protected void paintRowHeaderOrdinateIsADate(final Graphics2D g2d) {
	tickYPositions.clear();

	if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
	    return;
	}

	final long[] coefs = new long[] { 6 * 30 * ONE_DAY_IN_MS, // 6 months
		2 * 30 * ONE_DAY_IN_MS, // 2 months
		30 * ONE_DAY_IN_MS, // 1 month
		7 * ONE_DAY_IN_MS, // 1 week
		2 * ONE_DAY_IN_MS, // 2 day
		ONE_DAY_IN_MS, // 1 day
		ONE_DAY_IN_MS / 2l, // 1/2 day
		ONE_DAY_IN_MS / 4l, // 1/4 day
		60 * 60 * 1000l, // 1 hour
		30 * 60 * 1000l, // 1/2 hour
		15 * 60 * 1000l, // 1/4 hour
		5 * 60 * 1000l, // 5 minutes
	};

	/*
	 * 0 means, first day of week. >0 means add month. <0 means : nothing to do
	 */
	final int[] firstDayOfMonthOrWeekIfCoefSelected = new int[] { 6, // 6 months
		2, // 2 months
		1, // 1 month
		0, // 1 week
		-1, // 2 days
		-1, // 1 day
		-1, // 1/2 day
		-1, // 1/4 day
		-1, // 1 hour
		-1, // 1/2 hour
		-1, // 1/4 hour
		-1, // 5 minutes
	};

	// If there is more than 150 PX between 2 values when using this coef, keep it
	final int pxLimitForCoefSearching = 150;

	final double minViewable = jScoop3ScrollPane.computeMinViewableOrdinateForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableOrdinateForColumnHeader();

	// Abcissa tick
	g2d.setFont(new Font(null, Font.BOLD, 10));

	/*
	 * Compute Min and Max value for the "tick loop"
	 */
	final boolean isDefaultView = ((jScoop3ScrollPane.getDataAreaForZoomLevelOne().getWidth() == jScoop3ScrollPane
		.getDataAreaForZoomLevelCurrent().getWidth())
		&& (jScoop3ScrollPane.getDataAreaForZoomLevelOne().getHeight() == jScoop3ScrollPane
			.getDataAreaForZoomLevelCurrent().getHeight()));

	// Compute the precision
	final long minTickValue = (long) minViewable;
	final long maxTickValue = (long) maxViewable;
	final long diffMinMaxTickValue = maxTickValue - minTickValue;

	final List<String> tickLabels = new ArrayList<>();
	final double ordonneeToPixelYFactor = jScoop3ScrollPane.computeOrdonneeToPixelYFactor();

	int coefIndex = -1;

	if (isDefaultView) {
	    // Detect coef for 5 values
	    for (int index = 0; index < coefs.length; index++) {
		final long currentCoef = coefs[index];
		final long fiveTimesCoef = 5l * currentCoef;
		if (diffMinMaxTickValue >= fiveTimesCoef) {
		    coefIndex = index;
		    break;
		}
	    }
	} else {
	    /*
	     * Search the best coef to use ... it depends on the difference in PX between 2 values
	     */
	    for (int index = 0; index < coefs.length; index++) {
		final long currentCoef = coefs[index];
		final long twoTimesCoef = 2l * currentCoef;
		if (diffMinMaxTickValue >= twoTimesCoef) {
		    final int firstTickPos = (int) Math
			    .round((minTickValue - jScoop3ScrollPane.getMinOrdinatePhysVal()) * ordonneeToPixelYFactor);
		    final int secondTickPos = (int) Math
			    .round(((minTickValue + currentCoef) - jScoop3ScrollPane.getMinOrdinatePhysVal())
				    * ordonneeToPixelYFactor);

		    final int currentDiff = secondTickPos - firstTickPos;

		    if (coefIndex == -1) {
			coefIndex = index;
		    } else if (currentDiff >= pxLimitForCoefSearching) {
			coefIndex = index;
		    } else {
			break;
		    }
		}
	    }
	}

	final boolean firstDayOfMonth = firstDayOfMonthOrWeekIfCoefSelected[coefIndex] > 0;
	final boolean firstDayOfWeek = firstDayOfMonthOrWeekIfCoefSelected[coefIndex] == 0;

	final Calendar calendarForCurrentTickValue = Conversions.getUTCCalendar();

	/*
	 * Compute ticks ...
	 */
	long currentTickValue = minTickValue;
	for (int index = 0; currentTickValue <= maxTickValue; index++) {
	    if (firstDayOfMonth) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue);
		calendarForCurrentTickValue.set(Calendar.DAY_OF_MONTH, 1);
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);
		if (index > 0) {
		    calendarForCurrentTickValue.add(Calendar.MONTH,
			    index * firstDayOfMonthOrWeekIfCoefSelected[coefIndex]);
		}

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
	    } else if (firstDayOfWeek) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue + (index * coefs[coefIndex]));
		calendarForCurrentTickValue.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
	    } else if (coefs[coefIndex] >= ONE_DAY_IN_MS) {
		calendarForCurrentTickValue.setTimeInMillis(minTickValue + (index * coefs[coefIndex]));
		calendarForCurrentTickValue.set(Calendar.HOUR_OF_DAY, 0);
		calendarForCurrentTickValue.set(Calendar.MINUTE, 0);
		calendarForCurrentTickValue.set(Calendar.SECOND, 0);
		calendarForCurrentTickValue.set(Calendar.MILLISECOND, 0);

		if ((coefs[coefIndex] == (2 * ONE_DAY_IN_MS))
			&& ((calendarForCurrentTickValue.get(Calendar.DAY_OF_MONTH) % 2) == 1)) {
		    // Only display odd days
		    calendarForCurrentTickValue.add(Calendar.DAY_OF_MONTH, 1);
		}

		currentTickValue = calendarForCurrentTickValue.getTimeInMillis();
	    } else {
		currentTickValue = minTickValue + (index * coefs[coefIndex]);
	    }
	    final int curTick = (int) Math
		    .round((currentTickValue - jScoop3ScrollPane.getMinOrdinatePhysVal()) * ordonneeToPixelYFactor);
	    tickYPositions.add(curTick);

	    final Date date = new Date(currentTickValue);
	    tickLabels.add(Conversions.formatDateAndHourMinSec(date));
	}

	/*
	 * Display ticks
	 */
	final int nbIndexes = tickYPositions.size();
	for (int index = 0; index < nbIndexes; index++) {
	    final int curTick = tickYPositions.get(index);
	    final String toDisplay = tickLabels.get(index);

	    g2d.drawLine(JScoop3ChartCustomAspect.graphAreaDataX - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH,
		    curTick, JScoop3ChartCustomAspect.graphAreaDataX, curTick);

	    g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelXForDate, curTick);
	}

	if (!JScoop3ChartCustomAspect.drawQuadrillage) {
	    tickYPositions.clear();
	}
    }

    /**
     * Paint the row header, and the ordinate variable is the reference parameter
     *
     * @param g2d
     */
    protected void paintRowHeaderOrdinateIsNotADate(final Graphics2D g2d) {
	tickYPositions.clear();

	final double[] coefs = new double[] { 5000d, 2000d, 1000d, 500d, 200d, 100d, 50d, 20d, 10d, 5d, 2d, 1d, 0.5d,
		0.2d, 0.1d, 0.05d, 0.02d, 0.01d };
	// If there is more than 30 PX between 2 values when using this coef, keep it
	final int pxLimitForCoefSearching = 30;

	// Abcissa tick
	g2d.setFont(new Font(null, Font.BOLD, 10));

	final double minViewable = jScoop3ScrollPane.computeMinViewableOrdinateForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableOrdinateForColumnHeader();

	final long minTickValue = (long) Math.floor(minViewable);
	final long maxTickValue = (long) Math.ceil(maxViewable);

	final long diffMinMaxTickValue = maxTickValue - minTickValue;

	final List<String> tickLabels = new ArrayList<>();
	final double ordonneeToPixelYFactor = jScoop3ScrollPane.computeOrdonneeToPixelYFactor();

	double firstTickValue = -1;
	double lastTickValue = -1;
	double tickStep = -1;

	/*
	 * Search the best coef to use ... it depends on the difference in PX between 2 values
	 */
	double bestCoef = Double.NEGATIVE_INFINITY;
	for (final double currentCoef : coefs) {
	    if (diffMinMaxTickValue >= currentCoef) {
		final int firstTickPos = (int) Math
			.round((minTickValue - jScoop3ScrollPane.getMinOrdinatePhysVal()) * ordonneeToPixelYFactor);
		final int secondTickPos = (int) Math
			.round(((minTickValue + currentCoef) - jScoop3ScrollPane.getMinOrdinatePhysVal())
				* ordonneeToPixelYFactor);

		final int currentDiff = secondTickPos - firstTickPos;

		if (bestCoef == Double.NEGATIVE_INFINITY) {
		    bestCoef = currentCoef;
		} else if (currentDiff >= pxLimitForCoefSearching) {
		    bestCoef = currentCoef;
		} else {
		    break;
		}
	    }
	}

	String format;
	if (bestCoef >= 1) {
	    firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
	    lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
	    format = "%.0f";
	} else if (bestCoef < 0.1d) {
	    firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
	    lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
	    format = "%.2f";
	} else {
	    firstTickValue = ((int) (minTickValue / bestCoef)) * bestCoef;
	    lastTickValue = ((int) (maxTickValue / bestCoef)) * bestCoef;
	    format = "%.1f";
	}
	tickStep = bestCoef;

	// Compute ticks
	String toDisplay;
	double currentTickValue = firstTickValue;
	for (int index = 0; currentTickValue < lastTickValue; index++) {
	    currentTickValue = firstTickValue + (index * tickStep);
	    final int curTick = (int) Math
		    .round((currentTickValue - jScoop3ScrollPane.getMinOrdinatePhysVal()) * ordonneeToPixelYFactor);

	    // Check if the origin is in the corner up/left or bottom/left
	    int curTickYPosition = (isProfile) ? curTick : (getPreferredSize().height - curTick);

	    // If PSAL/TEMP graph, reverse the Y axis
	    if ((getOrdinatePhysicalVar().getLabel().equals("TEMP")
		    && getAbscissaPhysicalVar().getLabel().equals("PSAL"))
		    || (getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")
			    && getAbscissaPhysicalVar().getLabel().equals("PSAL_ADJUSTED"))) {
		curTickYPosition = (getPreferredSize().height - curTick);
	    }

	    tickYPositions.add(curTickYPosition);

	    tickLabels.add(String.format(format, currentTickValue));
	}

	// Draw ticks
	final int nbIndexes = tickYPositions.size();
	for (int index = 0; index < nbIndexes; index++) {
	    final int curTick = tickYPositions.get(index);
	    toDisplay = tickLabels.get(index);

	    g2d.drawLine(JScoop3ChartCustomAspect.graphAreaDataX - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH,
		    curTick, JScoop3ChartCustomAspect.graphAreaDataX, curTick);

	    g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelX, curTick);
	}

	if (!JScoop3ChartCustomAspect.drawQuadrillage) {
	    tickYPositions.clear();
	}
    }

    /**
     * Paint the row header, and the ordinate variable is NOT the reference parameter
     *
     * @param g2d
     * @param minOrdinateToDisplay
     * @deprecated
     */
    @Deprecated
    protected void paintRowHeaderOrdinateIsNotRef(final Graphics2D g2d, final double minOrdinateToDisplay) {
	tickYPositions.clear();

	// Compute the precision
	String format;
	final double diffMAxMinOrdinate = jScoop3ScrollPane.computeMaxViewableOrdonnee()
		- jScoop3ScrollPane.computeMinViewableOrdonnee();
	if (diffMAxMinOrdinate < 3d) {
	    format = "%.3f";
	} else if (diffMAxMinOrdinate < 20d) {
	    format = "%.2f";
	} else if (diffMAxMinOrdinate < 1000d) {
	    format = "%.1f";
	} else {
	    format = "%.0f";
	}

	/*
	 * Compute Min and Max value for the "tick loop"
	 */
	final int nbTicksNeeded = (getPreferredSize().height / JScoop3ChartCustomAspect.GRAPHAREA_TICK_STEP);
	final double maxMinusMinOrdinatePhysVal = jScoop3ScrollPane.getMaxOrdinatePhysVal()
		- jScoop3ScrollPane.getMinOrdinatePhysVal();
	final double minTick = (nbTicksNeeded * (jScoop3ScrollPane.computeMinViewableOrdinateForColumnHeader()
		- jScoop3ScrollPane.getMinOrdinatePhysVal())) / maxMinusMinOrdinatePhysVal;
	final double maxTick = (nbTicksNeeded * (jScoop3ScrollPane.computeMaxViewableOrdinateForColumnHeader()
		- jScoop3ScrollPane.getMinOrdinatePhysVal())) / maxMinusMinOrdinatePhysVal;

	final int diffForTicks = 100;
	final int minTickInt = Math.max(0, (int) Math.floor(minTick) - diffForTicks);
	final int maxTickInt = Math.min(nbTicksNeeded, (int) Math.ceil(maxTick) + diffForTicks);

	final double ordonneeToPixelYFactor = jScoop3ScrollPane.computeOrdonneeToPixelYFactor();

	String lastDateDisplayed = "";
	/*
	 * To avoid "infinite loop"
	 */
	final int maxTicks = 1000;
	int nbTicks = 0;
	// Ordinate tick
	g2d.setFont(new Font(null, Font.BOLD, 10));
	// for (int curTick = 0; curTick < this.getPreferredSize().height; curTick += GRAPHAREA_TICK_STEP) {
	for (int curTickInt = minTickInt; (curTickInt < maxTickInt) && (nbTicks < maxTicks); curTickInt++) {
	    nbTicks++;
	    final int curTick = curTickInt * JScoop3ChartCustomAspect.GRAPHAREA_TICK_STEP;

	    final int curTickYPosition = (isProfile) ? curTick : (getPreferredSize().height - curTick);

	    if (JScoop3ChartCustomAspect.drawQuadrillage) {
		tickYPositions.add(curTickYPosition);
	    }

	    g2d.drawLine(JScoop3ChartCustomAspect.graphAreaDataX - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH,
		    curTickYPosition, JScoop3ChartCustomAspect.graphAreaDataX, curTickYPosition);
	    String toDisplay;
	    final double valueToDisplay = minOrdinateToDisplay + (curTick / ordonneeToPixelYFactor);
	    if (isOrdinateADate) {
		final Date date = new Date((long) valueToDisplay);
		final String dateStr = JScoop3ChartCustomAspect.DATE_SDF.format(date);
		if (lastDateDisplayed.equals(dateStr)) {
		    toDisplay = JScoop3ChartCustomAspect.HOUR_SDF.format(date);

		} else {
		    toDisplay = dateStr;
		    lastDateDisplayed = dateStr;
		}
		g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelXForDate, curTickYPosition);
	    } else {
		toDisplay = String.format(format, valueToDisplay);
		g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelX, curTickYPosition);
	    }
	}
    }

    /**
     * Paint the row header, and the ordinate variable is the reference parameter
     *
     * @param g2d
     * @param minOrdinateToDisplay
     * @deprecated
     */
    @Deprecated
    protected void paintRowHeaderOrdinateIsRef(final Graphics2D g2d, final double minOrdinateToDisplay) {
	tickYPositions.clear();

	// final double realMinOrdinateToDisplay = Math.max(jScoop3ScrollPane.getRealMinOrdinatePhysVal(),
	// minOrdinateToDisplay);
	// final double realMaxOrdinateToDisplay = jScoop3ScrollPane.getRealMaxOrdinatePhysVal();

	final double minViewable = jScoop3ScrollPane.computeMinViewableOrdinateForColumnHeader();
	final double maxViewable = jScoop3ScrollPane.computeMaxViewableOrdinateForColumnHeader();

	// final long minTickValue = (long) Math.floor(Math.max(realMinOrdinateToDisplay, minViewable));
	final long minTickValue = (long) Math.floor(minViewable);
	final long maxTickValue = (long) Math.ceil(maxViewable);

	final long diffMinMaxTickValue = maxTickValue - minTickValue;

	// Print ticks 5% before and 5% after displayed values
	// final long minTickValueToPrint = (long) Math.floor(Math.max(realMinOrdinateToDisplay, minTickValue
	// - (diffMinMaxTickValue * 0.05d)));
	// final long maxTickValueToPrint = (long) Math.ceil(Math.min(realMaxOrdinateToDisplay, maxTickValue
	// + (diffMinMaxTickValue * 0.05d)));
	final long minTickValueToPrint = (long) Math.floor(minTickValue - (diffMinMaxTickValue * 0.05d));
	final long maxTickValueToPrint = (long) Math.ceil(maxTickValue + (diffMinMaxTickValue * 0.05d));

	// 20 pixels between 2 ticks ...
	final long numberOfTicks = (image.getHeight() - JScoop3ChartCustomAspect.graphAreaDataY) / 20;
	final long stepBetweenTickValues = ((diffMinMaxTickValue < numberOfTicks) || (numberOfTicks == 0)) ? 1l
		: diffMinMaxTickValue / numberOfTicks;

	final double ordonneeToPixelYFactor = jScoop3ScrollPane.computeOrdonneeToPixelYFactor();

	String lastDateDisplayed = "";
	boolean printTick = numberOfTicks > 0;
	long curTickValue = minTickValueToPrint;
	while (printTick) {
	    printTick = false;

	    final int curTick = (int) ((curTickValue - minOrdinateToDisplay) * ordonneeToPixelYFactor);
	    final int curTickYPosition = (isProfile) ? curTick : (getPreferredSize().height - curTick);

	    if (JScoop3ChartCustomAspect.drawQuadrillage) {
		tickYPositions.add(curTickYPosition);
	    }

	    g2d.drawLine(JScoop3ChartCustomAspect.graphAreaDataX - JScoop3ChartCustomAspect.GRAPHAREA_TICK_LENGTH,
		    curTickYPosition, JScoop3ChartCustomAspect.graphAreaDataX, curTickYPosition);
	    String toDisplay;
	    final double valueToDisplay = curTickValue;
	    if (isOrdinateADate) {
		final Date date = new Date(curTickValue);
		final String dateStr = JScoop3ChartCustomAspect.DATE_SDF.format(date);
		if (lastDateDisplayed.equals(dateStr)) {
		    toDisplay = JScoop3ChartCustomAspect.HOUR_SDF.format(date);

		} else {
		    toDisplay = dateStr;
		    lastDateDisplayed = dateStr;
		}
		g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelXForDate, curTickYPosition);
	    } else {
		// Center text
		toDisplay = String.format("%1$4s", (int) valueToDisplay);
		g2d.drawString(toDisplay, JScoop3ChartCustomAspect.graphAreaYTickLabelX, curTickYPosition);
	    }

	    if (curTickValue <= maxTickValueToPrint) {
		printTick = true;
		curTickValue += stepBetweenTickValues;
	    }
	}
    }

    /**
     * Dessine l'axe des X
     *
     * @param g2d
     *            Contexte graphique de dessin
     */
    void paintColumnHeader(final Graphics2D g2d) {

	jScoop3ScrollPane.updateDataAreaForZoomLevelCurrent();

	// Couleur des axes
	g2d.setColor(Color.BLACK);

	if (jScoop3ScrollPane.getMinAbscissaPhysVal() != Float.MAX_VALUE) {
	    // if (jScoop3ScrollPane.getAbscissaPhysicalVar().isReferenceParameter()
	    // || jScoop3ScrollPane.getAbscissaPhysicalVar().getLabel().equals(CommonViewModel.MEASURE_NUMBER)) {
	    // paintColumnHeaderAbscissaIsRef(g2d, minAbscissaToDisplay);
	    // } else {
	    // paintColumnHeaderAbscissaIsNotRef(g2d, minAbscissaToDisplay);
	    // }

	    if (isAbscissaADate) {
		paintColumnHeaderAbscissaIsADate(g2d);
	    } else {
		paintColumnHeaderAbscissaIsNotADate(g2d);
	    }
	}
    }

    /**
     * Dessine l'axe des Y
     *
     * @param g2d
     *            Contexte graphique de dessin
     */
    void paintRowHeader(final Graphics2D g2d) {

	jScoop3ScrollPane.updateDataAreaForZoomLevelCurrent();

	double minOrdinateToDisplay;
	// double maxOrdinateToDisplay;

	minOrdinateToDisplay = jScoop3ScrollPane.getMinOrdinatePhysVal();
	// maxOrdinateToDisplay = jScoop3ScrollPane.getMaxOrdinatePhysVal();

	// SC3Logger.LOGGER.trace("paintRowHeader min max : {} {}", minOrdinateToDisplay, maxOrdinateToDisplay);

	if (minOrdinateToDisplay != Float.MAX_VALUE) {
	    // Couleur des axes
	    g2d.setColor(Color.BLACK);

	    // if (jScoop3ScrollPane.getOrdinatePhysicalVar().isReferenceParameter()) {
	    // paintRowHeaderOrdinateIsRef(g2d, minOrdinateToDisplay);
	    // } else {
	    // paintRowHeaderOrdinateIsNotRef(g2d, minOrdinateToDisplay);
	    // }

	    if (isOrdinateADate) {
		paintRowHeaderOrdinateIsADate(g2d);
	    } else {
		paintRowHeaderOrdinateIsNotADate(g2d);
	    }
	}
    }

    public AbstractChartScrollPane getJScoop3ScrollPane() {
	return this.jScoop3ScrollPane;
    }

    // private Integer findTheNearestDivisor(final int total, final int goal) {
    // int residual;
    // boolean flag = true;
    // final ArrayList<Integer> divisor = new ArrayList<Integer>();
    //
    // for (int i = 2; i <= (total / 2); i++) {
    // residual = total % i;
    //
    // // if the residual is 0, add the divisor in arraylist
    // if (residual == 0) {
    // divisor.add(i);
    // flag = false;
    // }
    // }
    // if (flag) {
    // return null;
    // } else {
    // // find the nearest divisor with goal integer
    // int distance = Math.abs(divisor.get(0) - goal);
    // int nearestDivisor = divisor.get(0);
    // for (final Integer div : divisor) {
    // final int cdistance = Math.abs(div - goal);
    // if (cdistance < distance) {
    // distance = cdistance;
    // nearestDivisor = div;
    // }
    // }
    // return nearestDivisor;
    // }
    // }
}