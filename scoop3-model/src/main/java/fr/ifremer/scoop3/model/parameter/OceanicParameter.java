package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public class OceanicParameter extends Parameter<Double> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1838987279738468771L;

    private boolean partiallyEmpty = false;

    /**
     * Constructor
     *
     * @param name
     */
    public OceanicParameter(final String name) {
	this(new OceanicVariable(name));
    }

    /**
     * Constructor
     *
     * @param oceanicVariable
     */
    public OceanicParameter(final OceanicVariable oceanicVariable) {
	super();
	this.variable = oceanicVariable;
	setDimension(DIMENSION_UNLIMITED);
    }

    /**
     * Copy constructor
     *
     * @param oceanicParameter
     * @param code
     */
    public OceanicParameter(final OceanicParameter oceanicParameter, final String code) {
	super(oceanicParameter, code);
    }

    /**
     * @return the computedType
     */
    public LINK_PARAM_TYPE getLinkParamType() {
	return ((OceanicVariable) variable).getLinkParamType();
    }

    public boolean isPartiallyEmpty() {
	return this.partiallyEmpty;
    }

    public void setPartiallyEmpty(final boolean partiallyEmpty) {
	this.partiallyEmpty = partiallyEmpty;
    }
}
