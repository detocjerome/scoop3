package fr.ifremer.scoop3.gui.carousel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.ifremer.scoop3.infra.logger.SC3Logger;

public class Entete extends JPanel {

    private static final long serialVersionUID = 1811675415332771068L;
    private final Color couleurHaut = new Color(175, 175, 175);
    private final Color couleurBas = new Color(235, 235, 235);
    private final ImageIcon imageGauche = new ImageIcon(getClass().getResource("gauche.png"));
    private final ImageIcon imageDroite = new ImageIcon(getClass().getResource("droite.png"));
    private final ImageIcon imageGaucheS = new ImageIcon(getClass().getResource("gaucheS.png"));
    private final ImageIcon imageDroiteS = new ImageIcon(getClass().getResource("droiteS.png"));
    private final JLabel gauche;
    private final JLabel droite;

    public Entete() {
	setPreferredSize(new Dimension(100, 30));
	gauche = new JLabel(imageGauche);
	gauche.addMouseListener(new MouseListener() {
	    @Override
	    public void mouseReleased(final MouseEvent arg0) {
		// empty method
	    }

	    @Override
	    public void mousePressed(final MouseEvent arg0) {
		// empty method
	    }

	    @Override
	    public void mouseExited(final MouseEvent arg0) {
		gauche.setIcon(imageGauche);
		setCursor(Cursor.getDefaultCursor());

	    }

	    @Override
	    public void mouseEntered(final MouseEvent arg0) {
		gauche.setIcon(imageGaucheS);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

	    }

	    @Override
	    public void mouseClicked(final MouseEvent arg0) {
		// empty method
	    }
	});
	droite = new JLabel(imageDroite);
	droite.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseReleased(final MouseEvent arg0) {
		// empty method
	    }

	    @Override
	    public void mousePressed(final MouseEvent arg0) {
		// empty method
	    }

	    @Override
	    public void mouseExited(final MouseEvent arg0) {
		droite.setIcon(imageDroite);
		setCursor(Cursor.getDefaultCursor());
	    }

	    @Override
	    public void mouseEntered(final MouseEvent arg0) {
		droite.setIcon(imageDroiteS);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

	    }

	    @Override
	    public void mouseClicked(final MouseEvent arg0) {
		// empty method
	    }
	});
	setLayout(null);
	gauche.setBounds(getWidth() - 32, (getHeight() / 2) - 8, 16, 16);
	droite.setBounds(getWidth() - 16, (getHeight() / 2) - 8, 16, 16);
	add(gauche);
	add(droite);
    }

    public JLabel getGauche() {
	return gauche;
    }

    public JLabel getDroite() {
	return droite;
    }

    @Override
    public void paint(final Graphics g) {
	gauche.setBounds(getWidth() - 42, (getHeight() / 2) - 8, 16, 16);
	droite.setBounds(getWidth() - 21, (getHeight() / 2) - 8, 16, 16);
	Paint paint;
	Graphics2D g2d;
	if (g instanceof Graphics2D) {
	    g2d = (Graphics2D) g;
	} else {
	    SC3Logger.LOGGER.debug("Error");
	    return;
	}
	paint = new GradientPaint(0, 0, couleurHaut, 0, getHeight(), couleurBas);
	g2d.setPaint(paint);
	g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
	g2d.setColor(couleurHaut);
	g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
	super.paintComponents(g);
    }

}
