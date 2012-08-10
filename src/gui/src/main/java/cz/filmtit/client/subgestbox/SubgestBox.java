package cz.filmtit.client.subgestbox;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.*;
import cz.filmtit.share.annotations.Annotation;
import cz.filmtit.share.annotations.AnnotationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Variant of a TextBox with pop-up suggestions
 * taken from the given TranslationResult.
 * 
 * @author Honza Václ
 *
 */
public class SubgestBox extends RichTextArea implements Comparable<SubgestBox> {
	private ChunkIndex chunkIndex;
	private TranslationResult translationResult;
	private Gui gui = Gui.getGui();
    private TranslationWorkspace workspace;
	private PopupPanel suggestPanel;
    private Widget suggestionWidget;
    private boolean loadedSuggestions = false;
    String lastText = "";

    void replaceFakeWithReal() {
        workspace.replaceFake(chunkIndex, substitute, this);
    }

    private FakeSubgestBox substitute=null;
    
    public class FakeSubgestBox extends TextArea implements Comparable<FakeSubgestBox> {
       
        public FakeSubgestBox(int tabIndex) {
            SubgestBox.this.substitute = SubgestBox.FakeSubgestBox.this;

            this.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    if (event.getSource() instanceof FakeSubgestBox) { // should be
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                            @Override
                            public void execute() {
                                replaceFakeWithReal();
                            }
                        });
                    }
                }
            });
