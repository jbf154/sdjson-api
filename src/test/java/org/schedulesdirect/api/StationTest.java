package org.schedulesdirect.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.json.JSONObject;
import org.junit.Test;

public class StationTest {
	
	static private final String BASIC_STATION_JSON = "{\"logo\":{\"dimension\":\"w=360px|h=270px\",\"URL\":\"https://s3.amazonaws.com/schedulesdirect/sources/h3/NowShowing/10098/s10098_h3_aa.png\"},\"affiliate\":\"CBS Affiliate\",\"name\":\"WWNYDT (WWNY-DT)\",\"broadcaster\":{\"postalcode\":\"13601\",\"state\":\"NY\",\"country\":\"United States\",\"city\":\"Watertown\"},\"stationID\":\"35045\",\"language\":\"English\",\"callsign\":\"WWNYDT\"}";
	
	@Test
	public void validateNoPhysicalChannel() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(0);
		s.setAtscMajorNumber(1);
		s.setAtscMinorNumber(21);
		assertNull(s.getPhysicalChannelNumber());
	}
	
	@Test
	public void validateNoLogicalMinor() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setAtscMinorNumber(0);
		s.setAtscMajorNumber(4);
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateNoLogicalMajor() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setAtscMajorNumber(0);
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateValidChannelNTSC() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(15);
		s.setAtscMajorNumber(0);
		s.setAtscMinorNumber(0);
		assertEquals("15", s.getPhysicalChannelNumber());
		assertNull(s.getLogicalChannelNumber());
	}
	
	@Test
	public void validateValidChannelATSC() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		s.setUhfVhfNumber(31);
		s.setAtscMajorNumber(4);
		s.setAtscMinorNumber(2);
		assertEquals("31-4-2", s.getPhysicalChannelNumber());
		assertEquals("4-2", s.getLogicalChannelNumber());
	}
	
	@Test
	public void testLogoDimensionParsing() {
		Station s = new Station(new JSONObject(BASIC_STATION_JSON), null, mock(EpgClient.class));
		assertEquals(360, s.getLogo().getWidth());
		assertEquals(270, s.getLogo().getHeight());
	}
}
