package fr.ifremer.scoop3.bathyClimato.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ifremer.scoop3.bathyClimato.climato.ClimatoDirectories;
import fr.ifremer.scoop3.bathyClimato.climato.ClimatoReader;
import fr.ifremer.scoop3.bathyClimato.climato.ClimatoRequest;
import fr.ifremer.scoop3.bathyClimato.climato.ClimatoResult;
import fr.ifremer.scoop3.bathyClimato.climato.Climatology;
import fr.ifremer.scoop3.bathyClimato.climato.Constantes;
import fr.ifremer.scoop3.bathyClimato.etopo1.Etopo1Reader;
import fr.ifremer.scoop3.bathyClimato.etopo1.Etopo1ReaderException;
import fr.ifremer.scoop3.bathyClimato.etopo5.Etopo5Reader;
import fr.ifremer.scoop3.bathyClimato.etopo5.Etopo5ReaderException;
import fr.ifremer.scoop3.bathyClimato.gebco.GebcoReader;
import fr.ifremer.scoop3.bathyClimato.gebco.GebcoReaderException;
import fr.ifremer.scoop3.bathyClimato.interfaces.IBathyService;
import fr.ifremer.scoop3.bathyClimato.util.BrowseFiles;
import fr.ifremer.scoop3.infra.logger.SC3Logger;

public class BathyService implements IBathyService {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(BathyService.class);

    private ClimatoReader climatoReader;

    @Override
    public short getBathy(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	return getBathyEtopo5(latitude, longitude);
    }

    @Override
    public short getBathyEtopo1(final Double latitude, final Double longitude) throws Etopo1ReaderException {
	short bathyValue;
	final Etopo1Reader bathyReader = Etopo1Reader.getInstance();
	LOGGER.info("Appel getBathyEtopo1 " + latitude + "," + longitude);
	bathyValue = bathyReader.findDepthValue(latitude, longitude);
	return bathyValue;
    }

    @Override
    public short getBathyEtopo5(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	short bathyValue;
	final Etopo5Reader bathyReader = Etopo5Reader.getInstance();
	LOGGER.info("Appel getBathyEtopo5 " + latitude + "," + longitude);
	bathyValue = bathyReader.getBathyPosition(latitude, longitude);
	return bathyValue;
    }

    @Override
    public short getBathyGebco(final Double latitude, final Double longitude) throws GebcoReaderException {
	short bathyValue;
	final GebcoReader bathyReader = GebcoReader.getInstance();
	LOGGER.info("Appel getBathyGebco " + latitude + "," + longitude);
	bathyValue = bathyReader.findDepthValueWithCellAverage(latitude, longitude);
	return bathyValue;
    }

    @Override
    public short[] getSurroundBathy(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	return getSurroundBathyEtopo5(latitude, longitude);
    }

    @Override
    public short[] getSurroundBathyEtopo1(final Double latitude, final Double longitude) throws Etopo1ReaderException {
	final Etopo1Reader bathyReader = Etopo1Reader.getInstance();
	LOGGER.info(" Appel getSurroundBathyEtopo1 " + latitude + "," + longitude);
	return bathyReader.getSurroundBathyEtopo1(latitude, longitude);
    }

    @Override
    public short[] getSurroundBathyEtopo5(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	final Etopo5Reader bathyReader = Etopo5Reader.getInstance();
	LOGGER.info(" Appel getSurroundBathyEtopo5 " + latitude + "," + longitude);
	return bathyReader.getSurroundBathy(latitude, longitude);
    }

    @Override
    public short[] getSurroundBathyGebco(final Double latitude, final Double longitude) throws GebcoReaderException {
	final GebcoReader bathyReader = GebcoReader.getInstance();
	LOGGER.info(" Appel getSurroundBathyGebco " + latitude + "," + longitude);
	return bathyReader.getSurroundBathyGebco(latitude, longitude);
    }

    /**
     * Get climato
     *
     * @param coord
     *            $1 $2
     * @param Month
     * @param Parameter
     *            GF3
     * @param Climato
     */
    @Override
    public ClimatoResult getClimato(final Double latitude, final Double longitude, final Integer month,
	    String parameter, final String climatoType) {
	climatoReader = new ClimatoReader();
	final ClimatoResult climatoResult = new ClimatoResult();

	parameter = checkAndModifyParameter(parameter);

	LOGGER.info(
		" Appel getClimato " + latitude + "," + longitude + "," + month + "," + parameter + "," + climatoType);
	// lecture des donn�es
	climatoReader.readClimatoHydro(climatoType, latitude.floatValue(), longitude.floatValue(), month, parameter);

	climatoResult.setNbImmersion(climatoReader.getNbMaxImmersion());
	climatoResult.setProfondeurs(climatoReader.getProfondeur());
	climatoResult.setNbMesures(climatoReader.getNbMesures());
	climatoResult.setMoyennes(climatoReader.getMoyennes());
	climatoResult.setEcarts(climatoReader.getEcarts());

	return climatoResult;
    }

