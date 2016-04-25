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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import es.eucm.gleaner.tracker.C;

import java.util.ArrayList;
import java.util.Date;

public class XAPIFormat implements TraceFormat, C {

	public static final String VOCAB_PREFIX = "http://rage-eu.com/xapi/";

	public static final String VERB_PREFIX = VOCAB_PREFIX + "verbs/";

	public static final String EXT_PREFIX = VOCAB_PREFIX + "extensions/";

	private JsonValue actor;

	private String activityId;

	private Array<JsonValue> statements = new Array<JsonValue>();

	private JsonValuePool pool = new JsonValuePool();

	private PrettyPrintSettings prettyPrintSettings = new PrettyPrintSettings();

	private Date date;

	public XAPIFormat() {
		prettyPrintSettings.outputType = OutputType.json;
		date = new Date();
	}

	public void startData(ObjectMap data) {
		actor = ((JsonValue) data.get("actor")).child;
		activityId = (String) data.get("activityId");
		if (!activityId.endsWith("/")) {
			activityId += "/";
		}
	}

	public String serialize(ArrayList<String> traces) {
		statements.clear();
		for (String trace : traces) {
			statements.add(createStatement(trace));
		}
		String result = "[";
		for (JsonValue s : statements) {
			result += s.prettyPrint(prettyPrintSettings) + ",";
			pool.free(s);
		}
		return result.substring(0, result.length() - 1)
				.replaceAll("[\n\t]", "").replace(": ", ":")
				+ "]";
	}

	@Override
	public String contentType() {
		return "application/json; charset=utf-8";
	}

	public JsonValue createStatement(String trace) {
		JsonValue statement = pool.obtain();
		statement.setType(ValueType.object);

		JsonValue actorValue = pool.obtain();
		actorValue.setType(ValueType.object);
		actorValue.setName("actor");
		actorValue.child = actor;

		statement.child = actorValue;

		String[] parts = trace.split(",");

		JsonValue verb = pool.obtain();
		verb.setType(ValueType.object);
		verb.setName("verb");
		verb.child = createVerb(parts[1]);
		actorValue.next = verb;

		JsonValue activity = pool.obtain();
		activity.setType(ValueType.object);
		activity.setName("object");
		activity.child = createActivity(parts);
		verb.next = activity;

		JsonValue timeStamp = pool.obtain();
		timeStamp.setName("timestamp");
		date.setTime(Long.parseLong(parts[0]));
		timeStamp.set(toISODateString(date));
		activity.next = timeStamp;

		if (parts.length > 3) {

			JsonValue result = pool.obtain();
			result.setType(ValueType.object);
			result.setName("result");
			timeStamp.next = result;

			if (parts[1].equals(SELECTED)){
				JsonValue response = pool.obtain();
				response.setType(ValueType.stringValue);
				response.setName("response");
				response.set(parts[3]);

				result.child = response;
			} else {
				JsonValue extensions = pool.obtain();
				extensions.setType(ValueType.object);
				extensions.setName("extensions");

				extensions.child = pool.obtain();
				extensions.child.setType(ValueType.object);
				extensions.child.setName(EXT_PREFIX + "value");
				extensions.child.set(parts[3]);

				result.child = extensions;
			}
		}

		return statement;
	}

	private JsonValue createVerb(String event) {
		JsonValue verb = pool.obtain();
		verb.setType(ValueType.object);
		String id;
		String prefix = VERB_PREFIX;
		if (STARTED.equals(event)) {
			prefix = "http://adlnet.gov/xapi/verbs/";
			id = "launched";
		} else if (SELECTED.equals(event)) {
			prefix = "http://adlnet.gov/xapi/verbs/";
			id = "preferred";
		} else if (COMPLETED.equals(event)) {
			prefix = "http://adlnet.gov/xapi/verbs/";
			id = "terminated";
		} else {
			id = event;
		}
		verb.setName("id");
		verb.set(prefix + id);
		return verb;
	}

	private JsonValue createActivity(String[] parts) {
		JsonValue activity = pool.obtain();
		activity.setType(ValueType.object);
		String event = parts[1];
		String id;
		if (event.matches(COMPLETED + "|" + STARTED)) {
			id = "completable";
		} else if (event.matches(SET + "|" + INCREASED + "|" + DECREASED)) {
			id = "variable";
		} else if (event.matches(SELECTED)){
			id = "alternative";
		} else {
			id = event;
		}
		activity.setName("id");
		activity.set(activityId + id + "/" + parts[2]);
		return activity;
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

	private class JsonValuePool extends Pool<JsonValue> {

		@Override
		protected JsonValue newObject() {
			return new JsonValue(ValueType.nullValue);
		}

		@Override
		public void free(JsonValue object) {
			if (object == actor) {
				return;
			}

			for (JsonValue entry = object.child; entry != null; entry = entry.next) {
				free(entry);
			}
			object.next = null;
			object.child = null;
			object.setName(null);
			super.free(object);
		}
	}
}
