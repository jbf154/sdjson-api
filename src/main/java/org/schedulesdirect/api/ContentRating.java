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
 * Represents the rating of a particular airing or program
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class ContentRating {

	private String body;
	private String rating;

	/**
	 * Constructor
	 * @param body The organization or governing body that assigned this rating
	 * @param rating The rating assigned
	 */
	public ContentRating(String body, String rating) {
		this.body = body;
		this.rating = rating;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @return the rating
	 */
	public String getRating() {
		return rating;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ContentRating [body=" + body + ", rating=" + rating + "]";
	}

}
