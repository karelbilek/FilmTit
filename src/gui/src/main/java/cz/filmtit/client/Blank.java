package cz.filmtit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Blank extends Composite {

	private static BlankUiBinder uiBinder = GWT.create(BlankUiBinder.class);

	interface BlankUiBinder extends UiBinder<Widget, Blank> {
	}

	private Gui gui = Gui.getGui();
	
	public Blank() {
		initWidget(uiBinder.createAndBindUi(this));
		
        gui.guiStructure.contentPanel.setStyleName("blank");
        gui.guiStructure.contentPanel.setWidget(this);
	}

}
