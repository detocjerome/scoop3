package fr.ifremer.scoop3.bathyClimato.interfaces;

import java.util.List;

import fr.ifremer.scoop3.bathyClimato.climato.ClimatoRequest;
import fr.ifremer.scoop3.bathyClimato.climato.ClimatoResult;
import fr.ifremer.scoop3.bathyClimato.climato.Climatology;
import fr.ifremer.scoop3.bathyClimato.etopo1.Etopo1ReaderException;
import fr.ifremer.scoop3.bathyClimato.etopo5.Etopo5ReaderException;
import fr.ifremer.scoop3.bathyClimato.gebco.GebcoReaderException;

public interface IBathyService {
    /**
     * Renvois la bathym�trie de la position
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @return (short) la valeur de la bathym�trie � cette position
     * @throws Etopo5ReaderException
     */
    public short getBathy(Double latitude, Double longitude) throws Etopo5ReaderException;

    /**
     * Renvois la bathym�trie de la position
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @throws Etopo1ReaderException
     */
    public short getBathyEtopo1(Double latitude, Double longitude) throws Etopo1ReaderException;

    /**
     * Renvois la bathym�trie de la position
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @throws Etopo5ReaderException
     */
    public short getBathyEtopo5(Double latitude, Double longitude) throws Etopo5ReaderException;

    /**
     * Renvois la bathym�trie de la position
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @throws GebcoReaderException
     */
    public short getBathyGebco(Double latitude, Double longitude) throws GebcoReaderException;

    /**
     * Renvois les valeurs de la batyhym�trie de la position et des 8 points autour
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @return (short) tableau de la valeur de bathym�trie � la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire : ------------- | 1 | 2 | 3 | ------------- | 8 | 0 | 4 |
     *         ------------- | 7 | 6 | 5 | ------------- L'index des tableaux correspond a cette numerotation
     * @throws Etopo5ReaderException
     */
    public short[] getSurroundBathy(Double latitude, Double longitude) throws Etopo5ReaderException;

    /**
     * Renvois les valeurs de la batyhym�trie de la position et des 8 points autour pour un fichier etopo1
     *
     * @param latitude
     * @param longitude
     * @return (short) tableau de la valeur de bathym�trie � la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire :
     * @throws Etopo1ReaderException
     */
    public short[] getSurroundBathyEtopo1(Double latitude, Double longitude) throws Etopo1ReaderException;

    /**
     * Renvois les valeurs de la batyhym�trie de la position et des 8 points autour pour un fichier etopo5
     *
     * @param latitude
     * @param longitude
     * @return (short) tableau de la valeur de bathym�trie � la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire :
     * @throws Etopo5ReaderException
     */
    public short[] getSurroundBathyEtopo5(Double latitude, Double longitude) throws Etopo5ReaderException;

    /**
     * Renvois les valeurs de la batyhym�trie de la position et des 8 points autour pour un fichier gebco
     *
     * @param latitude
     * @param longitude
     * @return (short) tableau de la valeur de bathym�trie � la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire :
     * @throws GebcoReaderException
     */
    public short[] getSurroundBathyGebco(Double latitude, Double longitude) throws GebcoReaderException;

    /**
     * Permet d'obtenir toute la climatologie pour une position donn�es. (Moyennes,Nombre de mesures,Profondeurs,Ecarts)
     *
     * @param latitude
     * @param longitude
     * @param month
     * @param parameter
     * @param climatoType
     */
    public ClimatoResult getClimato(Double latitude, Double longitude, Integer month, String parameter,
	    String climatoType);

    /**
     * Permet d'obtenir toutee les climatologies pour des positions donn�es. (Moyennes,Nombre de
     * mesures,Profondeurs,Ecarts)
     *
     * @param climatoRequestArray
     */
    public ClimatoResult[] getClimatoArray(ClimatoRequest[] climatoRequestArray);

    /**
     * Renvois la liste des param�tres disponible pour la climatologie
     *
     * @param weatherType
     * @return
     */
    public String getParametersAvailable(String weatherType);

    /**
     * Renvoie la liste des climatologies
     *
     * @param filename
     * @return
     */
    public List<Climatology> getClimatoList(String filename);

}
