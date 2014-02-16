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
package org.schedulesdirect.api.utils;

/**
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class UriUtils {

	/**
	 * Given an absolute uri, strip the api version info from it and return a uri relative to the api version
	 * @param uri The uri to strip
	 * @return The stripped uri, which is relative to the api version details
	 */
	static public String stripApiVersion(String uri) {
		return uri.substring(uri.substring(1).indexOf('/') + 2);
	}
	
	private UriUtils() {}
}
