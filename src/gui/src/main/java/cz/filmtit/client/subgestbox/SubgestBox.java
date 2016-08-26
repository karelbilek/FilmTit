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
package cz.filmtit.client.subgestbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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

/**
 * Variant of a TextBox with pop-up suggestions taken from the given
 * TranslationResult.
 *
 * The SubgestBox, or SUBtitle sugGESTion BOX, provides a textbox-like interface
 * and visualizes the TM results, offering a variety of means of navigation
 * through them. It is based on the IFrame HTML element to support also
 * multi-line and formatted inputs.
 *
 * The TM results are shown as a pop-up suggestion list when the textbox is
 * focused in a {@link SubgestPopupStructure}.
 *
 * Another features like auto-scrolling to a certain place of the screen and
 * height auto-adjustment for multi-line inputs were added to improve the user
 * experience. The behaviour and features mentioned requires a custom event
 * handling, this is provided by a {@link SubgestHandler} instance (common to
 * all the SubgestBoxes within a {@link TranslationWorkspace}).
 */
public class SubgestBox extends RichTextArea implements Comparable<SubgestBox> {

    private TimedChunk chunk;
    private TranslationResult translationResult;
    private TranslationWorkspace workspace;
    private PopupPanel suggestPanel;
    private Widget suggestionWidget;
    private boolean loadedSuggestions = false;
    String lastText = "";

    private void replaceFakeWithReal() {
        workspace.replaceFake(chunk, substitute, this);
    }

    private FakeSubgestBox substitute = null;

