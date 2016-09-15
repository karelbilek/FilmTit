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
package cz.filmtit.client;

import java.util.HashMap;
import java.util.LinkedList;

import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.exceptions.*;
import cz.filmtit.share.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Represents an RPC with parameters. The method is automatically called on its
 * creation.
 */
public abstract class Callable<T> implements AsyncCallback<T> {

    // static members
    /**
     * Provides access to the FilmTitService interface for actually invoking the
     * calls.
     */
    public static FilmTitServiceAsync filmTitService = GWT.create(FilmTitService.class);

    /**
     * Number of error windows displayed until now.
     *
     * @see displayWindow()
     */
    protected static int windowsDisplayed;

    /**
     * Queue of calls to be invoked later.
     *
     * @see addCallToBeCalled(), callCallsToBeCalled()
     */
    protected static HashMap<Long, LinkedList<Callable<?>>> toBeCalled = new HashMap<Long, LinkedList<Callable<?>>>();

    // non-static members
    /**
     * The timer of the call. If it fires before the call returns, the call is
     * considered to be timed out.
     */
    protected Timer timeOutTimer;

    /**
     * whether the call has already returned successfully
     */
    protected boolean hasReturned = false;

    /**
     * the time (in ms) after which the call fails with a timeout exception
     * (defaults to 30s)
     */
    protected int callTimeOut = 30000;

    /**
     * Number of allowed retries
     */
    protected int retries = 4;

    /**
     * Number of ms to wait to retry the call; is multiplied by 10 on each
     * retry.
     */
    protected int waitToRetry = 5;

    // constructor
    /**
     * creates the RPC
     */
    public Callable() {
        // this.id = newId++;
    }

    // methods that must be overriden
    /**
     * Invokes the call. Not to be called directly, please use enqueue().
     */
    protected abstract void call();

    // methods that can be overriden
    /**
     * Processes the result of a successful return of the call. Nothing done by
     * default.
     *
     * @param returned
     */
    public void onSuccessAfterLog(T returned) {
        // on default do nothing and just be happy
    }

    /**
     * The name shown in logs. Should typically have the form of
     * ClassName(parameter1, parameter2). The default implementation is
     * sufficient for calls with no parameters. An override is expected to have
     * the form of: <br>
     * <code>return getNameWithParameters(parameter1, parameter2);</code>
     */
    public String getName() {
        return getClassName();
    }

    /**
     * Returns the name of this class, without any packages etc. To be used in
     * getName(). Can be overridden especially if the real class name does not
     * correspond to the name of the call as defined in FilmTitService.
     */
    protected String getClassName() {
        String fullClassName = this.getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    }

    /**
     * Called when the call returns, no matter whether successfully or not;
     * called exactly once for each call. (So it is roughly equivalent to
     * "finally", but it is called before anything else, so basically it is more
     * of "firstly".) If the call times out, this method is called at the moment
     * of its timeout (and not when (if) it eventually returns). Nothing done by
     * default.
     *
     * @return true if processing of the call should continue (the default),
     * false if it should be stopped
     */
    protected boolean onEachReturn(Object returned) {
        return true;
    }

    /**
     * Called if there is an error and we can do nothing about it. Only displays
     * the error message by default. Can be overridden especially to do some
     * cleaning, reseting, etc.
     *
     * @param message
     */
    protected void onFinalError(String message) {
        displayWindow(message);
    }

    /**
     * Called when there is a generic error on return of the RPC. (I.e. neither
     * an invalid session ID nor are we probably offline.) By default retries
     * the call if possible, or displays the error message using displayWindow()
     * and exits if not.
     *
     * @param returned
     */
    public void onFailureAfterLog(Throwable returned) {
        // try to retry
        if (!retry()) {
            onFinalError(returned.getLocalizedMessage());
        }
    }

