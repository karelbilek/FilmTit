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
        initWidget(uiBinder.createAndBindUi(this));
        target.setChecked(true);
        this.document = document;

        
        srtButton.addClickHandler(handlerForFormat("srt"));
        subButton.addClickHandler(handlerForFormat("sub"));
        txtButton.addClickHandler(handlerForFormat("txt"));

        dialogBox.show();

	}

    String detectWay() {
        if (source.isChecked()) {
            return "source";
        }
        if (target.isChecked()) {
            return "target";
        }
        return "targetthrowback";
    }

    String generateUrl(String way, String format) {
        return "/download/download?docId="+document.getId()+"&sessionId="+Gui.getSessionID()+"&type="+format+"&way="+way;
    }

    ClickHandler handlerForFormat(final String format) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location.assign(generateUrl(detectWay(), format));
                close();
            }
        };
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
    Button subButton;

    @UiField
    Button txtButton;

}
