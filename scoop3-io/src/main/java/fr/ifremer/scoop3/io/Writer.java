package fr.ifremer.scoop3.io;

import fr.ifremer.scoop3.model.Dataset;

/**
 * 
 * @author Altran
 * 
 */
public interface Writer {

    /**
     * @param dataset
     * @return TRUE is the writer can write the current dataset
     */
    boolean isWriterForSourceType(Dataset dataset);

    /**
     * Write the file
     * 
     * @throws Exception
     */
    void write(Dataset dataset) throws Exception;
}
