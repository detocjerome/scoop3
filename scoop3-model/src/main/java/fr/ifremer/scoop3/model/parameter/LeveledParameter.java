package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;

public class LeveledParameter extends OceanicParameter implements Serializable {

    /**
     * immersion level
     */
    private Integer immLevel;

    /**
     *
     */
    private static final long serialVersionUID = 4436841180959477621L;

    /**
     * constructor
     */
    public LeveledParameter(final String name) {
	super(name);
    }

    /**
     * Getter and setter
     */
    public Integer getImmLevel() {
	return this.immLevel;
    }

    public void setImmLevel(final Integer immLevel) {
	this.immLevel = immLevel;
    }
}
