package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;

/**
 * A special page with no contents and is used to temporarily hide the contents of another page.
 * @author rur
 *
 */
public class Blank extends Composite {

	private static BlankUiBinder uiBinder = GWT.create(BlankUiBinder.class);

	interface BlankUiBinder extends UiBinder<Widget, Blank> {
	}

	/**
	 * Show a blank page.
	 */
	public Blank() {
		initWidget(uiBinder.createAndBindUi(this));
		
        Gui.getGuiStructure().contentPanel.setStyleName("blank");
        Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
