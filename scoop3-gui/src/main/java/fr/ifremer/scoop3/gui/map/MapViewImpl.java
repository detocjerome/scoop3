package fr.ifremer.scoop3.gui.map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.bushe.swing.event.EventBus;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import fr.ifremer.scoop3.bathyClimato.BathyClimatologyManager;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.events.GuiEventChangeMainPanelToStep;
import fr.ifremer.scoop3.events.GuiEventRestoreBackupFileAndGoHome;
import fr.ifremer.scoop3.events.GuiEventStartStep;
import fr.ifremer.scoop3.events.GuiEventStepCompleted;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane;
import fr.ifremer.scoop3.gui.common.MetadataTable;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.jfreeChart.MyChartPanel;
import fr.ifremer.scoop3.gui.jfreeChart.SlidingCategoryDatasetPanel;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.LongValueAndQC;
import fr.ifremer.scoop3.tools.ComputeSpeed;

/**
 *
 *
 */
public class MapViewImpl extends CommonViewImpl {

    private ActionListener actionListenerForUpdatingQC;
    protected JButton closeButton;
    protected final List<CAErrorMessageItem> originalErrorMessages;
    private ChartPanel depthChartPanel = null;
    private JFreeChart depthChart = null;
    private Double bathy = null;
    private int obsSelectedIndex;
    private SlidingCategoryDatasetPanel slidingCategoryDatasetPanel;
    private ChartEditor editor = null;
    private double minSpeed = 0.0;
    private double maxSpeed = 0.0;

    /**
     * @param report
     *
     */
    public MapViewImpl(final Scoop3Frame scoop3Frame, final MetadataTable datasetMetadatasTable,
	    final MetadataTable observationMetadatasTable, final Dataset dataset, final Report report) {
	super(scoop3Frame, datasetMetadatasTable, observationMetadatasTable, dataset, report,
		STEP_TYPE.Q1_CONTROL_AUTO_METADATA);
	observationMetadatasTable.setMapViewImpl(this);

	originalErrorMessages = new ArrayList<>();

	for (final MessageItem messageItem : report.getStep(stepType).getMessages()) {
	    if (messageItem instanceof CAErrorMessageItem) {
		originalErrorMessages.add(((CAErrorMessageItem) messageItem).clone());
	    }
	}

	actionListenerForUpdatingQC = null;

	getMetadataSplitPane().requestFocusInWindow();
    }

    public MapViewImpl(final Scoop3Frame scoop3Frame, final MetadataSplitPane metadataSplitPane, final Dataset dataset,
	    final Report report) {
	super(scoop3Frame, metadataSplitPane, dataset, report, STEP_TYPE.Q1_CONTROL_AUTO_METADATA);
	metadataSplitPane.getObservationMetadatasTable().setMapViewImpl(this);
	metadataSplitPane.getDatasetMetadatasTable().setMapViewImpl(this);

	originalErrorMessages = new ArrayList<>();

	for (final MessageItem messageItem : report.getStep(stepType).getMessages()) {
	    if (messageItem instanceof CAErrorMessageItem) {
		originalErrorMessages.add(((CAErrorMessageItem) messageItem).clone());
	    }
	}

	actionListenerForUpdatingQC = null;

	getMetadataSplitPane().requestFocusInWindow();
    }

