package cz.filmtit.server;

import cz.filmtit.client.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServiceImpl extends RemoteServiceServlet implements
		FilmTitService {

	public TranslationResult suggestions(TimedChunk chunk) {
		
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

	public void feedback (long translationResultId, long chosenTranslationPair, String userTranslation) {
		
	}
	
}
