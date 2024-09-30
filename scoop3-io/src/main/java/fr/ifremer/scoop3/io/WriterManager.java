package fr.ifremer.scoop3.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bushe.swing.event.EventBus;

import fr.ifremer.scoop3.events.GuiEventBackupIsComplete;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.Dataset;

public abstract class WriterManager {

    private static Boolean closeScoop3AfterWriting = false;
    private static Dataset dataset = null;
    private static Boolean isWrittenInProgress = false;
    private static Boolean requestToWrite = false;
    private static List<Writer> writers = new ArrayList<>();

    /**
     * Memorize to shutdown at the end of the current writing
     */
    public static void closeScoop3AfterWriting() {
	synchronized (closeScoop3AfterWriting) {
	    closeScoop3AfterWriting = true;
	}
	synchronized (isWrittenInProgress) {
	    if (!isWrittenInProgress.booleanValue()) {
		shutdownNow();
	    }
	}
    }

    /**
     * @return the isWrittenInProgress
     */
    public static boolean isWrittenInProgress() {
	boolean toReturn;
	synchronized (isWrittenInProgress) {
	    toReturn = isWrittenInProgress;
	}
	return toReturn;
    }

    /**
     * Register the writer to use
     *
     * @param writer
     */
    public static void registerWriter(final Writer writer) {
	WriterManager.writers.add(writer);
    }

    /**
     * @return the list of writers
     */
    public static List<Writer> getWriters() {
	return writers;
    }

    /**
     * Memorize that the file need to be written
     */
    public static void requestWriteFile(final Dataset dataset) {
	synchronized (requestToWrite) {
	    if (!isWrittenInProgress()) {
		WriterManager.dataset = dataset;
	    }
	    requestToWrite = true;
	}
	if (!isWrittenInProgress()) {
	    startWrittingFile();
	}
    }

    /**
     * Set that there is no more request in progress
     */
    private static void noMoreRequestInProgress() {
	synchronized (requestToWrite) {
	    requestToWrite = false;
	}
    }

    /**
     * System.exit ...
     */
    private static void shutdownNow() {
	SC3Logger.LOGGER.info(Messages.getMessage("gui.scoop3-exit"));
	System.exit(0);
    }

    /**
     * Start to write the file.
     */
    private static void startWrittingFile() {
	noMoreRequestInProgress();
	synchronized (isWrittenInProgress) {
	    isWrittenInProgress = true;
	}

	// Create a new executor SINGLE thread service (thread are executed one by one)
	final ExecutorService executor = Executors.newSingleThreadExecutor();

	// Create the Future object which will return if the reading was successful or not
	final Future<Boolean> futureBoolean = executor.submit(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {
		boolean errorWhileWritting = false;
		SC3Logger.LOGGER.info("### START WRITTING FILE");
		Writer writerForDataset = null;
		for (final Writer writer : writers) {
		    if (writer.isWriterForSourceType(dataset) && (writerForDataset == null)) {
			writerForDataset = writer;
		    }
		}
		if (writerForDataset == null) {
		    SC3Logger.LOGGER.error("NO WRITER FOR \"" + dataset.getSourceType() + "\"");
		    errorWhileWritting = true;
		} else {
		    try {
			writerForDataset.write(dataset);
		    } catch (final Exception e) {
			SC3Logger.LOGGER.error(null, e);
			errorWhileWritting = true;
		    } finally {
			SC3Logger.LOGGER.info("### END WRITTING FILE");
		    }
		}
		return errorWhileWritting;
	    }
	});

	// This thread is executed just after the thread above (because it's a SingleThreadExecutor and not a pool of
	// thread)
	executor.execute(() -> {
	    boolean errorWhileWritting = false;
	    try {
		errorWhileWritting = futureBoolean.get().booleanValue();

		if (!errorWhileWritting) {
		    boolean newRequestToWrite;
		    synchronized (requestToWrite) {
			if (!requestToWrite.booleanValue()) {
			    dataset = null;
			}
			newRequestToWrite = requestToWrite;
		    }
		    // Notify that the backup is complete ...
		    EventBus.publish(new GuiEventBackupIsComplete());

		    if (!errorWhileWritting && newRequestToWrite) {
			startWrittingFile();
		    } else {
			synchronized (isWrittenInProgress) {
			    isWrittenInProgress = false;
			}
		    }
		} else {
		    synchronized (isWrittenInProgress) {
			isWrittenInProgress = false;
		    }
		}
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(null, e);

		synchronized (isWrittenInProgress) {
		    isWrittenInProgress = false;
		}
	    } finally {
		boolean currentIsWrittenInProgress;
		synchronized (isWrittenInProgress) {
		    currentIsWrittenInProgress = isWrittenInProgress;
		}
		synchronized (closeScoop3AfterWriting) {
		    if (!currentIsWrittenInProgress && closeScoop3AfterWriting.booleanValue()) {
			shutdownNow();
		    }
		}
	    }
	});

	// Shutdown the executor
	executor.shutdown();
    }
}
