package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.ITEM_STATE;

public class AutomaticControlStatusMessageItem extends MessageItem {

    /**
     *
     */
    private static final long serialVersionUID = -5202538004785792240L;
    private static final String XML_ELEMENT_AUTOMATIC_CONTROL_STATUS = "automaticControlStatus";
    private static final String XML_ELEMENT_PARAMETER = "parameter";
    private static final String XML_ELEMENT_STATUS = "status";
    private static final String XML_ELEMENT_ID = "id";

    private final String automaticControlName;
    private final Map<String, Object> parameters;
    private String status;

    public AutomaticControlStatusMessageItem(final String automaticControlName) {
	super(ITEM_STATE.INFO, null);
	this.automaticControlName = automaticControlName;
	parameters = new HashMap<>();
    }

    public AutomaticControlStatusMessageItem(final Element automaticControlStatusElt) {
	this(automaticControlStatusElt.getAttributeValue(XML_ELEMENT_ID));

	status = automaticControlStatusElt.getChildText(XML_ELEMENT_STATUS);

	for (final Element parameterElt : automaticControlStatusElt.getChildren(XML_ELEMENT_PARAMETER)) {
	    final String paramName = parameterElt.getAttributeValue(XML_ELEMENT_ID);
	    final Object paramValue = parameterElt.getValue();
	    parameters.put(paramName, paramValue);
	}
    }

    /**
     * Parameter used for the test
     *
     * @param parameterName
     * @param value
     */
    public void addParameter(final String parameterName, final Object value) {
	parameters.put(parameterName, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem#getXMLTree()
     */
    @Override
    public Element getXMLTree() {
	final Element stepElt = super.getXMLTree();

	stepElt.setAttribute(XML_ELEMENT_ID, automaticControlName);

	final Element statusElt = new Element(XML_ELEMENT_STATUS);
	statusElt.addContent(status);
	stepElt.addContent(statusElt);

	if (!parameters.isEmpty()) {
	    for (final String parameter : parameters.keySet()) {
		final Element paramElt = new Element(XML_ELEMENT_PARAMETER);
		paramElt.setAttribute(XML_ELEMENT_ID, parameter);
		paramElt.addContent(parameters.get(parameter).toString());
		stepElt.addContent(paramElt);
	    }
	}

	return stepElt;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(final String status) {
	this.status = status;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_AUTOMATIC_CONTROL_STATUS;
    }

    /**
     * @return the automaticControlName
     */
    public String getAutomaticControlName() {
	return automaticControlName;
    }

}
