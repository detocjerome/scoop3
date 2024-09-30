package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util;

public class Preconditions {

    private Preconditions() {
    }

    public static void checkArgument(final boolean expression) {
	if (!expression) {
	    throw new IllegalArgumentException();
	}
    }

    public static void checkArgument(final boolean expression, final String message) {
	if (!expression) {
	    throw new IllegalArgumentException(String.valueOf(message));
	}
    }

    public static <T> T checkNotNull(final T ref) {
	if (ref == null) {
	    throw new NullPointerException();
	}
	return ref;
    }

    public static <T> T checkNotNull(final T ref, final String message) {
	if (ref == null) {
	    throw new NullPointerException(String.valueOf(message));
	}
	return ref;
    }

}
