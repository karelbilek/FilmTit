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
public class LockTranslationResult extends Callable<Void> {

    TranslationResult tResult;
    SubgestBox subgestBox;
    TranslationWorkspace workspace;

    public LockTranslationResult() {
        // do nothing
    }

    public LockTranslationResult(SubgestBox subgestBox, TranslationWorkspace workspace) {
        super();
        this.subgestBox = subgestBox;
        this.tResult = subgestBox.getTranslationResult();
        this.workspace = workspace;
        this.subgestBox.setEnabled(false);

        /*  SubgestBox prevLockedSubgestBox = this.workspace.getLockedSubgestBox();
        if (prevLockedSubgestBox != null) {
            new UnlockTranslationResult(prevLockedSubgestBox, this.workspace);
        }*/
        //   Gui.log(LevelLogEnum.Error, "LockTranslationResult", String.valueOf(tResult.getId() + " " + tResult.getChunkId() + " " + tResult.getDocumentId()));
        enqueue();
    }

    @Override
    protected void onFinalError(String message) {
        super.onFinalError(message);
        subgestBox.setEnabled(true);
        // subgestBox.setFocus(false);
        subgestBox.addStyleDependentName("unlocked");
    }

    @Override
    public void onSuccessAfterLog(Void result) {
        Gui.log(LevelLogEnum.Error, "lockTranslationResult", String.valueOf(tResult.getChunkId()));
        subgestBox.setEnabled(true);
        //   workspace.alert("Locked subtitle n " + subgestBox.getChunk().getId());
        workspace.setLockedSubgestBox(subgestBox);

        if (workspace.getPrevLockedSubgestBox() != null) {
            workspace.getPrevLockedSubgestBox().removeStyleDependentName("unlocked");
        }

        workspace.getTimer().schedule(60000);
        subgestBox.addStyleDependentName("locked");

    }

    @Override
    protected void call() {
        Gui.log(LevelLogEnum.Error,"LockTranslationResult","Call: LockTranslationResult");
        filmTitService.lockTranslationResult(tResult, Gui.getSessionID(), this);
    }

}
