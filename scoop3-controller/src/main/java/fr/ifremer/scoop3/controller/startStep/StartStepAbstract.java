package fr.ifremer.scoop3.controller.startStep;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fr.ifremer.scoop3.controller.Controller;
import fr.ifremer.scoop3.gui.home.HomeViewController;

public abstract class StartStepAbstract {

    /**
     * Reference on the Controller
     */
    protected final Controller controller;

    /**
     * Result of Thread reader
     */
    protected Future<Boolean> futureBoolean;

    /**
     * Reference on the HomeViewController
     */
    protected final HomeViewController homeViewController;

    /**
     * Default constructor
     *
     * @param controller
     * @param homeViewController
     */
    protected StartStepAbstract(final Controller controller, final HomeViewController homeViewController) {
	this.controller = controller;
	this.homeViewController = homeViewController;
    }

    /**
     * Start the step (on Event reception)
     */
    public void start() {
	// single thread for argo and bpc version
	if (!getRunnableForExecutor().getClass().getName().contains("coriolis")) {
	    // Create a new executor SINGLE thread service (thread are executed one by one)
	    final ExecutorService executor = Executors.newSingleThreadExecutor();

	    // Create the Future object which will return if the reading was successful or not
	    futureBoolean = executor.submit(getCallableBooleanForExecutor());

	    // This thread is executed just after the thread above (because it's a SingleThreadExecutor and not a pool
	    // of thread)
	    executor.execute(getRunnableForExecutor());

	    // Shutdown the executor
	    executor.shutdown();
	}
	// double threads is only for coriolis version
	else {
	    // Create a new executor with 2 threads (thread are executed in parallel)
	    final ExecutorService executor = Executors.newFixedThreadPool(2);

	    executor.execute(getRunnableForExecutor());

	    // Create the Future object which will return if the reading was successful or not
	    futureBoolean = executor.submit(getCallableBooleanForExecutor());

	    // Shutdown the executor
	    executor.shutdown();
	}
    }

    /**
     * @return the Callable Method (witch returns a Boolean) executed by the Executor
     */
    protected abstract Callable<Boolean> getCallableBooleanForExecutor();

    /**
     * @return the Runnable called by the Executor
     */
    protected abstract Runnable getRunnableForExecutor();

}
