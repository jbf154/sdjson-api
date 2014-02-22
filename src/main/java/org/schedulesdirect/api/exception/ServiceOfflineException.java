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
package org.schedulesdirect.api.exception;

/**
 * Represents an exception that is thrown as a result of the Schedules Direct web service reporting itself as OFFLINE/unavailable
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class ServiceOfflineException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public ServiceOfflineException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ServiceOfflineException(Throwable cause) {
		super(cause);
	}
}
