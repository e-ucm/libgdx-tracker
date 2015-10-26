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
import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.format.LinesFormat;
import es.eucm.gleaner.tracker.format.TraceFormat;
import es.eucm.gleaner.tracker.storage.Storage;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ángel Serrano Laguna
 */
public class Tracker {

	private Storage storage;

	private TraceFormat traceFormat = new LinesFormat();

	private AtomicBoolean sending;

	private AtomicBoolean connected;

	private AtomicBoolean connecting;

	private boolean flushRequested;

	private FlushListener flushListener = new FlushListener();

	private StartListener startListener;

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

	public Tracker(Storage storage) {
		this(storage, -1);
	}

	/**
	 * @param flushInterval
	 *            seconds between flush calls
	 */
	public Tracker(Storage storage, float flushInterval) {
		this.storage = storage;
		storage.setTracker(this);
		this.flushInterval = flushInterval;
		this.nextFlush = flushInterval;
		startListener = new StartListener(this);
		sending = new AtomicBoolean();
		connected = new AtomicBoolean();
		connecting = new AtomicBoolean();
		queue = new ArrayList<String>();
		sent = new ArrayList<String>();
	}

	public void setTraceFormat(TraceFormat traceFormat) {
		this.traceFormat = traceFormat;
	}

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
     * Close the connection with the storage
     */
    public void close(){
        storage.close();
    }

	private void connect() {
		if (!connected.get() && !connecting.get()) {
			connecting.set(true);
			storage.start(startListener);
		}
	}

	/**
	 * Sends all traces to the server and empties the current queue of traces
	 */
	private void flush() {
		if (!connected.get()) {
			connect();
		} else if (!queue.isEmpty() && !sending.get()) {
			sending.set(true);
			sent.addAll(queue);
			queue.clear();
			flushRequested = false;
			storage.send(traceFormat.serialize(sent), flushListener);
		}
	}


	public static class StartListener implements HttpResponseListener {

		private Tracker tracker;

		public StartListener(Tracker tracker) {
			this.tracker = tracker;
		}

		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() == 200) {
				String data = httpResponse.getResultAsString();
				try {
					ObjectMap result = HttpRequestBuilder.json.fromJson(
							ObjectMap.class, data);
					processData(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
				tracker.connected.set(true);
			}
			tracker.connecting.set(false);
		}

		protected void processData(ObjectMap data) {
			tracker.traceFormat.startData(data);
		}

		public void failed(Throwable t) {
			tracker.connecting.set(false);
		}

		public void cancelled() {
			tracker.connecting.set(false);
		}
	}

	public class FlushListener implements HttpResponseListener {

		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() == 204) {
				sent.clear();
				sending.set(false);
			}
		}

		public void failed(Throwable t) {
			sending.set(false);
		}

		public void cancelled() {
			sending.set(false);
		}
	}

	// Trace methods
	/**
	 * Adds the given trace to the queue
	 */
	public void trace(String trace) {
		queue.add(System.currentTimeMillis() + "," + trace);
	}

	/**
	 * Adds a trace built with the given values
	 */
	public void trace(String... values) {
		String result = "";
		int i = 0;
		for (String value : values) {
			result += value + (i++ == values.length - 1 ? "" : ",");
		}
		trace(result);
	}

	/**
	 * Player entered in a new game screen.
	 * 
	 * @param screenId
	 *            an unique identifier for the screen
	 */
	public void screen(String screenId) {
		trace(C.SCREEN, screenId);
	}

	/**
	 * Player entered in a new game zone.
	 * 
	 * @param zoneId
	 *            an unique identifier for the zone
	 */
	public void zone(String zoneId) {
		trace(C.ZONE, zoneId);
	}

	/**
	 * Player choose an option in a given choice
	 * 
	 * @param choiceId
	 *            the choice identifier
	 * @param optionId
	 *            the option identifier
	 */
	public void choice(String choiceId, String optionId) {
		trace(C.CHOICE, choiceId, optionId);
	}

	/**
	 * A meaningful variable changed in the game
	 * 
	 * @param varName
	 *            variable's name
	 * @param value
	 *            variable's value
	 */
	public void var(String varName, Object value) {
		trace(C.VAR, varName, value.toString());
	}
}
