package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.incubator.Table;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import cz.filmtit.client.SubgestBox.FakeSubgestBox;

import cz.filmtit.share.*;
import cz.filmtit.share.parsing.*;

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
	
	private List<FakeSubgestBox> targetBoxes = new ArrayList<FakeSubgestBox>();

	protected RootPanel rootPanel;

	private FlexTable table;
	protected int counter = 0;
	// column numbers in the subtitle-table
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
	//private String subtext;
	
	private DocumentCreator docCreator;
	
	
	
	@Override
	public void onModuleLoad() {

        // RPC:
		// FilmTitServiceHandler has direct access
		// to package-internal (and public) fields and methods
		// of this Gui instance
		// (because the RPC calls are asynchronous)
		rpcHandler = new FilmTitServiceHandler(this);
		
		// Request translation suggestions for a TimedChunk via:
		// rpcHandler.getTranslationResults(timedchunk);
		// Because the calls are asynchronous, the method returns void.

		// Send feedback via:
		// rpcHandler.setUserTranslation(translationResultId, userTranslation, chosenTranslationPair);
		
		
		// -------------------- //
		// --- GUI creation --- //
		// -------------------- //
		
		rootPanel = RootPanel.get();
		//rootPanel.setSize("800", "600");


		// --- loading the uibinder-defined structure of the page --- //
		guiStructure = new GuiStructure();
		rootPanel.add(guiStructure, 0, 0);
		// --- end of loading the uibinder --- //
		

		// --- main interface --- //
		// only preparing the table - not showing it yet
		table = new FlexTable();
		table.setWidth("100%");

        table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "164px");
        table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "410px");
		table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "410px");

        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.getRowFormatter().setStyleName(0, "header");
		// --- end of main interface --- //


        docCreator = new DocumentCreator();
        guiStructure.scrollPanel.setWidget(docCreator);
        guiStructure.scrollPanel.addStyleName("creating_document");
        
        
        
		// --- file reading interface via lib-gwt-file --- //
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				createDocumentFromText( freader.getStringResult() );
				//log(subtext);
			}
		} );
		

		docCreator.fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				//log(fileUpload.getFilename());
				FileList fl = docCreator.fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					freader.readAsText(fit.next(), docCreator.getChosenEncoding());
				}
				else {
					error("No file chosen.\n");
				}
			}
		} );
		// --- end of file reading interface via lib-gwt-file --- //
		
		
		// --- textarea interface for loading whole subtitle file --- //
		docCreator.btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createDocumentFromText( docCreator.txtFileContentArea.getText() );
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
	



    private void createDocumentFromText(String subtext) {
        // replacing the document-creating interface with the subtitle table:
        guiStructure.scrollPanel.removeStyleName("creating_document");
        guiStructure.scrollPanel.addStyleName("translating");
        guiStructure.scrollPanel.setWidget(table);

        rpcHandler.createDocument(docCreator.getMovieTitle(),
                docCreator.getMovieYear(),
                docCreator.getChosenLanguage(),
                subtext);
        // sets currentDocument and calls processText() on success
    }


	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.chunklist (List<TimedChunk>).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
     *
     * @param subtext - multiline text (of the whole subtitle file, typically) to parse
	 */
	protected void processText(String subtext) {
		// dump the input text into the debug-area:
		log("processing the following input:\n" + subtext + "\n");
		
		// determine format (from corresponding radiobuttons) and choose parser:
		String subformat = docCreator.getChosenSubFormat();
		Parser subtextparser;
		if (subformat == "sub") {  // i.e. ".sub" is checked
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked
			assert subformat == "srt" : "One of the subtitle formats must be chosen.";
			subtextparser = new ParserSrt();
		}
		log("subtitle format chosen: " + subformat);
				
		// parse:
		log("starting parsing");
		long startTime = System.currentTimeMillis();
		this.chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
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
				for (int i = 0; i < exponential; i++) {
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
		
                            //html because of <br />
        Label sourcelabel = new HTML(chunk.getGUIForm());
        sourcelabel.setStyleName("chunk_l1");
		table.setWidget(index+1, SOURCETEXT_COLNUMBER, sourcelabel);

        SubgestBox targetbox = new SubgestBox(index, this); // suggestions handling - see the constructor for details
		SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox();
        targetBoxes.add(fake);
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
	    targetBoxes.get(index).getFather().setTranslationResult(transresult);
		
		counter++;
	}


	protected void setActiveSuggestionWidget(Widget w) {
		this.activeSuggestionWidget = w;
	}

	
	/**
	 * Hide the currently active (visible) popup with suggestions
	 */
	protected void deactivateSuggestionWidget() {
		Widget w = this.activeSuggestionWidget;
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
		int currentIndex = currentBox.getId();
		//final int nextIndex = (currentIndex < targetBoxes.size()-1) ? (currentIndex + 1) : currentIndex;
        final int nextIndex = currentIndex + 1;
		if (nextIndex >= targetBoxes.size()) {
            return false;
        }
        Scheduler.get().scheduleDeferred( new ScheduledCommand() {
			@Override
			public void execute() {
		        targetBoxes.get(nextIndex).setFocus(true);
			}
		} );
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
		int currentIndex = currentBox.getId();
		//final int prevIndex = (currentIndex > 0) ? (currentIndex - 1) : currentIndex;
		final int prevIndex = currentIndex - 1;
		if (prevIndex <0) {
        	return false;
        }
        Scheduler.get().scheduleDeferred( new ScheduledCommand() {
			@Override
			public void execute() {
		        targetBoxes.get(prevIndex).setFocus(true);
			}
		} );
	    return true;
    }


    long start=0;
	/**
	 * Output the given text in the debug textarea
     * with a timestamp relative to the first logging.
	 * @param logtext
	 */
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
