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
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.test.SdjsonTestSuite;

public class LineupTest extends SdjsonTestSuite {
	
	@Test
	public void testConstruction() throws Exception {
		Lineup l = new Lineup(null, null, "/20131021/lineups/CAN-OTA-L1C5M5", null, mock(EpgClient.class));
		assertEquals("CAN-OTA-L1C5M5", l.getId());
		assertEquals("lineups/CAN-OTA-L1C5M5", l.getUri());
	}
	
	@Test
	public void testFetchDetailsMetadataOnly() throws Exception {
		JSONObject map = new JSONObject();
		map.put("stations", new JSONArray());
		map.put("map", new JSONArray());
		map.put("metadata", new JSONObject(String.format("{\"modified\":\"%s\"}", Config.get().getDateTimeFormat().format(new Date()))));
		EpgClient clnt = mock(EpgClient.class);
		Lineup l = new Lineup(null, null, "/20131021/lineups/USA-OTA-90210", null, clnt);
		when(clnt.fetchChannelMapping(l)).thenReturn(map.toString());
		l.fetchDetails(false);
		verify(clnt).fetchChannelMapping(l);
		verifyNoMoreInteractions(clnt);
	}
	
	@Test
	public void testFetchDetailsWithAirings() throws Exception {
		JSONObject map = new JSONObject();
		map.put("stations", new JSONArray());
		map.put("map", new JSONArray());
		map.put("metadata", new JSONObject(String.format("{\"modified\":\"%s\"}", Config.get().getDateTimeFormat().format(new Date()))));
		EpgClient clnt = mock(EpgClient.class);
		Lineup l = new Lineup(null, null, "/20131021/lineups/USA-OTA-90210", null, clnt);
		when(clnt.fetchChannelMapping(l)).thenReturn(map.toString());
		when(clnt.fetchSchedules(l)).thenReturn(Collections.<Station, Airing[]>emptyMap());
		l.fetchDetails(true);
		verify(clnt).fetchChannelMapping(l);
		verify(clnt).fetchSchedules(l);
		verifyNoMoreInteractions(clnt);
	}
}
