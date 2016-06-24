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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import es.eucm.gleaner.tracker.AbstractTracker;
import es.eucm.gleaner.tracker.http.SimpleHttpResponse;

import java.io.IOException;
import java.io.Writer;

public class LocalStorage implements Storage {

	private AsyncExecutor asyncExecutor;

	private WriteTask writeTask;

	private FileHandle tracesFile;

	/**
	 * @param tracesFile
	 *            file where to write the traces
	 */
	public LocalStorage(FileHandle tracesFile) {
		this.tracesFile = tracesFile;
		asyncExecutor = new AsyncExecutor(1);
		writeTask = new WriteTask(tracesFile);
	}

	@Override
	public void setTracker(AbstractTracker tracker) {
	}

	@Override
	public void start(HttpResponseListener startListener) {
		writeTask = new WriteTask(tracesFile);
		write("session," + System.currentTimeMillis() + "\n", startListener);
	}

	@Override
	public void send(String data, HttpResponseListener flushListener) {
		write(data, flushListener);
	}

	private void write(String data, HttpResponseListener listener) {
		writeTask.setData(data, listener);
		asyncExecutor.submit(writeTask);
	}

	@Override
	public void close() {
		asyncExecutor.dispose();
		writeTask.close();
	}

	private static class WriteTask implements AsyncTask<Void> {

		private Writer writer;

		private String data;

		private HttpResponseListener responseListener;

		public WriteTask(FileHandle fileHandle) {
			writer = fileHandle.writer(true);
		}

		public void setData(String data, HttpResponseListener responseListener) {
			this.data = data;
			this.responseListener = responseListener;
		}

		@Override
		public Void call() throws Exception {
			try {
				synchronized (this) {
					writer.append(data);
					data = null;
					writer.flush();
				}
				responseListener.handleHttpResponse(new SimpleHttpResponse("",
						204));
			} catch (Exception e) {
				responseListener.failed(e);
			}
			return null;
		}

		public void close() {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
