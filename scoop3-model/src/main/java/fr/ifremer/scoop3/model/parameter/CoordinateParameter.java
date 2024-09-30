package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public abstract class CoordinateParameter<T extends Number> extends SpatioTemporalParameter<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4382841684946073037L;

    protected CoordinateParameter(final String name, final int dimension) {
	super(name, dimension);
    }

}
