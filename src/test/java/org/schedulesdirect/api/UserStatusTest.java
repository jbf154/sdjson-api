/*
 *      Copyright 2014-2015 Battams, Derek
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.utils.JsonResponseBuilder;

public class UserStatusTest extends SdjsonTestSuite {
	
	static public UserStatus build(Date d) throws Exception {
		return new UserStatus(new JSONObject(JsonResponseBuilder.buildStatusResponse(d)), "fake", mock(EpgClient.class));	
	}
	
	@Test
	public void testConstruction() throws Exception {
		Date d = new Date();
		UserStatus us = build(d);
		Date expectedDate = new Date(d.getTime() - (d.getTime() % 1000));
		assertEquals(expectedDate, us.getExpires());
		assertEquals(expectedDate, us.getLastServerRefresh());
		assertEquals(4, us.getMaxLineups());
	}
	
	@Test
	public void testIsExpired() throws Exception {
		Date d = new Date(System.currentTimeMillis() - 3600000L);
		UserStatus us = build(d);
		assertTrue(us.isExpired());
		d = new Date(d.getTime() + 86400L * 1000L * 30L);
		us = build(d);
		assertFalse(us.isExpired());
	}
	
	@Test
	public void testIsNewDataAvailable() throws Exception {
		Date d = new Date();
		UserStatus us = build(d);
		assertTrue(us.isNewDataAvailable(new Date(d.getTime() - 86400L * 1000L)));
	}
}
