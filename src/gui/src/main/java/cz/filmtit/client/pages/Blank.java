package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;

public class Blank extends Composite {

	private static BlankUiBinder uiBinder = GWT.create(BlankUiBinder.class);

	interface BlankUiBinder extends UiBinder<Widget, Blank> {
	}

	public Blank() {
		initWidget(uiBinder.createAndBindUi(this));
		
        Gui.getGuiStructure().contentPanel.setStyleName("blank");
        Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
