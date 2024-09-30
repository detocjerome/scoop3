package fr.ifremer.scoop3.io;

import fr.ifremer.scoop3.core.report.validation.Report;

/**
 * 
 * @author Altran
 * 
 */
public interface Reader<T> {

    /**
     * Read the file and return something
     * 
     * @param path
     * @throws Exception
     */
    T read(String path) throws Exception;

    Report validate();
}
