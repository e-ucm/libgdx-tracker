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

import com.badlogic.gdx.Net.HttpResponseListener;
import es.eucm.gleaner.tracker.Tracker;
import es.eucm.gleaner.tracker.http.SimpleHttpResponse;

public class TestStorage implements Storage {

	public boolean started;

	public String data = "";

	public void setTracker(Tracker tracker) {
	}

	public void start(HttpResponseListener startListener) {
		started = true;
		startListener.handleHttpResponse(new SimpleHttpResponse(
				"{\"actor\":{\"mbox\":\"user@example.com\"},\"activityId\":\"Test\"}", 200));
	}

	public void send(String data, HttpResponseListener flushListener) {
		this.data += data;
		flushListener.handleHttpResponse(new SimpleHttpResponse("", 204));
	}

    @Override
    public void close() {
    }
}
