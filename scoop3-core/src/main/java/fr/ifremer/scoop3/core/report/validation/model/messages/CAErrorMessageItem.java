package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.ITEM_STATE;
import fr.ifremer.scoop3.model.QCValues;

public class CAErrorMessageItem extends MessageItem {

    /**
     *
     */
    private static final long serialVersionUID = 1800006099536982570L;

    public enum ERROR_MESSAGE_TYPE {
	DATASET_METADATA, // Error on Dataset Metadata
	OBSERVATION_DATA, // Error on Observation Data
	OBSERVATION_REFERENCE_DATA, // Error on Observation Reference Parameter Data
	OBSERVATION_METADATA, // Error on Observation Metadata
    }

    public static final String XML_ELEMENT_CA_ERROR_MESSAGE = "caErrorMessage";
    private static final String XML_ELEMENT_COMMENT = "comment";
    private static final String XML_ELEMENT_ERROR_MESSAGE_TYPE = "errorMessageType";
    private static final String XML_ELEMENT_FLAG_AUTO = "flagAuto";
    private static final String XML_ELEMENT_FLAG_MANUAL = "flagManual";
    private static final String XML_ELEMENT_IS_BLOCKING_ERROR = "isBlockingError";
    private static final String XML_ELEMENT_IS_ERROR_CHECKED = "isErrorChecked";
    private static final String XML_ELEMENT_IS_MANUAL_CONTROL_MESSAGE = "isManualControlMessage";
    private static final String XML_ELEMENT_OBS1 = "obs1";
    private static final String XML_ELEMENT_OBS2 = "obs2";

    private String comment;
    private final ERROR_MESSAGE_TYPE errorMessageType;
    private QCValues flagAuto;
    private QCValues flagManual;
    private boolean isBlockingError = true;
    private boolean isErrorChecked = false;
    private boolean isManualControlMessage = false;
    protected String obs1Id;
    protected String obs2Id;

    /**
     * @param messageItemElt
     * @return
     */
    private static ERROR_MESSAGE_TYPE getErrorMessageType(final Element messageItemElt) {
	try {
	    return ERROR_MESSAGE_TYPE.valueOf(messageItemElt.getChildText(XML_ELEMENT_ERROR_MESSAGE_TYPE));
	} catch (final IllegalArgumentException iae) {
	    // This should never happened
	    return null;
	}
    }

    /**
     * @param messageItemElt
     * @return
     */
    private static QCValues getFlagAuto(final Element messageItemElt) {
	if ((messageItemElt.getChildText(XML_ELEMENT_FLAG_AUTO) != null)
		&& !"null".equals(messageItemElt.getChildText(XML_ELEMENT_FLAG_AUTO))) {
	    try {
		return QCValues.valueOf(messageItemElt.getChildText(XML_ELEMENT_FLAG_AUTO));
	    } catch (final IllegalArgumentException iae) {
		// This should never happened
	    }
	}

	return null;
    }

    public CAErrorMessageItem(final Element messageItemElt) {
	this(getErrorMessageType(messageItemElt), null, messageItemElt.getChildText(XML_ELEMENT_DETAILS),
		getFlagAuto(messageItemElt));

	try {
	    final ITEM_STATE itemState = ITEM_STATE.valueOf(messageItemElt.getAttributeValue(XML_ATTRIBUTE_STATE));
	    setState(itemState);
	} catch (final IllegalArgumentException iae) {
	}

	final Element obs1Elt = messageItemElt.getChild(XML_ELEMENT_OBS1);
	if (obs1Elt != null) {
	    obs1Id = obs1Elt.getText();
	}

	final Element obs2Elt = messageItemElt.getChild(XML_ELEMENT_OBS2);
	if (obs2Elt != null) {
	    obs2Id = obs2Elt.getText();
	}

	final Element isBlockingErrorElt = messageItemElt.getChild(XML_ELEMENT_IS_BLOCKING_ERROR);
	if (isBlockingErrorElt != null) {
	    isBlockingError = !"false".equalsIgnoreCase(isBlockingErrorElt.getValue());
	}

	try {
	    final QCValues flagAutoQCValues = QCValues
		    .getQCValues(Integer.valueOf(messageItemElt.getChildText(XML_ELEMENT_FLAG_AUTO)));
	    flagAuto = flagAutoQCValues;
	} catch (final NumberFormatException nfe) {
	}

	if (messageItemElt.getChild(XML_ELEMENT_FLAG_MANUAL) != null) {
	    try {
		final QCValues flagManualQCValues = QCValues
			.getQCValues(Integer.valueOf(messageItemElt.getChildText(XML_ELEMENT_FLAG_MANUAL)));
		flagManual = flagManualQCValues;
	    } catch (final NumberFormatException nfe) {
	    }
	}

	isErrorChecked = "true".equalsIgnoreCase(messageItemElt.getChildText(XML_ELEMENT_IS_ERROR_CHECKED));

	final Element commentElt = messageItemElt.getChild(XML_ELEMENT_COMMENT);
	if (commentElt != null) {
	    comment = messageItemElt.getChildText(XML_ELEMENT_COMMENT);
	}

	isManualControlMessage = "true"
		.equalsIgnoreCase(messageItemElt.getChildText(XML_ELEMENT_IS_MANUAL_CONTROL_MESSAGE));
    }

    public CAErrorMessageItem(final ERROR_MESSAGE_TYPE errorMessageType, final String obs1Id, final String details,
	    final QCValues flagAuto) {
	this(errorMessageType, obs1Id, null, details, flagAuto);
    }

