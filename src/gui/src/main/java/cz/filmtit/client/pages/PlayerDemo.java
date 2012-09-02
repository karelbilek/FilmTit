package cz.filmtit.client.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;

/**
 * A page showing a VLC player demo to test whether the user's browser supports it.
 * @author karel
 *
 */
public class PlayerDemo extends Composite {

	private static PlayerDemoUiBinder uiBinder = GWT
			.create(PlayerDemoUiBinder.class);

	interface PlayerDemoUiBinder extends UiBinder<Widget, PlayerDemo> {
	}

	/**
	 * Shows the page and plays the video.
	 */
	public PlayerDemo() {
		initWidget(uiBinder.createAndBindUi(this));
		
		Gui.getGuiStructure().contentPanel.setStyleName("playerDemo");
		Gui.getGuiStructure().contentPanel.setWidget(this);
	}

}
