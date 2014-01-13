package org.socketio.netty;

/**
 * Listens to the result of a {@link ISessionFuture}.  The result of the
 * asynchronous operation is notified once this listener 
 * is added by calling {@link ISessionFuture#addListener(ISessionFutureListener)}.
 * 
 * @author Anton Kharenko
 *
 */
public interface ISessionFutureListener {
	
	/**
     * Invoked when the operation associated with the {@link ISessionFuture}
     * has been completed.
     *
     * @param future  the source {@link ISessionFuture} which called this
     *                callback
     */
	void onOperationCompleted(ISessionFuture future);
	
}
