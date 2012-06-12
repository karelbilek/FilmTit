package cz.filmtit.client;

import org.vectomatic.file.FileUploadExt;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
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
		
	}

	@UiField
	TextBox txtMovieTitle;
	@UiField
	TextBox txtMovieYear;

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
	TextArea txtFileContentArea;
	
	@UiField
	SubmitButton btnSendToTm;

}
