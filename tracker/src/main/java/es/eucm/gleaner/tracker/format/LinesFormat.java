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

public class LinesFormat implements TraceFormat {

	@Override
	public void startData(ObjectMap data) {

	}

	@Override
	public String serialize(ArrayList<String> traces) {
		String result = "";
		for (String trace : traces) {
			result += trace + "\n";
		}
		return result;
	}

	@Override
	public String contentType() {
		return "text/plain";
	}
}
