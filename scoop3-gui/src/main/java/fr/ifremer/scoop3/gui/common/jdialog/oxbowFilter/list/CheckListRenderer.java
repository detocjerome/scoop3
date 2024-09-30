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

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("rawtypes")
public class CheckListRenderer extends JCheckBox implements ListCellRenderer, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border SAFE_NO_FOCUS_BORDER = NO_FOCUS_BORDER; // may change in the feature

    /**
     * Constructs a default renderer object for an item in a list.
     */
    public CheckListRenderer() {
	super();
	setOpaque(true);
	setBorder(getNoFocusBorder());
    }

    private static Border getNoFocusBorder() {
	if (System.getSecurityManager() != null) {
	    return SAFE_NO_FOCUS_BORDER;
	} else {
	    return NO_FOCUS_BORDER;
	}
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	    boolean isSelected, final boolean cellHasFocus) {

	setComponentOrientation(list.getComponentOrientation());

	Color bg = null;
	Color fg = null;

	final JList.DropLocation dropLocation = list.getDropLocation();
	if ((dropLocation != null) && !dropLocation.isInsert() && (dropLocation.getIndex() == index)) {

	    bg = UIManager.getColor("List.dropCellBackground");
	    fg = UIManager.getColor("List.dropCellForeground");

	    isSelected = true;
	}

	if (isSelected) {
	    setBackground(bg == null ? list.getSelectionBackground() : bg);
	    setForeground(fg == null ? list.getSelectionForeground() : fg);
	} else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}

	if (value instanceof Icon) {
	    setIcon((Icon) value);
	    setText("");
	} else {
	    setIcon(null);
	    setText(getObjectAsText(value));
	}

	setSelected(isChecked(list, index));

	setEnabled(list.isEnabled());
	setFont(list.getFont());

	Border border = null;
	if (cellHasFocus) {
	    if (isSelected) {
		border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
	    }
	    if (border == null) {
		border = UIManager.getBorder("List.focusCellHighlightBorder");
	    }
	} else {
	    border = getNoFocusBorder();
	}
	setBorder(border);

	return this;
    }

    protected String getObjectAsText(final Object obj) {
	return (obj == null) ? "" : obj.toString();
    }

    private boolean isChecked(final JList list, final int index) {

	if (list.getModel() instanceof ICheckListModel<?>) {
	    return ((ICheckListModel<?>) list.getModel()).isCheckedIndex(index);
	} else {
	    return false;
	}

    }

    /**
     * @return true if the background is opaque and differs from the JList's background; false otherwise
     */
    @Override
    public boolean isOpaque() {
	final Color back = getBackground();
	Component p = getParent();
	if (p != null) {
	    p = p.getParent();
	}
	// p should now be the JList.
	final boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
	return !colorMatch && super.isOpaque();
    }

    @Override
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {

	if ("text".equals(propertyName)
		|| (("font".equals(propertyName) || "foreground".equals(propertyName)) && (oldValue != newValue)
			&& (getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null))) {

	    super.firePropertyChange(propertyName, oldValue, newValue);
	}
    }

    // Methods below are overridden for performance reasons.

    @Override
    public void validate() {
	// empty method
    }

    @Override
    public void invalidate() {
	// empty method
    }

    @Override
    public void repaint() {
	// empty method
    }

    @Override
    public void revalidate() {
	// empty method
    }

    @Override
    public void repaint(final long tm, final int x, final int y, final int width, final int height) {
	// empty method
    }

    @Override
    public void repaint(final Rectangle r) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final char oldValue, final char newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final short oldValue, final short newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final long oldValue, final long newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final float oldValue, final float newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final double oldValue, final double newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
	// empty method
    }

    @SuppressWarnings("serial")
    public static class UIResource extends DefaultListCellRenderer implements javax.swing.plaf.UIResource {
	// empty method
    }

}