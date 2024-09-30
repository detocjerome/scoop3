package fr.ifremer.scoop3.gui.carousel;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JPanel;

public class DriverPanel extends JPanel {

    transient Image backgroundImage;

    public DriverPanel(final GridLayout gridLayout, final Image image) {
	// heritage
	super(gridLayout);
	this.backgroundImage = image;

    }

    @Override
    public void paint(final Graphics g) {
	g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }

}
