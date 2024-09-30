/* *****************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2009 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 * $Id: NavigationPanel.java 29356 2009-04-21 02:35:27Z rmacinty $
 *
 * ****************************************************************************/

package fr.ifremer.scoop3.map.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicSliderUI;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.SoloMapComponent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.event.PanListener;
import com.bbn.openmap.event.PanSupport;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.proj.ProjectionStackTrigger;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.tools.icon.OpenMapAppPartCollection;
import com.bbn.openmap.util.PropUtils;

import fr.ifremer.scoop3.infra.i18n.Messages;

/**
 * A panel with map navigation widgets.
 * <p>
 * Portions of the implementation were ripped from com.bbn.openmap.gui.NavigatePanel,
 * com.bbn.openmap.gui.ProjectionStackTool, and com.bbn.openmap.gui.ZoomPanel.
 * </p>
 */
public class EmbeddedNavPanel extends OMComponentPanel
	implements ProjectionListener, ProjectionStackTrigger, SoloMapComponent {

    private static final long serialVersionUID = 1L;

    public static final int SLIDER_MAX = 17;
    public static final String FADE_ATTRIBUTES_PROPERTY = "fade";
    public static final String LIVE_ATTRIBUTES_PROPERTY = "live";
    public static final String PAN_DISTANCE_PROPERTY = "panDistance";
    public static final String ZOOM_FACTOR_PROPERTY = "zoomFactor";

    public static final int DEFAULT_BUTTON_SIZE = 15;

    protected static final float DEFAULT_PAN_DISTANCE = Float.NaN;
    protected static final float DEFAULT_ZOOM_FACTOR = 2.0f;

    protected static final Color CONTROL_BACKGROUND = OMGraphicConstants.clear;
    protected DrawingAttributes fadeAttributes;
    protected DrawingAttributes liveAttributes;
    protected int buttonSize = DEFAULT_BUTTON_SIZE;
    protected ImageIcon backIcon;
    protected ImageIcon backDimIcon;
    protected ImageIcon forwardIcon;
    protected ImageIcon forwardDimIcon;

    protected MapBean map;
    protected CenterSupport centerDelegate;
    protected PanSupport panDelegate;
    protected ZoomSupport zoomDelegate;
    protected JButton forwardProjectionButton;
    protected JButton backProjectionButton;
    protected JSlider slider;

    private float panDistance = DEFAULT_PAN_DISTANCE;
    private float zoomFactor = DEFAULT_ZOOM_FACTOR;

    protected float minTransparency = .7f;
    protected float semiTransparency = .9f;
    protected float maxTransparency = 1.0f;
    protected boolean fade = false;

    protected transient Point2D recenterPoint;

    protected transient AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, minTransparency);

    public EmbeddedNavPanel() {
	this(null, null, DEFAULT_BUTTON_SIZE);
    }

    /**
     * Make one.
     *
     * @param buttonColors
     *            The live button colors when active.
     * @param fadeColors
     *            The faded button colors, when inactive.
     * @param buttonSize
     *            The relative pixel button sizes.
     */
    public EmbeddedNavPanel(final DrawingAttributes buttonColors, final DrawingAttributes fadeColors,
	    final int buttonSize) {
	super();
	centerDelegate = new CenterSupport(this);
	panDelegate = new PanSupport(this);
	zoomDelegate = new ZoomSupport(this);
	// the two commands required to make this panel transparent
	setBackground(OMGraphicConstants.clear);
	setOpaque(false);

	initColors(buttonColors, fadeColors, buttonSize);

	// Checks the openmap.Latitude and openmap.Longitude properties, and
	// initializes the re-center point to that.
	final float lat = Environment.getFloat(Environment.Latitude, 0f);
	final float lon = Environment.getFloat(Environment.Longitude, 0f);
	setRecenterPoint(new Point2D.Float(lon, lat));

	layoutPanel();
    }

    protected final void initColors(final DrawingAttributes buttonColors, final DrawingAttributes fadeColors,
	    final int buttonSize) {

	fadeAttributes = fadeColors;
	liveAttributes = buttonColors;

	if (buttonSize >= 10) {
	    this.buttonSize = buttonSize;
	}

	if (fadeAttributes == null) {
	    fadeAttributes = DrawingAttributes.getDefaultClone();
	    final Color fadeColor = new Color(0xffaaaaaa);
	    fadeAttributes.setFillPaint(fadeColor);
	    fadeAttributes.setLinePaint(fadeColor.darker());
	}

	if (buttonColors == null) {
	    liveAttributes = DrawingAttributes.getDefaultClone();
	    final Color liveColor = new Color(0xDDF3F3F3);
	    liveAttributes.setFillPaint(liveColor);
	    liveAttributes.setMattingPaint(liveColor);
	    liveAttributes.setMatted(true);
	}
    }

    @Override
    public void setProperties(final String prefix, final Properties props) {
	String curPrefix;
	super.setProperties(prefix, props);
	curPrefix = PropUtils.getScopedPropertyPrefix(prefix);

	fadeAttributes.setProperties(curPrefix + FADE_ATTRIBUTES_PROPERTY, props);
	liveAttributes.setProperties(curPrefix + LIVE_ATTRIBUTES_PROPERTY, props);

	panDistance = PropUtils.floatFromProperties(props, curPrefix + PAN_DISTANCE_PROPERTY, DEFAULT_PAN_DISTANCE);

	zoomFactor = PropUtils.floatFromProperties(props, curPrefix + ZOOM_FACTOR_PROPERTY, DEFAULT_ZOOM_FACTOR);
    }

    @Override
    public Properties getProperties(final Properties props) {
	Properties curProperties;
	curProperties = super.getProperties(props);

	fadeAttributes.getProperties(curProperties);
	liveAttributes.getProperties(curProperties);

	final String prefix = PropUtils.getScopedPropertyPrefix(this);
	curProperties.put(prefix + PAN_DISTANCE_PROPERTY, String.valueOf(panDistance));
	curProperties.put(prefix + ZOOM_FACTOR_PROPERTY, String.valueOf(zoomFactor));

	return curProperties;
    }

    /**
     * TODO: This is not complete, the drawing attributes need to be separated out and scoped, so they can be set
     * individually.
     */
    @Override
    public Properties getPropertyInfo(final Properties props) {
	Properties curProperties;
	curProperties = super.getPropertyInfo(props);

	String interString;
	curProperties.put(initPropertiesProperty, PAN_DISTANCE_PROPERTY + " " + ZOOM_FACTOR_PROPERTY);

	interString = i18n.get(EmbeddedNavPanel.class, PAN_DISTANCE_PROPERTY, com.bbn.openmap.I18n.TOOLTIP,
		"Panning Distance.");
	curProperties.put(PAN_DISTANCE_PROPERTY, interString);
	interString = i18n.get(EmbeddedNavPanel.class, PAN_DISTANCE_PROPERTY, "Panning Distance");
	curProperties.put(PAN_DISTANCE_PROPERTY + LabelEditorProperty, interString);
	curProperties.put(PAN_DISTANCE_PROPERTY + ScopedEditorProperty,
		"com.bbn.openmap.util.propertyEditor.TextPropertyEditor");

	interString = i18n.get(EmbeddedNavPanel.class, ZOOM_FACTOR_PROPERTY, com.bbn.openmap.I18n.TOOLTIP,
		"Zoom Factor.");
	curProperties.put(ZOOM_FACTOR_PROPERTY, interString);
	interString = i18n.get(EmbeddedNavPanel.class, ZOOM_FACTOR_PROPERTY, "Zoom Factor");
	curProperties.put(ZOOM_FACTOR_PROPERTY + LabelEditorProperty, interString);
	curProperties.put(ZOOM_FACTOR_PROPERTY + ScopedEditorProperty,
		"com.bbn.openmap.util.propertyEditor.TextPropertyEditor");

	return curProperties;
    }

    protected final void layoutPanel() {

	removeAll();

	final int projStackButtonSize = (int) (buttonSize * 1.25);
	final int rosetteButtonSize = buttonSize;
	final int zoomButtonSize = buttonSize;
	setLayout(new GridBagLayout());

	final GridBagConstraints layoutConstraints = new GridBagConstraints();
	int baseY = 0;

	final IconPartList ipl;

	final IconPart bigArrow = OpenMapAppPartCollection.BIG_ARROW.getIconPart();
	// bigArrow.setRenderingAttributes(fadeAttributes)
	backDimIcon = OMIconFactory.getIcon(projStackButtonSize, projStackButtonSize, bigArrow, null,
		Length.DECIMAL_DEGREE.toRadians(270.0));
	// bigArrow.setRenderingAttributes(liveAttributes)
	backIcon = OMIconFactory.getIcon(projStackButtonSize, projStackButtonSize, bigArrow, null,
		Length.DECIMAL_DEGREE.toRadians(270.0));
	backProjectionButton = makeButton(backDimIcon, Messages.getMessage("map.previous_observation"),
		(final ActionEvent event) -> firePropertyChange("SELECT_PREVIOUS_OBSERVATION_MAP", null, ""));

	// backProjectionButton.setActionCommand(ProjectionStack.BackProjCmd)

	// bigArrow.setRenderingAttributes(fadeAttributes)
	forwardDimIcon = OMIconFactory.getIcon(projStackButtonSize, projStackButtonSize, bigArrow, null,
		Length.DECIMAL_DEGREE.toRadians(90.0));
	// bigArrow.setRenderingAttributes(liveAttributes)
	forwardIcon = OMIconFactory.getIcon(projStackButtonSize, projStackButtonSize, bigArrow, null,
		Length.DECIMAL_DEGREE.toRadians(90.0));
	forwardProjectionButton = makeButton(forwardDimIcon, Messages.getMessage("map.next_observation"),
		(final ActionEvent event) -> firePropertyChange("SELECT_NEXT_OBSERVATION_MAP", null, ""));

	// final JPanel projStackButtonPanel = new JPanel();
	// projStackButtonPanel.setOpaque(false);
	// projStackButtonPanel.setBackground(CONTROL_BACKGROUND);
	// projStackButtonPanel.add(backProjectionButton);
	// projStackButtonPanel.add(forwardProjectionButton);
	//
	// layoutConstraints.anchor = GridBagConstraints.CENTER;
	// layoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
	// layoutConstraints.gridy = baseY++;
	//
	// add(projStackButtonPanel, layoutConstraints);

	// final JPanel rosette = new JPanel();
	// final GridBagLayout internalGridbag = new GridBagLayout();
	// final GridBagConstraints c2 = new GridBagConstraints();
	// rosette.setLayout(internalGridbag);
	//
	// rosette.setOpaque(false);
	// rosette.setBackground(CONTROL_BACKGROUND);
	//
	// c2.gridx = 0;
	// c2.gridy = 0;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI.getIconPart(), liveAttributes, rosetteButtonSize,
	// 0.0, "Pan Northwest", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(-45f, panDistance);
	// }
	// }), c2);
	// c2.gridx = 1;
	// rosette.add(makeButton(OpenMapAppPartCollection.MED_ARROW.getIconPart(), liveAttributes, rosetteButtonSize,
	// 0.0, "Pan North", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(0f, panDistance);
	// }
	// }));
	// c2.gridx = 2;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI.getIconPart(), liveAttributes, rosetteButtonSize,
	// 90.0, "Pan Northeast", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(45f, panDistance);
	// }
	// }), c2);
	//
	// c2.gridx = 0;
	// c2.gridy = 1;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.MED_ARROW.getIconPart(), liveAttributes, rosetteButtonSize, 270.0,
	// "Pan West", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(-90f, panDistance);
	// }
	// }), c2);
	// c2.gridx = 1;
	// ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
	// ipl.add(OpenMapAppPartCollection.DOT.getIconPart());
	// rosette.add(makeButton(ipl, liveAttributes, rosetteButtonSize, 0.0, "Center Map", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// final Point2D centerPnt = getRecenterPoint();
	// if (centerPnt == null) {
	// centerDelegate.fireCenter(0, 0);
	// } else {
	// centerDelegate.fireCenter(centerPnt.getY(), centerPnt.getX());
	// }
	// }
	// }), c2);
	// c2.gridx = 2;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.MED_ARROW.getIconPart(), liveAttributes, rosetteButtonSize, 90.0,
	// "Pan East", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(90f, panDistance);
	// }
	// }), c2);
	//
	// c2.gridx = 0;
	// c2.gridy = 2;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI.getIconPart(), liveAttributes, rosetteButtonSize,
	// 270.0, "Pan Southwest", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(-135f, panDistance);
	// }
	// }), c2);
	// c2.gridx = 1;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.MED_ARROW.getIconPart(), liveAttributes, rosetteButtonSize, 180.0,
	// "Pan South", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(180f, panDistance);
	// }
	// }), c2);
	// c2.gridx = 2;
	// rosette.add(
	// makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI.getIconPart(), liveAttributes, rosetteButtonSize,
	// 180.0, "Pan Southeast", new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// panDelegate.firePan(135f, panDistance);
	// }
	// }), c2);
	//
	// layoutConstraints.gridy = baseY++;
	// // add(rosette, layoutConstraints);

	// layoutConstraints.gridy = baseY++;
	// layoutConstraints.insets = new Insets(6, 0, 6, 0);
	// ipl = new IconPartList();
	// // ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
	// ipl.add(OpenMapAppPartCollection.BIG_BOX.getIconPart());
	// add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, Messages.getMessage("map.zoom_initial"),
	// new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// reinitZoom();
	// }
	// }), layoutConstraints);
	//
	// layoutConstraints.gridy = baseY++;
	// layoutConstraints.insets = new Insets(0, 0, 0, 0);

	// layoutConstraints.gridy = baseY++;
	// layoutConstraints.insets = new Insets(6, 0, 6, 0);
	// ipl = new IconPartList();
	// // ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart())
	// ipl.add(OpenMapAppPartCollection.PLUS.getIconPart());
	// add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, Messages.getMessage("map.zoom_in"),
	// new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// zoomDelegate.fireZoom(ZoomEvent.RELATIVE, 1.0f / zoomFactor);
	// }
	// }), layoutConstraints);
	//
	// layoutConstraints.gridy = baseY++;
	// layoutConstraints.insets = new Insets(0, 0, 0, 0);
	// // add(makeScaleSlider(liveAttributes), layoutConstraints)

	// layoutConstraints.gridy = baseY++;
	// layoutConstraints.insets = new Insets(6, 0, 6, 0);
	// ipl = new IconPartList();
	// // ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart())
	// ipl.add(OpenMapAppPartCollection.MINUS.getIconPart());
	// add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, Messages.getMessage("map.zoom_out"),
	// new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent event) {
	// zoomDelegate.fireZoom(ZoomEvent.RELATIVE, zoomFactor);
	// }
	// }), layoutConstraints);

	layoutConstraints.gridy = baseY++;
	layoutConstraints.insets = new Insets(0, 10, 6, 0);
	ipl = new IconPartList();
	// ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
	ipl.add(OpenMapAppPartCollection.BIG_BOX.getIconPart());
	add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, Messages.getMessage("map.full_screen"),
		(final ActionEvent event) -> firePropertyChange("TOGGLE_FULL_SCREEN_FOR_MAP", null, "")),
		layoutConstraints);

	layoutConstraints.gridy = baseY++;
	layoutConstraints.insets = new Insets(0, 0, 0, 0);

	// We could drop this, but I think it's needed to play well with other
	// containers when needed.
	layoutConstraints.fill = GridBagConstraints.VERTICAL;
	layoutConstraints.gridy = baseY++;
	layoutConstraints.weighty = 1;
	final JPanel filler = new JPanel();
	filler.setOpaque(false);
	filler.setBackground(OMGraphicConstants.clear);
	add(filler, layoutConstraints);

	setMinimumSize(
		new Dimension(75, projStackButtonSize + (3 * rosetteButtonSize) + (2 * zoomButtonSize) + 24 + 200));
    }

    public Point2D getRecenterPoint() {
	return recenterPoint;
    }

    public final void setRecenterPoint(final Point2D recenterPoint) {
	this.recenterPoint = recenterPoint;
    }

    public float getPanDistance() {
	return panDistance;
    }

    public void setPanDistance(final float panDistance) {
	this.panDistance = panDistance;
    }

    public float getZoomFactor() {
	return zoomFactor;
    }

    public void setZoomFactor(final float zoomFactor) {
	this.zoomFactor = zoomFactor;
    }

    protected JButton makeButton(final IconPart iconPart, final DrawingAttributes da, final int size,
	    final double ddRot, final String tooltip, final ActionListener ac) {
	iconPart.setRenderingAttributes(da);
	return makeButton(OMIconFactory.getIcon(size, size, iconPart, null, Length.DECIMAL_DEGREE.toRadians(ddRot)),
		tooltip, ac);
    }

    protected JButton makeButton(final ImageIcon icon, final String toolTip, final ActionListener listener) {
	final JButton button = makeButton(icon, toolTip);
	button.addActionListener(listener);
	// KNOX -- don't let buttons get focus and add transparency listener
	button.setFocusable(false);
	button.addMouseListener(new NavPanelMouseListener());
	return button;
    }

    protected JButton makeButton(final ImageIcon icon, final String toolTip) {
	final JButton button = new JButton(icon);
	// MAGIC: required to make background transparent!
	button.setBackground(CONTROL_BACKGROUND);
	button.setBorder(null);
	button.setMargin(new Insets(0, 0, 0, 0));
	// No surprise: also required to make background transparent.
	button.setOpaque(false);
	button.setBorderPainted(false);
	button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
	button.setToolTipText(toolTip);
	// KNOX -- don't let buttons get focus and add transparency listener
	button.setFocusable(false);
	button.addMouseListener(new NavPanelMouseListener());
	return button;
    }

    protected JComponent makeScaleSlider(final DrawingAttributes da) {
	slider = new JSlider(SwingConstants.VERTICAL, 0, SLIDER_MAX, SLIDER_MAX);
	slider.setUI(new NavPanelSliderUI(slider, (Color) da.getFillPaint()));
	// MAGIC: required to make background transparent!
	slider.setBackground((Color) da.getFillPaint());
	slider.setBorder(BorderFactory.createLineBorder((Color) da.getFillPaint(), 1));
	slider.setForeground((Color) da.getFillPaint());
	slider.setInverted(true);
	slider.setMinorTickSpacing(1);
	// No surprise: also required to make background transparent.
	slider.setOpaque(false);
	slider.setPaintTicks(true);
	slider.setSnapToTicks(true);
	slider.addChangeListener((final ChangeEvent event) -> {
	    // Need the check to avoid resetting the map scale if the window
	    // is resized. Only want this to happen of someone is moving the
	    // slider lever.
	    if (slider.getValueIsAdjusting()) {
		changeMapScale(slider.getValue());
	    }
	});
	// KNOX -- don't let slider get focus and add transparency listener
	slider.setFocusable(false);
	slider.addMouseListener(new NavPanelMouseListener());
	return slider;
    }

    protected void changeMapScale(final int sliderValue) {
	final float newScale = sliderToScale(sliderValue);

	if (map.getScale() != newScale) {
	    map.setScale(newScale);
	}
    }

    protected void changeSliderValue(final Projection projection) {
	final int newValue = scaleToSlider(projection.getScale());

	if (slider.getValue() != newValue) {
	    slider.setValue(newValue);
	}
    }

    protected float sliderToScale(final int sliderValue) {
	return (float) (getMapMaxScale() / Math.pow(2, (double) SLIDER_MAX - sliderValue));
    }

    protected int scaleToSlider(final float mapScale) {
	return SLIDER_MAX - logBase2(getMapMaxScale() / mapScale);
    }

    /** Returns the largest integer n, such that 2^n &lt;= the specified number. */
    public static final int logBase2(final double number) {
	double curNumber = number;
	int log = 0;

	while (curNumber > 1) {
	    curNumber = Math.floor(curNumber / 2);
	    ++log;
	}

	return log;
    }

    public Color getScaleSliderBackground() {
	return slider.getBackground();
    }

    public void setScaleSliderBackground(final Color sliderBackground) {
	slider.setBackground(sliderBackground);
    }

    public Color getScaleSliderForeground() {
	return slider.getForeground();
    }

    public void setScaleSliderForeground(final Color sliderForeground) {
	slider.setForeground(sliderForeground);
    }

    private final float getMapMaxScale() {
	return map.getProjection().getMaxScale();
    }

    // OMComponentPanel
    @Override
    public void findAndInit(final Object someObject) {
	if (someObject instanceof MapBean) {
	    map = (MapBean) someObject;
	    map.addProjectionListener(this);
	}
	if (someObject instanceof PanListener) {
	    addPanListener((PanListener) someObject);
	}
	if (someObject instanceof CenterListener) {
	    addCenterListener((CenterListener) someObject);
	}
	if (someObject instanceof ZoomListener) {
	    addZoomListener((ZoomListener) someObject);
	}
	if (someObject instanceof ProjectionStack) {
	    ((ProjectionStack) someObject).addProjectionStackTrigger(this);
	}
    }

    // OMComponentPanel
    @Override
    public void findAndUndo(final Object someObject) {
	if (someObject instanceof MapBean) {
	    map.removeProjectionListener(this);
	}
	if (someObject instanceof PanListener) {
	    removePanListener((PanListener) someObject);
	}
	if (someObject instanceof CenterListener) {
	    removeCenterListener((CenterListener) someObject);
	}
	if (someObject instanceof ZoomListener) {
	    removeZoomListener((ZoomListener) someObject);
	}
	if (someObject instanceof ProjectionStack) {
	    ((ProjectionStack) someObject).removeProjectionStackTrigger(this);
	}
    }

    public synchronized void addCenterListener(final CenterListener listener) {
	centerDelegate.add(listener);
    }

    public synchronized void removeCenterListener(final CenterListener listener) {
	centerDelegate.remove(listener);
    }

    public synchronized void addPanListener(final PanListener listener) {
	panDelegate.add(listener);
    }

    public synchronized void removePanListener(final PanListener listener) {
	panDelegate.remove(listener);
    }

    public synchronized void addZoomListener(final ZoomListener listener) {
	zoomDelegate.add(listener);
    }

    public synchronized void removeZoomListener(final ZoomListener listener) {
	zoomDelegate.remove(listener);
    }

    // ProjectionListener
    @Override
    public void projectionChanged(final ProjectionEvent event) {
	changeSliderValue(event.getProjection());
    }

    /** Adds a listener for events that shift the Projection stack. */
    // ProjectionStackTrigger
    @Override
    public void addActionListener(final ActionListener listener) {
	forwardProjectionButton.addActionListener(listener);
	backProjectionButton.addActionListener(listener);
    }

    /** Removes the listener for events that shift the Projection stack. */
    // ProjectionStackTrigger
    @Override
    public void removeActionListener(final ActionListener listener) {
	forwardProjectionButton.addActionListener(listener);
	backProjectionButton.addActionListener(listener);
    }

    /**
     * Respond to changes in the contents of the forward and back projection stacks.
     *
     * @param haveBackProjections
     *            true if there is at least one back projection available
     * @param haveForwardProjections
     *            true if there is at least one forward projection available
     */
    // ProjectionStackTrigger
    @Override
    public void updateProjectionStackStatus(final boolean haveBackProjections, final boolean haveForwardProjections) {
	forwardProjectionButton.setIcon(haveForwardProjections ? forwardIcon : forwardDimIcon);
	backProjectionButton.setIcon(haveBackProjections ? backIcon : backDimIcon);
	forwardProjectionButton.setEnabled(haveForwardProjections);
	backProjectionButton.setEnabled(haveBackProjections);
    }

    @Override
    public void paint(final Graphics g) {
	if (ac != null) {
	    final Graphics2D g2 = (Graphics2D) g.create();
	    g2.setComposite(ac);
	    super.paint(g2);
	    g2.dispose();
	} else {
	    super.paint(g);
	}
    }

    public void setTransparency(final float transparency) {
	float curTransparency = transparency;
	if (ac != null) {
	    if (curTransparency > maxTransparency) {
		curTransparency = maxTransparency;
	    }

	    ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, curTransparency);
	    repaint();
	}
    }

    public void setMinimumTransparency(final float minTransparency) {
	this.minTransparency = minTransparency;
    }

    public void setSemiTransparency(final float semiTransparency) {
	this.semiTransparency = semiTransparency;
    }

    public DrawingAttributes getFadeAttributes() {
	return fadeAttributes;
    }

    public void setFadeAttributes(final DrawingAttributes fadeAttributes) {
	this.fadeAttributes = fadeAttributes;
    }

    public DrawingAttributes getLiveAttributes() {
	return liveAttributes;
    }

    public void setLiveAttributes(final DrawingAttributes liveAttributes) {
	this.liveAttributes = liveAttributes;
    }

    public AlphaComposite getAc() {
	return ac;
    }

    public void setAc(final AlphaComposite ac) {
	this.ac = ac;
    }

    public MapBean getMap() {
	return map;
    }

    // KNOX -- using this to paint ticks on slider
    private class NavPanelSliderUI extends BasicSliderUI {
	Color sliderTickColor = Color.white;

	public NavPanelSliderUI(final JSlider slider, final Color tickColor) {
	    super(slider);
	    sliderTickColor = tickColor;
	}

	@Override
	protected void paintMinorTickForVertSlider(final Graphics g, final Rectangle tickBounds, final int y) {
	    g.setColor(sliderTickColor);
	    super.paintMinorTickForVertSlider(g, tickBounds, y);
	}

    }

    // KNOX -- using this to change level of transparency when mousing over
    // buttons/slider
    private class NavPanelMouseListener extends MouseAdapter {
	@Override
	public void mouseEntered(final MouseEvent e) {
	    if (ac.getAlpha() < semiTransparency) {
		setTransparency(semiTransparency);
		getTopLevelAncestor().repaint();
	    }
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	    if (ac.getAlpha() > minTransparency) {
		setTransparency(minTransparency);
		getTopLevelAncestor().repaint();
	    }
	}
    }

    @SuppressWarnings("unused")
    private void reinitZoom() {
	if (map != null) {
	    final Projection oldProj = map.getProjection();
	    final ProjectionFactory projFactory = map.getProjectionFactory();
	    final Class<? extends Projection> projClass = com.bbn.openmap.proj.Mercator.class;

	    final Projection newProj = projFactory.makeProjection(projClass, new Point2D.Float(0, 0), Float.MAX_VALUE,
		    oldProj.getWidth(), oldProj.getHeight());

	    map.setProjection(newProj);
	}
    }
}
