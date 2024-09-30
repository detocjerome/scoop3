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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProjMapBeanKeyListener.java,v $
// $RCSfile: ProjMapBeanKeyListener.java,v $
// $Revision: 1.5 $
// $Date: 2006/02/27 23:19:31 $
// $Author: dietrick $
//
// **********************************************************************

package fr.ifremer.scoop3.map.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.event.MapBeanKeyListener;
import com.bbn.openmap.event.PanSupport;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.proj.ProjectionStack;

import fr.ifremer.scoop3.map.layer.ObservationLayer;

/**
 * The ObservationMapBeanKeyListener is a KeyListener that gets events when the MapBean has focus, and responds to
 * certain keys by changing the projection. The arrow keys pan the map, and 'z' zooms in. Shift-z zooms out. The less
 * than/comma key tells a projection stack to go back to the last projection, and the greater than/period tells it to go
 * to the next projection. The MapBean has to have focus for these to work which is usually gained by clicking on the
 * map.
 */
public class ObservationMapBeanKeyListener extends MapBeanKeyListener {

    /**
     * Default Zoom In Factor is 2, meaning that the scale number will be cut in half to zoom in and doubled to zoom
     * out.
     */
    protected float zoomFactor = 2f;

    protected PanSupport panners;
    protected ZoomSupport zoomers;
    protected ListenerSupport<ActionListener> projListeners;
    protected ObservationLayer observationLayer;

    /**
     * Constructor
     */
    public ObservationMapBeanKeyListener() {
	panners = new PanSupport(this);
	zoomers = new ZoomSupport(this);
	projListeners = new ListenerSupport<ActionListener>(this);
    }

    /**
     * Constructor
     *
     * @param observationLayer
     */
    public ObservationMapBeanKeyListener(final ObservationLayer observationLayer) {
	super();
	this.observationLayer = observationLayer;
    }

    @Override
    public void keyReleased(final KeyEvent e) {

	final int keyCode = e.getKeyCode();

	switch (keyCode) {
	case KeyEvent.VK_LEFT:
	case KeyEvent.VK_KP_LEFT:
	    observationLayer.selectPreviousOMPoint();
	    break;
	case KeyEvent.VK_RIGHT:
	case KeyEvent.VK_KP_RIGHT:
	    observationLayer.selectNextOMPoint();
	    break;
	default:
	    break;
	}
    }

    /**
     * In addition to the super.setMapBean() method, also sets the MapBean as a zoom and pan listener.
     */
    @Override
    public void setMapBean(final MapBean map) {
	if (mapBean != null) {
	    panners.remove(map);
	    zoomers.remove(map);
	}

	super.setMapBean(map);

	if (mapBean != null) {
	    panners.add(map);
	    zoomers.add(map);
	}
    }

    /**
     * Called by keyReleased when the period/comma keys are pressed.
     */
    protected void fireProjectionStackEvent(final String command) {
	if (projListeners.isEmpty()) {
	    return;
	}

	final ActionEvent event = new ActionEvent(this, 0, command);
	for (final ActionListener listener : projListeners) {
	    listener.actionPerformed(event);
	}

    }

    /**
     * Add an ActionListener for events that trigger events to shift the Projection stack.
     */
    public void addActionListener(final ActionListener al) {
	projListeners.add(al);
    }

    /**
     * Remove an ActionListener that receives events that trigger events to shift the Projection stack.
     */
    public void removeActionListener(final ActionListener al) {
	projListeners.remove(al);
    }

    /**
     * In addition to the MapBean, find a projection stack so the less than/greater than works on that.
     */
    @Override
    public void findAndInit(final Object someObj) {
	super.findAndInit(someObj);
	if (someObj instanceof ProjectionStack) {
	    addActionListener((ActionListener) someObj);
	}
    }

    @Override
    public void findAndUndo(final Object someObj) {
	super.findAndUndo(someObj);
	if (someObj instanceof ProjectionStack) {
	    removeActionListener((ActionListener) someObj);
	}
    }

}