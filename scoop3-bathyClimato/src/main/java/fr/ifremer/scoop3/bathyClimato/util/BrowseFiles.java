package fr.ifremer.scoop3.bathyClimato.util;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowseFiles {
    /**
     * renvois la liste des fichiers apr�s un parcours recoursif de l'arborescence
     * 
     * @param allFiles
     * @param root
     * @param extension
     */
    public void getFilesRecursivly(final List<String> allFiles, final String root, final String extension) {
	if (root != null) {
	    final File f = new File(root);
	    if (f.exists()) {
		final File[] listFiles = f.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
		    if (listFiles[i].isDirectory()) {
			getFilesRecursivly(allFiles, listFiles[i].toString(), extension);
		    } else {
			if (listFiles[i].toString().matches("(.*)" + extension)) {
			    allFiles.add(listFiles[i].getName());
			}
		    }
		}
	    }
	}
    }

    /**
     * Extrait les codes GF3 en rempla�ant l'extension par une chaine vide
     * 
     * @param allFiles
     * @param extension
     */
    public Set<String> extractParameters(final List<String> allFiles, final String extension) {
	final Set<String> parameters = new HashSet<String>();
	for (final String fileName : allFiles) {
	    parameters.add(fileName.replace(extension, "").toUpperCase());
	}
	return parameters;
    }

}
