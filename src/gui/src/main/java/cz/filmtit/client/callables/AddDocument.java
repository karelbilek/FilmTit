/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.dialogs.AddDocumentDialog;
import cz.filmtit.share.User;

/**
 *
 * @author matus
 */
public class AddDocument extends Callable<Void> {

    String shareId;
    User user;
    AddDocumentDialog dialog;

    public AddDocument(String docShareId, User user, AddDocumentDialog dialog) {
        super();
        this.shareId = docShareId;
        this.user = user;
        this.dialog = dialog;
        enqueue();
    }

    @Override
    public void onSuccessAfterLog(Void result) {
        dialog.close();
    }

    @Override
    protected void call() {
        filmTitService.addDocument(shareId, user, this);
    }

}
