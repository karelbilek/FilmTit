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
package cz.filmtit.client.callables;

import cz.filmtit.client.*;

import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.share.*;

/**
 * Save the user translation of the given chunk (no matter whether it is user's
 * own translation or a suggestion taken over or post-edited). The ID of the
 * TranslationPair chosen for post-editing is also sent, providing feedback
 * which then can be used to improve future suggestions. If in Offline Mode, the
 * user translation is not sent to User Space but is saved in the Local Storage.
 *
 * @author rur
 *
 */
public class SetUserTranslation extends Callable<Void> implements Storable {

    // parameters
    private ChunkIndex chunkIndex;
    private long documentId;
    private String userTranslation;
    private long chosenTranslationPair;

    private boolean toLockNext = false;

    private SubgestBox toUnlockBox;
    private SubgestBox toLockBox;

    private TranslationWorkspace workspace;

    @Override
    public String getName() {
        return getNameWithParameters(chunkIndex, documentId, userTranslation, chosenTranslationPair);
    }

    @Override
    public void onSuccessAfterLog(Void o) {
        if (LocalStorageHandler.isUploading()) {
            LocalStorageHandler.SuccessOnLoadFromLocalStorage(this);
        }

        Gui.log(LevelLogEnum.Notice, "SetUserTranslation", "Set User Translation " + userTranslation);

        workspace.setLockedSubgestBox(null);
        workspace.setPrevLockedSubgestBox(toUnlockBox);
        toUnlockBox.setFocus(false);
        toUnlockBox.removeStyleDependentName("locked");

        /*

        if (toUnlock && toLockNext) {
            new UnlockTranslationResult(toUnlockBox, workspace, toLockBox);
        } else if (toUnlock) {
            new UnlockTranslationResult(toUnlockBox, workspace);
        }
        
         */
        // Setting user translation also unlocks the translation result so we only need to lock a new one
        if (toLockNext) {
            new LockTranslationResult(toLockBox, workspace);
        }

    }

    /**
     * Save the user translation of the given chunk (no matter whether it is
     * user's own translation or a suggestion taken over or post-edited). The ID
     * of the TranslationPair chosen for post-editing is also sent, providing
     * feedback which then can be used to improve future suggestions. If in
     * Offline Mode, the user translation is not sent to User Space but is saved
     * in the Local Storage.
     */
    public SetUserTranslation(ChunkIndex chunkIndex, long documentId,
            String userTranslation, long chosenTranslationPair, SubgestBox toUnlockBox, TranslationWorkspace workspace) {
        super();

        this.chunkIndex = chunkIndex;
        this.documentId = documentId;
        this.userTranslation = userTranslation;
        this.chosenTranslationPair = chosenTranslationPair;

        this.toUnlockBox = toUnlockBox;
        this.workspace = workspace;

        enqueue();
    }

    public SetUserTranslation(ChunkIndex chunkIndex, long documentId,
            String userTranslation, long chosenTranslationPair, SubgestBox toUnlockBox, TranslationWorkspace workspace, SubgestBox toLockBox) {
        super();

        this.chunkIndex = chunkIndex;
        this.documentId = documentId;
        this.userTranslation = userTranslation;
        this.chosenTranslationPair = chosenTranslationPair;

        this.toUnlockBox = toUnlockBox;

        this.toLockNext = true;
        this.toLockBox = toLockBox;

        this.workspace = workspace;

        enqueue();
    }

    @Override
    protected void call() {
        filmTitService.setUserTranslation(Gui.getSessionID(), chunkIndex,
                documentId, userTranslation, chosenTranslationPair,
                this);

        /*      if (LocalStorageHandler.isOnline()) {
            filmTitService.setUserTranslation(Gui.getSessionID(), chunkIndex,
                    documentId, userTranslation, chosenTranslationPair,
                    this);
        } else {
            hasReturned = LocalStorageHandler.storeInLocalStorage(this);
            if (hasReturned) {
                Gui.log("Saved to local storage: " + keyValuePair);
            } else {
                Gui.log("ERROR: Cannot save to local storage: " + keyValuePair);
                displayWindow("ERROR: Cannot save to local storage: " + keyValuePair);
            }
        }*/
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
        } else if (LocalStorageHandler.isOfferingOfflineStorage()) {
            LocalStorageHandler.queue.add(this);
        } else if (LocalStorageHandler.isOnline()) {
            LocalStorageHandler.offerOfflineStorage(this);
        } else {
            LocalStorageHandler.storeInLocalStorage(this);
        }
    }

    @Override
    public void onFailureAfterLog(Throwable returned) {
        if (LocalStorageHandler.isUploading()) {
            LocalStorageHandler.FailureOnLoadFromLocalStorage(this, returned.getLocalizedMessage());
        } else {
            super.onFailureAfterLog(returned);
        }
    }

    @Override
    public void onLoadFromLocalStorage() {
        enqueue();
    }

    /**
     * a constructor to be used in local storage retrieval, also setting the
     * keyValuePair and not invoking the RPC yet
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
     * Convert this SetUserTranslation object into a key value pair. The format
     * is: <br>
     * key = documentId;chunkId;partNumber <br>
     * value = chosenTranslationPair;userTranslation
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
        } else {
            // else use the already generated key value pair
            return keyValuePair;
        }
    }

    /**
     * Try to parse the given key value pair as a SetUserTranslation object. The
     * format is: <br>
     * key = documentId;chunkId;partNumber <br>
     * value = chosenTranslationPair;userTranslation
     *
     * @param keyValuePair
     * @return the SetUserTranslation if the parsing is successful, null
     * otherwise
     */
    // @Override
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
        } catch (Exception e) {
            Gui.log(
                    "Parsing error in SetUserTranslation.fromKeyValuePair("
                    + keyValuePair.getKey() + ","
                    + keyValuePair.getValue() + "): "
                    + e.toString()
            );
            return null;
        }
    }

    @Override
    public String toUserFriendlyString() {
        return this.userTranslation;
    }

}
