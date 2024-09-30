package fr.ifremer.scoop3.core.report.validation.model;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;

public class StepItem extends AbstractReportItem {

    /**
     *
     */
    private static final long serialVersionUID = -4186483798818763796L;

    public enum ITEM_STATE {
	ERROR, INFO, SUCCESS;
    }

    public enum STEP_TYPE {
	Q0_LOADING_FILE, //
	Q1_CONTROL_AUTO_METADATA, //
	Q2_CONTROL_AUTO_DATA, //
    }

    private static final String XML_ATTRIBUTE_STATE = "state";
    private static final String XML_ATTRIBUTE_STEP_TYPE = "stepType";
    private static final String XML_ELEMENT_MESSAGES = "messages";
    private static final String XML_ELEMENT_STEP = "step";

    private final STEP_TYPE stepType;
    protected final List<MessageItem> items = new ArrayList<>();

    public static StepItem getStepItem(final Element stepItemElt) {
	final StepItem stepItem = getStepItem(
		STEP_TYPE.valueOf(stepItemElt.getAttributeValue(XML_ATTRIBUTE_STEP_TYPE)));

	final Element messagesElt = stepItemElt.getChild(XML_ELEMENT_MESSAGES);
	if (messagesElt != null) {
	    for (final Element messageItemElt : messagesElt.getChildren()) {
		MessageItem messageItem;
		if (messageItemElt.getName().equals(CAErrorMessageItem.XML_ELEMENT_CA_ERROR_MESSAGE)) {
		    messageItem = new CAErrorMessageItem(messageItemElt);
		} else if (messageItemElt.getName().equals(CADataErrorMessageItem.XML_ELEMENT_CA_DATA_ERROR_MESSAGE)
			|| messageItemElt.getName()
				.equals(CADataLightErrorMessageItem.XML_ELEMENT_CA_DATA_LIGHT_ERROR_MESSAGE)) {
		    // if old data error message, convert into data light error
		    messageItem = new CADataLightErrorMessageItem(messageItemElt);
		} else if (messageItemElt.getName()
			.equals(CAMetadataErrorMessageItem.XML_ELEMENT_CA_OBS_METADATA_ERROR_MESSAGE)) {
		    messageItem = new CAMetadataErrorMessageItem(messageItemElt);
		} else {
		    messageItem = new MessageItem(messageItemElt);
		}
		if (stepItem != null) {
		    stepItem.addMessage(messageItem);
		}
	    }
	}

	return stepItem;
    }

    public static StepItem getStepItem(final STEP_TYPE stepType) {
	StepItem stepItem = null;
	switch (stepType) {
	case Q0_LOADING_FILE:
	    stepItem = new StepItem(stepType);
	    break;
	case Q1_CONTROL_AUTO_METADATA:
	case Q2_CONTROL_AUTO_DATA:
	    stepItem = new AutomaticControlStepItem(stepType);
	    break;
	}
	return stepItem;
    }

    /**
     * Default constructor.
     */
    protected StepItem(final STEP_TYPE stepType) {
	super(ITEM_STATE.SUCCESS);
	this.stepType = stepType;
    }

    /**
     * Add a new message in this step.
     *
     * @param message
     * @return
     */
    public CAErrorMessageItem addMessage(final MessageItem message) {

	this.items.add(message);

	if (message.getState().equals(ITEM_STATE.ERROR)) {
	    this.setState(ITEM_STATE.ERROR);
	}

	return null;
    }

    /**
     * Clear items ...
     */
    public void clearStepItems() {
	items.clear();
    }

    /**
     * TO BE OVERRIDE IN THE AutomaticControlStepItem CLASS
     *
     * @param caMetadataErrorMessageItem
     * @return
     */
    public boolean containsCAMetadataErrorMessageItem(final CAMetadataErrorMessageItem caMetadataErrorMessageItem) {
	return false;
    }

    /**
     * Get all messages in this step.
     *
     * @return
     */
    public List<MessageItem> getMessages() {
	return items;
    }

    /**
     * @return the stepType
     */
    public STEP_TYPE getStepType() {
	return stepType;
    }

    /**
     * Get the DOM XML Tree of the StepItem
     *
     * @return
     */
    public Element getXMLTree() {
	final Element stepElt = new Element(XML_ELEMENT_STEP);
	stepElt.setAttribute(XML_ATTRIBUTE_STEP_TYPE, String.valueOf(stepType));
	stepElt.setAttribute(XML_ATTRIBUTE_STATE, String.valueOf(getState()));

	final Element messagesElt = new Element(XML_ELEMENT_MESSAGES);
	for (final MessageItem messageItem : getMessages()) {
	    messagesElt.addContent(messageItem.getXMLTree());
	}
	stepElt.addContent(messagesElt);

	return stepElt;
    }

    /**
     * Remove an error message
     *
     * @param caErrorMessageItem
     */
    public void removeMessage(final CAErrorMessageItem caErrorMessageItem) {
	items.remove(caErrorMessageItem);

	if (caErrorMessageItem.getState().equals(ITEM_STATE.ERROR)) {
	    setState(ITEM_STATE.SUCCESS);
	    for (final MessageItem item : items) {
		if (item.getState() == null) {
		    item.setState(ITEM_STATE.ERROR);
		}
		if (item.getState().equals(ITEM_STATE.ERROR)) {
		    setState(ITEM_STATE.ERROR);
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final StringBuilder reportMessages = new StringBuilder();
	for (final MessageItem item : this.getMessages()) {
	    reportMessages.append("- " + item.getDetails());
	    reportMessages.append("\n");
	}
	return reportMessages.toString();
    }
}
