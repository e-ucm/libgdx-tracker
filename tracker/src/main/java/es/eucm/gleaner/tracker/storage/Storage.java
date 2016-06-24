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
import es.eucm.gleaner.tracker.AbstractTracker;

public interface Storage {

	void setTracker(AbstractTracker tracker);

	/**
	 * The tracker wants to start sending traces
	 */
	void start(HttpResponseListener startListener);

	/**
	 * The tracker wants to send the given data
	 */
	void send(String data, HttpResponseListener flushListener);

	/**
	 * Closes the connection with the storage
	 */
	void close();

}
