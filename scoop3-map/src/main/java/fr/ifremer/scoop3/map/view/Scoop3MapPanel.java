// **********************************************************************
//
// <copyright>
//
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************

package fr.ifremer.scoop3.map.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.event.PanMouseMode;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.EmbeddedScaleDisplayPanel;
import com.bbn.openmap.layer.imageTile.MapTileLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.plugin.wms.WMSPlugIn;
import com.bbn.openmap.tools.drawing.EditToolLoader;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.drawing.OMDrawingToolMouseMode;
import com.bbn.openmap.tools.drawing.OMRectLoader;
import com.bbn.openmap.util.PropUtils;

import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.map.event.ObservationMapBeanKeyListener;
import fr.ifremer.scoop3.map.layer.ObservationLayer;
import fr.ifremer.scoop3.map.layer.Scoop3GraticuleLayer;
import fr.ifremer.scoop3.map.tools.CommonFunctions;

/**
 * An extension of the BasicMapPanel that uses an OverlayLayout on the panel in the BorderLayout.CENTER position.
 * Contains a transparent widgets JPanel for placing components floating on top of the map. The default implementation
 * of layoutPanel() adds an EmbeddedNavPanel in the upper left position of the map, as well as a ProjectionStack for it
 * to use.
 * <p>
 * If a property prefix is set on this MapPanel, that property prefix can be used to designate MapPanelChild objects for
 * this MapPanel. The setName variable should be set to true, and the children's parent name should match whatever
 * property prefix is given to the panel.
 */
public class Scoop3MapPanel extends BasicMapPanel implements PropertyChangeListener, MouseListener {

    /**
     *
     */
    private static final long serialVersionUID = 6400919362954486915L;

    public static final String ACTIVE_WIDGET_COLOR_PROPERTY = "activeWidgets";
    public static final String INACTIVE_WIDGET_COLOR_PROPERTY = "inactiveWidgets";
    public static final String WIDGET_SIZE_PROPERTY = "widgetSize";
    protected static final int DEFAULT_WIDGET_BUTTON_SIZE = 15;

    private PropertyChangeSupport changes;

    /**
     * May be null, in which case the widgets should decide.
     */
    protected DrawingAttributes activeWidgetColors;
    /**
     * May be null, in which case the widgets should decide.
     */
    protected DrawingAttributes inactiveWidgetColors;
    /**
     * Defaults to 15;
     */
    protected int widgetButtonSize = DEFAULT_WIDGET_BUTTON_SIZE;

    /**
     * A transparent JPanel with a border layout, residing on top of the MapBean.
     */
    protected JPanel widgets;

    private JPanel centerContainer;

    private ObservationLayer observationLayer;

    private MouseDelegator mouseDelagator;

    private int minWidth = MapBean.DEFAULT_WIDTH;

    private int minHeight = MapBean.DEFAULT_HEIGHT;

    private JLabel latLonBathyLabel;
    /**
     * Found in the findAndInit() method, in the MapHandler.
     */
    protected GraphicAttributes filterGA = null;

    /**
     * Creates an empty OverlayMapPanel that creates its own empty PropertyHandler. The MapPanel will contain a MapBean,
     * a MapHandler, EmbeddedNavPanel and a PropertyHandler with no properties. The constructor to use to create a blank
     * map framework to add components to.
     */
    public Scoop3MapPanel() {
	super(new PropertyHandler(new Properties()), false);
	addMouseListener(this);
    }

    /**
     * Create a OverlayMapPanel with the option of delaying the search for properties until the <code>create()</code>
     * call is made.
     *
     * @param delayCreation
     *            true to let the MapPanel know that the artful programmer will call <code>create()</code>
     */
    public Scoop3MapPanel(final boolean delayCreation) {
	super(null, delayCreation);
	addMouseListener(this);
    }

    /**
     * Create a OverlayMapPanel that configures itself with the properties contained in the PropertyHandler provided. If
     * the PropertyHandler is null, a new one will be created.
     */
    public Scoop3MapPanel(final PropertyHandler propertyHandler) {
	super(propertyHandler, false);
	addMouseListener(this);
    }

    /**
     * Create a OverlayMapPanel that configures itself with properties contained in the PropertyHandler provided, and
     * with the option of delaying the search for properties until the <code>create()</code> call is made.
     *
     * @param delayCreation
     *            true to let the MapPanel know that the artful programmer will call <code>create()</code>
     */
    public Scoop3MapPanel(final PropertyHandler propertyHandler, final boolean delayCreation) {
	super(propertyHandler, delayCreation);
	addMouseListener(this);
    }

