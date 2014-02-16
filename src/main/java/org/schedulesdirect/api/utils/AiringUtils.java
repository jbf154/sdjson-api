/**
 * 
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
