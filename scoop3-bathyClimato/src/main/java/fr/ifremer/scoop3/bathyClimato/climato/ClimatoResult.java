package fr.ifremer.scoop3.bathyClimato.climato;

public class ClimatoResult {

    protected Integer nbImmersion;
    protected Float[] profondeurs;
    protected Float[] ecarts;
    protected Float[] moyennes;
    protected Integer[] nbMesures;

    /**
     * test si la mesure pour le niveau est compatible avec la climatologie
     * 
     * @param level
     *            niveau
     * @param value
     *            valeur
     * @param factor
     * @return true si elle est compatible false si non compatible
     */
    public boolean isCompatibleWithClimato(final Double level, final Double value, final Integer factor) {
	Float valeurMoyenne;
	Float ecartType;
	int compteur = 0;
	boolean status = true;

	// sort de la boucle lorsque le niveau est atteint
	// ou si celui ci est d�pass�
	while (this.profondeurs[compteur] < level) {
	    compteur++;
	}
	// Calcul inutile car valeurs directement accessibles
	if (this.profondeurs[compteur] == level.floatValue()) {
	    valeurMoyenne = this.moyennes[compteur];
	    ecartType = this.ecarts[compteur];
	} else {
	    // valeur moyenne
	    valeurMoyenne = interpolate(this.profondeurs[compteur - 1], this.moyennes[compteur - 1],
		    this.profondeurs[compteur], this.moyennes[compteur], level.floatValue());
	    // ecart type
	    ecartType = interpolate(this.profondeurs[compteur - 1], this.ecarts[compteur - 1],
		    this.profondeurs[compteur], this.ecarts[compteur], level.floatValue());
	}
	if ((value > (valeurMoyenne + (factor * ecartType))) || (value < (valeurMoyenne - (factor * ecartType)))) {
	    status = false;
	}
	return status;
    }

    /**
     * Interpolation lin�aire
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param level
     *            valeur (x)
     * @return valeur obtenuie pour le niveau saisi
     */
    protected Float interpolate(final Float x1, final Float y1, final Float x2, final Float y2, final Float level) {
	return (((y1 - y2) / (x1 - x2)) * level) + (((x1 * y2) - (x2 * y1)) / (x1 - x2));
    }

    /**
     * Renvois les ecart types
     * 
     * @return
     */
    public Float[] getEcarts() {
	return ecarts;
    }

    public void setEcarts(final Float[] ecarts) {
	if (ecarts != null) {
	    this.ecarts = ecarts;
	} else {
	    this.ecarts = new Float[0];
	}
    }

    /**
     * renvois les valeurs moyennes
     * 
     * @return
     */
    public Float[] getMoyennes() {
	return moyennes;
    }

    public void setMoyennes(final Float[] moyennes) {
	if (moyennes != null) {
	    this.moyennes = moyennes;
	} else {
	    this.moyennes = new Float[0];
	}
    }

    /**
     * renvois le nombre de valeurs
     * 
     * @return
     */
    public Integer getNbImmersion() {
	return nbImmersion;
    }

    public void setNbImmersion(final Integer nbImmersion) {
	if (nbImmersion != null) {
	    this.nbImmersion = nbImmersion;
	} else {
	    this.nbImmersion = 0;
	}
    }

    /**
     * renvois le nombre de mesures qui ont permis de calculer la valeur moyenne
     * 
     * @return
     */
    public Integer[] getNbMesures() {
	return nbMesures;
    }

    public void setNbMesures(final Integer[] nbMesures) {
	if (nbMesures != null) {
	    this.nbMesures = nbMesures;
	} else {
	    this.nbMesures = new Integer[0];
	}
    }

    /**
     * renvois les profondeurs
     * 
     * @return
     */
    public Float[] getProfondeurs() {
	return profondeurs;
    }

    public void setProfondeurs(final Float[] profondeurs) {
	if (profondeurs != null) {
	    this.profondeurs = profondeurs;
	} else {
	    this.profondeurs = new Float[0];
	}
    }

}