    /**
     * Add speedChart or depthChart to the view
     *
     * @return ChartPanel
     */
    public JComponent createChart() {

	JComponent toReturn;

	// Si TIMESERIE depthChart sinon SpeedChart
	if (dataset.getDatasetType() == DatasetType.TIMESERIE) {
	    /*
	     * Get the bathy ...
	     */
	    // Get the latitude and longitude from the first Observation
	    final Observation obs = dataset.getObservations().get(0);
	    if ((obs.getOceanDepth() != null)
		    && (obs.getOceanDepth().getValueAsDouble() != DoubleValueAndQC.DEFAULT_VALUE)) {
		bathy = obs.getOceanDepth().getValueAsDouble();
	    } else {
		final double latitude = obs.getFirstLatitudeClone().getValueAsDouble();
		final double longitude = obs.getFirstLongitudeClone().getValueAsDouble();

		final Short bathyFromService = BathyClimatologyManager.getSingleton()
			.getBestBathymetryWithoutException(latitude, longitude);
		bathy = (bathyFromService == null) ? null : -1d * bathyFromService;
	    }
	    depthChart = createDepthChart(dataset);
	    editor = ChartEditorManager.getChartEditor(depthChart);
	    depthChartPanel = new MyChartPanel(depthChart, editor, //
		    true, // popup menu - properties
		    true, // popup menu - save
		    true, // popup menu - print
		    false, // popup menu - zoom : disable
		    true); // popup menu - tooltip
	    // Disable zoom
	    depthChartPanel.setDomainZoomable(false);
	    depthChartPanel.setRangeZoomable(false);
	    depthChartPanel.setMouseZoomable(false);

	    /*
	     * Add mouse listener
	     */
	    depthChartPanel.addChartMouseListener(new ChartMouseListener() {
		@Override
		public void chartMouseClicked(final ChartMouseEvent event) {
		    if (event.getEntity() instanceof XYItemEntity) {
			final XYItemEntity entity = (XYItemEntity) event.getEntity();
			// ((XYSeriesCollection)entity.getDataset()).getSeries(0).getItems().get(entity.getItem())
			// Treat only clic on timeseries. (index == 1 if it is Bathy "layer")
			if (entity.getSeriesIndex() == 0) {
			    final String observationId = dataset.getObservations().get(entity.getItem()).getId();
			    setSelectedObservation(observationId);
			}
		    }
		}

		@Override
		public void chartMouseMoved(final ChartMouseEvent arg0) {
		    // Nothing to do here
		}
	    });

	    toReturn = depthChartPanel;
	} else {
	    final List<Color> barColors = new ArrayList<>();
	    final AtomicBoolean atLeastOneSpeedError = new AtomicBoolean(false);
	    final CategoryDataset speedDataset = createSpeedDataset(dataset.getObservations(), barColors,
		    atLeastOneSpeedError);

	    slidingCategoryDatasetPanel = new SlidingCategoryDatasetPanel(speedDataset, this, barColors,
		    atLeastOneSpeedError, minSpeed, maxSpeed);

	    toReturn = slidingCategoryDatasetPanel;
	}

	return toReturn;
    }

    /**
     * @param actionListenerForUpdatingQC
     *            the actionListenerForUpdatingQC to set
     */
    public void setActionListenerForUpdatingQC(final ActionListener actionListenerForUpdatingQC) {
	this.actionListenerForUpdatingQC = actionListenerForUpdatingQC;
    }

    /**
     * @param obsSelectedIndex
     */
    public void updateDepthChart(final int obsSelectedIndex) {
	if (depthChartPanel != null) {
	    if (obsSelectedIndex != -1) {
		this.obsSelectedIndex = obsSelectedIndex;
	    }
	    depthChart = createDepthChart(dataset);
	    depthChartPanel.setChart(depthChart);
	    editor.updateChart(depthChart);
	    // Disable zoom
	    depthChartPanel.setDomainZoomable(false);
	    depthChartPanel.setRangeZoomable(false);
	    depthChartPanel.setMouseZoomable(false);

	    depthChartPanel.validate();
	    depthChartPanel.repaint();
	}
    }

    /**
     */
    public void updateSpeedChart() {
	if (slidingCategoryDatasetPanel != null) {
	    slidingCategoryDatasetPanel = null;
	    final List<Color> barColors = new ArrayList<>();
	    final AtomicBoolean atLeastOneSpeedError = new AtomicBoolean(false);
	    final CategoryDataset speedDataset = createSpeedDataset(dataset.getObservations(), barColors,
		    atLeastOneSpeedError);

	    slidingCategoryDatasetPanel = new SlidingCategoryDatasetPanel(speedDataset, this, barColors,
		    atLeastOneSpeedError, minSpeed, maxSpeed);

	    slidingCategoryDatasetPanel.validate();
	    slidingCategoryDatasetPanel.repaint();
	}
    }

    /**
     * @param observationReference
     * @param newQCValue
     */
    public void updateQCFor(final Observation observation, final QCValues newQCValue) {
	// ActionEvent.getSource() is an ARRAY [(String) observationReference, (Integer) currentLevel, (Integer)
	// newQCValue]
	if (actionListenerForUpdatingQC != null) {
	    actionListenerForUpdatingQC.actionPerformed(new ActionEvent(new Object[] { observation.getReference(), -1,
		    newQCValue.getQCValue(), observation.getFirstLatitudeClone().getValueAsDouble(),
		    observation.getFirstLongitudeClone().getValueAsDouble() }, 0, null));

	    updateDepthChart(-1);
	    updateSpeedChart();
	}
    }

