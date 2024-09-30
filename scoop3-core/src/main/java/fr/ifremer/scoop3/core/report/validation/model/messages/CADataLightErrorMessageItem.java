package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public class CADataLightErrorMessageItem extends CADataErrorMessageItem {

    public static final String XML_ELEMENT_CA_DATA_LIGHT_ERROR_MESSAGE = "caDataLightErrorMessage";

    private static final String XML_ELEMENT_INFO = "infoParameter";

    /**
     * @param messageItemElt
     */
    public CADataLightErrorMessageItem(final Element messageItemElt) {
	super(messageItemElt);
    }

    /**
     *
     * @param obs
     * @param details
     * @param paramCode
     * @param referenceIndex
     * @param flagAuto
     * @param isErrorOnReferenceParameter
     */
    public CADataLightErrorMessageItem(final Observation obs, final String details, final String paramCode,
	    final int referenceIndex, final QCValues flagAuto, final boolean isErrorOnReferenceParameter) {
	super(obs, details, paramCode, referenceIndex, flagAuto, isErrorOnReferenceParameter);
    }

    /**
     *
     * @param details
     * @param paramCode
     * @param referenceValue
     * @param referenceIndex
     * @param paramValue
     * @param paramValueStr
     * @param flagAuto
     * @param isErrorOnReferenceParameter
     */
    public CADataLightErrorMessageItem(final String obsId, final String details, final String paramCode,
	    final String referenceValue, final int referenceIndex, final Number paramValue, final String paramValueStr,
	    final QCValues flagAuto, final boolean isErrorOnReferenceParameter) {
	super(obsId, details, paramCode, referenceValue, referenceIndex, paramValue, paramValueStr, flagAuto,
		isErrorOnReferenceParameter);
    }

    @Override
    protected void parseDataErrorMessageItem(final Element messageItemElt) {

	final Element infoElement = messageItemElt.getChild(XML_ELEMENT_INFO);
	if (infoElement != null) {

	    isErrorOnReferenceParameter = "true"
		    .equalsIgnoreCase(infoElement.getAttributeValue(XML_ELEMENT_IS_REFERENCE_PARAMETER));

	    paramCode = infoElement.getAttributeValue(XML_ELEMENT_PARAM_CODE);

	    final String xmlParamValue = infoElement.getAttributeValue(XML_ELEMENT_PARAM_VALUE);
	    if ((xmlParamValue == null) || "null".equals(xmlParamValue)) {
		paramValue = null;
	    } else {
		try {
		    paramValue = Long.parseLong(xmlParamValue); // Date
		} catch (final NumberFormatException e) {
		    paramValue = Double.parseDouble(xmlParamValue);
		}
	    }

	    final String xmlParamValueStr = infoElement.getAttributeValue(XML_ELEMENT_PARAM_VALUE_STR);
	    paramValueStr = ((xmlParamValueStr == null) || "null".equals(xmlParamValueStr)) ? null : xmlParamValueStr;

	    try {
		final int refIndex = Integer.parseInt(infoElement.getAttributeValue(XML_ELEMENT_REFERENCE_INDEX));
		referenceIndex = refIndex;
	    } catch (final NumberFormatException nfe) {
	    }

	    referenceValue = infoElement.getAttributeValue(XML_ELEMENT_REFERENCE_VALUE);

	    referenceValueToDisplayInReportDialog = infoElement
		    .getAttributeValue(XML_ELEMENT_REFERENCE_VALUE_TO_DISPLAY_IN_REPORT_DIALOG);
	} else {
	    super.parseDataErrorMessageItem(messageItemElt);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	boolean equals = super.equals(obj);
	if (equals && (obj instanceof CADataLightErrorMessageItem)) {
	    final CADataLightErrorMessageItem otherMsg = (CADataLightErrorMessageItem) obj;

	    equals &= isErrorOnReferenceParameter == otherMsg.isErrorOnReferenceParameter();
	    equals &= (paramCode == otherMsg.getParamCode())
		    || ((paramCode != null) && paramCode.equals(otherMsg.getParamCode()));
	    equals &= (paramValueStr == otherMsg.getParamValueStr())
		    || ((paramValueStr != null) && paramValueStr.equals(otherMsg.getParamValueStr()));
	    equals &= referenceIndex == otherMsg.getReferenceIndex();
	}

	return equals;
    }

    @Override
    protected List<Element> addElementAdditionnalInfo(final List<Element> additionalInfo) {
	final Element infoElt = new Element(XML_ELEMENT_INFO);
	infoElt.setAttribute(XML_ELEMENT_IS_REFERENCE_PARAMETER, String.valueOf(isErrorOnReferenceParameter));
	infoElt.setAttribute(XML_ELEMENT_PARAM_CODE, paramCode);
	infoElt.setAttribute(XML_ELEMENT_PARAM_VALUE, String.valueOf(paramValue));
	infoElt.setAttribute(XML_ELEMENT_PARAM_VALUE_STR, String.valueOf(paramValueStr));
	infoElt.setAttribute(XML_ELEMENT_REFERENCE_INDEX, String.valueOf(referenceIndex));
	infoElt.setAttribute(XML_ELEMENT_REFERENCE_VALUE, referenceValue);
	infoElt.setAttribute(XML_ELEMENT_REFERENCE_VALUE_TO_DISPLAY_IN_REPORT_DIALOG,
		referenceValueToDisplayInReportDialog);
	additionalInfo.add(infoElt);

	return additionalInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_CA_DATA_LIGHT_ERROR_MESSAGE;
    }
}
