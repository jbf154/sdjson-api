/*
 *      Copyright 2013-2014 Battams, Derek
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
 * Defined response codes from the Schedules Direct JSON service
 * <p>See wiki for complete list.</p>
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class ApiResponse {
	static public final int OK = 0;
	
	static public final int INVALID_JSON = 1001;
	static public final int API_VERSION_MISSING = 1002;
	static public final int INVALID_API_VERSION = 1003;
	static public final int HASH_MISSING = 1004;
	
	static public final int SERVICE_OFFLINE = 3000;
	
	static public final int NO_LINEUPS = 4102;
	
	static public final int INVALID_PROGID = 6000;
	static public final int PROGRAMID_QUEUED = 6001;
	
	static public final int SCHEDULE_QUEUED = 7000;
	
	static public final int NOT_PROVIDED = -1;

	private ApiResponse() {}

}
