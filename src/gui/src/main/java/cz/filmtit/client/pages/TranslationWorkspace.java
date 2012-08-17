package cz.filmtit.client.pages;

import com.google.gwt.user.client.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

import cz.filmtit.client.*;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.ChangeSourceChunk;
import cz.filmtit.client.callables.CreateDocument;
import cz.filmtit.client.callables.DeleteDocumentSilently;
import cz.filmtit.client.callables.GetTranslationResults;
import cz.filmtit.client.callables.SaveSourceChunks;
import cz.filmtit.client.callables.SetChunkTimes;
import cz.filmtit.client.dialogs.TimeEditDialog;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.client.subgestbox.SubgestHandler;
import cz.filmtit.client.widgets.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.share.*;
import cz.filmtit.share.parsing.Parser;
import cz.filmtit.share.parsing.ParserSrt;
import cz.filmtit.share.parsing.ParserSub;

import java.util.*;


public class TranslationWorkspace extends Composite {

	private static TranslationWorkspaceUiBinder uiBinder = GWT.create(TranslationWorkspaceUiBinder.class);

	interface TranslationWorkspaceUiBinder extends UiBinder<Widget, TranslationWorkspace> {
	}

//    not used at the moment
//    /**
//     * Identifies this instance of workspace.
//     * Used because translation results must be bound to a workspace instance
//     * and invalidated if that instance is closed.
//     */
//    public final int id;
    
    /**
     * the currently active workspace
     */
    private static TranslationWorkspace currentWorkspace;
    
    /**
     * the currently active workspace
     */
    public static TranslationWorkspace getCurrentWorkspace() {
		return currentWorkspace;
	}

	/**
     * Indicates that the user moved away from this workspace
     * and that the loading of TranslationResults should be stopped
     */
    private boolean stopLoading = false;
    
	public void setStopLoading(boolean stopLoading) {
        for (GetTranslationResults getTranslationResults : sentGetTranslationsResultsCalls.values()) {
			getTranslationResults.stop();
		}
		this.stopLoading = true;
		Gui.log("stopLoading set for the workspace");
	}

	public boolean getStopLoading() {
		return stopLoading;
	}

	public enum DocumentOrigin { NEW, FROM_DB }
	
	// private DocumentOrigin documentOrigin;
	
    ///////////////////////////////////////
    //                                   //
    //      Current document             //
    //                                   //
    ///////////////////////////////////////
    
    public Document currentDocument;

    public Document getCurrentDocument() {
         return currentDocument;
    }

    public void setCurrentDocument(Document currentDocument) {
         this.currentDocument = currentDocument;
         Gui.getPageHandler().setDocumentId(currentDocument.getId());
    }
    
    SubtitleSynchronizer synchronizer = new SubtitleSynchronizer();


    ///////////////////////////////////////
    //                                   //
    //      Display fields               //
    //                                   //
    ///////////////////////////////////////

    public SubgestHandler subgestHandler;

    private List<SubgestBox.FakeSubgestBox> targetBoxes;
    private Widget activeSuggestionWidget = null;

    // column numbers in the subtitle-table
    private static final int TIMES_COLNUMBER      = 0;
    private static final int SOURCETEXT_COLNUMBER = 2;
    private static final int TARGETBOX_COLNUMBER  = 4;
    private static final int SOURCE_DIALOGMARK_COLNUMBER = 1;
    private static final int TARGET_DIALOGMARK_COLNUMBER = 3;

    private boolean isVideo=false;

    VLCWidget vlcPlayer;
    public VLCWidget getVlcPlayer(){
        return vlcPlayer;
    }
    
    HTMLPanel playerFixedPanel = null;
    HTMLPanel fixedWrapper = null;
    // UI binder fields
    @UiField
    ScrollPanel scrollPanel;
    @UiField
    SimplePanel emptyPanel;
	@UiField
    FlexTable table;
    
    @UiField
    HorizontalPanel translationHPanel;
  
    private boolean sourceSelected = false;

    /////////////////////////////////////
    //                                   //
    //      Initialization               //
    //                                   //
    ///////////////////////////////////////

