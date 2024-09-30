package fr.ifremer.scoop3.bathyClimato.etopo1;

public class Etopo1ReaderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2924633955664075363L;
    public static final String CREATE_NEW_ETOPO1READER = "Can't create a new Etopo1Reader";

    public Etopo1ReaderException(final String message) {
	super(message);
    }

    public Etopo1ReaderException() {
	super();
    }

    public Etopo1ReaderException(final Throwable cause) {
	super(cause);
    }

    public Etopo1ReaderException(final String message, final Throwable cause) {
	super(message, cause);
    }

}
