package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler;
import cz.filmtit.client.PageHandler.Page;
import cz.filmtit.share.Document;

public class ChangeDocumentTitle extends Callable<Void> {

	private long documentID;
	private String newTitle;
	
	@Override
	public String getName() {
		return getNameWithParameters(documentID, newTitle);
	}
	
	public ChangeDocumentTitle(long documentID, String newTitle) {
		super();
		
		if (newTitle == null || newTitle.isEmpty()) {
			// we don't accept the new title - refresh to load the old title
			Gui.getPageHandler().refresh();
		}
		else {
			this.newTitle = newTitle;
			this.documentID = documentID;
			enqueue();
		}
	}

	@Override
	protected void call() {
		filmTitService.changeDocumentTitle(Gui.getSessionID(), documentID, newTitle, this);
	}

	@Override
	public void onSuccessAfterLog(Void returned) {
		// don't refresh since the new title is already there
		// Gui.getPageHandler().refreshIf(Page.UserPage)
	}
	
	@Override
	protected void onFinalError(String message) {
		// we don't accept the new title - refresh to load the old title
		Gui.getPageHandler().refreshIf(Page.UserPage);
		super.onFinalError(message);
	}

}
