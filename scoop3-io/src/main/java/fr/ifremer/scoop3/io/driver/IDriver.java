package fr.ifremer.scoop3.io.driver;

import java.awt.Image;
import java.io.IOException;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.io.impl.AbstractDataFileManager;

/**
 *
 * @author Altran
 *
 */
public interface IDriver {

    /**
     *
     * @param file
     * @return true if the file can be opened, false otherwise
     * @throws IOException
     * @throws DriverException
     */
    boolean canOpen(String file) throws DriverException;

    /**
     *
     * @return
     */
    AbstractDataFileManager getAbstractDataFileManager();

    /**
     * Validate and return a Report
     *
     * @return
     */
    Report validate();

    public Image getBackgroundImage() throws IOException;

    public abstract void resetFileManager();
}
