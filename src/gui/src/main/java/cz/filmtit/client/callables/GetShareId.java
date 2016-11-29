/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import static cz.filmtit.client.Callable.filmTitService;
import cz.filmtit.client.Gui;
import cz.filmtit.client.dialogs.ShareDialog;
import cz.filmtit.share.Document;

/**
 *
 * @author matus
 */
public class GetShareId extends Callable<Long> {

    private ShareDialog shareDialog;
    private Document doc;

    public GetShareId(Document doc, ShareDialog shareDialog) {
        super();
        this.shareDialog = shareDialog;
        this.doc = doc;
        enqueue();
    }

    @Override
    public void onSuccessAfterLog(Long result) {
        shareDialog.shareIdBox.setText(result.toString());
    }

    @Override
    protected void call() {
        filmTitService.getShareId(doc, this);
    }

}
