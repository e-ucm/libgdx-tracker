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

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.storage.Storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class XAPITracker extends AbstractTracker {

	public enum Verb {
		INITIALIZED, PROGRESSED, COMPLETED, ACCESSED, SKIPPED, SELECTED, UNLOCKED, INTERACTED, USED;

		public String toString() {
			switch (this) {
			case ACCESSED:
				return "https://w3id.org/xapi/seriousgames/verbs/accessed";
			case COMPLETED:
				return "http://adlnet.gov/expapi/verbs/completed";
			case INITIALIZED:
				return "https://w3id.org/xapi/adb/verbs/initialized";
			case INTERACTED:
				return "http://adlnet.gov/expapi/verbs/interacted";
			case PROGRESSED:
				return "http://adlnet.gov/expapi/verbs/progressed";
			case SELECTED:
				return "https://w3id.org/xapi/adb/verbs/selected";
			case SKIPPED:
				return "http://id.tincanapi.com/verb/skipped";
			case UNLOCKED:
				return "https://w3id.org/xapi/seriousgames/verbs/unlocked";
			case USED:
				return "https://w3id.org/xapi/seriousgames/verbs/used";
			}
			return "";
		}
	}

	public enum Completable {
		SERIOUS_GAME, LEVEL, QUEST;

		public String toString() {
			switch (this) {
			case SERIOUS_GAME:
				return "https://w3id.org/xapi/seriousgames/activity-types/serious-game";
			case LEVEL:
				return "https://w3id.org/xapi/seriousgames/activity-types/level";
			case QUEST:
				return "https://w3id.org/xapi/seriousgames/activity-types/quest";
			}
			return "";
		}
	}

	public enum Accessible {
		SCREEN, AREA, ZONE, CUTSCENE;

		public String toString() {
			switch (this) {
			case SCREEN:
				return "https://w3id.org/xapi/seriousgames/activity-types/screen";
			case AREA:
				return "https://w3id.org/xapi/seriousgames/activity-types/area";
			case ZONE:
				return "https://w3id.org/xapi/seriousgames/activity-types/zone";
			case CUTSCENE:
				return "https://w3id.org/xapi/seriousgames/activity-types/cutscene";
			}
			return "";
		}
	}

	public enum Alternative {
		QUESTION, MENU, DIALOG;
		public String toString() {
			switch (this) {
			case QUESTION:
				return "http://adlnet.gov/expapi/activities/question";
			case MENU:
				return "https://w3id.org/xapi/seriousgames/activity-types/menu";
			case DIALOG:
				return "https://w3id.org/xapi/seriousgames/activity-types/dialog-tree";
			}
			return "";
		}
	}

	public enum GameObject {
		ENEMY, NPC, ITEM;
		public String toString() {
			switch (this) {
			case ENEMY:
				return "https://w3id.org/xapi/seriousgames/activity-types/enemy";
			case ITEM:
				return "https://w3id.org/xapi/seriousgames/activity-types/item";
			case NPC:
				return "https://w3id.org/xapi/seriousgames/activity-types/non-player-character";
			}
			return "";
		}
	}

	public enum Extension {
		HEALTH, POSITION, PROGRESS;

		public String toString() {
			switch (this) {
			case HEALTH:
				return "https://w3id.org/xapi/seriousgames/extensions/health";
			case POSITION:
				return "https://w3id.org/xapi/seriousgames/extensions/position";
			case PROGRESS:
				return "https://w3id.org/xapi/seriousgames/extensions/progress";
			}
			return "";
		}
	}

	public class Result {
		Float score;
		Boolean success;
		Boolean completion;
		String response;
		Map<String, Object> extensions;

		public String toString() {
			String s = "\"result\":{";
			if (score != null) {
				s += "\"score\":{\"raw\":" + score + "},";
			}

			if (success != null) {
				s += "\"success\":" + success + ",";
			}

			if (completion != null) {
				s += "\"completion\":" + completion + ",";
			}

			if (response != null) {
				s += "\"response\": \"" + response + "\",";
			}

			if (extensions != null) {
				s += "\"extensions\":{";
				for (Entry<String, Object> e : extensions.entrySet()) {
					s += "\"" + e.getKey() + "\":";
					if (e.getValue() instanceof String) {
						s += "\"" + e.getValue() + "\"";
					} else {
						s += e.getValue().toString();
					}
					s += ",";
				}
				s = s.substring(0, s.length() - 1) + "},";
			}
			return s.substring(0, s.length() - 1) + "},";
		}
	}

	private Result result;

	private String actor;

	private String objectId;

	private Date date = new Date();

	public XAPITracker(Storage storage) {
		super(storage);
	}

	public XAPITracker(Storage storage, float flushInterval) {
		super(storage, flushInterval);
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
			statements += trace + ",";
		}
		return statements.substring(0, statements.length() - 1) + "]";
	}

	@Override
	public String contentType() {
		return "application/json; charset=utf-8";
	}

	public void initialized(String id, Completable type) {
		statement(Verb.INITIALIZED, id, type);
	}

	public void progressed(String id, Completable type, float progress) {
		setProgress(progress);
		statement(Verb.PROGRESSED, id, type);
	}

	public void completed(String id, Completable type) {
		statement(Verb.COMPLETED, id, type);
	}

	public void completed(String id, Completable type, boolean success) {
		setSuccess(success);
		statement(Verb.COMPLETED, id, type);
	}

	public void completed(String id, Completable type, boolean success,
			boolean completion, float score) {
		setCompletion(completion);
		setScore(score);
		completed(id, type, success);
	}

	public void accessed(String id, Accessible type) {
		statement(Verb.ACCESSED, id, type);
	}

	public void skipped(String id, Accessible type) {
		statement(Verb.SKIPPED, id, type);
	}

	public void selected(String alternative, Alternative type,
			String selectedOption) {
		setResponse(selectedOption);
		statement(Verb.SELECTED, alternative, type);
	}

	public void unlocked(String alternative, Alternative type,
			String unlockedOption) {
		setResponse(unlockedOption);
		statement(Verb.UNLOCKED, alternative, type);
	}

	public void interacted(String id, GameObject type) {
		statement(Verb.INTERACTED, id, type);
	}

	public void used(String id, GameObject type) {
		statement(Verb.USED, id, type);
	}

	private Result result() {
		if (result == null) {
			result = new Result();
		}
		return result;
	}

	public void setSuccess(boolean success) {
		result().success = success;
	}

	public void setScore(float score) {
		result().score = score;
	}

	public void setResponse(String response) {
		result().response = response;
	}

	public void setCompletion(boolean completion) {
		result().completion = completion;
	}

	public void setProgress(float progress) {
		setExtension(Extension.PROGRESS, progress);
	}

	public void setPosition(float x, float y, float z) {
		setExtension(Extension.POSITION, "{\"x\":" + x + ", \"y\": " + y
				+ ", \"z\": " + z + "}");
	}

	public void setHealth(float health) {
		setExtension(Extension.HEALTH, health);
	}

	public void setVar(String id, Object value) {
		setExtension(id, value);
	}

	public void setExtension(Extension key, Object value) {
		setExtension(key.toString(), value);
	}

	public void setExtension(String key, Object value) {
		Map<String, Object> extensions = result().extensions;
		if (extensions == null) {
			result().extensions = extensions = new HashMap<String, Object>();
		}
		extensions.put(key, value);
	}

	private void statement(Verb verb, String activityId, Object activityType) {
		date.setTime(System.currentTimeMillis());
		String statement = "{\"actor\":" + actor + ","
				+ createVerb(verb.toString()) + ","
				+ createObject(activityId, activityType.toString())
				+ ",\"timestamp\":\"" + toISODateString(date) + "\"";

		if (result != null) {
			statement += "," + result.toString();
		}
		// Clear accumulated result
		result = null;

		statement += "}";
		connect();
		addTrace(statement);
	}

	private String createVerb(String id) {
		return "\"verb\":{\"id\":\"" + id + "\"}";
	}

	private String createObject(String id, String type) {
		return "\"object\":{\"id\":\"" + objectId + id
				+ "\",\"definition\":{\"type\":\"" + type + "\"}}";
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
