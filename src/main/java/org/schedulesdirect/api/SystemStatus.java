/*
 *      Copyright 2013-2014 Battams, Derek
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
package org.schedulesdirect.api;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

/**
 * An encapsulation of the current state of the Schedules Direct system.
 * <p>Only EpgClient and its subclasses can instantiate objects of this class.</p>
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class SystemStatus {
	private Date statusDate;
	private String status;
	private String statusMessage;
	
	SystemStatus(JSONArray src) throws InvalidJsonObjectException {
		for(int i = 0; i < src.length(); ++i) {
			Date objDate;
			try {
				JSONObject obj = src.getJSONObject(i);
				objDate = Config.get().getDateTimeFormat().parse(obj.getString("date"));
				if(statusDate == null || statusDate.before(objDate)) {
					statusDate = objDate;
					status = obj.getString("status");
					statusMessage = obj.getString("details");
				}
			} catch(Exception e) {
				throw new InvalidJsonObjectException(e);
			}
		}
	}

	/**
	 * @return the statusDate
	 */
	public Date getStatusDate() {
		return statusDate;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SystemStatus [statusDate=" + statusDate + ", status=" + status
				+ ", statusMessage=" + statusMessage + "]";
	}
}
