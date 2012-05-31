package cz.filmtit.client;

import java.util.List;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.TranslationPair;


/**
 * Variant of a TextBox with pop-up suggestions
 * taken from the given TranslationResult.
 * 
 * @author Honza Václ
 *
 */
public class SubgestBox extends TextBox implements Comparable {
	private int id;
	private TranslationResult translationResult;
	private Widget suggestionWidget;
	private Gui gui;
	private PopupPanel suggestPanel;
	
	
	public SubgestBox(int id, TranslationResult translationResult, Gui gui) {
		this.id = id;
		this.translationResult = translationResult;
		this.gui = gui;
		
		this.addFocusHandler(this.gui.subgestHandler);
		this.addKeyDownHandler(this.gui.subgestHandler);
		this.addValueChangeHandler(this.gui.subgestHandler);
		
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
			super("keydown");
			this.selectionModel = selectionModel;
			this.parentPopup = parentPopup;
		}

		@Override
		public void render(Context context, TranslationPair value, SafeHtmlBuilder sb) {
			// Value can be null, so do a null check:
			if (value == null) {
				return;
			}

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
		}
		
		@Override
		protected void onEnterKeyDown(Context context, Element parent, TranslationPair value,
				NativeEvent event, ValueUpdater<TranslationPair> valueUpdater) {
			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
			// selecting also by Enter is automatic in Opera only, others use only Spacebar
			// (and we want also Enter everywhere)
			selectionModel.setSelected(value, true);
		}
		
		@Override
		public void onBrowserEvent(
				com.google.gwt.cell.client.Cell.Context context,
				Element parent, TranslationPair value, NativeEvent event,
				ValueUpdater<TranslationPair> valueUpdater) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
			// handle the tabbing out - hiding the popup:
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
					setValue(selected.getStringL2(), true);
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
		
	
	/**
	 * Returns the underlying TranslationResult.
	 * @return the TranslationResult upon which this SubgestBox is built
	 */
	public TranslationResult getTranslationResult() {
		return this.translationResult;
	}

	@Override
	public int compareTo(Object that) {
		if (that instanceof SubgestBox) {
			return this.getTranslationResult().compareTo( ((SubgestBox)that).getTranslationResult() );
		}
		else {
			throw new ClassCastException();
		}
	}
	
}
