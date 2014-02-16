/*
 *      Copyright 2012-2013 Battams, Derek
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
 * Represents an exception thrown when a received JSON object does not meet the expected format (i.e. expected fields are missing, etc.)
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class InvalidJsonObjectException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidJsonObjectException() {
		// TODO Auto-generated constructor stub
	}

	public InvalidJsonObjectException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidJsonObjectException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidJsonObjectException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
