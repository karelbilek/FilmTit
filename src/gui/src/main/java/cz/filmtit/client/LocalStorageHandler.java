package cz.filmtit.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;

import cz.filmtit.client.callables.SetUserTranslation;

public class LocalStorageHandler {

    // TODO: a bunch of Dialogs instead of the Alerts and Confirms.
	
	// TODO: use a special object stored in the Storage as a descriptor of the data in the storage
	// probably under the userID as the key
	// (but beware because the user can modify the contents of the Storage)

	private static Storage storage = Storage.getLocalStorageIfSupported();
	
	private static boolean isStorageSupported = Storage.isLocalStorageSupported();
	
    public static boolean isStorageSupported() {
		return isStorageSupported;
	}

	/**
	 * Number of yet unfinished calls.
	 */
	private static int yetToUpload;
	
	public static void decrementYetToUpload () {
		yetToUpload--;
		if (yetToUpload == 0) {
			uploading = false;
			Gui.log("All requests from local storage returned!");
			if (numberOfItemsInLocalStorage() == 0) {
				Window.alert("All relevant items stored from Offline Mode " +
				"have been successfully saved!");				
			}
			else {
				Window.alert("All relevant items stored from Offline Mode " +
				"have been successfully saved! " +
				"However, there are still " + numberOfItemsInLocalStorage() +
				" items that could not be stored, " +
				"probably because they belong to another user.");
			}
		}
	}
	
	/**
	 * Whether the user is in online mode (default) or offline mode (experimental now).
	 */
	private static boolean online = true;
	
	/**
	 * Whether the user is now uploading data from offline mode.
	 */
	private static boolean uploading = false;
	
	public static boolean isUploading() {
		return uploading;
	}

	public static boolean isOnline() {
		return online;
	}

