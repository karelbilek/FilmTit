/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;

import cz.filmtit.client.callables.SetUserTranslation;
import cz.filmtit.client.dialogs.Dialog;
import cz.filmtit.client.dialogs.GoingOfflineDialog;
import cz.filmtit.client.dialogs.GoingOnlineDialog;

/**
 * A static class that serves as a wrapper for Local Storage,
 * providing methods to read objects from it and write objects to it,
 * using the Storable interface.
 * All manipulation with the Local Storage is to be done through this class.
 * @author rur
 *
 */
public class LocalStorageHandler {

	// TODO: use a special object stored in the Storage as a descriptor of the data in the storage
	// probably under the userID as the key
	// (but beware because the user can modify the contents of the Storage)

	private static Storage storage = Storage.getLocalStorageIfSupported();
	
	private static boolean isStorageSupported = Storage.isLocalStorageSupported();
	
	/**
	 * Checks the support for the Local Storage in the user's browser.
	 * @return true if Local Storage can be used.
	 */
    public static boolean isStorageSupported() {
		return isStorageSupported;
	}

	/**
	 * Number of yet unfinished calls.
	 */
	private static int yetToUpload;
	
	private static void decrementYetToUpload () {
		yetToUpload--;
		if (yetToUpload == 0) {
			uploading = false;
			Gui.log("All requests from local storage returned!");
			Gui.getPageHandler().refresh();
			if (failedCount == 0) {
				goingOnlineDialog.reactivateWithInfoMessage("All " + succeededCount + " items have been successfully saved!");
			}
			else {
				StringBuilder sb = new StringBuilder();
				if (succeededCount > 0) {
					sb.append(succeededCount);
					sb.append(" items have been successfully saved!\n ");
					sb.append("However, ");
				}
				else {
					sb.append("No items saved! ");
				}
				sb.append(failedCount);
				sb.append(" items could not be stored. Error message from the server: ");
				for (Object error : failedMessages.keySet().toArray()) {
					sb.append('\n');
					sb.append(error);
				}
				goingOnlineDialog.reactivateWithErrorMessage(sb.toString());
			}
		}
	}
	
	private static int succeededCount;
	
	/**
	 * To be called by a Storable after invoking its onLoadFromLocalStorage() method
	 * if the method succeeds.
	 * @param object The Storable is to pass a self-reference as a parameter.
	 */
	public static void SuccessOnLoadFromLocalStorage (Storable object) {
		removeFromLocalStorage(object);
		succeededCount++;
		decrementYetToUpload();
	}
	
	private static int failedCount;
	private static List<Storable> failedObjects;
	private static Map<String, Integer> failedMessages;
	
	/**
	 * To be called by a Storable after invoking its onLoadFromLocalStorage() method
	 * if the method fails.
	 * @param object The Storable is to pass a self-reference as a parameter.
	 * @param errorMessage A message describing the error that occurred.
	 */
	public static void FailureOnLoadFromLocalStorage (Storable object, String errorMessage) {
		failedCount++;
		failedObjects.add(object);
		Integer count = failedMessages.get(errorMessage);
		if (count != null) {
			failedMessages.put(errorMessage, count+1);
		}
		else {
			failedMessages.put(errorMessage, 1);			
		}
		decrementYetToUpload();
	}
	
	/**
	 * Whether the user is in the Online Mode (default) or the Offline Mode.
	 */
	private static boolean online = true;
	
	/**
	 * Whether the user is in the Online Mode (default) or the Offline Mode.
	 */
	public static boolean isOnline() {
		return online;
	}

	/**
	 * Whether the user is now uploading data from offline mode.
	 */
	private static boolean uploading = false;
	
	/**
	 * Whether the user is now uploading data from offline mode.
	 */
	public static boolean isUploading() {
		return uploading;
	}

	/**
	 * Whether GoingOfflineDialog is currently open.
	 */
	private static boolean offeringOfflineStorage = false;
	
	/**
	 * Whether GoingOfflineDialog is currently open.
	 */
	public static boolean isOfferingOfflineStorage() {
		return offeringOfflineStorage;
	}
	
	/**
	 * Whether GoingOfflineDialog has been shown to the user.
	 */
	private static boolean offeredOfflineStorage = false;
	
	/**
	 * Temporary queue of calls that failed because of the problems with connection to server.
	 */
	public static List<Storable> queue;
	
	/**
	 * The objects that belong to the current user and should be uploaded.
	 */
	private static List<KeyValuePair> userObjects;

