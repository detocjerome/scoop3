package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParametersRelationships implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4368966640623239990L;
    /**
     * Map which contains the "sons" for a given parameter
     */
    private static final Map<Parameter<? extends Number>, List<Parameter<? extends Number>>> mapFatherToSons;
    /**
     * Map which contains the fathers of a given son
     */
    private static final Map<Parameter<? extends Number>, List<Parameter<? extends Number>>> mapSonToFathers;

    static {
	mapSonToFathers = new HashMap<Parameter<? extends Number>, List<Parameter<? extends Number>>>();
	mapFatherToSons = new HashMap<Parameter<? extends Number>, List<Parameter<? extends Number>>>();
    }

    /**
     * Clear all relations memorized
     */
    public static void clearRelations() {
	mapSonToFathers.clear();
	mapFatherToSons.clear();
    }

    /**
     * 
     * @param computedParameter
     * @return the fathers for a given computed parameter or an empty list.
     */
    public static List<Parameter<? extends Number>> getFathers(final Parameter<? extends Number> computedParameter) {
	if (mapSonToFathers.containsKey(computedParameter)) {
	    return mapSonToFathers.get(computedParameter);
	}
	return new ArrayList<Parameter<? extends Number>>(0);
    }

    /**
     * 
     * @param fatherParameter
     * @return the computed parameters for a given father or an empty list.
     */
    public static List<Parameter<? extends Number>> getLinkedParameters(
	    final Parameter<? extends Number> fatherParameter) {
	if (mapFatherToSons.containsKey(fatherParameter)) {
	    return mapFatherToSons.get(fatherParameter);
	}
	return new ArrayList<Parameter<? extends Number>>(0);
    }

    /**
     * Memorize a relation between a computed parameter and one of its "parent".
     * 
     * @param computedParameter
     * @param parentParameter
     */
    public static void memorizeParametersRelation(final Parameter<? extends Number> computedParameter,
	    final Parameter<? extends Number> parentParameter) {

	List<Parameter<? extends Number>> fathers;
	if (mapSonToFathers.containsKey(computedParameter)) {
	    fathers = mapSonToFathers.get(computedParameter);
	} else {
	    fathers = new ArrayList<Parameter<? extends Number>>();
	}
	if (!fathers.contains(parentParameter)) {
	    fathers.add(parentParameter);
	    mapSonToFathers.put(computedParameter, fathers);
	}

	List<Parameter<? extends Number>> sons;
	if (mapFatherToSons.containsKey(parentParameter)) {
	    sons = mapFatherToSons.get(parentParameter);
	} else {
	    sons = new ArrayList<Parameter<? extends Number>>();
	}
	if (!sons.contains(computedParameter)) {
	    sons.add(computedParameter);
	    mapFatherToSons.put(parentParameter, sons);
	}
    }
}
