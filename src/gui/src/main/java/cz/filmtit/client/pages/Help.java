package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.UIObject;

import cz.filmtit.client.Gui;

/**
 * A page showing the user manual of the application.
 * @author rur
 *
 */
public class Help extends UIObject {

	private static HelpUiBinder uiBinder = GWT.create(HelpUiBinder.class);

	interface HelpUiBinder extends UiBinder<Element, Help> {
	}

	/**
	 * Shows the page.
	 */
	public Help() {
		setElement(uiBinder.createAndBindUi(this));
		
		Gui.getGuiStructure().contentPanel.setStyleName("help");
		
		HTMLPanel htmlWrapper = new HTMLPanel(this.getElement().getInnerHTML());
		Gui.getGuiStructure().contentPanel.setWidget(htmlWrapper);
		
		// Document.get().getBody().appendChild(this.getElement());
		// Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