	/**
	 * The dialog that is used to issue commands on going online.
	 */
	private static Dialog goingOnlineDialog;

	/**
	 * Switch the online/offline mode.
	 * When going offline, the Offline Mode is initialized
	 * and all Storables are saved into Local Storage instead of sending them to the server.
	 * When going online, Local Storage is inspected for saved Storables,
	 * and if some are found, the user is offered to upload them.
	 * @param online
	 */
	public static void setOnline(boolean online) {
		LocalStorageHandler.online = online;
		
		if (online) {
			// going online
			final int itemsNo = numberOfItemsInLocalStorage();
			if (itemsNo > 0) {
				Gui.log("Inspecting " + itemsNo + " items from local storage...");
				userObjects = loadUserObjectsFromLocalStorage();
				int count = (userObjects == null ? 0 : userObjects.size());
				Gui.log("Found " + count + " items from local storage.");
											
				if (count > 0) {
					goingOnlineDialog = new GoingOnlineDialog(count);
				}
			}
		}
		else {
			// going offline
			offeringOfflineStorage = false;
			offeredOfflineStorage = true;
			if (Storage.isLocalStorageSupported()) {
				Gui.getGuiStructure().offline_mode();
				for (Storable storableInError : queue) {
					storeInLocalStorage(storableInError);
				}
				queue = null;
			}
			else {
				Window.alert("Unfortunately, Offline Mode is not supported for your browser. " +
						"You need a browser that supports the Storage API. " +
						"(Most of the new versions of browsers support this.)");
				online = false;
			}
		}
	}
	
	/**
	 * Display a dialog asking the user whether they want to turn on the Offline Mode.
	 * To be used if the user appears to be offline,
	 * which is detected by a call failing with onProbablyOffline;
	 * use only if the call implements Storable.
	 * @param storableInError The Storable that decided to offer the user the Offline Mode;
	 * will be stored offline if the user decides to turn the Offline Mode on.
	 */
	public static void offerOfflineStorage(Storable storableInError) {
		if (queue == null) {
			queue = new LinkedList<Storable>();
		}
		queue.add(storableInError);
		if (Storage.isLocalStorageSupported() && !offeredOfflineStorage) {
			offeringOfflineStorage = true;
			goingOnlineDialog = new GoingOfflineDialog();
		}
		else {
			Window.alert("There is no connection to the server. " +
					"It is not possible to continue with the translation at the moment. " +
					"All translations you have done when online are safely stored on the server " +
					"(except for the last one or two, including '" + storableInError.toUserFriendlyString() + "') " +
					"but please do not fill in any new ones now as they would be lost. " +
					"Please try again later when the connection to the server is available again.");
		}
	}
	
	/**
	 * This method is used by the GoingOfflineDialog to signal that the user cancelled the dialog.
	 */
	public static void cancelledOfflineStorageOffer () {
		offeringOfflineStorage = false;
		offeredOfflineStorage = true;
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
	 * a separator of userid at the end of the key
	 */
	public static final String USERID_SEPARATOR = "@";
	
	/**
	 * a separator of class at the end of the key but before the user id
	 */
	public static final String CLASSID_SEPARATOR = ":";
	
	/**
	 * A separator of the individual fields in the value,
	 * and in the key
	 * after class and user id are stripped off
	 */
	public static final String FIELDS_SEPARATOR = ";";
	
	/**
	 * Loads all objects
	 * belonging to the current user
	 * from the local storage.
	 * @return A list of KeyValuePairs of objects belonging to the current user, with userID stripped from the key,
	 * or null if LocalStorage is not supported.
	 */
	private static List<KeyValuePair> loadUserObjectsFromLocalStorage() {
		if (isStorageSupported) {
			List<KeyValuePair> objects = new LinkedList<KeyValuePair>();
			List<KeyValuePair> corrupted = new LinkedList<KeyValuePair>();
			String currentUserId = Long.toString(Gui.getUserID());
			// go through all items
			for (int i = 0; i < storage.getLength(); i++) {
				String key = storage.key(i);
				Gui.log("Found item with key " + key);
				String[] keyFields = key.split(USERID_SEPARATOR, 2);
				// check the key
				if (keyFields.length == 2) {
					String userid = keyFields[0];
					String key_without_userid = keyFields[1];
					// check userid
					if ( userid.equals(currentUserId) ) {
						String value = storage.getItem(key);
						objects.add(new KeyValuePair(key_without_userid, value));
					}
					// else somebody elses object, just keep it
				}
				else {
					corrupted.add(new KeyValuePair(key, storage.getItem(key)));
				}
			}
			for (KeyValuePair keyValuePair : corrupted) {
				Gui.log("Removing corrupted item " + keyValuePair);
				storage.removeItem(keyValuePair.getKey());
			}
			return objects;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Takes the objects loaded from Local Storage
	 * and invokes their onLoadFromLocalStorage() methods.
	 * Called by the GoingOnlineDialog if the user decides
	 * to upload objects stored in Local Storage from Offline Mode.
	 */
	public static void uploadUserObjects() {
		
		initUploadVariables(userObjects.size());
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				// convert the objects
				List<Storable> objects = convertToStorable(userObjects);
				// upload the objects
				// TODO RepeatingCommand
				for (Storable object : objects) {
					object.onLoadFromLocalStorage();
				}
				Gui.log("Sent " + objects.size() + " requests.");
			}
		});
		
	}
	
