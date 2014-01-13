/**
 * Copyright 2012 Ronen Hamias, Anton Kharenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
