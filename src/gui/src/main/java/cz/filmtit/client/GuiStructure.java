package cz.filmtit.client;


import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import com.github.gwtbootstrap.client.ui.*;


public class GuiStructure extends Composite {
	
	private static GuiStructureUiBinder uiBinder = GWT.create(GuiStructureUiBinder.class);

	interface GuiStructureUiBinder extends UiBinder<Widget, GuiStructure> {
	}

	public GuiStructure() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	NavLink login;

	@UiField
    ScrollPanel contentPanel;
	
	@UiField
	TextArea txtDebug;
	
}
