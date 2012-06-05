package cz.filmtit.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import cz.filmtit.share.Chunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.annotations.Annotation;
import cz.filmtit.share.annotations.AnnotationType;


/**
 * Variant of a TextBox with pop-up suggestions
 * taken from the given TranslationResult.
 * 
 * @author Honza Václ
 *
 */
public class SubgestBox extends RichTextArea implements Comparable<SubgestBox> {
	private int id;
	private TranslationResult translationResult;
	private Widget suggestionWidget;
	private Gui gui;
	private PopupPanel suggestPanel;
	
	private static final Map<AnnotationType, String> annotationColor = new HashMap<AnnotationType, String>();
	static {
		// setting the colors for annotations:
		annotationColor.put(AnnotationType.PLACE,        "#ffccff");
		annotationColor.put(AnnotationType.ORGANIZATION, "#ccffff");
		annotationColor.put(AnnotationType.PERSON,       "#ffff99");
	};
	
	
	public SubgestBox(int id, TranslationResult translationResult, Gui gui) {
		this.id = id;
		this.translationResult = translationResult;
		this.gui = gui;
		
		this.setHeight("36px");
		
		this.addFocusHandler(this.gui.subgestHandler);
		this.addKeyDownHandler(this.gui.subgestHandler);
		//this.addValueChangeHandler(this.gui.subgestHandler);
		this.addBlurHandler(this.gui.subgestHandler);
		
		this.setTabIndex(id + 1);
		
		this.loadSuggestions();
		
	}
	
	public int getId() {
		return id;
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

	
	private void loadSuggestions() {
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
					// TODO: rewrite the TPair's "id" acquisition in some reasonable way...
					int i = 0;
					for (TranslationPair transpair : translationResult.getTmSuggestions()) {
						if (transpair.equals(selected)) {
							break;
						}
						else {
							i++;
						}
					}
					translationResult.setSelectedTranslationPairID(i);
					// copy the selected suggestion into the textbox:
					//setValue(selected.getStringL2(), true);
					// copy the selected suggestion into the richtextarea with the annotation highlighting:
					setHTML(getAnnotatedSuggestionFromChunk(selected.getChunkL2()));
							
					setFocus(true);
				}
			}
		} );
		cellList.setRowData(this.getSuggestions());
		suggestPanel.setWidget(cellList);
		
		this.setSuggestionWidget(suggestPanel);
	}
	
	
	public void showSuggestions() {
		suggestPanel.showRelativeTo(this);
		suggestionWidget.setWidth(this.getOffsetWidth() + "px");
	}
	
	
	private String getAnnotatedSuggestionFromChunk(Chunk chunk) {
		// naively expects that annotations are non-overlapping and ordered by their position
		// TODO: generalize to correct behavior independent of the annotations' positions and possible nesting
		gui.log("number of annotations for this chunk: " + chunk.getAnnotations().size());
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

	@Override
	public int compareTo(SubgestBox that) {
		// compare according to the underlying TranslationResult
		return this.translationResult.compareTo( ((SubgestBox)that).getTranslationResult() );
	}
	
}
