/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.UserPage;
import cz.filmtit.share.LevelLogEnum;
import cz.filmtit.share.User;
import java.util.List;

/**
 *
 * @author matus
 */
public class LockDocument extends Callable<Integer>{
    
    private Long userId;
    private Long documentId;
    private UserPage userPage;
    
    public LockDocument() {
        //nothing
    }
    
    public LockDocument(Long userId, Long documentId, UserPage userPage) {
        super();
        this.userId = userId;
        this.documentId = documentId;
        this.userPage = userPage;
        enqueue();
    }
    
    @Override
    public void onSuccessAfterLog(Integer callReturn) {
        this.userPage.setCallLockResult(callReturn);
    }

    @Override
    protected void call() {
        Gui.log(LevelLogEnum.Error, "LockDocument", "calling filmTitService.lockDocument");
        filmTitService.lockDocument(userId, documentId, this);
    }
    
}
