package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.UIObject;

import cz.filmtit.client.Gui;

public class Help extends UIObject {

	private static HelpUiBinder uiBinder = GWT.create(HelpUiBinder.class);

	interface HelpUiBinder extends UiBinder<Element, Help> {
	}

	public Help() {
		setElement(uiBinder.createAndBindUi(this));
		
		Gui.getGuiStructure().contentPanel.setStyleName("help");
		Document.get().getBody().appendChild(this.getElement());
		// Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
