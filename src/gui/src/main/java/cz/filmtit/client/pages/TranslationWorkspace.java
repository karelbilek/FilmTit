package cz.filmtit.client.pages;

import com.google.gwt.user.client.*;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

import cz.filmtit.client.FilmTitServiceHandler;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.client.callables.GetTranslationResults;
import cz.filmtit.client.callables.SetChunkTimes;
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
    
    public Map<ChunkIndex, TimedChunk> chunkmap;
    
    //this is for quick time lookups for the subtitle displaying
    //"logically" it should be double, but GWT is actually javascript
    //and works faster with Doubles instead of Longs,
    //which it emulates by strings or something
    public TreeMap<Double, TranslationResult> reverseTimeMap;

    public TimedChunk getChunk(ChunkIndex chunkIndex) {
       return chunkmap.get(chunkIndex);
    }

    public Collection<TranslationResult> getChunkIndexesFrom(double start, double end) {
        Gui.log("Chci from "+start+" to "+end+ " A je jich "+reverseTimeMap.subMap(start, end).values().size()+" !");
        if (reverseTimeMap== null) {
            return new ArrayList<TranslationResult>();
        }
        return reverseTimeMap.subMap(start, end).values();
    }

    ///////////////////////////////////////
    //                                   //
    //      Display fields               //
    //                                   //
    ///////////////////////////////////////

    public SubgestHandler subgestHandler;

    private Map<ChunkIndex, Integer> indexes;
    private List<SubgestBox.FakeSubgestBox> targetBoxes;
    private Widget activeSuggestionWidget = null;

    // column numbers in the subtitle-table
    private static final int TIMES_COLNUMBER      = 0;
    private static final int SOURCETEXT_COLNUMBER = 1;
    private static final int TARGETBOX_COLNUMBER  = 2;

    private boolean isVideo=false;

    VLCWidget vlcPlayer;
    
    HTMLPanel playerFixedPanel = null;

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

    ///////////////////////////////////////
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
        this.indexes = new HashMap<ChunkIndex, Integer>();

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
        table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "410px");
        table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "400px");
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
            
            HTMLPanel fixedWrapper = new HTMLPanel("");
            fixedWrapper.setWidth("984 px");


            HTML leftLabel = new HTML("");
            leftLabel.addStyleName("subtitleDisplayedLeft");
            fixedWrapper.addStyleName("fixedPlayerWrapper");
            fixedWrapper.add(leftLabel);


            HTML rightLabel = new HTML("");
            rightLabel.addStyleName("subtitleDisplayedRight");
   
            vlcPlayer = new VLCWidget(path, 400, 225, leftLabel, rightLabel, this);
            vlcPlayer.addStyleName("vlcPlayerDisplayed"); 
            this.subgestHandler = new SubgestHandler(this, vlcPlayer);
            fixedWrapper.add(vlcPlayer);

            fixedWrapper.add(rightLabel);
           
            HTMLPanel playerStatusPanel = new HTMLPanel("");
            playerStatusPanel.add(new InlineLabel("[status, pause, replay will be here] "));
            
            fixedWrapper.add(playerStatusPanel);
            playerStatusPanel.addStyleName("statusPanel");
            
            playerFixedPanel.add(fixedWrapper);

        } else {
            this.subgestHandler = new SubgestHandler(this, null);
        }
        
        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.getRowFormatter().setStyleName(0, "header");
         
        Gui.getGuiStructure().contentPanel.setWidget(this);
        Gui.getGuiStructure().contentPanel.setStyleName("translating");
        Gui.getGuiStructure().contentPanel.addStyleName("parsing");
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
    	
          chunkmap = new HashMap<ChunkIndex, TimedChunk>();
          reverseTimeMap = new TreeMap<Double, TranslationResult>();

          List<TimedChunk> untranslatedOnes = new LinkedList<TimedChunk>();
          List<TimedChunk> allChunks = new LinkedList<TimedChunk>();
          List<TranslationResult> results = new LinkedList<TranslationResult>();
        
          for (TranslationResult tr:translations) {
              TimedChunk sChunk = tr.getSourceChunk();
              chunkmap.put(sChunk.getChunkIndex(), sChunk);
              reverseTimeMap.put((double)(sChunk.getStartTimeLong()), tr);

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
     * @param subtext - multiline text (of the whole subtitle file, typically) to parse
      */
     public void processText(String subtext, String subformat) {
          // dump the input text into the debug-area:
    	  // Gui.log("processing the following input:\n" + subtext + "\n");

          chunkmap = new HashMap<ChunkIndex, TimedChunk>();
          reverseTimeMap = new TreeMap<Double, TranslationResult>();

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
          List<TimedChunk> chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
          long endTime = System.currentTimeMillis();
          long parsingTime = endTime - startTime;
          Gui.log("parsing finished in " + parsingTime + "ms");

          

          for (TimedChunk chunk : chunklist) {
              chunkmap.put(chunk.getChunkIndex(), chunk);
              ChunkIndex chunkIndex = chunk.getChunkIndex();
              TranslationResult tr = new TranslationResult(chunk);
              this.currentDocument.translationResults.put(chunkIndex, tr);
              reverseTimeMap.put((double)(chunk.getStartTimeLong()), tr);

          }
          
          

          // output the parsed chunks:
          Gui.log("parsed chunks: "+chunklist.size());
          
          // save the chunks
          FilmTitServiceHandler.saveSourceChunks(chunklist, this);
          
          Gui.log("called saveSourceChunks()");
          
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
          
          reverseTimeMap.put((double)(transresult.getSourceChunk().getStartTimeLong()), transresult);
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

     private Map<Integer, List<Label>> timeslabels = new HashMap<Integer, List<Label>>();
     
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
        Label timeslabel = new Label(chunk.getStartTime() + " - " + chunk.getEndTime());
        timeslabel.setStyleName("chunk_timing");
		timeslabel.addDoubleClickHandler(new TimeChangeHandler(chunk));
		// add label to map
		List<Label> timeslabelsWithThisTime = timeslabels.get(chunkIndex.getId());
		if (timeslabelsWithThisTime == null) {
			timeslabelsWithThisTime = new LinkedList<Label>();
			timeslabels.put(chunkIndex.getId(), timeslabelsWithThisTime);
		}
		timeslabelsWithThisTime.add(timeslabel);
		
        int index = lastIndex;
        lastIndex++;

        indexes.put(chunkIndex, index);

                        //+1 because of the header
        table.setWidget(index + 1, TIMES_COLNUMBER, timeslabel);

        
        //html because of <br />
        Label sourcelabel = new HTML(chunk.getGUIForm());
        sourcelabel.setStyleName("chunk_l1");
        table.setWidget(index + 1, SOURCETEXT_COLNUMBER, sourcelabel);

        SubgestBox targetbox = new SubgestBox(chunk, this, index+1);
        SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox(index+1);
        targetBoxes.add(fake);
        table.setWidget(index + 1, TARGETBOX_COLNUMBER, fake);
        
    }

    /**
     * Used to change the time of a chunk.
     * Also changes of all chunks with the same id
     * (i.e. which are parts of the same chunk actually).
     * Very rough and very TODO now.
     */
    class TimeChangeHandler implements DoubleClickHandler {

    	private TimedChunk chunk;
    	
		private TimeChangeHandler(TimedChunk chunk) {
			this.chunk = chunk;
		}

		@Override
		public void onDoubleClick(DoubleClickEvent event) {
			// TODO something nicer
			// TODO: check values
			// ask user for new values, showing the old ones
			String newStartTime = Window.prompt(
					"Start time of chunk " + chunk.getSurfaceForm(),
					chunk.getStartTime());
			String newEndTime = Window.prompt(
					"End time of chunk " + chunk.getSurfaceForm(),
					chunk.getEndTime());
			// handle cancels
			if (newStartTime == null) {
				newStartTime = chunk.getStartTime();
			}
			if (newEndTime == null) {
				newEndTime = chunk.getEndTime();
			}
			Gui.log("change times " + chunk + ": " + newStartTime + " - " + newEndTime);
			// change values
			if (!newStartTime.equals(chunk.getStartTime()) || !newEndTime.equals(chunk.getEndTime())) {
				// change chunks
				int id = chunk.getId();
				int partNumber = 1;
				ChunkIndex chunkIndex = new ChunkIndex(partNumber, id);
				while (chunkmap.containsKey(chunkIndex)) {
					// change chunk
					TimedChunk cochunk = chunkmap.get(chunkIndex);
					cochunk.setStartTime(newStartTime);
					cochunk.setEndTime(newEndTime);
					// RPC call
					new SetChunkTimes(cochunk);
					// move on
					partNumber++;
					chunkIndex = new ChunkIndex(partNumber, id);
				}
				// change labels
				String newValue = chunk.getStartTime() + " - " + chunk.getEndTime();
				List<Label> timeslabelsWithThisTime = timeslabels.get(id);
				assert timeslabelsWithThisTime != null : "Each chunk must be there.";
				for (Label label : timeslabelsWithThisTime) {
					label.setText(newValue);
				}
			}
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
			TimedChunk chunk = chunkmap.get(chunkIndex);
			String oldSource = chunk.getSurfaceForm();
			
			// ask user for new values, showing the old ones
			String newSource = Window.prompt("Source text for this chunk:", oldSource);
			
			if (newSource == null || newSource.equals(oldSource)) {
				// cancel or no change
				return;
			}
			else {
				// change the values
				Gui.log("change source " + chunk + ": " + newSource);
				chunk.setSurfaceForm(newSource);
				label.setText(chunk.getGUIForm());
				// TODO store
				// TODO new suggestions
			}
		}
    }
    
    public void replaceFake(ChunkIndex chunkIndex, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        int id = indexes.get(chunkIndex);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);
        
        real.setFocus(true);
        real.updateVerticalSize();
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
            
        ChunkIndex chunkIndex = transresult.getSourceChunk().getChunkIndex();
        
        if (!indexes.containsKey(chunkIndex)) {
            //try it again after some time
             new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    showResult(transresult); 
                } 
            }.schedule(400); 
        } else {
            
            //index is there -> insert result
            int index = indexes.get(chunkIndex);

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
        
        if (!indexes.containsKey(chunkIndex)) {
            //try it again after some time
             new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    noResult(chunkIndex); 
                } 
            }.schedule(400); 
        } else {
            //index is there -> insert result
            int index = indexes.get(chunkIndex);
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
        int currentIndex = indexes.get(currentBox.getChunkIndex());
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
        int currentIndex = indexes.get(currentBox.getChunkIndex());
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
        //scrollPanel.ensureVisible(subbox);
        //Window.alert("ensuring visible");
        //ensureVisibleInWindow(subbox.getElement());
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
