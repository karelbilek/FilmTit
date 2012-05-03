package cz.filmtit.client;

import java.util.List;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.TranslationPair;


/**
 * Variant of a TextBox with pop-up suggestions
 * taken from the given TranslationResult.
 * 
 * @author omikronn
 *
 */

public class SubgestBox extends TextBox {
	private int id;
	private TranslationResult translationResult;
	private Widget suggestionWidget;
	private RootPanel rootPanel;
	
	public SubgestBox(int id, TranslationResult translationResult, RootPanel rootPanel) {
		this.id = id;
		this.translationResult = translationResult;
		
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
	
	public void showSuggestions() {
		AbsolutePanel parentPanel = (AbsolutePanel)this.getParent();
		List<TranslationPair> suggestions = this.getSuggestions();
		String suggestion = "";
		for (TranslationPair suggestiontp : suggestions) {
			suggestion += suggestiontp.getStringL2() + "\n(\"" + suggestiontp.getStringL1() + "\")" + "\n";
		}
		
		Label lblSuggestions = new HTML(new SafeHtmlBuilder().appendEscapedLines(suggestion).toSafeHtml());
		lblSuggestions.setStylePrimaryName("suggestionsPopup");
		int sugleft = parentPanel.getAbsoluteLeft() + parentPanel.getWidgetLeft(this) + 8;
		int sugtop  = parentPanel.getAbsoluteTop()  + parentPanel.getWidgetTop(this) + 25;
		parentPanel.add(lblSuggestions, sugleft, sugtop);
		this.setSuggestionWidget(lblSuggestions);
	}
}
