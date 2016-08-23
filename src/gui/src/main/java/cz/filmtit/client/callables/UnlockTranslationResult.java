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

/**
 *
 * @author matus
 */
public class UnlockTranslationResult extends Callable<Void>{
    TranslationResult tResult;
    SubgestBox subgestBox;
    
    public UnlockTranslationResult() {
        // do nothing
    }
    
    public UnlockTranslationResult(TranslationResult tResult, SubgestBox subgestBox) {
        super();
        this.tResult = tResult;
        this.subgestBox = subgestBox;
        enqueue();
    }
    
    @Override
    public void onSuccessAfterLog(Void result) {
        Gui.log(LevelLogEnum.Error, "unlockTranslationResult", String.valueOf(tResult.getChunkId()));
    }
    
    
    
    @Override
    protected void call() {
        filmTitService.unlockTranslationResult(tResult, Gui.getSessionID(), this);
    }
}
