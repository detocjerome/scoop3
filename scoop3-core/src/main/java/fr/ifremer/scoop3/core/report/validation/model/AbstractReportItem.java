package fr.ifremer.scoop3.core.report.validation.model;

import java.io.Serializable;

import fr.ifremer.scoop3.core.report.validation.model.StepItem.ITEM_STATE;

public abstract class AbstractReportItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5805848087988250474L;
    protected ITEM_STATE state;

    /**
     * Default constructor.
     */
    protected AbstractReportItem() {
	this(null);
    }

    /**
     * Constructor. Each report item has a {@link ITEM_STATE}. This constructor initialize this report item at this
     * {@link ITEM_STATE}.
     *
     * @param state
     */
    protected AbstractReportItem(final ITEM_STATE state) {
	this.state = state;
    }

    /**
     * Get the {@link ITEM_STATE}.
     *
     * @return
     */
    public ITEM_STATE getState() {
	return state;
    }

    /**
     * Set the {@link ITEM_STATE}.
     *
     * @param state
     */
    public void setState(final ITEM_STATE state) {
	this.state = state;
    }
}
