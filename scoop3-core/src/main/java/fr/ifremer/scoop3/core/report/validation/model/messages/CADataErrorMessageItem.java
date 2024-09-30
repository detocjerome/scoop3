package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public class CADataErrorMessageItem extends CAErrorMessageItem {

    /**
     *
     */
    private static final long serialVersionUID = -2126347086846794765L;

    public static final String XML_ELEMENT_CA_DATA_ERROR_MESSAGE = "caDataErrorMessage";

    protected static final String XML_ELEMENT_IS_REFERENCE_PARAMETER = "isReferenceParameter";
    protected static final String XML_ELEMENT_PARAM_CODE = "paramCode";
    protected static final String XML_ELEMENT_PARAM_VALUE = "paramValue";
    protected static final String XML_ELEMENT_PARAM_VALUE_STR = "paramValueStr";
    protected static final String XML_ELEMENT_REFERENCE_INDEX = "referenceIndex";
    protected static final String XML_ELEMENT_REFERENCE_VALUE = "referenceValue";
    protected static final String XML_ELEMENT_REFERENCE_VALUE_TO_DISPLAY_IN_REPORT_DIALOG = "referenceValueToDisplayInReportDialog";

    /**
     * SimpleDateFormat used for TimeSeries
     */
    protected static final SimpleDateFormat sdf = Conversions.getSimpleDateFormat("dd/MM/YY HH:mm");

    protected boolean isErrorOnReferenceParameter = false;
    protected String paramCode;
    protected Number paramValue = null;
    protected String paramValueStr;
    protected int referenceIndex;
    protected String referenceValue;
    protected String referenceValueToDisplayInReportDialog;

    /**
     * @param messageItemElt
     */
    public CADataErrorMessageItem(final Element messageItemElt) {
	super(messageItemElt);

	parseDataErrorMessageItem(messageItemElt);
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
    public CADataErrorMessageItem(final Observation obs, final String details, final String paramCode,
	    final int referenceIndex, final QCValues flagAuto, final boolean isErrorOnReferenceParameter) {
	this(obs.getId(), details, paramCode,
		String.valueOf(obs.getReferenceParameter().getValues().get(referenceIndex)), referenceIndex,
		(obs.getOceanicParameter(paramCode) == null) ? null
			: obs.getOceanicParameter(paramCode).getValues().get(referenceIndex),
		(obs.getOceanicParameter(paramCode) == null) ? null
			: String.valueOf(obs.getOceanicParameter(paramCode).getValues().get(referenceIndex)),
		flagAuto, isErrorOnReferenceParameter);
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
    public CADataErrorMessageItem(final String obsId, final String details, final String paramCode,
	    final String referenceValue, final int referenceIndex, final Number paramValue, final String paramValueStr,
	    final QCValues flagAuto, final boolean isErrorOnReferenceParameter) {
	super(isErrorOnReferenceParameter ? ERROR_MESSAGE_TYPE.OBSERVATION_REFERENCE_DATA
		: ERROR_MESSAGE_TYPE.OBSERVATION_DATA, obsId, details, flagAuto);
	this.paramCode = paramCode;
	this.paramValue = paramValue;
	this.paramValueStr = paramValueStr;
	this.referenceValue = referenceValue;
	this.referenceIndex = referenceIndex;
	this.isErrorOnReferenceParameter = isErrorOnReferenceParameter;

	try {
	    // If we can convert the String as a Long, it is a TimeSerie => It is a Date
	    final long refIsLongValue = Long.parseLong(referenceValue);
	    referenceValueToDisplayInReportDialog = sdf.format(new Date(refIsLongValue));
	} catch (final NumberFormatException nfe) {
	    referenceValueToDisplayInReportDialog = referenceValue;
	}
    }

    protected void parseDataErrorMessageItem(final Element messageItemElt) {
	isErrorOnReferenceParameter = "true"
		.equalsIgnoreCase(messageItemElt.getChildText(XML_ELEMENT_IS_REFERENCE_PARAMETER));

	paramCode = messageItemElt.getChildText(XML_ELEMENT_PARAM_CODE);

	final String xmlParamValue = messageItemElt.getChildText(XML_ELEMENT_PARAM_VALUE);
	if ((xmlParamValue == null) || "null".equals(xmlParamValue)) {
	    paramValue = null;
	} else {
	    try {
		paramValue = Long.parseLong(xmlParamValue); // Date
	    } catch (final NumberFormatException e) {
		paramValue = Double.parseDouble(xmlParamValue);
	    }
	}

	final String xmlParamValueStr = messageItemElt.getChildText(XML_ELEMENT_PARAM_VALUE_STR);
	paramValueStr = ((xmlParamValueStr == null) || "null".equals(xmlParamValueStr)) ? null : xmlParamValueStr;

	try {
	    final int refIndex = Integer.parseInt(messageItemElt.getChildText(XML_ELEMENT_REFERENCE_INDEX));
	    referenceIndex = refIndex;
	} catch (final NumberFormatException nfe) {
	}

	referenceValue = messageItemElt.getChildText(XML_ELEMENT_REFERENCE_VALUE);

	referenceValueToDisplayInReportDialog = messageItemElt
		.getChildText(XML_ELEMENT_REFERENCE_VALUE_TO_DISPLAY_IN_REPORT_DIALOG);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	boolean equals = super.equals(obj);
	if (equals && (obj instanceof CADataErrorMessageItem)) {
	    final CADataErrorMessageItem otherMsg = (CADataErrorMessageItem) obj;

	    equals &= isErrorOnReferenceParameter == otherMsg.isErrorOnReferenceParameter();
	    equals &= (paramCode == otherMsg.getParamCode())
		    || ((paramCode != null) && paramCode.equals(otherMsg.getParamCode()));
	    equals &= (paramValueStr == otherMsg.getParamValueStr())
		    || ((paramValueStr != null) && paramValueStr.equals(otherMsg.getParamValueStr()));
	    equals &= referenceIndex == otherMsg.getReferenceIndex();
	}

	return equals;
    }

    /**
     * @return the paramCode
     */
    public String getParamCode() {
	return paramCode;
    }

    /**
     * @return the paramValue
     */
    public Number getParamValue() {
	return paramValue;
    }

    /**
     * @return the paramValueStr
     */
    public String getParamValueStr() {
	return paramValueStr;
    }

    /**
     * @return the referenceIndex
     */
    public int getReferenceIndex() {
	return referenceIndex;
    }

    /**
     * @return the referenceValue
     */
    public String getReferenceValue() {
	return referenceValue;
    }

    /**
     * @return the referenceValueToDisplayInReportDialog
     */
    public String getReferenceValueToDisplayInReportDialog() {
	return referenceValueToDisplayInReportDialog;
    }

    /**
     * @return the isErrorOnReferenceParameter
     */
    public boolean isErrorOnReferenceParameter() {
	return isErrorOnReferenceParameter;
    }

    /**
     * @param isErrorOnReferenceParameter
     *            the isErrorOnReferenceParameter to set
     */
    public void setErrorOnReferenceParameter(final boolean isErrorOnReferenceParameter) {
	this.isErrorOnReferenceParameter = isErrorOnReferenceParameter;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementAdditionnalInfo()
     */
    @Override
    protected List<Element> getElementAdditionnalInfo() {
	final List<Element> additionalInfo = super.getElementAdditionnalInfo();

	addElementAdditionnalInfo(additionalInfo);

	return additionalInfo;
    }

    protected List<Element> addElementAdditionnalInfo(final List<Element> additionalInfo) {
	final Element isRefParamElt = new Element(XML_ELEMENT_IS_REFERENCE_PARAMETER);
	isRefParamElt.addContent(String.valueOf(isErrorOnReferenceParameter));
	additionalInfo.add(isRefParamElt);

	final Element paramCodeElt = new Element(XML_ELEMENT_PARAM_CODE);
	paramCodeElt.addContent(paramCode);
	additionalInfo.add(paramCodeElt);

	final Element paramValueElt = new Element(XML_ELEMENT_PARAM_VALUE);
	paramValueElt.addContent(String.valueOf(paramValue));
	additionalInfo.add(paramValueElt);

	final Element paramValueStrElt = new Element(XML_ELEMENT_PARAM_VALUE_STR);
	paramValueStrElt.addContent(String.valueOf(paramValueStr));
	additionalInfo.add(paramValueStrElt);

	final Element refIndexElt = new Element(XML_ELEMENT_REFERENCE_INDEX);
	refIndexElt.addContent(String.valueOf(referenceIndex));
	additionalInfo.add(refIndexElt);

	final Element refValueElt = new Element(XML_ELEMENT_REFERENCE_VALUE);
	refValueElt.addContent(referenceValue);
	additionalInfo.add(refValueElt);

	final Element refValueToDisplayInReportDialogElt = new Element(
		XML_ELEMENT_REFERENCE_VALUE_TO_DISPLAY_IN_REPORT_DIALOG);
	refValueToDisplayInReportDialogElt.addContent(referenceValueToDisplayInReportDialog);
	additionalInfo.add(refValueToDisplayInReportDialogElt);

	return additionalInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_CA_DATA_ERROR_MESSAGE;
    }
}
