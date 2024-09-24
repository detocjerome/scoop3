package fr.ifremer.scoop3.infra.mail;

public class UnhandledException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 8699675529238404638L;
    private final Exception e;
    private final String message;

    public UnhandledException(final String message, final Exception e) {
	this.message = message;
	this.e = e;
	Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), this);
    }

    public String getUnhandledMessage() {
	return message + " / error : " + e;
    }
}
