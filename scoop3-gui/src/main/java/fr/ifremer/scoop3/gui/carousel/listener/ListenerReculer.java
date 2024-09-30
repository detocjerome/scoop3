package fr.ifremer.scoop3.gui.carousel.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import fr.ifremer.scoop3.gui.carousel.Carousel;

public class ListenerReculer implements MouseListener {

    private final Carousel caroussel;

    public ListenerReculer(final Carousel c) {
	this.caroussel = c;
    }

    @Override
    public void mouseReleased(final MouseEvent arg0) {
	caroussel.reculer();
    }

    @Override
    public void mousePressed(final MouseEvent arg0) {
	// empty method
    }

    @Override
    public void mouseExited(final MouseEvent arg0) {
	// empty method
    }

    @Override
    public void mouseEntered(final MouseEvent arg0) {
	// empty method
    }

    @Override
    public void mouseClicked(final MouseEvent arg0) {
	// empty method
    }

}
