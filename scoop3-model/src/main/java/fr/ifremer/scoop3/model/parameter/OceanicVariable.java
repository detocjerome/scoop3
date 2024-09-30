package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public class OceanicVariable extends Variable<Double> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3397156530934628753L;

    public OceanicVariable(final String name) {
	super();
	setCode(name);
    }
}
