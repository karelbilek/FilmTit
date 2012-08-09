package cz.filmtit.client;

import cz.filmtit.client.dialogs.LoginDialog;
import cz.filmtit.share.exceptions.*;
import cz.filmtit.share.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Represents an RPC with parameters.
 * The method is automatically called on its creation
 * and stored into a queue of active calls
 * so that it can be easily re-called on failure.
 */
public abstract class Callable<T> implements AsyncCallback<T> {
	
	// static members
	
//	static private Dictionary<Integer, Callable> queue;
	
	// static private int newId = 1;
	
	protected static FilmTitServiceAsync filmTitService = GWT.create(FilmTitService.class);
	
	protected static int windowsDisplayed;
	
	// non-static members
	
	protected Gui gui = Gui.getGui();
	
	protected Timer timeOutTimer;
	
	/**
	 * whether the call has already returned
	 */
	protected boolean hasReturned = false;
	
	/**
	 * sets hasReturned to true and cancels the timeout timer.
	 */
	public void setHasReturned() {
		hasReturned = true;
		timeOutTimer.cancel();
	}

	/**
	 * whether the call has timed out (it means that it is invalid)
	 */
	protected boolean hasTimedOut = false;

	/**
	 * the time (in ms) after which the call fails with a timeout exception
	 */
	protected int callTimeOut = 10000;
	
	// protected int id;
	
	protected int retriesOnStatusZero = 3;
	
	protected boolean retryOnStatusZero () {
		retriesOnStatusZero--;
		if (retriesOnStatusZero < 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * number of ms to wait to retry the call
	 */
	protected int waitToRetry = 10;

	/**
	 * creates the RPC
	 */
	public Callable() {
		// this.id = newId++;
	}
	
	/**
	 * invokes the RPC
	 */
	protected abstract void call();
    abstract public String getName();
    abstract public void onSuccessAfterLog(T returned);
    
    public void onFailureAfterLog(Throwable returned) {
        displayWindow(returned.getLocalizedMessage());
    }

    @Override
    public final void onSuccess(T returned) {
    	setHasReturned();
    	if (!hasTimedOut) {
            gui.log("RPC SUCCESS "+getName());
            onSuccessAfterLog(returned);
    	} else {
    		onTimedOutReturn(returned);
    	}
    }

    @Override
    public final void onFailure(Throwable returned) {
    	setHasReturned();
    	if (!hasTimedOut) {
	        if (returned instanceof StatusCodeException && ((StatusCodeException) returned).getStatusCode() == 0) {
	            // this happens if there is no connection to the server, and reportedly in other cases as well
	            if (retryOnStatusZero()) {
	            	// try to send it again
		        	gui.log("RPC " + getName() + " returned with a status code 0, calling again...");
		        	new EnqueueTimer(waitToRetry);
		        	waitToRetry *= 10; // wait 10ms, 100ms, 1000ms...
	            } else {
	            	// stop trying, we might be offline
	        		gui.log("RPC FAILURE " + getName() + " (status code 0)");
		            onProbablyOffline(returned);
	            }
	        } else if (returned.getClass().equals(InvalidSessionIdException.class)) {
	            onInvalidSession();
	            // TODO: store user input to be used when user logs in
	        } else {  
	            gui.log("RPC FAILURE " + getName() + "! " + returned.toString());
	            // the stacktrace is actually hardly-ever useful for anything in these external exceptions
	            // so probably the exception name and message is just enough
	            // gui.exceptionCatcher(returned, false);
	            onFailureAfterLog(returned);
	        }
    	} else {
    		onTimedOutReturn(returned);
    	}
    }

    protected void onProbablyOffline(Throwable returned) {
		displayWindow(
				"There seems to be no response from the server. " +
				"Either your computer is offline " +
				"or the server is down or overloaded. " +
				"Please try again later or ask the administrators."
			);
		// TODO: use some ping to find out whether user is offline
		// TODO: store user input to be used when user goes back online
	}
    
    /**
     * Called when there is an error because the sessionID is invalid.
     * Displays the login dialog by default.
     */
	protected void onInvalidSession() {
        Gui.logged_out ();
        new LoginDialog(Gui.getUsername(), "You have not logged in or your session has expired. Please log in.");
	}    
    
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
    
    protected class EnqueueTimer extends Timer {
		/**
		 * sets the timer to call enqueue() after the given number of miliseconds
		 */
    	public EnqueueTimer(int ms) {
			schedule(ms);
		}
		
		@Override
		public void run() {
			enqueue();
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
    
    final protected void timeOut() {
    	if (!hasReturned) {
    		hasTimedOut = true;
    		gui.log("RPC " + getName() + " TIMED OUT after " + callTimeOut + "ms");
    		onTimeOut();
    	}
    }
    
    protected void onTimeOut() {
    	onFailure(new Throwable("The call timed out because the server didn't send a response for " + (callTimeOut/1000) + " seconds."));
    }
	
    final protected void onTimedOutReturn(Object returned) {
        gui.log("TIMED OUT RPC " + getName() + " RETURNED WITH " + returned);
        onTimedOutReturnAfterLog(returned);
    }
    
    protected void onTimedOutReturnAfterLog(Object returned) {
		// ignore by default
	}

	/**
	 * enqueues the object and invokes the RPC
	 */
	public final void enqueue() {
//		queue.put(id, this);
		hasReturned = false;
		hasTimedOut = false;
		setTimer();
		call();
	}
	
	/**
	 * called after successful completion of RPC
	 */
	public final void dequeue() {
//		queue.remove(id);
	}
	
    
    /**
     * display a widow with an error message
     * unless maximum number of error messages has been reached
     * @param string
     */
    public void displayWindow(String message) {
        if (windowsDisplayed < 10) {
            windowsDisplayed++;
            Window.alert(message);
            if (windowsDisplayed==10) {
                Window.alert("Last window displayed.");
            }
        } else {
      //      gui.log("ERROR - message");
        }
    }
        
}
