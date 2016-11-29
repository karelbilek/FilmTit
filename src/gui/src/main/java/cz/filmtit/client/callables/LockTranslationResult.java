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

        retries = 0;
        this.subgestBox = subgestBox;
        this.tResult = subgestBox.getTranslationResult();
        this.workspace = workspace;
        this.subgestBox.setEnabled(false);

        enqueue();
    }

    @Override
    protected void onFinalError(String message) {
        
        subgestBox.setEnabled(true);
        subgestBox.addStyleDependentName("unlocked");
        subgestBox.setEnabled(false);
        workspace.setPrevLockedSubgestBox(subgestBox);
        
    }

    @Override
    public void onSuccessAfterLog(Void result) {
        Gui.log(LevelLogEnum.Notice, "LockTranslationResult", "Locked Translation Result id: " + String.valueOf(tResult.getChunkId()) + " SessionId: " + Gui.getSessionID());
        subgestBox.setEnabled(true);
        workspace.setLockedSubgestBox(subgestBox);

        if (workspace.getPrevLockedSubgestBox() != null) {
            workspace.getPrevLockedSubgestBox().removeStyleDependentName("unlocked");
            workspace.getPrevLockedSubgestBox().setEnabled(true);
        }

        workspace.getTimer().schedule(60000);
        subgestBox.addStyleDependentName("locked");

        new ReloadTranslationResults(workspace.getCurrentDocument().getId(), workspace);

    }

    @Override
    protected void call() {
        filmTitService.lockTranslationResult(tResult, Gui.getSessionID(), this);
    }

}
