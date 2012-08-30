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



public class DownloadDialog extends Dialog {

	private static DownloadDialogUiBinder uiBinder = GWT
			.create(DownloadDialogUiBinder.class);

	interface DownloadDialogUiBinder extends UiBinder<Widget, DownloadDialog> {
	}
	
    Document document;

	public DownloadDialog(Document document) {
		super();
        initWidget(uiBinder.createAndBindUi(this));
        target.setValue(true);
        this.document = document;
        
        srtButton.addClickHandler(new HandlerForFormat("srt"));
        txtButton.addClickHandler(new HandlerForFormat("txt"));
	}

    String detectWay() {
        if (source.getValue()) {
            return "source";
        }
        if (target.getValue()) {
            return "target";
        }
        return "targetthrowback";
    }

    String generateUrl(String way, String format) {
        return "/download/download?docId="+document.getId()+"&sessionId="+Gui.getSessionID()+"&type="+format+"&way="+way;
    }

    class HandlerForFormat implements ClickHandler {

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
