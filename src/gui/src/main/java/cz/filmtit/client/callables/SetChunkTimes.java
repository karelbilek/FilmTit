package cz.filmtit.client.callables;

import com.google.gwt.user.client.Window;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.TimedChunk;

/**
 * Updated the chunk's start time and end time in the UserSpace database.
 * @author rur
 */
public class SetChunkTimes extends Callable<Void> {

	private TimedChunk chunk;
	
	@Override
	public String getName() {
		return getNameWithParameters(chunk.getChunkIndex(), chunk.getStartTime(), chunk.getEndTime());
	}
	
	
	/**
	 * Updated the chunk's start time and end time in the UserSpace database.
	 */
	public SetChunkTimes(TimedChunk chunk) {
		// TODO: change to SetChunkTimes(TimedChunk... chunks)
		super();
		
		this.chunk = chunk;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.setChunkTimes(Gui.getSessionID(), chunk.getChunkIndex(), chunk.getDocumentId(), chunk.getStartTime(), chunk.getEndTime(), this);
	}

}
