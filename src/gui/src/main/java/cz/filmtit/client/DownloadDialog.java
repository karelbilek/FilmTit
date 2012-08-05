package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import cz.filmtit.share.Document;
//import com.google.gwt.user.client.ui.RadioButton;



public class DownloadDialog extends Composite {

	private static DownloadDialogUiBinder uiBinder = GWT
			.create(DownloadDialogUiBinder.class);

	interface DownloadDialogUiBinder extends UiBinder<Widget, DownloadDialog> {
	}
	
	private Gui gui = Gui.getGui();
    
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
        return "/download/download?docId="+document.getId()+"&sessionId="+gui.getSessionID()+"&type="+format+"&way="+way;
    }

    ClickHandler handlerForFormat(final String format) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location.assign(generateUrl(detectWay(), format));
                dialogBox.hide();
            }
        };
    }

    @UiField
    Modal dialogBox;

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
