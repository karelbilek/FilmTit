package cz.filmtit.client;

import cz.filmtit.share.*;

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

	protected static FilmTitServiceHandler filmTitServiceHandler;

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
	
    public void displayWindow(String message) {
    	filmTitServiceHandler.displayWindow(message);
    }
}
