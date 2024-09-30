package fr.ifremer.scoop3.bathyClimato.gebco;

public class GebcoReaderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -5224029889625307622L;
    public static final String CREATE_NEW_GEBCOREADER = "Can't create a new GebcoReader";

    public GebcoReaderException(final String message) {
	super(message);
    }

    public GebcoReaderException() {
	super();
    }

    public GebcoReaderException(final Throwable cause) {
	super(cause);
    }

    public GebcoReaderException(final String message, final Throwable cause) {
	super(message, cause);
    }
}