    /**
     * Get climato
     *
     * @param coord
     *            $1 $2
     * @param Month
     * @param Parameter
     *            GF3
     * @param Climato
     */
    @Override
    public ClimatoResult[] getClimatoArray(final ClimatoRequest[] climatoRequestArray) {
	ClimatoReader localClimatoReader = new ClimatoReader();
	ClimatoResult climatoResult = new ClimatoResult();
	final List<ClimatoResult> climatoResults = new ArrayList<ClimatoResult>();

	Double latitude;
	Double longitude;
	Integer month;
	String parameter;
	String climatoType;
	ClimatoRequest climatoRequest;

	LOGGER.debug(" Appel getClimatoArray");

	// lecture des donn�es
	for (int i = 0; i < climatoRequestArray.length; i++) {
	    LOGGER.debug(" Appel getClimatoArray : " + i);

	    localClimatoReader = new ClimatoReader();
	    climatoResult = new ClimatoResult();

	    climatoRequest = climatoRequestArray[i];
	    latitude = climatoRequest.getLatitude().doubleValue();
	    longitude = climatoRequest.getLongitude().doubleValue();
	    month = climatoRequest.getMonth();
	    parameter = climatoRequest.getGf3();
	    climatoType = climatoRequest.getClimatoCode();

	    LOGGER.debug(" Appel getClimato " + latitude + "," + longitude + "," + month + "," + parameter + ","
		    + climatoType);

	    parameter = checkAndModifyParameter(parameter);

	    localClimatoReader.readClimatoHydro(climatoType, latitude.floatValue(), longitude.floatValue(), month,
		    parameter);

	    climatoResult.setNbImmersion(localClimatoReader.getNbMaxImmersion());
	    climatoResult.setProfondeurs(localClimatoReader.getProfondeur());
	    climatoResult.setNbMesures(localClimatoReader.getNbMesures());
	    climatoResult.setMoyennes(localClimatoReader.getMoyennes());
	    climatoResult.setEcarts(localClimatoReader.getEcarts());

	    climatoResults.add(climatoResult);

	}

	final ClimatoResult[] climatoResultArray = new ClimatoResult[climatoResults.size()];

	return climatoResults.toArray(climatoResultArray);
    }

    /**
     * Renvois la liste des param�tres disponibles pour la climatologie
     */
    @Override
    public String getParametersAvailable(final String weatherType) {
	final BrowseFiles bf = new BrowseFiles();
	final List<String> list = new ArrayList<String>();
	Set<String> gf3CodesExtracted;
	String parameters = "";
	final ClimatoDirectories climDir = new ClimatoDirectories();
	LOGGER.debug("Appel getParametersAvailable " + weatherType);
	LOGGER.debug("Chemin du fichier de climato : " + climDir.getClimatoPath(weatherType));
	bf.getFilesRecursivly(list, climDir.getClimatoPath(weatherType), Constantes.EXTENSION_FILTRE);
	gf3CodesExtracted = bf.extractParameters(list, Constantes.EXTENSION_FILTRE);
	for (final String code : gf3CodesExtracted) {
	    parameters += code + "|";
	}
	return parameters;
    }

    private static String getTagValue(final String sTag, final Element eElement) {
	final NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	final Node nValue = nlList.item(0);

	return nValue.getNodeValue();
    }

    @Override
    public List<Climatology> getClimatoList(final String filename) {

	final List<Climatology> climatoList = new ArrayList<Climatology>();
	final Map<Integer, Integer> monthes = new HashMap<Integer, Integer>();
	Climatology climato;
	File fXmlFile;
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	Document doc;
	NodeList climatoNodes;
	NodeList nodeList;
	Node node;
	Element element;
	Node monthNode;
	NodeList monthList;
	int id;

	try {
	    fXmlFile = new File(filename);
	    dbFactory = DocumentBuilderFactory.newInstance();
	    dBuilder = dbFactory.newDocumentBuilder();
	    doc = dBuilder.parse(fXmlFile);
	    doc.getDocumentElement().normalize();

	    climatoNodes = doc.getElementsByTagName("climato");

	    for (int nbClimato = 0; nbClimato < climatoNodes.getLength(); nbClimato++) {
		node = climatoNodes.item(nbClimato);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
		    climato = new Climatology();
		    element = (Element) node;
		    climato.setCode(getTagValue("code", element));
		    climato.setType(getTagValue("type", element));
		    climato.setFileType(getTagValue("file_type", element));
		    climato.setName(getTagValue("name", element));
		    climato.setDirectory(getTagValue("path", element));
		    climato.setStdDevDirectory(getTagValue("path", element));
		    climato.setStep(new Float(getTagValue("step", element)));

		    // Recuperation du noeud seasons
		    nodeList = element.getElementsByTagName("seasons");

		    // Recuperation des noeuds season
		    if (nodeList.getLength() != 0) {
			node = nodeList.item(nodeList.getLength() - 1);
			element = (Element) node;
			nodeList = element.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
			    node = nodeList.item(i);
			    if (node.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) node;
				id = new Integer(element.getAttribute("id"));

				monthList = element.getElementsByTagName("month");
				for (int j = 0; j < monthList.getLength(); j++) {
				    monthNode = monthList.item(j);
				    if (monthNode.getNodeType() == Node.ELEMENT_NODE) {
					monthes.put(new Integer(monthNode.getFirstChild().getNodeValue()), id);
				    }
				}
			    }
			}
			climato.setMonthes(monthes);
		    }
		    climatoList.add(climato);
		}
	    }
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	return climatoList;
    }

    /**
     * Modifie le param�tre GF3 si celui-ci est suffix� de ADJUSTED Dans le cas ou celui-ci est suffix� de
     * ADJUSTED_ERROR aucune modification n'est r�alis�e ex : <param>_ADJUSTED -> <param>
     *
     * @param parameter
     *            String en majuscule
     * @return parameter String en majuscule
     */
    private String checkAndModifyParameter(final String parameter) {
	String modifiedParameter = parameter.toUpperCase();
	if (modifiedParameter.matches("(.*)_ADJUSTED")) {
	    modifiedParameter = modifiedParameter.replace("_ADJUSTED", "");
	}
	return modifiedParameter;
    }

    public ClimatoReader getClimatoReader() {
	return climatoReader;
    }

}
