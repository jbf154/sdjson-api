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
package org.schedulesdirect.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {
	static public final double DBL_DELTA = 1e-8;
	static public final File UT_DATA_ROOT;
	static public final Properties TEST_PROPS = new Properties();
	static {
		// set UT_DATA_ROOT
		String prop = System.getProperty("sdjon.test.unit.data.root");
		UT_DATA_ROOT = new File(prop == null ? "ut_data" : prop);
		
		// set TEST_PROPS
		try {
			InputStream ins = new FileInputStream(new File(System.getProperty("user.home"), ".sdjson_testng"));
			try {
				TEST_PROPS.load(ins);
			} finally {
				if(ins != null) ins.close();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
