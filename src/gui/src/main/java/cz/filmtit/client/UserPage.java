package cz.filmtit.client;



import com.google.gwt.user.client.Window;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.cell.client.*;

import java.util.List;
import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.share.Document;


public class UserPage extends Composite {

	private static UserPageUiBinder uiBinder = GWT
			.create(UserPageUiBinder.class);

	interface UserPageUiBinder extends UiBinder<Widget, UserPage> {
	}

	private Gui gui = Gui.getGui();

	public UserPage() {
		initWidget(uiBinder.createAndBindUi(this));

        TextColumn<Document> nameClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return doc.getTitle();
            }
        };
        TextColumn<Document> mSourceClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                if (doc.getMovie()==null) {
                    return "";
                }
                return doc.getMovie().toString();
            }
        };
        TextColumn<Document> doneClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return "TODO %";
            }
        };
        TextColumn<Document> lastEditedClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return "TODO";
            }
        };

        com.github.gwtbootstrap.client.ui.ButtonCell buttonCell = new com.github.gwtbootstrap.client.ui.ButtonCell();
        
        // edit button
        com.google.gwt.user.cellview.client.Column<Document, String> buttonClm = new com.google.gwt.user.cellview.client.Column<Document, String>(buttonCell) {
             @Override
             public String getValue(Document doc) {
                return "Edit";
             }
        };

        buttonClm.setFieldUpdater(new FieldUpdater<Document, String>() {
            public void update(int index, Document doc, String value) {
                editDocument(doc);
            }
        });

        // export button
        com.google.gwt.user.cellview.client.Column<Document, String> exportSubtitlesButton = new com.google.gwt.user.cellview.client.Column<Document, String>(buttonCell) {
            @Override
            public String getValue(Document doc) {
               return "Export";
            }
       };

       exportSubtitlesButton.setFieldUpdater(new FieldUpdater<Document, String>() {
           public void update(int index, Document doc, String value) {
                    Window.Location.assign("/download/download?docId="+doc.getId()+"&sessionId="+gui.getSessionID()+"&type=srt&way=target");
           }
       });



        

        docTable.addColumn(nameClm, "Document");
        docTable.addColumn(mSourceClm, "Media");
        docTable.addColumn(doneClm, "Percent done");
        docTable.addColumn(lastEditedClm, "Last edited");
        docTable.addColumn(buttonClm, "Edit");
        docTable.addColumn(exportSubtitlesButton, "Export");
      
        emptyLabel.setVisible(false);
        
        
        
        gui.guiStructure.activateMenuItem(gui.guiStructure.userPage);

        gui.guiStructure.contentPanel.setStyleName("users_page");

        gui.log("getting list of documents...");
        gui.rpcHandler.getListOfDocuments(this);



        btnDisplayCreator.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	gui.pageHandler.loadPage(Page.DocumentCreator);
            }

        });


        gui.guiStructure.contentPanel.setWidget(this);

      
	}

    void editDocument(Document document) {
    	gui.pageHandler.setDocumentId(document.getId());
    	gui.pageHandler.loadPage(Page.TranslationWorkspace);
    }

    public void setDocuments(List<Document> documents) {
        if (documents.size() == 0) {
            emptyLabel.setText("You don't have any documents. Let's create some!"); 
            emptyLabel.setVisible(true);
        }
        docTable.setRowCount(documents.size(), true);
        docTable.setRowData(0, documents);
        docTable.redraw();
     }

    @UiField
    Label emptyLabel;

    @UiField
    Button btnDisplayCreator;

    @UiField
    com.github.gwtbootstrap.client.ui.CellTable docTable;

 /*   @UiField
    TabPanel tabPanel;

    @UiField
    Tab tabDocumentList;

    @UiField
    Tab tabNewDocument;*/

}
