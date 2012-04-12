package cz.filmtit.client;

import java.util.ArrayList;
import java.util.ListIterator;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.cellview.client.CellBrowser;
import cz.filmtit.share.Chunk;

/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class gui implements EntryPoint {

	private SubtitleList sublist;

	private FilmTitServiceAsync filmTitSvc = GWT.create(FilmTitService.class);
	
	TextBox txtbxText;
	Label translation;

	
	@Override
	public void onModuleLoad() {
		/*
		// process JSON
		JSONHandler jhandler = new JSONHandler(new JSONExampleText().jsonText);
		sublist = jhandler.generateSubtitleList();
		*/

		// instead of JSON: filling SubtitleList by hand:
		sublist = new SubtitleList();
		{
			GUIChunk chunk1 = new GUIChunk("Hi, Bob!", new ArrayList<GUIMatch>());
			GUIMatch match1_1 = new GUIMatch("Hi, Bob!", new ArrayList<GUITranslation>());
			match1_1.getTranslations().add( new GUITranslation("Ahoj, Bobe!") );
			match1_1.getTranslations().add( new GUITranslation("ahoj, bobe!") );
			match1_1.getTranslations().add( new GUITranslation("nazdar, bobe!") );
			match1_1.getTranslations().add( new GUITranslation("Čau, Roberte!") );
			chunk1.getMatches().add(match1_1);
			GUIMatch match1_2 = new GUIMatch("Hi, Bob.", new ArrayList<GUITranslation>());
			match1_2.getTranslations().add( new GUITranslation("ahoj, bobe.") );
			match1_2.getTranslations().add( new GUITranslation("ahoj, bobe") );
			chunk1.getMatches().add(match1_2);
			sublist.addSubtitleChunk(chunk1);
			
			GUIChunk chunk2 = new GUIChunk("Hi, Tom!", new ArrayList<GUIMatch>());
			GUIMatch match2_1 = new GUIMatch("Hi Tom!", new ArrayList<GUITranslation>());
			match2_1.getTranslations().add( new GUITranslation("Ahoj Tome!") );
			match2_1.getTranslations().add( new GUITranslation("ahoj, tome!") );
			chunk2.getMatches().add(match2_1);
			sublist.addSubtitleChunk(chunk2);
			
			GUIChunk chunk3 = new GUIChunk("And he hath spoken...", new ArrayList<GUIMatch>());
			//GUIMatch match3_1 = new GUIMatch("", new ArrayList<GUITranslation>());
			//chunk3.getMatches().add(match3_1);
			sublist.addSubtitleChunk(chunk3);
			
			GUIChunk chunk4 = new GUIChunk("Run, you fools!", new ArrayList<GUIMatch>());
			GUIMatch match4_1 = new GUIMatch("Run, you fools!", new ArrayList<GUITranslation>());
			match4_1.getTranslations().add( new GUITranslation("Utíkejte, hlupáci!") );
			match4_1.getTranslations().add( new GUITranslation("Utíkejte, blbci!") );
			chunk4.getMatches().add(match4_1);
			GUIMatch match4_2 = new GUIMatch("Run, you bastards!", new ArrayList<GUITranslation>());
			//match4_2.getTranslations().add( new GUITranslation("Zdrhejte, hovada!") );
			//match4_2.getTranslations().add( new GUITranslation("utíkejte, plantážníci!") );
			chunk4.getMatches().add(match4_2);
			GUIMatch match4_3 = new GUIMatch("Run, run, run!", new ArrayList<GUITranslation>());
			match4_3.getTranslations().add( new GUITranslation("Běžte, běžte, běžte!") );
			match4_3.getTranslations().add( new GUITranslation("Makáme, makáme!") );
			match4_3.getTranslations().add( new GUITranslation("Utíkejte!!!") );
			chunk4.getMatches().add(match4_3);
			sublist.addSubtitleChunk(chunk4);
		}
		
		
		// -------------------- //
		// --- GUI creation --- //
		// -------------------- //
		
		RootPanel rootPanel = RootPanel.get();
		rootPanel.setSize("700", "500");

		
		// adding header:
		Label lblHeader = new Label("FilmTit - translate your subtits!");
		lblHeader.setStyleName("gwt-Header");
		lblHeader.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.add(lblHeader, 117, 10);
		lblHeader.setSize("345px", "-33px");
		

		// --- ListBox interface --- //
		// adding ListBox for the source subtitles:
		final ListBox sourceBox = new ListBox();
		sourceBox.setVisibleItemCount(3);
		rootPanel.add(sourceBox, 10, 108);
		sourceBox.setSize("164px", "77px");
		// filling the box with the source subtitles:
		ListIterator<GUIChunk> chunkiterator = sublist.getChunks().listIterator();
		while(chunkiterator.hasNext()) {
			sourceBox.addItem(chunkiterator.next().getChunkText());
		}
				
		// adding ListBox for the matching sentences:
		final ListBox matchBox = new ListBox();
		matchBox.setVisibleItemCount(3);
		rootPanel.add(matchBox, 186, 108);
		matchBox.setSize("164px", "77px");
		
		sourceBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int sourceindex = sourceBox.getSelectedIndex();
				matchBox.clear();
				ListIterator<GUIMatch> matchiterator = sublist.getSubtitleChunkAt(sourceindex).getMatches().listIterator();
				while (matchiterator.hasNext()) {
					matchBox.addItem(matchiterator.next().getMatchText());	
				}
				matchBox.setVisible(true);
			}
		} );
				
		// adding ListBox for the translations:
		final ListBox translationBox = new ListBox();
		translationBox.setVisibleItemCount(3);
		rootPanel.add(translationBox, 362, 108);
		translationBox.setSize("164px", "77px");
		
		matchBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int sourceindex = sourceBox.getSelectedIndex();
				int matchindex = matchBox.getSelectedIndex();
				translationBox.clear();
				// here will be rather list of matches (probably strings):
				ListIterator<GUITranslation> translationiterator
					= sublist.getSubtitleChunkAt(sourceindex).getMatches().get(matchindex).getTranslations().listIterator();
				while (translationiterator.hasNext()) {
					translationBox.addItem(translationiterator.next().getTranslationText());	
				}
				translationBox.setVisible(true);
			}
		} );
		// --- end of ListBox interface --- //		

		
		// --- CellBrowser interface --- //
		CellBrowser cellBrowser = new CellBrowser(new SubtitleTreeModel(sublist), null);
		cellBrowser.setMinimumColumnWidth(154);
		cellBrowser.setDefaultColumnWidth(154);
		cellBrowser.setSize("100%", "100px");
		rootPanel.add(cellBrowser, 10, 219);
		// --- end of CellBrowser interface --- //
		
		
		
		
		
		
		
		
		
		
		
		
			txtbxText = new TextBox();
			txtbxText.setText("hi");
			rootPanel.add(txtbxText, 17, 25);
			
			Button btnTranslate = new Button("Translate");
			btnTranslate.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// handling todleto
					if (filmTitSvc == null) {
						filmTitSvc = GWT.create(FilmTitService.class);
					}
					
					AsyncCallback<Chunk> callback = new AsyncCallback<Chunk>() {
						
						public void onSuccess(Chunk result) {
							translation.setText(result.userTranslation);
						}
						
						public void onFailure(Throwable caught) {
							Window.alert(caught.getLocalizedMessage());
						}
					};
					
					Chunk chunk = new Chunk();
					chunk.text = txtbxText.getText();
					filmTitSvc.suggestions(chunk, callback);
					
				}
			});
			rootPanel.add(btnTranslate, 57, 73);
			
			translation = new Label("");
			rootPanel.add(translation, 30, 129);
		
		
		
		
		
		
		
		
		
		
		
		
		

	}	// onModuleLoad()
	
}
