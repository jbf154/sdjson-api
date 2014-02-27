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
package org.schedulesdirect.test.utils;

import java.text.DateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schedulesdirect.api.ApiResponse;
import org.schedulesdirect.api.Config;

public final class JsonResponseBuilder {

	static public String buildSearchResponse() {
		return "{}";
	}
	
	static public String buildProgramResponse() {
		return "{}";
	}
	
	static public String buildScheduleResponse() {
		JSONObject resp = new JSONObject();
		resp.put("programs", new JSONArray());
		return resp.toString();
	}
	
	static public String buildLineupsResponse() {
		JSONObject resp = new JSONObject();
		resp.put("lineups", new JSONArray());
		return resp.toString();
	}
	
	static public String buildErrorResponse() {
		return buildErrorResponse(ApiResponse.NOT_PROVIDED);
	}
	
	static public String buildErrorResponse(int code) {
		final JSONObject resp = new JSONObject();
		resp.put("code", code);
		return resp.toString();
	}
	
	static public String buildStatusResponse(Date date) {
		final Date d = date != null ? date : new Date();
		final DateFormat fmt = Config.get().getDateTimeFormat();
		final JSONObject o = new JSONObject();
		o.put("lastServerRefresh", fmt.format(d));
		o.put("notifications", new JSONArray());
		o.put("lineups", new JSONArray());
		o.put("lastDataUpdate", fmt.format(d));
		final JSONObject acct = new JSONObject();
		acct.put("expires", fmt.format(d));
		acct.put("maxLineups", 4);
		acct.put("messages", new JSONArray());
		acct.put("nextSuggestedConnectTime", fmt.format(d));
		o.put("account", acct);
		o.put("systemStatus", new JSONArray());
		return o.toString();
	}
	
	static public String buildTokenResponse() {
		final JSONObject resp = new JSONObject();
		resp.put("token", "12345abcdefg");
		return resp.toString();
	}
}
