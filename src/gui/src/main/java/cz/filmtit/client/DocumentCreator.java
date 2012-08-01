package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.*;
import org.vectomatic.file.FileUploadExt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

public class DocumentCreator extends Composite {

	private static DocumentCreatorUiBinder uiBinder = GWT
			.create(DocumentCreatorUiBinder.class);

	interface DocumentCreatorUiBinder extends UiBinder<Widget, DocumentCreator> {
	}

	public DocumentCreator() {
		initWidget(uiBinder.createAndBindUi(this));

        btnCreateDocument.setEnabled(false);
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
