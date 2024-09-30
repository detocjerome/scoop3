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

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.table;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Common class for customizing table header renderer without loosing its L&F
 *
 * Created on Aug 10, 2010
 *
 * @author Eugene Ryzhikov
 *
 */
public class TableHeaderRenderer extends JComponent implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	    final boolean hasFocus, final int row, final int column) {

	// returns component used for default header rendering
	// makes it independent on current L&F

	return table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, isSelected,
		hasFocus, row, column);

    }

    // following methods are overriden for performance reasons

    @Override
    public void validate() {
	// empty method
    }

    @Override
    public void revalidate() {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
	// empty method
    }

    @Override
    public void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
	// empty method
    }

}
