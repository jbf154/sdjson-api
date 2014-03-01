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

/**
 * A subclass that never logs to disk, regardless of system property
 * <p>
 *  Objects that are only constructed as members of other objects that
 *  also throw InvalidJsonObjectException should throw this exception as
 *  needed in order to allow the exception to bubble up to its parent and
 *  only be logged once, at the root cause of the exception.
 * </p>
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class SilentInvalidJsonObjectException extends
		InvalidJsonObjectException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public SilentInvalidJsonObjectException(String message) {
		super(message, null);
	}

	/**
	 * @param cause
	 */
	public SilentInvalidJsonObjectException(Throwable cause) {
		super(cause, null);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SilentInvalidJsonObjectException(String message, Throwable cause) {
		super(message, cause, null);
	}
	
	@Override
	protected void capture() {
		// Do nothing, on purpose
	}

}
