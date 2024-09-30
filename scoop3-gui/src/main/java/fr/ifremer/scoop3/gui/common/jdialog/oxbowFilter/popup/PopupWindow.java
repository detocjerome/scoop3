/*
 * Copyright (c) 2009-2011, EzWare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.Neither the name of the
 * EzWare nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public abstract class PopupWindow {

    private final JPopupMenu menu;
    private Dimension defaultSize = new Dimension(100, 100);

    protected PopupWindow(final boolean resizable) {
	menu = new ResizablePopupMenu(resizable) {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		if (menu.getComponentCount() == 0) {
		    final JComponent content = buildContent();
		    defaultSize = content.getPreferredSize();

		    menu.add(content);

		}
		beforeShow();
	    }

	    @Override
	    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		beforeHide();
	    }

	};
    }

    public final Dimension getDefaultSize() {
	return defaultSize;
    }

    public final Dimension getPreferredSize() {
	return menu.getPreferredSize();
    }

    public final void setPreferredSize(final Dimension preferredSize) {
	menu.setPreferredSize(preferredSize);
    }

    /**
     * Override this method to add content yo the owner. This method is only executed when owner has no subcomponents
     *
     * @param owner
     */
    protected abstract JComponent buildContent();

    /**
     * Shows Popup in predefined location
     *
     * @param invoker
     * @param x
     * @param y
     */
    public void show(final Component invoker, final int x, final int y) {
	menu.show(invoker, x, y);
    }

    /**
     * Shows popup in predefined location
     *
     * @param invoker
     * @param location
     */
    public void show(final Component invoker, final Point location) {
	show(invoker, location.x, location.y);
    }

    /**
     * Hides popup
     */
    public final void hide() {
	menu.setVisible(false);
    }

    protected void beforeShow() {
    }

    protected void beforeHide() {
    }

    /**
     * Simple action to for the popup window. To use - override perform method.
     *
     * Created on Feb 4, 2011
     *
     * @author Eugene Ryzhikov
     *
     */
    public class CommandAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public CommandAction(final String name, final Icon icon) {
	    super(name, icon);

	    if (icon != null) {
		putValue(Action.SHORT_DESCRIPTION, name);
		putValue(Action.NAME, null);
	    }

	}

	public CommandAction(final String name) {
	    super(name);
	}

	@Override
	public final void actionPerformed(final ActionEvent e) {
	    if (perform()) {
		hide();
	    }
	}

	/**
	 * Preforms action
	 *
	 * @return true if popup should be closed
	 */
	protected boolean perform() {
	    return true;
	}
    }

}

class ResizablePopupMenu extends JPopupMenu implements PopupMenuListener {

    private static final long serialVersionUID = 1L;

    private static final int DOT_SIZE = 2;
    private static final int DOT_START = 2;
    private static final int DOT_STEP = 4;

    private final boolean resizable;

    public ResizablePopupMenu(final boolean resizable) {
	super();
	this.resizable = resizable;
	if (resizable) {
	    PopupMenuResizer.decorate(this);
	}
	addPopupMenuListener(this);
    }

    @Override
    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
	// empty method
    }

    @Override
    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
	// empty method
    }

    @Override
    public void popupMenuCanceled(final PopupMenuEvent e) {
	// empty method
    }

    @Override
    public void paintChildren(final Graphics g) {
	super.paintChildren(g);
	if (resizable) {
	    drawResizer(g);
	}
    }

    private void drawResizer(final Graphics g) {

	final int x = getWidth() - 2;
	final int y = getHeight() - 2;

	final Graphics g2 = g.create();

	try {
	    for (int dy = DOT_START, j = 2; j > 0; j--, dy += DOT_STEP) {
		for (int dx = DOT_START, i = 0; i < j; i++, dx += DOT_STEP) {
		    drawDot(g2, x - dx, y - dy);
		}
	    }
	} finally {
	    g2.dispose();
	}

    }

    private void drawDot(final Graphics g, final int x, final int y) {
	g.setColor(Color.WHITE);
	g.fillRect(x, y, DOT_SIZE, DOT_SIZE);
	g.setColor(Color.LIGHT_GRAY);
	g.fillRect(x - 1, y - 1, DOT_SIZE, DOT_SIZE);
    }

}

/**
 * Allows to resize popup with the mouse.
 *
 * Created on Aug 6, 2010
 *
 * @author exr0bs5
 *
 */
final class PopupMenuResizer extends MouseAdapter {

    private final JPopupMenu menu;

    private static final int REZSIZE_SPOT_SIZE = 10;

    private Point mouseStart = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

    private Dimension startSize;

    private boolean isResizing = false;

    public static void decorate(final JPopupMenu menu) {
	new PopupMenuResizer(menu);
    }

    private PopupMenuResizer(final JPopupMenu menu) {
	this.menu = menu;
	this.menu.setLightWeightPopupEnabled(true);
	menu.addMouseListener(this);
	menu.addMouseMotionListener(this);
    }

    private boolean isInResizeSpot(final Point point) {

	if (point == null) {
	    return false;
	}

	final Rectangle resizeSpot = new Rectangle(menu.getWidth() - REZSIZE_SPOT_SIZE,
		menu.getHeight() - REZSIZE_SPOT_SIZE, REZSIZE_SPOT_SIZE, REZSIZE_SPOT_SIZE);

	return resizeSpot.contains(point);

    }

    @Override
    public void mouseMoved(final MouseEvent e) {

	menu.setCursor(Cursor
		.getPredefinedCursor(isInResizeSpot(e.getPoint()) ? Cursor.SE_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private Point toScreen(final MouseEvent e) {

	final Point p = e.getPoint();
	SwingUtilities.convertPointToScreen(p, e.getComponent());
	return p;

    }

    @Override
    public void mousePressed(final MouseEvent e) {
	mouseStart = toScreen(e);
	startSize = menu.getSize();
	isResizing = isInResizeSpot(e.getPoint());
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
	mouseStart = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
	isResizing = false;
    }

    @Override
    public void mouseDragged(final MouseEvent e) {

	if (!isResizing) {
	    return;
	}

	final Point p = toScreen(e);

	final int dx = p.x - mouseStart.x;
	final int dy = p.y - mouseStart.y;

	final Dimension minDim = menu.getMinimumSize();
	// Dimension maxDim = menu.getMaximumSize();
	final Dimension newDim = new Dimension(startSize.width + dx, startSize.height + dy);

	if ((newDim.width >= minDim.width)
		&& (newDim.height >= minDim.height /*
						    * && newDim.width <= maxDim.width && newDim.height <= maxDim.height
						    */)) {
	    menu.setPopupSize(newDim);
	}

    }
}
