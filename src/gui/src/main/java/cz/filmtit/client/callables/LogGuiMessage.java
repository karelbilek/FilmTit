package cz.filmtit.client.callables;

import com.google.gwt.user.client.rpc.AsyncCallback;

import cz.filmtit.client.Callable;
import cz.filmtit.client.Gui;
import cz.filmtit.share.LevelLogEnum;

/**
 * Logs the message with the given context to the server,
 * if its level is high enough (>= LogGuiMessage.logLevel).
 * Is the only call not extended from Callable<T>
 * (because it is also used for logging in the other calls,
 * which could lead to cycles in logging).
 * @author rur
 *
 */
public class LogGuiMessage implements AsyncCallback<Void> {

	// settings
	// TODO: enable changing them somehow
	/**
	 * The minimum level of a message to be logged.
	 */
	public static LevelLogEnum logLevel = LevelLogEnum.Warning;
	
	// parameters
	LevelLogEnum level;
	String context;
	String message;
	
	public LogGuiMessage(LevelLogEnum level, String context, String message) {
		super();
		
		if (level.compareTo(logLevel) >= 0) {
			this.level = level;
			this.context = context;
			this.message = message;
			
			call();
		}
		else {
			// not logged to server, the level is too low
		}
	}

	protected void call() {
		Callable.filmTitService.logGuiMessage(level, context, message, Gui.getSessionID(), this);
	}

	public void onFailure(Throwable caught) {
		// ignore
	}

	public void onSuccess(Void result) {
		// ignore
	}


}
