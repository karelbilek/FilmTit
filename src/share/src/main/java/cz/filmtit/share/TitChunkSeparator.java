package cz.filmtit.share;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;

import java.util.ArrayList;
import java.util.List;

public class TitChunkSeparator {

	public static final String SUBLINE_SEPARATOR_OUT = " | ";
	
	public static final RegExp indirectSplitter = RegExp.compile(" *\\| *");
	public static final RegExp dialogSegmenter = RegExp.compile("^ ?- ?[a-zA-Z0-9]");
	
	// formatting tags - in srt e.g. "<i>", in sub e.g. "{Y:i}"
	public static final RegExp formatTag = RegExp.compile("(<[^>]*>)|({[^}]*})", "g");  // the "{}" are here as literals


	public static List<String> separate(String tit) {
		// remove formatting tags
		tit = formatTag.replace(tit, "");

		SplitResult lines = indirectSplitter.split(tit);

		List<String> resultChunks = new ArrayList<String>();
		String intermediateChunk = lines.get(0);
		for (int i = 1; i < lines.length(); i++) {
			String line = lines.get(i);

			if (dialogSegmenter.test(line)) {
				// is a dialog line -> splitting
				resultChunks.add(intermediateChunk);
				intermediateChunk = line;
			}
			else {
				intermediateChunk += SUBLINE_SEPARATOR_OUT + line;
			}
		}
		resultChunks.add(intermediateChunk);
		
		return resultChunks;
	}

}
