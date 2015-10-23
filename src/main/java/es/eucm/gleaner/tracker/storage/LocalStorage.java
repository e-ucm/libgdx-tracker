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

import com.badlogic.gdx.files.FileHandle;
import es.eucm.gleaner.tracker.Tracker;
import es.eucm.gleaner.tracker.Tracker.FlushListener;
import es.eucm.gleaner.tracker.Tracker.StartListener;
import es.eucm.gleaner.tracker.http.SimpleHttpResponse;

import java.io.IOException;
import java.io.Writer;

public class LocalStorage implements Storage {

	private FileHandle tracesFile;

	private Writer writer;

	/**
	 * @param tracesFile
	 *            file where to write the traces
	 */
	public LocalStorage(FileHandle tracesFile) {
		this.tracesFile = tracesFile;
	}

	@Override
	public void setTracker(Tracker tracker) {
	}

	@Override
	public void start(StartListener startListener) {
		try {
			writer = tracesFile.writer(true);
			startListener.handleHttpResponse(new SimpleHttpResponse("", 200));
		} catch (Exception e) {
			startListener.failed(e);
		}
	}

	@Override
	public void send(String data, FlushListener flushListener) {
		try {
			writer.append(data);
            writer.flush();
			flushListener.handleHttpResponse(new SimpleHttpResponse("", 204));
		} catch (IOException e) {
			flushListener.failed(e);
		}
	}

	@Override
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
