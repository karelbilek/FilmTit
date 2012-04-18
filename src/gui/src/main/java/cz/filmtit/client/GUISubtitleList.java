package cz.filmtit.client;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import cz.filmtit.share.*;



//import com.google.gwt.user.client.Window;

public class GUISubtitleList {
	private List<GUIChunk> subtitles;
	private ListIterator<GUIChunk> _cursor;
	
	public GUISubtitleList() {
		subtitles = new ArrayList<GUIChunk>();
		_cursor = subtitles.listIterator();
	}
	
	/**
	 * Creates the GUISubtitleList from the list of chunks from
	 * the shared structure Document.
	 * @param shareddocument
	 */
	public GUISubtitleList(Document shareddocument) {
		subtitles = new ArrayList<GUIChunk>();
		ListIterator<Chunk> dociterator = shareddocument.chunks.listIterator();
		while (dociterator.hasNext()) {
			subtitles.add( new GUIChunk(dociterator.next()) );
		}		
		_cursor = subtitles.listIterator();
	}
	
	public GUIChunk getNextSubtitleChunk() {
		if (_cursor.hasNext()) {		
			return _cursor.next();
		}
		else {
			//Window.alert("no next subtitle");
			return GUIChunk.NoNextSubtitle;
		}
	}
	
	public GUIChunk getSubtitleChunkAt(int index) {
		return subtitles.get(index);
	}
	
	public List<GUIChunk> getChunks() {
    	return subtitles;
    }

	public List<String> getChunksAsStrings() {
		List<String> allSources = new ArrayList<String>();
		ListIterator<GUIChunk> li = subtitles.listIterator();
		while (li.hasNext()) {
			allSources.add(li.next().getChunkText());
		}
		return allSources;
	}
	
	
	public void addSubtitleChunk(GUIChunk chunk) {
		subtitles.add(chunk);
	}
	
}
