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
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
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



/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

	protected List<TimedChunk> chunklist;
	
	//private List<Label> sources = new ArrayList<Label>();
	private List<SubgestBox> targetBoxes = new ArrayList<SubgestBox>();

	private TextArea txtDebug;
	private RadioButton rdbFormatSrt;
	private RadioButton rdbFormatSub;

	protected RootPanel rootPanel;
	protected AbsolutePanel mainPanel;
	protected AbsolutePanel suggestArea;
	
	private FlexTable table;
	protected int counter = 0;
	// column numbers in 
	private static final int TIMES_COLNUMBER      = 0;
	private static final int SOURCETEXT_COLNUMBER = 1;
	private static final int TARGETBOX_COLNUMBER  = 2;  

	private FilmTitServiceHandler rpcHandler;
	protected Document currentDocument;
	
	protected Widget activeSuggestionWidget = null;
	protected SubgestHandler subgestHandler = new SubgestHandler(this);

	/**
	 * Multi-line subtitle text to parse
	 */
	private String subtext;
	
	
	@Override
	public void onModuleLoad() {

		
		//sublist = new GUISubtitleList(new SampleDocument()); 
		
		// FilmTitServiceHandler has direct access
		// to package-internal (and public) fields and methods
		// of this Gui instance
		// (because the RPC calls are asynchronous)
		rpcHandler = new FilmTitServiceHandler(this);
		
		// Request translation suggestions for a TimedChunk via:
		// rpcHandler.getTranslationResults(timedchunk);
		//
		// Because the calls are asynchronous, the method returns void.
		// The result will automatically appear in trlist once it arrives.
		
		// Send feedback via:
		// rpcHandler.setUserTranslation(translationResultId, userTranslation, chosenTranslationPair);
		
		
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

		
		// debug-area (for logging output, dumps etc.):
		txtDebug = new TextArea();
		rootPanel.add(txtDebug, 412, 624);
		txtDebug.setSize("368px", "176px");
		txtDebug.setText("debugging outputs...\n");

		
		
		
		// --- main interface --- //
		ScrollPanel scrollPanel = new ScrollPanel();
		rootPanel.add(scrollPanel, 10, 80);
		scrollPanel.setSize("774px", "402px");
		mainPanel = new AbsolutePanel();
		mainPanel.setStylePrimaryName("mainPanel");
		scrollPanel.add(mainPanel);
		scrollPanel.setStylePrimaryName("scrollPanel");
		
		table = new FlexTable();
		mainPanel.add(table);
		table.setWidth("100%");
		table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "15%");
		table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "40%");
		table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "50%");
		// TODO: header row?
		
		/*
		// filling the interface with the sample subtitles:
		List<TranslationResult> transresults = (new SampleDocument()).translationResults;
		for (TranslationResult transresult : transresults) {
			showResult(transresult);
		}
		*/
		// --- end of main interface --- //
		
		
		
		
		// --- subfile format (srt/sub) options --- //
		// (currently used by both textarea and file input)
		Label lblChooseFileFormat = new Label("Choose file format: (both for file & txtarea)");
		rootPanel.add(lblChooseFileFormat, 10, 530);
		lblChooseFileFormat.setSize("160px", "42px");
		
		rdbFormatSrt = new RadioButton("file format", ".srt");
		rootPanel.add(rdbFormatSrt, 175, 530);
		rdbFormatSrt.setSize("105px", "20px");
		rdbFormatSub = new RadioButton("file format", ".sub");
		rootPanel.add(rdbFormatSub, 175, 556);
		rdbFormatSub.setSize("105px", "20px");
		rdbFormatSrt.setValue(true);  // default - srt
		// --- end of subfile format options --- //


		
		// --- file reading interface via lib-gwt-file --- //
		Label lblChooseEncoding = new Label("Choose encoding:");
		rootPanel.add(lblChooseEncoding, 286, 530);
		lblChooseEncoding.setSize("121px", "42px");
		
		final RadioButton rdbEncodingUtf8 = new RadioButton("file encoding", "UTF-8");
		rootPanel.add(rdbEncodingUtf8, 412, 530);
		rdbEncodingUtf8.setSize("105px", "20px");
		final RadioButton rdbEncodingWin  = new RadioButton("file encoding", "windows-1250");
		rootPanel.add(rdbEncodingWin, 412, 556);
		rdbEncodingWin.setSize("105px", "20px");
		final RadioButton rdbEncodingIso  = new RadioButton("file encoding", "iso-8859-2");
		rootPanel.add(rdbEncodingIso, 412, 582);
		rdbEncodingIso.setSize("105px", "20px");
		rdbEncodingUtf8.setValue(true);  // default - UTF-8
		
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				subtext = freader.getStringResult();
				//txtDebug.setText(txtDebug.getText() + subtext);
				
				// TODO: movieTitle, year, language
				rpcHandler.createDocument("My Movie", "2012", "en");
				// sets currentDocument and calls processText() on success
			}
		} );
		
		final FileUploadExt fileUpload = new FileUploadExt();
		fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				//log(fileUpload.getFilename());
				FileList fl = fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					String encoding = "utf-8";  // default value
					if (rdbEncodingUtf8.getValue()) {
						encoding = "utf-8";
					}
					else if (rdbEncodingWin.getValue()) {
						encoding = "windows-1250";
					}
					else if (rdbEncodingIso.getValue()) {
						encoding = "iso-8859-2";
					}
					freader.readAsText(fit.next(), encoding);
				}
				else {
					error("No file chosen.\n");
				}
			}
		} );
		rootPanel.add(fileUpload, 10, 494);
		// --- end of file reading interface via lib-gwt-file --- //
		
		
		
		// --- textarea interface for loading whole subtitle file --- //
		final TextArea txtFileContentArea = new TextArea();
		rootPanel.add(txtFileContentArea, 10, 624);
		txtFileContentArea.setSize("260px", "176px");
		
		Button btnSendToTm = new Button("Send to TM");
		btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				subtext = txtFileContentArea.getText();
				// TODO: movieTitle, year, language
				rpcHandler.createDocument("My Movie", "2012", "en");
				// sets currentDocument and calls processText() on success
			}
		} );
		rootPanel.add(btnSendToTm, 286, 624);
		btnSendToTm.setSize("120px", "47px");
		// --- end of textarea interface --- //
		

		

	}	// onModuleLoad()
	

	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.chunklist (List<TimedChunk>).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
	 */
	protected void processText() {
		// dump the input text into the debug-area:
		log("processing the following input:\n" + this.subtext + "\n");
		
		// determine format (from corresponding radiobuttons) and choose parser:
		String subformat;
		Parser subtextparser;
		if (rdbFormatSub.getValue()) {  // i.e. ".sub" is checked
			subformat = "sub";
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked
			assert rdbFormatSrt.getValue() : "One of the subtitle formats must be chosen.";
			subformat = "srt";
			subtextparser = new ParserSrt();
		}
		log("subtitle format chosen: " + subformat);
				
		// parse:
		this.chunklist = subtextparser.parse(this.subtext, this.currentDocument.getId());
		for (TimedChunk chunk : chunklist) {
			TranslationResult tr = new TranslationResult();
			tr.setSourceChunk(chunk);
			this.currentDocument.translationResults.add(tr);
		}
		
		// output the parsed chunks:
		log("\nparsed chunks:");
		for (TimedChunk timedchunk : chunklist) {
			log(timedchunk.getStartTime() + " --> " + timedchunk.getEndTime() + " ::: " + timedchunk.getSurfaceForm() + "\n");

			log("sending timed chunk to get some translation result: " + timedchunk.getSurfaceForm());
			rpcHandler.getTranslationResults(timedchunk);
		}
		
	}
	
	
	public Document getCurrentDocument() {
		return currentDocument;
	}
		
	protected void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}

	/**
	 * Adds the given TranslationResult to the current listing interface.
	 * @param transresult - the TranslationResult to be shown
	 */
	public void showResult(TranslationResult transresult, int index) {
		//log("showing result of chunk: " + transresult.getSourceChunk().getSurfaceForm());
		Label timeslabel = new Label(transresult.getSourceChunk().getStartTime() + " -> " + transresult.getSourceChunk().getEndTime());
		table.setWidget(index, TIMES_COLNUMBER, timeslabel);
		
		Label sourcelabel = new Label(transresult.getSourceChunk().getSurfaceForm());
		table.setWidget(index, SOURCETEXT_COLNUMBER, sourcelabel);
		
		SubgestBox targetbox = new SubgestBox(index, transresult, this); // suggestions handling - see the constructor for details
		targetBoxes.add(targetbox);
		table.setWidget(index, TARGETBOX_COLNUMBER, targetbox);
		targetbox.setWidth("97%");
		
		counter++;
	}
	
	public Widget getActiveSuggestionWidget() {
		return activeSuggestionWidget;
	}
	
	public void setActiveSuggestionWidget(Widget w) {
		activeSuggestionWidget = w;
	}
	
	public void submitUserTranslation(TranslationResult transresult) {
		String combinedTRId = transresult.getDocumentId() + ":" + transresult.getChunkId();
		log("sending user feedback with values: " + combinedTRId + ", " + transresult.getUserTranslation() + ", " + transresult.getSelectedTranslationPairID());
		rpcHandler.setUserTranslation(transresult.getChunkId(), transresult.getDocumentId(),
				                      transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
	}
	
	public void goToNextBox(SubgestBox currentBox) {
		int nextIndex = targetBoxes.indexOf(currentBox) + 1;
		if (nextIndex <= targetBoxes.size()) {
			targetBoxes.get(nextIndex).setFocus(true);
		}
		else {
			// do nothing - stay where you are...
		}
	}
	
	public void log(String logtext) {
		txtDebug.setText(txtDebug.getText() + logtext + "\n");
		txtDebug.setCursorPos(txtDebug.getText().length());
	}
	
	private void error(String errtext) {
		log(errtext);
	}
	
}
