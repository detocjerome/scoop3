package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public class ZParameter extends SpatioTemporalParameter<Double> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8175249134024381099L;

    public ZParameter(final String name, final int dimension) {
	super(name, dimension);
    }

    /**
     * @return the code
     */
    @Override
    public String getCode() {
	 return super.getCode();
//	return "REF_PARAM";
    }
}
