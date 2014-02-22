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

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.Config;

/**
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class AiringUtils {

	/**
	 * Calculate the end date of an Airing
	 * @param src An Airing object as JSON
	 * @return The Date when the given airing is scheduled to end
	 * @throws JSONException If the src is not valid
	 */
	static public Date getEndDate(JSONObject src) throws JSONException {
		try {
			return new Date(Config.get().getDateTimeFormat().parse(src.getString("airDateTime")).getTime() + (src.getLong("duration") * 1000L));
		} catch(ParseException e) {
			throw new RuntimeException(e);
		}
	}

	
	private AiringUtils() {}
}
