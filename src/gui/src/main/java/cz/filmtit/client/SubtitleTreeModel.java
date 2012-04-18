package cz.filmtit.client;

import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Represents a tree data model created from given SubtitleList,
 * it is meant for backing up a CellBrowser (or alternatively TreeBrowser).
 * 
 * @author Honza Václ
 */

public class SubtitleTreeModel implements TreeViewModel {

	private GUISubtitleList sublist;
	
	public SubtitleTreeModel(GUISubtitleList sublist) {
		this.sublist = sublist;
	}
	
	// main method for the node visualization
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		if (value == null) {
			// 1st column - getting all chunks from sublist
			ListDataProvider<GUIChunk> dataProvider
				= new ListDataProvider<GUIChunk>(sublist.getChunks());
			Cell<GUIChunk> cell = new AbstractCell<GUIChunk>() {
				@Override
				public void render(Context context, GUIChunk value, SafeHtmlBuilder sb) {
					if (value != null) {
						sb.appendEscaped(value.getChunkText());
					}
				}
			};
			return new DefaultNodeInfo<GUIChunk>(dataProvider, cell);
		}
		else if (value instanceof GUIChunk) {
			// 2nd column - getting all matches from the chunk (selected in 1st column)
			ListDataProvider<GUIMatch> dataProvider
				= new ListDataProvider<GUIMatch>(((GUIChunk) value).getMatches());
			Cell<GUIMatch> cell = new AbstractCell<GUIMatch>() {
				@Override
				public void render(Context context, GUIMatch value, SafeHtmlBuilder sb) {
					if (value != null) {
						sb.appendEscaped(value.getMatchText());
					}
				}
			};
			return new DefaultNodeInfo<GUIMatch>(dataProvider, cell);
		}
		else if (value instanceof GUIMatch) {
			// 3rd column - getting all translations from the match (selected in 2nd column)
			// ...and rendering them as editable Strings (TextInputCells):
			AbstractDataProvider<String> dataProvider
				= new ListDataProvider<String>(((GUIMatch) value).getTranslationsAsStrings());
			TextInputCell cell = new TextInputCell();
			return new DefaultNodeInfo<String>(dataProvider, cell);

			/*
			// or - handling the whole GUITranslations?
			AbstractDataProvider<GUITranslation> dataProvider
				= new ListDataProvider<GUITranslation>(((GUIMatch) value).getTranslations());
			
			AbstractInputCell<GUITranslation, TextInputCell.ViewData> cell = new AbstractInputCell<GUITranslation, TextInputCell.ViewData>() {
				@Override
				public void render(Context context, GUITranslation value, SafeHtmlBuilder sb) {
					if (value != null) {
						sb.appendEscaped(value.getTranslationText());
					}
				}
			};
			return new DefaultNodeInfo<GUITranslation>(dataProvider, cell);
			*/

		}

		return null;
	}	// getNodeInfo(...)
	
	@Override
	public boolean isLeaf(Object value) {
		// the leaf nodes are the translations as Strings(?)
		if (value instanceof String) {
			return true;
		}
		return false;
	}
	
}