    public TranslationWorkspace(Document doc, String path, DocumentOrigin documentOrigin) {
        initWidget(uiBinder.createAndBindUi(this));

        Gui.getPageHandler().setPageUrl(Page.TranslationWorkspace);
        Gui.getGuiStructure().activateMenuItem(Page.TranslationWorkspace);
        
        // id = Random.nextInt(Integer.MAX_VALUE);
        currentWorkspace = this;
        
        isVideo = path!=null;
        
        setCurrentDocument(doc);
        

        switch (documentOrigin) {
            case NEW:
                // wait for everything to load and for selectSource to return
                sourceSelected = false;
                break;
            case FROM_DB:
                // only wait for everything to load
                sourceSelected = true;    
                break;
            default:
                assert false;
                break;
        }
        
        this.targetBoxes = new ArrayList<SubgestBox.FakeSubgestBox>();

        scrollPanel.setStyleName("scrollPanel");
        // hiding the suggestion popup when scrolling the subtitle panel
        Gui.getGuiStructure().contentPanel.addScrollHandler( new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                deactivateSuggestionWidget();
            }
        } );

        table.setWidth("100%");
        table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "164px");
        table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "400px");
        table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "390px");
        table.getColumnFormatter().setWidth(SOURCE_DIALOGMARK_COLNUMBER,  "10px");
        table.getColumnFormatter().setWidth(TARGET_DIALOGMARK_COLNUMBER,  "10px");
        translationHPanel.setCellWidth(scrollPanel, "100%");
        translationHPanel.setCellWidth(emptyPanel, "0%");            

        if (isVideo) {
            HTMLPanel panelForVLC = Gui.getPanelForVLC();
            playerFixedPanel = new HTMLPanel("");
            
            panelForVLC.add(playerFixedPanel);
            playerFixedPanel.setWidth("100%");
            playerFixedPanel.setHeight("250px");
            playerFixedPanel.addStyleName("fixedPlayer");
            table.addStyleName("tableMoved");
            
            fixedWrapper = new HTMLPanel("");
            fixedWrapper.setWidth("984 px");


            HTML leftLabel = new HTML("");
            leftLabel.addStyleName("subtitleDisplayedLeft");
            fixedWrapper.addStyleName("fixedPlayerWrapper");
            fixedWrapper.add(leftLabel);


            HTML rightLabel = new HTML("");
            rightLabel.addStyleName("subtitleDisplayedRight");
            
            InlineLabel fromLabel = new InlineLabel("0:0:0");
            InlineLabel toLabel = new InlineLabel("0:0:30");
            Anchor pauseA = new Anchor("[pause]");
            Anchor replayA = new Anchor("[replay]");
   
            vlcPlayer = new VLCWidget(path, 400, 225, leftLabel, rightLabel, synchronizer, fromLabel, toLabel, pauseA, replayA, 0, this);
            vlcPlayer.addStyleName("vlcPlayerDisplayed"); 
            this.subgestHandler = new SubgestHandler(this);
            fixedWrapper.add(vlcPlayer);

            fixedWrapper.add(rightLabel);
           
            HTMLPanel playerStatusPanel = new HTMLPanel("");
            playerStatusPanel.add(new InlineLabel("currently playing from "));
            playerStatusPanel.add(fromLabel);
            playerStatusPanel.add(new InlineLabel(" to "));
            playerStatusPanel.add(toLabel);
 
            playerStatusPanel.add(new InlineLabel(" "));
            playerStatusPanel.add(pauseA);
            playerStatusPanel.add(new InlineLabel(" "));
            playerStatusPanel.add(replayA);
 
            fixedWrapper.add(playerStatusPanel);
            playerStatusPanel.addStyleName("statusPanel");
            
            playerFixedPanel.add(fixedWrapper);

        } else {
            this.subgestHandler = new SubgestHandler(this);
        }
        
        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.setWidget(0, SOURCE_DIALOGMARK_COLNUMBER, new Label(""));
        table.setWidget(0, TARGET_DIALOGMARK_COLNUMBER, new Label(""));
        table.getRowFormatter().setStyleName(0, "header");
         
        Gui.getGuiStructure().contentPanel.setWidget(this);
        Gui.getGuiStructure().contentPanel.setStyleName("translating");
        Gui.getGuiStructure().contentPanel.addStyleName("parsing");
	}

    
    //getting around the VLC bug when it randomly stops 
    public void reloadPlayer() {
        VLCWidget newWidget = vlcPlayer.higherNonce();
        fixedWrapper.addAndReplaceElement(newWidget, "video");
        vlcPlayer = newWidget;
    }
    
    ///////////////////////////////////////
    //                                   //
    //      Un-initialization            //
    //                                   //
    ///////////////////////////////////////

    private Map<Integer, GetTranslationResults> sentGetTranslationsResultsCalls = new HashMap<Integer, GetTranslationResults>();

    public void addGetTranslationsResultsCall (int id, GetTranslationResults call) {
    	sentGetTranslationsResultsCalls.put(id, call);
    }
    
    public void removeGetTranslationsResultsCall (int id) {
    	sentGetTranslationsResultsCalls.remove(id);
    }
    
    @Override
    public void onUnload() {
        setStopLoading(true);
        sourceSelected = false;
        translationStarted = false;
        Gui.getGuiStructure().contentPanel.removeStyleName("parsing");
        if (playerFixedPanel != null){
            Gui.getPanelForVLC().remove(playerFixedPanel);
        }
    }
   

    ///////////////////////////////////////
    //                                   //
    //      Action methods               //
    //                                   //
    ///////////////////////////////////////
    
    int lastIndex = 0;

    /**
     * Goes through TranslationResults of an already existing document
     * and loads translation results for the untranslated ones.
     */
    public void processTranslationResultList(List<TranslationResult> translations) {

    	if (stopLoading) {
    		return;
    	}
    	

          List<TimedChunk> untranslatedOnes = new LinkedList<TimedChunk>();
          List<TimedChunk> allChunks = new LinkedList<TimedChunk>();
          List<TranslationResult> results = new LinkedList<TranslationResult>();
        
          for (TranslationResult tr:translations) {
              TimedChunk sChunk = tr.getSourceChunk();
              synchronizer.putTranslationResult(tr);
              synchronizer.putSourceChunk(sChunk, -1, false);

              String tChunk = tr.getUserTranslation();
              

              ChunkIndex chunkIndex = sChunk.getChunkIndex();

              this.currentDocument.translationResults.put(chunkIndex, tr);

              allChunks.add(sChunk);


              if (tChunk==null || tChunk.equals("")){
                 untranslatedOnes.add(sChunk);
              } else {
                 results.add(tr);
              }
          }
          
          dealWithChunks(allChunks, results, untranslatedOnes);
          
    }

     /**
      * Parse the given text in the subtitle format of choice (by the radiobuttons)
      * into this.chunklist (List<TimedChunk>).
      * Currently verbosely outputting both input text, format
      * and output chunks into the debug-area,
      * also "reloads" the CellBrowser interface accordingly.
      * 
      * Might return prematurely if there is a parsing error.
      * In such case, the document is deleted and the user is redirected back to DocumentCreator.
     *
     * @param subtext - multiline text (of the whole subtitle file, typically) to parse
     * @param createDocumentCall reference to the call that created the document
     * and now probably holds a reference to an open MediaSelector
      */
     public void processText(String subtext, String subformat, CreateDocument createDocumentCall) {
          // dump the input text into the debug-area:
    	  // Gui.log("processing the following input:\n" + subtext + "\n");

          //chunkmap = new HashMap<ChunkIndex, TimedChunk>();
          //reverseTimeMap = new TreeMap<Double, TranslationResult>();

          // determine format (from corresponding radiobuttons) and choose parser:
          Parser subtextparser;
          if (subformat == "sub") {  // i.e. ".sub" is checked
               subtextparser = new ParserSub();
          }
          else {  // i.e. ".srt" is checked
               assert subformat == "srt" : "One of the subtitle formats must be chosen.";
               subtextparser = new ParserSrt();
          }
          Gui.log("subtitle format chosen: " + subformat);

          // parse:
          Gui.log("starting parsing");
          long startTime = System.currentTimeMillis();
          List<TimedChunk> chunklist = null;
          try {
              chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
          }
          catch (Exception e) {
        	  // user interaction
        	  createDocumentCall.hideMediaSelector();
        	  Window.alert("There was an error parsing the subtitle file:\n" + e.getMessage());
        	  // logging
        	  Gui.log("There was an error parsing the subtitle file!");
        	  Gui.exceptionCatcher(e, false);
        	  // action
        	  Gui.getPageHandler().loadPage(Page.DocumentCreator);
        	  new DeleteDocumentSilently(currentDocument.getId());
        	  // return prematurely
        	  return;
		  }
          long endTime = System.currentTimeMillis();
          long parsingTime = endTime - startTime;
          Gui.log("parsing finished in " + parsingTime + "ms");


          for (TimedChunk chunk : chunklist) {
              ChunkIndex chunkIndex = chunk.getChunkIndex();
              TranslationResult tr = new TranslationResult(chunk);
              this.currentDocument.translationResults.put(chunkIndex, tr);
              synchronizer.putTranslationResult(tr);
              synchronizer.putSourceChunk(tr, -1, false);
          }
          
          // save the chunks
          new SaveSourceChunks(chunklist, this, createDocumentCall);
          // now the user can close the browser, chunks are safely saved
     }

     SendChunksCommand sendChunksCommand;
     
     boolean translationStarted=false;
     /**
      * Creates the SendChunksCommand and, if possible, executes it
      * @param chunklist
      */
     void prepareSendChunkCommand(List<TimedChunk> chunklist) {
           
           
              sendChunksCommand = new SendChunksCommand(chunklist);
          
     }

     public void setSourceSelectedTrue() {
         this.sourceSelected = true;
     }
     public void startShowingTranslationsIfReady() {
         if (sourceSelected) {
            if (sendChunksCommand!=null) {
               if (translationStarted == false) {
                 sendChunksCommand.execute();
                 translationStarted=true;
               }
            }
         }
     }
     
        

     /**
      * Requests TranslationResults for the chunks,
      * sending them in groups to compromise between responsiveness and effectiveness.
      */
     public class SendChunksCommand {

          LinkedList<TimedChunk> chunks;

          public SendChunksCommand(List<TimedChunk> chunks) {
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
        
        /**
         * The maximum size of the window
         */
        int expMax = 64;

        public boolean execute() {
               if (stopLoading) {
                   return false;
               }

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
                    if (exponential > expMax) {
                    	exponential = expMax;
                    }
                    return true;
               }
          }
          
          /**
           * Called to tell that there will be no more results,
           * probably because the browser is offline,
           * so that for all the remaining chunks
           * there is no point in requesting them
           * or waiting for them.
           */
          public void noMoreResults() {
        	  for (TimedChunk chunk : chunks) {
				  noResult(chunk.getChunkIndex());
			  }
        	  chunks.clear();
          }

          private void sendChunks(List<TimedChunk> timedchunks) {
        	  FilmTitServiceHandler.getTranslationResults(timedchunks, SendChunksCommand.this, TranslationWorkspace.this);
          }
     }


     /**
      * Send the given translation result as a "user-feedback" to the userspace
      * @param transresult
      */
     public void submitUserTranslation(TranslationResult transresult) {
          String combinedTRId = transresult.getDocumentId() + ":" + transresult.getSourceChunk().getChunkIndex();
          Gui.log("sending user feedback with values: " + combinedTRId + ", " + transresult.getUserTranslation() + ", " + transresult.getSelectedTranslationPairID());

          ChunkIndex chunkIndex = transresult.getSourceChunk().getChunkIndex();
          FilmTitServiceHandler.setUserTranslation(chunkIndex, transresult.getDocumentId(),
                                          transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
          
          synchronizer.putTranslationResult(transresult);
          //reverseTimeMap.put((double)(transresult.getSourceChunk().getStartTimeLong()), transresult);
     }

     ///////////////////////////////////////
     //                                   //
     //      Display methods              //
     //                                   //
     ///////////////////////////////////////

     class ShowUserTranslatedCommand implements RepeatingCommand {
        LinkedList<TranslationResult> resultsToDisplay = new LinkedList<TranslationResult>();

        public ShowUserTranslatedCommand(List<TranslationResult> chunks) {
            this.resultsToDisplay.addAll(chunks);
        }

        @Override
        public boolean execute() {
             if (stopLoading) {
                return false;
             }

             if (!resultsToDisplay.isEmpty()) {
                 TranslationResult result = resultsToDisplay.removeFirst();
                 showResult(result);
                 return true;
             }
             return false;
        }
     }

     class ShowOriginalCommand implements RepeatingCommand {
         LinkedList<TimedChunk> chunksToDisplay = new LinkedList<TimedChunk>();
        
         /**
          * for a new document
          * (all chunks are sent to be translated, none of the chunks has a translation yet)
          * @param chunks all chunks
          */
         public ShowOriginalCommand(List<TimedChunk> chunks) {
              this.chunksToDisplay.addAll(chunks);
         }

        @Override
         public boolean execute() {
             if (stopLoading) {
                return false;
             }

             if (!chunksToDisplay.isEmpty()) {
                 TimedChunk timedchunk = chunksToDisplay.removeFirst();
                 showSource(timedchunk);
                 return true;
             }
             Gui.getGuiStructure().contentPanel.removeStyleName("parsing");
             return false;
         }
     }
    

     public void dealWithChunks(List<TimedChunk> original, List<TranslationResult> translated, List<TimedChunk> untranslated) {
          Scheduler.get().scheduleIncremental(new ShowOriginalCommand(original));
          Scheduler.get().scheduleIncremental(new ShowUserTranslatedCommand(translated));
          prepareSendChunkCommand(untranslated) ; 
          startShowingTranslationsIfReady() ; 
     }
     public void showSources(List<TimedChunk> chunks) {
        dealWithChunks(chunks, new LinkedList<TranslationResult>(), chunks);
     }

     private Map<ChunkIndex, Label> timeLabels = new HashMap<ChunkIndex, Label>();
     
     /**
     * Display the whole row for the given (source-language) chunk in the table, i.e. the timing,
     * the chunk text and an empty (fake)subgestbox. 
     *
     * We have to suppose these are coming in the same order as they appear in the source.
     * @param chunk - source-language chunk to show
     */
    public void showSource(TimedChunk chunk) {
        
    	ChunkIndex chunkIndex = chunk.getChunkIndex();

    	// create label
        Label timeslabel = new Label(chunk.getDisplayTimeInterval());
        timeslabel.setStyleName("chunk_timing");
		timeslabel.addDoubleClickHandler(new TimeChangeHandler(chunk));
		// add label to map
		timeLabels.put(chunkIndex, timeslabel);
		
        int index = lastIndex;
        lastIndex++;

        synchronizer.putSourceChunk(chunk, index, true);

                        //+1 because of the header
        table.setWidget(index + 1, TIMES_COLNUMBER, timeslabel);

        
        //html because of <br />
        Label sourcelabel = new HTML(chunk.getSurfaceForm());
        sourcelabel.setStyleName("chunk_l1");
        sourcelabel.addDoubleClickHandler(new SourceChangeHandler(chunk, sourcelabel));
        table.setWidget(index + 1, SOURCETEXT_COLNUMBER, sourcelabel);

        // initializing targetbox - fake
        SubgestBox targetbox = new SubgestBox(chunk, this, index+1);
        SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox(index+1);
        targetBoxes.add(fake);
        table.setWidget(index + 1, TARGETBOX_COLNUMBER, fake);

        // chunk-marking (dialogs):
        // setting sourcemarks:
        HTML sourcemarks = new HTML();
        sourcemarks.setStyleName("chunkmark");
        sourcemarks.setTitle("This line is part of the one-screen dialog.");
        if (chunk.isDialogue()) {
            sourcemarks.setHTML(sourcemarks.getHTML() + " - ");
        }
        if (! sourcemarks.getHTML().isEmpty()) {
            table.setWidget(index + 1, SOURCE_DIALOGMARK_COLNUMBER, sourcemarks);
            // and copying the same to the targets (GWT does not have .clone()):
            HTML targetmarks = new HTML(sourcemarks.getHTML());
            targetmarks.setStyleName(sourcemarks.getStyleName());
            targetmarks.setTitle(sourcemarks.getTitle());
            table.setWidget(index + 1, TARGET_DIALOGMARK_COLNUMBER, targetmarks);
        }

        // grouping:
        // alignment because of the grouping:
        //table.getRowFormatter().setVerticalAlign(index + 1, HasVerticalAlignment.ALIGN_BOTTOM);
        if (chunk.getPartNumber() > 1) {
            table.getRowFormatter().addStyleName(index + 1, "row_group_continue");
        }
        else {
            table.getRowFormatter().addStyleName(index + 1, "row_group_begin");
        }
        
    }

    /**
     * Used to change the time of a chunk.
     * Also changes of all chunks with the same id
     * (i.e. which are parts of the same chunk actually).
     * Very rough and very TODO now.
     */
    class TimeChangeHandler implements DoubleClickHandler {

    	private TimedChunk chunk;
    	
    	// computed and cached when invoked for the first time
    	private List<TimedChunk> chunks = null;
    	
		private TimeChangeHandler(TimedChunk chunk) {
			this.chunk = chunk;
		}

		@Override
		public void onDoubleClick(DoubleClickEvent event) {
			if (chunks == null) {
				chunks = synchronizer.getChunksById(chunk.getId());
			}
			// the chunks are directly modified by the TimeEditDialog
			new TimeEditDialog(chunks, TranslationWorkspace.this);
		}
    }

	/**
	 * Called when a time of some chunks gets changed
	 * by the TimeEditDialog.
	 * Changes the labels in the workspace
	 * to match the new values.
	 */
	public void changeTimeLabels(List<TimedChunk> chunks) {
		if (chunks == null || chunks.isEmpty()) {
			return;
		}
		
		String newLabelValue = chunks.get(0).getDisplayTimeInterval();
		for (TimedChunk chunk : chunks) {
			Label label = timeLabels.get(chunk.getChunkIndex());
			assert label != null : "Each chunk has its timelabel";
			label.setText(newLabelValue);
		}
	}


    /**
     * Used to change the source of a chunk.
     * Rough and probably TODO now.
     */
    class SourceChangeHandler implements DoubleClickHandler {

    	private ChunkIndex chunkIndex;
    	private Label label;
    	
		private SourceChangeHandler(TimedChunk chunk, Label label) {
			this.chunkIndex = chunk.getChunkIndex();
			this.label = label;
		}

		@Override
		public void onDoubleClick(DoubleClickEvent event) {
			// TODO probably something nicer than the prompt
			
			// init
			TimedChunk chunk = synchronizer.getChunkByIndex(chunkIndex);
			String oldSource = chunk.getDatabaseForm();
			
			// ask user for new value, showing the old one
			String newSource = Window.prompt("Source text for this chunk. " +
					"The pipe sign  |  (surrounded by spaces) denotes a new line.",
					oldSource);
			
			if (newSource == null || newSource.equals(oldSource)) {
				// cancel or no change
				return;
			}
			else {
				// change the values
				chunk.setDatabaseFormForce(newSource);
				label.getElement().setInnerHTML(chunk.getGUIForm());
				// this call brings a fresh translation result on return :-)
				// which is then given directly to showResult()
				new ChangeSourceChunk(chunk, newSource, TranslationWorkspace.this);
			}
		}
    }
    
    public void replaceFake(TimedChunk chunk, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        int id = synchronizer.getIndexOf(chunk);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);
        
        real.setFocus(true);
    }

    public TranslationResult getTranslationResultForIndex(int id) {
        SubgestBox sb = targetBoxes.get(id).getFather();
        TranslationResult tr = sb.getTranslationResult();
        return tr;
    }

    /**
     * Add the given TranslationResult to the current listing interface.
     * @param transresult - the TranslationResult to be shown
     */
    public void showResult(final TranslationResult transresult) {
        
        
        if (!synchronizer.isChunkDisplayed(transresult)) {
            //try it again after some time
             new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    showResult(transresult); 
                } 
            }.schedule(400); 
        } else {

            //index is there -> insert result
            int index = synchronizer.getIndexOf(transresult);

            targetBoxes.get(index).getFather().setTranslationResult(transresult);
            targetBoxes.get(index).removeStyleName("loading");
        }

    }

    /**
     * Called to tell workspace that there will be no translation result.
     * Removes the "loading" style from the boxes.
     * @param chunkIndex
     */
    public void noResult(final ChunkIndex chunkIndex) {
        
        if (!synchronizer.isChunkDisplayed(chunkIndex)) {
            //try it again after some time
             new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    noResult(chunkIndex); 
                } 
            }.schedule(400); 
        } else {
            //index is there -> insert result
            int index = synchronizer.getIndexOf(chunkIndex);
            targetBoxes.get(index).removeStyleName("loading");
        }

    }

    /**
     * Set the focus to the next SubgestBox in order.
     * If there is not any, stay in the current one and return false.
     * @param currentBox - the SubgestBox relative to which is the "next" determined
     * @return false if the currentBox is the last one (and therefore nothing has changed),
     *         true otherwise
     */
    public boolean goToNextBox(SubgestBox currentBox) {
        int currentIndex = synchronizer.getIndexOf(currentBox.getChunk());
        //final int nextIndex = (currentIndex < targetBoxes.size()-1) ? (currentIndex + 1) : currentIndex;
        final int nextIndex = currentIndex + 1;
        if (nextIndex >= targetBoxes.size()) {
            return false;
        }
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                SubgestBox.FakeSubgestBox targetbox = targetBoxes.get(nextIndex);
                if (targetbox.isAttached()) {
                    targetbox.setFocus(true);
                }
                else { // there is already a real box instead of the fake one
                    targetbox.getFather().setFocus(true);
                }
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
        int currentIndex = synchronizer.getIndexOf(currentBox.getChunk());
        //final int prevIndex = (currentIndex > 0) ? (currentIndex - 1) : currentIndex;
        final int prevIndex = currentIndex - 1;
        if (prevIndex <0) {
            return false;
        }
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                SubgestBox.FakeSubgestBox targetbox = targetBoxes.get(prevIndex);
                if (targetbox.isAttached()) {
                    targetbox.setFocus(true);
                }
                else { // there is already a real box instead of the fake one
                    targetbox.getFather().setFocus(true);
                }
            }
        } );
        return true;
    }


    public void ensureVisible(SubgestBox subbox) {
        Window.scrollTo(
                Window.getScrollLeft(),
                getScrollOffsetY(subbox.getElement())
                        - getVideoHeight()
                        - (Window.getClientHeight() - getVideoHeight()) * 2 / 5
        );
    }

    private int getVideoHeight() {
        return ( isVideo ? playerFixedPanel.getOffsetHeight() : 0 );
    }

    private native int getScrollOffsetY(Element e) /*-{
        if (!e)
          return;


        var item = e;
        var realOffset = 0;
        while (item && (item != document.body)) {
            realOffset += item.offsetTop;
            item = item.offsetParent;
        }

        return realOffset;
    }-*/;


    public void setActiveSuggestionWidget(Widget w) {
        this.activeSuggestionWidget = w;
    }


    /**
     * Hide the currently active (visible) popup with suggestions
     */
    public void deactivateSuggestionWidget() {
        Widget w = this.activeSuggestionWidget;
        if (w != null) {
            if (w instanceof PopupPanel) {
                //((PopupPanel)w).hide();
                w.setVisible(false);
            }
            else {
                ((Panel)(w.getParent())).remove(w);
            }
            setActiveSuggestionWidget(null);
        }
    }



}
