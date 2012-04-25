package cz.filmtit.server;

import cz.filmtit.client.FilmTitService;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServiceImpl extends RemoteServiceServlet implements
		FilmTitService {

	public TranslationResult suggestions(TranslationResult chunk) {
		
		USTranslationResult uschunk = new USTranslationResult(chunk);
				
		if(uschunk.getText().equals("hi")) {
			uschunk.setUserTranslation("ahoj");
		} else if (chunk.text.equals("bye")) {
			uschunk.setUserTranslation("čau");
		} else if (chunk.text.equals("platypus")) {
			uschunk.setUserTranslation("ptakopysk");
		} else {
			uschunk.setUserTranslation("no translation");
		}

		return uschunk.getSharedChunk();
	}

	public void feedback (long translationResultId, long chosenTranslationPair, String userTranslation) {
		
	}
	
}
