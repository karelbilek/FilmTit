/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.share.LevelLogEnum;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.User;

/**
 *
 * @author matus
 */
public class LockTranslationResult extends Callable<Void> {

    TranslationResult tResult;
    SubgestBox subgestBox;

    public LockTranslationResult() {
        // do nothing
    }

    public LockTranslationResult(SubgestBox subgestBox) {
        super();
        this.subgestBox = subgestBox;
        this.tResult = subgestBox.getTranslationResult();

        Gui.log(LevelLogEnum.Error, "LockTranslationResult", String.valueOf(tResult.getId()));
        enqueue();
    }

    @Override
    public void onSuccessAfterLog(Void result) {
        Gui.log(LevelLogEnum.Error, "lockTranslationResult", String.valueOf(tResult.getChunkId()));
    }

    @Override
    protected void call() {
        filmTitService.lockTranslationResult(tResult, Gui.getSessionID(), this);
    }

}
