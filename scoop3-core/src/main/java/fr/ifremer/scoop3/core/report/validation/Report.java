package fr.ifremer.scoop3.core.report.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.common.io.Files;

import fr.ifremer.scoop3.core.report.validation.backup.Backup;
import fr.ifremer.scoop3.core.report.validation.backup.BackupCache;
import fr.ifremer.scoop3.core.report.validation.backup.BackupCacheException;
import fr.ifremer.scoop3.core.report.validation.backup.BackupKey;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.ITEM_STATE;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.ComputedParameterMessageItem;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;

/**
 * @author Altran Report class cache Binding with ehCache to persist a model
 *
 */
public class Report {

    public static final String XML_ATTRIBUTE_FILEPATH = "filepath";
    public static final String XML_ATTRIBUTE_ORIGINAL_FILEPATH = "originalFilepath";
    public static final String XML_ELEMENT_REPORT = "report";

    private final Backup backup;
    private static BackupCache backupCache;
    private static BackupCacheException be;

    /**
     * Constructor. Initialize attributes and create the first {@link StepItem}.
     *
     * @param path
     */
    public Report(final String path) {
	if ((backupCache != null) && !backupCache.getURI().equals(path)) {
	    try {
		backupCache.close();
		be = null;
	    } catch (final BackupCacheException e) {
		SC3Logger.LOGGER.error("Cannot close backup cache:" + e);
		be = e;
	    }
	}

	backupCache = new BackupCache(path);
	try {
	    backupCache.open();
	} catch (final BackupCacheException e) {
	    SC3Logger.LOGGER.warn("Cannot open local cache:" + e);
	    be = e;
	}

	// 57274 : old backup uri was path, new backup uri is fileName
	final String fileName = Files.getNameWithoutExtension(path) + "." + Files.getFileExtension(path);
	backup = new Backup(fileName);

    }

    public Report(final Backup backup, final BackupCache backupCache) {
	this.backup = backup;
	Report.backupCache = backupCache;
    }

    /**
     * Get a new Report from the XML
     *
     * @param dataset
     * @param rootElement
     * @return
     */
    public static Report getReport(final String path) {
	be = null;
	final BackupCache backupCache = getBackupCache(path);
	final Backup backup = getBackup(path);
	if (be != null) {
	    return null;
	}
	return new Report(backup, backupCache);
    }

    public static Backup getBackup(final String URI) {
	Backup backup = null;
	try {
	    if (backupCache != null) {
		final BackupKey key = backupCache.getBackupKey(URI);
		if (key != null) {
		    backup = backupCache.getFromCache(key);
		}
	    }
	} catch (final BackupCacheException e) {
	    SC3Logger.LOGGER.error("Cannot access local cache:" + e);
	    be = e;
	    return null;
	}
	if (backup == null) {

	    // 57274 : old backup uri was path, new backup uri is fileName
	    final String fileName = Files.getNameWithoutExtension(URI) + "." + Files.getFileExtension(URI);

	    backup = new Backup(fileName);
	}

	return backup;
    }

    public static BackupCache getBackupCache(final String URI) {

	if ((backupCache == null) || !backupCache.getURI().equals(URI)) {
	    if (backupCache != null) {
		try {
		    backupCache.close();
		} catch (final BackupCacheException e) {
		    SC3Logger.LOGGER.error("Cannot close backup cache:" + e);
		    be = e;
		}
	    }
	    backupCache = new BackupCache(URI);
	}

	try {
	    backupCache.open();

	} catch (final BackupCacheException e) {
	    SC3Logger.LOGGER.warn("Cannot open local cache:" + e);

	    backupCache = null;
	    be = e;
	}

	return backupCache;
    }

    public static BackupCache getBackupCache() {
	return backupCache;
    }

