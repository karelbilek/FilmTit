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
