package cz.filmtit.client;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import cz.filmtit.share.*;



//import com.google.gwt.user.client.Window;

public class GUITranslationResultList {
	private ArrayList<TranslationResult> translationResults;
	private ListIterator<TranslationResult> _cursor;
	
	public GUITranslationResultList() {
		translationResults = new ArrayList<TranslationResult>();
		_cursor = translationResults.listIterator();
	}
	
	/**
	 * Creates the GUISubtitleList from the list of chunks from
	 * the shared structure Document.
	 * @param shareddocument
	 */
	public GUITranslationResultList(Document shareddocument) {
		translationResults = new ArrayList<TranslationResult>();
		ListIterator<TranslationResult> dociterator = shareddocument.translationResults.listIterator();
		while (dociterator.hasNext()) {
			//subtitles.add( new GUIChunk(dociterator.next()) );
			translationResults.add( dociterator.next() );
		}		
		_cursor = translationResults.listIterator();
	}
	
	public TranslationResult getNextTranslationResult() {
		if (_cursor.hasNext()) {		
			return _cursor.next();
		}
		else {
			//Window.alert("no next subtitle");
			//return TranslationResult.NoNextSubtitle;
			return null; 
		}
	}
	
	public TranslationResult getTranslationResultAt(int index) {
		return translationResults.get(index);
	}
	
	public List<TranslationResult> getTranslationResults() {
    	return translationResults;
    }

	/*public List<String> getTranslationResultsAsStrings() {
		List<String> allSources = new ArrayList<String>();
		ListIterator<TranslationResult> li = translationResults.listIterator();
		while (li.hasNext()) {
			allSources.add(li.next().getUserTranslation());
		}
		return allSources;
	}*/
	
	
	public void addTranslationResult(TranslationResult translationResult) {
		// TODO: can arrive in various order -> has to be put on correct position in translationResults
		translationResults.add(translationResult);
	}
	
}
