package cz.filmtit.client;

/**
 * Provides methods to access the dialogs in FilmTit.
 * To be implemented by each dialog.
 * All dialogs should be accessed only through this interface.
 * @author rur
 *
 */
public interface Dialog {
	
	/**
	 * Temporarily prevent the user from using the dialog,
	 * but do not hide it.
	 * Used to block the user from doing anything
	 * e.g. while waiting for an RPC call to complete.
	 */
	public void deactivate();
	
	/**
	 * Activate the dialog, showing an error message to the user.
	 * @param message
	 */
	public void reactivateWithErrorMessage(String message);
	
	/**
	 * Hide the dialog.
	 */
	public void close();
	
}
