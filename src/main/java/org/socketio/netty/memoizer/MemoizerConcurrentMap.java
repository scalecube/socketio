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

package org.socketio.netty.memoizer;

import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public final class MemoizerConcurrentMap<A, V> {

	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();

	private final Computable<A, V> defaultComputable;

	public MemoizerConcurrentMap() {
		this(null);
	}

	public MemoizerConcurrentMap(final Computable<A, V> computable) {
		this.defaultComputable = computable;
	}

	public V get(final A arg) throws Exception {
		return get(arg, defaultComputable);
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public V get(final A arg, final Computable<A, V> computable)
			throws Exception {
		while (true) {
			Future<V> future = cache.get(arg);
			if (future == null) {
				Callable<V> eval = new Callable<V>() {
					public V call() throws Exception {
						if (computable == null) {
							throw new InvalidParameterException(
									"the computable is null");
						}
						return computable.compute(arg);
					}
				};
				FutureTask<V> futureTask = new FutureTask<V>(eval);
				future = cache.putIfAbsent(arg, futureTask);
				if (future == null) {
					future = futureTask;
					futureTask.run();
				}
			}
			try {
				return future.get();
			} catch (CancellationException e) {
				cache.remove(arg, future);
			} catch (ExecutionException e) {
				cache.remove(arg, future);
				throw launderThrowable(e.getCause());
			} catch (InterruptedException e) {
				cache.remove(arg, future);
			}
		}
	}

	/**
	 * If the Throwable is an Error, throw it; if it is a RuntimeException
	 * return it, otherwise throw IllegalStateException.
	 */
	private static RuntimeException launderThrowable(final Throwable t) {
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		} else if (t instanceof Error) {
			throw (Error) t;
		} else {
			throw new IllegalStateException("Not unchecked", t);
		}
	}

	public Future<V> remove(final A arg) {
		return cache.remove(arg);
	}

	public boolean containsKey(final A arg) {
		return cache.containsKey(arg);
	}

}