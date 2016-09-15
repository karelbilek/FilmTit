/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.Document;
import cz.filmtit.share.TranslationResult;
import java.util.List;

/**
 *
 * @author matus
 */
public class ReloadTranslationResults extends Callable<Document>{
    
    private long documentId;
    private TranslationWorkspace workspace;
    
    public ReloadTranslationResults() {
        
    }
    
    public ReloadTranslationResults(long documentId, TranslationWorkspace workspace) {
        super();
        this.documentId = documentId;
        this.workspace = workspace;
        enqueue();
    }
    
    @Override
    public void onSuccessAfterLog(Document result) {
        List<TranslationResult> translations = result.getSortedTranslationResults();
        workspace.fillTranslationResults(translations);
    }

    @Override
    protected void call() {
        filmTitService.reloadTranslationResults(documentId, this);
    }
    
}
