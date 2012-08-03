package cz.filmtit.client;

import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Random;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
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
    
	private Gui gui = Gui.getGui();
	
    ///////////////////////////////////////
    //                                   //
    //      Current document             //
    //                                   //
    ///////////////////////////////////////
    
    protected Document currentDocument;

    public Document getCurrentDocument() {
         return currentDocument;
    }

    protected void setCurrentDocument(Document currentDocument) {
         this.currentDocument = currentDocument;
         gui.pageHandler.setDocumentId(currentDocument.getId());
    }
    
    protected Map<ChunkIndex, TimedChunk> chunkmap;

    public TimedChunk getChunk(ChunkIndex chunkIndex) {
       return chunkmap.get(chunkIndex);
    }

    ///////////////////////////////////////
    //                                   //
    //      Display fields               //
    //                                   //
    ///////////////////////////////////////

    protected SubgestHandler subgestHandler;

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

    public TranslationWorkspace(Document doc, String path) {
        initWidget(uiBinder.createAndBindUi(this));

        // 0 <= id < Integer.MAX_VALUE
        id = Random.nextInt(Integer.MAX_VALUE);
        gui.currentWorkspaceId = id;
        
        isVideo = path!=null;
        
        setCurrentDocument(doc);

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

    
   
    int lastIndex = 0;

    ///////////////////////////////////////
    //                                   //
    //      Action methods               //
    //                                   //
    ///////////////////////////////////////

    /**
     * Goes through TranslationResults of an already existing document
     * and loads translation results for the untranslated ones.
     */
    protected void processTranslationResultList(List<TranslationResult> translations) {

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
     protected void processText(String subtext, String subformat) {
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
              TranslationResult tr = new TranslationResult();
              tr.setSourceChunk(chunk);
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

     public void startShowingTranslations(List<TimedChunk> chunklist) {
          
          SendChunksCommand sendChunks = new SendChunksCommand(chunklist);
          sendChunks.execute();
     }

     /**
      * Requests TranslationResults for the chunks,
      * sending them in groups to compromise between responsiveness and effectiveness.
      */
     class SendChunksCommand {

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
              //Window.alert("HAHA spoustim sendChunks");
        	  gui.rpcHandler.getTranslationResults(timedchunks, SendChunksCommand.this, TranslationWorkspace.this);
          }
     }


     /**
      * Send the given translation result as a "user-feedback" to the userspace
      * @param transresult
      */
     public void submitUserTranslation(TranslationResult transresult) {
          String combinedTRId = transresult.getDocumentId() + ":" + transresult.getChunkId();
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
         LinkedList<TimedChunk> chunksToDisplay;
         List<TimedChunk> chunksToTranslate;
         LinkedList<TranslationResult> resultsToDisplay = new LinkedList<TranslationResult>();
        
         public FakeSubgestIncrementalCommand(List<TimedChunk> chunks) {
              this.chunksToDisplay = new LinkedList<TimedChunk>();
              this.chunksToDisplay.addAll(chunks);
              this.chunksToTranslate = chunks;
         }

         public FakeSubgestIncrementalCommand(List<TimedChunk> chunksToDisplay, List<TimedChunk> chunksToTranslate, List<TranslationResult> resultsToDisplay) {
              this.chunksToDisplay = new LinkedList<TimedChunk>();
              this.chunksToDisplay.addAll(chunksToDisplay);
              this.chunksToTranslate = chunksToTranslate;
              this.resultsToDisplay = new LinkedList<TranslationResult>();
              this.resultsToDisplay.addAll(resultsToDisplay);
          }

         @Override
         public boolean execute() {
                if (chunksToDisplay.isEmpty()) {
                    if (resultsToDisplay.isEmpty()) {
                        gui.guiStructure.contentPanel.removeStyleName("parsing");
                        startShowingTranslations(chunksToTranslate) ;
                        return false;
                    } else {
                        TranslationResult result = resultsToDisplay.removeFirst();
                        showResult(result);
                        return true;
                    }
               } else {
                    TimedChunk timedchunk = chunksToDisplay.removeFirst();
                    showSource(timedchunk);
                    return true;
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

        SubgestBox targetbox = new SubgestBox(chunkIndex, this, !isVideo, index+1);
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
                w.setVisible(false);
            }
            else {
                ((Panel)(w.getParent())).remove(w);
            }
            setActiveSuggestionWidget(null);
        }
    }



}
