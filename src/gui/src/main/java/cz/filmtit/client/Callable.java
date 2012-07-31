package cz.filmtit.client;

import cz.filmtit.share.*;
import com.google.gwt.user.client.Window;

/**
 * Represents an RPC with parameters.
 * The method is automatically called on its creation
 * and stored into a queue of active calls
 * so that it can be easily re-called on failure.
 */
public abstract class Callable {
	
	// static members
	
//	static private Dictionary<Integer, Callable> queue;
	
	static private int newId = 1;
	
	protected static FilmTitServiceAsync filmTitService;
	
	protected static Gui gui;

	protected static int windowsDisplayed;
	
	// non-static members
	
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
	abstract void call();
	
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
