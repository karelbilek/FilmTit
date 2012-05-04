package cz.filmtit.userspace;

import java.io.File;

import cz.filmtit.share.Document;
import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.*;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServiceImpl extends RemoteServiceServlet implements
		FilmTitService {
	
	private static final long serialVersionUID = 3546115L;
	
	private TranslationMemory TM;
	
	public FilmTitServiceImpl() {
		// TODO Auto-generated constructor stub
		Configuration configuration = new Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml")); 
        TranslationMemory TM = Factory.createTM(configuration, true);
	}

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

	public Document createDocument(String movieTitle, int year, String language) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
