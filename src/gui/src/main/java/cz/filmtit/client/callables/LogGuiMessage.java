package cz.filmtit.client.callables;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.share.LevelLogEnum;

public class LogGuiMessage extends Callable<Void> {

	// parameters
	LevelLogEnum level;
	String context;
	String message;
	
	@Override
	public String getName() {
		return getNameWithParameters(level, context, message);
	}
	
	public LogGuiMessage(LevelLogEnum level, String context, String message) {
		super();
		
		this.level = level;
		this.context = context;
		this.message = message;
		
		enqueue();
	}

	@Override
	protected void call() {
		filmTitService.logGuiMessage(level, context, message, Gui.getSessionID(), this);
	}

	@Override
	protected boolean onEachReturn(Object returned) {
		// ignore anything and stop processing of the call
		return false;
	}

}
