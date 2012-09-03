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

package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.user.client.Window;

/**
 * Adjustment of gwt-bootstrap's Modal component
 * - disabled animation for Opera browser
 */
public class CustomModal extends Modal {
	
	/**
	 * Creates the modal.
	 */
    public CustomModal() {
        super();
        if (Window.Navigator.getUserAgent().matches(".*Opera.*")) {
            this.setAnimation(false);
        }
        else {
            this.setAnimation(true);
        }
    }
}
