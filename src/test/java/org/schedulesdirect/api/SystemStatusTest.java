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
package org.schedulesdirect.api;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.test.SdjsonTestSuite;

public class SystemStatusTest extends SdjsonTestSuite {

	@Test
	public void validateFindsNewestMessage() throws Exception {
		JSONArray a = new JSONArray();
		JSONObject o = new JSONObject();
		o.put("status", "foo");
		o.put("details", "foo");
		o.put("date", Config.get().getDateTimeFormat().format(new Date(0)));
		a.put(o);
		o = new JSONObject();
		o.put("status", "bar");
		o.put("details", "bar");
		o.put("date", Config.get().getDateTimeFormat().format(new Date()));
		a.put(o);
		SystemStatus s = new SystemStatus(a);
		assertEquals("bar", s.getStatus());
	}
	
	@Test(expected=InvalidJsonObjectException.class)
	public void testInvalidObject() throws Exception {
		JSONArray a = new JSONArray();
		JSONObject o = new JSONObject();
		o.put("status", "foo");
		o.put("details", "foo");
		o.put("date", Config.get().getDateTimeFormat().format(new Date(0)));
		a.put(o);
		o = new JSONObject();
		o.put("status", "bar");
		o.put("date", Config.get().getDateTimeFormat().format(new Date()));
		a.put(o);
		new SystemStatus(a);
	}
}
