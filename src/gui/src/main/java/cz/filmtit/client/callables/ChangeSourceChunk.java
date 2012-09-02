package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;

/**
 * Change the source text of the chunk,
 * resulting in new translation suggestions
 * which are sent as the result
 * and shown in the Translation Workspace.
 * @author rur
 *
 */
public class ChangeSourceChunk extends Callable<TranslationResult> {

	private TimedChunk chunk;
	private String newText;
	private TranslationWorkspace workspace;
	
	@Override
	public String getName() {
		return getNameWithParameters(chunk, newText);
	}
	
	/**
	 * Change the source text of the chunk,
	 * resulting in new translation suggestions
	 * which are sent as the result
	 * and shown in the Translation Workspace.
	 */
	public ChangeSourceChunk(TimedChunk chunk, String newText, TranslationWorkspace workspace) {
		super();
		
		this.chunk = chunk;
		this.newText = newText;
		this.workspace = workspace;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.changeText(Gui.getSessionID(), chunk, newText, this);
	}

	@Override
	public void onSuccessAfterLog(TranslationResult result) {
		workspace.showResult(result);
	}

}