    public void updateRibbonButtons(final boolean isListOfUndoableChangesEmpty,
	    final boolean isListOfRedoableChangesEmpty) {
	undoButton.setEnabled(!isListOfUndoableChangesEmpty);
	undoAllButton.setEnabled(!isListOfUndoableChangesEmpty);
	redoButton.setEnabled(!isListOfRedoableChangesEmpty);
    }

    /**
     * depthChart creation
     *
     * @param dataset
     * @return JFreeChart
     */
    private JFreeChart createDepthChart(final Dataset dataset) {
	final XYDataset depthDataset = createDepthDataset(dataset.getObservations());
	// Creation du graphique de profondeur
	final JFreeChart localDepthChart = ChartFactory.createXYLineChart( //
		Messages.getMessage("gui.chart-depth.title"), // chart title
		Messages.getMessage("gui.chart-depth.x-label"), // x axis label
		Messages.getMessage("gui.chart-depth.y-label"), // y axis label
		depthDataset, // data
		PlotOrientation.HORIZONTAL, //
		true, // include legend
		true, // tooltips
		false // urls
	);

	final Color[] colors = new Color[dataset.getObservations().size()];
	/*
	 * Compute points colors
	 */
	int index = 0;
	for (final Observation observation : dataset.getObservations()) {
	    colors[index] = observation.getWorstQCExcept9().getColor();
	    index++;
	}

	// Axe en haut du graphe
	localDepthChart.getXYPlot().setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
	localDepthChart.getLegend().setVisible(false);

	// Renderer pour l'affichage des points/lignes
	final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
	    private static final long serialVersionUID = -589112340686994681L;
	    private final Color brownColor = new Color(75, 40, 3);

	    /**
	     * Returns the paint for an item. Overrides the default behavior inherited from AbstractSeriesRenderer.
	     *
	     * @param row
	     *            the series.
	     * @param column
	     *            the category.
	     *
	     * @return The item color.
	     */
	    @Override
	    public Paint getItemPaint(final int row, final int column) {
		// Bathy layer if paint in Brown
		if (row == 1) {
		    return brownColor;
		}
		return colors[column % colors.length];
	    }
	};
	renderer.setSeriesLinesVisible(0, false);
	// renderer.setSeriesShapesVisible(1, false);
	// renderer.setSeriesPaint(0, Color.GREEN);

	// Pour modifier le style des points
	final Shape diamond = ShapeUtilities.createDiamond(8);
	renderer.setSeriesShape(0, diamond);
	// Render for bathy is a rectangle
	renderer.setSeriesShape(1, new Rectangle(new Dimension(getScoop3Frame().getWidth() + 50, 50)));

	// Definition du renderer
	final XYPlot plot = localDepthChart.getXYPlot();
	plot.setRenderer(renderer);

	/*
	 * Add the vertical line
	 */
	final ValueMarker m1 = new ValueMarker(1, Color.BLACK, new BasicStroke(1.0f));
	// Draw in Range as the graph is horizontal
	plot.addRangeMarker(m1, Layer.BACKGROUND);

	// inversion de l'axe des X (effet de profondeur)
	final ValueAxis xaxis = plot.getDomainAxis();
	xaxis.setInverted(true);
	xaxis.setLowerBound(0);
	xaxis.setVisible(false);

	// Masque l'axe du haut (Y)
	final ValueAxis yaxis = plot.getRangeAxis();
	yaxis.setRange(0.9, 1.1);
	yaxis.setVisible(false);

	/*
	 * Add annotations
	 */
	XYTextAnnotation annotation = null;
	final Font font = new Font("Calibri", Font.PLAIN, 16);

