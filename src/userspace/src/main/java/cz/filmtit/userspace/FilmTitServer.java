package cz.filmtit.userspace;

import java.io.File;
import java.util.*;

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
    private static FilmTitServer instance;
	
	private TranslationMemory TM;
    private Map<Long, USDocument> activeDocuments;
    private Map<Long, USTranslationResult> activeTranslationResults;
	
	private FilmTitServer() {
		Configuration configuration = new Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml")); 
        TranslationMemory TM = Factory.createTM(configuration, true);

        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, USTranslationResult>());
	}

    public static FilmTitServer getInstance() {
        if (instance == null) { instance = new FilmTitServer(); }
        return instance;
    }

    public TranslationMemory getTM() {
        return TM;
    }

	public TranslationResult getTranslationResults(TimedChunk chunk) {
		USTranslationResult usTranslationResult = new USTranslationResult(chunk);

        usTranslationResult.generateMTSuggestions(TM);
        activeTranslationResults.put(usTranslationResult.getDatabaseId(), usTranslationResult);

        return usTranslationResult.getTranslationResult();
	}

	public Void setUserTranslation(long translationResultId, String userTranslation, long chosenTranslationPairID) {
	    USTranslationResult tr = activeTranslationResults.get(translationResultId);
        tr.setUserTranslation(userTranslation);
        tr.setSelectedTranslationPairID(chosenTranslationPairID);

        return null;
	}

	public Document createDocument(String movieTitle, String year, String language) {
		USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
		activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        return usDocument.getDocument();
	}
	
}
