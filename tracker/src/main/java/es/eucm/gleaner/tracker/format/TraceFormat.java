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
package es.eucm.gleaner.tracker.format;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;

public interface TraceFormat {

	/**
	 * Processes the data sent by the server to start the session
	 */
	void startData(ObjectMap data);

	/**
	 * Serialize all the traces in an unique string. This will be sent the POST
	 * body
	 */
	String serialize(ArrayList<String> traces);

	/**
	 * @return the mime type of the content produced by this format
	 */
	String contentType();
}
