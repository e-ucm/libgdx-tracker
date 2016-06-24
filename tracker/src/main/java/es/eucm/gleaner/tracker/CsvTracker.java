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

import com.badlogic.gdx.utils.ObjectMap;
import es.eucm.gleaner.tracker.storage.Storage;

import java.util.ArrayList;

public class CsvTracker extends AbstractTracker{

	String STARTED = "started";
	String COMPLETED = "completed";

	String SELECTED = "selected";
	String SET = "set";
	String INCREASED = "increased";
	String DECREASED = "decreased";

	String SCREEN = "screen";
	String ZONE = "zone";
	String CLICK = "click";

	public CsvTracker(Storage storage) {
		super(storage);
	}

	public CsvTracker(Storage storage, float flushInterval) {
		super(storage, flushInterval);
	}



	/**
	 * Escapes a piece of input in case it has commas by adding quotes at
	 * beginning and end. If there are any quotes in the piece of input
	 * provided, these are escaped by adding a back slash Examples: input with
	 * a, comma => "input with a, comma" input "with \"quotes    =>   "input
	 * \"with \\"quotes" "input with, quotes, and commas" =>
	 * "\"input with, quotes, and commas\""
	 *
	 * @param input
	 *            The input to be escaped
	 * @return The escaped version of the input
	 */
	private String escapeInput(String input) {
		if (!input.contains(",") && !input.contains("\"")) {
			return input;
		}
		input = input.replaceAll("\"", "\\\\\"");
		input = "\"" + input + "\"";
		return input;
	}

	// Trace methods
	/**
	 * Adds the given trace to the queue
	 */
	public void trace(String trace) {
		String t = System.currentTimeMillis() + "," + trace;
		addTrace(t);
	}

	/**
	 * Adds a trace built with the given values
	 */
	public void trace(String... values) {
		String result = "";
		int i = 0;
		for (String value : values) {
			result += escapeInput(value)
					+ (i++ == values.length - 1 ? "" : ",");
		}
		trace(result);
	}

	/**
	 * Starts a completable
	 */
	public void started(String completableId){
		trace(STARTED, completableId);
	}

	/**
	 * Fulfills a completable
	 */
	public void completed(String completableId){
		trace(COMPLETED, completableId);
	}

	/**
	 * Player entered in a new game screen.
	 *
	 * @param screenId
	 *            an unique identifier for the screen
	 */
	public void screen(String screenId) {
		trace(SCREEN, screenId);
	}

	/**
	 * Player entered in a new game zone.
	 *
	 * @param zoneId
	 *            an unique identifier for the zone
	 */
	public void zone(String zoneId) {
		trace(ZONE, zoneId);
	}

	/**
	 * An option from a set of choices was made. This can be used both to log
	 * "internal decisions" of the game (e.g. random selection of a type of
	 * enemy from a list), or users' selections over a group of options (e.g.
	 * avatar picked from a list)
	 *
	 * @param alternativeId
	 *            the choice identifier
	 * @param optionId
	 *            the option identifier
	 */
	public void selected(String alternativeId, String optionId) {
		trace(SELECTED, alternativeId, optionId);
	}

	/**
	 * A meaningful variable changed in the game
	 *
	 * @param varName
	 *            variable's name
	 * @param value
	 *            variable's value. Note: The class of the value should be
	 *            serializable
	 */
	public void set(String varName, Object value) {
		trace(SET, varName, value.toString());
	}

	public void increase(String varName, Number value){
		trace(INCREASED, varName, value.toString());
	}

	public void decrease(String varName, Number value){
		trace(DECREASED, varName, value.toString());
	}

	/**
	 * Logs that the user clicked with the mouse a particular target (e.g. an
	 * enemy, an ally, a button of the HUD, etc.).
	 *
	 * This method can also be used for logging touches on tactile screens (e.g.
	 * smartphones or tablets).
	 *
	 * @param x
	 *            Horizontal coordinate of the mouse or touch event, in the
	 *            game's coordinate system (please avoid using the window's
	 *            coordinate system)
	 * @param y
	 *            Vertical coordinate of the mouse or touch event, in the game's
	 *            coordinate system
	 * @param target
	 *            Id of the element that was hit by the click
	 */
	public void click(float x, float y, String target) {
		trace(CLICK, Float.toString(x), Float.toString(y), target);
	}

	/**
	 * Logs that the user clicked the mouse on position (x,y).
	 *
	 * Unlike {@link #click(float, float, String)}, this method does not log the
	 * particular target (e.g. an enemy, an ally, a button of the HUD, etc.)
	 * that was hit.
	 *
	 * This method is more convenient when the actual target is not relevant
	 * (for example, to produce heatmaps) or it can be inferred otherwise (for
	 * example, using information collected in other traces).
	 *
	 * As with {@link #click(float, float, String)}, this method can also be
	 * used for logging touches on tactile screens (e.g. smartphones or
	 * tablets).
	 *
	 * @param x
	 *            Horizontal coordinate of the mouse or touch event, in the
	 *            game's coordinate system (please avoid using the window's
	 *            coordinate system)
	 * @param y
	 *            Vertical coordinate of the mouse or touch event, in the game's
	 *            coordinate system
	 */
	public void click(float x, float y) {
		trace(CLICK, Float.toString(x), Float.toString(y));
	}


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
