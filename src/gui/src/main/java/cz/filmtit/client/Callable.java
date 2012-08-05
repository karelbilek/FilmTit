package cz.filmtit.client;

import cz.filmtit.share.exceptions.*;
import cz.filmtit.share.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

import com.google.gwt.core.client.GWT;
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
	abstract public void call();
    abstract public String getName();
    abstract public void onSuccessAfterLog(T returned);
    
    public void onFailureAfterLog(Throwable returned) {
        displayWindow(returned.getLocalizedMessage());
    }

    @Override
    public final void onSuccess(T returned) {
        gui.log("RPC SUCCESS "+getName());
        onSuccessAfterLog(returned);
    }

    @Override
    public final void onFailure(Throwable returned) {
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
    }
	
	/**
	 * enqueues the object and invokes the RPC
	 */
	public void enqueue() {
//		queue.put(id, this);
		call();
	}
	
	/**
	 * called after successful completion of RPC
	 */
	public void dequeue() {
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
