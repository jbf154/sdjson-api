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

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

/**
 * Represents a message object as received from the SD server.
 * <p>Messages can be acknowledged and deleted on the server to prevent constant sending.</p>
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class Message {

	private String id;
	private Date date;
	private String content;
	private EpgClient clnt;
	
	/**
	 * Constructor
	 * @param src The source JSON object
	 * @param clnt The EpgClient instance to which this message belongs
	 * @throws InvalidJsonObjectException Thrown if the src is an invalid object
	 */
	Message(JSONObject src, EpgClient clnt) throws InvalidJsonObjectException {
		try {
			date = Config.get().getDateTimeFormat().parse(src.getString("date"));
			id = src.getString("msgID");
		} catch(JSONException | ParseException e) {
			throw new InvalidJsonObjectException(e);
		}
		content = src.optString("message");
		this.clnt = clnt;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Acknowledge receipt of and delete this message from the SD servers; prevents it from being resent in the future
	 * @throws IOException Thrown in case of any IO error
	 */
	public void delete() throws IOException {
		clnt.deleteMessage(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [id=");
		builder.append(id);
		builder.append(", date=");
		builder.append(date);
		builder.append(", content=");
		builder.append(content);
		builder.append(", clnt=");
		builder.append(clnt);
		builder.append("]");
		return builder.toString();
	}
}
