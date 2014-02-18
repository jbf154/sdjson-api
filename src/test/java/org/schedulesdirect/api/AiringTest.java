package org.schedulesdirect.api;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.test.utils.SampleData;
import org.schedulesdirect.test.utils.SampleData.SampleType;

@PowerMockIgnore({"org.apache.http.*", "org.apache.log4j.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Program.class, Station.class})
public class AiringTest {

	static public final List<String> SAMPLE_DATA = new ArrayList<String>();

	static private final Random RNG = new Random();

	@BeforeClass
	public static void init() throws Exception {
		initJsonData();
		loadAllSamples();
	}

	@AfterClass
	static public void cleanup() {
		SAMPLE_DATA.clear();
	}

	static private void initJsonData() throws Exception {
		try {
			if(!SampleData.updateFor(SampleType.SCHEDULES, false));
				//Reporter.log("Using current sample data because it's less than a week old.");
		} catch(Exception e) {
			if(!SampleData.exists(SampleType.SCHEDULES))
				throw new IOException("No sample data available!", e);
//			else
//				Reporter.log("Error downloading fresh sample data; using existing data instead!");
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
						new Airing(airings.getJSONObject(j), p, s);
					} catch(InvalidJsonObjectException e) {
						sb.append(String.format("\t(line %d:%d) %s: %s%n", i + 1, j, input.optString("programID", "<UNKNOWN>"), e.getMessage()));
						SAMPLE_DATA.set(i, null);
						++failed;
					}
				}
		}
//		if(failed > 0)
//			Reporter.log(String.format("<pre>%n%d of %d samples (%s%%) failed to load!%n%s</pre>", failed, SAMPLE_DATA.size(), String.format("%.2f", 100.0F * failed / SAMPLE_DATA.size()), sb));
//		else
//			Reporter.log("No load failures!");
		if(failed >= SAMPLE_DATA.size() / 10)
			throw new IOException("Too many load failures! Halting testing now.");
	}
	
	private String getRandomSampleProgram() {
		String s = null;
		while(s == null) s = SAMPLE_DATA.get(RNG.nextInt(SAMPLE_DATA.size()));
		return s;
	}

	
	@Test
	public void Airing() {
		throw new RuntimeException("Test not implemented");
	}
/*
	@Test
	public void setId() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void setProgram() {
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void setStation() {
		throw new RuntimeException("Test not implemented");
	}
*/
}