//            this.setHeight(getFather().getCorrectVerticalSize()+"px");
            this.setTabIndex(tabIndex);
            this.setStyleName("pre_subgestbox");
            this.addStyleName("loading");
                this.addStyleName("subgest_fullwidth");
        }

        public /*static */boolean stringHasMoreThanTwoLines(String s) {
            int count =0;
            int first = s.indexOf("\n",0);
            if (first == -1) {
                return false;
            }
            int second = s.indexOf("\n", first);
            if (second == -1) {
                return false;
            }
            return true;
        }

        public int lastVerticalSize;

        public int verticalSize() {
            String text = this.getText();
            if (stringHasMoreThanTwoLines(text)) {
                return this.getElement().getScrollHeight();
            } else {
                return 30;
            }
        }

        public void updateVerticalSize() {
            //Window.alert("JDU UPDATE FAKE VERTICAL SIZE "+ verticalSize());
            this.setHeight(verticalSize() + "px");
            lastVerticalSize = verticalSize();
        }

        public SubgestBox getFather(){
            return SubgestBox.this;
        }

        @Override
	    public int compareTo(FakeSubgestBox that) {
	        return getFather().compareTo(that.getFather());
        }

    }


    /**
     * Color of the annotation highlighting.
     */
	private static final Map<AnnotationType, String> annotationColor = new HashMap<AnnotationType, String>();
	static {
		// setting the colors for annotations:
		annotationColor.put(AnnotationType.PLACE,        "#ffccff");
		annotationColor.put(AnnotationType.ORGANIZATION, "#ccffff");
		annotationColor.put(AnnotationType.PERSON,       "#ffff99");
	};


    private String subgestBoxHTML(String content) {
        content = content.replaceAll("\n", "<br>");
        return content;
    }

	public SubgestBox(TimedChunk chunk, TranslationWorkspace workspace, int tabIndex) {
		this.chunkIndex = chunk.getChunkIndex();
		this.translationResult = new TranslationResult(chunk);
        this.workspace = workspace;
        if (this.workspace == null) {
            gui.log("workspace for subgestbox is null!!!");
        }

        this.setHeight("36px");
        this.setHTML(subgestBoxHTML(""));

        this.addFocusHandler(this.workspace.subgestHandler);
		this.addKeyDownHandler(this.workspace.subgestHandler);
        this.addKeyUpHandler(this.workspace.subgestHandler);
		//this.addValueChangeHandler(this.workspace.subgestHandler);
		this.addBlurHandler(this.workspace.subgestHandler);

        this.setTabIndex(tabIndex);

        //delaying loadSuggestions() for focus
		//this.loadSuggestions();
        this.addStyleName("subgest_fullwidth");

        final RichTextArea richtext = this;
        richtext.addInitializeHandler(new InitializeHandler() {
            public void onInitialize(InitializeEvent ie) {
                IFrameElement fe = (IFrameElement) richtext.getElement().cast();
                Style s = fe.getContentDocument().getBody().getStyle();
                s.setProperty("fontFamily", "Arial Unicode MS,Arial,sans-serif");
                s.setProperty("fontSize", "small");
                s.setColor("#333");
            }
        });
        
    //    this.updateVerticalSize();

	}
	
    public void setTranslationResult(TranslationResult translationResult) {
        this.translationResult = translationResult;
        loadedSuggestions = false;
        String userTranslation = translationResult.getUserTranslation();

        if (userTranslation != null && !userTranslation.equals("")) {
            //replaceFakeWithReal();
            substitute.setText(userTranslation);
            substitute.updateVerticalSize();
            this.setHTML(subgestBoxHTML(userTranslation));
            //updateVerticalSize();
        }
    }

	public ChunkIndex getChunkIndex() {
		return chunkIndex;
	}
	
	public List<TranslationPair> getSuggestions() {
		return this.translationResult.getTmSuggestions();
	}
	
	public void setSuggestionWidget(Widget suggestionWidget) {
		this.suggestionWidget = suggestionWidget;
	}
	
	public Widget getSuggestionWidget() {
		return suggestionWidget;
	}
	

	/**
	 * The Cell used to render the list of suggestions from the current TranslationPair.
	 */
	static class SuggestionCell extends AbstractCell<TranslationPair> {

		// for explicitly setting the selection after Enter key press
		private SingleSelectionModel<TranslationPair> selectionModel;
		private PopupPanel parentPopup;

		public SuggestionCell(SingleSelectionModel<TranslationPair> selectionModel, PopupPanel parentPopup) {
			super("keydown"); // tells the AbstractCell that we want to catch the keydown events
			this.selectionModel = selectionModel;
			this.parentPopup = parentPopup;
		}

		@Override
		public void render(Context context, TranslationPair value, SafeHtmlBuilder sb) {
			// Value can be null, so do a null check:
			if (value == null) {
				return;
			}
			SubgestPopupStructure struct = new SubgestPopupStructure(value);
			// TODO: find another way how to render this - this is probably neither the safest, nor the best one...
			sb.append( SafeHtmlUtils.fromTrustedString(struct.toString()) );
			
			
			/*
			 * previously rendered this way:
			sb.appendHtmlConstant("<table class='suggestionItem'>");
			
			// show the suggestion:
			sb.appendHtmlConstant("<tr><td class='suggestionItemText'>");
			sb.appendEscaped(value.getStringL2());
			sb.appendHtmlConstant("</td></tr>");
			
			// show the corresponding match:
			sb.appendHtmlConstant("<tr><td class='suggestionItemMatch'>(\"");
			sb.appendEscaped(value.getStringL1());
			sb.appendHtmlConstant("\")</td>");
			
			// show their (combined) score:
			sb.appendHtmlConstant("<td class='suggestionItemScore'>(");
			if (value.getScore() != null) {
				sb.appendEscaped( Double.toString(value.getScore()) );
			}
			sb.appendHtmlConstant(")</td>");
			sb.appendHtmlConstant("</tr>");
			
			// show the corresponding match:
			sb.appendHtmlConstant("<tr><td class='suggestionItemSource'>(source: ");
			sb.appendEscaped(value.getSource().getDescription());
			sb.appendHtmlConstant(")</td>");

			sb.appendHtmlConstant("</tr>");
			sb.appendHtmlConstant("</table>");
			*/
		}
		
		@Override
		protected void onEnterKeyDown(Context context, Element parent, TranslationPair value,
				NativeEvent event, ValueUpdater<TranslationPair> valueUpdater) {
			//super.onEnterKeyDown(context, parent, value, event, valueUpdater);
			// selecting also by Enter is automatic in Opera only, others use only Spacebar
			// (and we want also Enter everywhere)
			event.preventDefault();
			selectionModel.setSelected(value, true);
		}
		
		@Override
		public void onBrowserEvent(
				com.google.gwt.cell.client.Cell.Context context,
				Element parent, TranslationPair value, NativeEvent event,
				ValueUpdater<TranslationPair> valueUpdater) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
			// handle the tabbing out from the selecting process (by keyboard) - hiding the popup:
			if ("keydown".equals(event.getType())) {
				if (event.getKeyCode() == KeyCodes.KEY_TAB) {
					parentPopup.setVisible(false);
					// (we cannot hide it because the list would lose its proper TabIndex)
				}
			}
		}
		
	}

	
	public void loadSuggestions() {
		if (loadedSuggestions == true) {
            return;
        }

        loadedSuggestions=true;
        // creating the suggestions pop-up panel:
		//FlowPanel suggestPanel = new FlowPanel();
		suggestPanel = new PopupPanel();
		suggestPanel.setAutoHideEnabled(true);
		suggestPanel.setStylePrimaryName("suggestionsPopup");
		
		final SingleSelectionModel<TranslationPair> selectionModel = new SingleSelectionModel<TranslationPair>();
		CellList<TranslationPair> cellList = new CellList<TranslationPair>( new SuggestionCell(selectionModel, suggestPanel) );
		cellList.setWidth( Integer.toString(this.getOffsetWidth()) + "px" );
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		
		// setting tabindex so that the suggestions are focused between this box and the next one
		cellList.setTabIndex(this.getTabIndex());
		
		cellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				TranslationPair selected = selectionModel.getSelectedObject();
				if (selected != null) {
					//gui.log("selection changed...");
					//int selectedIndex = translationResult.getTmSuggestions().indexOf(selected);
					translationResult.setSelectedTranslationPairID(selected.getId());
					
                    // copy the selected suggestion into the textbox:
					//setValue(selected.getStringL2(), true);
					// copy the selected suggestion into the richtextarea with the annotation highlighting:
					setHTML(subgestBoxHTML(getAnnotatedSuggestionFromChunk(selected.getChunkL2())));
                    updateVerticalSize();

                    Scheduler.get().scheduleDeferred( new ScheduledCommand() {
                        @Override
                        public void execute() {
                            SubgestBox.this.setFocus(true);
                        }
                    } );
				}
			}
		} );
		cellList.setRowData(this.getSuggestions());
		suggestPanel.setWidget(cellList);
		
		this.setSuggestionWidget(suggestPanel);
	}

	public void showSuggestions() {
        if(this.getSuggestions().size() > 0) {
		    //suggestPanel.showRelativeTo(this);
            final UIObject relativeObject = this;
            suggestPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    // Calculate left position for the popup
                    int left = relativeObject.getAbsoluteLeft();
                    // Calculate top position for the popup
                    int top = relativeObject.getAbsoluteTop();
                    // Position below the textbox:
                    top += relativeObject.getOffsetHeight();
                    suggestPanel.setPopupPosition(left, top);
                }
            });
		    suggestionWidget.setWidth(this.getOffsetWidth() + "px");
        }
	}
	
	
	private String getAnnotatedSuggestionFromChunk(Chunk chunk) {
        // expects that annotations are non-overlapping and ordered by their position
        // (should be)
        if (chunk.getAnnotations().size() > 0) {
            gui.log("current chunk has " + chunk.getAnnotations().size() + " annotations");
        }
        StringBuffer sb = new StringBuffer(chunk.getSurfaceForm());
        for (Annotation annotation : chunk.getAnnotations()) {
			String toInsertBegin = "<span style=\"background-color:" + annotationColor.get(annotation.getType()) + "\">";
			String toInsertEnd   = "</span>";
			sb.insert(annotation.getBegin(), toInsertBegin);
			sb.insert(annotation.getEnd() + toInsertBegin.length(), toInsertEnd);
		}
		return sb.toString();
	}
	
	
	/**
	 * Returns the underlying TranslationResult.
	 * @return the TranslationResult upon which this SubgestBox is built
	 */
	public TranslationResult getTranslationResult() {
		return this.translationResult;
	}

    protected String getTextWithNewlines() {
        String text = this.getHTML();
        RegExp newlineTags = RegExp.compile("<p>|<div>|<br>", "g");
        RegExp toClean = RegExp.compile("</p>|</div>|&nbsp;", "g");
        RegExp newlineSequence = RegExp.compile("\n*");
        text = newlineTags.replace(text, "\n");
        text = toClean.replace(text, "");
        text = newlineSequence.replace(text, "\n");
        text = text.trim();
        return text;
    }

    protected boolean textChanged() {
        return !this.getText().equals(this.lastText);
    }

    protected void updateLastText() {
        this.lastText = this.getText();
    }

    public Document getFrameDoc(){
        return ((FrameElement) this.getElement().cast()).getContentDocument();
    }


    public int getCorrectVerticalSize() {
        //if this happen right after replacing, it's wrong
        //=> I will also have to take in the size of the substitute
        
        int newHeight = getFrameDoc().getScrollHeight(); // or: .getDocumentElement().getOffsetHeight();
        int substituteSize = substitute.lastVerticalSize;
        //Window.alert("new je "+newHeight+"subst je "+substituteSize);
        if (newHeight>substituteSize) {
            return newHeight;
        } else {
            return substituteSize;
        }

    }

    public void updateVerticalSize() {
         int newHeight = getCorrectVerticalSize();
//         Window.alert("JDU UPDATE REAL VERTICAL SIZE "+newHeight);
         
         if (newHeight != getFrameDoc().getClientHeight()) {
            setHeight((newHeight) + "px");
            showSuggestions();
        }
    }

    @Override
	public int compareTo(SubgestBox that) {
		// compare according to the underlying TranslationResult
		return this.translationResult.compareTo( that.getTranslationResult() );
	}
	
}
