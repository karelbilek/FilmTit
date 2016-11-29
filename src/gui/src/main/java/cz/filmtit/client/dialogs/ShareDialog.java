/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import cz.filmtit.client.callables.GetShareId;
import cz.filmtit.share.Document;

/**
 *
 * @author matus
 */
public class ShareDialog extends Dialog {

    private Document doc;

    private static ShareDialogUiBinder uiBinder = GWT.create(ShareDialogUiBinder.class);

    interface ShareDialogUiBinder extends UiBinder<Widget, ShareDialog> {
    }

    public ShareDialog(Document doc) {
        super();
        initWidget(uiBinder.createAndBindUi(this));

        this.doc = doc;

        if (doc == null) {
            shareIdBox.setText("null");
        } else {
            shareIdBox.setText("Not null");
        }
        
        shareIdBox.setReadOnly(true);
        shareIdBox.selectAll();
        
        new GetShareId(this.doc, this);
    }

    public ShareDialog() {
        // nothing
    }

    @UiField
    public TextBox shareIdBox;

}
