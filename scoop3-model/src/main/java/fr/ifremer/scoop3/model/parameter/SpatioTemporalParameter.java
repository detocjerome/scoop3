package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public abstract class SpatioTemporalParameter<T extends Number> extends Parameter<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7123002460296639049L;

    protected SpatioTemporalParameter(final String name, final int dimension) {
	super(name, dimension);
    }

}
