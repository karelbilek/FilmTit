package cz.filmtit.userspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import cz.filmtit.share.Document;
import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationPair;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.userspace.*;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServer extends RemoteServiceServlet implements
		FilmTitService {
	
	private static final long serialVersionUID = 3546115L;
	
	private TranslationMemory TM;
	
	public FilmTitServer() {
		Configuration configuration = new Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml")); 
        // TranslationMemory TM = Factory.createTM(configuration, true);
	}

	public TranslationResult getTranslationResults(TimedChunk chunk) {
		
		// TODO: get TranslationPairs from core

		USTranslationResult usTranslationResult = new USTranslationResult(chunk);
		
		// TODO: use this:
		// generateMTSuggestions(TranslationMemory TM)
		
		/*
		// TODO: remove this:
		if(usTranslationResult.getText().equals("hi")) {
			usTranslationResult.setUserTranslation("ahoj");
		} else if (usTranslationResult.getText().equals("bye")) {
			usTranslationResult.setUserTranslation("čau");
		} else if (usTranslationResult.getText().equals("platypus")) {
			usTranslationResult.setUserTranslation("ptakopysk");
		} else {
			usTranslationResult.setUserTranslation("no translation");
		}
		*/
		
		// TODO: remove this:
		ArrayList<TranslationPair> tms = new ArrayList<TranslationPair>();
		tms.add(new TranslationPair("platypus", "ptakopysk"));
		usTranslationResult.getTranslationResult().setTmSuggestions(tms);

        return usTranslationResult.getTranslationResult();
	}

	public Void setUserTranslation(long translationResultId, String userTranslation, long chosenTranslationPair) {
		// TODO: store TranslationResult to DB
		// TODO: pass feedback to core
		
		return null;		
	}

	public Document createDocument(String movieTitle, int year, String language) {
		// TODO: check with Jindra what to actually do in this method
		USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );		
		// TODO: DatabaseId should be generated automatically upon new USDocument()
		usDocument.setDatabaseId(1234);
		return usDocument.getDocument();
	}
	
}
