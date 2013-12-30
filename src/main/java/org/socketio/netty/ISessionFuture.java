package org.socketio.netty;

/**
 * The result of an asynchronous operation with session.
 * 
 * @author Anton Kharenko
 *
 */
public interface ISessionFuture {
	
	/**
     * Returns a channel where the operation associated with this
     * future takes place.
     */
    ISession getSession();
    
    /**
     * Returns {@code true} if and only if this future is
     * complete, regardless of whether the operation was successful or failed.
     */
	boolean isDone();
	
	/**
     * Returns {@code true} if and only if the operation was completed
     * successfully.
     */
	boolean isSuccess();
	
	/**
     * Returns the cause of the failed operation if the operation has
     * failed.
     *
     * @return the cause of the failure.
     *         {@code null} if succeeded or this future is not
     *         completed yet.
     */
	Throwable getCause();
	
	/**
     * Adds the specified listener to this future.  The
     * specified listener is notified when this future is
     * done. If this future is already completed, the 
     * specified listener is notified immediately.
     */
	void addListener(ISessionFutureListener listener);
	
	/**
     * Removes the specified listener from this future.
     * The specified listener is no longer notified when this
     * future is done. If the specified listener is not 
     * associated with this future, this method does nothing 
     * and returns silently.
     */
	void removeListener(ISessionFutureListener listener);
	
}
