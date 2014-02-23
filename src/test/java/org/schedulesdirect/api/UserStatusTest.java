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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.text.DateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.test.SdjsonTestSuite;

public class UserStatusTest extends SdjsonTestSuite {
	
	static private UserStatus build(Date d) {
		final DateFormat fmt = Config.get().getDateTimeFormat();
		JSONObject o = new JSONObject();
		o.put("lastServerRefresh", fmt.format(d));
		o.put("notifications", new JSONArray());
		o.put("lineups", new JSONArray());
		o.put("lastDataUpdate", fmt.format(d));
		JSONObject acct = new JSONObject();
		acct.put("expires", fmt.format(d));
		acct.put("maxLineups", 4);
		acct.put("messages", new JSONArray());
		acct.put("nextSuggestedConnectTime", fmt.format(d));
		o.put("account", acct);
		return new UserStatus(o, "fake", mock(EpgClient.class));		
	}
	
	@Test
	public void testConstruction() {
		Date d = new Date();
		UserStatus us = build(d);
		Date expectedDate = new Date(d.getTime() - (d.getTime() % 1000));
		assertEquals(expectedDate, us.getExpires());
		assertEquals(expectedDate, us.getLastServerRefresh());
		assertEquals(expectedDate, us.getNextSuggestedConnectTime());
		assertEquals(4, us.getMaxLineups());
	}
	
	@Test
	public void testIsExpired() {
		Date d = new Date();
		UserStatus us = build(d);
		assertTrue(us.isExpired());
		d = new Date(d.getTime() + 86400L * 1000L * 30L);
		us = build(d);
		assertFalse(us.isExpired());
	}
	
	@Test
	public void testIsNewDataAvailable() {
		Date d = new Date();
		UserStatus us = build(d);
		assertTrue(us.isNewDataAvailable(new Date(d.getTime() - 86400L * 1000L)));
	}
}
