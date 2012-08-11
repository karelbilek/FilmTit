package cz.filmtit.client.callables;

import com.google.gwt.user.client.Window;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.share.ChunkIndex;
import cz.filmtit.share.TimedChunk;

public class SetChunkTimes extends Callable<Void> {

	private TimedChunk chunk;
	
	@Override
	public String getName() {
		return "setChunkTimes(" + chunk.getChunkIndex() + " " + chunk.getStartTime() + " - " + chunk.getEndTime() + ")";
	}
	
	
	public SetChunkTimes(TimedChunk chunk) {
		super();
		
		this.chunk = chunk;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.setChunkTimes(Gui.getSessionID(), chunk.getChunkIndex(), chunk.getDocumentId(), chunk.getStartTime(), chunk.getEndTime(), this);
	}

	@Override
	public void onSuccessAfterLog(Void returned) {
		// TODO Auto-generated method stub
	}

}
