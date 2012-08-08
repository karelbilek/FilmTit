package cz.filmtit.client.callables;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;

import java.util.*;

public class SetUserTranslation extends Callable<Void> {
	
	// parameters
	ChunkIndex chunkIndex;
	long documentId;
	String userTranslation;
	long chosenTranslationPair;


    @Override
    public String getName() {
        return "setUserTranslation("+chunkIndex.toString()+","+documentId+","+userTranslation+","+chosenTranslationPair+")";
    }

    @Override
    public void onSuccessAfterLog(Void o) {
    	if (uploading) {
    		removeFromLocalStorage();
    		decrementYetToUpload();
    	}
    }
    
    // constructor
	public SetUserTranslation(ChunkIndex chunkIndex, long documentId,
			String userTranslation, long chosenTranslationPair) {		
		super();
		
		this.chunkIndex = chunkIndex;
		this.documentId = documentId;
		this.userTranslation = userTranslation;
		this.chosenTranslationPair = chosenTranslationPair;
		
        enqueue();
	}

	@Override protected void call() {
		if (isOnline()) {
			filmTitService.setUserTranslation(gui.getSessionID(), chunkIndex,
					documentId, userTranslation, chosenTranslationPair,
					this);
		}
		else {
			hasReturned = storeInLocalStorage();
			if (hasReturned) {
				gui.log("Saved to local storage: " + keyValuePair);
			} else {
				gui.log("ERROR: Cannot save to local storage: " + keyValuePair);
				displayWindow("ERROR: Cannot save to local storage: " + keyValuePair);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
    // local storage
	
	// TODO: a bunch of Dialogs instead of the Alerts and Confirms.
	
	/**
	 * Whether the user is in online mode (default) or offline mode (experimental now).
	 */
	private static boolean online = true;
	
	/**
	 * Whether the user is now uploading data from offline mode.
	 */
	private static boolean uploading = false;
	
	public static boolean isOnline() {
		return online;
	}

	public static void setOnline(boolean online) {
		SetUserTranslation.online = online;
		
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
							Gui.getGui().log("Loading " + itemsNo + " items from local storage...");
							List<SetUserTranslation> calls = loadFromLocalStorage(false);
							yetToUpload = calls.size();
							Gui.getGui().log("Loaded " + yetToUpload + " items from local storage.");
							// TODO RepeatingCommand
							for (SetUserTranslation setUserTranslation : calls) {
								setUserTranslation.enqueue();
							}
							Gui.getGui().log("Sent " + yetToUpload + " requests.");
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
				SetUserTranslation.online = false;
			}
		}
	}

	/**
	 * Number of yet unfinished calls.
	 */
	private static int yetToUpload;
	
	private static void decrementYetToUpload () {
		yetToUpload--;
		if (yetToUpload == 0) {
			uploading = false;
			Gui.getGui().log("All requests from local storage returned!");
			Window.alert("All relevant items stored from Offline Mode " +
					"have been successfully saved!");
		}
	}
	
	@Override
	protected void onProbablyOffline(Throwable returned) {
		if (isOnline()) {
			offerOfflineStorage();
		}
		else {
			storeInLocalStorage();
		}
	}
	
	@Override
	protected void onTimeOut() {
		if (isOnline()) {
			offerOfflineStorage();
		}
		else {
			storeInLocalStorage();
		}
	}
	
	private void offerOfflineStorage() {
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
			storeInLocalStorage();
		}
		else {
			Window.alert("There is no connection to the server. " +
					"It is not possible to continue with the translation at the moment. " +
					"All translations you have done so far are safely stored on the server " +
					"(except for the last one: '" + userTranslation + "') " +
					"but please do not fill in any new ones now as they would be lost. " +
					"Please try again later.");
		}

	}

	/**
	 * a cache for the key value pair to avoid repeated generation thereof
	 */
	private KeyValuePair keyValuePair;
	
	/**
	 * a constructor to be used in local storage retrieval,
	 * also setting the keyValuePair
	 * and not invoking the RPC yet
	 */
	private SetUserTranslation(ChunkIndex chunkIndex, long documentId,
			String userTranslation, long chosenTranslationPair, KeyValuePair keyValuePair) {		
		super();
		
		this.chunkIndex = chunkIndex;
		this.documentId = documentId;
		this.userTranslation = userTranslation;
		this.chosenTranslationPair = chosenTranslationPair;
		this.keyValuePair = keyValuePair;
	}
	
	
	/**
	 * Stores this object into the local storage.
	 * @return true if the object was successfully saved, false if not
	 */
    private boolean storeInLocalStorage () {
    	if (Storage.isLocalStorageSupported()) {
    		Storage storage = Storage.getLocalStorageIfSupported();
    		KeyValuePair keyValuePair = toKeyValuePair();
    		storage.setItem(keyValuePair.getKey(), keyValuePair.getValue());
//    		return true;
    		if (storage.getItem(keyValuePair.getKey()).equals(keyValuePair.getValue())) {
    			return true;
    		} else {
    			return false;
    		}
		} else {
			return false;
		}
	}
    
	/**
	 * Removes this object from the local storage.
	 */
    private void removeFromLocalStorage () {
    	if (Storage.isLocalStorageSupported()) {
    		Storage storage = Storage.getLocalStorageIfSupported();
    		KeyValuePair keyValuePair = toKeyValuePair();
    		storage.removeItem(keyValuePair.getKey());
		}
	}
    
