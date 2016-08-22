/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.User;

/**
 *
 * @author matus
 */
public class LockTranslationResult extends Callable<Void>{
    
    TranslationResult tResult;
    User user;
    SubgestBox subgestBox;
    
    public LockTranslationResult() {
        // do nothing
    }
    
    public LockTranslationResult(TranslationResult tResult, User user, SubgestBox subgestBox) {
        super();
        this.tResult = tResult;
        this.user = user;
        this.subgestBox = subgestBox;
        enqueue();
    }
    
    @Override
    public void onSuccessAfterLog(Void result) {
        //TODO make IFrame change border color
    }
    
    
    @Override
    protected void call() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        //TODO implement locking on server
    }
    
}
