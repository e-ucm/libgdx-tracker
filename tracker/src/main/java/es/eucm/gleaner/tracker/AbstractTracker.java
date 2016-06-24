/**
 * Copyright (C) 2015 eUCM Research Group (e-adventure-dev@e-ucm.es)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.eucm.gleaner.tracker;

import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.storage.Storage;

import java.util.ArrayList;

/**
 * @author Ángel Serrano Laguna
 */
public abstract class AbstractTracker {

	private Storage storage;

	private boolean sending;

	private boolean connected;

	private boolean connecting;

	private boolean flushRequested;

	private FlushListener flushListener = new FlushListener();

	private StartListener startListener;

	private Array<TraceListener> listeners = new Array<TraceListener>(3);

	/**
	 * Queue of traces that will be sent in the next flush
	 */
	private ArrayList<String> queue;

	/**
	 * Traces sent to the server but yet not acknowledged by it
	 */
	private ArrayList<String> sent;

	private float nextFlush;

	private float flushInterval = -1;

	public AbstractTracker(Storage storage) {
		this(storage, -1);
	}

	/**
	 * @param flushInterval
	 *            seconds between flush calls
	 */
	public AbstractTracker(Storage storage, float flushInterval) {
		this.storage = storage;
		storage.setTracker(this);
		this.flushInterval = flushInterval;
		this.nextFlush = flushInterval;
		startListener = new StartListener(this);
		sending = false;
		connected = false;
		connecting = false;
		queue = new ArrayList<String>();
		sent = new ArrayList<String>();
	}

	public synchronized boolean isSending() {
		return sending;
	}

	public synchronized void setSending(boolean sending) {
		this.sending = sending;
	}

	public synchronized boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}

	public synchronized boolean isConnecting() {
		return connecting;
	}

	public synchronized void setConnecting(boolean connecting) {
		this.connecting = connecting;
	}

	/**
	 * Invoke this method to start the data collection. {@link #start()} must be
	 * invoked before any traces are logged A game initialization method is a
	 * good place to call {@link #start()}
	 */
	public void start() {
		connect();
	}

	/**
	 * Updates the tracker. This method must be called in the game loop
	 * 
	 * @param delta
	 *            seconds since last update
	 */
	public void update(float delta) {
		if (flushInterval >= 0) {
			nextFlush -= delta;
			if (nextFlush <= 0) {
				flushRequested = true;
			}
			while (nextFlush <= 0) {
				nextFlush += flushInterval;
			}
		}

		if (flushRequested) {
			flush();
		}
	}

	/**
	 * Request a flush that will send the current stored traces in the next
	 * update
	 */
	public void requestFlush() {
		flushRequested = true;
	}

	/**
	 * Closes the connection and finalizes the tracking session. No further
	 * traces can be logged after {@link #close()} is invoked. Make sure to
	 * invoke this method before your game exits.
	 */
	public void close() {
		int retries = 10;
		while ((!queue.isEmpty() || isSending()) && retries > 0) {
			if (isSending() || isConnecting()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				retries--;
			} else {
				flush();
			}
		}
		storage.close();
	}

	protected void connect() {
		if (!isConnected() && !isConnecting()) {
			setConnecting(true);
			storage.start(startListener);
		}
	}

	/**
	 * Sends all traces to the server and empties the current queue of traces
	 */
	private void flush() {
		if (!isConnected()) {
			connect();
		} else if (!queue.isEmpty() && !isSending()) {
			setSending(true);
			sent.addAll(queue);
			queue.clear();
			flushRequested = false;
			storage.send(serialize(sent), flushListener);
		}
	}

	public void addTraceListener(TraceListener traceListener) {
		listeners.add(traceListener);
	}

	public void addTrace(String trace) {
		queue.add(trace);
		for (TraceListener listener : listeners) {
			listener.trace(trace);
		}
	}

	public boolean isReady() {
		return true;
	}

	public abstract void startData(ObjectMap data);

	public abstract String contentType();

	public abstract String serialize(ArrayList<String> traces);

	public static class StartListener implements HttpResponseListener {

		private AbstractTracker tracker;

		public StartListener(AbstractTracker tracker) {
			this.tracker = tracker;
		}

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() / 100 == 2) {
				String data = httpResponse.getResultAsString();
				try {
					ObjectMap result = HttpRequestBuilder.json.fromJson(
							ObjectMap.class, data);
					processData(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
				tracker.setConnected(true);
			}
			tracker.setConnecting(false);
		}

		protected void processData(ObjectMap data) {
			tracker.startData(data);
		}

		@Override
		public void failed(Throwable t) {
			tracker.setConnecting(false);
		}

		@Override
		public void cancelled() {
			tracker.setConnecting(false);
		}
	}

	public class FlushListener implements HttpResponseListener {

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() / 100 == 2) {
				sent.clear();
			}
			setSending(false);
		}

		@Override
		public void failed(Throwable t) {
			setSending(false);
		}

		@Override
		public void cancelled() {
			setSending(false);
		}
	}

	public interface TraceListener {
		/**
		 * A trace was added to the queue
		 */
		void trace(String trace);
	}

}
