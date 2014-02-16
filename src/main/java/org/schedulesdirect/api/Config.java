/*
 *      Copyright 2012-2014 Battams, Derek
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides various configuration options to the API.
 * 
 * <p>Only some constants are made public from this class</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class Config {
	static private final Log LOG = LogFactory.getLog(Config.class);
	
	/**
	 * The version of this API build being used; this is NOT the version of the Schedules Direct service feed
	 */
	static public final String API_VERSION = initApiVersion();
	static private String initApiVersion() {
		for(String p : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
			File jar = new File(p);
			String jarName = jar.getName();
			if(jarName.startsWith("sdjson") && jarName.endsWith(".jar")) {
				JarInputStream jfs = null;
				try {
					jfs = new JarInputStream(new FileInputStream(jar));
					Manifest mf = jfs.getManifest();
					String ver = mf.getMainAttributes().getValue("sdjson-version");
					return ver != null ? ver : "Unknown";
				} catch (IOException e) {
					LOG.error("IOError grabbing API version", e);
					return "Unknown";
				} finally {
					if(jfs != null)
						try {
							jfs.close();
						} catch (IOException e) {
							LOG.warn("IOError on close()", e);
						}
				}
			}
		}
		LOG.warn("Unable to determine API version!  Setting to 'Unknown'.");
		return "Unknown";
	}

	/**
	 * The default URL for contacting the Schedules Direct JSON data feed server
	 */
	static public final String DEFAULT_BASE_URL = "https://data2.schedulesdirect.org";
	static private Config INSTANCE = null;
	/**
	 * Obtain the singleton instance of the Config class
	 * @return The lone, global instance of the Config class
	 */
	static public Config get() {
		if(INSTANCE == null)
			INSTANCE = new Config();
		return INSTANCE;
	}
	
	private boolean debugOverrideActive;
	private boolean captureZipResponses;
	private String dateTimeFmt;
	
	private Config() {
		debugOverrideActive = System.getenv("SDJSON_DEBUG") != null;
		captureZipResponses = System.getenv("SDJSON_SAVE_SRV_ZIPS") != null;
		dateTimeFmt = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	}

	/**
	 * Return the expected format string for all date/time values in the upstream JSON
	 * @return The format string suitable for use in SimpleDateFormat constructor
	 */
	public String getDateTimeFormatString() { return dateTimeFmt; }
	
	/**
	 * Get a SimpleDateFormat instance for the configured date/time format string
	 * @return The SimpleDateFormat instance
	 */
	public SimpleDateFormat getDateTimeFormat() {
		SimpleDateFormat fmt = new SimpleDateFormat(dateTimeFmt);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt;
	}
	
	/**
	 * If true, various responses from the Schedules Direct server are ignored.
	 * 
	 * <p>
	 * When true, the client will ignore various action directives from the server and proceed.
	 * One example is if this flag is true then the client will ignore the SD server if it says
	 * there is no new data.  Instead it will assume there is always new data and attempt a full
	 * download.  This should only be enabled during client development and testing; it should
	 * never be enabled in a production environment!
	 * </p>
	 * @return The current state of the debug override flag
	 */
	public boolean isDebugOverrideActive() { return debugOverrideActive; }
	
	/**
	 * If true, all zip file responses from the Schedules Direct server are saved to disk.
	 * 
	 * <p>
	 * During normal operation, the client simply processes all zip file responses from the SD
	 * server in memory then discards the data stream.  When this debug flag is turned on, the
	 * client will save all zip file responses to a zip file on disk.  This is used for development
	 * and debugging of the client and should never be enabled in a production environment.
	 * </p>
	 * @return The current state of the capture zip debug flag
	 */
	public boolean isCaptureZipResponsesActive() { return captureZipResponses; }	
}