	index = 0;
	for (final Observation observation : dataset.getObservations()) {
	    final double value = (observation.getSensor().getNominalDepth() == null)
		    ? observation.getZ().getValues().get(0)
		    : observation.getSensor().getNominalDepth().getValueAsDouble();
	    annotation = new XYTextAnnotation(String.valueOf(value) + " m", value, 0.98);
	    annotation.setFont(font);
	    annotation.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
	    plot.addAnnotation(annotation);

	    annotation = new XYTextAnnotation(observation.getId(), value, 1.02);
	    annotation.setFont(font);
	    annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
	    plot.addAnnotation(annotation);

	    // Add circle around selected observation
	    if (index == obsSelectedIndex) {
		final double radiusX;
		if (bathy != null) {
		    radiusX = bathy / 70;
		} else {
		    radiusX = 1;
		}
		final double radiusY = 0.01;
		final Ellipse2D.Double arc = new Ellipse2D.Double(value - radiusX, 1 - radiusY, radiusX + radiusX,
			radiusY + radiusY);
		plot.addAnnotation(new XYShapeAnnotation(arc, new BasicStroke(2.0f), Color.BLACK));
	    }
	    index++;
	}

	if (bathy != null) {
	    annotation = new XYTextAnnotation(Messages.getMessage("gui.chart-depth") + " : " + bathy + " m", bathy, 1);
	    annotation.setPaint(Color.WHITE);
	    annotation.setFont(font);
	    annotation.setTextAnchor(TextAnchor.TOP_CENTER);
	    plot.addAnnotation(annotation);
	}

	plot.setBackgroundPaint(Color.WHITE);

