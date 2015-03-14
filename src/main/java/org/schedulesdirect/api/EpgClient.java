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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An EpgClient provides access to Schedules Direct JSON feed data.
 * 
 * <p>An EpgClient instance must be used in order to gain access to all of the other
 * object types available in this API.  Only an EpgClient object can be directly
 * instantiated by classes outside of this package.</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public abstract class EpgClient {
	/**
	 * The Schedules Direct server API version this client implements.
	 */
	static public final int API_VERSION = 20141201;
	
	/**
	 * Given a lineup id, generate the full, absolute URI path for the lineup
	 * @param id The unique lineup
	 * @return The unique, absolute URI path for the lineup id
	 */
	static public String getUriPathForLineupId(String id) {
		return String.format("/%s/lineups/%s", API_VERSION, id);
	}
	
	private String userAgent;
	private String baseUrl;
	
	/**
	 * Constructor
	 * @param userAgent The user agent to pass along to all SD HTTP requests
	 * @param baseUri The base URI to use when constructing URIs/URLs based on relative data in the raw JSON
	 */
	public EpgClient(final String userAgent, final String baseUrl) {
		this.userAgent = userAgent;
		this.baseUrl = baseUrl != null ? baseUrl : Config.DEFAULT_BASE_URL;
	}
	
	/**
	 * Provide all available lineups for the given location.
	 * 
 	 * @param location The 3 letter ISO country code; must be a country supported by the service (USA, CAN, etc.)
	 * @param zip The zip/postal code to find headends for
	 * @return An array of Lineup objects representing all available Lineups for the given zip; never returns null, but may return an empty array
	 * @throws InvalidZipCodeException Thrown if the given zip/postal code is invalid
	 * @throws IOException Thrown if there is any kind of IO error accessing the raw data feed
	 */
	public final Lineup[] getLineups(final String location, final String zip) throws IOException {
		return searchForLineups(location, zip);
	}
	
	/**
	 * Provide all available lineups for the given location.
	 * 
	 * @param location The 3 letter ISO country code; must be a country supported by the service (USA, CAN, etc.)
	 * @param zip The zip/postal code to find headends for
	 * @return An array of Lineup objects representing all available headends for the given zip; never returns null, but may return an empty array
	 * @throws IOException Thrown if there is any kind of IO error accessing the raw data feed
	 */
	abstract protected Lineup[] searchForLineups(final String location, final String zip) throws IOException;
	
	/**
	 * Provide all available lineups for the logged in user.
	 * 
	 * <p>
	 * This method will only return those lineups that are configured in the user's Schedules Direct account.
	 * These are the only lineups to which the user is guaranteed to have access to listings data for.
	 * </p>
	 * @return An array of Lineup objects, one for each lineup configured in the user's Schedules Direct account
	 * @throws IOException Thrown if there is any kind of IO error accessing the data feed
	 */
	abstract public Lineup[] getLineups() throws IOException;
	
	/**
	 * Get the lineup for the given uri
	 * <p><b>NOTE:</b>This method will only return objects registered to the user's account (or
	 *    available in the local cache).  This method will not construct a Lineup object and
	 *    return it if you do not have access to the Lineup in question.  In other words, you
	 *    must register a lineup in your SD account before this method will return it.
	 * </p>
	 * @param path The absolute path to access the lineup data from; appended to BASE_URL to form full URI to be accessed
	 * @return The Lineup instance for the given path or null if it could not be found
	 * @throws IOException Thrown if there is any kind of IO error accessing the raw data feed
	 */
	abstract public Lineup getLineupByUriPath(final String path) throws IOException;
	
	/**
	 * Get the UserStatus object associated with this EpgClient connection
	 * @return The UserStatus object associated with this EpgClient instance
	 * @throws IOException Thrown if there is any kind of IO error accessing the raw data feed
	 */
	abstract public UserStatus getUserStatus() throws IOException;
	
	/**
	 * Set the user agent for all HTTP requests submitted to the SD servers from this client.
	 * 
	 * <p>A default agent string is generated if not set.</p>
	 * @param userAgent The user agent string to use for all HTTP requests to the SD servers
	 */
	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}
	
	/**
	 * Return the current user agent string being submitted to the SD servers from this client.
	 * @return The current user agent string
	 */
	public String getUserAgent() {
		return String.format("%ssdjson/%s (%s Java %s)", userAgent != null && userAgent.length() > 0 ? userAgent + " " : "", Config.API_VERSION, System.getProperty("java.vendor"), System.getProperty("java.version"));
	}
	
	/**
	 * Close and free all resources associated with this client connection
	 * @throws IOException On any IO error
	 */
	abstract public void close() throws IOException;
	
	/**
	 * Fetch a single airing schedule for the given Station reference
	 * @param station The station to fetch the airing schedule for
	 * @return An array of Airing objects representing the airing schedule for the given Station; empty array if schedule data isn't available
	 * @throws IOException Thrown on any IO error accessing the schedule data
	 */
	abstract protected Airing[] fetchSchedule(final Station station) throws IOException;

	/**
	 * Fetch the channel mapping for the given Linup object
	 * @param lineup The lineup to fetch channel mappings for
	 * @return The JSON encoded response received from upstream
	 * @throws IOException On any IO error
	 */
	abstract protected String fetchChannelMapping(final Lineup lineup) throws IOException;
	
	/**
	 * Fetch a Station's logo object
	 * @param station The station whose logo is to be fetched
	 * @return An InputStream containing the station's logo or null if one isn't available or an error occurred 
	 * @throws IOException Thrown on any IO error
	 */
	abstract protected InputStream fetchLogoStream(final Station station) throws IOException;

	/**
	 * Write a station's logo to a file
	 * @param station The station whose logo is to be written to file
	 * @param dest The destination of the write
	 * @throws IOException Thrown on any IO error
	 */
	protected void writeLogoToFile(final Station station, final File dest) throws IOException {
		try(InputStream ins = fetchLogoStream(station)) {
			EpgClientHelper.writeLogoToFile(ins, dest);
		}
	}
	
	/**
	 * Fetch a single Program object
	 * @param progId The program id to fetch
	 * @return The Program instance for the given program id or null if unavailable
	 * @throws IOException Thrown on any IO error accessing the data
	 */
	abstract protected Program fetchProgram(final String progId) throws IOException;
	
	/**
	 * Fetch multiple recording schedules in batch.
	 * 
	 * <p>
	 * This method is preferred to fetchSchedule() as it will grab
	 * multiple schedules much more efficiently.  Use this when	ever you can.
	 * </p>
	 * @param lineup The Lineup object to download schedules for
	 * @return A Map of Airing arrays, the key is the Station id, the value is that station's airing schedule
	 * @throws IOException In case of any IO error accessing the data
	 */
	abstract protected Map<Station, Airing[]> fetchSchedules(final Lineup lineup) throws IOException;

	/**
	 * Fetch multiple programs in batch.
	 * 
	 * <p>
	 * This method is preferred to fetchProgram() as it will grab
	 * multiple programs much more efficiently.  Use this when ever you can.
	 * </p>
	 * @param progIds An array of program ids to fetch
	 * @return A Map of fetched programs; the key is the progId and the value is the Program instance
	 * @throws IOException On any IO error accessing the data
	 */
	abstract protected Map<String, Program> fetchPrograms(final String[] progIds) throws IOException;
		
	/**
	 * Purge the client's object cache.  This will force the client to reload all objects from
	 * their source.
	 */
	abstract public void purgeCache();
	
	/**
	 * Purge a specific object from the client's cache.  Accessing this object from this client
	 * after the purge will force the object to be reloaded from the client's upstream source.
	 * @param obj The object to purge from the client's local cache
	 */
	abstract public void purgeCache(final Object obj);
	
	/**
	 * Acknowledge receipt of and delete a message object from the upstream provider.
	 * <p><i>Optional operation</i></p>
	 * @param msg The message object to be deleted
	 * @throws IOException In case of any IO error upstream
	 */
	abstract public void deleteMessage(final Message msg) throws IOException;
	
	/**
	 * Return details of the current state of the Schedules Direct system
	 * @return The current SD system status
	 * @throws IOException In case of any errors obtaining the status info
	 */
	abstract public SystemStatus getSystemStatus() throws IOException;
	
	/**
	 * Register the given lineup with the user's SD account
	 * <p><i>Optional operation</i></p>
	 * @param path The full, absolute URI of the lineup to register; perhaps grab this value from <code>EpgClient.getUriPathForLineupId()</code>
	 * @return The number of register/unregister calls the user has remaining for today
	 * @throws IOException On any IO error
	 * @throws UnsupportedOperationException If the client type doesn't support the operation
	 */
	abstract public int registerLineup(final String path) throws IOException;
	
	/**
	 * Unregister the lineup from the user's SD account
	 * <p><i>Optional operation</i></p>
	 * @param l The linup to unregister from the user's account
	 * @return The number of register/unregister calls the user has remaining for today
	 * @throws IOException On any IO error
	 * @throws UnsupportedOperationException If the client type doesn't support the operation
	 */
	abstract public int unregisterLineup(final Lineup l) throws IOException;

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUri the baseUrl to set
	 */
	public void setBaseUri(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
