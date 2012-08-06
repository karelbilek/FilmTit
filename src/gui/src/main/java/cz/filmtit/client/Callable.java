package cz.filmtit.client;

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
	
	static private int newId = 1;
	
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
	
	int id;

	/**
	 * creates the RPC
	 */
	public Callable() {
		this.id = newId++;
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
	            return;
	        } else if (returned.getClass().equals(InvalidSessionIdException.class)) {
	            gui.please_relog_in();
	            // TODO: store user input to be used when user logs in
	        } else {  
	            gui.log("RPC FAILURE "+getName());
	            gui.exceptionCatcher(returned, false);            
	            onFailureAfterLog(returned);
	        }
    	} else {
    		onTimedOutReturn(returned);
    	}
    }
    
    protected class CallTimer extends Timer {
		/**
		 * sets the timer to call timeOut() after callTimeOut miliseconds
		 */
    	public CallTimer() {
			super();
			this.schedule(callTimeOut);
		}
		
		@Override
		public void run() {
			timeOut();
		}
    }
    
	/**
	 * sets the timer to call timeOut() after callTimeOut miliseconds
	 */
    final protected void setTimer() {
    	timeOutTimer = new CallTimer();
    }
    
    final protected void timeOut() {
    	if (!hasReturned) {
    		hasTimedOut = true;
    		gui.log("RPC " + getName() + " TIMED OUT after " + callTimeOut + "ms");
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
