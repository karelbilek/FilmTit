package cz.filmtit.client;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import cz.filmtit.client.widgets.*;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

import java.util.ArrayList;
import java.util.List;


public class TranslationWorkspace extends Composite {

	private static TranslationWorkspaceUiBinder uiBinder = GWT.create(TranslationWorkspaceUiBinder.class);

	interface TranslationWorkspaceUiBinder extends UiBinder<Widget, TranslationWorkspace> {
	}

    Gui gui;

    protected SubgestHandler subgestHandler;

    private List<SubgestBox.FakeSubgestBox> targetBoxes;
    private Widget activeSuggestionWidget = null;

    // column numbers in the subtitle-table
    private static final int TIMES_COLNUMBER      = 0;
    private static final int SOURCETEXT_COLNUMBER = 1;
    private static final int TARGETBOX_COLNUMBER  = 2;

    private boolean isVideo=false;


    public TranslationWorkspace(Gui gui, String path) {
		initWidget(uiBinder.createAndBindUi(this));

        isVideo = path!=null;

        this.gui = gui;

        this.targetBoxes = new ArrayList<SubgestBox.FakeSubgestBox>();

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
            this.subgestHandler = new SubgestHandler(this.gui, null);
            translationHPanel.setCellWidth(scrollPanel, "100%");
            translationHPanel.setCellWidth(emptyPanel, "0%");            
         } else {
            table.getColumnFormatter().setWidth(TIMES_COLNUMBER,      "99px");
            table.getColumnFormatter().setWidth(SOURCETEXT_COLNUMBER, "246px");
            table.getColumnFormatter().setWidth(TARGETBOX_COLNUMBER, "240px");
            vlcPlayer = new VLCWidget(path, 400, 225);
            this.subgestHandler = new SubgestHandler(this.gui, vlcPlayer);
            hPanel.add(vlcPlayer);
            translationHPanel.setCellWidth(scrollPanel, "60%");
            translationHPanel.setCellWidth(emptyPanel, "40%");            
       }
        
        
        table.setWidget(0, TIMES_COLNUMBER,      new Label("Timing"));
        table.setWidget(0, SOURCETEXT_COLNUMBER, new Label("Original"));
        table.setWidget(0, TARGETBOX_COLNUMBER,  new Label("Translation"));
        table.getRowFormatter().setStyleName(0, "header");
         
        
	}

    
    VLCWidget vlcPlayer;

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
   
    
    /**
     * Display the whole row for the given (source-language) chunk in the table, i.e. the timing,
     * the chunk text and an empty (fake)subgestbox.
     * @param chunk - source-language chunk to show
     * @param index - index of the chunk in the chunk-list
     */
    public void showSource(TimedChunk chunk, int index) {
        Label timeslabel = new Label(chunk.getStartTime() + " - " + chunk.getEndTime());
        timeslabel.setStyleName("chunk_timing");
        table.setWidget(index + 1, TIMES_COLNUMBER, timeslabel);

        //html because of <br />
        Label sourcelabel = new HTML(chunk.getGUIForm());
        sourcelabel.setStyleName("chunk_l1");
        table.setWidget(index + 1, SOURCETEXT_COLNUMBER, sourcelabel);

        SubgestBox targetbox = new SubgestBox(index, gui, !isVideo);
        SubgestBox.FakeSubgestBox fake = targetbox.new FakeSubgestBox();
        targetBoxes.add(fake);
        table.setWidget(index + 1, TARGETBOX_COLNUMBER, fake);
    }


    public void replaceFake(int id, SubgestBox.FakeSubgestBox fake, SubgestBox real) {
        table.remove(fake);
        table.setWidget(id+1, TARGETBOX_COLNUMBER, real);

        real.setFocus(true);
    }


    /**
     * Add the given TranslationResult to the current listing interface.
     * @param transresult - the TranslationResult to be shown
     */
    public void showResult(TranslationResult transresult, int index) {
        targetBoxes.get(index).getFather().setTranslationResult(transresult);
        targetBoxes.get(index).removeStyleName("loading");

        gui.counter++;
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
        int currentIndex = currentBox.getId();
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

        //window.alert("ensuring visible");

        var item = e;
        var realOffset = 0;
        while (item && (item != document.body)) {
            realOffset += item.offsetTop;
            item = item.offsetParent;
        }

        //return (realOffset - document.body.offsetHeight / 2);
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
