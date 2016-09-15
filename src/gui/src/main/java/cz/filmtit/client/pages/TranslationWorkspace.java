/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.SubtitleSynchronizer;
import cz.filmtit.client.callables.*;
import cz.filmtit.client.dialogs.TimeEditDialog;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.client.subgestbox.SubgestHandler;
import cz.filmtit.client.widgets.VideoWidget;
import cz.filmtit.share.*;
import cz.filmtit.share.parsing.Parser;

import java.util.*;

/**
 * The main page of the application where the actual translations take place.
 * @author rur
 *
 */
public class TranslationWorkspace extends Composite {

	private static TranslationWorkspaceUiBinder uiBinder = GWT.create(TranslationWorkspaceUiBinder.class);

    /**
     * @return the lockedSubgestBox
     */
    public SubgestBox getLockedSubgestBox() {
        return lockedSubgestBox;
    }

    /**
     * @param lockedSubgestBox the lockedSubgestBox to set
     */
    public void setLockedSubgestBox(SubgestBox lockedSubgestBox) {
        this.lockedSubgestBox = lockedSubgestBox;
    }

    /**
     * @return the timer
     */
    public com.google.gwt.user.client.Timer getTimer() {
        return timer;
    }

    /**
     * @param timer the timer to set
     */
    public void setTimer(com.google.gwt.user.client.Timer timer) {
        this.timer = timer;
    }

    /**
     * @return the prevLockedSubgestBox
     */
    public SubgestBox getPrevLockedSubgestBox() {
        return prevLockedSubgestBox;
    }

    /**
     * @param prevLockedSubgestBox the prevLockedSubgestBox to set
     */
    public void setPrevLockedSubgestBox(SubgestBox prevLockedSubgestBox) {
        this.prevLockedSubgestBox = prevLockedSubgestBox;
    }

    /**
     * @return the currentDocument
     */
    public Document getCurrentDocument() {
        return currentDocument;
    }

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
    
    /**
     * Stops loading of translation suggestions into this workspace.
     * @param stopLoading
     */
	public void setStopLoading(boolean stopLoading) {
        for (GetTranslationResults getTranslationResults : sentGetTranslationsResultsCalls.values()) {
			getTranslationResults.stop();
		}
		this.stopLoading = true;
		Gui.log("stopLoading set for the workspace");
	}

	/**
	 * Checks whether loading of translation suggestions has been stopped.
	 */
	public boolean getStopLoading() {
		return stopLoading;
	}

	/**
	 * The document can be either newly created or loaded from the database.
	 * @author rur
	 *
	 */
	public enum DocumentOrigin { NEW, FROM_DB }
	
	// private DocumentOrigin documentOrigin;
	
    ///////////////////////////////////////
    //                                   //
    //      Current document             //
    //                                   //
    ///////////////////////////////////////
    
    private Document currentDocument;

//    public Document getCurrentDocument() {
//         return currentDocument;
//    }

    private void setCurrentDocument(Document currentDocument) {
         this.currentDocument = currentDocument;
         Gui.getPageHandler().setDocumentId(currentDocument.getId());
    }
    
    /**
     * Handles the subtitles of the current document.
     */
    SubtitleSynchronizer synchronizer = new SubtitleSynchronizer();


    ///////////////////////////////////////
    //                                   //
    //      Display fields               //
    //                                   //
    ///////////////////////////////////////

    /**
     * Handles events for all {@link SubgestBox} instances in this workspace.
     */
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

  //  private VLCWidget vlcPlayer;
    private VideoWidget videoPlayer;
   
    
    /**
     * Returns the player associated with this workspace.
     */
  //  public VLCWidget getVlcPlayer(){
   //     return vlcPlayer;
   // }
    
    /**
     * @return the videoPlayer
     */
    public VideoWidget getVideoPlayer() {
        return videoPlayer;
    }
    
    //UI elements for VLC
 //   private HTMLPanel playerFixedPanel = null;
 //   private HTMLPanel fixedWrapper = null;
    
        //UI elements for VideoWidget
    private HTMLPanel videoPlayerFixedPanel = null;
    private HTMLPanel videoFixedWrapper = null;
    
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
    
