package cz.filmtit.client.pages;


import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.dialogs.DownloadDialog;
import cz.filmtit.share.Document;

import java.util.Date;
import java.util.List;


public class UserPage extends Composite {

	private static UserPageUiBinder uiBinder = GWT
			.create(UserPageUiBinder.class);

	interface UserPageUiBinder extends UiBinder<Widget, UserPage> {
	}

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
        TextColumn<Document> languageClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return doc.getLanguage().getName();
            }
        };
        TextColumn<Document> doneClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return Double.toString(Math.round(10000 * doc.getTranslatedChunksCount() /
                        doc.getTotalChunksCount()) / 100) + "%";
            }
        };
        TextColumn<Document> lastEditedClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                Date lastChange = new Date(doc.getLastChange());
                return DateTimeFormat.getFormat("dd/MM/yyy HH:mm").format(lastChange);
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
        	   new DownloadDialog(doc);
           }
       });

       // delete button
       com.google.gwt.user.cellview.client.Column<Document, String> deleteButton = new com.google.gwt.user.cellview.client.Column<Document, String>(buttonCell) {
           @Override
           public String getValue(Document doc) {
               return "Delete";
           }
       };
       
       deleteButton.setFieldUpdater(new FieldUpdater<Document, String>() {
           public void update(int index, Document doc, String value) {
        	   if (Window.confirm("Do you really want to delete the document " + doc.getTitle() + "?")) {
        		   FilmTitServiceHandler.deleteDocument(doc.getId());
        	   }
           }
       });


        

        docTable.addColumn(nameClm, "Document");
        docTable.addColumn(mSourceClm, "Movie/TV Show");
        docTable.addColumn(languageClm, "Language");
        docTable.addColumn(doneClm, "Translated");
        docTable.addColumn(lastEditedClm, "Last edited");
        docTable.addColumn(buttonClm, "Edit");
        docTable.addColumn(exportSubtitlesButton, "Export");
        docTable.addColumn(deleteButton, "Delete");
      
        emptyLabel.setVisible(false);
        

        Gui.getGuiStructure().contentPanel.setStyleName("users_page");

        Gui.log("getting list of documents...");
        FilmTitServiceHandler.getListOfDocuments(this);



        btnDisplayCreator.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	Gui.getPageHandler().loadPage(Page.DocumentCreator);
            }

        });


        Gui.getGuiStructure().contentPanel.setWidget(this);

      
	}

    void editDocument(Document document) {
    	Gui.getPageHandler().setDocumentId(document.getId());
    	Gui.getPageHandler().loadPage(Page.TranslationWorkspace);
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
