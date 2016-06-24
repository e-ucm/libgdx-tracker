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

import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import es.eucm.gleaner.tracker.CsvTracker;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocalStorageTest {

	static LwjglFiles files;

	private CsvTracker tracker;

	private FileHandle tempFile;

	@BeforeClass
	public static void setUpClass() {
		files = new LwjglFiles();
	}

	@Test
	public void testTraces() {
		try {
			File file = File.createTempFile("local-storage", "test");
			tracker = new CsvTracker(new LocalStorage(
					tempFile = files.absolute(file.getAbsolutePath())));
		} catch (IOException e) {
			fail("Impossible to create temp file");
		}
		tracker.start();
		tracker.zone("zone1");
		tracker.requestFlush();
		tracker.update(0);
		tracker.screen("screen1");
		tracker.requestFlush();
		tracker.update(0);
		tracker.close();
		assertTrue(tempFile.readString().matches(
				"^session,[0-9]+\n[0-9]+,zone,zone1\n[0-9]+,screen,screen1\n$"));
	}

	@Test
	public void testLocalStorageWithDelay() {
		try {
			final File file = File.createTempFile("local-storage", "test");
			tracker = new CsvTracker(new LocalStorage(
					tempFile = files.absolute(file.getAbsolutePath())) {
				@Override
				public void start(HttpResponseListener startListener) {
					super.start(new DelayedListener(1000, startListener));
				}

				@Override
				public void send(String data, HttpResponseListener flushListener) {
					super.send(data, new DelayedListener(1000, flushListener));
				}
			});
		} catch (IOException e) {
			fail("Impossible to create temp file");
		}

		tracker.start();
		for (int i = 0; i < 1000; i++) {
			tracker.zone("zone" + i);
			tracker.requestFlush();
			tracker.update(0);
		}
		tracker.close();
		for (int i = 0; i < 1000; i++) {
			assertTrue(tempFile.readString().contains("zone" + i));
		}
	}

	public static class DelayedListener implements HttpResponseListener {

		private long delay;

		private HttpResponseListener responseListener;

		public DelayedListener(long delay, HttpResponseListener responseListener) {
			this.delay = delay;
			this.responseListener = responseListener;
		}

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			responseListener.handleHttpResponse(httpResponse);
		}

		@Override
		public void failed(Throwable t) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			responseListener.failed(t);
		}

		@Override
		public void cancelled() {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			responseListener.cancelled();
		}
	}

}
