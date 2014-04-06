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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.utils.Logging;
import org.schedulesdirect.test.utils.SampleData;
import org.schedulesdirect.test.utils.SampleData.SampleType;

public class LineupTest extends SdjsonTestSuite {
	static private final Log LOG = Logging.getLogger(LineupTest.class);
	static public final List<String> SAMPLE_DATA = new ArrayList<String>();
	static private final Random RNG = new Random();
	
	@BeforeClass
	static public void init() throws Exception {
		initJsonData();
		loadAllSamples();
	}

	@AfterClass
	static public void cleanup() {
		SAMPLE_DATA.clear();
	}

	static private void initJsonData() throws Exception {
		try {
			if(!SampleData.updateFor(SampleType.LINEUPS, false))
				LOG.info("Using current sample data because it's less than a week old.");
		} catch(Exception e) {
			if(!SampleData.exists(SampleType.LINEUPS))
				throw new IOException("No sample data available!", e);
			else
				LOG.warn("Failed to download fresh sample data; using existing data instead!");
		}
		LineIterator itr = FileUtils.lineIterator(SampleData.locate(SampleType.LINEUPS), "UTF-8");
		while(itr.hasNext())
			SAMPLE_DATA.add(itr.nextLine());
	}
	
	static private void loadAllSamples() throws Exception {
		final String PROP = "sdjson.capture.encode-errors";
		String capVal = System.setProperty(PROP, "1");
		assertTrue(Config.get().captureJsonEncodingErrors());
		int failed = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < SAMPLE_DATA.size(); ++i) {
			try {
				EpgClient clnt = mock(EpgClient.class);
				Lineup l = new Lineup("Test Lineup", "Test Location", "/20131021/lineups/CAN-OTA-A0A0A0", "Cable", clnt);
				when(clnt.fetchChannelMapping(l)).thenReturn(SAMPLE_DATA.get(i));
				l.fetchDetails(false);
			} catch(IOException e) {
				sb.append(String.format("\t(line %d) %s%n", i + 1, e.getMessage()));
				SAMPLE_DATA.set(i, null);
				++failed;
			}
		}
		if(capVal == null)
			System.clearProperty(PROP);
		else
			System.setProperty(PROP, capVal);
		
		if(failed > 0) {
			String msg = String.format("%d of %d samples (%s%%) failed to load!%n%s", failed, SAMPLE_DATA.size(), String.format("%.2f", 100.0F * failed / SAMPLE_DATA.size()), sb);
			if(failed < SAMPLE_DATA.size() / 10)
				LOG.warn(msg);
			else {
				LOG.error(msg);
				throw new IOException("Too many load failures! Halting testing now.");
			}
		}			
	}

	private String getRandomSampleProgram() {
		String s = null;
		while(s == null) s = SAMPLE_DATA.get(RNG.nextInt(SAMPLE_DATA.size()));
		return s;
	}

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
