/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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
	private LevelLogEnum level;
	private String context;
	private String message;
	
	/**
	 * Logs the message with the given context to the server,
	 * if its level is high enough (>= LogGuiMessage.logLevel).
	 * @param level The supported levels are DebugNotice, Notice, Warning and Error.
	 * @param context The context specifies the type of the message and should be constant for each instance of a similar message; it can contain e.g.\ a class name, a method name or an RPC name. The message should be as detailed as to provide all necessary information, including e.g.\ values of variables or a stack trace if applicable.
	 * @param message
	 */
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
