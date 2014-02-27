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

import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.ApiResponse;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

/**
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class JsonResponseUtils {

	static public int getErrorCode(JSONObject resp) throws InvalidJsonObjectException {
		try {
			return resp.getInt("code");
		} catch(JSONException e) {
			throw new InvalidJsonObjectException("ErrorResponse: Not an error!", e, resp.toString(3));
		}
	}
	
	static public boolean isErrorResponse(JSONObject resp) {
		try {
			return resp.has("code") && getErrorCode(resp) != ApiResponse.OK;
		} catch(InvalidJsonObjectException e) {
			return false;
		}
	}
	
	private JsonResponseUtils() {}
}
