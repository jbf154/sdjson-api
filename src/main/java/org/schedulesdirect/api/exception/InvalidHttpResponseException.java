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
package org.schedulesdirect.api.exception;

import java.io.IOException;

/**
 * Thrown when an HTTP request to the SD servers does not return an expected result (usually thrown when the request does not return status 200)
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class InvalidHttpResponseException extends IOException {

	private static final long serialVersionUID = 1L;
	
	private int status;
	private String details;

	/**
	 * Constructor
	 * @param message User readable reason for the exception
	 * @param status The HTTP status code received
	 * @param details Extra details (response txt; header txt, etc)
	 */
	public InvalidHttpResponseException(String message, int status, String details) {
		super(message);
		this.status = status;
		this.details = details;
	}

	/**
	 * 
	 * @param cause The underlying cause
	 * @param status The HTTP status code received
	 * @param details Extra details (response txt; header txt, etc)
	 */
	public InvalidHttpResponseException(Throwable cause, int status, String details) {
		super(cause);
		this.status = status;
		this.details = details;
	}

	/**
	 * 
	 * @param message User friendly message describing reason for exception
	 * @param cause The underlying cause
	 * @param status The HTTP status code received
	 * @param details Extra details (response txt; header txt, etc)
	 */
	public InvalidHttpResponseException(String message, Throwable cause, int status, String details) {
		super(message, cause);
		this.status = status;
		this.details = details;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

}
