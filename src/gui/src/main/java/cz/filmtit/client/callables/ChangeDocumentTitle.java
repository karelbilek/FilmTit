package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.PageHandler;
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
		
		if (newTitle != null && !newTitle.isEmpty()) {
			this.newTitle = newTitle;
			this.documentID = documentID;
			enqueue();
		}
		else {
			Gui.getPageHandler().refresh();
		}
	}

	@Override
	protected void call() {
		filmTitService.changeDocumentTitle(Gui.getSessionID(), documentID, newTitle, this);
	}

	@Override
	public void onSuccessAfterLog(Void returned) {
		Gui.getPageHandler().refresh();
	}
	
	@Override
	protected void onFinalError(String message) {
		Gui.getPageHandler().refresh();
		super.onFinalError(message);
	}

}