    /**
     * Create a OverlayMapPanel that configures itself with properties contained in the PropertyHandler provided, and
     * with the option of delaying the search for properties until the <code>create()</code> call is made.
     *
     * @param delayCreation
     *            true to let the MapPanel know that the artful programmer will call <code>create()</code>
     */
    public Scoop3MapPanel(final PropertyHandler propertyHandler, final boolean delayCreation, final int minWidth,
	    final int minHeight) {
	this(propertyHandler, delayCreation);
	this.minWidth = minWidth;
	this.minHeight = minHeight;
	addMouseListener(this);
    }

    /**
     * Calls layoutPanel(MapBean), which configures the panel.
     */
    @Override
    protected void addMapBeanToPanel(final MapBean map) {
	layoutPanel(map);
	map.addPropertyChangeListener(this);
    }

    public DrawingAttributes getActiveWidgetColors() {
	return activeWidgetColors;
    }

    public void setActiveWidgetColors(final DrawingAttributes activeWidgetColors) {
	this.activeWidgetColors = activeWidgetColors;
    }

    public DrawingAttributes getInactiveWidgetColors() {
	return inactiveWidgetColors;
    }

    public void setInactiveWidgetColors(final DrawingAttributes inactiveWidgetColors) {
	this.inactiveWidgetColors = inactiveWidgetColors;
    }

    public int getWidgetButtonSize() {
	return widgetButtonSize;
    }

    public void setWidgetButtonSize(final int widgetButtonSize) {
	this.widgetButtonSize = widgetButtonSize;
    }

