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
package org.schedulesdirect.api.exception;

/**
 * Thrown when an upstream response does not meet the expected format (i.e. not a valid JSON encoded string, not GZIP encoded, etc.)
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class InvalidResponseException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public InvalidResponseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidResponseException(Throwable cause) {
		super(cause);
	}
}
