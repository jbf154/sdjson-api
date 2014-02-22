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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
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
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.utils.Logging;
import org.schedulesdirect.test.utils.SampleData;
import org.schedulesdirect.test.utils.SampleData.SampleType;

//@PowerMockIgnore({"org.apache.http.*", "org.apache.log4j.*"})
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({Program.class, Station.class})
public class AiringTest extends SdjsonTestSuite {
	static private final Log LOG = Logging.getLogger(AiringTest.class);
	
	static public final List<String> SAMPLE_DATA = new ArrayList<>();
	static public final List<String> SAMPLE_AIRS = new ArrayList<>();

	static private final Random RNG = new Random();

	@BeforeClass
	public static void init() throws Exception {
		initJsonData();
		loadAllSamples();
	}

	@AfterClass
	static public void cleanup() {
		SAMPLE_AIRS.clear();
	}

	static private void initJsonData() throws Exception {
		try {
			if(!SampleData.updateFor(SampleType.SCHEDULES, false));
				LOG.info("Using current sample data because it's less than a week old.");
		} catch(Exception e) {
			if(!SampleData.exists(SampleType.SCHEDULES))
				throw new IOException("No sample data available!", e);
			else
				LOG.warn("Error downloading fresh sample data; using existing data instead!");
		}
		LineIterator itr = FileUtils.lineIterator(SampleData.locate(SampleType.SCHEDULES), "UTF-8");
		while(itr.hasNext())
			SAMPLE_DATA.add(itr.nextLine());
	}
	
	static private void loadAllSamples() throws Exception {
		int failed = 0;
		StringBuilder sb = new StringBuilder();
		Station s = mock(Station.class);
		for(int i = 0; i < SAMPLE_DATA.size(); ++i) {
			JSONObject input = new JSONObject(SAMPLE_DATA.get(i));
			JSONArray airings = input.optJSONArray("programs");
			if(airings != null)
				for(int j = 0; j < airings.length(); ++j) {
					JSONObject a = airings.getJSONObject(j);
					Program p = mock(Program.class);
					when(p.getId()).thenReturn(a.getString("programID"));
					try {
						JSONObject o = airings.getJSONObject(j);
						new Airing(o, p, s);
						SAMPLE_AIRS.add(o.toString());
					} catch(InvalidJsonObjectException e) {
						sb.append(String.format("\t(line %d:%d) %s: %s%n", i + 1, j, input.optString("programID", "<UNKNOWN>"), e.getMessage()));
						++failed;
					}
				}
		}
		if(failed > 0)
			LOG.warn(String.format("%d of %d samples (%s%%) failed to load!%n%s%n", failed, SAMPLE_DATA.size(), String.format("%.2f", 100.0F * failed / SAMPLE_DATA.size()), sb));
		else if(LOG.isDebugEnabled())
			LOG.debug("No load failures!");
		if(failed >= SAMPLE_DATA.size() / 10)
			throw new IOException("Too many load failures! Halting testing now.");
		SAMPLE_DATA.clear();
	}
	
	private String getRandomSample() {
		return SAMPLE_AIRS.get(RNG.nextInt(SAMPLE_AIRS.size()));
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void setNullId() {
		JSONObject src = new JSONObject(getRandomSample());
		Program p = mock(Program.class);
		when(p.getId()).thenReturn(src.getString("programID"));
		Station s = mock(Station.class);
		Airing a = new Airing(src, p, s);
		a.setId(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setNonMatchingId() {
		JSONObject src = new JSONObject(getRandomSample());
		Program p = mock(Program.class);
		when(p.getId()).thenReturn(src.getString("programID"));
		Station s = mock(Station.class);
		Airing a = new Airing(src, p, s);
		a.setId("foobar");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setNullStation() {
		JSONObject src = new JSONObject(getRandomSample());
		Program p = mock(Program.class);
		when(p.getId()).thenReturn(src.getString("programID"));
		Station s = mock(Station.class);
		Airing a = new Airing(src, p, s);
		a.setStation(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setNullProgram() {
		JSONObject src = new JSONObject(getRandomSample());
		Program p = mock(Program.class);
		when(p.getId()).thenReturn(src.getString("programID"));
		Station s = mock(Station.class);
		Airing a = new Airing(src, p, s);
		a.setProgram(null);
	}
	
	@Test
	public void setProgram() {
		JSONObject src = new JSONObject(getRandomSample());
		Program p = mock(Program.class);
		when(p.getId()).thenReturn(src.getString("programID"));
		Station s = mock(Station.class);
		Airing a = new Airing(src, p, s);
		p = mock(Program.class);
		when(p.getId()).thenReturn("foobar");
		assertEquals(src.getString("programID"), a.getId());
		a.setProgram(p);
		assertEquals("foobar", a.getId());
	}

}