    /**
     * New method added, called from addMapBeanToPanel(MapBean).
     *
     * @param map
     */
    protected void layoutPanel(final MapBean map) {
	final Dimension minimumSize = new Dimension(minWidth, minHeight);

	final JPanel hackPanel = new JPanel();
	hackPanel.setLayout(new BorderLayout());
	hackPanel.setOpaque(false);
	hackPanel.add(map, BorderLayout.CENTER);

	centerContainer = new JPanel();

	centerContainer.setLayout(new OverlayLayout(centerContainer));

	// These may be null, but the EmbeddedNavPanel will choose it's own
	// default colors if that is so.
	final DrawingAttributes curActiveWidgetColors = getActiveWidgetColors();
	final DrawingAttributes curInactiveWidgetColors = getInactiveWidgetColors();
	final int curWidgetButtonSize = getWidgetButtonSize();

	final EmbeddedNavPanel navPanel = new EmbeddedNavPanel(curActiveWidgetColors, curInactiveWidgetColors,
		curWidgetButtonSize);
	navPanel.setBounds(12, 12, navPanel.getMinimumSize().width, navPanel.getMinimumSize().height);

	navPanel.addPropertyChangeListener((final PropertyChangeEvent evt) -> {
	    SC3Logger.LOGGER.trace("Scoop3MapPanel - navPanel - propertyChangeEvent");
	    if ("SELECT_NEXT_OBSERVATION_MAP".equals(evt.getPropertyName())) {
		observationLayer.selectNextOMPoint();
	    }
	    if ("SELECT_PREVIOUS_OBSERVATION_MAP".equals(evt.getPropertyName())) {
		observationLayer.selectPreviousOMPoint();
	    }
	    if ("TOGGLE_FULL_SCREEN_FOR_MAP".equals(evt.getPropertyName())) {
		observationLayer.toggleFullScreen();
	    }
	});

	addMapComponent(navPanel);

	final EmbeddedScaleDisplayPanel scaleDisplay = new EmbeddedScaleDisplayPanel();
	addMapComponent(scaleDisplay);

	widgets = new JPanel();
	widgets.setLayout(new BorderLayout());
	widgets.setBackground(OMGraphicConstants.clear);
	widgets.setOpaque(false);
	widgets.setBounds(0, 0, map.getWidth(), map.getHeight());
	widgets.setMinimumSize(minimumSize);
	widgets.add(navPanel, BorderLayout.WEST);
	widgets.add(scaleDisplay, BorderLayout.EAST);

	latLonBathyLabel = new JLabel(" ", JLabel.CENTER);
	widgets.add(latLonBathyLabel, BorderLayout.NORTH);

	setBorders(map, widgets);

	centerContainer.add(widgets);
	centerContainer.add(hackPanel);

	add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * If you want different borders or color them differently, override this method.
     *
     * @param map
     * @param widgets
     */
    protected void setBorders(final MapBean map, final JPanel widgets) {

	if (map != null) {
	    map.setBorder(null);
	}

	if (widgets != null) {
	    widgets.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.GRAY, Color.DARK_GRAY));
	}
    }

    /** Include exit in the File menu. Call this before create(). */
    public void includeExitMenuItem() {
	addProperty("quitMenu.class", "com.bbn.openmap.gui.map.QuitMenuItem");
	appendProperty("fileMenu.items", "quitMenu");
    }

    @Override
    public void setProperties(final String prefix, final Properties props) {
	super.setProperties(prefix, props);
	final String curPrefix = PropUtils.getScopedPropertyPrefix(prefix);

	DrawingAttributes awc = getActiveWidgetColors();
	if (awc == null) {
	    awc = DrawingAttributes.getDefaultClone();
	}
	DrawingAttributes iwc = getInactiveWidgetColors();
	if (iwc == null) {
	    iwc = DrawingAttributes.getDefaultClone();
	}

	// If no properties have been set for them, reset to null so the
	// EmbeddedNavPanel default colors are used.
	awc.setProperties(curPrefix + ACTIVE_WIDGET_COLOR_PROPERTY, props);
	if (awc.equals(DrawingAttributes.getDefaultClone())) {
	    awc = null;
	}

	iwc.setProperties(curPrefix + INACTIVE_WIDGET_COLOR_PROPERTY, props);
	if (iwc.equals(DrawingAttributes.getDefaultClone())) {
	    iwc = null;
	}

	setActiveWidgetColors(awc);
	setInactiveWidgetColors(iwc);

	setWidgetButtonSize(
		PropUtils.intFromProperties(props, curPrefix + WIDGET_SIZE_PROPERTY, getWidgetButtonSize()));
    }

    @Override
    public Properties getProperties(final Properties props) {
	final Properties properties = super.getProperties(props);
	final String prefix = PropUtils.getScopedPropertyPrefix(this);

	final DrawingAttributes awc = getActiveWidgetColors();
	if (awc != null) {
	    awc.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(this) + ACTIVE_WIDGET_COLOR_PROPERTY);
	    awc.getProperties(properties);
	}

	final DrawingAttributes iwc = getInactiveWidgetColors();
	if (iwc != null) {
	    iwc.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(this) + INACTIVE_WIDGET_COLOR_PROPERTY);
	    iwc.getProperties(properties);
	}

	final int widgetSize = getWidgetButtonSize();
	if (widgetSize != DEFAULT_WIDGET_BUTTON_SIZE) {
	    props.put(prefix + WIDGET_SIZE_PROPERTY, Integer.toString(widgetSize));
	}

	return properties;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans. PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
	SC3Logger.LOGGER.trace("Scoop3MapPanel - propertyChange - " + evt.getPropertyName());

	if (MapBean.CursorProperty.equals(evt.getPropertyName())) {
	    centerContainer.setCursor((Cursor) evt.getNewValue());
	} else if ("MapBean.projection".equals(evt.getPropertyName())) {
	    // Reinit to the default mouse event type
	    if (mouseDelagator != null) {
		// setMouseModeForSelecting();
	    }
	} else {
	    if (evt instanceof MapPropertyChangeEvent) {
		switch (((MapPropertyChangeEvent) evt).getEventEnum()) {
		case LINK_UNLINK_OBSERVATIONS:
		    observationLayer.linkUnlinkObservations();
		    break;
		case SELECT_OBSERVATION_NAV:
		case SELECT_OBSERVATION_BY_REFERENCE:
		    setMapSelectedElement(evt.getSource());
		    break;
		case SHOW_HIDE_LABELS:
		    observationLayer.showHideLabelObservations();
		    break;
		case UPDATE_LOCATION_FOR_OBS:
		    // evt.getSource() is an ARRAY [(String) observationReference, (Integer) currentLevel, (Double[])
		    // [newLat, newLon]]
		    final Object[] newPosition = (Object[]) evt.getSource();
		    observationLayer.updateLocationForObs((String) newPosition[0], (Integer) newPosition[1],
			    (Double[]) newPosition[2]);
		    break;
		case UPDATE_QC_FOR_OBS:
		    // evt.getSource() is an ARRAY [(String) observationReference, (Integer) currentLevel, (Integer)
		    // newQCValue]
		    final Object[] newValue = (Object[]) evt.getSource();
		    observationLayer.updateQcForObs((String) newValue[0], (Integer) newValue[1], (Integer) newValue[2],
			    (Double) newValue[3], (Double) newValue[4]);
		    break;
		case MAP_SELECTION:
		    // observationLayer.mapSelection();
		    break;
		case ZOOM_IN:
		    observationLayer.zoomIn();
		    break;
		case ZOOM_INITIAL:
		    observationLayer.zoomInitial();
		    break;
		case ZOOM_OUT:
		    observationLayer.zoomOut();
		    break;
		case ZOOM_RECTANGLE:
		    observationLayer.zoomRectangle((boolean) evt.getNewValue());
		    break;
		case ZOOM_WORLD:
		    observationLayer.zoomWorld();
		    break;
		}
	    }
	}
    }

    protected GraphicAttributes getFilterGA() {
	if (filterGA == null) {
	    filterGA = new GraphicAttributes();
	    filterGA.setLinePaint(Color.red);
	    filterGA.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	    // filterGA.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
	    final BasicStroke filterStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f,
		    new float[] { 3, 3 }, 0f);
	    filterGA.setStroke(filterStroke);
	}
	return (GraphicAttributes) filterGA.clone();
    }

    /**
     * Init of the map
     *
     * @param omList
     *            The list of observation to add to the map.
     * @param addKeyListener
     * @return The JPanel containing the map
     */
    public JPanel init(final OMGraphicList omList, final boolean addKeyListener) {
	// Create the map
	create();

	final String baseMap = FileConfig.getScoop3FileConfig().getString("map.base-map");

	if ((baseMap != null) && baseMap.equalsIgnoreCase("MARINE-GEO")) {
	    final String mapUrl = FileConfig.getScoop3FileConfig().getString("map.url.marine-geo");

	    final WMSPlugIn wmsPlugin = new WMSPlugIn();
	    final Properties wmsPluginProperties = new Properties();
	    wmsPluginProperties.setProperty("wmsserver",
		    (mapUrl != null) ? mapUrl : "http://www.marine-geo.org/services/wms");
	    wmsPluginProperties.setProperty("wmsversion", "1.1.1");
	    wmsPluginProperties.setProperty("layers", "topo");
	    wmsPluginProperties.setProperty("format", "image/png");
	    wmsPlugin.setProperties(wmsPluginProperties);
	    getMapHandler().add(wmsPlugin);
	} else {
	    final String mapUrl = FileConfig.getScoop3FileConfig().getString("map.url.argis");

	    // Base map layer from tile server
	    final MapTileLayer mapTileLayer = new MapTileLayer();
	    final Properties tileProperties = new Properties();
	    // tileProperties.setProperty("rootDir",
	    // "http://c.tile.openstreetmap.org/")
	    // tileProperties.setProperty("rootDir",
	    // "http://mtile04.mqcdn.com/tiles/1.0.0/vy/sat/")

	    // Ocean base ESRI
	    tileProperties.setProperty("rootDir", (mapUrl != null) ? mapUrl
		    : "https://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Base/MapServer/tile/{z}/{y}/{x}");

	    mapTileLayer.setProperties(tileProperties);
	    mapTileLayer.setVisible(true);
	    mapTileLayer.setTransparency(1);
	    getMapHandler().add(mapTileLayer);
	}

	getMapBean().setBackgroundColor(new Color(0x99b3cc));
	addMapComponent(new LayerHandler());
	getMapBean().setBackgroundColor(new Color(0x99b3cc));
	addMapComponent(new LayerHandler());
	mouseDelagator = new MouseDelegator();
	addMapComponent(mouseDelagator);
	addMapComponent(new OMMouseMode());
	final NavMouseMode nmm = new NavMouseMode();
	mouseDelagator.addMouseMode(nmm);
	final PanMouseMode pmm = new PanMouseMode();
	final Cursor cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	pmm.setModeCursor(cursor);
	pmm.setLeaveShadow(false);
	mouseDelagator.addMouseMode(pmm);
	final OMDrawingToolMouseMode omdtmm = new OMDrawingToolMouseMode();
	mouseDelagator.addMouseMode(omdtmm);

	/*
	 *
	 */
	final OMDrawingTool dt = new OMDrawingTool();
	dt.setMouseDelegator(mouseDelagator);
	final EditToolLoader[] etls = { new OMRectLoader() };
	dt.setLoaders(etls);
	addMapComponent(dt);

	// Display DialogBox received by ObservationLayer.fireRequestMessage("...")
	// final InformationDelegator informationDelagator = new InformationDelegator();
	// addMapComponent(informationDelagator);

	// Create the graticule layer
	final Scoop3GraticuleLayer graticule = new Scoop3GraticuleLayer();
	addMapComponent(graticule);

	// Create the observation layer and add to map
	observationLayer = new ObservationLayer(getMapBean(), dt);
	observationLayer.setLatLonBathyLabel(latLonBathyLabel);
	observationLayer.init(omList);
	addMapComponent(observationLayer);

	// Add listener to the observation layer
	observationLayer.addPropertyChangeListener((final PropertyChangeEvent evt) -> {
	    SC3Logger.LOGGER.trace("Scoop3MapPanel - observation - propertyChangeEvent - " + evt.getPropertyName());
	    if ("SELECTED_OBSERVATION_MAP".equals(evt.getPropertyName())) {
		changes.firePropertyChange("SELECTED_OBSERVATION_MAP", null, evt.getNewValue());
	    } else if ("MAP_MODE_ZOOM".equals(evt.getPropertyName())) {
		setMouseModeForZooming((boolean) evt.getNewValue());
	    } else if ("TOGGLE_FULL_SCREEN_FOR_MAP".equals(evt.getPropertyName())) {
		changes.firePropertyChange("TOGGLE_FULL_SCREEN_FOR_MAP", null, "");
	    } else if ("MAP_MODE_SELECT".equals(evt.getPropertyName())) {
		setMouseModeForSelecting();
	    } else if ("MAP_MODE_NAVIGATION".equals(evt.getPropertyName())) {
		setMouseModeForNavigation();
	    } else if ("MAP_MODE_DRAWING".equals(evt.getPropertyName())) {
		setMouseModeForDrawing();
	    } else if ("CHANGE_QC_ON_MAP".equals(evt.getPropertyName())) {
		changes.firePropertyChange("CHANGE_QC_ON_MAP", null, evt.getNewValue());
	    }
	});

	// Zoom on data
	final MapBean mapBean = getMapBean();
	CommonFunctions.zoomToDatas(mapBean, omList);

	if (addKeyListener) {
	    getMapBean().addKeyListener(new ObservationMapBeanKeyListener(observationLayer));
	}

	return centerContainer;
    }

    /**
     *
     * @param ae
     *            The action event
     */
    public void actionPerformed(final ActionEvent ae) {
	SC3Logger.LOGGER.trace("actionPerformed");
    }

    /**
     * Set the selectedElement for the map
     *
     * @param object
     *            The selected element
     */
    public void setMapSelectedElement(final Object object) {
	observationLayer.selectOMPointFromObject(object);
    }

    /**
     * Add a property change listener to this component
     */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
	if (changes == null) {
	    changes = new PropertyChangeSupport(this);
	}
	changes.addPropertyChangeListener(listener);
    }

    /**
     * Return the observations layer
     *
     * @return
     */
    public ObservationLayer getObservationLayer() {
	return observationLayer;
    }

    public void clean() {
	getMapHandler().dispose();
	getMapBean().dispose();
	observationLayer = null;
    }

    /**
     * Set the mouse mode for zooming
     *
     * @param enabled
     */
    public void setMouseModeForZooming(final boolean enabled) {
	if (enabled) {
	    updateDrawingMode(mouseDelagator.getActiveMouseModeID());
	    mouseDelagator.setActiveMouseModeWithID("Navigation");

	} else {
	    setMouseModeForSelecting();
	}

	changes.firePropertyChange("CHANGE_ZOOM_RECT_MODE", null, enabled);
    }

    /**
     * Set the mouse mode for selecting
     */
    public void setMouseModeForSelecting() {
	updateDrawingMode(mouseDelagator.getActiveMouseModeID());
	mouseDelagator.setActiveMouseModeWithID("Gestures");
    }

    /**
     * Set the mouse mode for navigation
     */
    public void setMouseModeForNavigation() {
	updateDrawingMode(mouseDelagator.getActiveMouseModeID());
	mouseDelagator.setActiveMouseModeWithID("Pan");
    }

    private void updateDrawingMode(final String oldActiveMouseModeID) {
	if ((oldActiveMouseModeID != null) && oldActiveMouseModeID.equals("Drawing")) {
	    ((OMDrawingTool) observationLayer.getDrawingTool()).deactivate();
	}
    }

    /**
     * Set the mouse mode for drawing
     */
    public void setMouseModeForDrawing() {
	updateDrawingMode(mouseDelagator.getActiveMouseModeID());
	mouseDelagator.setActiveMouseModeWithID("Drawing");
	observationLayer.mapSelection();
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
	SC3Logger.LOGGER.debug("mouseClicked : " + e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
	SC3Logger.LOGGER.debug("mousePressed : " + e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
	SC3Logger.LOGGER.debug("mouseReleased : " + e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
	SC3Logger.LOGGER.debug("mouseEntered : " + e);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
	SC3Logger.LOGGER.debug("mouseExited : " + e);
    }

    public void setToggleFullScreen() {
	observationLayer.setToggleFullScreen();
    }

    public void setMapBackupDirectory(final JFrame sc3Frame, final String mapBackupDir) {
	observationLayer.setMapBackupDirectory(sc3Frame, mapBackupDir);
    }
}
