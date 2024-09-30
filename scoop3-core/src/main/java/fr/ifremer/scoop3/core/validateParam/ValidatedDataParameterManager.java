package fr.ifremer.scoop3.core.validateParam;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ValidatedDataParameterManager {

    /**
     * Reference on the instance
     */
    private static ValidatedDataParameterManager instance;

    /**
     * The key is the observationId. The Value is the List of the validated Parameter Code.
     */
    private final List<String> validatedParamList;

    /**
     * Clear all Validated Parameters
     */
    public static void clearValidatedParameters() {
	if (instance != null) {
	    instance.validatedParamList.clear();
	}
    }

    /**
     * @return the instance
     */
    public static ValidatedDataParameterManager getInstance() {
	if (instance == null) {
	    instance = new ValidatedDataParameterManager();
	}
	return instance;
    }

    public static Icon getValidatedImageIcon() {
	return new ImageIcon(ValidatedDataParameterManager.class.getClassLoader().getResource(
		"icons/dialog_ok_apply_16x16.png"));
    }

    /**
     * Default constructor. Protected to use the Singleton.
     */
    protected ValidatedDataParameterManager() {
	validatedParamList = new ArrayList<>();
    }

    /**
     * @return the validatedMap
     */
    public List<String> getValidatedParamList() {
	return validatedParamList;
    }

    /**
     * @param parameterCode
     * @return the value if the parameterCode for the given observationCode is validated
     */
    public boolean isValidated(final String parameterCode) {
	return validatedParamList.contains(parameterCode);
    }

    /**
     * Memorize value
     * 
     * @param parameterCode
     * @param isValidated
     */
    public void setIsValidated(final String parameterCode, final boolean isValidated) {
	if (isValidated) {
	    if (!validatedParamList.contains(parameterCode)) {
		validatedParamList.add(parameterCode);
	    }
	} else {
	    validatedParamList.remove(parameterCode);
	}
    }
}
