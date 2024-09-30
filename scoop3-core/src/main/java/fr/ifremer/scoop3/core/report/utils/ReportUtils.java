package fr.ifremer.scoop3.core.report.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bushe.swing.event.EventBus;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.backup.BackupCache;
import fr.ifremer.scoop3.events.GuiEventDisplayDialog;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;

public abstract class ReportUtils {

    public static final String CONTEXT_DIRECTORY_SUFFIX = ".scoop3"; // System.getProperty("user.home") + "/.scoop3";
    public static String contextDir = "";
    public static final String XML_EXTENSION = ".xml";
    public static final String CACHE_EXTENSION = ".data";
    public static final String CACHE_INDEX_EXTENSION = ".index";
    private static Map<String, String> additionalParametersInXMLContext = new HashMap<>();
    private static Path loadedFilePath;
    private static Path backupFileInContextDirPath;
    private static Path backupIndexFileInContextDirPath;
    private static Report report;
    public static boolean isReportLoading = false;

    public static void clearLoadedFilePath() {
	additionalParametersInXMLContext.clear();
	contextDir = null;
	loadedFilePath = null;
	backupFileInContextDirPath = null;
	backupIndexFileInContextDirPath = null;

    }

    public static void setLoadedFilePath(final Path path) {
	loadedFilePath = path;
    }

    /**
     * @param source
     * @param copyOption
     *            (i.e. StandardCopyOption.REPLACE_EXISTING)
     */
    public static final void copyFileToContextDirectory(final String source, final CopyOption copyOption) {
	// Get path for the source file from the manager
	loadedFilePath = Paths.get(source);

	// Get path for the source file from the manager
	final Path originalCopyPath = Paths.get(source + ".orig");

	// Get XML path in the context directory
	backupFileInContextDirPath = Paths.get(contextDir).resolve(BackupCache.LOCAL_CACHE + CACHE_EXTENSION);
	backupIndexFileInContextDirPath = Paths.get(contextDir)
		.resolve(BackupCache.LOCAL_CACHE + CACHE_INDEX_EXTENSION);

	// Try to copy the file in the working directory
	try {
	    if ((copyOption == null) && !new File(originalCopyPath.toString()).exists()) {
		Files.copy(loadedFilePath, originalCopyPath);
	    } else if (copyOption != null) {
		Files.copy(loadedFilePath, originalCopyPath, copyOption);
	    }
	} catch (final IOException e) {
	    // Handle exception
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
    }

    /**
     * Create context directory
     */
    public static void createContextDirectory(final Path filePath) {
	// We need to save the current context directory in order to save the
	// XML RR at the right location each time we need to persist it.
	contextDir = filePath.toString() + CONTEXT_DIRECTORY_SUFFIX;

	// Get the path from the String URI.
	final Path contextWorkingDirectory = Paths.get(contextDir);

	try {
	    // If the context working directory do not already exist
	    if (Files.notExists(contextWorkingDirectory)) {
		// Create it !
		Files.createDirectories(contextWorkingDirectory);

		// Set attribute to set the working directory hidden
		// Files.setAttribute(contextWorkingDirectory, "dos:hidden",
		// true);
	    }

	    // 20161206 jdetoc Initialisation des variables static a la creation du contexte et non plus dans
	    // copyFileToContextDirectory pour lecture du report

	    // Get path for the source file from the manager
	    loadedFilePath = filePath;
	    // Get backup path in the context directory
	    backupFileInContextDirPath = Paths.get(contextDir).resolve(BackupCache.LOCAL_CACHE + CACHE_EXTENSION);
	    backupIndexFileInContextDirPath = Paths.get(contextDir)
		    .resolve(BackupCache.LOCAL_CACHE + CACHE_INDEX_EXTENSION);
	} catch (final IOException e) {
	    SC3Logger.LOGGER.debug(e.getMessage());
	}
    }

    /**
     * @return the additionalParametersInXMLContext
     */
    public static Map<String, String> getAdditionalParametersInXMLContext() {
	return additionalParametersInXMLContext;
    }

    /**
     * @return the destPath + ".BAK"
     */
    public static Path getBackupCopyPath() {
	return Paths.get(contextDir).resolve(getLoadedFilePath().getFileName() + ".BAK");
    }

    /**
     * @return the contextDir
     */
    public static String getContextDir() {
	return contextDir;
    }

    /**
     * @return the loadedFilePath
     */
    public static Path getLoadedFilePath() {
	return loadedFilePath;
    }

    /**
     * @return the cacheFileInContextDirPath
     */
    public static Path getBackupFileInContextDirPath() {
	return backupFileInContextDirPath;
    }

    /**
     * @return the cacheIndexFileInContextDirPath
     */
    public static Path getBackupIndexFileInContextDirPath() {
	return backupIndexFileInContextDirPath;
    }

    /**
     * If the XML context exists, this method returns the corresponding report
     *
     * @return
     */
    public static Report loadCacheIfExists() {
	report = null;
	ReportUtils.isReportLoading = true;
	// execute thread to launch progress bar
	final ExecutorService executor = Executors.newSingleThreadExecutor();
	executor.execute(() -> {
	    if (Files.exists(backupFileInContextDirPath)) {
		report = Report.getReport(loadedFilePath.toString());
	    }

	    // Remove the JDialog to inform the user that the Metadata control is in progress
	    EventBus.publish(new GuiEventDisplayDialog());

	    ReportUtils.isReportLoading = false;

	    executor.shutdown();
	});

	// Add a JDialog to inform the user that the Format is in progress
	EventBus.publish(new GuiEventDisplayDialog(Messages.getMessage("bpc-controller.report-in-progress-title"),
		Messages.getMessage("bpc-controller.report-in-progress-message")));

	// Shutdown the executor
	executor.shutdown();

	// wait for the report to be loaded. When done, return the report
	while (ReportUtils.isReportLoading) {
	    try {
		Thread.sleep(10);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
	    }
	}

	return report;
    }

    /**
     * Add a new additional parameter to write in the XML context (if value != null)
     *
     * @param key
     * @param value
     */
    public static void setAdditionalParameterInXMLContext(final String key, final String value) {
	if (value == null) {
	    additionalParametersInXMLContext.remove(key);
	} else {
	    additionalParametersInXMLContext.put(key, value);
	}
    }
}
