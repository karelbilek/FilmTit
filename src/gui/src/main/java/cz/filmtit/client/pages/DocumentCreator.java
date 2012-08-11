package cz.filmtit.client.pages;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.Gui;
import cz.filmtit.client.widgets.FileLoadWidget;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import java.util.Iterator;


public class DocumentCreator extends Composite {

	private static DocumentCreatorUiBinder uiBinder = GWT
			.create(DocumentCreatorUiBinder.class);

	interface DocumentCreatorUiBinder extends UiBinder<Widget, DocumentCreator> {
	}

    boolean copyingTitle = true;

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
                    File file = fit.next();
                    if (file.getSize() > 500000) {
                        lblUploadProgress.setText("File too bix (maximum is 500 kB)");
                    } else {
                        freader.readAsText(file, getChosenEncoding());
                    }
                } else {
                    Gui.log("No file chosen.\n");
                }
            }
        });

        btnCreateDocument.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	btnCreateDocument.setEnabled(false);
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

        
        Gui.getGuiStructure().contentPanel.setWidget(this);
	}


    public void addressSet(FileLoadWidget widget, String address) {
       
		moviePath.setText(address);

      // widgetToRemove = widget ;
    }
    
    @UiField
    FormActions bottomControlGroup;

    private void createDocumentFromText(String subtext) {
        FilmTitServiceHandler.createDocument(
                getTitle(),
                getMovieTitle(),
                getChosenLanguage(),
                subtext,
                getChosenSubFormat(),
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
