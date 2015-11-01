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
package es.eucm.gleaner.tracker.storage;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.Tracker;
import es.eucm.gleaner.tracker.Tracker.FlushListener;
import es.eucm.gleaner.tracker.Tracker.StartListener;

public class NetStorage implements Storage {

	public static final String REST_API_TRACK = "track/";

	public static final String REST_API_START = "start/";

	private Net net;

	private Tracker tracker;

	private String host;

	private String trackingCode;

	private String authToken;

	private HttpRequestBuilder httpBuilder = new HttpRequestBuilder();

	private NetStartListener netStartListener;

	/**
	 * @param net
	 *            an object to interact with the network
	 * @param host
	 *            host of the collector server
	 * @param trackingCode
	 *            tracking code for the game
	 */
	public NetStorage(Net net, String host, String trackingCode) {
		this.net = net;
		this.host = host;
		this.trackingCode = trackingCode;
	}

	@Override
	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
		netStartListener = new NetStartListener(tracker);
	}

	@Override
	public void start(StartListener startListener) {
		net.sendHttpRequest(
				httpBuilder
						.newRequest()
						.url(host + REST_API_START + trackingCode)
						.method("POST").build(), netStartListener);
	}

	@Override
	public void send(String data, FlushListener flushListener) {
		net.sendHttpRequest(httpBuilder.newRequest().url(host + REST_API_TRACK)
				.header("Content-Type",
						tracker.getTraceFormat().contentType())
				.method("POST").header("Authorization", authToken)
				.content(data).build(), flushListener);
	}

	@Override
	public void close() {
	}

	public class NetStartListener extends StartListener {

		public NetStartListener(Tracker tracker) {
			super(tracker);
		}

		@Override
		protected void processData(ObjectMap data) {
			authToken = data.get("authToken").toString();
			super.processData(data);
		}
	}
}
