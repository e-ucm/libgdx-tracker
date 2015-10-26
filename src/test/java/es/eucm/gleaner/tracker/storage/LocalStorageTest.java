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

import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import es.eucm.gleaner.tracker.Tracker;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocalStorageTest {

	static LwjglFiles files;

	private Tracker tracker;

	private FileHandle tempFile;

	@BeforeClass
	public static void setUpClass() {
		files = new LwjglFiles();
	}

	@Before
	public void setUp() {
		try {
			File file = File.createTempFile("local-storage", "test");
			tracker = new Tracker(new LocalStorage(
					tempFile = files.absolute(file.getAbsolutePath())));
		} catch (IOException e) {
			fail("Impossible to create temp file");
		}
	}

	@Test
	public void testTraces() {
		tracker.start();
		tracker.zone("zone1");
		tracker.screen("screen1");
		tracker.requestFlush();
		tracker.update(0);
		assertTrue(tempFile.readString().matches(
				"^--new session\n[0-9]+,zone,zone1\n[0-9]+,screen,screen1\n$"));
		tracker.close();
	}
}
