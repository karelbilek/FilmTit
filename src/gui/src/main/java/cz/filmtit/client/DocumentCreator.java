package cz.filmtit.client;

import cz.filmtit.client.widgets.*;
import java.util.*;
import com.google.gwt.user.client.Window;

import com.github.gwtbootstrap.client.ui.*;
import org.vectomatic.file.FileUploadExt;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.core.client.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.*;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import com.google.gwt.cell.client.FieldUpdater;

import cz.filmtit.share.*;
import cz.filmtit.share.parsing.*;
import cz.filmtit.client.SubgestBox.FakeSubgestBox;

import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import java.util.*;


public class DocumentCreator extends Composite {

	private static DocumentCreatorUiBinder uiBinder = GWT
			.create(DocumentCreatorUiBinder.class);

	interface DocumentCreatorUiBinder extends UiBinder<Widget, DocumentCreator> {
	}

	private Gui gui;
	
	public DocumentCreator(final Gui gui) {
		initWidget(uiBinder.createAndBindUi(this));
        
        btnCreateDocument.setEnabled(false);

		this.gui = gui;
		
		gui.guiStructure.activateMenuItem(gui.guiStructure.documentCreator);

        // --- file reading interface via lib-gwt-file --- //
            final FileReader freader = new FileReader();
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
                        freader.readAsText(fit.next(), getChosenEncoding());
                    } else {
                        gui.error("No file chosen.\n");
                    }
                }
            });

            btnCreateDocument.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    lblCreateProgress.setVisible(true);
                    lblCreateProgress.setText("Creating the document...");
                    createDocumentFromText(freader.getStringResult());
                }
            });

            btnApplet.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /*if (widgetToRemove!=null) {
                    Window.alert("snadmazu");
                    try {
                        widgetToRemove.hide();
                    } catch (Exception e) {
                        Window.alert(e.toString());
                    }
                }*/
                FileLoadWidget.setDocumentCreator(DocumentCreator.this);
                FileLoadWidget loadWidget = new FileLoadWidget();
                bottomControlGroup.add(loadWidget);
           }
        });
            gui.guiStructure.contentPanel.setWidget(this);
            
            btnCreateDocument.setEnabled(false);
	}


    public void addressSet(FileLoadWidget widget, String address) {
       
       
       try {
         moviePath.setText(address);
       } catch (Exception e){
         Window.alert(e.toString());
       }

      // widgetToRemove = widget ;
    }
    
    @UiField
    ControlGroup bottomControlGroup;

    private void createDocumentFromText(String subtext) {
        gui.rpcHandler.createDocument(
                getTitle(),
                getMovieTitle(),
                getChosenLanguage(),
                subtext,
                getMoviePathOrNull());
        // sets currentDocument and calls processText() on success
    }
	
    @UiField
    TextBox txtTitle;

	@UiField
	TextBox txtMovieTitle;

    @UiField
	TextBox moviePath;

    @UiField
    ListBox lsbLanguage;


    @UiField
    RadioButton rdbFormatSrt;
    @UiField
    RadioButton rdbFormatSub;


    @UiField
    RadioButton rdbEncodingUtf8;
    @UiField
    RadioButton rdbEncodingWin;
    @UiField
    RadioButton rdbEncodingIso;

    @UiField
    FileUploadExt fileUpload;
    @UiField
    Label lblUploadProgress;

    @UiField
    Button btnCreateDocument;
    @UiField
    Label lblCreateProgress;


    @UiField
    Button btnApplet;

    public String getMoviePathOrNull() {
        if (moviePath.getText()==null || moviePath.getText().equals("")) {
            return null;
        }
        return moviePath.getText();
    }




    public String getTitle() {
        return txtTitle.getText();
    }

    public String getMovieTitle() {
        return txtMovieTitle.getText();
    }

    public String getChosenLanguage() {
        return lsbLanguage.getValue();
    }

    public String getChosenEncoding() {
        if (rdbEncodingUtf8.getValue()) {
            return "utf-8";
        }
        else if (rdbEncodingWin.getValue()) {
            return "windows-1250";
        }
        else if (rdbEncodingIso.getValue()) {
            return "iso-8859-2";
        }
        else return "utf-8";  // default value
    }

    public String getChosenSubFormat() {
        if (rdbFormatSrt.getValue()) {
            return "srt";
        }
        else if (rdbFormatSub.getValue()) {
            return "sub";
        }
        else return "srt";	// default value
    }



}
