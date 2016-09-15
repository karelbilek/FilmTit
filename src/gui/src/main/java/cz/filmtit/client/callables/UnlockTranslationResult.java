/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.share.LevelLogEnum;
import cz.filmtit.share.TranslationResult;

/**
 *
 * @author matus
 */
public class UnlockTranslationResult extends Callable<Void> {

    TranslationResult tResult;
    TranslationWorkspace workspace;
    SubgestBox subgestBox;

    boolean toLock = false;
    SubgestBox toLockBox;

    public UnlockTranslationResult() {
        // do nothing
    }

    public UnlockTranslationResult(SubgestBox subgestBox, TranslationWorkspace workspace) {
        super();

        retries = 0;
        this.tResult = subgestBox.getTranslationResult();
        this.subgestBox = subgestBox;
        this.workspace = workspace;
        
        enqueue();
    }

    public UnlockTranslationResult(SubgestBox subgestBox, TranslationWorkspace workspace, SubgestBox toLockBox) {
        super();

        retries = 0;
        this.tResult = subgestBox.getTranslationResult();
        this.subgestBox = subgestBox;
        this.workspace = workspace;

        this.toLock = true;
        this.toLockBox = toLockBox;

        enqueue();
    }

    @Override
    public void onSuccessAfterLog(Void result) {
        Gui.log(LevelLogEnum.Notice, "UnlockTranslationResult", "Unlocked Translation Result id: " + String.valueOf(tResult.getChunkId()));
        workspace.setLockedSubgestBox(null);
        workspace.setPrevLockedSubgestBox(subgestBox);
        subgestBox.setFocus(false);
        subgestBox.removeStyleDependentName("locked");

        if (toLock) {
            new LockTranslationResult(toLockBox, workspace);
        }
    }

    @Override
    protected void call() {
        filmTitService.unlockTranslationResult(tResult.getSourceChunk().getChunkIndex(),
                tResult.getDocumentId(), Gui.getSessionID(), this);
    }
}
