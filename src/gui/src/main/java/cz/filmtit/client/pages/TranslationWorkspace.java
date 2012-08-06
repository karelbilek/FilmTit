package cz.filmtit.client.pages;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Random;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

import cz.filmtit.client.Gui;
import cz.filmtit.client.subgestbox.SubgestBox;
import cz.filmtit.client.subgestbox.SubgestHandler;
import cz.filmtit.client.subgestbox.SubgestBox.FakeSubgestBox;
import cz.filmtit.client.widgets.*;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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

    /**
     * Identifies this instance of workspace.
     * Used because translation results must be bound to a workspace instance
     * and invalidated if that instance is closed.
     */
    public final int id;
    
    /**
     * Indicates that the user moved away from this workspace
     * and that the loading of TranslationResults should be stopped
     */
    private boolean stopLoading = false;
    
	public void setStopLoading(boolean stopLoading) {
		this.stopLoading = stopLoading;
		gui.log("stopLoading set for the workspace");
	}

	public boolean getStopLoading() {
		return stopLoading;
	}

	private Gui gui = Gui.getGui();
	
	public enum DocumentOrigin { NEW, FROM_DB }
	
	private DocumentOrigin documentOrigin;
	
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
         gui.pageHandler.setDocumentId(currentDocument.getId());
    }
    
    public Map<ChunkIndex, TimedChunk> chunkmap;

    public TimedChunk getChunk(ChunkIndex chunkIndex) {
       return chunkmap.get(chunkIndex);
    }

    ///////////////////////////////////////
    //                                   //
    //      Display fields               //
    //                                   //
    ///////////////////////////////////////

    public SubgestHandler subgestHandler;

    private HashMap<ChunkIndex, Integer> indexes;
    private List<SubgestBox.FakeSubgestBox> targetBoxes;
    private Widget activeSuggestionWidget = null;

    // column numbers in the subtitle-table
    private static final int TIMES_COLNUMBER      = 0;
    private static final int SOURCETEXT_COLNUMBER = 1;
    private static final int TARGETBOX_COLNUMBER  = 2;

    private boolean isVideo=false;

    VLCWidget vlcPlayer;

    // UI binder fields
    @UiField
    ScrollPanel scrollPanel;
    @UiField
    SimplePanel emptyPanel;
	@UiField
    FlexTable table;
    @UiField
    HorizontalPanel hPanel;
    @UiField
    HorizontalPanel translationHPanel;
   
    ///////////////////////////////////////
    //                                   //
    //      Initialization               //
    //                                   //
    ///////////////////////////////////////

    public TranslationWorkspace(Document doc, String path, DocumentOrigin documentOrigin) {
        initWidget(uiBinder.createAndBindUi(this));

        // 0 <= id < Integer.MAX_VALUE
        id = Random.nextInt(Integer.MAX_VALUE);
        gui.currentWorkspace = this;
        
        isVideo = path!=null;
        
        setCurrentDocument(doc);
        
        this.documentOrigin = documentOrigin;
        
        // initialize readyToSendChunksSemaphore
        switch (documentOrigin) {
		case NEW:
			// wait for everything to load and for selectSource to return
			readyToSendChunksSemaphore = 2;
			break;
		case FROM_DB:
			// only wait for everything to load
			readyToSendChunksSemaphore = 1;
		default:
			assert false;
			break;
		}

        this.targetBoxes = new ArrayList<SubgestBox.FakeSubgestBox>();
        this.indexes = new HashMap<ChunkIndex, Integer>();

        scrollPanel.setStyleName("scrollPanel");
        // hiding the suggestion popup when scrolling the subtitle panel
        gui.guiStructure.contentPanel.addScrollHandler( new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                deactivateSuggestionWidget();
            }
        } );

        table.setWidth("100%");
        if (!isVideo) {
            table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "164px");
            table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "410px");
            table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER,  "400px");
            this.subgestHandler = new SubgestHandler(this, null);
            translationHPanel.setCellWidth(scrollPanel, "100%");
            translationHPanel.setCellWidth(emptyPanel, "0%");            
         } else {
            table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "99px");
            table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "246px");
            table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER, "240px");
            vlcPlayer = new VLCWidget(path, 400, 225);
            this.subgestHandler = new SubgestHandler(this, vlcPlayer);
            hPanel.add(vlcPlayer);
            translationHPanel.setCellWidth(scrollPanel, "60%");
            translationHPanel.setCellWidth(emptyPanel, "40%");            
       }
        
        
        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.getRowFormatter().setStyleName(0, "header");
         
        gui.guiStructure.contentPanel.setWidget(this);
        gui.guiStructure.contentPanel.setStyleName("translating");
        gui.guiStructure.contentPanel.addStyleName("parsing");

        
	}
    
    ///////////////////////////////////////
    //                                   //
    //      Un-initialization            //
    //                                   //
    ///////////////////////////////////////

    @Override
    public void onUnload() {
        setStopLoading(true);
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

          List<TimedChunk> untranslatedOnes = new LinkedList<TimedChunk>();
          List<TimedChunk> allChunks = new LinkedList<TimedChunk>();
          List<TranslationResult> results = new LinkedList<TranslationResult>();
        
          for (TranslationResult tr:translations) {
              TimedChunk sChunk = tr.getSourceChunk();
              chunkmap.put(sChunk.getChunkIndex(), sChunk);
              String tChunk = tr.getUserTranslation();
              
              // log("processing TrResult for " + sChunk);

              ChunkIndex chunkIndex = sChunk.getChunkIndex();

              this.currentDocument.translationResults.put(chunkIndex, tr);

              allChunks.add(sChunk);

//              showSource(sChunk);

              if (tChunk==null || tChunk.equals("")){
                  // log(sChunk + " has not yet been translated");
                 untranslatedOnes.add(sChunk);
              } else {
                  // log(sChunk + " has already been translated");
                 results.add(tr);
              }
          }

          Scheduler.get().scheduleIncremental(new FakeSubgestIncrementalCommand(allChunks, untranslatedOnes, results));
          
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
    	 gui.log("processing the following input:\n" + subtext + "\n");

          chunkmap = new HashMap<ChunkIndex, TimedChunk>();

          // determine format (from corresponding radiobuttons) and choose parser:
          Parser subtextparser;
          if (subformat == "sub") {  // i.e. ".sub" is checked
               subtextparser = new ParserSub();
          }
          else {  // i.e. ".srt" is checked
               assert subformat == "srt" : "One of the subtitle formats must be chosen.";
               subtextparser = new ParserSrt();
          }
          gui.log("subtitle format chosen: " + subformat);

          // parse:
          gui.log("starting parsing");
          //Window.alert("starting parsing");
          long startTime = System.currentTimeMillis();
          List<TimedChunk> chunklist = subtextparser.parse(subtext, this.currentDocument.getId(), Language.EN);
          long endTime = System.currentTimeMillis();
          long parsingTime = endTime - startTime;
          gui.log("parsing finished in " + parsingTime + "ms");

          
          //Window.alert("finished parsing");

          for (TimedChunk chunk : chunklist) {
              chunkmap.put(chunk.getChunkIndex(), chunk);
              ChunkIndex chunkIndex = chunk.getChunkIndex();
              TranslationResult tr = new TranslationResult(chunk);
              this.currentDocument.translationResults.put(chunkIndex, tr);

          }
          
          //Window.alert("finished hashmap");
          

          // output the parsed chunks:
          gui.log("parsed chunks: "+chunklist.size());
          
          // save the chunks
          gui.rpcHandler.saveSourceChunks(chunklist, this);
          
          gui.log("called saveSourceChunks()");
          
          // now the user can close the browser, chunks are safely saved
     }

     SendChunksCommand sendChunksCommand;
     
     /**
      * Creates the SendChunksCommand and, if possible, executes it
      * @param chunklist
      */
     void startShowingTranslations(List<TimedChunk> chunklist) {
          
          sendChunksCommand = new SendChunksCommand(chunklist);
          startShowingTranslationsIfReady();
          
     }
     
     /**
      * Determines whether chunks can be sent for translation
      * similarly to a Semaphore.
      * Starts as a positive number,
      * gets decremented from startShowingTranslations()
      * and from SelectSource.onSuccessAfterLog()
      * and when the value is 0, the chunks can be sent.
      */
     private byte readyToSendChunksSemaphore;
     
     /**
      * Sends chunks for translation if everything is ready.
      * Is called once from startShowingTranslations()
      * (when the document is parsed and the source chunks and user translations are displayed)
      * and once from SelectSource.onSuccessAfterLog()
      * (when the MediaSource has been set).
      */
     synchronized public void startShowingTranslationsIfReady() {
    	 // "synchronized" is here only for clarity, GWT ignores that as JS is single-threaded.

    	 readyToSendChunksSemaphore--;
    	 if (readyToSendChunksSemaphore <= 0) {
             sendChunksCommand.execute();    		 
    	 }
    }
    
     

     /**
      * Requests TranslationResults for the chunks,
      * sending them in groups to compromise between responsiveness and effectiveness.
      */
     public class SendChunksCommand {

          LinkedList<TimedChunk> chunks;

          public SendChunksCommand(List<TimedChunk> chunks) {
               //Window.alert("wtf");
               this.chunks = new LinkedList<TimedChunk>(chunks);
               //Window.alert("Chunks je velky "+chunks.size());
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

        public boolean execute() {
               if (stopLoading) {
                   return false;
               }

               if (chunks.isEmpty()) {
                    //Window.alert("chunks is empty");
                    return false;
               } else {
                    List<TimedChunk> sentTimedchunks = new ArrayList<TimedChunk>(exponential);
                    for (int i = 0; i < exponential; i++) {
                        if (!chunks.isEmpty()){
                            TimedChunk timedchunk = chunks.removeFirst();
                            sentTimedchunks.add(timedchunk);
                        }
                    }
                    //Window.alert("chunk neni empty, posilam "+sentTimedchunks.size());
                    sendChunks(sentTimedchunks);
                    exponential = exponential*2;
                    return true;
               }
          }

          private void sendChunks(List<TimedChunk> timedchunks) {
              //Window.alert("HAHA spoustim sendChunksCommand");
        	  gui.rpcHandler.getTranslationResults(timedchunks, SendChunksCommand.this, TranslationWorkspace.this);
          }
     }


     /**
      * Send the given translation result as a "user-feedback" to the userspace
      * @param transresult
      */
     public void submitUserTranslation(TranslationResult transresult) {
    	  // assert transresult.getDocumentId() == currentDocument.getId();
          String combinedTRId = transresult.getDocumentId() + ":" + transresult.getSourceChunk().getChunkIndex();
          gui.log("sending user feedback with values: " + combinedTRId + ", " + transresult.getUserTranslation() + ", " + transresult.getSelectedTranslationPairID());

          ChunkIndex chunkIndex = transresult.getSourceChunk().getChunkIndex();
          gui.rpcHandler.setUserTranslation(chunkIndex, transresult.getDocumentId(),
                                          transresult.getUserTranslation(), transresult.getSelectedTranslationPairID());
     }

     ///////////////////////////////////////
     //                                   //
     //      Display methods              //
     //                                   //
     ///////////////////////////////////////

     class FakeSubgestIncrementalCommand implements RepeatingCommand {
         LinkedList<TimedChunk> chunksToDisplay = new LinkedList<TimedChunk>();
         List<TimedChunk> chunksToTranslate = new LinkedList<TimedChunk>();
         LinkedList<TranslationResult> resultsToDisplay = new LinkedList<TranslationResult>();
        
         /**
          * for a new document
          * (all chunks are sent to be translated, none of the chunks has a translation yet)
          * @param chunks all chunks
          */
         public FakeSubgestIncrementalCommand(List<TimedChunk> chunks) {
              this.chunksToDisplay.addAll(chunks);
              this.chunksToTranslate.addAll(chunks);
         }

         /**
          * for an existing document
          * @param chunksToDisplay <b>all</b> chunks (all chunks will be displayed first)
          * @param chunksToTranslate chunks <b>without userTranslation</b> (TM will be queried for translation suggestions for these)
          * @param resultsToDisplay TranslationResults containing chunks <b>with userTranslation</b> (no translation suggestions will be displayed for these)
          */
         public FakeSubgestIncrementalCommand(List<TimedChunk> chunksToDisplay, List<TimedChunk> chunksToTranslate, List<TranslationResult> resultsToDisplay) {
              this.chunksToDisplay.addAll(chunksToDisplay);
              this.chunksToTranslate.addAll(chunksToTranslate);
              this.resultsToDisplay.addAll(resultsToDisplay);
          }

         @Override
         public boolean execute() {
             if (stopLoading) {
                return false;
             }

        	 // First, process chunksToDisplay
             if (!chunksToDisplay.isEmpty()) {
                 TimedChunk timedchunk = chunksToDisplay.removeFirst();
                 showSource(timedchunk);
                 return true;
             }
             // Then, process resultsToDisplay
             else if (!resultsToDisplay.isEmpty()) {
                 TranslationResult result = resultsToDisplay.removeFirst();
                 showResult(result);
                 return true;
             }
             // Finally, process chunksToTranslate
             else {
                 gui.guiStructure.contentPanel.removeStyleName("parsing");
                 startShowingTranslations(chunksToTranslate) ;
                 return false;
             }
         }
     }
     
     public void showSources(List<TimedChunk> chunks) {
        Scheduler.get().scheduleIncremental(new FakeSubgestIncrementalCommand(chunks));
     }

     /**
     * Display the whole row for the given (source-language) chunk in the table, i.e. the timing,
     * the chunk text and an empty (fake)subgestbox. 
     *
     * We have to suppose these are coming in the same order as they appear in the source.
     * @param chunk - source-language chunk to show
     * @param index - index of the chunk in the chunk-list
     */
    public void showSource(TimedChunk chunk) {
        
    	ChunkIndex chunkIndex = chunk.getChunkIndex();

        Label timeslabel = new Label(chunk.getStartTime() + " - " + chunk.getEndTime());
        timeslabel.setStyleName("chunk_timing");

        int index = lastIndex;
        lastIndex++;

        indexes.put(chunkIndex, index);

                        //+1 because of the header
        table.setWidget(index + 1, TIMES_COLNUMBER, timeslabel);

        
        //html because of <br />
        Label sourcelabel = new HTML(chunk.getGUIForm());
        sourcelabel.setStyleName("chunk_l1");
        table.setWidget(index + 1, SOURCETEXT_COLNUMBER, sourcelabel);

        SubgestBox targetbox = new SubgestBox(chunk, this, !isVideo, index+1);
        SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox(index+1);
        targetBoxes.add(fake);
        table.setWidget(index + 1, TARGETBOX_COLNUMBER, fake);
        
    }


    public void replaceFake(ChunkIndex chunkIndex, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        int id = indexes.get(chunkIndex);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);

        real.setFocus(true);
    }


    /**
     * Add the given TranslationResult to the current listing interface.
     * @param transresult - the TranslationResult to be shown
     */
    public void showResult(TranslationResult transresult) {
    	

        ChunkIndex chunkIndex = transresult.getSourceChunk().getChunkIndex();
        int index = indexes.get(chunkIndex);

        targetBoxes.get(index).getFather().setTranslationResult(transresult);
        targetBoxes.get(index).removeStyleName("loading");

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
        Window.scrollTo(Window.getScrollLeft(), getScrollOffsetY(subbox.getElement()) - Window.getClientHeight() / 2);
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