    /**
     * 
     * @return Number of items found in local storage (can be 0).
     */
	private static int numberOfItemsInLocalStorage() {
    	if (Storage.isLocalStorageSupported()) {
    		Storage storage = Storage.getLocalStorageIfSupported();
    		return storage.getLength();
    	}
    	else {
    		return 0;
    	}
	}
	
    /**
     * Load all instances of this class from the local storage,
     * possibly removing them from the storage.
     * @param remove true to automatically remove returned instances from the storage,
     * false to keep them there
     * @return A list of all instances of this class retrieved from the local storage (possibly empty),
     * or null if local storage is not supported.
     */
	private static List<SetUserTranslation> loadFromLocalStorage (boolean remove) {
    	if (Storage.isLocalStorageSupported()) {
    		Storage storage = Storage.getLocalStorageIfSupported();
    		// load all key value pairs
    		List<KeyValuePair> keyValuePairs = loadObjectsFromLocalStorage(storage);
    		// pop key value pairs that represent objects of this class
    		List<SetUserTranslation> result = new LinkedList<SetUserTranslation>();
    		for (KeyValuePair keyValuePair : keyValuePairs) {
				SetUserTranslation setUserTranslation = SetUserTranslation.fromKeyValuePair(keyValuePair);
				if (setUserTranslation != null) {
					result.add(setUserTranslation);
					if(remove) {
						storage.removeItem(keyValuePair.getKey());
					}					
				}
			}
    		return result;
		} else {
			return null;
		}		
	}

	// TODO: have a StorageHandler as middleware
	// TODO: use a special object stored in the Storage as a descriptor of the data in the storage
	// probably under the userID as the key
	/**
	 * Loads all objects from the local storage.
	 * @param storage
	 * @return
	 */
	private static List<KeyValuePair> loadObjectsFromLocalStorage(Storage storage) {
		List<KeyValuePair> keyValuePairs = new LinkedList<KeyValuePair>();
		for (int i = 0; i < storage.getLength(); i++) {
			String key = storage.key(i);
			String value = storage.getItem(key);
			keyValuePairs.add(new KeyValuePair(key, value));
		}
		return keyValuePairs;
	}
	
    
	private static final String CLASS_ID = "SetUserTranslation";
	private static final String SEPARATOR = ";";
	
	// TODO: add userID to the key
	/**
	 * Convert this SetUserTranslation object into a key value pair.
	 * The format is: <br>
	 * key = SetUserTranslation;documentId;chunkId;partNumber <br>
	 * value = chosenTranslationPair;userTranslation
	 * @param keyValuePair
	 * @return the SetUserTranslation if the parsing is successful, null otherwise
	 */
    private KeyValuePair toKeyValuePair() {
    	if (keyValuePair == null) {
    		// we don't have the key value pair yet, we must generate it
    		
    		// key fields
			StringBuilder key = new StringBuilder();
			key.append(CLASS_ID);
			key.append(SEPARATOR);
			key.append(documentId);
			key.append(SEPARATOR);
			key.append(chunkIndex.getId());
			key.append(SEPARATOR);
			key.append(chunkIndex.getPartNumber());
			
			// value fields
			StringBuilder value = new StringBuilder();
			value.append(chosenTranslationPair);
			value.append(SEPARATOR);
			value.append(userTranslation);
			
			keyValuePair = new KeyValuePair(key.toString(), value.toString());
    		return keyValuePair;
    	}
    	else {
        	// else use the already generated key value pair
    		return keyValuePair;
    	}
	}

	// TODO: add userID to the key
	/**
	 * Try to parse the given key value pair as a SetUserTranslation object.
	 * The format is: <br>
	 * key = documentId;chunkId;partNumber <br>
	 * value = chosenTranslationPair;userTranslation
	 * @param keyValuePair
	 * @return the SetUserTranslation if the parsing is successful, null otherwise
	 */
    private static SetUserTranslation fromKeyValuePair(KeyValuePair keyValuePair) {
    	try {
    		// key fields
	    	String[] keyFields = keyValuePair.getKey().split(SEPARATOR, 4);
	    	// class check
	    	String classId = keyFields[0];
	    	if (classId.equals(CLASS_ID)) {
	    		// key fields
		    	long documentId = Long.parseLong(keyFields[1]);
				int chunkId = Integer.parseInt(keyFields[2]);
		    	int partNumber = Integer.parseInt(keyFields[3]);
				ChunkIndex chunkIndex = new ChunkIndex(partNumber, chunkId);
				
				// value fields
		    	String[] valueFields = keyValuePair.getValue().split(SEPARATOR, 2);
		    	long chosenTranslationPair = Long.parseLong(valueFields[0]);
		    	String userTranslation = valueFields[1];
		    	
		    	return new SetUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPair, keyValuePair);
	    	} else {
	    		return null;
	    	}
    	}
    	catch (Exception e) {
    		Gui.getGui().log(
    				"Parsing error in SetUserTranslation.fromKeyValuePair(" +
    				keyValuePair.getKey() + "," +
    				keyValuePair.getValue() + "): " +
    				e.toString()
				);
			return null;
		}
    }
    

}

