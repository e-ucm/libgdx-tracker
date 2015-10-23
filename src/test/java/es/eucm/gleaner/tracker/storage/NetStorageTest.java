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
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import es.eucm.gleaner.tracker.Tracker;
import es.eucm.gleaner.tracker.http.SimpleHttpResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetStorageTest {

	private Tracker tracker;

	private TestNet net;

	@Before
	public void setUp() {
		tracker = new Tracker(new NetStorage(net = new TestNet(), "", "", ""), 1);
	}

	@Test
	public void testRequest() {
		tracker.start();
		tracker.trace("trace", "test");
		tracker.update(0.5f);
		assertTrue(net.started);
		assertEquals(net.data, "");
		tracker.update(1.5f);
		assertTrue(net.data.matches("[0-9]+,trace,test\n"));
	}

	public static class TestNet implements Net {

		public boolean started;

		public String data = "";

		public void sendHttpRequest(HttpRequest httpRequest,
				HttpResponseListener httpResponseListener) {
			if (httpRequest.getUrl().equals("start/")) {
				httpResponseListener.handleHttpResponse(new SimpleHttpResponse(
						"{\"authToken\":\"test\"}", 200));
				started = true;
			} else if (httpRequest.getUrl().equals("track/")) {
				data += httpRequest.getContent();
				httpResponseListener.handleHttpResponse(new SimpleHttpResponse(
						"", 204));
			}
		}

		public void cancelHttpRequest(HttpRequest httpRequest) {

		}

		public ServerSocket newServerSocket(Protocol protocol, String hostname,
				int port, ServerSocketHints hints) {
			return null;
		}

		public ServerSocket newServerSocket(Protocol protocol, int port,
				ServerSocketHints hints) {
			return null;
		}

		public Socket newClientSocket(Protocol protocol, String host, int port,
				SocketHints hints) {
			return null;
		}

		public boolean openURI(String URI) {
			return false;
		}
	}
}
