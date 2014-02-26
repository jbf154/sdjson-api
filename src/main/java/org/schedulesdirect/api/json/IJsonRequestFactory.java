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

/**
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public interface IJsonRequestFactory {
	/**
	 * Construct a complete JsonRequest suitable for execution.
	 * @param action The action to execute
	 * @param resource The resource to execute against
	 * @param hash The user auth hash or null if not available/not required
	 * @param userAgent The user agent to pass to the server
	 * @param baseUrl The service base URL
	 * @return The constructed JsonRequest, ready to be executed
	 */
	public JsonRequest get(JsonRequest.Action action, String resource, String hash, String userAgent, String baseUrl);
	
	/**
	 * Construct a partial JsonRequest that CANNOT be executed; used as skeleton for a clone construction
	 * @param action The action to execute
	 * @param resource The resource to execute against
	 * @return The partial JsonRequest object; use this object as the basis for constructing a full one
	 */
	public JsonRequest get(JsonRequest.Action action, String resource);
}