    /**
     * Lightweight input area serving as a substitute for the SubgestBox before
     * it is focused (and worked with)
     */
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
            this.setTabIndex(tabIndex);
            this.setStyleName("pre_subgestbox");
            this.addStyleName("loading");
            this.addStyleName("subgest_fullwidth");
        }

        /**
         * Set the fakebox' height according to its current contents.
         */
        public void updateVerticalSize() {
            int newHeight = this.getElement().getScrollHeight();
            // setHeight probably sets the "inner" height, i.e. this would be a bit larger
            // (everywhere but in Firefox):
            if (!Window.Navigator.getUserAgent().matches(".*Firefox.*")) {
                newHeight -= 16;
            }
            this.setHeight(newHeight + "px");
        }

        /**
         * Returns the "real" SubgestBox which is substituted by this fake one.
         *
         * @return the corresponding "real" SubgestBox
         */
        public SubgestBox getFather() {
            return SubgestBox.this;
        }

        /**
         * Comparison according to the underlying "real" SubgestBoxes
         *
         * @param that
         * @return
         */
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
        annotationColor.put(AnnotationType.PLACE, "#ffccff");
        annotationColor.put(AnnotationType.ORGANIZATION, "#ccffff");
        annotationColor.put(AnnotationType.PERSON, "#ffff99");
    }

    ;


    private String subgestBoxHTML(String content) {
        content = content.replaceAll("\n", "<br>");
        return content;
    }

    /**
     * Primary constructor for the SubgestBox.
     *
     * @param chunk - the source chunk of the underlying TranslationResult
     * @param workspace - the TranslationWorkspace in which this SubgestBox
     * operates
     * @param tabIndex - intended tabIndex within the workspace
     */
    public SubgestBox(TimedChunk chunk, TranslationWorkspace workspace, int tabIndex) {
        this.chunk = chunk;
        this.translationResult = new TranslationResult(chunk);
        this.workspace = workspace;
        if (this.workspace == null) {
            Gui.log("workspace for subgestbox is null!!!");
        }

        this.setHeight("36px");
        this.setHTML(subgestBoxHTML(""));

        this.addFocusHandler(this.workspace.subgestHandler);
        this.addKeyDownHandler(this.workspace.subgestHandler);
        this.addKeyUpHandler(this.workspace.subgestHandler);
        this.addBlurHandler(this.workspace.subgestHandler);
        this.addClickHandler(this.workspace.subgestHandler);

        this.setTabIndex(tabIndex);

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

    }

    /**
     * Set the underlying TranslationResult, displaying its userTranslation
     * immediately (if not empty). Also reset loadedSuggestions to false.
     *
     * @param translationResult - new value of the underlying TranslationResult
     */
    public void setTranslationResult(TranslationResult translationResult) {
        this.translationResult = translationResult;
        loadedSuggestions = false;
        String userTranslation = translationResult.getUserTranslation();

        if (userTranslation != null && !userTranslation.equals("")) {
            substitute.setText(userTranslation);
            substitute.updateVerticalSize();
            this.setHTML(subgestBoxHTML(userTranslation));
            updateLastText();
        }
    }

    /**
     * Returns the underlying TranslationResult's source chunk.
     *
     * @return
     */
    public TimedChunk getChunk() {
        return chunk;
    }

    /**
     * Returns the list of suggestions from the underlying TranslationResult.
     *
     * @return
     */
    public List<TranslationPair> getSuggestions() {
        return this.translationResult.getTmSuggestions();
    }

    /**
     * Set the widget used by this SubgestBox to display the suggestions.
     *
     * @param suggestionWidget
     */
    public void setSuggestionWidget(Widget suggestionWidget) {
        this.suggestionWidget = suggestionWidget;
    }

    /**
     * Returns the widget used by this SubgestBox to display the suggestions.
     *
     * @return
     */
    public Widget getSuggestionWidget() {
        return suggestionWidget;
    }

    /**
     * The Cell used to render the list of suggestions from the current
     * TranslationPair.
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
            // TODO after switching to GWT 2.5 - use UiRenderer for doing this;
            // (this is probably neither the safest, nor the best way...)
            sb.append(SafeHtmlUtils.fromTrustedString(struct.toString()));
        }

        @Override
        protected void onEnterKeyDown(Context context, Element parent, TranslationPair value,
                NativeEvent event, ValueUpdater<TranslationPair> valueUpdater) {
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

    /**
     * Prepare the suggestions from the underlying TranslationResult to be
     * displayed (if they are already fetched and not loaded yet).
     */
    public void loadSuggestions() {
        if (loadedSuggestions == true) {
            return;
        }

        loadedSuggestions = true;
        // creating the suggestions pop-up panel:
        suggestPanel = new PopupPanel();
        suggestPanel.setAutoHideEnabled(true);
        suggestPanel.setStylePrimaryName("suggestionsPopup");

        final SingleSelectionModel<TranslationPair> selectionModel = new SingleSelectionModel<TranslationPair>();
        CellList<TranslationPair> cellList = new CellList<TranslationPair>(new SuggestionCell(selectionModel, suggestPanel));
        cellList.setWidth(Integer.toString(this.getOffsetWidth()) + "px");
        cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        // setting tabindex so that the suggestions are focused between this box and the next one
        cellList.setTabIndex(this.getTabIndex());

        cellList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                TranslationPair selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    translationResult.setSelectedTranslationPairID(selected.getId());
                    // copy the selected suggestion into the richtextarea with the annotation highlighting:
                    setHTML(subgestBoxHTML(getAnnotatedSuggestionFromChunk(selected.getChunkL2())));
                    // contents have changed - resize if necessary:
                    updateVerticalSize();

                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            SubgestBox.this.setFocus(true);
                        }
                    });
                }
            }
        });
        cellList.setRowData(this.getSuggestions());
        suggestPanel.setWidget(cellList);

        this.setSuggestionWidget(suggestPanel);
    }

    /**
     * Display the suggestion widget with the suggestions from the underlying
     * TranslationResult.
     */
    public void showSuggestions() {
        if (this.getSuggestions().size() > 0) {
            // showing the suggestions always below this SubgestBox:
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
            Gui.log("current chunk has " + chunk.getAnnotations().size() + " annotations");
        }
        StringBuffer sb = new StringBuffer(chunk.getSurfaceForm());
        for (Annotation annotation : chunk.getAnnotations()) {
            String toInsertBegin = "<span style=\"background-color:" + annotationColor.get(annotation.getType()) + "\">";
            String toInsertEnd = "</span>";
            sb.insert(annotation.getBegin(), toInsertBegin);
            sb.insert(annotation.getEnd() + toInsertBegin.length(), toInsertEnd);
        }
        return sb.toString();
    }

    /**
     * Returns the underlying TranslationResult.
     *
     * @return the TranslationResult upon which this SubgestBox is built
     */
    public TranslationResult getTranslationResult() {
        return this.translationResult;
    }

    /**
     * Returns the SubgestBox' contents as text with newlines unified as "\n"
     * (also trimmed on the beginning and end and removed duplicate newlines)
     *
     * @return the text contents with unified newlines
     */
    public String getTextWithNewlines() {
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

    /**
     * True if the user translation text has changed since the last submitting
     * (or update) (except for the trimmed newlines).
     *
     * @return
     */
    public boolean textChanged() {
        return !this.getTextWithNewlines().equals(this.lastText);
    }

    /**
     * Replace the text remembered as the last saved text with the current text.
     * To be used when the text is sent to be saved as the user translation via
     * SetUserTranslation.
     */
    public void updateLastText() {
        this.lastText = this.getTextWithNewlines();
    }

    private int getCorrectVerticalSize() {
        FrameElement frameElement = (FrameElement) this.getElement().cast();
        int newHeight = frameElement.getContentDocument().getScrollHeight();
        return newHeight;
    }

    /**
     * Adjust the height of the input area according to the height of its
     * contents.
     */
    public void updateVerticalSize() {
        setHeight("36px"); // == height of the one-line SubgestBox
        // grow from that, if necessary:
        setHeight(getCorrectVerticalSize() + "px");
        // and refresh suggestions (to be placed correctly):
        showSuggestions();
    }

    /**
     * Comparison according to the underlying TranslationResults.
     *
     * @param that
     * @return value of compareTo applied on the underlying TranslationResults
     */
    @Override
    public int compareTo(SubgestBox that) {
        return this.translationResult.compareTo(that.getTranslationResult());
    }

}
