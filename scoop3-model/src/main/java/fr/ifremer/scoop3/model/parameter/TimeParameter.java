package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public class TimeParameter extends SpatioTemporalParameter<Long> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -9054972356382319188L;

    public TimeParameter(String name, int dimension) {
	super(name, dimension);
    }

}
