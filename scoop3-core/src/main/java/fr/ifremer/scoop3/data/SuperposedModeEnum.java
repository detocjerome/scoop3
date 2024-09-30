package fr.ifremer.scoop3.data;

import fr.ifremer.scoop3.infra.i18n.Messages;

public enum SuperposedModeEnum {
    /**
     * Display only the current Observation only
     */
    CURRENT_OBSERVATION_ONLY,
    /**
     * Display nearest profiles of the current profile, but for current platform only. The "nearest" is defined by a
     * square around the lat/lon and a period.
     */
    NEAREST_PROFILES_FOR_ALL_PLATFORMS,
    /**
     * Display nearest profiles of the current profile, but for all platforms. The "nearest" is defined by a square
     * around the lat/lon and a period.
     */
    NEAREST_PROFILES_FOR_CURRENT_PLATFORM,
    /**
     * Display only observations for the current Platform returned by the original query
     */
    PROFILES_FOR_PLATFORM_FROM_DATASET,
    /**
     * Display all observations from the dataset
     */
    ALL_OBSERVATIONS_FROM_DATASET,
    //
    ;

    public String getLabel() {
	return Messages.getMessage("coriolis-gui.SuperposedModeEnum." + this.name());
    }
}
