/*
 *      Copyright 2014 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package org.schedulesdirect.api.json;

import java.net.URL;

import org.schedulesdirect.api.json.DefaultJsonRequest.Action;

/**
 * Constructs requests that function against the given web serivce URL
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class JsonRequestFactory implements IJsonRequestFactory {
	static private final JsonRequestFactory INSTANCE = new JsonRequestFactory();
	static public JsonRequestFactory get() { return INSTANCE; }

	@Override
	public DefaultJsonRequest get(DefaultJsonRequest.Action action, String resource, String hash, String userAgent, String baseUrl) {
		return new DefaultJsonRequest(action, resource, hash, userAgent, baseUrl);
	}
	
	@Override
	public DefaultJsonRequest get(DefaultJsonRequest.Action action, String resource) {
		return new DefaultJsonRequest(action, resource);
	}

	@Override
	public DefaultJsonRequest get(Action action, URL url) {
		String[] data = url.getPath().substring(1).split("\\/", 2);
		return new DefaultJsonRequest(action, data[1]);
	}
}
