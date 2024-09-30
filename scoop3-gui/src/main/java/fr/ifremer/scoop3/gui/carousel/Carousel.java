package fr.ifremer.scoop3.gui.carousel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import fr.ifremer.scoop3.gui.carousel.listener.ListenerAvancer;
import fr.ifremer.scoop3.gui.carousel.listener.ListenerReculer;


/**
 * <b>Classe definissant le Carroussel permettant de faire d�filer les images</b>
 * <p>
 * Cette classe est caract�ris�e par les informations suivantes
 * <ul>
 * <li>un nombre de composants visibles</li>
 * <li>les composants � afficher</li>
 * </ul>
 * </p>
 * 
 * @author Julien
 * @version 1.0
 *
 */
public class Carousel extends JPanel {

	/**
	 * L'uid
	 */
	private static final long serialVersionUID = 954320876901301631L;

	/**
	 * La liste des panels � afficher
	 */
	private List<DriverPanel> composants;
	
	/**
	 * Le nombre de panels visibles
	 */
	private int nbVisibles;
	
	/**
	 * L'indice du premier panel visible
	 */
	private int curseur;
	
	/**
	 * Le panel dans lequel sont affich�s les composants
	 */
	private JPanel affichage;
	
	/**
	 * Le GridBagConstraits de affichage
	 */
	private GridBagConstraints gc;

	/**
	 * Constructeur de Caroussel
	 * @param nbVisibles
	 * 	Le nombre de panels visibles
	 * @param composants
	 * 	Les panels � afficher
	 * @return 
	 * @throws IOException 
	 */
	
	
	public Carousel(int nbVisibles) {
		this.nbVisibles = nbVisibles;
		curseur=0;
		affichage = new JPanel();
		affichage.setOpaque(false);
		affichage.setLayout(new GridBagLayout());
		gc = new GridBagConstraints();
		gc.insets = new Insets(7, 7, 7, 7);
		gc.fill=GridBagConstraints.BOTH;
		gc.weighty=1;
		gc.weightx=nbVisibles;
		gc.gridy=0;
		//dessiner();
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		Entete entete = new Entete();
		entete.getGauche().addMouseListener(new ListenerReculer(this));
		entete.getDroite().addMouseListener(new ListenerAvancer(this));
		add(entete, BorderLayout.NORTH);
		
	}
	
	public  void setComposants(List<DriverPanel> drivers) throws IOException {
		this.composants = new ArrayList<DriverPanel>();
		for(DriverPanel p : drivers){
			DriverPanel c = new DriverPanel(new GridLayout(1,0), p.backgroundImage);
			c.add(p);
			c.setBorder(BorderFactory.createEtchedBorder());
			this.composants.add(c);
		}
		
		dessiner();
	}
	
	
	/**
	 * M�thode qui permet d'avancer le curseur de 1
	 */
	public void avancer(){
		if(curseur<composants.size()-nbVisibles){
			curseur++;
			dessiner();
		}
	}
	/**
	 * M�thode qui permet de reculer le curseur de 1
	 */
	public void reculer(){
		if(curseur>0){
			curseur--;
			dessiner();
		}
	}
	/**
	 * M�thode qui dessine les panels en fonction de la
	 * position du curseur
	 */
	public void dessiner(){
		affichage.removeAll();
		for(int i=curseur; i<Math.min(curseur+nbVisibles, curseur+composants.size()); i++){
			gc.gridx=i-curseur;
			affichage.add(composants.get(i), gc);
		}
		add(affichage, BorderLayout.CENTER);
		affichage.validate();
		affichage.repaint();
		validate();
		repaint();
	}
	
}