    public CAErrorMessageItem(final ERROR_MESSAGE_TYPE errorMessageType, final String obs1Id, final String obs2Id,
	    final String details, final QCValues flagAuto) {
	super(ITEM_STATE.ERROR, details);
	this.errorMessageType = errorMessageType;
	this.obs1Id = obs1Id;
	this.obs2Id = obs2Id;
	this.flagAuto = flagAuto;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public CAErrorMessageItem clone() {

	final CAErrorMessageItem newMsg = new CAErrorMessageItem(errorMessageType, obs1Id, obs2Id, details, flagAuto);
	newMsg.setComment(comment);
	newMsg.setBlockingError(isBlockingError);
	newMsg.setFlagManual(flagManual);
	newMsg.setErrorChecked(isErrorChecked);

	return newMsg;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	boolean equals = super.equals(obj);
	if (obj instanceof CAErrorMessageItem) {
	    final CAErrorMessageItem otherMsg = (CAErrorMessageItem) obj;
	    equals &= errorMessageType == otherMsg.getErrorMessageType();
	    equals &= (obs1Id == otherMsg.getObs1Id()) || ((obs1Id != null) && obs1Id.equals(otherMsg.getObs1Id()));
	    equals &= (obs2Id == otherMsg.getObs2Id()) || ((obs2Id != null) && obs2Id.equals(otherMsg.getObs2Id()));
	    equals &= (isBlockingError == otherMsg.isBlockingError());
	    equals &= flagAuto == otherMsg.getFlagAuto();
	}
	return equals;
    }

    /**
     * @return the comment
     */
    public String getComment() {
	return comment;
    }

    /**
     * @return the errorMessageType
     */
    public ERROR_MESSAGE_TYPE getErrorMessageType() {
	return errorMessageType;
    }

    /**
     * @return the flagAuto
     */
    public QCValues getFlagAuto() {
	return flagAuto;
    }

    /**
     * @return the flagManual
     */
    public QCValues getFlagManual() {
	return flagManual;
    }

    /**
     * @return the obs1Id
     */
    public String getObs1Id() {
	return obs1Id;
    }

    /**
     * @return the obs2Id
     */
    public String getObs2Id() {
	return obs2Id;
    }

    /**
     * @return the isBlockingError
     */
    public boolean isBlockingError() {
	return isBlockingError;
    }

    /**
     * @return the isErrorChecked
     */
    public boolean isErrorChecked() {
	return isErrorChecked;
    }

    /**
     * @return the isManualControlMessage
     */
    public boolean isManualControlMessage() {
	return isManualControlMessage;
    }

    /**
     * @param isBlockingError
     *            the isBlockingError to set
     */
    public void setBlockingError(final boolean isBlockingError) {
	this.isBlockingError = isBlockingError;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(final String comment) {
	this.comment = comment;
    }

    /**
     * @param isErrorChecked
     *            the isErrorChecked to set
     */
    public void setErrorChecked(final boolean isErrorChecked) {
	this.isErrorChecked = isErrorChecked;
    }

    /**
     * @param flagManual
     *            the flagManual to set
     */
    public void setFlagManual(final QCValues flagManual) {
	this.flagManual = flagManual;
    }

    /**
     * @param isManualControlMessage
     *            the isManualControlMessage to set
     */
    public void setIsManualControlMessage(final boolean isManualControlMessage) {
	this.isManualControlMessage = isManualControlMessage;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementAdditionnalInfo()
     */
    @Override
    protected List<Element> getElementAdditionnalInfo() {
	final ArrayList<Element> additionalInfo = new ArrayList<>();

	final Element errorMessageTypeElt = new Element(XML_ELEMENT_ERROR_MESSAGE_TYPE);
	errorMessageTypeElt.addContent(errorMessageType.toString());
	additionalInfo.add(errorMessageTypeElt);

	if (obs1Id != null) {
	    final Element obsElt = new Element(XML_ELEMENT_OBS1);
	    obsElt.addContent(obs1Id);
	    additionalInfo.add(obsElt);
	}
	if (obs2Id != null) {
	    final Element obsElt = new Element(XML_ELEMENT_OBS2);
	    obsElt.addContent(obs2Id);
	    additionalInfo.add(obsElt);
	}

	final Element isBlockingErrorElt = new Element(XML_ELEMENT_IS_BLOCKING_ERROR);
	isBlockingErrorElt.addContent(String.valueOf(isBlockingError));
	additionalInfo.add(isBlockingErrorElt);

	if (flagAuto != null) {
	    final Element flagAutoElt = new Element(XML_ELEMENT_FLAG_AUTO);
	    flagAutoElt.addContent(String.valueOf(flagAuto.getQCValue()));
	    additionalInfo.add(flagAutoElt);
	}

	if (flagManual != null) {
	    final Element flagManualElt = new Element(XML_ELEMENT_FLAG_MANUAL);
	    flagManualElt.addContent(String.valueOf(flagManual.getQCValue()));
	    additionalInfo.add(flagManualElt);
	}

	final Element isErrorCheckedMsgElt = new Element(XML_ELEMENT_IS_ERROR_CHECKED);
	isErrorCheckedMsgElt.addContent(String.valueOf(isErrorChecked));
	additionalInfo.add(isErrorCheckedMsgElt);

	if (comment != null) {
	    final Element commentElt = new Element(XML_ELEMENT_COMMENT);
	    commentElt.addContent(comment);
	    additionalInfo.add(commentElt);
	}

	final Element isManualControlMsgElt = new Element(XML_ELEMENT_IS_MANUAL_CONTROL_MESSAGE);
	isManualControlMsgElt.addContent(String.valueOf(isManualControlMessage));
	additionalInfo.add(isManualControlMsgElt);

	return additionalInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_CA_ERROR_MESSAGE;
    }

}
