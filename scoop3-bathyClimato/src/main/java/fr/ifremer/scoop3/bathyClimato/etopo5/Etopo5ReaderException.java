package fr.ifremer.scoop3.bathyClimato.etopo5;

public class Etopo5ReaderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 80673046660524835L;
    public static final String CREATE_NEW_ETOPO5READER = "Can't create a new Etopo5Reader";

    public Etopo5ReaderException(final String message) {
	super(message);
    }

    public Etopo5ReaderException() {
	super();
    }

    public Etopo5ReaderException(final Throwable cause) {
	super(cause);
    }

    public Etopo5ReaderException(final String message, final Throwable cause) {
	super(message, cause);
    }

}
