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

/**
 * Supported API resources
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class RestNouns {
	
	static public final String LOGIN_TOKEN = "token";
	static public final String STATUS = "status";
	static public final String HEADENDS = "headends";
	static public final String PROGRAMS = "programs";
	static public final String LINEUPS = "lineups";
	static public final String SCHEDULES = "schedules";
	static public final String SCHEDULE_MD5S = SCHEDULES + "/md5";
	static public final String MESSAGES = "messages";
	static public final String AVAILABLE = "available";
	static public final String METADATA = "metadata" + "/" + PROGRAMS;
	
	private RestNouns() {}
}
