package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;

public class ComputedParameterMessageItem extends MessageItem {

    /**
     *
     */
    private static final long serialVersionUID = 2857921319490473661L;
    public static final String XML_ELEMENT_COMPUTED_PARAM_LINK_PARAM_TYPE = "computedParameterLinkParamType";
    public static final String XML_ELEMENT_COMPUTED_PARAM_NAME = "computedParameterName";
    public static final String XML_ELEMENT_FATHER_S_NAME = "fatherSName";
    public static final String XML_ELEMENT_OBSERVATION_REFERENCE = "observationReference";
    private static final String XML_ELEMENT_COMPUTED_PARAMETER = "computedParameter";

    private final LINK_PARAM_TYPE computedParameterLinkParamType;
    private final String computedParameterName;
    private final ArrayList<String> fathersName;
    private final String observationId;

    public ComputedParameterMessageItem(final Observation observation, final OceanicParameter computedParameter,
	    final ArrayList<Parameter<? extends Number>> fathers) {
	super();

	observationId = observation.getId();
	computedParameterName = computedParameter.getCode();
	computedParameterLinkParamType = computedParameter.getLinkParamType();

	fathersName = new ArrayList<>();
	for (final Parameter<? extends Number> parameter : fathers) {
	    fathersName.add(parameter.getCode());
	}
    }

    /**
     * @return the computedParameterLinkParamType
     */
    public LINK_PARAM_TYPE getComputedParameterLinkParamType() {
	return computedParameterLinkParamType;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementAdditionnalInfo()
     */
    @Override
    protected List<Element> getElementAdditionnalInfo() {
	final ArrayList<Element> additionalInfo = new ArrayList<>();

	final Element obsIdElt = new Element(XML_ELEMENT_OBSERVATION_REFERENCE);
	obsIdElt.addContent(observationId);
	additionalInfo.add(obsIdElt);

	final Element computedParamNameElt = new Element(XML_ELEMENT_COMPUTED_PARAM_NAME);
	computedParamNameElt.addContent(computedParameterName);
	additionalInfo.add(computedParamNameElt);

	final Element computedParamLinkParamTypeElt = new Element(XML_ELEMENT_COMPUTED_PARAM_LINK_PARAM_TYPE);
	computedParamLinkParamTypeElt.addContent(computedParameterLinkParamType.toString());
	additionalInfo.add(computedParamLinkParamTypeElt);

	for (final String fatherName : fathersName) {
	    final Element fatherSNameTypeElt = new Element(XML_ELEMENT_FATHER_S_NAME);
	    fatherSNameTypeElt.addContent(fatherName);
	    additionalInfo.add(fatherSNameTypeElt);
	}

	return additionalInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_COMPUTED_PARAMETER;
    }

}