    /**
     * Creates and shows the workspace.
     */
    public TranslationWorkspace(Document doc, DocumentOrigin documentOrigin) {
        initWidget(uiBinder.createAndBindUi(this));

        Gui.getPageHandler().setPageUrl(Page.TranslationWorkspace);
        Gui.getGuiStructure().activateMenuItem(Page.TranslationWorkspace);
        
        // id = Random.nextInt(Integer.MAX_VALUE);
        currentWorkspace = this;
        
        isVideo = true;
                
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
        //translationHPanel.setCellWidth(emptyPanel, "0%");

        
        this.subgestHandler = new SubgestHandler(this);
        
        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.setWidget(0, SOURCE_DIALOGMARK_COLNUMBER, new Label(""));
        table.setWidget(0, TARGET_DIALOGMARK_COLNUMBER, new Label(""));
        table.getRowFormatter().setStyleName(0, "header");
         
        Gui.getGuiStructure().contentPanel.setWidget(this);
        Gui.getGuiStructure().contentPanel.setStyleName("translating");
        Gui.getGuiStructure().contentPanel.addStyleName("parsing");
        
        timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                lockedSubgestBox.getTranslationResult().setUserTranslation(lockedSubgestBox.getTextWithNewlines());

            // submitting only when the contents have changed
                if (lockedSubgestBox.textChanged()) {
                    submitUserTranslation(lockedSubgestBox, null);
                    lockedSubgestBox.updateLastText();
                } else {
                    new UnlockTranslationResult(lockedSubgestBox, currentWorkspace);
                }
                
                prevLockedSubgestBox.addStyleDependentName("unlocked");
            }
        };
	}
    
    /**
     * Closes the player.
     */
    public void turnOffPlayer() {
    	if (videoPlayerFixedPanel != null) {
            videoPlayerFixedPanel.removeFromParent();
    	}
    	/*if (vlcPlayer != null) {
            vlcPlayer.setHiddenTrue();
    	}*/
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
        if (videoPlayerFixedPanel != null){
            Gui.getPanelForVideo().remove(videoPlayerFixedPanel);
        }
    }
   

    ///////////////////////////////////////
    //                                   //
    //      Action methods               //
    //                                   //
    ///////////////////////////////////////
    
    private int lastIndex = 0;

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

            this.getCurrentDocument().translationResults.put(chunkIndex, tr);

            allChunks.add(sChunk);

            if (tChunk==null || tChunk.equals("")){
               untranslatedOnes.add(sChunk);
            } else {
               results.add(tr);
            }
        }
          
        dealWithChunks(allChunks, results, untranslatedOnes);
          
    }
    
    public void fillTranslationResults(List<TranslationResult> translations) {
        
        List<TranslationResult> translated = new ArrayList<TranslationResult>();
        
        for (TranslationResult tr:translations) {
            TimedChunk sChunk = tr.getSourceChunk();
            synchronizer.putTranslationResult(tr);
            synchronizer.putSourceChunk(sChunk, -1, false);
            String tChunk = tr.getUserTranslation();
             
            ChunkIndex chunkIndex = sChunk.getChunkIndex();
            this.getCurrentDocument().translationResults.put(chunkIndex, tr);

            if (tChunk!=null && !tChunk.equals("")){
                translated.add(tr);
            }
        }
        
        Scheduler.get().scheduleIncremental(new ShowUserTranslatedCommand(translated));
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

          // choose the appropriate parser:
          Parser subtextparser;
          if (subformat == "sub") {
               subtextparser = Parser.PARSER_SUB;
          }
          else {
               assert subformat == "srt" : "One of the subtitle formats must be chosen.";
               subtextparser = Parser.PARSER_SRT;
          }
          Gui.log("subtitle format chosen: " + subformat);

          // parse:
          Gui.log("starting parsing");
          long startTime = System.currentTimeMillis();
          List<TimedChunk> chunklist = null;
          try {
              chunklist = subtextparser.parse(subtext, this.getCurrentDocument().getId(), Language.EN);
          }
          catch (Exception e) {
        	  // user interaction
        	  createDocumentCall.hideMediaSelector();
        	  Window.alert("There was an error parsing the subtitle file:\n" + e.getMessage());
        	  // logging
        	  Gui.log("There was an error parsing the subtitle file!");
              //Gui.exceptionCatcher(e, false);
        	  // action
        	  Gui.getPageHandler().loadPage(Page.DocumentCreator, true);
              new DeleteDocumentSilently(getCurrentDocument().getId());
        	  // return prematurely
        	  return;
		  }
          long endTime = System.currentTimeMillis();
          long parsingTime = endTime - startTime;
          Gui.log("parsing finished in " + parsingTime + "ms");


          for (TimedChunk chunk : chunklist) {
              ChunkIndex chunkIndex = chunk.getChunkIndex();
              TranslationResult tr = new TranslationResult(chunk);
              this.getCurrentDocument().translationResults.put(chunkIndex, tr);
              synchronizer.putTranslationResult(tr);
              synchronizer.putSourceChunk(tr, -1, false);
          }
          
          // save the chunks
          new SaveSourceChunks(chunklist, this, createDocumentCall);
          // now the user can close the browser, chunks are safely saved
     }

     private SendChunksCommand sendChunksCommand;
     
     private boolean translationStarted=false;
     /**
      * Creates the SendChunksCommand and, if possible, executes it
      * @param chunklist
      */
     private void prepareSendChunkCommand(List<TimedChunk> chunklist) {
           
           
              sendChunksCommand = new SendChunksCommand(chunklist);
          
     }

     /**
      * Called when the user selects the media source.
      */
     public void setSourceSelectedTrue() {
         this.sourceSelected = true;
     }
     
     /**
      * Starts requesting trabslation suggestions if not waiting for anything.
      */
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
        	  new GetTranslationResults(timedchunks, SendChunksCommand.this, TranslationWorkspace.this);
          }
     }


     /**
      * Send the given translation result as a "user-feedback" to the userspace
      * @param transresult
     * @param toSaveAndUnlock
     * @param toLock
      */
     public void submitUserTranslation(SubgestBox toSaveAndUnlock, SubgestBox toLock) {
         TranslationResult transResult = toSaveAndUnlock.getTranslationResult();
          String combinedTRId = transResult.getDocumentId() + ":" + transResult.getSourceChunk().getChunkIndex();
          Gui.log("sending user feedback with values: " + combinedTRId + ", " + transResult.getUserTranslation() + ", " + transResult.getSelectedTranslationPairID());

          ChunkIndex chunkIndex = transResult.getSourceChunk().getChunkIndex();
          
          if (toLock != null) {          
            new SetUserTranslation(chunkIndex, transResult.getDocumentId(),
                                              transResult.getUserTranslation(), transResult.getSelectedTranslationPairID(), lockedSubgestBox, this, toLock);
          } else {
              new SetUserTranslation(chunkIndex, transResult.getDocumentId(),
                                              transResult.getUserTranslation(), transResult.getSelectedTranslationPairID(), lockedSubgestBox, this);
          }
          
          synchronizer.putTranslationResult(transResult);
          //reverseTimeMap.put((double)(transresult.getSourceChunk().getStartTimeLong()), transresult);
     }

     ///////////////////////////////////////
     //                                   //
     //      Display methods              //
     //                                   //
     ///////////////////////////////////////

     private class ShowUserTranslatedCommand implements RepeatingCommand {
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

     private class ShowOriginalCommand implements RepeatingCommand {
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
    

     private void dealWithChunks(List<TimedChunk> original, List<TranslationResult> translated, List<TimedChunk> untranslated) {
         
              videoPlayerFixedPanel = new HTMLPanel("");
              videoFixedWrapper = new HTMLPanel("");

              videoPlayer = VideoWidget.initVideoWidget(videoPlayerFixedPanel, table, videoFixedWrapper, synchronizer, this);
          
          
          Scheduler.get().scheduleIncremental(new ShowOriginalCommand(original));
          Scheduler.get().scheduleIncremental(new ShowUserTranslatedCommand(translated));
          prepareSendChunkCommand(untranslated) ; 
          startShowingTranslationsIfReady() ; 
     }
     
     /**
      * Shows the source chunks.
      * @param chunks
      */
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
        timeslabel.setTitle("double-click to change the timing");
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
        sourcelabel.setTitle("double-click to change this text");
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
            sourcemarks.setHTML(sourcemarks.getHTML() + " &ndash; ");
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
    private class TimeChangeHandler implements DoubleClickHandler {

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
    private class SourceChangeHandler implements DoubleClickHandler {

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
    
    /**
     * Shows the real subgestbox instead of the fake one.
     */
    public void replaceFake(TimedChunk chunk, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        int id = synchronizer.getIndexOf(chunk);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);
        
        real.setFocus(true);
    }

//    public TranslationResult getTranslationResultForIndex(int id) {
//        SubgestBox sb = targetBoxes.get(id).getFather();
//        TranslationResult tr = sb.getTranslationResult();
//        return tr;
//    }

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

    /**
     * Scrolls the page so that the subgestbox isvisible to the user.
     */
    public void ensureVisible(SubgestBox subbox) {
        Window.scrollTo(
                Window.getScrollLeft(),
                getScrollOffsetY(subbox.getElement())
                        - getVideoHeight()
                        - (Window.getClientHeight() - getVideoHeight()) * 2 / 5
        );
    }

    private int getVideoHeight() {
        return ( isVideo ? videoPlayerFixedPanel.getOffsetHeight() : 0 );
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

    /**
     * Sets the subgestbox that is currently active.
     */
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
    
    public native void alert(String message)/*-{
        $wnd.alert(message);
            
    }-*/;
    
    private SubgestBox lockedSubgestBox;
    private SubgestBox prevLockedSubgestBox;
    
    private com.google.gwt.user.client.Timer timer;



}
