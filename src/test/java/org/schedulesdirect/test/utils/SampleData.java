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
package org.schedulesdirect.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.schedulesdirect.api.NetworkEpgClient;
import org.schedulesdirect.api.exception.InvalidCredentialsException;
import org.schedulesdirect.api.exception.ServiceOfflineException;
import org.schedulesdirect.api.json.JsonRequest;
import org.schedulesdirect.api.json.JsonRequestFactory;
import org.schedulesdirect.test.TestConfig;

/**
 * Utilities for updating and accesssing test data samples from the SD servers
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class SampleData {

	public enum SampleType {
		PROGRAMS,
		SCHEDULES
	}
	
	private static void grabSample(SampleType type, File target) throws IOException {
		try {
			NetworkEpgClient clnt = new NetworkEpgClient(TestConfig.TEST_PROPS.getProperty("SD_USER"), TestConfig.TEST_PROPS.getProperty("SD_PWD"), null, TestConfig.TEST_PROPS.getProperty("SD_URL"), false, JsonRequestFactory.get());
			target.getParentFile().mkdirs();
			try(
				InputStream ins = clnt.submitRequest(JsonRequestFactory.get().get(JsonRequest.Action.GET, String.format("%s/%s", "sample", type.toString().toLowerCase())), null);
				Writer w = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")
			) {
				IOUtils.copy(ins, w);
			}
		} catch(ServiceOfflineException e) {
			throw new IOException(e);
		} catch(InvalidCredentialsException e) {
			throw new IOException(e);
		}
	}
	
	private static boolean isFresh(File f) {
		return f != null && f.canRead() && System.currentTimeMillis() - f.lastModified() < 1000L * 7 * 86400;
	}
	
	public static boolean updateFor(SampleType type, boolean forceUpdate) throws IOException {
		File target = new File(TestConfig.UT_DATA_ROOT, String.format("%s/sample.txt", type.toString().toLowerCase()));
		boolean fresh = isFresh(target);
		if(forceUpdate || !fresh)
			grabSample(type, target);
		return forceUpdate || !fresh;
	}
	
	public static boolean exists(SampleType type) {
		return new File(TestConfig.UT_DATA_ROOT, String.format("%s/sample.txt", type.toString().toLowerCase())).canRead();
	}
	
	public static File locate(SampleType type) {
		return new File(TestConfig.UT_DATA_ROOT, String.format("%s/sample.txt", type.toString().toLowerCase()));
	}	
}
