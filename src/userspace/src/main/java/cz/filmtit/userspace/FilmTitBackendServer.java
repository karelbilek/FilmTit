package cz.filmtit.userspace;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.Factory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Document;
import cz.filmtit.share.FilmTitService;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FilmTitBackendServer extends RemoteServiceServlet implements
		FilmTitService {
	
	private static final long serialVersionUID = 3546115L;


    
	private TranslationMemory TM;
    private Map<Long, USDocument> activeDocuments;
    private Map<Long, USTranslationResult> activeTranslationResults;

    public FilmTitBackendServer(/*Configuration configuration*/) {
        TM = Factory.createTM(ConfigurationSingleton.getConf(), true);

        activeDocuments = Collections.synchronizedMap(new HashMap<Long, USDocument>());
        activeTranslationResults = Collections.synchronizedMap(new HashMap<Long, USTranslationResult>());
        System.err.println("FilmTitBackendServer started fine!");
    }

	/*public FilmTitBackendServer() throws Exception {
        this(new Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml")));
    }*/

    public TranslationMemory getTM() {
        return TM;
    }

	public TranslationResult getTranslationResults(TimedChunk chunk) {
		USTranslationResult usTranslationResult = new USTranslationResult(chunk);

        usTranslationResult.generateMTSuggestions(TM);

        // TODO: make maps of maps to deal with the two ID policy
        activeTranslationResults.put(usTranslationResult.getDatabaseId(), usTranslationResult);

        return usTranslationResult.getTranslationResult();
	}

    @Override
    public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID) {
        //USTranslationResult tr = activeTranslationResults.get(translationResultId);
        //tr.setUserTranslation(userTranslation);
        //tr.setSelectedTranslationPairID(chosenTranslationPairID);

        return null;
    }


	public Document createDocument(String movieTitle, String year, String language) {
		USDocument usDocument = new USDocument( new Document(movieTitle, year, language) );
		activeDocuments.put(usDocument.getDatabaseId(), usDocument);
        return usDocument.getDocument();
	}
	
}
