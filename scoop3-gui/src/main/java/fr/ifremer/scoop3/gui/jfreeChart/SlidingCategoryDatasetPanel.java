package fr.ifremer.scoop3.gui.jfreeChart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import org.jfree.ui.Layer;

import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.tools.ComputeSpeed;

public class SlidingCategoryDatasetPanel extends DemoPanel implements ChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -4438176180812092844L;

    /** A scrollbar to update the dataset value. */
    JScrollBar scroller;

    /** The dataset. */
    SlidingCategoryDataset dataset;

    /** The mapViewImpl. */
    MapViewImpl mapViewImpl;

    /** The colors of the bars to draw */
    static List<Color> barColors;

    /** There is a speed error or not ? */
    static AtomicBoolean atLeastOneSpeedError;

    static double minSpeed;

    static double maxSpeed;

    /**
     * Creates a new demo panel.
     */
    public SlidingCategoryDatasetPanel(final CategoryDataset dataset, final MapViewImpl mapViewImpl,
	    final List<Color> barColors, final AtomicBoolean atLeastOneSpeedError, final double minSpeed,
	    final double maxSpeed) {
	super(new BorderLayout());
	this.dataset = new SlidingCategoryDataset(dataset, 0, 20);
	this.mapViewImpl = mapViewImpl;
	SlidingCategoryDatasetPanel.barColors = barColors;
	SlidingCategoryDatasetPanel.atLeastOneSpeedError = atLeastOneSpeedError;
	SlidingCategoryDatasetPanel.minSpeed = minSpeed;
	SlidingCategoryDatasetPanel.maxSpeed = maxSpeed;

	// get data for diagrams
	final JFreeChart chart = createChart(this.dataset);
	addChart(chart);
	final ChartPanel cp1 = new ChartPanel(chart);

	cp1.addChartMouseListener(new ChartMouseListener() {
	    @Override
	    public void chartMouseClicked(final ChartMouseEvent event) {
		if (event.getEntity() instanceof CategoryItemEntity) {
		    final CategoryItemEntity entity = (CategoryItemEntity) event.getEntity();
		    final String observationId = entity.getColumnKey().toString();
		    mapViewImpl.setSelectedObservation(observationId);
		}
	    }

	    @Override
	    public void chartMouseMoved(final ChartMouseEvent arg0) {
		// Nothing to do here
	    }
	});

	cp1.setPreferredSize(new Dimension(400, 400));
	if (dataset.getColumnCount() < 20) {
	    // if there is less than 20 bars, create a scrollbar to avoid nullPointerException and hide it because we
	    // don't need it
	    this.scroller = new JScrollBar(SwingConstants.HORIZONTAL, 0, 10, 0, 20);
	    this.scroller.setVisible(false);
	} else {
	    final int maxScrollbar = dataset.getColumnCount() - 10;
	    this.scroller = new JScrollBar(SwingConstants.HORIZONTAL, 0, 10, 0, maxScrollbar);
	}
	add(cp1);
	this.scroller.getModel().addChangeListener(this);
	final JPanel scrollPanel = new JPanel(new BorderLayout());
	scrollPanel.add(this.scroller);
	scrollPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	scrollPanel.setBackground(Color.white);
	add(scrollPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset
     *            the dataset.
     *
     * @return The chart.
     */
    private static JFreeChart createChart(final CategoryDataset dataset) {

	// create the chart...
	final JFreeChart chart = ChartFactory.createBarChart(Messages.getMessage("gui.chart-speed.title"), // chart
													   // title
		Messages.getMessage("gui.chart-speed.x-label"), // x axis label
		Messages.getMessage("gui.chart-speed.y-label"), // y axis label
		dataset, // data
		PlotOrientation.VERTICAL, // orientation
		true, // include legend
		true, // tooltips?
		false // URLs?
	);

	final CategoryPlot categoryPlot = chart.getCategoryPlot();

	/*
	 * Set the bar colors
	 */
	final BarRenderer renderer = new BarRenderer() {
	    private static final long serialVersionUID = 1451816938486623813L;

	    Color[] colors = barColors.toArray(new Color[0]);

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
		return this.colors[column % this.colors.length];
	    }

	    @Override
	    protected void calculateBarWidth(final CategoryPlot plot, final Rectangle2D dataArea,
		    final int rendererIndex, final CategoryItemRendererState state) {
		// fixe la taille de toutes les barres à 30 pixels
		state.setBarWidth(30);
	    }
	};

	/*
	 * Add Platform speed limit if needed
	 */
	if ((ComputeSpeed.getPlatformSpeedLimit() != null) && atLeastOneSpeedError.get()) {
	    final ValueMarker speedMarker = new ValueMarker(ComputeSpeed.getPlatformSpeedLimit(), Color.BLACK,
		    new BasicStroke(1.0f));
	    categoryPlot.addRangeMarker(speedMarker, Layer.BACKGROUND);
	}

	chart.getLegend().setVisible(false);

	final CategoryPlot plot = (CategoryPlot) chart.getPlot();

	final CategoryAxis domainAxis = plot.getDomainAxis();
	domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);
	domainAxis.setLowerMargin(0.02);
	domainAxis.setUpperMargin(0.02);

	final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	// avoid a 0 range size
	if ((minSpeed == 0.0) && (maxSpeed == 0.0)) {
	    maxSpeed = 1.0;
	}
	// margin up and down the min / max
	rangeAxis.setRange(minSpeed < 0.0 ? minSpeed * 1.1 : 0.0, maxSpeed * 1.1);

	// disable bar outlines...
	final BarRenderer renderer2 = (BarRenderer) plot.getRenderer();
	renderer2.setDrawBarOutline(false);

	// Rotation de la legende de 90° afin qu'elle soit visible
	domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

	// hide the shadow
	renderer.setShadowVisible(false);
	categoryPlot.setRenderer(renderer);

	return chart;

    }

    /**
     * Handle a change in the slider by updating the dataset value. This automatically triggers a chart repaint.
     *
     * @param e
     *            the event.
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
	this.dataset.setFirstCategoryIndex(this.scroller.getValue());
    }

}
