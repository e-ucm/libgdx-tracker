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

import es.eucm.gleaner.tracker.format.LinesFormat;
import es.eucm.gleaner.tracker.format.XAPIFormat;
import es.eucm.gleaner.tracker.storage.TestStorage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TrackerTest {

	private Tracker tracker;

	private TestStorage storage;

	@Before
	public void setUp() {
		tracker = new Tracker(storage = new TestStorage());
	}

	private void generateAllTraces() {
		tracker.start();
		tracker.screen("menu");
		tracker.choice("options", "start");
		tracker.zone("zone1");
		tracker.var("score", 1000);
		tracker.trace("random", "random", "random");
		tracker.requestFlush();
		tracker.update(0);
	}

	@Test
	public void testLinesFormat() {
		tracker.setTraceFormat(new LinesFormat());
		generateAllTraces();
		assertTrue(storage.started);
		String timeStamp = "[0-9]+,";
		assertTrue(storage.data.matches(timeStamp + C.SCREEN + ",menu\n"
				+ timeStamp + C.CHOICE + ",options,start\n" + timeStamp
				+ C.ZONE + ",zone1\n" + timeStamp + C.VAR + ",score,1000\n"
				+ timeStamp + "random,random,random\n"));
	}

	@Test
	public void testXAPIFormat() {
		tracker.setTraceFormat(new XAPIFormat());
		generateAllTraces();
		assertTrue(storage.started);
        System.out.println(storage.data);
        assertTrue(storage.data.contains("actor") && storage.data.contains("verb") && storage.data.contains("object"));
	}

}