	return localDepthChart;
    }

    /**
     * Implementation des valeurs du graphes de profondeur en fonction du model
     *
     * @param obsList
     *            liste d'observations
     * @return XYDataset
     */
    private XYDataset createDepthDataset(final List<Observation> obsList) {

	final XYSeries series = new XYSeries("Profondeur", false); // DO NOT AUTOSORT

	// pour chaque observation ajout de la profondeur
	for (final Observation obs : obsList) {
	    try {
		series.add((double) obs.getSensor().getNominalDepth().getValueAsDouble(), 1.0);
	    } catch (final NullPointerException npe) {
		SC3Logger.LOGGER.error(npe.getMessage(), npe);
	    }
	}

	final XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);

	if (bathy != null) {
	    final XYSeries seriesBathy = new XYSeries("Bathy");
	    seriesBathy.add(bathy.doubleValue(), 0.9);
	    // Add a 2nd point to be sure to display correctly the bathy annotation
	    double otherPoint = bathy.doubleValue();
	    if (bathy.doubleValue() < 2000) {
		otherPoint *= 1.06d;
	    } else if (bathy.doubleValue() < 4000) {
		otherPoint *= 1.03d;
	    }
	    seriesBathy.add(otherPoint, 0.9);
	    dataset.addSeries(seriesBathy);
	}

	return dataset;
    }

    /**
     * Implementation des valeurs du graphes de vitesse en fonction du model
     *
     * @param obsList
     *            liste d'observations
     * @param atLeastOneSpeedError
     * @return CategoryDataset
     */
    private CategoryDataset createSpeedDataset(final List<Observation> obsList, final List<Color> barColors,
	    final AtomicBoolean atLeastOneSpeedError) {
	// row keys...
	final String series_qc_ok = "OK";
	// create the dataset...
	final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	if (this.dataset.getDatasetType() == DatasetType.PROFILE) {
	    // ne tracer le graphique de vitesse des profils qu'avec les observations ayant un TIME != 9999
	    final ArrayList<Observation> timeKnownObservations = new ArrayList<Observation>();
	    for (int i = 0; i < obsList.size(); i++) {
		if (!obsList.get(i).isTimeUnknown()) {
		    timeKnownObservations.add(obsList.get(i));
		}
	    }
	    // pour chaque observation ajout de la vitesse
	    for (int i = 1; i < timeKnownObservations.size(); i++) {
		final String station_Name = timeKnownObservations.get(i).getReference().substring(
			timeKnownObservations.get(i).getReference().length() - 5,
			timeKnownObservations.get(i).getReference().length());

		final double speed = computeSpeedForObservations(timeKnownObservations.get(i - 1),
			timeKnownObservations.get(i));
		minSpeed = Math.min(minSpeed, speed);
		maxSpeed = Math.max(maxSpeed, speed);
		dataset.addValue(speed, series_qc_ok, station_Name);

		if (ComputeSpeed.getPlatformSpeedLimit() != null) {
		    if (speed <= ComputeSpeed.getPlatformSpeedLimit()) {
			barColors.add(QCValues.QC_1.getColor());
		    } else {
			atLeastOneSpeedError.set(true);
			barColors.add(QCValues.QC_4.getColor());
		    }
		}
	    }
	} else if (this.dataset.getDatasetType() == DatasetType.TRAJECTORY) {
	    for (int i = 1; i < obsList.get(0).getLatitude().getValues().size(); i++) {

		final String station_Name = String.format("%05d", i);

		final double speed = ComputeSpeed.computeCurrentSpeed(obsList.get(0),
			new LongValueAndQC(obsList.get(0).getTime().getValues().get(i - 1),
				obsList.get(0).getTime().getQcValues().get(i - 1)),
			new DoubleValueAndQC(obsList.get(0).getLatitude().getValues().get(i - 1),
				obsList.get(0).getLatitude().getQcValues().get(i - 1)),
			new DoubleValueAndQC(obsList.get(0).getLongitude().getValues().get(i - 1),
				obsList.get(0).getLongitude().getQcValues().get(i - 1)),
			obsList.get(0),
			new LongValueAndQC(obsList.get(0).getTime().getValues().get(i),
				obsList.get(0).getTime().getQcValues().get(i)),
			new DoubleValueAndQC(obsList.get(0).getLatitude().getValues().get(i),
				obsList.get(0).getLatitude().getQcValues().get(i)),
			new DoubleValueAndQC(obsList.get(0).getLongitude().getValues().get(i),
				obsList.get(0).getLongitude().getQcValues().get(i)));
		minSpeed = Math.min(minSpeed, speed);
		maxSpeed = Math.max(maxSpeed, speed);
		dataset.addValue(speed, series_qc_ok, station_Name);

		if (ComputeSpeed.getPlatformSpeedLimit() != null) {
		    if (speed <= ComputeSpeed.getPlatformSpeedLimit()) {
			barColors.add(QCValues.QC_1.getColor());
		    } else {
			atLeastOneSpeedError.set(true);
			barColors.add(QCValues.QC_4.getColor());
		    }
		}
	    }
	}

	if (ComputeSpeed.getPlatformSpeedLimit() == null) {
	    barColors.add(QCValues.QC_0.getColor());
	}

	return dataset;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#cancelButtonClicked()
     */
    @Override
    protected void cancelButtonClicked() {
	revertCommentUpdates();

	if (metadataSplitPane.backupUpdates(false)) {
	    // If there is at least one update, restore the file
	    EventBus.publish(new GuiEventRestoreBackupFileAndGoHome());
	} else {
	    // Else, just go Home
	    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
	}
	ReportJDialog.disposeIfExists();
    }

    /**
     * @param obs1
     * @param obs2
     * @return
     */
    protected double computeSpeedForObservations(final Observation obs1, final Observation obs2) {
	return ComputeSpeed.computeCurrentSpeed(obs1, obs1.getFirstDateTimeClone(), obs1.getFirstLatitudeClone(),
		obs1.getFirstLongitudeClone(), obs2, obs2.getFirstDateTimeClone(), obs2.getFirstLatitudeClone(),
		obs2.getFirstLongitudeClone());
    }

    /**
     * A ribbon band is a sort a large toolbar
     */
    @Override
    protected void createRibbonBands() {
	// Map tab
	final RibbonTask mapRibbonTask = new RibbonTask(Messages.getMessage("bpc-gui.ribbon-map"), getMapRibbonBand(),
		getUndoRedoRibbonBand(), getReportActionBand());

	// Add Task to the Ribbon
	// try {
	// SwingUtilities.invokeAndWait(new Runnable() {
	// @Override
	// public void run() {
	getScoop3Frame().getRibbon().addTask(mapRibbonTask);
	// }
	// });
	// } catch (InvocationTargetException | InterruptedException e) {
	// SC3Logger.LOGGER.error(e.getMessage(), e);
	// }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#getButtonsPanel()
     */
    @Override
    protected JPanel getButtonsPanel() {
	final JPanel buttonsPanel = super.getButtonsPanel();

	closeButton = new JButton(Messages.getMessage("bpc-gui.button-close"));
	buttonsPanel.add(closeButton);
	closeButton.addActionListener((final ActionEvent e) -> {
	    final int result = Dialogs.showConfirmDialog(getScoop3Frame(),
		    Messages.getMessage("bpc-gui.button-close-confirm-title"),
		    Messages.getMessage("bpc-gui.button-close-confirm-message"));
	    if (result == JOptionPane.YES_OPTION) {
		// Backup eventual update
		if (metadataSplitPane.backupUpdates(false)) {
		    // If there is at least one update : Set State as Q0 to force the launch of the Automatic
		    // Controls when opening the MapView next time.
		    EventBus.publish(new GuiEventStepCompleted(StepCode.QC0));
		} else {
		    // Do nothing
		    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
		}
	    } else {
		revertCommentUpdates();

		if (metadataSplitPane.backupUpdates(false)) {
		    // If there is at least one update, restore the file
		    EventBus.publish(new GuiEventRestoreBackupFileAndGoHome());
		} else {
		    // Else, just go Home
		    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
		}
	    }
	    ReportJDialog.disposeIfExists();
	});

	return buttonsPanel;
    }

    /**
     *
     */
    protected void revertCommentUpdates() {
	for (final CAErrorMessageItem caErrorMessageItemORI : originalErrorMessages) {
	    for (final MessageItem messageItem : report.getStep(stepType).getMessages()) {
		if (messageItem instanceof CAErrorMessageItem) {
		    final CAErrorMessageItem caErrorMessageItem = (CAErrorMessageItem) messageItem;
		    if (caErrorMessageItem.getDetails().equals(caErrorMessageItemORI.getDetails())
			    && ((caErrorMessageItem.getObs1Id() == caErrorMessageItemORI.getObs1Id())
				    || caErrorMessageItem.getObs1Id().equals(caErrorMessageItemORI.getObs1Id()))
			    && ((caErrorMessageItem.getObs2Id() == caErrorMessageItemORI.getObs2Id())
				    || caErrorMessageItem.getObs2Id().equals(caErrorMessageItemORI.getObs2Id()))) {
			caErrorMessageItem.setComment(caErrorMessageItemORI.getComment());
		    }
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	originalErrorMessages.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#validateButtonClicked()
     */
    @Override
    protected void validateButtonClicked() {
	// First, call backupUpdates on the MetadataSplitPane
	if (metadataSplitPane.backupUpdates(false)) {
	    // At least one update done. Use of StartStepQ12 to launch again the Automatic Control.
	    EventBus.publish(new GuiEventStartStep(StepCode.QC12));
	} else if (isThereAtLeastOneBlockingError() || metadataSplitPane.containsAtLeastOneMetadataWithQC4()) {
	    // Go to Home view without doing anything more
	    // /!\ Il reste des erreurs bloquantes
	    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
	} else {
	    // No update, but no more QC_4 => Q12 is complete
	    EventBus.publish(new GuiEventStepCompleted(StepCode.QC12));
	}
	ReportJDialog.disposeIfExists();
    }

    @Override
    public void updateAllProfilesIcons(final boolean allProfileIsActive) {
	// empty method
    }

    @Override
    protected void createWestSplitPane() {
	westPanel = new JPanel();
	westPanel.setLayout(new BorderLayout());
	westPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	westPanel.add(this.metadataSplitPane, BorderLayout.CENTER);
    }

    @Override
    protected void createMainSplitPane() {
	mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, eastPanel);

	// map and meta-data split pane
	mainSplitPane.setDividerSize(SPLITPANE_DIVIDERSIZE);
	// Add arrows (left and right) in the divider
	mainSplitPane.setOneTouchExpandable(true);
	mainSplitPane.setDividerLocation(WESTPANEL_DEFAULT_WIDTH);

    }

    public boolean toggleFullScreenForMap() {
	final int currentLocationForMainJSplitPane = getMainJSplitPane().getDividerLocation();
	final int currentLocationForCenterJSplitPane = getCenterSplitPane().getDividerLocation();
	if ((currentLocationForMainJSplitPane > 1) || (currentLocationForCenterJSplitPane < 1)) {
	    // MAXIMIZE
	    getMainJSplitPane().setDividerLocation(0);
	    getMainJSplitPane().setLastDividerLocation(currentLocationForMainJSplitPane);

	    getCenterSplitPane().setDividerLocation(1d);
	    getCenterSplitPane().setLastDividerLocation(currentLocationForCenterJSplitPane);

	    return true;
	} else {
	    getMainJSplitPane().setDividerLocation(getMainJSplitPane().getLastDividerLocation());
	    getMainJSplitPane().setLastDividerLocation(-1);

	    getCenterSplitPane().setDividerLocation(getCenterSplitPane().getLastDividerLocation());
	    getCenterSplitPane().setLastDividerLocation(-1);

	    return false;
	}
    }
}
