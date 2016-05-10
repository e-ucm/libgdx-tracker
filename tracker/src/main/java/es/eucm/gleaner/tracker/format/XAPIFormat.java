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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.C;

import java.util.ArrayList;
import java.util.Date;

public class XAPIFormat extends Json implements TraceFormat, C {

	public static final String VOCAB_PREFIX = "https://w3id.org/xapi/seriousgames/";

	public static final String VERB_PREFIX = VOCAB_PREFIX + "verbs/";

	public static final String EXT_PREFIX = VOCAB_PREFIX + "extensions/";

	private String actor;

	private String objectId;

	private Date date;

	public XAPIFormat() {
		date = new Date();
	}

	public boolean isReady() {
		return actor != null;
	}

	public void startData(ObjectMap data) {
		actor = ((JsonValue) data.get("actor")).prettyPrint(OutputType.json, 0);
		objectId = (String) data.get("objectId");
		if (!objectId.endsWith("/")) {
			objectId += "/";
		}
	}

	public String serialize(ArrayList<String> traces) {
		String statements = "[";
		for (String trace : traces) {
			statements += createStatement(trace) + ",";
		}
		return statements.substring(0, statements.length() - 1) + "]";
	}

	@Override
	public String contentType() {
		return "application/json; charset=utf-8";
	}

	public String createStatement(String trace) {
		String[] parts = trace.split(",");

		date.setTime(Long.parseLong(parts[0]));
		String statement = "{\"actor\":" + actor + "," + createVerb(parts[1])
				+ "," + createObject(parts) + ",\"timestamp\":\""
				+ toISODateString(date) + "\"";

		if (parts.length > 3) {
			statement += ",\"result\":";

			if (parts[1].equals(SELECTED)) {
				statement += "{\"response\":\"" + parts[3] + "\"}";
			} else {
				statement += "{\"extensions\":{\"" + EXT_PREFIX + "value\": \""
						+ parts[3] + "\"}}";
			}
		}

		return statement + "}";
	}

	private String createVerb(String event) {
		String prefix = VERB_PREFIX;
		if (SELECTED.equals(event)) {
			prefix = "https://w3id.org/xapi/adb/verbs/";
		} else if (COMPLETED.equals(event)) {
			prefix = "http://adlnet.gov/expapi/verbs/";
		}
		return "\"verb\":{\"id\":\"" + prefix + event + "\"}";
	}

	private String createObject(String[] parts) {
		String event = parts[1];
		String id;
		if (event.matches(COMPLETED + "|" + STARTED)) {
			id = "completables";
		} else if (event.matches(SET + "|" + INCREASED + "|" + DECREASED)) {
			id = "vars";
		} else if (event.matches(SELECTED)) {
			id = "alternatives";
		} else {
			id = event;
		}

		return "\"object\":{\"id\":\"" + objectId + id + "/" + parts[2] + "\"}";
	}

	private String pad(int n) {
		return n < 10 ? "0" + n : n + "";
	}

	private String toISODateString(Date d) {
		int year = d.getYear();

		if (year < 2000) {
			year += 1900;
		}
		return year + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate())
				+ "T" + pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":"
				+ pad(d.getSeconds()) + "Z";
	}
}
