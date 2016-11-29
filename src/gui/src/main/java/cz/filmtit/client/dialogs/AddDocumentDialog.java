/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import cz.filmtit.client.Gui;
import cz.filmtit.client.callables.AddDocument;

/**
 *
 * @author matus
 */
public class AddDocumentDialog extends Dialog {
    
    private static AddDocumentDialogUiBinder uiBinder = GWT.create(AddDocumentDialogUiBinder.class);
    
    interface AddDocumentDialogUiBinder extends UiBinder<Widget, AddDocumentDialog> {
    }
    
    public AddDocumentDialog() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        submitBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String docShareId = shareIdBox.getText();
                if (!docShareId.isEmpty()) {
                    new AddDocument(docShareId, Gui.getUser(), AddDocumentDialog.this);
                }
            }
        });
    }

    @UiField
    TextBox shareIdBox;
    
    @UiField
    Button submitBtn;
}
