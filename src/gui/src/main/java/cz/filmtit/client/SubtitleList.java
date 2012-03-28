package cz.filmtit.client;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

//import com.google.gwt.user.client.Window;

public class SubtitleList {
	private List<GUIChunk> subtitles;
	private ListIterator<GUIChunk> _cursor;
	
	public SubtitleList() {
		subtitles = new ArrayList<GUIChunk>();
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
