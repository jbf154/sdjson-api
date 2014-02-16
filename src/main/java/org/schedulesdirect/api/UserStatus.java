/*
 *      Copyright 2012-2013 Battams, Derek
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.api.exception.InvalidResponseException;

/**
 * UserStatus encapsulates the authenticated user's details with respect to the EpgClient session used to access the instance
 * 
 * <p>At this time, UserStatus is only capable of telling you when your account expires with the upstream data feed.
 * For the Network client, this will tell you when your Schedules Direct account expires, but for the Zip client
 * it won't tell you anything useful (because a "subscription" to your zip file of data never expires).</p>
 * 
 * <p>Only EpgClient can instantiate objects of this class</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class UserStatus {
		
	private String userId;
	private Date expires;
	private Date lastServerRefresh;
	private Date nextSuggestedConnectTime;
	private Message[] userMessages;
	private Message[] systemMessages;
	private Map<String, Date> headendInfo;
	private String jsonEncoding;
	private int maxLineups;

	/**
	 * Constructor
	 * @param src The JSON object received from upstream
	 * @param userId The Schedules Direct user id associated with this client
	 * @param clnt The EpgClient instance to attach this status to
	 * @throws InvalidJsonObjectException Thrown if the src is not in the expected format
	 */
	UserStatus(JSONObject src, String userId, EpgClient clnt) throws InvalidResponseException {
		try {
			if(userId != null)
				this.userId = userId;
			else
				this.userId = src.getString("userId");
			JSONObject acct = src.getJSONObject("account");
			final SimpleDateFormat fmt = Config.get().getDateTimeFormat();
			nextSuggestedConnectTime = fmt.parse(acct.getString("nextSuggestedConnectTime"));
			expires = fmt.parse(acct.getString("expires"));
			JSONArray msgs = acct.getJSONArray("messages");
			userMessages = new Message[msgs.length()];
			for(int i = 0; i < msgs.length(); ++i)
				userMessages[i] = new Message(msgs.getJSONObject(i), clnt);
			lastServerRefresh = fmt.parse(src.getString("lastDataUpdate"));
			msgs = src.getJSONArray("notifications");
			systemMessages = new Message[msgs.length()];
			for(int i = 0; i < msgs.length(); ++i)
				systemMessages[i] = new Message(msgs.getJSONObject(i), clnt);
			headendInfo = new HashMap<String, Date>();
			msgs = src.getJSONArray("lineups");
			for(int i = 0; i < msgs.length(); ++i) {
				JSONObject heInfo = msgs.getJSONObject(i);
				headendInfo.put(heInfo.getString("ID"), fmt.parse(heInfo.getString("modified")));
			}
			maxLineups = acct.getInt("maxLineups");
			JSONObject clone = new JSONObject(src.toString());
			clone.put("userId", this.userId);
			jsonEncoding = clone.toString(3);
		} catch(JSONException e) {
			throw new InvalidResponseException(e);
		} catch(ParseException e) {
			throw new InvalidResponseException(e);
		}
	}

	/**
	 * 
	 * @return A JSON encoding of this object
	 */
	public String toJson() {
		return jsonEncoding;
	}
	
	/**
	 * @return The date of the user's expiration
	 */
	public Date getExpires() {
		return expires;
	}
	
	/**
	 * 
	 * @return A boolean denoting if the user's access to the upstream data feed has expired AS OF THE TIME THIS METHOD WAS CALLED
	 */
	public boolean isExpired() {
		return expires.getTime() < System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @param lastDownload The time this user last downloaded data from the Schedules Direct servers
	 * @return A boolean denoting if Schedules Direct has refreshed its data server side based on the Date object argument received
	 */
	public boolean isNewDataAvailable(Date lastDownload) {
		return lastDownload != null && lastDownload.before(lastServerRefresh);
	}
	
	/**
	 * @return The last time the server reports it refreshed its EPG data on the Schedules Direct servers
	 */
	public Date getLastServerRefresh() {
		return lastServerRefresh;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 2;
		StringBuilder builder = new StringBuilder();
		builder.append("UserStatus [userId=");
		builder.append(userId);
		builder.append(", expires=");
		builder.append(expires);
		builder.append(", lastServerRefresh=");
		builder.append(lastServerRefresh);
		builder.append(", nextSuggestedConnectTime=");
		builder.append(nextSuggestedConnectTime);
		builder.append(", userMessages=");
		builder.append(userMessages != null ? Arrays.asList(userMessages)
				.subList(0, Math.min(userMessages.length, maxLen)) : null);
		builder.append(", systemMessages=");
		builder.append(systemMessages != null ? Arrays.asList(systemMessages)
				.subList(0, Math.min(systemMessages.length, maxLen)) : null);
		builder.append(", headendInfo=");
		builder.append(headendInfo != null ? toString(headendInfo.entrySet(),
				maxLen) : null);
		builder.append(", jsonEncoding=");
		builder.append(jsonEncoding);
		builder.append(", maxLineups=");
		builder.append(maxLineups);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the userMessages
	 */
	public Message[] getUserMessages() {
		return userMessages;
	}

	/**
	 * @return the systemMessages
	 */
	public Message[] getSystemMessages() {
		return systemMessages;
	}

	/**
	 * @return the headendInfo
	 */
	public Map<String, Date> getHeadendInfo() {
		Map<String, Date> map = new HashMap<String, Date>();
		for(String k : headendInfo.keySet())
			map.put(k, (Date)headendInfo.get(k).clone());
		return map;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the maxLineups
	 */
	public int getMaxLineups() {
		return maxLineups;
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the nextSuggestedConnectTime
	 */
	public Date getNextSuggestedConnectTime() {
		return nextSuggestedConnectTime;
	}
}
