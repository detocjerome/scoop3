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

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.misc;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JTextField;

/**
 * A text field with search symbol painted to indicate that it is used as search field
 * 
 * @author Eugene Ryzhikov
 * 
 */
public class JSearchTextField extends JTextField {

    private static final String ICON_NAME = "icons/oxbow/search.png";
    private static final long serialVersionUID = 1L;

    private static ImageIcon icon;

    private static Image getScaledImage(final int size) {

	if (icon == null) {
	    icon = new ImageIcon(JSearchTextField.class.getClassLoader().getResource(ICON_NAME));
	}
	return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)).getImage();
    }

    private static int pad = 4;
    private static int pad2 = pad * 2;

    @Override
    public void paint(final Graphics g) {
	super.paint(g);
	final int size = getHeight() - pad2;
	g.drawImage(getScaledImage(size), getWidth() - size - pad, pad, null);
    }

}
