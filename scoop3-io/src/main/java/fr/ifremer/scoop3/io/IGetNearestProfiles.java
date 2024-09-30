package fr.ifremer.scoop3.io;

import java.sql.SQLException;
import java.util.List;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.Profile;

/**
 * Used to get the nearest profiles
 */
public interface IGetNearestProfiles {

    /**
     * @param report
     * @param platform
     * @param profile
     * @return
     * @throws SQLException
     */
    public List<Profile> getNearestProfilesForCurrentPlatform(Report report, Platform platform, Profile profile)
	    throws SQLException;

    /**
     * @param report
     * @param platform
     * @param profile
     * @return
     * @throws SQLException
     */
    public List<Profile> getNearestProfilesForAllPlatforms(Report report, Platform platform, Profile profile)
	    throws SQLException;

}
