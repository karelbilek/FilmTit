package cz.filmtit.client;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
//import com.google.gwt.user.cellview.client.Column;
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
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionModel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;



/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

	private List<TimedChunk> chunklist;
	
	private List<Label> sources = new ArrayList<Label>();
	private List<TextBox> targets = new ArrayList<TextBox>();

	private TextArea txtDebug;
	private RadioButton rdbFormatSrt;
	private RadioButton rdbFormatSub;

	private RootPanel rootPanel;
	private AbsolutePanel panSources, panTargets;
	private int counter;

	private FilmTitServiceHandler rpcHandler;
	protected Document currentDoc;
	
	protected Widget activeSuggestionWidget;
	
	
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

		
		// debug-area:
		txtDebug = new TextArea();
		rootPanel.add(txtDebug, 412, 530);
		txtDebug.setSize("460px", "176px");
		txtDebug.setText("debugging outputs...\n");

		
		/*
		FlexTable table = new FlexTable();
		rootPanel.add(table, 10, 80);
		table.setWidth("315px");
		*/
		
		// --- main interface --- //
		//VerticalPanel panSources = new VerticalPanel();
		panSources = new AbsolutePanel();
		rootPanel.add(panSources, 10, 80);
		panSources.setSize("327px", "315px");
		panTargets = new AbsolutePanel();
		rootPanel.add(panTargets, 345, 80);
		panTargets.setSize("327px", "315px");
		
		// filling the interface with the source subtitles:
		//ListIterator<GUIChunk> chunkiterator = sublist.getChunks().listIterator();
		final List<TranslationResult> transresults = (new SampleDocument()).translationResults;
		counter = 0;
		for (TranslationResult transresult : transresults) {
			
			Label sourcelabel = new Label(transresult.getSourceChunk().getSurfaceForm());
			sources.add(sourcelabel);
			panSources.add(sourcelabel);
			//table.setWidget(counter, 0, sourcelabel);
			
			//AbsolutePanel targetpanel = new AbsolutePanel();
			
			SubgestBox targetbox = new SubgestBox(counter, transresult, rootPanel); // suggestions handling - see the constructor for details
			targetbox.addFocusHandler( new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					SubgestBox subbox = (SubgestBox) event.getSource();
					// hide the suggestion widget (currently Label) corresponding to the SubgestBox which previously lost focus
					if (activeSuggestionWidget != null) {
						((Panel)subbox.getParent()).remove(activeSuggestionWidget);
						activeSuggestionWidget = null;
					}
					subbox.showSuggestions();
					activeSuggestionWidget = subbox.getSuggestionWidget();
				}
			});
			targetbox.addKeyDownHandler( new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					SubgestBox subbox = (SubgestBox) event.getSource();
					// pressing Esc
					if (event.getNativeEvent().getCharCode() == KeyCodes.KEY_ESCAPE) {
						// hide the suggestion widget (currently Label) corresponding to the SubgestBox
						//   which previously had focus
						if (activeSuggestionWidget != null) {
							((Panel)subbox.getParent()).remove(activeSuggestionWidget);
							activeSuggestionWidget = null;
						}
					}
				}
			} );
			targetbox.addValueChangeHandler( new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					log("valuechange handled: " + event.getValue());
				}
			} );
			//targetpanel.add(targetbox);
			
			//table.setWidget(counter, 1, targetbox);
			panTargets.add(targetbox);
			targetbox.setWidth("80%");
			
			counter++;
		}
		// --- end of main interface --- //
		
		/*
		// --- celltable interface --- //
		// creating the table
		final CellTable<TranslationResult> subTable = new CellTable<TranslationResult>();
		subTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		// creating columns
		// times (separately?):
		Column<TranslationResult, String> timesColumn = new Column<TranslationResult, String>( new TextCell() ) {
			@Override
			public String getValue(TranslationResult transresult) {
				String startTime = transresult.getSourceChunk().getStartTime();
				String endTime   = transresult.getSourceChunk().getEndTime();
				return startTime + " -> " + endTime;
			}
		};
		subTable.addColumn(timesColumn);
		// source-language chunks:
		Column<TranslationResult, String> sourceChunkColumn = new Column<TranslationResult, String>( new TextCell() ) {
			@Override
			public String getValue(TranslationResult transresult) {
				return transresult.getSourceChunk().getSurfaceform();
			}
		};
		subTable.addColumn(sourceChunkColumn);
		// target-language edit boxes (with suggestions):
//		ClickableTextInputCell targetcell = new ClickableTextInputCell();
//		{
//			@Override
//			public void render(Context context,
//		            String value, SafeHtmlBuilder sb) {
//		        if (value != null) {
//		             MyWidget widget = new MyWidget(value);
//		             sb.appendEscaped(widget.getElement.getInnerHTML()); 
//		        }
//			}
//		};
		Column<TranslationResult, TranslationResult> targetColumn = new Column<TranslationResult, TranslationResult>(targetcell) {
			@Override
			public TranslationResult getValue(TranslationResult transresult) {
				return transresult;
			}
		};
		targetColumn.setFieldUpdater( new FieldUpdater<TranslationResult, TranslationResult>() {
			@Override
			public void update(int index, TranslationResult transresult, TranslationResult value) {
				Window.alert("You clicked " + transresult.getSourceChunk());
			}
		});
		subTable.addColumn(targetColumn);
		
		// filling with data
		//List<TranslationResult> transresults = (new SampleDocument()).translationResults;
		subTable.setRowCount(transresults.size(), true);
		subTable.setRowData(transresults);

		final int ROW_HEIGHT = 20;
		final SingleSelectionModel<TranslationResult> selectionModel = new SingleSelectionModel<TranslationResult>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				int rownumber = 5;
				TranslationResult transresult = selectionModel.getSelectedObject();
				if (transresult != null) {
					Label lblSuggestions = new Label("asdfasdf");
					for (TranslationPair transpair : transresult.getTmSuggestions()) {
						lblSuggestions.setText(lblSuggestions.getText() + transpair.getStringL2() + "\n");
					}
					rootPanel.add(lblSuggestions,
							subTable.getAbsoluteLeft() + 120,
							subTable.getAbsoluteTop() + rownumber*ROW_HEIGHT + 50);
				}
			}
		});
		subTable.setSelectionModel(selectionModel);
		
		// setting widths of table and columns
		subTable.setWidth("800px");
		subTable.setColumnWidth(timesColumn, "20%");
		subTable.setColumnWidth(sourceChunkColumn, "40%");
		subTable.setColumnWidth(targetColumn, "40%");
		
		rootPanel.add(subTable, 10, 350);
		// --- end of celltable interface --- //
		*/

		
		
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
				processText(subtext);
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
				processText(subtext);
			}
		} );
		rootPanel.add(btnSendToTm, 286, 530);
		btnSendToTm.setSize("120px", "47px");
		// --- end of textarea interface --- //		

		

	}	// onModuleLoad()
	

	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.chunklist (List<TimedChunk>).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
	 * 
	 * @param text Multi-line subtitle text to parse
	 */
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
		List<TimedChunk> mysublist = subtextparser.parse(text);
		
		// output the parsed chunks:
		//txtDebug.setText( Integer.toString( sublist2.getChunks().size()) + "\n");
		log("\nparsed chunks:");
		for (TimedChunk timedchunk : mysublist) {
			log(timedchunk.getStartTime() + " --> " + timedchunk.getEndTime() + " ::: " + timedchunk.getSurfaceForm() + "\n");

			log("sending timed chunk: " + timedchunk.getSurfaceForm());
			rpcHandler.getTranslationResults(timedchunk);
		}
		
		
		chunklist = mysublist;
	}	// processText(...)
	
	public Document getCurrentDocument() {
		return currentDoc;
	}
	
	public void showResult(TranslationResult transresult) {
		
		log("received result of chunk: " + transresult.getSourceChunk().getSurfaceForm());

		Label sourcelabel = new Label(transresult.getSourceChunk().getSurfaceForm());
		sources.add(sourcelabel);
		panSources.add(sourcelabel);
		//table.setWidget(counter, 0, sourcelabel);
		
		//AbsolutePanel targetpanel = new AbsolutePanel();
		
		SubgestBox targetbox = new SubgestBox(counter, transresult, rootPanel); // suggestions handling - see the constructor for details
		targetbox.addFocusHandler( new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				SubgestBox subbox = (SubgestBox) event.getSource();
				// hide the suggestion widget (currently Label) corresponding to the SubgestBox which previously lost focus
				if (activeSuggestionWidget != null) {
					((Panel)subbox.getParent()).remove(activeSuggestionWidget);
					activeSuggestionWidget = null;
				}
				subbox.showSuggestions();
				activeSuggestionWidget = subbox.getSuggestionWidget();
			}
		});
		targetbox.addKeyDownHandler( new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				SubgestBox subbox = (SubgestBox) event.getSource();
				// pressing Esc
				if (event.getNativeEvent().getCharCode() == KeyCodes.KEY_ESCAPE) {
					// hide the suggestion widget (currently Label) corresponding to the SubgestBox
					//   which previously had focus
					if (activeSuggestionWidget != null) {
						((Panel)subbox.getParent()).remove(activeSuggestionWidget);
						activeSuggestionWidget = null;
					}
				}
			}
		} );
		targetbox.addValueChangeHandler( new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				log("valuechange handled: " + event.getValue());
			}
		} );
		//targetpanel.add(targetbox);
		
		//table.setWidget(counter, 1, targetbox);
		panTargets.add(targetbox);
		targetbox.setWidth("80%");
		
		counter++;
	}
	
	public void log(String logtext) {
		txtDebug.setText(txtDebug.getText() + logtext + "\n");
	}
	
	private void error(String errtext) {
		log(errtext);
	}
	
}
