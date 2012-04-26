package cz.filmtit.client;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.RadioButton;

import cz.filmtit.share.*;

//lib-gwt-file imports:
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestBox;



/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

	//private GUISubtitleList sublist;
	
	private List<Label> sources = new ArrayList<Label>();
	private List<TextBox> targets = new ArrayList<TextBox>();

	private TextArea txtDebug;
	private RadioButton rdbFormatSrt;
	private RadioButton rdbFormatSub;

	private RootPanel rootPanel; 

	private FilmTitServiceHandler rpcHandler;
	protected Document currentDoc;
	
	
	@Override
	public void onModuleLoad() {

		
		//sublist = new GUISubtitleList(new SampleDocument()); 
		
		// FilmTitServiceHandler has direct access
		// to package-internal (and public) fields and methods
		// of this Gui instance
		// (because the RPC calls are asynchronous)
		rpcHandler = new FilmTitServiceHandler(this);
		
		// Request translation suggestions for a TimedChunk via:
		// rpcHandler.suggestions(chunk);
		//
		// Because the calls are asynchronous, the method returns void.
		// The result will automatically appear in trlist once it arrives.
		
		// Send feedback via:
		// rpcHandler.feedback(translationResultId, chosenTranslationPair, userTranslation);
		
		
		// -------------------- //
		// --- GUI creation --- //
		// -------------------- //
		
		rootPanel = RootPanel.get();
		rootPanel.setSize("800", "600");

		
		// adding header:
		Label lblHeader = new Label("FilmTit - translate your subtits!");
		lblHeader.setStyleName("gwt-Header");
		lblHeader.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.add(lblHeader, 10, 0);
		lblHeader.setSize("436px", "0px");

		
		// --- main interface --- //
		VerticalPanel panSources = new VerticalPanel();
		rootPanel.add(panSources, 10, 80);
		panSources.setSize("327px", "315px");
		VerticalPanel panTargets = new VerticalPanel();
		rootPanel.add(panTargets, 345, 80);
		panTargets.setSize("327px", "315px");
		
		Label lblNeco = new Label("To je teda něco...");
		panSources.add(lblNeco);
		
		TextBox textBox = new TextBox();
		panTargets.add(textBox);
		
		SuggestBox suggestBox = new SuggestBox();
		panTargets.add(suggestBox);
		
		// filling the interface with the source subtitles:
		//ListIterator<GUIChunk> chunkiterator = sublist.getChunks().listIterator();
		Iterator<TranslationResult> transresultiterator = (new SampleDocument()).translationResults.iterator();
		while(transresultiterator.hasNext()) {
			TranslationResult transresult = transresultiterator.next();
			
			Label sourcelabel = new Label(transresult.getSourceChunk().getSurfaceform());
			sources.add(sourcelabel);
			panSources.add(sourcelabel);
			
		}
		
		
		// --- end of main interface --- //
		
		
		

		// debug-area:
		txtDebug = new TextArea();
		rootPanel.add(txtDebug, 412, 530);
		txtDebug.setSize("460px", "176px");
		txtDebug.setText("debugging outputs...\n");
		
		
		
		// --- subfile format (srt/sub) options --- //
		// (currently used by both textarea and file input)
		Label lblChooseFileFormat = new Label("Choose file format: (both for file & txtarea)");
		rootPanel.add(lblChooseFileFormat, 10, 476);
		lblChooseFileFormat.setSize("160px", "42px");
		
		rdbFormatSrt = new RadioButton("file format", ".srt");
		rootPanel.add(rdbFormatSrt, 175, 476);
		rdbFormatSrt.setSize("105px", "20px");
		rdbFormatSub = new RadioButton("file format", ".sub");
		rootPanel.add(rdbFormatSub, 175, 502);
		rdbFormatSub.setSize("105px", "20px");
		rdbFormatSrt.setValue(true);  // default - srt
		// --- end of subfile format options --- //


		
		// --- file reading interface via lib-gwt-file --- //
		Label lblChooseEncoding = new Label("Choose encoding:");
		rootPanel.add(lblChooseEncoding, 286, 476);
		lblChooseEncoding.setSize("121px", "42px");
		
		final RadioButton rdbEncodingUtf8 = new RadioButton("file encoding", "UTF-8");
		rootPanel.add(rdbEncodingUtf8, 412, 476);
		rdbEncodingUtf8.setSize("105px", "20px");
		final RadioButton rdbEncodingIso = new RadioButton("file encoding", "iso-8859-2");
		rootPanel.add(rdbEncodingIso, 412, 502);
		rdbEncodingIso.setSize("105px", "20px");
		rdbEncodingUtf8.setValue(true);  // default - UTF-8
		
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				String subtext = freader.getStringResult();
				//txtDebug.setText(txtDebug.getText() + subtext);
				//processText(subtext);
			}
		} );
		
		final FileUploadExt fileUpload = new FileUploadExt();
		fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				FileList fl = fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					String encoding = "utf-8";  // default value
					if (rdbEncodingUtf8.getValue()) {
						encoding = "utf-8";
					}
					else if (rdbEncodingIso.getValue()) {
						encoding = "iso-8859-2";
					}
					freader.readAsText(fit.next(), encoding);
				}
				else {
					txtDebug.setText(txtDebug.getText() + "No file chosen.\n");
				}
			}
		} );
		rootPanel.add(fileUpload, 10, 440);
		// --- end of file reading interface via lib-gwt-file --- //
		
		
		
		// --- textarea interface for loading whole subtitle file --- //
		final TextArea txtFileContentArea = new TextArea();
		rootPanel.add(txtFileContentArea, 10, 530);
		txtFileContentArea.setSize("260px", "176px");
		
		Button btnSendToTm = new Button("Send to TM");
		btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String subtext = txtFileContentArea.getText();
				//processText(subtext);
			}
		} );
		rootPanel.add(btnSendToTm, 286, 530);
		btnSendToTm.setSize("120px", "47px");
		// --- end of textarea interface --- //		

		

	}	// onModuleLoad()
	

	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.sublist (GUISubtitleList).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
	 * 
	 * @param text Multi-line subtitle text to parse
	 */
	/*
	private void processText(String text) {
		// dump the input text into the debug-area:
		txtDebug.setText(txtDebug.getText() + "processing the following input:\n" + text + "\n");
		
		// determine format (from corresponding radiobuttons) and choose parser:
		String subformat;
		Parser subtextparser;
		if (rdbFormatSub.getValue()) {  // i.e. ".sub" is checked
			subformat = "sub";
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked (or something else - probably weird)
			subformat = "srt";
			subtextparser = new ParserSrt();
		}
		txtDebug.setText(txtDebug.getText() + "subtitle format chosen: " + subformat + "\n");
		
		// parse:
		GUISubtitleList mysublist = subtextparser.parse(text);
		
		// output the parsed chunks:
		//txtDebug.setText( Integer.toString( sublist2.getChunks().size()) + "\n");
		txtDebug.setText(txtDebug.getText() + "\nparsed chunks:\n");
		ListIterator<GUIChunk> chunkit = mysublist.getChunks().listIterator();
		while (chunkit.hasNext()) {
			txtDebug.setText(txtDebug.getText() + chunkit.next().toString() + "\n");
		}
		
		
		// TODO: reload the interface
		
		sublist = mysublist;
	}	// processText(...)
	*/
	
	public Document getCurrentDocument() {
		return currentDoc;
	}
}
