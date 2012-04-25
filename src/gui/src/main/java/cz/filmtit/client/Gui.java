package cz.filmtit.client;


import java.util.Iterator;
import java.util.ListIterator;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.RadioButton;

import cz.filmtit.share.*;

//lib-gwt-file imports:
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.FileUploadExt;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;



/**
 * Entry point for the FilmTit GWT web application,
 * including the GUI creation.
 * 
 * @author Honza Václ
 *
 */

public class Gui implements EntryPoint {

	private GUISubtitleList sublist;

	private TextArea txtDebug;
	private RadioButton rdbFormatSrt;
	private RadioButton rdbFormatSub;

	private RootPanel rootPanel; 
	private CellBrowser cellBrowser;

	// FilmTitServiceAsync should be created automatically
	// from FilmTitService during compilation...?
	private FilmTitServiceHandler rpcHandler = new FilmTitServiceHandler();
	
	TextBox txtbxText;
	Label translation;

	
	@Override
	public void onModuleLoad() {

		
		
		sublist = new GUISubtitleList(new SampleDocument()); 
		
		
		// -------------------- //
		// --- GUI creation --- //
		// -------------------- //
		
		rootPanel = RootPanel.get();
		rootPanel.setSize("800", "600");

		
		// adding header:
		Label lblHeader = new Label("FilmTit - translate your subtits!");
		lblHeader.setStyleName("gwt-Header");
		lblHeader.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPanel.add(lblHeader, 10, 0);
		lblHeader.setSize("436px", "0px");


		// debug-area:
		txtDebug = new TextArea();
		rootPanel.add(txtDebug, 412, 188);
		txtDebug.setSize("460px", "176px");
		txtDebug.setText("debugging outputs...\n");


		// --- ListBox interface --- //
		// adding ListBox for the source subtitles:
		final ListBox sourceBox = new ListBox();
		sourceBox.setVisibleItemCount(3);
		rootPanel.add(sourceBox, 11, 419);
		sourceBox.setSize("212px", "120px");
		// filling the box with the source subtitles:
		ListIterator<GUIChunk> chunkiterator = sublist.getChunks().listIterator();
		while(chunkiterator.hasNext()) {
			sourceBox.addItem(chunkiterator.next().getChunkText());
		}
		/*
		ListIterator<Chunk> chunkiterator = document.chunks.listIterator();
		while(chunkiterator.hasNext()) {
			sourceBox.addItem(chunkiterator.next().text);
		}
		*/
		
		// adding ListBox for the matching sentences:
		final ListBox matchBox = new ListBox();
		matchBox.setVisibleItemCount(3);
		rootPanel.add(matchBox, 234, 419);
		matchBox.setSize("212px", "120px");
		
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
		rootPanel.add(translationBox, 457, 419);
		translationBox.setSize("212px", "120px");
		
		matchBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int sourceindex = sourceBox.getSelectedIndex();
				int matchindex = matchBox.getSelectedIndex();
				translationBox.clear();
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
		cellBrowser = new CellBrowser(new SubtitleTreeModel(sublist), null);
		cellBrowser.setMinimumColumnWidth(154);
		cellBrowser.setDefaultColumnWidth(154);
		cellBrowser.setSize("660px", "150px");
		rootPanel.add(cellBrowser, 11, 566);
		// --- end of CellBrowser interface --- //
		
		
		// --- subfile format (srt/sub) options --- //
		// (currently used by both textarea and file input)
		Label lblChooseFileFormat = new Label("Choose file format: (both for file & txtarea)");
		rootPanel.add(lblChooseFileFormat, 10, 134);
		lblChooseFileFormat.setSize("160px", "42px");
		
		rdbFormatSrt = new RadioButton("file format", ".srt");
		rootPanel.add(rdbFormatSrt, 175, 134);
		rdbFormatSrt.setSize("105px", "20px");
		rdbFormatSub = new RadioButton("file format", ".sub");
		rootPanel.add(rdbFormatSub, 175, 160);
		rdbFormatSub.setSize("105px", "20px");
		rdbFormatSrt.setValue(true);  // default - srt
		// --- end of subfile format options --- //


		
		// --- file reading interface via lib-gwt-file --- //
		Label lblChooseEncoding = new Label("Choose encoding:");
		rootPanel.add(lblChooseEncoding, 286, 134);
		lblChooseEncoding.setSize("121px", "42px");
		
		final RadioButton rdbEncodingUtf8 = new RadioButton("file encoding", "UTF-8");
		rootPanel.add(rdbEncodingUtf8, 412, 134);
		rdbEncodingUtf8.setSize("105px", "20px");
		final RadioButton rdbEncodingIso = new RadioButton("file encoding", "iso-8859-2");
		rootPanel.add(rdbEncodingIso, 412, 160);
		rdbEncodingIso.setSize("105px", "20px");
		rdbEncodingUtf8.setValue(true);  // default - UTF-8
		
		final FileReader freader = new FileReader();
		freader.addLoadEndHandler( new LoadEndHandler() {
			@Override
			public void onLoadEnd(LoadEndEvent event) {
				String subtext = freader.getStringResult();
				//txtDebug.setText(txtDebug.getText() + subtext);
				processText(subtext);
			}
		} );
		
		final FileUploadExt fileUpload = new FileUploadExt();
		fileUpload.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				FileList fl = fileUpload.getFiles();
				Iterator<File> fit = fl.iterator();
				if (fit.hasNext()) {
					String encoding = "utf-8";  // default value
					if (rdbEncodingUtf8.getValue()) {
						encoding = "utf-8";
					}
					else if (rdbEncodingIso.getValue()) {
						encoding = "iso-8859-2";
					}
					freader.readAsText(fit.next(), encoding);
				}
				else {
					txtDebug.setText(txtDebug.getText() + "No file chosen.\n");
				}
			}
		} );
		rootPanel.add(fileUpload, 10, 98);
		// --- end of file reading interface via lib-gwt-file --- //
		
		
		
		// --- textarea interface for loading whole subtitle file --- //
		final TextArea txtFileContentArea = new TextArea();
		rootPanel.add(txtFileContentArea, 10, 188);
		txtFileContentArea.setSize("260px", "176px");
		
		Button btnSendToTm = new Button("Send to TM");
		btnSendToTm.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String subtext = txtFileContentArea.getText();
				processText(subtext);
			}
		} );
		rootPanel.add(btnSendToTm, 286, 188);
		btnSendToTm.setSize("120px", "47px");
		// --- end of textarea interface --- //		

		

		
		
		txtbxText = new TextBox();
		txtbxText.setText("hi");
		rootPanel.add(txtbxText, 17, 25);
		
		Button btnTranslate = new Button("Translate");
		rootPanel.add(btnTranslate, 57, 73);
		
		translation = new Label("");
		rootPanel.add(translation, 30, 129);
		
		
		
		

	}	// onModuleLoad()
	

	
	/**
	 * Parse the given text in the subtitle format of choice (by the radiobuttons)
	 * into this.sublist (GUISubtitleList).
	 * Currently verbosely outputting both input text, format
	 * and output chunks into the debug-area,
	 * also "reloads" the CellBrowser interface accordingly.
	 * 
	 * @param text Multi-line subtitle text to parse
	 */
	private void processText(String text) {
		// dump the input text into the debug-area:
		txtDebug.setText(txtDebug.getText() + "processing the following input:\n" + text + "\n");
		
		// determine format (from corresponding radiobuttons) and choose parser:
		String subformat;
		Parser subtextparser;
		if (rdbFormatSub.getValue()) {  // i.e. ".sub" is checked
			subformat = "sub";
			subtextparser = new ParserSub();
		}
		else {  // i.e. ".srt" is checked (or something else - probably weird)
			subformat = "srt";
			subtextparser = new ParserSrt();
		}
		txtDebug.setText(txtDebug.getText() + "subtitle format chosen: " + subformat + "\n");
		
		// parse:
		GUISubtitleList mysublist = subtextparser.parse(text);
		
		// output the parsed chunks:
		//txtDebug.setText( Integer.toString( sublist2.getChunks().size()) + "\n");
		txtDebug.setText(txtDebug.getText() + "\nparsed chunks:\n");
		ListIterator<GUIChunk> chunkit = mysublist.getChunks().listIterator();
		while (chunkit.hasNext()) {
			txtDebug.setText(txtDebug.getText() + chunkit.next().toString() + "\n");
		}
		
		
		// reload the CellBrowser interface:
		rootPanel.remove(cellBrowser);
		cellBrowser = new CellBrowser(new SubtitleTreeModel(mysublist), null);
		cellBrowser.setMinimumColumnWidth(154);
		cellBrowser.setDefaultColumnWidth(154);
		cellBrowser.setSize("100%", "100px");
		rootPanel.add(cellBrowser, 0, 502);
		
		sublist = mysublist;
	}	// processText(...)
	
}