	/**
	 * Takes the objects loaded from Local Storage
	 * and invokes their onLoadFromLocalStorage() methods.
	 * Called by the GoingOnlineDialog if the user decides
	 * to retry uploading objects which did not succeed at first.
	 */
	public static void retryUploadUserObjects() {
		
		final List<Storable> objects = new LinkedList<Storable>(failedObjects);
		
		initUploadVariables(objects.size());
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				// upload the objects
				// TODO RepeatingCommand
				for (Storable object : objects) {
					object.onLoadFromLocalStorage();
				}
				Gui.log("Sent " + objects.size() + " requests.");
			}
		});
	}
	
	private static void initUploadVariables(int count) {
		uploading = true;
		yetToUpload = count;
		succeededCount = 0;
		failedCount = 0;
		failedObjects = new LinkedList<Storable>();	
		failedMessages = new HashMap<String, Integer>();
	}
	
	/**
	 * Takes the objects loaded from Local Storage
	 * and deletes them from the Storage.
	 * Called by the GoingOnlineDialog if the user decides
	 * to delete objects which did not succeed at first.
	 */
	public static void deleteFailedObjects() {
		for (Storable failedObject : failedObjects) {
			removeFromLocalStorage(failedObject);
		}
		failedObjects = null;
		goingOnlineDialog.reactivateWithInfoMessage("All " + failedCount + " items have been deleted!");
		// goingOnlineDialog.reactivateWithErrorMessage - not used
	}
	
	/**
	 * Loads all objects
	 * belonging to the current user
	 * from the local storage.
	 * The key must contain the class_id
	 * and not contain the userid,
	 * i.e. classID:key
	 */
	private static List<Storable> convertToStorable(List<KeyValuePair> keyValuePairs) {
		List<Storable> objects = new LinkedList<Storable>();
		// go through all items
		for (KeyValuePair keyValuePair : keyValuePairs) {
			Gui.log("Creating item " + keyValuePair);
			Storable object = loadObject(keyValuePair);
			// check that object was successfully created
			if (object != null) {
				objects.add(object);
			}
			else {
				Gui.log("Removing corrupted item " + keyValuePair);
				storage.removeItem(Gui.getUserID() + USERID_SEPARATOR + keyValuePair.getKey());
			}
		}
		return objects;
	}
	
	/**
	 * Determines the class of the object and loads it.
	 * The key must contain the class_id
	 * and not contain the user id,
	 * i.e. have the form classID:key
	 * @return The object on success, null otherwise.
	 */
	private static Storable loadObject(KeyValuePair keyValuePair) {
		Storable object = null;
		
		String[] keyFields = keyValuePair.getKey().split(CLASSID_SEPARATOR, 2);
		// check the key
		if (keyFields.length == 2) {
	    	String classId = keyFields[0];
	    	String key = keyFields[1];
	    	// class switch
	    	if (classId.equals(SetUserTranslation.CLASS_ID)) {
				object = SetUserTranslation.fromKeyValuePair(new KeyValuePair(key, keyValuePair.getValue()));
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
     * Add class id and user id to the key.
     * @return userID@classID:key
     */
    private static String fullKey (String key, String classID) {
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(Gui.getUserID());
		keyBuilder.append(USERID_SEPARATOR);
		keyBuilder.append(classID);
		keyBuilder.append(CLASSID_SEPARATOR);
		keyBuilder.append(key);
		return keyBuilder.toString();
    }

}
