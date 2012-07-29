package cz.filmtit.client;



import com.google.gwt.user.client.Window;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.cell.client.*;
import java.util.List;
import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.share.Document;


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
                return doc.getMovie().getTitle();
            }
        };
        TextColumn<Document> yearClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return doc.getMovie().getYear().toString();
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
        com.google.gwt.user.cellview.client.Column buttonClm = new com.google.gwt.user.cellview.client.Column<Document, String>(buttonCell) {
             @Override
             public String getValue(Document doc) {
                return "Edit";
             }
        };

        buttonClm.setFieldUpdater(new FieldUpdater<Document, String>() {
            public void update(int index, Document doc, String value) {
                Window.alert("You want to edit document "+doc.getMovie().getTitle()+", but filmtit can't do that at the moment.");
            }
        });




        

        docTable.addColumn(nameClm, "Name");
        docTable.addColumn(yearClm, "Year");
        docTable.addColumn(doneClm, "Percent done");
        docTable.addColumn(lastEditedClm, "Last edited");
        docTable.addColumn(buttonClm, "Edit");
      
        emptyLabel.setVisible(false);
      
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
