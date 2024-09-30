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
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DemoLayer.java,v $
// $RCSfile: DemoLayer.java,v $
// $Revision: 1.25 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
//
// **********************************************************************
package fr.ifremer.scoop3.map.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.image.AbstractImageFormatter;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolReferenceLibrary;
import com.bbn.openmap.util.PaletteHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.ifremer.scoop3.bathyClimato.BathyClimatologyManager;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.map.model.PointModel;
import fr.ifremer.scoop3.map.tools.CommonFunctions;
import fr.ifremer.scoop3.model.valueAndQc.QCColor;

/**
 * This layer demonstrates interactive capabilities of OpenMap. Instantiating this layer should show an icon loaded
 * using HTTP Protocol, which represents Boston, MA in USA. Above Boston it should show a square that would change color
 * when mouse is moved over it in 'Gesture' mode. Also clicking once brings up a message box and more than once brings
 * up browser.
 * <P>
 *
 * The DemoLayer has also been modified to demonstrate the first uses of the OMDrawingTool. The Palette has buttons that
 * can be used to start the tool in several different ways.
 *
 * @see com.bbn.openmap.layer.DemoLayer
 *
 *      Just added some decorated splines to test them. EL
 */
public class ObservationLayer extends OMGraphicHandlerLayer
	implements DrawingToolRequestor, ActionListener, PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 4382596046564631693L;

    /**
     * Used by geometries created in GUI for specify if the spatial filter is for objects outside the drawn shape.
     */
    protected static final String EXTERNAL_KEY = "ek";
    /**
     * Used by geometries created in GUI for specify if the spatial filter is for objects inside the drawn shape.
     */
    protected static final String INTERNAL_KEY = "ik";
    private static final String RCT = "rightClickTest";
    protected CenterSupport centerDelegate;
    /**
     * Found in the findAndInit() method, in the MapHandler.
     */
    protected OMDrawingTool drawingTool;
    protected GraphicAttributes filterGA = null;
    /**
     * Used by the internal ActionListeners for a callback, see getGUI().
     */
    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;
    protected JPanel legend;

    protected MapBean mapBean;
    /**
     * The MouseDelegator that is controlling the MouseModes. We need to keep track of what's going on so we can adjust
     * our tools accordingly.
     */
    protected MouseDelegator mouseDelegator;
    /**
     * This is a list to hold the non-changing OMGraphics to display on the layer. It is used to load the OMGraphicList
     * that the layer actually paints.
     */
    protected OMGraphicList objects;

    protected transient List<OMPoly> polyLines = new ArrayList<>();

    protected OMCircle selectedCircle;

    protected transient Object selectedObject;
    protected OMPoint selectedPoint;

    protected boolean selectFromNavigation;

    /**
     * Found in the findAndInit() method, in the MapHandler.
     */
    protected transient SymbolReferenceLibrary srl;

    protected ZoomSupport zoomDelegate;

    final NumberFormat latLonFormatter = new DecimalFormat("#0.00000");
    private JLabel latLonBathyLabel;

    protected OMRect selectLine;

    private double lastLatitudeToDisplay = 0d;
    private double lastLongitudeToDisplay = 0d;

    private String mapBackupDir = ".";
    private JFrame sc3Frame = null;
    private boolean zoomIn = true;

    public ObservationLayer() {
	setName("Observation");
	// This is how to set the ProjectionChangePolicy, which
	// dictates how the layer behaves when a new projection is
	// received.
	setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
	// Improves performance
	setRenderPolicy(new BufferedImageRenderPolicy());
	// Making the setting so this layer receives events from the
	// SelectMouseMode, which has a modeID of "Gestures". Other
	// IDs can be added as needed.
	setMouseModeIDsForEvents(new String[] { "Gestures" });
	centerDelegate = new CenterSupport(this);
	zoomDelegate = new ZoomSupport(this);
    }

    public ObservationLayer(final MapBean mapBean, final OMDrawingTool dt) {
	setName("Observation");

	this.mapBean = mapBean;
	this.drawingTool = dt;

	// This is how to set the ProjectionChangePolicy, which
	// dictates how the layer behaves when a new projection is
	// received.
	setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
	// Making the setting so this layer receives events from the
	// SelectMouseMode, which has a modeID of "Gestures". Other
	// IDs can be added as needed.
	setMouseModeIDsForEvents(new String[] { "Gestures" });
	centerDelegate = new CenterSupport(this);
	zoomDelegate = new ZoomSupport(this);
    }

    /**
     * Add CenterListener
     *
     * @param listener
     */
    public synchronized void addCenterListener(final CenterListener listener) {
	centerDelegate.add(listener);
    }

    /**
     * Add ZoomListener
     *
     * @param listener
     */
    public synchronized void addZoomListener(final ZoomListener listener) {
	zoomDelegate.add(listener);
    }

    /**
     * Called when the DrawingTool is complete, providing the layer with the modified OMGraphic.
     */
    @Override
    public void drawingComplete(final OMGraphic omg, final OMAction action) {
	SC3Logger.LOGGER.trace("drawingComplete");

	final Object obj = omg.getAppObject();

	if ((obj != null) && (INTERNAL_KEY.equals(obj) || EXTERNAL_KEY.equals(obj))
		&& !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {
	    final java.awt.Shape filterShape = omg.getShape();
	    final OMGraphicList filteredList = filter(filterShape, INTERNAL_KEY.equals(omg.getAppObject()));
	    SC3Logger.LOGGER
		    .trace("drawingComplete : filteredList.getDescription() : " + filteredList.getDescription());
	} else if (omg instanceof OMRect) {
	    setSelectLine((OMRect) omg);
	    // eventBroker.post("drawingComplete", this); // FIXME ???

	    final OMRect selectedRect = (OMRect) omg;
	    final OMGraphicList omgl = CommonFunctions.getOMPointFromList(objects);
	    final ListIterator<? extends OMGraphic> iterator = omgl.listIterator();

	    final double latNorth = (selectedRect.getNorthLat() > selectedRect.getSouthLat())
		    ? selectedRect.getNorthLat() : selectedRect.getSouthLat();
	    final double latSouth = (selectedRect.getNorthLat() > selectedRect.getSouthLat())
		    ? selectedRect.getSouthLat() : selectedRect.getNorthLat();
	    final double lonWest = (selectedRect.getWestLon() < selectedRect.getEastLon()) ? selectedRect.getWestLon()
		    : selectedRect.getEastLon();
	    final double lonEast = (selectedRect.getWestLon() < selectedRect.getEastLon()) ? selectedRect.getEastLon()
		    : selectedRect.getWestLon();

	    // must change the reference and add latitude and longitude to distinct observations with no
	    // parameters and level 0 (same reference if not managed)
	    final List<String[]> selectedObjects = new ArrayList<>();
	    while (iterator.hasNext()) {
		final OMPoint graphic = (OMPoint) iterator.next();
		final PointModel pointModel = (PointModel) graphic.getAppObject();
		if ((latNorth >= graphic.getLat()) && (latSouth <= graphic.getLat()) && (lonWest <= graphic.getLon())
			&& (lonEast >= graphic.getLon()) && (pointModel != null)) {
		    final String[] pointModelValues = new String[] { pointModel.getReference(),
			    String.valueOf(pointModel.getLevel()), pointModel.getLatitude().toString(),
			    pointModel.getLongitude().toString() };
		    selectedObjects.add(pointModelValues);
		}
	    }
	    if (!selectedObjects.isEmpty()) {
		firePropertyChange("CHANGE_QC_ON_MAP", null, selectedObjects);
	    }
	    mapSelection();
	} else {
	    if (!doAction(omg, action)) {
		// null OMGraphicList on failure, should only occur if
		// OMGraphic is added to layer before it's ever been
		// on the map.
		setList(new OMGraphicList());
		doAction(omg, action);
	    }
	}

	validate();
	repaint();
    }

    /**
     * Export A JPEG IMAGE Of the map view near the XML context file
     *
     * @return true if success, false other
     * @param imageWidth
     * @param imageHeight
     * @param outputFilename
     */
    public boolean exportMapImage(final String outputFilename) {

	final AbstractImageFormatter formatter = new SunJPEGFormatter();

	final byte[] imageBytes = formatter.getImageFromMapBean(mapBean, this.mapBean.getSize().width,
		this.mapBean.getSize().height, true);
	// final byte[] imageBytes = formatter.getImageFromMapBean(mapBean, 1920, 1080);

	try (FileOutputStream binFile = new FileOutputStream(outputFilename)) {
	    binFile.write(imageBytes);
	    return true;
	} catch (final IOException e) {
	    SC3Logger.LOGGER.error(e.getMessage());
	    return false;
	}

    }

    /**
     * Called when a component that is needed, and not available with an appropriate iterator from the BeanContext. This
     * lets this object hook up with what it needs. For Layers, this method doesn't do anything by default. If you need
     * your layer to get ahold of another object, then you can use the Iterator to go through the objects to look for
     * the one you need.
     */
    @Override
    public void findAndInit(final Object someObj) {
	if (someObj instanceof OMDrawingTool) {
	    SC3Logger.LOGGER.trace("demo", "Found a drawing tool");
	    setDrawingTool((OMDrawingTool) someObj);
	}

	if (someObj instanceof SymbolReferenceLibrary) {
	    setSymbolReferenceLibrary((SymbolReferenceLibrary) someObj);
	}

	if (someObj instanceof ZoomListener) {
	    zoomDelegate.add((ZoomListener) someObj);
	}

	if (someObj instanceof CenterListener) {
	    addCenterListener((CenterListener) someObj);
	}

	if (someObj instanceof MouseDelegator) {
	    mouseDelegator = (MouseDelegator) someObj;
	    mouseDelegator.addPropertyChangeListener(this);
	}
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is removed from the BeanContext of this object.
     * For the Layer, this method doesn't do anything. If your layer does something with the childrenAdded method, or
     * findAndInit, you should take steps in this method to unhook the layer from the object used in those methods.
     */
    @Override
    public void findAndUndo(final Object someObj) {
	if ((someObj instanceof DrawingTool) && (getDrawingTool() == (DrawingTool) someObj)) {
	    setDrawingTool(null);
	}
    }

    public DrawingTool getDrawingTool() {
	// Usually set in the findAndInit() method.
	return drawingTool;
    }

    /**
     * Method called in the AbstractDrawingEditorTool constructor.
     */
    public void initDrawingTool() {
	drawingTool.setUseAsTool(true); // prevents popup menu use.
	drawingTool.getMouseMode().setVisible(true);
	final GraphicAttributes ga = drawingTool.getAttributes();
	ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	// ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
    }

    @Override
    public java.awt.Component getGUI() {

	final JPanel panel = new JPanel();
	final GridBagLayout gridbag = new GridBagLayout();
	final GridBagConstraints c = new GridBagConstraints();
	panel.setLayout(gridbag);

	final JPanel box = PaletteHelper.createVerticalPanel(" Create Filters for Map ");
	box.setLayout(new java.awt.GridLayout(0, 1));
	JButton button = new JButton("Create Containing Rectangle Filter");
	button.addActionListener((final ActionEvent event) -> {
	    final DrawingTool dt = getDrawingTool();
	    if (dt != null) {
		final GraphicAttributes fga = getFilterGA();
		fga.setFillPaint(new OMColor(0x0c0a0a0a));

		final OMRect rect = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", fga, layer,
			false);
		if (rect != null) {
		    rect.setAppObject(INTERNAL_KEY);
		} else {
		    SC3Logger.LOGGER.error("Drawing tool can't create OMRect");
		}
	    } else {
		SC3Logger.LOGGER.warn("Can't find a drawing tool");
	    }
	});
	box.add(button);

	button = new JButton("Create Containing Polygon Filter");
	button.addActionListener((final ActionEvent event) -> {
	    final DrawingTool dt = getDrawingTool();
	    if (dt != null) {
		final GraphicAttributes fga = getFilterGA();
		fga.setFillPaint(OMColor.clear);

		final EditableOMPoly eomp = new EditableOMPoly(fga);
		eomp.setEnclosed(true);
		eomp.setShowGUI(false);

		dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
		final OMPoly poly = (OMPoly) getDrawingTool().edit(eomp, layer);

		if (poly != null) {
		    poly.setIsPolygon(true);
		    poly.setAppObject(INTERNAL_KEY);
		} else {
		    SC3Logger.LOGGER.error("Drawing tool can't create OMPoly");
		}
	    } else {
		SC3Logger.LOGGER.warn("Can't find a drawing tool");
	    }
	});
	box.add(button);

	button = new JButton("Create Excluding Rectangle Filter");
	button.addActionListener((final ActionEvent event) -> {
	    final DrawingTool dt = getDrawingTool();
	    if (dt != null) {
		final GraphicAttributes fga = getFilterGA();
		fga.setFillPaint(OMColor.clear);

		final OMRect rect = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", fga, layer,
			false);
		if (rect != null) {
		    rect.setAppObject(EXTERNAL_KEY);
		} else {
		    SC3Logger.LOGGER.error("Drawing tool can't create OMRect");
		}
	    } else {
		SC3Logger.LOGGER.warn("Can't find a drawing tool");
	    }
	});
	box.add(button);

	button = new JButton("Reset filter");
	button.addActionListener((final ActionEvent event) -> {
	    resetFiltering();
	    repaint();
	});
	box.add(button);

	gridbag.setConstraints(box, c);
	panel.add(box);
	return panel;
    }

    /**
     * Query for what text should be placed over the information bar when the mouse is over a particular OMGraphic.
     */
    @Override
    public String getInfoText(final OMGraphic omg) {
	final DrawingTool dt = getDrawingTool();
	if ((dt != null) && dt.canEdit(omg.getClass())) {
	    return "Click to edit graphic.";
	} else {
	    return null;
	}
    }

    /**
     *
     */
    @Override
    public List<Component> getItemsForMapMenu(final MapMouseEvent me) {
	final List<Component> l = new ArrayList<Component>();

	final JMenuItem zoomPlus = new JMenuItem(Messages.getMessage("map.zoom_in"));
	zoomPlus.addActionListener((final ActionEvent ae) -> zoomIn());

	final JMenuItem zoomMinus = new JMenuItem(Messages.getMessage("map.zoom_out"));
	zoomMinus.addActionListener((final ActionEvent ae) -> zoomOut());

	final JMenuItem zoomInit = new JMenuItem(Messages.getMessage("map.zoom_initial"));
	zoomInit.addActionListener((final ActionEvent ae) -> zoomInitial());

	final JMenuItem zoomWorld = new JMenuItem(Messages.getMessage("map.zoom_world"));
	zoomWorld.addActionListener((final ActionEvent ae) -> zoomWorld());

	final JMenuItem previousObservation = new JMenuItem(Messages.getMessage("map.next_observation"));
	previousObservation.addActionListener((final ActionEvent ae) -> selectNextOMPoint());

	final JMenuItem nextObservation = new JMenuItem(Messages.getMessage("map.previous_observation"));
	nextObservation.addActionListener((final ActionEvent ae) -> selectPreviousOMPoint());

	final JMenuItem showHideLabel = new JMenuItem(Messages.getMessage("map.show_observation_label"));
	showHideLabel.addActionListener((final ActionEvent ae) -> showHideLabelObservations());

	final JMenuItem linkUnlinkObservations = new JMenuItem(Messages.getMessage("map.link-observation"));
	linkUnlinkObservations.addActionListener((final ActionEvent ae) -> linkUnlinkObservations());

	final JMenuItem zoomRect = new JMenuItem(Messages.getMessage("map.zoom_rectangle"));
	zoomRect.addActionListener((final ActionEvent ae) -> zoomRectangle(true));

	final JMenuItem saveMap = new JMenuItem(Messages.getMessage("map.save_map"));
	saveMap.addActionListener((final ActionEvent ae) -> saveMap());

	l.add(zoomPlus);
	l.add(zoomMinus);
	l.add(zoomInit);
	l.add(zoomWorld);
	l.add(new JSeparator());
	l.add(previousObservation);
	l.add(nextObservation);
	l.add(new JSeparator());
	l.add(showHideLabel);
	l.add(linkUnlinkObservations);
	l.add(new JSeparator());
	l.add(zoomRect);
	l.add(new JSeparator());
	l.add(saveMap);

	return l;
    }

    /**
     *
     */
    @Override
    public List<Component> getItemsForOMGraphicMenu(final OMGraphic omg) {

	final String rightClickTest = (String) omg.getAttribute(RCT);

	SC3Logger.LOGGER.trace("rightClickTest : " + rightClickTest);

	final List<Component> l = new ArrayList<Component>();
	// l.add(new JMenuItem("Which"));
	// l.add(new JMenuItem("Why"));
	// l.add(new JSeparator());
	// l.add(new JMenuItem(rightClickTest));
	return l;
    }

    public SymbolReferenceLibrary getSymbolReferenceLibrary() {
	return srl;
    }

    /**
     * Query for what tooltip to display for an OMGraphic when the mouse is over it.
     */
    @Override
    public String getToolTipTextFor(final OMGraphic omg) {
	final Object tt = omg.getAttribute(OMGraphic.TOOLTIP);
	if (tt instanceof String) {
	    return (String) tt;
	}

	String classname = omg.getClass().getName();
	final int lio = classname.lastIndexOf('.');
	if (lio != -1) {
	    classname = classname.substring(lio + 1);
	}

	return "Demo Layer Object: " + classname;
    }

    /**
     * Hide the trajectory
     */
    public void hideTrajectory() {
	for (final OMPoly polyLine : polyLines) {
	    objects.remove(polyLine);
	}
	repaint();
    }

    /**
     * Initialisation des objets de la couche par rapport � une liste d'objets
     *
     * @param omList
     *            La liste d'objets
     */
    public void init(final OMGraphicList omList) {

	// This layer keeps a pointer to an OMGraphicList that it uses
	// for painting. It's initially set to null, which is used as
	// a flag in prepare() to signal that the OMGraphcs need to be
	// created. The list returned from prepare() gets set in the
	// layer.
	// This layer uses the StandardPCPolicy for new
	// projections, which keeps the list intact and simply calls
	// generate() on it with the new projection, and repaint()
	// which calls paint().

	if ((objects != null) && (polyLines != null)) {
	    hideTrajectory();
	    polyLines.clear();
	}

	selectedPoint = null;

	objects = new OMGraphicList();

	objects.add(omList);

	initPolylines();

	initDrawingTool();
    }

    /**
     *
     */
    private void initPolylines() {
	final OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	final List<Double> listPoints = new ArrayList<Double>();

	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	final List<String> platformsCode = new ArrayList<>();
	final List<String> references = new ArrayList<>();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    listPoints.add(((OMPoint) graphic).getLat());
	    listPoints.add(((OMPoint) graphic).getLon());
	    if (((OMPoint) graphic).getAppObject() != null) {
		final String platformCode = ((PointModel) ((OMPoint) graphic).getAppObject()).getPlatformCode();
		final String reference = ((PointModel) ((OMPoint) graphic).getAppObject()).getReference();
		// SC3Logger.LOGGER.debug(platformCode);
		platformsCode.add(platformCode);
		references.add(reference);
	    }
	}

	String prevPlatformCode = (platformsCode.isEmpty()) ? "" : platformsCode.get(0);
	String prevReference = (references.isEmpty()) ? "" : references.get(0);
	final List<Double> targetList = new ArrayList<>();
	for (int i = 0; i < platformsCode.size(); i++) {
	    if (!prevPlatformCode.equals(platformsCode.get(i)) || ((prevReference != null)
		    && prevReference.contains("- Level") && !prevReference.equals(references.get(i)))) {
		final double[] target = new double[targetList.size()];
		for (int j = 0; j < target.length; j++) {
		    target[j] = targetList.get(j);
		}
		targetList.clear();
		final OMPoly polyLine = new OMPoly(target, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
		polyLines.add(polyLine);
		prevPlatformCode = platformsCode.get(i);
		prevReference = references.get(i);
	    }
	    targetList.add(listPoints.get(2 * i));
	    targetList.add(listPoints.get((2 * i) + 1));
	}

	if (!targetList.isEmpty()) {
	    final double[] target = new double[targetList.size()];
	    for (int j = 0; j < target.length; j++) {
		target[j] = targetList.get(j);
	    }
	    targetList.clear();
	    final OMPoly polyLine = new OMPoly(target, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
	    polyLines.add(polyLine);
	}

	for (final OMPoly polyLine : polyLines) {
	    objects.add(polyLine);
	}
    }

    /**
     * Query that an OMGraphic can be highlighted when the mouse moves over it. If the answer is true, then highlight
     * with this OMGraphics will be called.
     */
    @Override
    public boolean isHighlightable(final OMGraphic omg) {
	return (omg != selectLine);
    }

    /**
     * Query that an OMGraphic is selectable.
     */
    @Override
    public boolean isSelectable(final OMGraphic omg) {
	// DrawingTool dt = getDrawingTool()
	// return (dt != null && dt.canEdit(omg.getClass()))
	return (omg != selectLine);

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#leftClick(com.bbn.openmap.event.MapMouseEvent)
     */
    @Override
    public boolean leftClick(final MapMouseEvent mme) {
	displayLatLonBathy(mme.getLatLon().getY(), mme.getLatLon().getX(), true);
	return super.leftClick(mme);
    }

    /**
     * Add or remove the observation trajectory
     */
    public void linkUnlinkObservations() {

	for (final OMPoly polyLine : polyLines) {
	    if (objects.contains(polyLine)) {
		objects.remove(polyLine);
	    } else {
		objects.add(polyLine);
	    }
	}

	// Refresh the map
	doPrepare();
    }

    @Override
    public void paint(final java.awt.Graphics g) {
	// Super calls the RenderPolicy that makes decisions on how to
	// paint the OMGraphicList. The only reason we have this
	// method overridden is to paint the legend if it exists.
	super.paint(g);
	if (legend != null) {
	    legend.paint(g);
	}
    }

    /**
     * This is an important Layer method to override. The prepare method gets called when the layer is added to the map,
     * or when the map projection changes. We need to make sure the OMGraphicList returned from this method is what we
     * want painted on the map. The OMGraphics need to be generated with the current projection. We test for a null
     * OMGraphicList in the layer to see if we need to create the OMGraphics. This layer doesn't change it's OMGraphics
     * for different projections, if your layer does, you need to clear out the OMGraphicList and add the OMGraphics you
     * want for the current projection.
     */
    @Override
    public synchronized OMGraphicList prepare() {
	final OMGraphicList list = new OMGraphicList();
	// Return new list of the objects to mange for the projection change.
	list.addAll(objects);
	list.generate(getProjection());
	return list;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
	SC3Logger.LOGGER.trace("ObservationLayer propertyChange");
	// if (MouseDelegator.MouseModesProperty.equals(evt.getPropertyName()))
	// {
	// }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#receivesMapEvents()
     */
    @Override
    public boolean receivesMapEvents() {
	// Used by mouseOver(...)
	return true;
    }

    /**
     * Checks to see if a label should be painted based on what methods were called in generate(), and renders the label
     * if necessary. If the label wasn't set up, a quick no-op occurs.
     *
     * @param g
     */
    public void renderLabel(final Graphics g) {
	SC3Logger.LOGGER.trace("ObservationLayer renderLabel");
	// if (hasLabel) {
	// OMLabeler labeler = (OMLabeler) getAttribute(LABEL)
	// if (labeler != null) {
	// labeler.render(g)
	// }
	// }
    }

    /**
     * Called if isSelectable(OMGraphic) was true, so the list has the OMGraphic. A list is used in case underlying code
     * is written to handle more than one OMGraphic being selected at a time.
     */
    @Override
    public void select(final OMGraphicList list) {
	if ((list != null) && !list.isEmpty()) {
	    // Get the first point of the list
	    final OMGraphic omg = list.getOMGraphicAt(0);

	    // Add a circle around the selected point
	    if (omg instanceof OMPoint) {

		final OMPoint ompoint = (OMPoint) omg;

		// FAE 50006 : fix the double jump on observation selection (mouse pressed + mouse clicked)
		if (ompoint.isSelected() || selectFromNavigation) {

		    // Select the new point only if it is a new point
		    if ((selectedPoint == null) || (ompoint.getLat() != selectedPoint.getLat())
			    || (ompoint.getLon() != selectedPoint.getLon())) {

			// Remove the previous selectedPoint
			objects.remove(selectedPoint);

			// Create the object to add the circle over the selectedPoint
			// selectedCircle = new OMCircle(ompoint.getLat(),
			// ompoint.getLon(), 2)
			selectedPoint = new OMPoint(ompoint.getLat(), ompoint.getLon(), 6);
			selectedPoint.setOval(true);
			final BasicStroke filterStroke = new BasicStroke(2f, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_MITER, 10f);
			selectedPoint.setStroke(filterStroke);

			// Add the selected point to the objects
			objects.add(selectedPoint);

			// Save the selected object
			selectedObject = omg.getAppObject();

			// If the point is selected from the map then send the event to
			// update navigation
			if (!selectFromNavigation) {
			    firePropertyChange("SELECTED_OBSERVATION_MAP", null, omg.getAppObject());
			}

			displayLatLonBathy(ompoint.getLat(), ompoint.getLon(), true);
		    }

		    // Refresh the map
		    doPrepare();
		    // zoom to the current observation point on map if it's not Scoop3 BPC
		    if (zoomIn) {
			CommonFunctions.zoomToDatas(mapBean, list);
			zoomIn();
		    }
		}
	    }

	}
    }

    /**
     * Select the next point on the map
     */
    public void selectNextOMPoint() {

	final int indexSelectedElement = getIndexSelectedObject();
	OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	int index = indexSelectedElement + 1;
	if (index > (omg.size() - 1)) {
	    index = 0;
	}
	while (omg.get(index).getAppObject() == null) {
	    index++;
	    if (index > (omg.size() - 1)) {
		index = 0;
	    }
	}

	final OMPoint ompoint = (OMPoint) omg.get(index);

	omg = new OMGraphicList();
	omg.add(ompoint);
	select(omg);
    }

    /**
     * Set the selected point on the map by the selected observation
     *
     * @param object
     *            The selected observation
     */
    public void selectOMPointFromObject(final Object object) {
	// set the boolean zoomIn at false if on Scoop3-BPC
	if (!sc3Frame.getTitle().contains("IDMDB")) {
	    setZoomIn(((PointModel) object).getZoomIn());
	} else {
	    setZoomIn(false);
	}
	// SC3Logger.LOGGER.debug("object: " + object);
	OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	OMPoint ompoint = null;
	if (!((PointModel) object).getDatasetType().equals("PROFILE")) {
	    try {
		// find the correct index of the point in the dataset (not only the currentObservation)
		for (int index = 0; index < omg.getTargets().size(); index++) {
		    if (((PointModel) omg.getTargets().get(index).getAppObject()).getReference()
			    .equals(((PointModel) object).getReference())
			    && (((PointModel) omg.getTargets().get(index).getAppObject()).getLatitude()
				    .equals(((PointModel) object).getLatitude()))
			    && (((PointModel) omg.getTargets().get(index).getAppObject()).getLongitude()
				    .equals(((PointModel) object).getLongitude()))) {
			ompoint = (OMPoint) omg.getOMGraphicAt(index + ((PointModel) object).getLevel());
			break;
		    }
		}
	    } catch (final Exception e) {
		ompoint = (OMPoint) omg.getWithObject(object);
	    }
	} else {
	    ompoint = (OMPoint) omg.getWithObject(object);
	}
	omg = new OMGraphicList();
	omg.add(ompoint);
	selectFromNavigation = true;
	select(omg);
	selectFromNavigation = false;
    }

    /**
     * Select the previous point on the map
     */
    public void selectPreviousOMPoint() {

	final int indexSelectedElement = getIndexSelectedObject();
	OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	int index = indexSelectedElement - 1;
	if (index < 0) {
	    index = omg.size() - 1;
	}
	while (omg.get(index).getAppObject() == null) {
	    index--;
	    if (index < 0) {
		index = omg.size() - 1;
	    }
	}

	final OMPoint ompoint = (OMPoint) omg.get(index);

	omg = new OMGraphicList();
	omg.add(ompoint);
	select(omg);
    }

    public void setDrawingTool(final OMDrawingTool dt) {
	// Called by the findAndInit method.
	drawingTool = dt;
    }

    @Override
    public void setProperties(final String prefix, final Properties props) {
	super.setProperties(prefix, props);
	setAddToBeanContext(true);
    }

    /**
     * Set the MilStd2525 SymbolReferenceLibrary object used to create symbols.
     *
     * @param library
     */
    public void setSymbolReferenceLibrary(final SymbolReferenceLibrary library) {
	srl = library;
    }

    /**
     * Add or remove the observations label
     */
    public void showHideLabelObservations() {
	boolean isLabelShowed = false;
	boolean isFirst = true;
	final OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);

	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    final OMPoint point = (OMPoint) graphic;

	    if (graphic.getAppObject() != null) {
		if (isFirst) {
		    isFirst = false;
		    if (graphic.getAttribute(OMGraphicConstants.LABEL) != null) {
			isLabelShowed = false;
		    } else {
			isLabelShowed = true;
		    }
		}
		if (isLabelShowed) {
		    CommonFunctions.addLabelToPoint(point);
		} else {
		    CommonFunctions.removeLabelToPoint(point);
		}
	    }
	}

	// Refresh the map
	doPrepare();
    }

    /**
     * Show the trajectory
     */
    public void showTrajectory() {
	for (final OMPoly polyLine : polyLines) {
	    objects.remove(polyLine);
	    objects.add(polyLine);
	}
	repaint();
    }

    /**
     * Update the location of the OMPoint for a given observation
     *
     * @param obsReference
     * @param level
     * @param newPosition
     *            Double[] {newLat, newLon}
     */
    public void updateLocationForObs(final String obsReference, final Integer level, final Double[] newPosition) {
	final OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    if ((graphic instanceof OMPoint) && (((OMPoint) graphic).getAppObject() != null)) {
		final String currentObsRef = ((PointModel) ((OMPoint) graphic).getAppObject()).getReference();
		final int currentLevel = ((PointModel) ((OMPoint) graphic).getAppObject()).getLevel();
		final boolean sameRef = (currentObsRef != null) && currentObsRef.equals(obsReference);
		final boolean sameLevel = (level == -1) || (level == currentLevel);
		if (sameRef && sameLevel) {
		    ((OMPoint) graphic).setLat(newPosition[0]);
		    ((OMPoint) graphic).setLon(newPosition[1]);

		    if ((objects != null) && (polyLines != null)) {
			hideTrajectory();
			polyLines.clear();
		    }

		    initPolylines();
		    validate();
		    repaint();

		    selectPreviousOMPoint();
		    selectNextOMPoint();
		}
	    }
	}
    }

    /**
     * Update the color of the OMPoint for a given observation
     *
     * @param obsReference
     * @param level
     * @param newQCValue
     */
    public void updateQcForObs(final String obsReference, final Integer level, final Integer newQCValue,
	    final Double latitude, final Double longitude) {
	final OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	final ListIterator<? extends OMGraphic> iterator = omg.listIteratorCopy();
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    if ((graphic instanceof OMPoint) && (((OMPoint) graphic).getAppObject() != null)) {
		final String currentObsRef = ((PointModel) ((OMPoint) graphic).getAppObject()).getReference();
		final int currentLevel = ((PointModel) ((OMPoint) graphic).getAppObject()).getLevel();
		final Double currentLatitude = ((PointModel) ((OMPoint) graphic).getAppObject()).getLatitude();
		final Double currentLongitude = ((PointModel) ((OMPoint) graphic).getAppObject()).getLongitude();
		final boolean sameRef = (currentObsRef != null) && currentObsRef.contains(obsReference);
		final boolean sameLevel = (level == -1) || (level == currentLevel);
		final boolean sameLat = latitude.equals(currentLatitude);
		final boolean sameLon = longitude.equals(currentLongitude);
		if (sameRef && sameLevel && sameLat && sameLon) {
		    ((OMPoint) graphic).setFillPaint(QCColor.QC_COLOR_MAP.get(newQCValue));
		    repaint();
		}
	    }
	}
    }

    /**
     * Zoom In (x2)
     */
    public void zoomIn() {
	// reinitZoom
	fireRequestMessage("Zoom X2.");
	zoomDelegate.fireZoom(ZoomEvent.RELATIVE, 1.0f / 2.0f);
    }

    /**
     * Zoom Initial
     */
    public void zoomInitial() {
	fireRequestMessage("Zoom initial.");
	CommonFunctions.zoomToDatas(mapBean, CommonFunctions.getOMPointFromList(objects));
	doPrepare();
    }

    /**
     * Zoom Out (x2)
     */
    public void zoomOut() {
	fireRequestMessage("Zoom /2.");
	zoomDelegate.fireZoom(ZoomEvent.RELATIVE, 2.0f);
    }

    /**
     * Set the mode for zooming
     *
     * @param enabled
     */
    public void zoomRectangle(final boolean enabled) {
	final NavMouseMode nmm = new NavMouseMode();
	mouseDelegator.addMouseMode(nmm);
	firePropertyChange("MAP_MODE_ZOOM", null, enabled);
    }

    /**
     * Zoom World
     */
    public void zoomWorld() {
	fireRequestMessage("Zoom World.");
	centerDelegate.fireCenter(0, 0);
	zoomDelegate.fireZoom(ZoomEvent.ABSOLUTE, Float.MAX_VALUE);
    }

    private void displayLatLonBathy(final double latitude, final double longitude, final boolean displayBathy) {
	lastLatitudeToDisplay = latitude;
	lastLongitudeToDisplay = longitude;

	final Short bathyFromService = BathyClimatologyManager.getSingleton()
		.getBestBathymetryWithoutException(latitude, longitude);
	final Double bathy = (bathyFromService == null) ? null : -1d * bathyFromService;

	String additionalInfo = "";
	if (selectedObject instanceof PointModel) {
	    additionalInfo = " ; ptf : " + ((PointModel) selectedObject).getPlatformCode();
	}

	latLonBathyLabel
		.setText("lat : " + latLonFormatter.format(latitude) + " ; lon : " + latLonFormatter.format(longitude)
			+ ((displayBathy && (bathy != null)) ? (" ; bathy : " + bathy) : "") + additionalInfo);
    }

    /**
     * Get the index of the the selected object
     *
     * @return
     */
    private int getIndexSelectedObject() {
	final OMGraphicList omg = CommonFunctions.getOMPointFromList(objects);
	final OMPoint ompoint = (OMPoint) omg.getWithObject(selectedObject);

	final ListIterator<? extends OMGraphic> iterator = omg.listIterator();
	int index = 0;
	while (iterator.hasNext()) {
	    final OMGraphic graphic = iterator.next();
	    final OMPoint point = (OMPoint) graphic;

	    if ((ompoint != null) && ompoint.equals(point)) {
		break;
	    }

	    index++;
	}

	return index;
    }

    protected GraphicAttributes getFilterGA() {
	if (filterGA == null) {
	    filterGA = new GraphicAttributes();
	    filterGA.setLinePaint(Color.red);
	    filterGA.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	    filterGA.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
	    final BasicStroke filterStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f,
		    new float[] { 3, 3 }, 0f);
	    filterGA.setStroke(filterStroke);
	}
	return (GraphicAttributes) filterGA.clone();
    }

    /**
     * @param latLonBathyLabel
     *            the latLonBathyLabel to set
     */
    public void setLatLonBathyLabel(final JLabel latLonBathyLabel) {
	this.latLonBathyLabel = latLonBathyLabel;
    }

    /**
     * Set the map in "full screen mode" or come back to "normal mode"
     */
    public void toggleFullScreen() {
	firePropertyChange("TOGGLE_FULL_SCREEN_FOR_MAP", null, "");
    }

    /**
     * Select an area on the map
     */
    public void mapSelection() {
	final DrawingTool dt = getDrawingTool();
	if (dt != null) {
	    final GraphicAttributes fga = getFilterGA();
	    fga.setFillPaint(new OMColor(0x0c0a0a0a));
	    fga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	    getDrawingTool().setBehaviorMask(OMDrawingTool.DEACTIVATE_ASAP_BEHAVIOR_MASK);
	    getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", fga, layer, false);
	} else {
	    SC3Logger.LOGGER.warn("Can't find a drawing tool");
	}
    }

    public void saveMap() {

	final String directory = (((mapBackupDir == null) || mapBackupDir.equals("")) ? "." : mapBackupDir);
	final String fullPath = directory + File.separator //
	// OK pour new SimpleDate au lieu d'utiliser Conversions
		+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) // OK
		+ "_map.jpg";
	exportMapImage(fullPath);

	// Popup pour informer
	JOptionPane.showMessageDialog(sc3Frame,
		MessageFormat.format(Messages.getMessage("map.save-map.dialog"), fullPath));

	// Ouverture d'un explorateur de fichier
	try {
	    Desktop.getDesktop().open(new File(directory));
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Return the initail map bounds computed with OM points list
     *
     * @return
     */
    public double[][] getInitialMapBounds() {
	final double[][] initialMapBounds = new double[2][2];

	final Proj proj = CommonFunctions.getInitialZoomProjection(mapBean,
		CommonFunctions.getOMPointFromList(objects));

	initialMapBounds[0][0] = proj.getLowerRight().getY();
	initialMapBounds[0][1] = proj.getLowerRight().getX();

	initialMapBounds[1][0] = proj.getUpperLeft().getY();
	initialMapBounds[1][1] = proj.getUpperLeft().getX();

	return initialMapBounds;
    }

    /**
     * Create a geoJson string with the openmap points & line
     *
     * @return
     */
    public String createGeoJsonString() {

	// Crate the Feature Collection GeoJson OBject
	final FeatureCollection featureCollection = new FeatureCollection();

	// Add PolyLine
	for (int i = 0; i < polyLines.size(); i++) {
	    final OMGraphic omGraphic = polyLines.get(i);

	    if (omGraphic instanceof OMPoly) {
		final OMPoly omPoly = (OMPoly) omGraphic;

		final Feature feature = new Feature();

		// care! : omPoly store lat/lon in radian, need to convert in decimal degree
		// use getLatLonArrayCopy() if convert or modifying
		double[] latLonArray = omPoly.getLatLonArrayCopy();
		latLonArray = ProjMath.arrayRadToDeg(latLonArray);

		final int trueSizeLatLonArray = latLonArray.length / 2;

		// Construct the LngLatAlt array of lat/lon from each point of OmPoly polyline
		final LngLatAlt[] lngLatAltList = new LngLatAlt[trueSizeLatLonArray];
		for (int j = 0; j < trueSizeLatLonArray; j++) {

		    final LngLatAlt lngLatAlt = new LngLatAlt();

		    final double latitude = latLonArray[j * 2];
		    final double longitude = latLonArray[(j * 2) + 1];

		    lngLatAlt.setLatitude(latitude);
		    lngLatAlt.setLongitude(longitude);

		    lngLatAltList[j] = lngLatAlt;
		}
		final GeoJsonObject geoJsonObject = new LineString(lngLatAltList);
		feature.setGeometry(geoJsonObject);

		featureCollection.add(feature);
	    }

	}

	// Add points
	final OMGraphicList pointList = CommonFunctions.getOMPointFromList(objects);
	for (int i = 0; i < pointList.size(); i++) {
	    final OMGraphic omGraphic = pointList.get(i);
	    final OMPoint omPoint = (OMPoint) omGraphic;

	    final Feature feature = new Feature();
	    final GeoJsonObject geoJsonObject = new Point(omPoint.getLon(), omPoint.getLat());
	    feature.setGeometry(geoJsonObject);

	    featureCollection.add(feature);
	}

	// Create mapper
	final ObjectMapper mapper = new ObjectMapper();
	mapper.enable(SerializationFeature.INDENT_OUTPUT);

	// GeoJson Object to JSON in String
	String geoJsonString = "";
	try {
	    geoJsonString = mapper.writeValueAsString(featureCollection);
	} catch (final JsonProcessingException e) {
	    SC3Logger.LOGGER.debug("Unable to write geoJson");
	    e.printStackTrace();
	}

	return geoJsonString;
    }

    public OMRect getSelectLine() {
	return selectLine;
    }

    public void setSelectLine(final OMRect omRect) {
	objects.remove(selectLine);
	selectLine = omRect;
	if (null != omRect) {
	    // Draw rectangle on the Map
	    // objects.add(selectLine);
	}
	validate();
	repaint();
    }

    /**
     * Hide select line
     */
    public void hideSelectLine() {
	objects.remove(selectLine);
	repaint();
    }

    public void setToggleFullScreen() {
	displayLatLonBathy(lastLatitudeToDisplay, lastLongitudeToDisplay, true);
    }

    public void setMapBackupDirectory(final JFrame sc3Frame, final String mapBackupDir) {
	this.sc3Frame = sc3Frame;
	this.mapBackupDir = mapBackupDir;
    }

    public ZoomSupport getZoomDelegate() {
	return this.zoomDelegate;
    }

    private void setZoomIn(final boolean b) {
	zoomIn = b;
    }
}
