package filmtit0.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import filmtit0.client.SubtitleList;


public class Filmtit0 implements EntryPoint {

	private Label subtitleBar;
	private SubtitleList sublist;

	@Override
	public void onModuleLoad() {
		
		String jsonText =
		"[" +
		"    {" +
		"        \"source\": \"Hi, Bob!\"," +
		"        \"match\": \"Hi, Bob!\"," +
		"        \"translation\": \"Ahoj, Bobe!\"" +
		"    }," +
		"    {" +
		"        \"source\": \"Hi, Tom!\"," +
		"        \"match\": \"Hi, Tom!\"," +
		"        \"translation\": \"Ahoj, Tome!\"" +
		"    }" +
		"    {" +
		"        \"source\": \"And he hath spoken...\"," +
		"        \"match\": \"\"," +
		"        \"translation\": \"\"" +
		"    }" +
		"    {" +
		"        \"source\": \"- Run, you fools!\"," +
		"        \"match\": \"\"," +
		"        \"translation\": \"\"" +
		"    }" +
		"]";

		// process JSON
		JSONHandler jhandler = new JSONHandler(jsonText);
		sublist = jhandler.generateSubtitleList();
		
		subtitleBar = new Label("< nothing here yet >");
		Button button = new Button("Next subtitle");
		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				SubtitleChunk nextSentence = sublist.getNextSubtitleChunk(); 
				subtitleBar.setText(nextSentence.source);
			}
			
		});

		RootPanel.get().add(subtitleBar);
		RootPanel.get().add(button, 105, 200);
	}
}
