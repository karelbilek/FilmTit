package cz.filmtit.client;

import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Represents a tree data model created from given SubtitleList,
 * it is meant for backing up a CellBrowser (or alternatively TreeBrowser).
 * 
 * @author Honza Václ
 */

public class SubtitleTreeModel implements TreeViewModel {

	private SubtitleList sublist;
	
	public SubtitleTreeModel(SubtitleList sublist) {
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
			ListDataProvider<GUITranslation> dataProvider = new ListDataProvider<GUITranslation>(((GUIMatch) value).getTranslations());
			Cell<GUITranslation> cell = new AbstractCell<GUITranslation>() {
				@Override
				public void render(Context context, GUITranslation value, SafeHtmlBuilder sb) {
					if (value != null) {
						sb.appendEscaped(value.getTranslationText());
					}
				}
			};
			return new DefaultNodeInfo<GUITranslation>(dataProvider, cell);
		    /*
			ListDataProvider<String> dataProvider
				= new ListDataProvider<String>(((GUIMatch) value).getTranslationsAsStrings());
			return new DefaultNodeInfo<String>(dataProvider, new TextCell(), selectionModel, null);
			*/
		}

		return null;
	}	// getNodeInfo(...)
	
	@Override
	public boolean isLeaf(Object value) {
		// the leaf nodes are the translations
		if (value instanceof GUITranslation) {
			return true;
		}
		return false;
	}
	
}
