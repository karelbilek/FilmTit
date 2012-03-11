package filmtit0.client;

import java.util.ArrayList;
import java.util.ListIterator;

//import com.google.gwt.user.client.Window;

public class SubtitleList {
	public ArrayList<SubtitleChunk> subtitles;
	private ListIterator<SubtitleChunk> _cursor;
	
	public SubtitleList() {
		subtitles = new ArrayList<SubtitleChunk>();
		_cursor = subtitles.listIterator();
	}
	
	public SubtitleChunk getNextSubtitleChunk() {
		if (_cursor.hasNext()) {		
			return _cursor.next();
		}
		else {
			//Window.alert("no next subtitle");
			return SubtitleChunk.NoNextSubtitle;
		}
	}
	
	public void addSubtitleChunk(SubtitleChunk chunk) {
		subtitles.add(chunk);
	}
	
}