    /**
     * Called when it seems that the user is offline and the allowed number of
     * retries has been exhausted. By default displays an error message and
     * exits.
     *
     * @param returned
     */
    protected void onProbablyOffline(Throwable returned) {
        onFinalError(
                "There seems to be no response from the server. "
                + "Either your computer is offline "
                + "or the server is down or overloaded. "
                + "Please try again later or ask the administrators."
        );
        // TODO: store user input to be used when user goes back online
    }

    /**
     * Called when there is an error because the sessionID is invalid. Displays
     * the login dialog by default.
     */
    protected void onInvalidSession() {
        // store to retry after reloging in
        addCallToBeCalled();

        Gui.logged_out();
        new LoginDialog(Gui.getUsername(), "You have not logged in or your session has expired. Please log in.");
    }

    /**
     * Called when the call times out. Call onFinalError() by default.
     */
    protected void onTimeOut() {
        onFinalError("The call timed out because the server didn't send a response for " + (callTimeOut / 1000) + " seconds.");
    }

    /**
     * Called when a call returns, either successfully or not, after already
     * having returned successfully once (this can happen because of timeouts).
     * Ignored by default.
     */
    protected void onTimedOutReturnAfterLog(Object returned) {
        // ignore by default
    }

    // final methods
    /**
     * Constructs the name of the call, in the form of ClassName(parameter1,
     * parameter2), using getClassName() for ClassName and toString() on
     * parameters. Should be used to implement getName() in case the call has
     * some parameters.
     *
     * @param parameters
     * @return
     */
    protected final String getNameWithParameters(Object... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClassName());
        if (parameters != null && parameters.length > 0) {
            sb.append('(');
            boolean prependComma = false;
            for (Object object : parameters) {
                if (prependComma) {
                    sb.append(',');
                }
                prependComma = true;
                if (object == null) {
                    sb.append("null");
                } else {
                    sb.append(object.toString());
                }
            }
            sb.append(')');
        }

