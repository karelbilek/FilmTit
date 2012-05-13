package cz.filmtit.client;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
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
	
	
	
	/**
	 * The Cell used to render a {@link ContactInfo}.
	 */
	static class SuggestionCell extends AbstractCell<TranslationPair> {

		//private TranslationResult transresult;

		/*
		public SuggestionCell(TranslationResult transresult) {
			this.transresult = transresult;
		}
		*/

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
	}
	
	
	public void showSuggestions() {
		// creating the suggestions pop-up panel:
		FlowPanel suggestPanel = new FlowPanel();
		
		CellList<TranslationPair> cellList = new CellList<TranslationPair>( new SuggestionCell() );
		cellList.setWidth( Integer.toString(this.getOffsetWidth()) + "px" );
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

		//int sugleft = this.getAbsoluteLeft() - this.gui.rootPanel.getAbsoluteLeft() + 8;
		//int sugtop  = this.getAbsoluteTop()  - this.gui.rootPanel.getAbsoluteTop() + 35;
		//this.gui.rootPanel.add(suggestPanel, sugleft, sugtop);
		
		int sugleft = this.getAbsoluteLeft() - this.gui.mainPanel.getAbsoluteLeft() +  8;
		int sugtop  = this.getAbsoluteTop()  - this.gui.mainPanel.getAbsoluteTop()  + 35;
		//gui.log("suggestion coordinates: " + Integer.toString(sugleft) + ", " + Integer.toString(sugtop));
		this.gui.mainPanel.add(suggestPanel, sugleft, sugtop);
				
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
