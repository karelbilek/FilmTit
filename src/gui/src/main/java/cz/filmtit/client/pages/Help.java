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
