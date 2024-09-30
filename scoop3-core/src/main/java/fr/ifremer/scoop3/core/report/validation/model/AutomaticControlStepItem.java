package fr.ifremer.scoop3.core.report.validation.model;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;

public class AutomaticControlStepItem extends StepItem {

    /**
     *
     */
    private static final long serialVersionUID = -6860794834232857601L;

    public static final String XML_ELEMENT_AUTOMATIC_CONTROL_STATUS_MESSAGES = "automaticControlsStatus";

    private final List<AutomaticControlStatusMessageItem> automaticControlStatusMessageItems;

    public static StepItem getStepItem(final Element stepItemElt) {
	final StepItem toReturn = StepItem.getStepItem(stepItemElt);

	final Element automaticControlsStatusElt = stepItemElt.getChild(XML_ELEMENT_AUTOMATIC_CONTROL_STATUS_MESSAGES);
	if (automaticControlsStatusElt != null) {
	    for (final Element automaticControlStatusElt : automaticControlsStatusElt.getChildren()) {
		toReturn.addMessage(new AutomaticControlStatusMessageItem(automaticControlStatusElt));
	    }
	}

	return toReturn;
    }

    public AutomaticControlStepItem(final STEP_TYPE stepType) {
	super(stepType);
	automaticControlStatusMessageItems = new ArrayList<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.core.report.validation.model.StepItem#addMessage(fr.ifremer.scoop3.core.report.validation.model
     * .MessageItem)
     */
    @Override
    public CAErrorMessageItem addMessage(final MessageItem message) {
	CAErrorMessageItem toReturn = null;
	if (message instanceof AutomaticControlStatusMessageItem) {
	    AutomaticControlStatusMessageItem messageItem = null;
	    for (final AutomaticControlStatusMessageItem automaticControlStatusMessageItem : automaticControlStatusMessageItems) {
		if (automaticControlStatusMessageItem.getAutomaticControlName()
			.equals(((AutomaticControlStatusMessageItem) message).getAutomaticControlName())) {
		    messageItem = automaticControlStatusMessageItem;
		}
	    }
	    if (messageItem != null) {
		automaticControlStatusMessageItems.remove(messageItem);
	    }
	    automaticControlStatusMessageItems.add((AutomaticControlStatusMessageItem) message);
	} else {
	    boolean messageAlreadyExists = false;
	    for (final MessageItem currentItem : items) {
		if (currentItem.equals(message)) {
		    messageAlreadyExists = true;
		    if (currentItem instanceof CAErrorMessageItem) {
			toReturn = (CAErrorMessageItem) currentItem;

			// if (currentItem instanceof CADataErrorMessageItem) {
			// ((CADataErrorMessageItem) currentItem).setErrorChecked(false);
			// }
		    }
		} else {
		    // Check if there is another Error Message on the same station / param / level
		    if ((currentItem instanceof CADataErrorMessageItem)
			    && (message instanceof CADataErrorMessageItem)) {
			final CADataErrorMessageItem currentItemCADataErrorMessageItem = (CADataErrorMessageItem) currentItem;
			final CADataErrorMessageItem messageCADataErrorMessageItem = (CADataErrorMessageItem) message;

			// Same OBS1ID
			if (((currentItemCADataErrorMessageItem.getObs1Id() != null)
				&& currentItemCADataErrorMessageItem.getObs1Id()
					.equals(messageCADataErrorMessageItem.getObs1Id()))
				// Same PARAM
				&& (((currentItemCADataErrorMessageItem.getParamCode() != null)
					&& currentItemCADataErrorMessageItem.getParamCode()
						.equals(messageCADataErrorMessageItem.getParamCode()))
					&& (((currentItemCADataErrorMessageItem.getParamValueStr() == null)
						&& (messageCADataErrorMessageItem.getParamValueStr() == null))
						&& ((currentItemCADataErrorMessageItem.getParamValueStr() != null)
							&& currentItemCADataErrorMessageItem.getParamValueStr().equals(
								messageCADataErrorMessageItem.getParamValueStr())))
					// Same REF_LEVEL
					&& ((currentItemCADataErrorMessageItem.getReferenceValue() != null)
						&& currentItemCADataErrorMessageItem.getReferenceValue()
							.equals(messageCADataErrorMessageItem.getReferenceValue())))) {

			    // Keep a reference on the message to update the QC
			    if (toReturn == null) {
				toReturn = currentItemCADataErrorMessageItem;
			    }

			    // Check if the current Message is a Manual Control Message
			    if (currentItemCADataErrorMessageItem.isManualControlMessage()
				    && !messageCADataErrorMessageItem.isManualControlMessage()) {
				// ==> Replace the Manual Control Message by this message
				messageAlreadyExists = true;
				currentItemCADataErrorMessageItem.setIsManualControlMessage(false);
				currentItemCADataErrorMessageItem
					.setDetails(messageCADataErrorMessageItem.getDetails());
				currentItemCADataErrorMessageItem
					.setComment(((currentItemCADataErrorMessageItem.getComment() == null) ? ""
						: currentItemCADataErrorMessageItem.getComment() == null) + " "
						+ Messages.getMessage("core.automatic-control-step.manual-qc-copied"));
			    } else
			    // ==> set the same manual QC (if != NULL)
			    if (currentItemCADataErrorMessageItem.getFlagManual() != null) {
				messageCADataErrorMessageItem
					.setFlagManual(currentItemCADataErrorMessageItem.getFlagManual());
				messageCADataErrorMessageItem.setComment(
					Messages.getMessage("core.automatic-control-step.manual-qc-copied"));
			    }
			}
		    }
		}
	    }
	    if (!messageAlreadyExists) {
		super.addMessage(message);
	    }
	}
	return toReturn;
    }

