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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.test.SdjsonTestSuite;

public class StationTest extends SdjsonTestSuite {
	
	static private final String BASIC_STATION_JSON = "{\"logo\":{\"dimension\":\"w=360px|h=270px\",\"URL\":\"https://s3.amazonaws.com/schedulesdirect/sources/h3/NowShowing/10098/s10098_h3_aa.png\"},\"affiliate\":\"CBS Affiliate\",\"name\":\"WWNYDT (WWNY-DT)\",\"broadcaster\":{\"postalcode\":\"13601\",\"state\":\"NY\",\"country\":\"United States\",\"city\":\"Watertown\"},\"stationID\":\"35045\",\"language\":\"English\",\"callsign\":\"WWNYDT\"}";
	
	@Test
	public void validateNoPhysicalChannel() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(0);
		s.setAtscMajorNumber(1);
		s.setAtscMinorNumber(21);
		assertNull(s.getPhysicalChannelNumber());
	}
	
	@Test
	public void validateNoLogicalMinor() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setAtscMinorNumber(0);
		s.setAtscMajorNumber(4);
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateNoLogicalMajor() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setAtscMajorNumber(0);
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateValidChannelNTSC() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(15);
		s.setAtscMajorNumber(0);
		s.setAtscMinorNumber(0);
		assertEquals("15", s.getPhysicalChannelNumber());
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateValidChannelATSC() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(31);
		s.setAtscMajorNumber(4);
		s.setAtscMinorNumber(2);
		assertEquals("31-4-2", s.getPhysicalChannelNumber());
		assertEquals("4-2", s.getLogicalChannelNumber());
	}
	
	@Test
	public void testLogoDimensionParsing() throws Exception {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		assertEquals(360, s.getLogo().getWidth());
		assertEquals(270, s.getLogo().getHeight());
	}
}
