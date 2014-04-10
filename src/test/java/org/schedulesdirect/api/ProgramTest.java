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

import java.io.IOException;
import java.text.ParseException;
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
import org.schedulesdirect.api.Program.Credit;
import org.schedulesdirect.api.Program.Role;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.TestConfig;
import org.schedulesdirect.test.utils.Logging;
import org.schedulesdirect.test.utils.SampleData;
import org.schedulesdirect.test.utils.SampleData.SampleType;

public class ProgramTest extends SdjsonTestSuite {
	static private final Log LOG = Logging.getLogger(ProgramTest.class);
	
	static public final String OBJ_TITLES = "titles";
	static public final String TITLES_PROP_MAIN = "title120";
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
			if(!SampleData.updateFor(SampleType.PROGRAMS, false))
				LOG.info("Using current sample data because it's less than a week old.");
		} catch(Exception e) {
			if(!SampleData.exists(SampleType.PROGRAMS))
				throw new IOException("No sample data available!", e);
			else
				LOG.warn("Failed to download fresh sample data; using existing data instead!");
		}
		LineIterator itr = FileUtils.lineIterator(SampleData.locate(SampleType.PROGRAMS), "UTF-8");
		while(itr.hasNext())
			SAMPLE_DATA.add(itr.nextLine());
	}
	
	static private void loadAllSamples() throws Exception {
		final String PROP = "sdjson.capture.json-errors";
		String capVal = System.setProperty(PROP, "1");
		assertTrue(Config.get().captureJsonParseErrors());
		int failed = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < SAMPLE_DATA.size(); ++i) {
			JSONObject input = new JSONObject(SAMPLE_DATA.get(i));
			try {
				new Program(input);
			} catch(InvalidJsonObjectException e) {
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
	
	@Test(expected=IllegalArgumentException.class)
	public void validateNullCtor() throws Exception {
		new Program(null);
	}
	
	@Test(expected=InvalidJsonObjectException.class)
	public void validateNoTitlesObjInCtor() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		input.remove(OBJ_TITLES);
		new Program(input);
	}

	@Test(expected=InvalidJsonObjectException.class)
	public void validateNoMainTitleInTitlesInCtor() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		JSONObject o = input.getJSONObject(OBJ_TITLES);
		o.remove(TITLES_PROP_MAIN);
		input.put(OBJ_TITLES, o);
		new Program(input);
	}

	@Test(expected=InvalidJsonObjectException.class)
	public void validateNoProgIdInCtor() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		input.remove("programID");
		new Program(input);
	}

	@Test
	public void validateUnknownCredit() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		JSONArray credits = new JSONArray();
		credits.put(new JSONObject("{\"role\":\"bad role\",\"name\": \"John Doe\"}"));
		input.put("crew", credits);
		Program p = new Program(input);
		Credit[] array = p.getCredits();
		assertEquals(1, array.length);
		Credit c = array[0];
		assertEquals(Role.UNKNOWN, c.getRole());
		assertEquals("John Doe", c.getName());	
	}

	@Test
	public void validateKnownRoleCredit() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		JSONArray credits = new JSONArray();
		credits.put(new JSONObject("{\"role\":\"Guest Star\",\"name\": \"John Doe\"}"));
		input.put("cast", credits);
		Program p = new Program(input);
		Credit[] array = p.getCredits();
		assertEquals(1, array.length);
		Credit c = array[0];
		assertEquals(Role.GUEST_STAR, c.getRole());
		assertEquals("John Doe", c.getName());	
	}
	
	@Test(expected=InvalidJsonObjectException.class)
	public void validateInvalidCreditInput() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		JSONArray credits = new JSONArray();
		credits.put("John Doe");
		input.put("crew", credits);
		new Program(input);
	}

	@Test(expected=InvalidJsonObjectException.class)
	public void validateInvalidGameTime() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		input.put("gameDatetime", "invalid");
		new Program(input);
	}

	@Test(expected=InvalidJsonObjectException.class)
	public void validateInvalidOriginalAirDate() throws Exception {
		JSONObject input = new JSONObject(getRandomSampleProgram());
		input.put("originalAirDate", "invalid");
		new Program(input);
	}
	
	@Test
	public void calcZeroStarRatingForNull() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating(null);
		assertEquals(0.0F, p.getStarRatingValue(), TestConfig.DBL_DELTA);
	}

	@Test
	public void calcZeroStarRatingForEmptyStr() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("");
		assertEquals(0.0F, p.getStarRatingValue(), TestConfig.DBL_DELTA);
	}

	@Test
	public void calcWholeStarRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("***");
		assertEquals(3.0F, p.getStarRatingValue(), TestConfig.DBL_DELTA);
	}

	@Test
	public void calcHalfStarRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("+");
		assertEquals(0.5F, p.getStarRatingValue(), TestConfig.DBL_DELTA);
	}

	@Test
	public void calcWholeAndHalfStarRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("***+");
		assertEquals(3.5F, p.getStarRatingValue(), TestConfig.DBL_DELTA);
	}

	@Test(expected=ParseException.class)
	public void calcInvalidCharRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("%");
	}

	@Test(expected=ParseException.class)
	public void calcValidAndInvalidEndCharRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("*+b");
	}

	@Test(expected=ParseException.class)
	public void calcValidAndInvaidFirstCharRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("$**+");
	}

	@Test(expected=ParseException.class)
	public void calcValidAndInvalidMiddleCharRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("**42+");
	}
	
	@Test(expected=ParseException.class)
	public void calcHalfStarInMiddleRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("**+*");
	}
	
	@Test(expected=ParseException.class)
	public void calcMultipleHalfsRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("**++");
	}
	
	@Test(expected=ParseException.class)
	public void calcTooBigRating() throws Exception {
		Program p = new Program(new JSONObject(getRandomSampleProgram()));
		p.setStarRating("****+");
	}
}
