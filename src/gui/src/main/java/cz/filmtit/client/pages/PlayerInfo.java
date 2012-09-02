package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;

/**
 * A page showing guidelines on how to run the media player.
 * @author karel
 *
 */
public class PlayerInfo extends Composite {

	private static PlayerInfoUiBinder uiBinder = GWT
			.create(PlayerInfoUiBinder.class);

	interface PlayerInfoUiBinder extends UiBinder<Widget, PlayerInfo> {
	}

	/**
	 * Shows the page.
	 */
	public PlayerInfo() {
		initWidget(uiBinder.createAndBindUi(this));
		
		Gui.getGuiStructure().contentPanel.setStyleName("playerInfo");
		Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
