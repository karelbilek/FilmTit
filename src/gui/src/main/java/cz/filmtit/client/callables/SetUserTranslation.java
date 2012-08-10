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

public class SetUserTranslation extends Callable<Void> implements Storable {
	
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
    	if (LocalStorageHandler.isUploading()) {
    		LocalStorageHandler.SuccessOnLoadFromLocalStorage(this);
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
		if (LocalStorageHandler.isOnline()) {
			filmTitService.setUserTranslation(gui.getSessionID(), chunkIndex,
					documentId, userTranslation, chosenTranslationPair,
					this);
		}
		else {
			hasReturned = LocalStorageHandler.storeInLocalStorage(this);
			if (hasReturned) {
				gui.log("Saved to local storage: " + keyValuePair);
			} else {
				gui.log("ERROR: Cannot save to local storage: " + keyValuePair);
				displayWindow("ERROR: Cannot save to local storage: " + keyValuePair);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
    // local storage
	
	/**
	 * An ID used by the LocalStorageHandler to identify object of this class.
	 */
	public static final String CLASS_ID = "SetUserTranslation";
	
	@Override
	public String getClassID() {
		return CLASS_ID;
	}
	
	/**
	 * a cache for the key value pair to avoid repeated generation thereof
	 */
	private KeyValuePair keyValuePair;
	
	@Override
	protected void onProbablyOffline(Throwable returned) {
		onOfflineOrTimeout();
	}
	
	@Override
	protected void onTimeOut() {
		onOfflineOrTimeout();
	}
	
	private void onOfflineOrTimeout() {
		if (LocalStorageHandler.isUploading()) {
			LocalStorageHandler.FailureOnLoadFromLocalStorage(this, "The connection to server is not working.");
		}
		else if (LocalStorageHandler.isOfferingOfflineStorage()) {
			LocalStorageHandler.queue.add(this);
		}
		else if (LocalStorageHandler.isOnline()) {
			LocalStorageHandler.offerOfflineStorage(this);
		}
		else {
			LocalStorageHandler.storeInLocalStorage(this);				
		}
	}

	@Override
	public void onFailureAfterLog(Throwable returned) {
		if (LocalStorageHandler.isUploading()) {
			LocalStorageHandler.FailureOnLoadFromLocalStorage(this, returned.getLocalizedMessage());
		}
		else {
			super.onFailureAfterLog(returned);
		}
	}
	
	@Override
	public void onLoadFromLocalStorage() {
		enqueue();
	}
    
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
	 * Convert this SetUserTranslation object into a key value pair.
	 * The format is: <br>
	 * key = documentId;chunkId;partNumber <br>
	 * value = chosenTranslationPair;userTranslation
	 * @param keyValuePair
	 * @return the SetUserTranslation if the parsing is successful, null otherwise
	 */
	@Override
    public KeyValuePair toKeyValuePair() {
    	if (keyValuePair == null) {
    		// we don't have the key value pair yet, we must generate it
    		
    		// key fields
			StringBuilder key = new StringBuilder();
			key.append(documentId);
			key.append(LocalStorageHandler.FIELDS_SEPARATOR);
			key.append(chunkIndex.getId());
			key.append(LocalStorageHandler.FIELDS_SEPARATOR);
			key.append(chunkIndex.getPartNumber());
			
			// value fields
			StringBuilder value = new StringBuilder();
			value.append(chosenTranslationPair);
			value.append(LocalStorageHandler.FIELDS_SEPARATOR);
			value.append(userTranslation);
			
			keyValuePair = new KeyValuePair(key.toString(), value.toString());
    		return keyValuePair;
    	}
    	else {
        	// else use the already generated key value pair
    		return keyValuePair;
    	}
	}

	/**
	 * Try to parse the given key value pair as a SetUserTranslation object.
	 * The format is: <br>
	 * key = documentId;chunkId;partNumber <br>
	 * value = chosenTranslationPair;userTranslation
	 * @param keyValuePair
	 * @return the SetUserTranslation if the parsing is successful, null otherwise
	 */
    public static SetUserTranslation fromKeyValuePair(KeyValuePair keyValuePair) {
    	try {
    		// key fields
	    	String[] keyFields = keyValuePair.getKey().split(LocalStorageHandler.FIELDS_SEPARATOR, 3);
	    	long documentId = Long.parseLong(keyFields[0]);
			int chunkId = Integer.parseInt(keyFields[1]);
	    	int partNumber = Integer.parseInt(keyFields[2]);
			ChunkIndex chunkIndex = new ChunkIndex(partNumber, chunkId);
			
			// value fields
	    	String[] valueFields = keyValuePair.getValue().split(LocalStorageHandler.FIELDS_SEPARATOR, 2);
	    	long chosenTranslationPair = Long.parseLong(valueFields[0]);
	    	String userTranslation = valueFields[1];
	    	
	    	return new SetUserTranslation(chunkIndex, documentId, userTranslation, chosenTranslationPair, keyValuePair);
    	}
    	catch (Exception e) {
    		Gui.log(
    				"Parsing error in SetUserTranslation.fromKeyValuePair(" +
    				keyValuePair.getKey() + "," +
    				keyValuePair.getValue() + "): " +
    				e.toString()
				);
			return null;
		}
    }

	
    @Override
	public String toUserFriendlyString() {
		return this.userTranslation;
	}

}