    /**
     * @param caMetadataErrorMessageItem
     * @return TRUE if the message already exists in this step
     */
    @Override
    public boolean containsCAMetadataErrorMessageItem(final CAMetadataErrorMessageItem caMetadataErrorMessageItem) {
	for (final MessageItem currentItem : items) {
	    // Check if there is another Error Message on the same station / qc_to_update
	    if (currentItem instanceof CAMetadataErrorMessageItem) {
		final CAMetadataErrorMessageItem currentItemCAMetadataErrorMessageItem = (CAMetadataErrorMessageItem) currentItem;

		// Same OBS1ID
		if (((currentItemCAMetadataErrorMessageItem.getObs1Id() != null)
			&& currentItemCAMetadataErrorMessageItem.getObs1Id()
				.equals(caMetadataErrorMessageItem.getObs1Id()))
			// Same QC_TO_UPDATE
			&& ((currentItemCAMetadataErrorMessageItem.getQcToUpdate() != null)
				&& currentItemCAMetadataErrorMessageItem.getQcToUpdate()
					.equals(caMetadataErrorMessageItem.getQcToUpdate()))
			&& (((currentItemCAMetadataErrorMessageItem.getMetadata() == null)
				&& (caMetadataErrorMessageItem.getMetadata() == null))
				|| ((currentItemCAMetadataErrorMessageItem.getMetadata() != null)
					&& currentItemCAMetadataErrorMessageItem.getMetadata()
						.equals(caMetadataErrorMessageItem.getMetadata())))
			//
			&& (currentItemCAMetadataErrorMessageItem.isManualControlMessage() == caMetadataErrorMessageItem
				.isManualControlMessage())) {
		    // FAE 29771 -- memorize that currentItem is "generated" during last control
		    currentItemCAMetadataErrorMessageItem.setIsGenerateDuringLastControl(true);

		    return true;
		}
	    }
	}

	return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.StepItem#getXMLTree()
     */
    @Override
    public Element getXMLTree() {
	final Element stepElt = super.getXMLTree();

	stepElt.setAttribute(XML_ELEMENT_AUTOMATIC_CONTROL_STATUS_MESSAGES, String.valueOf(true));

	final Element messagesElt = new Element(XML_ELEMENT_AUTOMATIC_CONTROL_STATUS_MESSAGES);
	for (final AutomaticControlStatusMessageItem automaticControlStatusMessageItem : automaticControlStatusMessageItems) {
	    messagesElt.addContent(automaticControlStatusMessageItem.getXMLTree());
	}
	if (stepElt.getChildren().isEmpty()) {
	    stepElt.addContent(messagesElt);
	} else {
	    stepElt.addContent(0, messagesElt);
	}
	return stepElt;
    }
}
