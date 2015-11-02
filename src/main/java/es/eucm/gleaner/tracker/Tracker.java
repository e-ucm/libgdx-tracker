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

/**
 * @author Ángel Serrano Laguna
 */
public class Tracker {

	private Storage storage;

	private TraceFormat traceFormat = new LinesFormat();

	private boolean sending;

	private boolean connected;

	private boolean connecting;

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

	public void setTraceFormat(TraceFormat traceFormat) {
		this.traceFormat = traceFormat;
	}

	public TraceFormat getTraceFormat() {
		return traceFormat;
	}

	/**
	 * Invoke this method to start the data collection. {@link #start()} must be invoked
	 * before any traces are logged
	 * A game initialization method is a good place to call {@link #start()}
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
	 * traces can be logged after {@link #close()} is invoked.
	 * Make sure to invoke this method before your game exits.
     */
    public void close(){
		requestFlush();
		update(0);
		storage.close();
    }

	private void connect() {
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
				tracker.setConnected(true);
			}
			tracker.setConnecting(false);
		}

		protected void processData(ObjectMap data) {
			tracker.traceFormat.startData(data);
		}

		public void failed(Throwable t) {
			tracker.setConnecting(false);
		}

		public void cancelled() {
			tracker.setConnecting(false);
		}
	}

	public class FlushListener implements HttpResponseListener {

		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() == 200) {
				sent.clear();
			}
			setSending(false);
		}

		public void failed(Throwable t) {
			setSending(false);
		}

		public void cancelled() {
			setSending(false);
		}
	}

	/**
	 * Escapes a piece of input in case it has commas by adding quotes at beginning and end.
	 * If there are any quotes in the piece of input provided, these are escaped by adding a back slash
	 * Examples:
	 * 		input with a, comma     =>   "input with a, comma"
	 * 	    input "with \"quotes    =>   "input \"with \\"quotes"
	 * 	    "input with, quotes, and commas"   =>   "\"input with, quotes, and commas\""
	 * @param input	The input to be escaped
	 * @return	The escaped version of the input
	 */
	private String escapeInput(String input){
		if (!input.contains(",") && !input.contains("\"")){
			return input;
		}
		input = input.replaceAll("\"", "\\\\\"");
		input="\""+input+"\"";
		return input;
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
			result += escapeInput(value) + (i++ == values.length - 1 ? "" : ",");
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
	 * An option from a set of choices was made. This can be used both
	 * to log "internal decisions" of the game (e.g. random selection of
	 * a type of enemy from a list), or users' selections over a group
	 * of options (e.g. avatar picked from a list)
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
	 *            variable's value. Note: The class of the value should be serializable
	 */
	public void var(String varName, Object value) {
		trace(C.VAR, varName, value.toString());
	}

	/**
	 * Logs that the user clicked with the mouse a particular target
	 * (e.g. an enemy, an ally, a button of the HUD, etc.).
	 *
	 * This method can also be used for logging touches on tactile screens
	 * (e.g. smartphones or tablets).
	 *
	 * @param x Horizontal coordinate of the mouse or touch event, in the game's
	 *          coordinate system (please avoid using the window's coordinate system)
	 * @param y Vertical coordinate of the mouse or touch event,
	 *          in the game's coordinate system
	 * @param target Id of the element that was hit by the click
	 */
	public void click(float x, float y, String target){
		trace(C.CLICK, Float.toString(x), Float.toString(y), target);
	}

	/**
	 * Logs that the user clicked the mouse on position (x,y).
	 *
	 * Unlike {@link #click(float, float, String)}, this method does not log
	 * the particular target (e.g. an enemy, an ally, a button of the HUD,
	 * etc.) that was hit.
	 *
	 * This method is more convenient when the actual target is not relevant
	 * (for example, to produce heatmaps) or it can be inferred otherwise
	 * (for example, using information collected in other traces).
	 *
	 * As with {@link #click(float, float, String)}, this method can also
	 * be used for logging touches on tactile screens (e.g. smartphones or tablets).
	 *
	 * @param x Horizontal coordinate of the mouse or touch event, in the game's
	 *          coordinate system (please avoid using the window's coordinate system)
	 * @param y Vertical coordinate of the mouse or touch event,
	 *          in the game's coordinate system
	 */
	public void click(float x, float y){
		trace(C.CLICK, Float.toString(x), Float.toString(y));
	}
}
