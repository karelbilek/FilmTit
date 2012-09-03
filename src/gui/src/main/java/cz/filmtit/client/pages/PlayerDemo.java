/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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
