package cz.filmtit.userspace;

import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServiceImpl extends RemoteServiceServlet implements
		FilmTitService {

	public TranslationResult getTranslationResults(TimedChunk chunk) {
		
		// TODO: get TranslationPairs from core

		USTranslationResult usTranslationResult = new USTranslationResult(chunk);
				
		if(usTranslationResult.getText().equals("hi")) {
			usTranslationResult.setUserTranslation("ahoj");
		} else if (usTranslationResult.getText().equals("bye")) {
			usTranslationResult.setUserTranslation("čau");
		} else if (usTranslationResult.getText().equals("platypus")) {
			usTranslationResult.setUserTranslation("ptakopysk");
		} else {
			usTranslationResult.setUserTranslation("no translation");
		}

		return usTranslationResult.getTranslationResult();
	}

	public Void setUserTranslation(long translationResultId, String userTranslation, long chosenTranslationPair) {
		// TODO: pass feedback to core
		
		return null;		
	}
	
}
