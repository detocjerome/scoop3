package fr.ifremer.scoop3.infra.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public abstract class SC3Logger {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(SC3Logger.class);

    public static final Logger PERF4JLOGGER = LoggerFactory.getLogger("org.perf4j.TimingLogger");

    public static void main(final String[] args) {
	LOGGER.debug(LoggerFactory.getILoggerFactory().toString());

	// assume SLF4J is bound to logback in the current environment
	if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
	    final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	    // print logback's internal status
	    StatusPrinter.print(lc);
	}

	final String URL = "logback.xml";
	SC3Logger.LOGGER.debug(ClassLoader.getSystemResource(URL).toString());

	SC3Logger.LOGGER.debug("ROOT_LOGGER_NAME : " + LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));

	SC3Logger.LOGGER.debug("is error enabled? " + LOGGER.isErrorEnabled());
	SC3Logger.LOGGER.debug("is warn enabled? " + LOGGER.isWarnEnabled());
	SC3Logger.LOGGER.debug("is info enabled? " + LOGGER.isInfoEnabled());
	SC3Logger.LOGGER.debug("is debug enabled? " + LOGGER.isDebugEnabled());
	SC3Logger.LOGGER.debug("is trace enabled? " + LOGGER.isTraceEnabled());

	LOGGER.debug("TEST DEBUG");
	LOGGER.info("TEST INFO");
	LOGGER.trace("TEST TRACE");
    }
}
