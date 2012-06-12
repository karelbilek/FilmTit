package cz.filmtit.client;

import com.google.gwt.user.client.Window;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import cz.filmtit.client.SubgestBox.FakeSubgestBox;

import cz.filmtit.share.*;

//lib-gwt-file imports:
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
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

	GuiStructure guiStructure;
	
	protected List<TimedChunk> chunklist;
	
	//private List<Label> sources = new ArrayList<Label>();
	private List<SubgestBox> targetBoxes = new ArrayList<SubgestBox>();
	
	//private TextArea txtDebug;
	//private RadioButton rdbFormatSrt;
	//private RadioButton rdbFormatSub;

	protected RootPanel rootPanel;
	//protected AbsolutePanel mainPanel;
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
		//rootPanel.setSize("800", "600");


		// --- loading of the uibinder-defined structure --- //
		guiStructure = new GuiStructure();
		rootPanel.add(guiStructure, 20, 20);
		// --- end of loading of uibinder --- //
		

		// --- main interface --- //
		table = new FlexTable();
		guiStructure.scrollPanel.add(table);
		table.setWidth("100%");

        table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "164px");
        table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "410px");
		table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "410px");

        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.getRowFormatter().setStyleName(0, "header");


		/*
		// filling the interface with the sample subtitles:
		List<TranslationResult> transresults = (new SampleDocument()).translationResults;
		for (TranslationResult transresult : transresults) {
			showResult(transresult);
		}
		*/
		// --- end of main interface --- //

		
		// --- file reading interface via lib-gwt-file --- //
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				subtext = freader.getStringResult();
				//log(subtext);
				
				// TODO: movieTitle, year, language
				rpcHandler.createDocument("My Movie", "2012", "en");
				// sets currentDocument and calls processText() on success
			}
		} );
		

		guiStructure.fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				//log(fileUpload.getFilename());
				FileList fl = guiStructure.fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					String encoding = "utf-8";  // default value
					if (guiStructure.rdbEncodingUtf8.getValue()) {
						encoding = "utf-8";
					}
					else if (guiStructure.rdbEncodingWin.getValue()) {
						encoding = "windows-1250";
					}
					else if (guiStructure.rdbEncodingIso.getValue()) {
						encoding = "iso-8859-2";
					}
					freader.readAsText(fit.next(), encoding);
				}
				else {
					error("No file chosen.\n");
				}
			}
		} );
		// --- end of file reading interface via lib-gwt-file --- //
		
		
		// --- textarea interface for loading whole subtitle file --- //
		guiStructure.btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				subtext = guiStructure.txtFileContentArea.getText();
				// TODO: movieTitle, year, language
				rpcHandler.createDocument("My Movie", "2012", "en");
				// sets currentDocument and calls processText() on success
			}
		} );
		// --- end of textarea interface --- //
		
		
		// hiding the suggestion popup when scrolling the subtitle panel
		guiStructure.scrollPanel.addScrollHandler( new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				deactivateSuggestionWidget();
			}
		} );
		

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
		if (guiStructure.rdbFormatSub.getValue()) {  // i.e. ".sub" is checked
			subformat = "sub";
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked
			assert guiStructure.rdbFormatSrt.getValue() : "One of the subtitle formats must be chosen.";
			subformat = "srt";
			subtextparser = new ParserSrt();
		}
		log("subtitle format chosen: " + subformat);
				
		// parse:
		log("starting parsing");
		long startTime = System.currentTimeMillis();
		this.chunklist = subtextparser.parse(this.subtext, this.currentDocument.getId());
		long endTime = System.currentTimeMillis();
		long parsingTime = endTime - startTime;
		log("parsing finished in " + parsingTime + "ms");

		for (TimedChunk chunk : chunklist) {
		    TranslationResult tr = new TranslationResult();
		    tr.setSourceChunk(chunk);
		    this.currentDocument.translationResults.add(tr);
		}

		// output the parsed chunks:
		log("\nparsed chunks: "+chunklist.size());

        int i=0;
        for (TimedChunk timedchunk : chunklist) {
            this.showSource(timedchunk, i++);
		}

		Scheduler.get().scheduleIncremental(new SendChunksRepeatingCommand(chunklist));

	}
	
	
	
	class SendChunksRepeatingCommand implements RepeatingCommand {

		LinkedList<TimedChunk> chunks;
		
		public SendChunksRepeatingCommand(List<TimedChunk> chunks) {
			this.chunks = new LinkedList<TimedChunk>(chunks);
		}


        //exponential window
        //
        //a "trick" - first subtitle goes in a single request so it's here soonest without wait
        //then the next two
        //then the next four
        //then next eight
        //so the first 15 subtitles arrive as quickly as possible
        //but we also want as little requests as possible -> the "window" is
        //exponentially growing
        int exponential = 1;

		@Override
        public boolean execute() {
			if (chunks.isEmpty()) {
				return false;
			} else {
                List<TimedChunk> sentTimedchunks = new ArrayList<TimedChunk>(exponential);
				for (int i = 0; i< exponential; i++) {
                    if (!chunks.isEmpty()){
                        TimedChunk timedchunk = chunks.removeFirst();
				        sentTimedchunks.add(timedchunk);
                    }
                }
                sendChunks(sentTimedchunks);
			    exponential = exponential*2;	
                return true;
			}
		}
		
		private void sendChunks(List<TimedChunk> timedchunks) {
			rpcHandler.getTranslationResults(timedchunks);
		}
	}
	
	
	public Document getCurrentDocument() {
		return currentDocument;
	}
		
	protected void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}

    public void showSource(TimedChunk chunk, int index) {
		Label timeslabel = new Label(chunk.getStartTime() + " - " + chunk.getEndTime());
        timeslabel.setStyleName("chunk_timing");
		table.setWidget(index+1, TIMES_COLNUMBER, timeslabel);
		
        Label sourcelabel = new Label(chunk.getSurfaceForm());
        sourcelabel.setStyleName("chunk_l1");
		table.setWidget(index+1, SOURCETEXT_COLNUMBER, sourcelabel);

        SubgestBox targetbox = new SubgestBox(index, this); // suggestions handling - see the constructor for details
		SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox();
        targetBoxes.add(targetbox);
		table.setWidget(index + 1, TARGETBOX_COLNUMBER, fake);
		fake.setWidth("97%");
		

    }

    public void replaceFake(int id, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);
		
        real.setWidth("97%");
        real.setFocus(true);
    }

	/**
	 * Adds the given TranslationResult to the current listing interface.
	 * @param transresult - the TranslationResult to be shown
	 */
	public void showResult(TranslationResult transresult, int index) {
	    targetBoxes.get(index).setTranslationResult(transresult);	
		
		counter++;
	}
	
	private Widget getActiveSuggestionWidget() {
		return activeSuggestionWidget;
	}
	
	protected void setActiveSuggestionWidget(Widget w) {
		activeSuggestionWidget = w;
	}
	
	/**
	 * Hide the currently active (visible) popup with suggestions
	 */
	protected void deactivateSuggestionWidget() {
		Widget w = getActiveSuggestionWidget();
		if (w != null) {
			if (w instanceof PopupPanel) {
				//((PopupPanel)w).hide();
				((PopupPanel)w).setVisible(false);
			}
			else {
				((Panel)(w.getParent())).remove(w);
			}
			setActiveSuggestionWidget(null);
		}
	}

	/**
	 * Send the given translation result as a "user-feedback" to the userspace
	 * @param transresult
	 */
	public void submitUserTranslation(TranslationResult transresult) {
		String combinedTRId = transresult.getDocumentId() + ":" + transresult.getChunkId();
		log("sending user feedback with values: " + combinedTRId + ", " + transresult.getUserTranslation() + ", " + transresult.getSelectedTranslationPairID());
		rpcHandler.setUserTranslation(transresult.getChunkId(), transresult.getDocumentId(),
				                      transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
	}
	
	/**
	 * Set the focus to the next SubgestBox in order.
	 * If there is not any, stay in the current one and return false.
	 * @param currentBox - the SubgestBox relative to which is the "next" determined
	 * @return false if the currentBox is the last one (and therefore nothing has changed),
	 *         true otherwise
	 */
	public boolean goToNextBox(SubgestBox currentBox) {
		// the next box is the second element of the tailSet determined by the currentBox
		int nextIndex = currentBox.getId()+1;
        if (nextIndex >= targetBoxes.size()) {
            return false;
        }
        //TODO: sort out FakeSubgestBox
        targetBoxes.get(nextIndex).setFocus(true);
        return true;
	}
	
	/**
	 * Set the focus to the previous SubgestBox in order.
	 * If there is not any, stay in the current one and return false.
	 * @param currentBox - the SubgestBox relative to which is the "previous" determined
	 * @return false if the currentBox is the first one (and therefore nothing has changed),
	 *         true otherwise
	 */
	public boolean goToPreviousBox(SubgestBox currentBox) {
		// the previous box is the last element of the headSet determined by the currentBox
		int lastIndex = currentBox.getId()-1;
        if (lastIndex <0) {
            return false;
        }
        //TODO: sort out FakeSubgestBox
        targetBoxes.get(lastIndex).setFocus(true);
	    return true;
    }
	
	/**
	 * Output the given text in the debug textarea
	 * @param logtext
	 */

    long start=0;
	public void log(String logtext) {
		if (start == 0) {
            start = System.currentTimeMillis();
        }
        long diff = (System.currentTimeMillis() - start);
        guiStructure.txtDebug.setText(guiStructure.txtDebug.getText() + diff+" : " + logtext + "\n");
		guiStructure.txtDebug.setCursorPos(guiStructure.txtDebug.getText().length());
	}
	
	private void error(String errtext) {
		log(errtext);
	}
	
}