        return sb.toString();
    }

    /**
     * Prepares the call to be invoked and invokes it.
     */
    public final void enqueue() {
        if (!hasReturned) {
            Gui.log(LevelLogEnum.DebugNotice, "RPC ENQUEUE", getName());
            setTimer();
            call();
        }
    }

    /**
     * invoked by the RPC mechanism if the call returns successfully
     */
    @Override
    public final void onSuccess(T returned) {
        timeOutTimer.cancel();
        if (!hasReturned) {
            hasReturned = true;
            Gui.log(LevelLogEnum.DebugNotice, "RPC SUCCESS", getName());
            if (onEachReturn(returned)) {
                onSuccessAfterLog(returned);
            }
        } else {
            // has returned successfully for the second time
            Gui.log(LevelLogEnum.DebugNotice, "RPC TIMEOUT RETURN", getName() + " (returned with " + returned + ")");
            onTimedOutReturnAfterLog(returned);
        }
    }

    /**
     * invoked by the RPC mechanism if the call returns unsuccessfully
     *
     * @param returned An exception thrown by the server
     */
    @Override
    public final void onFailure(Throwable returned) {
        timeOutTimer.cancel();
        if (!hasReturned) {
            Gui.log(LevelLogEnum.Warning, "RPC FAILURE", getName() + " WITH " + returned.toString());
            
            if (onEachReturn(returned)) {
                if (returned instanceof StatusCodeException && ((StatusCodeException) returned).getStatusCode() == 0) {
                    // this happens if there is no connection to the server, and reportedly in other cases as well
                    // try to send it again
                    if (!retry()) {
                        // stop trying, we might be offline
                        onProbablyOffline(returned);
                    }
                } else if (returned instanceof InvalidSessionIdException) {
                    // the user session is no longer valid
                    onInvalidSession();
                } else // onFailureAfterLog()
                if (returned instanceof StatusCodeException && ((StatusCodeException) returned).getStatusCode() == 500) {
                    // server error
                    Throwable serverError = new Throwable(
                            "An unexpected error occured on the server. Please try again. If the problem persists, please contact the administrators.",
                            returned);
                    onFailureAfterLog(serverError);
                } else {
                    // a "generic" failure
                    onFailureAfterLog(returned);
                }
            }
        } else {
            // has failed after already having returned successfully
            Gui.log(LevelLogEnum.DebugNotice, "RPC TIMEOUT RETURN", getName() + " (returned with " + returned + ")");
            onTimedOutReturnAfterLog(returned);
        }
    }

    /**
     * Retries if number of allowed retries has not been exhausted.
     *
     * @return true if retried, false if number of allowed retries has been
     * exhausted.
     */
    protected final boolean retry() {
        if (retries <= 0) {
            Gui.log(LevelLogEnum.Error, "RPC NORETRY", getName() + " (retry attempts exhausted)");
            return false;
        } else {
            retries--;
            Gui.log(LevelLogEnum.Notice, "RPC RETRY", getName() + " IN " + waitToRetry + "ms (" + retries + " retries left)");
            new EnqueueTimer(waitToRetry);
            waitToRetry *= 10; // wait 5ms, 50ms, 0.5s, 5s
            return true;
        }
    }

    /**
     * sets the timer to call timeOut() after callTimeOut miliseconds
     */
    final protected void setTimer() {
        if (timeOutTimer != null) {
            timeOutTimer.cancel();
        }
        timeOutTimer = new CallTimer();
    }

    /**
     * Called when the call times out.
     */
    final protected void timeOut() {
        if (!hasReturned) {
            Gui.log(LevelLogEnum.Error, "RPC TIMEOUT", getName() + " (after " + callTimeOut + "ms)");
            if (onEachReturn("TIMEOUT")) {
                onTimeOut();
            }
        } else {
            assert false : "has already returned, so the timer should already have been cancelled";
        }
    }

    /**
     * Display a widow with an error message unless maximum number of error
     * messages has been reached.
     *
     * @param message
     */
    final public void displayWindow(String message) {
        if (windowsDisplayed < 10) {
            windowsDisplayed++;
            Window.alert(message);
            if (windowsDisplayed == 10) {
                Window.alert("Last window displayed.");
            }
        } else {
            //      Gui.log("ERROR - message");
        }
    }

    /**
     * Store the call in Callable.toBeCalled for future execution.
     */
    final protected void addCallToBeCalled() {
        if (!toBeCalled.containsKey(Gui.getUserID())) {
            toBeCalled.put(Gui.getUserID(), new LinkedList<Callable<?>>());
        }
        toBeCalled.get(Gui.getUserID()).add(this);
    }

    /**
     * Call all calls stored in Callable.toBeCalled for future execution.
     */
    final public static void callCallsToBeCalled() {
        if (toBeCalled.containsKey(Gui.getUserID())) {
            Scheduler.get().scheduleIncremental(new RepeatingCommand() {
                public boolean execute() {
                    if (toBeCalled.get(Gui.getUserID()).isEmpty()) {
                        return false;
                    } else {
                        Callable<?> callable = toBeCalled.get(Gui.getUserID()).remove();
                        callable.enqueue();
                        return true;
                    }
                }
            });
        }
    }

    // timers
    /**
     * The timer of the call. If it fires before the call returns, the call is
     * considered to be timed out.
     */
    protected class CallTimer extends Timer {

        /**
         * sets the timer to call timeOut() after callTimeOut miliseconds
         */
        public CallTimer() {
            schedule(callTimeOut);
        }

        @Override
        public void run() {
            timeOut();
        }
    }

    /**
     * A timer for post-poning of enqueueing the call.
     *
     * @see retry()
     */
    protected class EnqueueTimer extends Timer {

        /**
         * sets the timer to call enqueue() after the given number of
         * miliseconds
         */
        public EnqueueTimer(int ms) {
            schedule(ms);
        }

        @Override
        public void run() {
            enqueue();
        }
    }

}
