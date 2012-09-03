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

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import cz.filmtit.client.Gui;
import cz.filmtit.share.Document;


/**
 * A dialog that enables the user to download the subtitles.
 * @author rur
 *
 */
public class DownloadDialog extends Dialog {

	private static DownloadDialogUiBinder uiBinder = GWT
			.create(DownloadDialogUiBinder.class);

	interface DownloadDialogUiBinder extends UiBinder<Widget, DownloadDialog> {
	}
	
    private Document document;

    /**
     * Shows the dialog.
     * @param document
     */
	public DownloadDialog(Document document) {
		super();
        initWidget(uiBinder.createAndBindUi(this));
        target.setValue(true);
        this.document = document;
        
        srtButton.addClickHandler(new HandlerForFormat("srt"));
        txtButton.addClickHandler(new HandlerForFormat("txt"));
	}

    private String detectWay() {
        if (source.getValue()) {
            return "source";
        }
        if (target.getValue()) {
            return "target";
        }
        return "targetthrowback";
    }

    private String generateUrl(String way, String format) {
        return "/download/download?docId="+document.getId()+"&sessionId="+Gui.getSessionID()+"&type="+format+"&way="+way;
    }

    private class HandlerForFormat implements ClickHandler {

    	private String format;

		public HandlerForFormat(String format) {
    		this.format = format;
		}
    	
		public void onClick(ClickEvent event) {
            Window.Location.assign(generateUrl(detectWay(), format));
            close();
		}
    	
    }

    @UiField
    RadioButton source;

    @UiField
    RadioButton target;

    @UiField
    RadioButton targetthrowback;

    @UiField
	Button srtButton;

    @UiField
    Button txtButton;

}
