package cz.filmtit.client;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
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
	
	public SubgestBox(int id, TranslationResult translationResult, Gui gui) {
		this.id = id;
		this.translationResult = translationResult;
		this.gui = gui;
		
		this.addFocusHandler(this.gui.subgestHandler);
		this.addKeyDownHandler(this.gui.subgestHandler);
		this.addValueChangeHandler(this.gui.subgestHandler);
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
	
	
	private class SuggestionCell extends AbstractCell<TranslationPair> {
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				TranslationPair value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}
			sb.appendEscapedLines(value.getStringL2() + "\n(\"" + value.getStringL1() + "\")" + "\n");
		}		
	}
	
	public void showSuggestions() {
		// creating the suggestions pop-up panel:
		FlowPanel suggestPanel = new FlowPanel();
		
		CellList<TranslationPair> cellList = new CellList<TranslationPair>(new SuggestionCell());
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		final SingleSelectionModel<TranslationPair> selectionModel = new SingleSelectionModel<TranslationPair>();
		cellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				TranslationPair selected = selectionModel.getSelectedObject();
				if (selected != null) {
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
		suggestPanel.add(cellList);
		suggestPanel.setStylePrimaryName("suggestionsPopup");
		
		
		// positioning the pop-up panel:
		//AbsolutePanel parentPanel = (AbsolutePanel) this.getParent();
		//int sugleft = parentPanel.getAbsoluteLeft() + parentPanel.getWidgetLeft(this) + 8;
		//int sugtop  = parentPanel.getAbsoluteTop()  + parentPanel.getWidgetTop(this) + 25;
		int sugleft = this.getAbsoluteLeft() - this.gui.rootPanel.getAbsoluteLeft() + 8;
		int sugtop  = this.getAbsoluteTop()  - this.gui.rootPanel.getAbsoluteTop() + 35;
		this.gui.rootPanel.add(suggestPanel, sugleft, sugtop);
		this.setSuggestionWidget(suggestPanel);
	}
	
	
	/**
	 * Returns the underlying TranslationResult.
	 * @return
	 */
	public TranslationResult getTranslationResult() {
		return this.translationResult;
	}
	
}