	public static void setOnline(boolean online) {
		LocalStorageHandler.online = online;
		
		if (online) {
			// going online
			final int itemsNo = numberOfItemsInLocalStorage();
			if (itemsNo > 0) {
				boolean loadItems = Window.confirm("Welcome online! " +
						"There are " + itemsNo + " items stored in your browser " +
						"from the Offline Mode. " +
						"Do you want to upload them to the server now?");
				if (loadItems) {
					uploading = true;
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							
							Gui.log("Loading " + itemsNo + " items from local storage...");
							List<Storable> objects = loadUserObjectsFromLocalStorage();
							yetToUpload = objects.size();
							Gui.log("Loaded " + yetToUpload + " items from local storage.");
														
							if (yetToUpload > 0) {
								// TODO RepeatingCommand
								for (Storable object : objects) {
									object.onLoadFromLocalStorage();
								}
							}
							else {
								Window.alert("No items were stored. " +
										"There are still " + numberOfItemsInLocalStorage() +
										" items that could not be stored, " +
										"probably because they belong to another user.");
							}
							
							Gui.log("Sent " + yetToUpload + " requests.");
						}
					});
				}
			}
		}
		else {
			// going offline
			if (Storage.isLocalStorageSupported()) {
				// TODO: disable the menu etc.
				Window.alert("Welcome to Offline Mode! " +
						"You can continue with your translation, " +
						"all your input will be saved in your browser " +
						"even if you close it and turn off your computer. " +
						"(However, if you close the translation page, " +
						"you will not be able to reopen it until you go online again!) " +
						"Once you go back online, please log in and follow the instructions " +
						"that will appear.");
			}
			else {
				Window.alert("Unfortunately, Offline Mode is not supported for your browser. " +
						"You need a browser that supports the Storage API. " +
						"(Most of the new versions of browsers support this.)");
				online = false;
			}
		}
	}
	
	public static boolean offerOfflineStorage() {
		boolean useOfflineStorage = false;
		if (Storage.isLocalStorageSupported()) {
			useOfflineStorage = Window.confirm(
					"Either your computer is offline or the server is down. " +
					"Do you want to go into Offline Mode? " +
					"You can continue with your translations in Offline Mode, " +
					"your work will be saved in your browser " +
					"and uploaded to the server once you go online again."
				);
		}
		
		if (useOfflineStorage) {
			setOnline(false);
		}
		else {
			Window.alert("There is no connection to the server. " +
					"It is not possible to continue with the translation at the moment. " +
					"All translations you have done so far are safely stored on the server " +
					"(except for the last one) " +
					"but please do not fill in any new ones now as they would be lost. " +
					"Please try again later.");
		}
		
		return useOfflineStorage;
	}
	
	/**
     * 
     * @return Number of items found in local storage (can be 0).
     */
	public static int numberOfItemsInLocalStorage() {
    	if (isStorageSupported) {
    		return storage.getLength();
    	}
    	else {
    		return 0;
    	}
	}

	/**
	 * a separator of username at the end of the key
	 */
	public static final String USERNAME_SEPARATOR = "@";
	
	/**
	 * a separator of class at the end of the key but before the username
	 */
	public static final String CLASSID_SEPARATOR = ":";
	
	/**
	 * A separator of the individual fields in the value,
	 * and in the key
	 * after class and username are stripped off
	 */
	public static final String FIELDS_SEPARATOR = ";";
	
	/**
	 * Loads all objects
	 * belonging to the current user
	 * from the local storage.
	 */
	private static List<Storable> loadUserObjectsFromLocalStorage() {
		if (isStorageSupported) {
			List<Storable> objects = new LinkedList<Storable>();
			// go through all items
			for (int i = 0; i < storage.getLength(); i++) {
				String key = storage.key(i);
				Gui.log("Found item with key " + key);
				String[] keyFields = key.split(USERNAME_SEPARATOR, 2);
				// check the key
				if (keyFields.length == 2) {
					String key_without_username = keyFields[0];
					String username = keyFields[1];
					// check username
					if (username.equals(Gui.getUsername())) {
						String value = storage.getItem(key);
						Gui.log("Creating that item with value " + value);
						Storable object = loadObject(key_without_username, value);
						// check that object was successfully created
						if (object != null) {
							objects.add(object);
						}
					}
				}
			}
			return objects;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Determines the class of the object and loads it.
	 * @param key_with_class_id
	 * @param value
	 * @return The object on success, null otherwise.
	 */
	private static Storable loadObject(String key_with_class_id, String value) {
		Storable object = null;
		
		String[] keyFields = key_with_class_id.split(CLASSID_SEPARATOR, 2);
		// check the key
		if (keyFields.length == 2) {
	    	String key = keyFields[0];
	    	String classId = keyFields[1];
	    	// class switch
	    	if (classId.equals(SetUserTranslation.CLASS_ID)) {
				object = SetUserTranslation.fromKeyValuePair(new KeyValuePair(key, value));
			}
			// else if other class...
		}

		return object;
	}
	
	/**
	 * Stores the object into the local storage.
	 * @return true if the object was successfully saved, false if not
	 */
    public static boolean storeInLocalStorage (Storable storable) {
    	if (isStorageSupported) {
    		KeyValuePair keyValuePair = storable.toKeyValuePair();
    		String key = fullKey(keyValuePair.getKey(), storable.getClassID());
    		String value = keyValuePair.getValue();
    		storage.setItem(key, value);
    		// check
    		if (storage.getItem(key).equals(value)) {
    			return true;
    		}
    		else {
    			return false;
    		}
		}
    	else {
			return false;
		}
	}
	
	/**
	 * Removes the object from the local storage.
	 */
    public static void removeFromLocalStorage (Storable storable) {
    	if (Storage.isLocalStorageSupported()) {
    		KeyValuePair keyValuePair = storable.toKeyValuePair();
    		String key = fullKey(keyValuePair.getKey(), storable.getClassID());
    		storage.removeItem(key);
		}
	}
    
    /**
     * Add class id and username to the key.
     * @param key
     * @return
     */
    private static String fullKey (String key, String classId) {
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(key);
		keyBuilder.append(CLASSID_SEPARATOR);
		keyBuilder.append(classId);
		keyBuilder.append(USERNAME_SEPARATOR);
		keyBuilder.append(Gui.getUsername());
		return keyBuilder.toString();
    }
	
}
