package fr.ifremer.scoop3.bathyClimato.util;

/**
 * Exception lev�e en cas d'erreur associ� � la configuration.
 * @author vfachero
 */
public class ConfigException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur par d�faut.
	 * @param message le message d'erreur.
	 * @param cause la cause de l'erreur.
	 */
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
