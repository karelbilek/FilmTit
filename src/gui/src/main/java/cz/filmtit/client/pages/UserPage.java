/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.client.pages;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.ChangeDocumentTitle;
import cz.filmtit.client.callables.ChangeMovieTitle;
import cz.filmtit.client.callables.DeleteDocument;
import cz.filmtit.client.callables.GetListOfDocuments;
import cz.filmtit.client.dialogs.AddDocumentDialog;
import cz.filmtit.client.dialogs.DownloadDialog;
import cz.filmtit.client.dialogs.ShareDialog;
import cz.filmtit.share.Document;
import cz.filmtit.share.LevelLogEnum;

/**
 * A page providing listing of user documents and the possibility to edit them
 *
 */
public class UserPage extends Composite {
    
    private Integer callLockResult;

    private static UserPageUiBinder uiBinder = GWT
            .create(UserPageUiBinder.class);

    /**
     * @return the callLockResult
     */
    public Integer getCallLockResult() {
        return callLockResult;
    }

    /**
     * @param callLockResult the callLockResult to set
     */
    public void setCallLockResult(Integer callLockResult) {
        this.callLockResult = callLockResult;
    }

    interface UserPageUiBinder extends UiBinder<Widget, UserPage> {
    }

    /**
     * Shows the page and loads the list of user's documents.
     */
    public UserPage() {
        initWidget(uiBinder.createAndBindUi(this));

        /**
         * A column consisting of EdiTextCells with a title (aka a tool-tip)
         *
         * @author rur
         *
         */
        abstract class EditableTitledColumn extends Column<Document, String> {

            private String title;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public EditableTitledColumn(String title) {
                super(new EditTextCell());
                this.title = title;
            }

            @Override
            public void render(Context context, Document object,
                    SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div title=\"" + title + "\">");
                super.render(context, object, sb);
                sb.appendHtmlConstant("</div>");
            }
        }

        // column with Document title
        Column<Document, String> nameClm = new EditableTitledColumn("click to change document title") {
            @Override
            public String getValue(Document doc) {
                return doc.getTitle();
            }
        };
        // use ChangeDocumentTitle() to change the title
        nameClm.setFieldUpdater(new FieldUpdater<Document, String>() {
            @Override
            public void update(int index, Document doc, String newTitle) {
                if (newTitle == null || newTitle.isEmpty()) {
                    // we don't accept the new title
                    // refresh to show the original one
                    Gui.getPageHandler().refresh();
                } else if (newTitle.equals(doc.getTitle())) {
                    // not changed, ignore
                } else {
                    new ChangeDocumentTitle(doc.getId(), newTitle);
                    doc.setTitle(newTitle);
                }
            }
        });

        // column with Movie title
        Column<Document, String> mSourceClm = new EditableTitledColumn("click to change movie title") {
            @Override
            public String getValue(Document doc) {
                if (doc.getMovie() == null) {
                    return "(none)";
                } else {
                    return doc.getMovie().toString();
                }
            }

        };
        // use ChangeMovieTitle() to change the title
        mSourceClm.setFieldUpdater(new FieldUpdater<Document, String>() {
            @Override
            public void update(int index, Document doc, String newTitle) {
                if (newTitle == null || newTitle.isEmpty()) {
                    // we don't accept the new title
                    // refresh to show the original one
                    Gui.getPageHandler().refresh();
                } else if (doc.getMovie() != null && doc.getMovie().toString().equals(newTitle)) {
                    // not changed, ignore
                } else {
                    // TODO: should lock the page while waiting for media sources
                    new ChangeMovieTitle(doc.getId(), newTitle);
                }
            }
        });

        // column with translation direction
        TextColumn<Document> languageClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                // TODO: do you prefer names or codes?
                return doc.getLanguage().getTranslationDirectionNames();
            }
        };

        // percentage of translated chunks
        TextColumn<Document> doneClm = new TextColumn<Document>() {
            @Override
            public String getValue(Document doc) {
                return Double.toString(Math.round(10000 * doc.getTranslatedChunksCount()
                        / doc.getTotalChunksCount()) / 100) + "%";
            }
        };

        // date and time of last edit of the document (table is sorted by this column)
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
        
        // share button
        com.google.gwt.user.cellview.client.Column<Document, String> shareButton = new com.google.gwt.user.cellview.client.Column<Document, String>(buttonCell) {
            @Override
            public String getValue(Document doc) {
                return "Share";
            }
        };

        shareButton.setFieldUpdater(new FieldUpdater<Document, String>() {
            public void update(int index, Document doc, String value) {
                new ShareDialog(doc);
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
                    new DeleteDocument(doc.getId());
                }
            }
        });

        docTable.addColumn(nameClm, "Document");
        docTable.addColumn(mSourceClm, "Movie/TV Show");
        docTable.addColumn(languageClm, "Language");
        docTable.addColumn(doneClm, "Translated");
        docTable.addColumn(lastEditedClm, "Last edited");
        docTable.addColumn(buttonClm, "Edit");
        docTable.addColumn(shareButton, "Share");
        docTable.addColumn(exportSubtitlesButton, "Export");
        docTable.addColumn(deleteButton, "Delete");

        // load documents
        new GetListOfDocuments(this);

        Gui.getGuiStructure().contentPanel.setStyleName("users_page");
        Gui.getGuiStructure().contentPanel.setWidget(this);
    }

    void editDocument(Document document) {
        
        Gui.getPageHandler().setDocumentId(document.getId());
        Gui.getPageHandler().loadPage(Page.TranslationWorkspace);
    }

    /**
     * Called by GetListOfDocuments to show the documents of the user.
     */
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
    
    @UiHandler("btnDisplayCreator")
    void onClick(ClickEvent event) {
        // loading a new DocumentCreator
        Gui.getPageHandler().forgetCurrentDocumentCreator();
        Gui.getPageHandler().loadPage(Page.DocumentCreator);
    }
    
    @UiField
    Button btnAddNewDoc;
    
    @UiHandler("btnAddNewDoc")
    void newDocOnClick(ClickEvent event) {
        Gui.log(LevelLogEnum.Error, "btnNewDoc", "It works!");
        new AddDocumentDialog();
    }

    @UiField
    com.github.gwtbootstrap.client.ui.CellTable<Document> docTable;

    /*   @UiField
    TabPanel tabPanel;

    @UiField
    Tab tabDocumentList;

    @UiField
    Tab tabNewDocument;*/
}
