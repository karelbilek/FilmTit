package cz.filmtit.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
public class SubgestBox extends TextBox {
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

		//private TranslationResult transresult;

		/*
		public SuggestionCell(TranslationResult transresult) {
			this.transresult = transresult;
		}
		*/
		public SuggestionCell() {
			super("keydown");
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
			//sb.appendEscaped( Double.toString(value.getScore()) );
			sb.appendHtmlConstant(")</td>");
			
			sb.appendHtmlConstant("</tr>");
			sb.appendHtmlConstant("</table>");
		}
		
		@Override
		protected void onEnterKeyDown(Context context, Element parent, TranslationPair value,
				NativeEvent event, ValueUpdater<TranslationPair> valueUpdater) {
			//Window.alert("onEnterKeyDown on a cell");
			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	
	private void loadSuggestions() {
		// creating the suggestions pop-up panel:
		//FlowPanel suggestPanel = new FlowPanel();
		suggestPanel = new PopupPanel(true);
		suggestPanel.setStylePrimaryName("suggestionsPopup");
		
		CellList<TranslationPair> cellList = new CellList<TranslationPair>( new SuggestionCell() );
		cellList.setWidth( Integer.toString(this.getOffsetWidth()) + "px" );
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		
		final SingleSelectionModel<TranslationPair> selectionModel = new SingleSelectionModel<TranslationPair>();
		cellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				TranslationPair selected = selectionModel.getSelectedObject();
				if (selected != null) {
					gui.log("selection changed...");
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
		//suggestPanel.add(cellList);
		suggestPanel.setWidget(cellList);
		//suggestPanel.setAutoHideEnabled(true);
		
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
	
}
