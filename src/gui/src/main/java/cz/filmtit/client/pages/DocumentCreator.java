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

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;
import cz.filmtit.client.callables.CreateDocument;
import cz.filmtit.share.LevelLogEnum;
import java.util.Arrays;

import java.util.Iterator;
import java.util.List;

/**
 * This page is used to create a new document.
 *
 * @author rur
 *
 */
public class DocumentCreator extends Composite {

    private static DocumentCreatorUiBinder uiBinder = GWT
            .create(DocumentCreatorUiBinder.class);

    interface DocumentCreatorUiBinder extends UiBinder<Widget, DocumentCreator> {
    }

    boolean copyingTitle = true;

    private FileReader freader;


    /**
     * Shows the page.
     */
    public DocumentCreator() {

        initWidget(uiBinder.createAndBindUi(this));
              
        // movie title copying
        txtTitle.addStyleName("copying_title");

        txtMovieTitle.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (copyingTitle) {
                    txtTitle.setText(txtMovieTitle.getText());
                }
            }
        });

        txtTitle.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (copyingTitle) {
                    copyingTitle = false;
                    txtTitle.removeStyleName("copying_title");
                }
            }
        });

        // --- file reading interface via lib-gwt-file --- //
        //final FileReader
        try {
            freader = new FileReader();

            freader.addLoadEndHandler(new LoadEndHandler() {
                @Override
                public void onLoadEnd(LoadEndEvent event) {
                    lblUploadProgress.setText("File uploaded successfully.");
                    btnCreateDocument.setEnabled(true);
                    //log(subtext);
                }
            });

            fileUpload.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    //log(fileUpload.getFilename());
                    lblUploadProgress.setVisible(true);
                    lblUploadProgress.setText("Uploading the file...");
                    FileList fl = fileUpload.getFiles();
                    Iterator<File> fit = fl.iterator();
                    if (fit.hasNext()) {
                        File file = fit.next();
                        if (file.getSize() > 500000) {
                            lblUploadProgress.setText("File too big (maximum is 500 kB)");
                        } else {
                            freader.readAsText(file, getChosenEncoding());
                            // fires onLoadEnd() which enables btnCreateDocument
                        }
                    } else {
                        Gui.log("No file chosen.\n");
                    }
                }
            });

            // FileReader is available - creating document from it:
            btnCreateDocument.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    createDocumentFromText(freader.getStringResult());
                }
            });

        } catch (JavaScriptException e) {
            //Window.alert("The HTML5 file reading interface is not supported "
            //        + "by your browser:\n" + e.getMessage());
            // hiding the FileUpload interface and showing the copy-paste fallback:
            fileUploadControlGroup.setVisible(false);
            // FileReader is not available - creating document from the paste-textarea:
            btnCreateDocument.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    createDocumentFromText(txtFilePaste.getText());
                }
            });
            txtFilePaste.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    btnCreateDocument.setEnabled(true);
                }
            });
            filePasteControlGroup.setVisible(true);
        }

        /*       btnApplet.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                
                //FileLoadWidget.setDocumentCreator(DocumentCreator.this);
                FileLoadWidget loadWidget = new FileLoadWidget(DocumentCreator.this);
                bottomControlGroup.add(loadWidget);
                btnApplet.setLoadingText("Loading....");
                btnApplet.state().loading();
           }
        });*/
        Gui.getGuiStructure().contentPanel.setWidget(this);

        Gui.getPageHandler().setCurrentDocumentCreator(this);
    }

    /*@Override
	public void onSettingsReceived(User user) {
		useMT.setValue(user.getUseMoses());
        reactivate();
	}
	
	protected boolean getUseMT() {
	    return useMT.getValue();
	}*/
 /*public void addressSet(FileLoadWidget widget, String address) {
       
		moviePath.setText(address);
		btnApplet.state().reset();
        

    }*/
    @UiField
    FormActions bottomControlGroup;
    
    private void createDocumentFromText(String subtext) {
        btnCreateDocument.setEnabled(false);
        lblCreateProgress.setVisible(true);
        lblCreateProgress.setText("Creating the document...");

        new CreateDocument(
                getDocumentTitle(),
                getMovieTitle(),
                getChosenLanguage(),
                subtext,
                "srt",
                getMoviePathOrNull(),
                this
        );
        // sets TranslationWorkspace.currentDocument and calls TranslationWorkspace.processText() on success       

    }

    @UiField
    TextBox txtTitle;

    @UiField
    TextBox txtMovieTitle;

    /*   @UiField
	TextBox moviePath;*/
    @UiField
    ListBox lsbLanguage;


    /*@UiField
    RadioButton rdbFormatSrt;
    @UiField
    RadioButton rdbFormatSub;*/
    @UiField
    RadioButton rdbEncodingUtf8;
    @UiField
    RadioButton rdbEncodingWin;
    @UiField
    RadioButton rdbEncodingIso;

    @UiField
    ControlGroup fileUploadControlGroup;
    @UiField
    FileUploadExt fileUpload;
    @UiField
    Label lblUploadProgress;

    @UiField
    ControlGroup filePasteControlGroup;
    @UiField
    TextArea txtFilePaste;
    

    /*@UiField
    CheckBox useMT;
     */
    @UiField
    Button btnCreateDocument;
    @UiField
    Label lblCreateProgress;

    /*private void deactivate() {
        btnCreateDocument.setEnabled(false);
    }
    
    private void reactivate() {
        btnCreateDocument.setEnabled(true); 
    } */
 /*  @UiField
    Button btnApplet;*/
    private String getMoviePathOrNull() {
        /*  if (moviePath.getText()==null || moviePath.getText().equals("")) {
            return null;
        }
        return moviePath.getText();*/
        return null;
    }

    private String getDocumentTitle() {
        return txtTitle.getText();
    }

    private String getMovieTitle() {
        return txtMovieTitle.getText();
    }

    private String getChosenLanguage() {
        return lsbLanguage.getValue();
    }

    private String getChosenEncoding() {
        if (rdbEncodingUtf8.getValue()) {
            return "utf-8";
        } else if (rdbEncodingWin.getValue()) {
            return "windows-1250";
        } else if (rdbEncodingIso.getValue()) {
            return "iso-8859-2";
        } else {
            return "utf-8";  // default value
        }
    }
    

    /**
     * prepare for being reshown to the user
     */
    public void reactivate() {
        btnCreateDocument.setEnabled(true);
        lblCreateProgress.setVisible(false);
    }

    /*public String getChosenSubFormat() {
        if (rdbFormatSrt.getValue()) {
            return "srt";
        }
        else if (rdbFormatSub.getValue()) {
            return "sub";
        }
        else return "srt";	// default value
    }*/
}
