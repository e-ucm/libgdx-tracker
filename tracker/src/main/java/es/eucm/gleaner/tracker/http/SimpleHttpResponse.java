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
package es.eucm.gleaner.tracker.http;

import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.net.HttpStatus;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SimpleHttpResponse implements HttpResponse {

	private String data;

	private HttpStatus status;

	public SimpleHttpResponse(String data, int status) {
		this.data = data;
		this.status = new HttpStatus(status);
	}

	@Override
	public byte[] getResult() {
		String data = this.data;
		this.data = null;
		return data.getBytes();
	}

	@Override
	public String getResultAsString() {
		String data = this.data;
		this.data = null;
		return data;
	}

	@Override
	public InputStream getResultAsStream() {
		return null;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return null;
	}
}