    /**
     * Add a {@link ComputedParameterMessageItem} into the Computed Parameters list (except for type
     * LINK_PARAM_TYPE.COMPUTED_CONTROL)
     *
     * @param messageItem
     */
    public void addComputedParameterMessage(final ComputedParameterMessageItem messageItem) {
	if (messageItem.getComputedParameterLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL) {
	    if (backup.getComputedParameters() == null) {
		backup.setComputedParameters(new ArrayList<ComputedParameterMessageItem>());
	    }
	    backup.getComputedParameters().add(messageItem);
	}
    }

    /**
     * Add a {@link MessageItem} in the current {@link StepItem}
     *
     * @param item
     * @return
     */
    public CAErrorMessageItem addMessage(final MessageItem item, final STEP_TYPE stepType) {
	return getStep(stepType).addMessage(item);
    }

    /**
     * Allows to a {@link Report} to this {@link Report}.
     *
     * @param report
     */
    public void concat(final Report report) {
	backup.setOriginalFilepath(report.getOriginalFilepath());
	if (report.getSteps() != null) {
	    for (final StepItem step : report.getSteps().values()) {
		backup.getSteps().put(step.getStepType(), step);
	    }
	}
    }

    /**
     * @return the originalFilepath
     */
    public String getOriginalFilepath() {
	return backup.getOriginalFilepath();
    }

    /**
     *
     * @param stepType
     * @return
     */
    public StepItem getStep(final STEP_TYPE stepType) {
	if (!backup.getSteps().containsKey(stepType)) {
	    backup.getSteps().put(stepType, StepItem.getStepItem(stepType));
	}
	return backup.getSteps().get(stepType);
    }

    /**
     *
     * @param stepType
     * @return
     */
    public StepItem getErrorMessagesInStep(final STEP_TYPE stepType) {
	if (!backup.getSteps().containsKey(stepType)) {
	    return getStep(stepType);
	}

	final StepItem toReturn = StepItem.getStepItem(stepType);
	for (final MessageItem messageItem : backup.getSteps().get(stepType).getMessages()) {
	    if (messageItem.getState() == ITEM_STATE.ERROR) {
		toReturn.addMessage(messageItem);
	    }
	}
	return toReturn;
    }

    /**
     * Get all ended {@link StepItem}.
     *
     * @return
     */
    public Map<STEP_TYPE, StepItem> getSteps() {
	return backup.steps;
    }

    /**
     * Persist the current report into a file.
     *
     * @throws IOException
     */
    public void persist() {
	if (be == null) {
	    try {
		if ((this.backup.getURI() != null) && (backupCache != null)) {
		    backupCache.addNewToCache(backup);
		}
	    } catch (final BackupCacheException e) {
		SC3Logger.LOGGER.warn("Cannot add an element to local cache:" + e);
		be = e;
		JOptionPane.showMessageDialog(null,
			"Impossible de sauvegarder le backup cache: " + Report.getBackupException().getMessage(),
			"Erreur Cache", JOptionPane.ERROR_MESSAGE);
	    }
	} else {
	    JOptionPane.showMessageDialog(null,
		    "Impossible de sauvegarder le backup cache: " + Report.getBackupException().getMessage(),
		    "Erreur Cache", JOptionPane.ERROR_MESSAGE);
	}

    }

    /**
     * Remove an error message
     *
     * @param caErrorMessageItem
     */
    public void removeMessage(final CAErrorMessageItem caErrorMessageItem, final STEP_TYPE stepType) {
	getStep(stepType).removeMessage(caErrorMessageItem);
    }

    /**
     * @param stepType
     * @param setOldMessageDetectedWithLastCA
     */
    public void setOldMessageDetectedWithLastCA(final STEP_TYPE stepType) {
	final StepItem stepItem = getStep(stepType);

	for (final MessageItem messageItem : stepItem.getMessages()) {
	    if (messageItem instanceof CADataErrorMessageItem) {
		((CADataErrorMessageItem) messageItem)
			.setErrorChecked(((CADataErrorMessageItem) messageItem).isErrorChecked());
	    }
	}
    }

    public Backup getBackup() {
	return backup;

    }

    public static BackupCacheException getBackupException() {
	return be;
    }
}
