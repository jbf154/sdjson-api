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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidCredentialsException;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.api.exception.JsonEncodingException;
import org.schedulesdirect.api.exception.ServiceOfflineException;
import org.schedulesdirect.api.exception.SilentInvalidJsonObjectException;
import org.schedulesdirect.api.json.IJsonRequestFactory;
import org.schedulesdirect.api.json.JsonRequest;
import org.schedulesdirect.api.json.JsonRequest.Action;
import org.schedulesdirect.api.json.JsonRequestFactory;
import org.schedulesdirect.api.utils.JsonResponseUtils;
import org.schedulesdirect.api.utils.UriUtils;

/**
 * An implementation of EpgClient that accesses all data from the Schedules Direct JSON feed servers
 * 
 * <p>Use this implementation of EpgClient when you need to download the latest available data directly
 * from the Schedules Direct servers.  Use of this client type will always hit the Schedules Direct
 * servers for all requests.</p>
 * 
 * <p>
 * In almost all conceivable real world application scenarions, the NetworkEpgClient should only
 * be used to perform management functions (i.e. check SD server status, check user account status,
 * change user password, submit metadata updates, etc.)  It is rare that an application would ever
 * use the NetworkEpgClient instance to directly access EPG data.  Instead, applications should
 * always generate and maintain a local cache of their EPG data by downloading it via the sdjson
 * grabber application.  The grabber is extremely efficient at maintaining a local cache of a user's
 * EPG data and only requesting data that has changed and needs to be updated in the local cache.
 * Applicatons should periodically call the grabber to update their local cache then use ZipEpgClient
 * instances to access the EPG data in their Java applications.
 * </p>
 * 
 * <p>
 * The NetworkEpgClient class is capable of accessing all of the EPG objects, but it does so one
 * object at a time and sends a single network request to the Schedules Direct servers for every
 * such object fetch.  Besides being inefficient, this method is extremely slow.  Such access to
 * the EPG data objects is provided for use by the sdjson grabber application and for development,
 * debugging, and testing of the sdjson package.  Most applications should <b>not</b> directly access
 * their EPG data via this class.  Instead, use the grabber app to create a <code>.epg</code> file
 * then feed that file into an instance of ZipEpgClient to access your EPG data from the cache file,
 * which is orders of magnitude faster than using this class.
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> Program objects fetched with a NetworkEpgClient will <b>never</b> contain metadata.
 * Metadata is only made available when accessed via a ZipEpgClient with a zip file source that
 * contains the metadata.
 * </p>
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class NetworkEpgClient extends EpgClient {
	/*
	 * This is all for the client instance cache; just in case people do use this class to access EPG data, there is some effort to cache what we can
	 */
	static private final Map<String, Object> CACHE = Collections.synchronizedMap(new HashMap<String, Object>());
	static private String getCacheKey(Object obj) {
		if(obj instanceof Program)
			return getCacheKeyForProgram(((Program)obj).getId());
		else if(obj instanceof Station)
			return getCacheKeyForStation(((Station)obj).getId());
		else
			return null;
	}
	static private String getCacheKeyForProgram(String progId) { return "__PROG__" + progId; }
	static private String getCacheKeyForStation(String stationId) { return "__STAT__" + stationId; }
	
	private String id;
	private String password;
	private String hash;
	private UserStatus userStatus;
	private boolean useCache;
	private SystemStatus systemStatus;
	private IJsonRequestFactory factory;

	/**
	 * Constructor
	 * @param id The Schedules Direct username to authorize with
	 * @param pwd The Schedules Direct password to authorize with
	 * @throws InvalidCredentialsException Thrown if the given credentials were invalid
	 * @throws IOException Thrown if there is any IO error communicating with the Schedules Direct servers
	 */
	public NetworkEpgClient(final String id, final String pwd) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		this(id, pwd, null, null, true, JsonRequestFactory.get());
	}

	/**
	 * ctor; typically only used for development & testing; allows overriding of JsonRequestFactory instance 
	 * @param id
	 * @param pwd
	 * @param factory
	 * @throws InvalidCredentialsException
	 * @throws IOException
	 * @throws ServiceOfflineException
	 */
	public NetworkEpgClient(final String id, final String pwd, IJsonRequestFactory factory) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		this(id, pwd, null, null, true, factory);
	}

	/**
	 * Constructor
	 * @param id The Schedules Direct username to authorize with
	 * @param pwd The Schedules Direct password to authorize with
	 * @param userAgent The user agent to send on all requests to the SD servers
	 * @throws InvalidCredentialsException Thrown if the given credentials were invalid
	 * @throws IOException Thrown if there is any IO error communicating with the Schedules Direct servers
	 * @throws ServiceOfflineException Thrown if the web service reports itself as offline/unavailable
	 */
	public NetworkEpgClient(final String id, final String pwd, final String userAgent) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		this(id, pwd, userAgent, null, true, JsonRequestFactory.get());
	}

	/**
	 * ctor; typically only used for development & testing; allows overriding of JsonRequestFactory instance
	 * @param id
	 * @param pwd
	 * @param userAgent
	 * @param factory
	 * @throws InvalidCredentialsException
	 * @throws IOException
	 * @throws ServiceOfflineException
	 */
	public NetworkEpgClient(final String id, final String pwd, final String userAgent, IJsonRequestFactory factory) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		this(id, pwd, userAgent, null, true, factory);
	}

	/**
	 * Constructor
	 * @param id The Schedules Direct username to authorize with
	 * @param pwd The Schedules Direct password to authorize with
	 * @param userAgent The user agent to send on all requests to the SD servers
	 * @param baseUrl The base URL to use for all HTTP communication; most should not set this value as it is for testing and development only!
	 * @param useCache Should the client instance maintain a cache of created objects or hit the SD server on every request?  Though memory intensive, use of the cache is greatly encouraged!
	 * @param factory The JsonRequestFactory to be used for this client to generate network requests
	 * @throws InvalidCredentialsException Thrown if the given credentials were invalid
	 * @throws IOException Thrown if there is any IO error communicating with the Schedules Direct servers	 
	 * @throws ServiceOfflineException Thrown if the web service reports itself as offline/unavailable
	 */
	public NetworkEpgClient(final String id, final String pwd, final String userAgent, final String baseUrl, final boolean useCache) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		this(id, pwd, userAgent, baseUrl, useCache, JsonRequestFactory.get());
	}

	/**
	 * Constructor
	 * @param id The Schedules Direct username to authorize with
	 * @param pwd The Schedules Direct password to authorize with
	 * @param userAgent The user agent to send on all requests to the SD servers
	 * @param baseUrl The base URL to use for all HTTP communication; most should not set this value as it is for testing and development only!
	 * @param useCache Should the client instance maintain a cache of created objects or hit the SD server on every request?  Though memory intensive, use of the cache is greatly encouraged!
	 * @param factory The JsonRequestFactory to be used for this client to generate network requests
	 * @throws InvalidCredentialsException Thrown if the given credentials were invalid
	 * @throws IOException Thrown if there is any IO error communicating with the Schedules Direct servers	 
	 * @throws ServiceOfflineException Thrown if the web service reports itself as offline/unavailable
	 */
	public NetworkEpgClient(final String id, final String pwd, final String userAgent, final String baseUrl, final boolean useCache, final IJsonRequestFactory factory) throws InvalidCredentialsException, IOException, ServiceOfflineException {
		super(userAgent, baseUrl);
		this.factory = factory;
		if(id == null || id.length() == 0)
			throw new InvalidCredentialsException("Schedules Direct username cannot be empty!");
		if(pwd == null || pwd.length() == 0)
			throw new InvalidCredentialsException("Schedules Direct password cannot be empty!");
		this.id = id;
		password = pwd;
		hash = null;
		userStatus = null;
		this.useCache = useCache;
		authorize();
	}

	/**
	 * Get the JsonRequestFactory implementation being used by this client
	 * @return The factory implementation
	 */
	public IJsonRequestFactory getJsonRequestFactory() { return factory; }
	
	/**
	 * Perform user authorization with Schedules Direct
	 * @throws InvalidCredentialsException Thrown if authorization failed
	 * @throws IOException Thrown on any IO error communicating with the Schedules Direct servers
	 * @throws ServiceOfflineException Thrown if the web service reports itself as offline/unavailable
	 * @throws InvalidJsonObjectException Thrown if the response object is not in the expected format
	 */
	protected void authorize() throws InvalidJsonObjectException, InvalidCredentialsException, IOException, ServiceOfflineException {
		JSONObject creds = new JSONObject();
		creds.put("username", id);
		/*
		 *  Using the deprecated shaHex() b/c some people still use very old
		 *  version of commons-codec; SageTV being an example
		 */			
		creds.put("password", DigestUtils.shaHex(password));

		JSONObject resp;
		String input = factory.get(JsonRequest.Action.POST, RestNouns.LOGIN_TOKEN, hash, getUserAgent(), getBaseUrl()).submitForJson(creds);
		try {
			resp = new JSONObject(input);
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Token: %s", e.getMessage()), e, input);
		}
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			try {
				hash = resp.getString("token");
			} catch (JSONException e) {
				throw new InvalidJsonObjectException(String.format("Token: %s", e.getMessage()), e, resp.toString(3));
			}
		} else if(resp.optInt("code", ApiResponse.NOT_PROVIDED) == ApiResponse.SERVICE_OFFLINE)
			throw new ServiceOfflineException(resp.optString("message"));
		else
			throw new InvalidCredentialsException(resp.optString("message"));
	}

	/**
	 * Grab the status objects for the user and system from Schedules Direct
	 * @throws IOException On any IO error, including an error response from the server
	 */
	protected void initStatusObjects() throws IOException {
		JSONObject resp;
		String input = factory.get(JsonRequest.Action.GET, RestNouns.STATUS, hash, getUserAgent(), getBaseUrl()).submitForJson(null);
		try {
			resp = new JSONObject(input);
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Status[%s]: %s", id, e.getMessage()), e, input);
		}
		
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			userStatus = new UserStatus(resp, id, this);
			try {
				systemStatus = new SystemStatus(resp.getJSONArray("systemStatus"));
			} catch(JSONException e) {
				throw new InvalidJsonObjectException(String.format("Status[%s]: %s", id, e.getMessage()), e, resp.toString(3));
			}
		} else
			throw new IOException(resp.optString("message"));
	}
	
	@Override
	public UserStatus getUserStatus() throws IOException {
		if(userStatus == null)
			initStatusObjects();
		return userStatus;
	}

	@Override
	public Lineup[] getLineups() throws IOException {
		Lineup[] list = null;
		JSONObject resp;
		String input = factory.get(JsonRequest.Action.GET, RestNouns.LINEUPS, hash, getUserAgent(), getBaseUrl()).submitForJson(null);
		try {
			resp = new JSONObject(input);
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Lineups[%s]: %s", id, e.getMessage()), e, input);
		}
		
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			try {
				JSONArray lineups = resp.getJSONArray("lineups");
				list = new Lineup[lineups.length()];
				for(int i = 0; i < lineups.length(); ++i) {
					JSONObject lineup = lineups.getJSONObject(i);
					list[i] = new Lineup(lineup.getString("name"), lineup.getString("location"), lineup.getString("uri"), lineup.getString("type"), this);
				}
			} catch(JSONException e) {
				throw new InvalidJsonObjectException(String.format("Lineups[%s]: %s", id, e.getMessage()), e, resp.toString(3));
			}
		} else if(JsonResponseUtils.getErrorCode(resp) != ApiResponse.NO_LINEUPS)
			throw new IOException(String.format("Error getting lineups! [%s]", resp.optString("message")));
		return list;
	}
	
	@Override
	protected Lineup[] searchForLineups(final String location, final String zip) throws IOException {
		List<Lineup> hes = new ArrayList<Lineup>();
		JSONObject resp;
		String input = factory.get(JsonRequest.Action.GET, String.format("%s?country=%s&postalcode=%s", RestNouns.HEADENDS, URLEncoder.encode(location, "UTF-8"), URLEncoder.encode(zip, "UTF-8")), hash, getUserAgent(), getBaseUrl()).submitForJson(null);
		try {
			resp = new JSONObject(input);			
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("SearchResp: %s", e.getMessage()), e, input);
		}
		
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			try {
				@SuppressWarnings("unchecked")
				Iterator<String> itr = (Iterator<String>)resp.keys();
				while(itr.hasNext()) {
					String k = itr.next();
					JSONObject headend = resp.getJSONObject(k);
					String heLoc = headend.getString("location");
					String heType = headend.getString("type");
					JSONArray lineups = headend.getJSONArray("lineups");
					for(int i = 0; i < lineups.length(); ++i) {
						JSONObject lineup = lineups.getJSONObject(i);
						hes.add(new Lineup(lineup.getString("name"), heLoc, lineup.getString("uri"), heType, this));
					}
				}				
			} catch(JSONException e) {
				throw new InvalidJsonObjectException(String.format("SearchResp: %s", e.getMessage()), e, resp.toString(3));
			}
		}
		return hes.toArray(new Lineup[hes.size()]);
	}
	
	/**
	 * @return The id used to authenticate with Schedules Direct
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The password used to authenticate with Schedules Direct
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return The unique hashkey received from Schedules Direct for this client session
	 */
	public String getHash() {
		return hash;
	}

	@Override
	public void close() throws IOException {
		purgeCache();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	/**
	 * Submit a self constructed JsonRequest
	 * <p>Meant for development only, but can also be used to handle parts of the Schedules Direct service not directly implemented by this API (yet).</p>
	 * <p><b>This method will replace the UserAgent and hash properties of the JsonRequest argument with those of this client.</b></p>
	 * <p>
	 * 	If there are production needs to use this method by people, then please open a ticket at the project site to get the missing API features
	 *  implemented into a future release.
	 * </p>
	 * @param req A JsonRequest to be submitted to the service
	 * @param data The data to attach to the request or null if no data is to be attached
	 * @return An input stream of the result's entity; caller responsible to close stream when done
	 * @throws IOException On any error
	 */
	public InputStream submitRequest(final JsonRequest req, final Object data) throws IOException {
		JsonRequest scrubbedReq = factory.get(req.getAction(), req.getResource(), getHash(), getUserAgent(), getBaseUrl());
		return scrubbedReq.submitForInputStream(data);
	}
	
	@Override
	protected Airing[] fetchSchedule(final Station station) throws IOException {
		Airing[] sched = null;
		if(useCache)
			sched = (Airing[])CACHE.get(getCacheKey(station));
		if(sched == null) {
			List<Airing> schedList = new ArrayList<Airing>();
			JSONArray ids = new JSONArray();
			ids.put(station.getId());
			JSONObject reqObj = new JSONObject();
			reqObj.put("request", ids);
			JsonRequest req = factory.get(Action.POST, RestNouns.SCHEDULES, hash, getUserAgent(), getBaseUrl());
			InputStream ins = req.submitForInputStream(reqObj);
			@SuppressWarnings("unchecked")
			List<String> input = IOUtils.readLines(ins);
			for(String obj : input) {
				JSONObject o;
				try {
					o = new JSONObject(obj);
				} catch(JSONException e) {
					throw new JsonEncodingException(String.format("ScheduleResp: %s", e.getMessage()), e, obj);
				}
				if(!JsonResponseUtils.isErrorResponse(o)) {
					try {
						JSONArray progs = o.getJSONArray("programs");
						for(int i = 0; i < progs.length(); ++i) {
							JSONObject p = progs.getJSONObject(i);
							if(!JsonResponseUtils.isErrorResponse(p))
								schedList.add(new Airing(p, fetchProgram(p.getString("programID")), station));
						}
					} catch(JSONException e) {
						throw new InvalidJsonObjectException(String.format(""), e, o.toString(3));
					}
				}
			}
		}
		return sched;
	}

	@Override
	protected Program fetchProgram(final String progId) throws IOException {
		return fetchPrograms(new String[] { progId }).values().toArray(new Program[1])[0];
	}

	private void prefetch(JSONArray airings) throws IOException {
		List<String> ids = new ArrayList<>();
		try {
			for(int i = 0; i < airings.length(); ++i)
				ids.add(airings.getJSONObject(i).getString("programID"));
		} catch(JSONException e) {
			throw new SilentInvalidJsonObjectException(e);
		}
		fetchPrograms(ids.toArray(new String[0]));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Map<Station, Airing[]> fetchSchedules(final Lineup lineup) throws IOException {
		Collection<String> misses = new ArrayList<>();
		Map<Station, Airing[]> scheds = new HashMap<Station, Airing[]>();
		if(useCache)
			for(Station s : lineup.getStations()) {
				Airing[] sched = (Airing[])CACHE.get(getCacheKey(s));
				if(sched != null)
					scheds.put(s, sched);
				else
					misses.add(s.getId());
			}
		else
			for(Station s : lineup.getStations())
				misses.add(s.getId());
		
		if(misses.size() > 0) {
			JSONObject reqObj = new JSONObject();
			reqObj.put("request", misses);
			try(InputStream resp = factory.get(JsonRequest.Action.POST, RestNouns.SCHEDULES, hash, getUserAgent(), getBaseUrl()).submitForInputStream(reqObj)) {
				for(String input : (List<String>)IOUtils.readLines(resp)) {
					JSONObject sched;
					try {
						sched = new JSONObject(input);
					} catch(JSONException e) {
						throw new JsonEncodingException(String.format("Schedule: %s", e.getMessage()), e, input);
					}
					try {
						Station s = lineup.getStation(sched.getString("stationID"));
						JSONArray airs = sched.getJSONArray("programs");
						if(useCache)
							prefetch(airs);
						List<Airing> result = new ArrayList<>();
						for(int i = 0; i < airs.length(); ++i) {
							JSONObject a = airs.getJSONObject(i);
							Program p = fetchProgram(a.getString("programID"));
							result.add(new Airing(a, p, s));
						}
						scheds.put(s, result.toArray(new Airing[0]));
						if(useCache)
							CACHE.put(getCacheKeyForStation(s.getId()), s);
					} catch(JSONException e) {
						throw new InvalidJsonObjectException(String.format("Schedule: %s", e.getMessage()), e, sched.toString(3));
					}
				}
			}
		}
		return scheds;		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Program> fetchPrograms(final String[] progIds) throws IOException {
		Collection<String> misses = new ArrayList<String>();
		Map<String, Program> progs = new HashMap<String, Program>();
		if(useCache)
			for(String progId : progIds) {
				Program p = (Program)CACHE.get(getCacheKeyForProgram(progId));
				if(p != null)
					progs.put(progId, p);
				else
					misses.add(progId);
			}
		else
			misses.addAll(Arrays.asList(progIds));
		if(misses.size() > 0) {
			JSONObject req = new JSONObject();
			req.put("request", new JSONArray(misses));
			
			try (InputStream resp = factory.get(JsonRequest.Action.POST, RestNouns.PROGRAMS, hash, getUserAgent(), getBaseUrl()).submitForInputStream(req)) {
				for(String input : (List<String>)IOUtils.readLines(resp)) {
					JSONObject prog;
					try {
						prog = new JSONObject(input);
						if(JsonResponseUtils.isErrorResponse(prog) && JsonResponseUtils.getErrorCode(prog) == ApiResponse.INVALID_PROGID) {
							progs.put(prog.getString("programID"), null);
							continue;
						}
					} catch(JSONException e) {
						throw new JsonEncodingException(String.format("Program: %s", e.getMessage()), e, input);
					}
					try {
						Program p = new Program(prog, this);
						String key = p.getId();
						progs.put(key, p);
						if(useCache)
							CACHE.put(getCacheKeyForProgram(key), p);
					} catch(JSONException e) {
						throw new InvalidJsonObjectException(String.format("Program[%s] is not valid!", prog.optString("programID", "unknown")), e, prog.toString(3));
					}
				}
			}
		}
		return progs;
	}
	
	@Override
	public void purgeCache() {
		CACHE.clear();
	}

	@Override
	public void purgeCache(final Object obj) {
		String k = getCacheKey(obj);
		if(k != null) CACHE.remove(k);
	}
	
	@Override
	public void deleteMessage(final Message msg) throws IOException {
		JsonRequest req = factory.get(JsonRequest.Action.DELETE, String.format("%s/%s", RestNouns.MESSAGES, msg.getId()), getHash(), getUserAgent(), getBaseUrl());
		String input = req.submitForJson(null);
		JSONObject resp;
		try {
			resp = new JSONObject(input);	
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Message(DELETE): %s", e.getMessage()), e, input);
		}
		
		if(JsonResponseUtils.isErrorResponse(resp))
			throw new IOException(resp.optString("message"));
	}
	
	@Override
	public SystemStatus getSystemStatus() throws IOException {
		if(systemStatus == null)
			initStatusObjects();
		return systemStatus;
	}
	
	@Override
	protected InputStream fetchLogoStream(final Station station) throws IOException {
		return station.getLogo().getUrl().openStream();
	}
	
	@Override
	public int registerLineup(final String path) throws IOException {
		JsonRequest req = factory.get(Action.PUT, UriUtils.stripApiVersion(path), getHash(), getUserAgent(), getBaseUrl());
		JSONObject resp;
		String input = req.submitForJson(null);
		try {
			resp = new JSONObject(input);	
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Lineup(REGISTER): %s", e.getMessage()), e, input);
		}
		
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			try {
				return resp.getInt("changesRemaining");
			} catch(JSONException e) {
				throw new InvalidJsonObjectException(String.format("Lineup(REGISTER): %s", e.getMessage()), e, resp.toString(3));
			}
		} else
			throw new IOException(String.format("Error registering lineup! [%s]", resp.optString("message")));
	}
	
	@Override
	public int unregisterLineup(final Lineup l) throws IOException {
		JsonRequest req = factory.get(Action.DELETE, l.getUri(), getHash(), getUserAgent(), getBaseUrl());
		JSONObject resp;
		String input = req.submitForJson(null);
		try {
			resp = new JSONObject(input);
		} catch(JSONException e) {
			throw new JsonEncodingException(String.format("Lineup(DELETE): %s", e.getMessage()), e, input);
		}
		
		if(!JsonResponseUtils.isErrorResponse(resp)) {
			try {
				return resp.getInt("changesRemaining");
			} catch(JSONException e) {
				throw new InvalidJsonObjectException(String.format("Lineup(DELETE): %s", e.getMessage()), e, resp.toString(3));
			}
		} else
			throw new IOException(String.format("Error unregistering lineup! [%s]", resp.optString("message")));
	}
	
	@Override
	protected String fetchChannelMapping(Lineup lineup) throws IOException {
		return factory.get(Action.GET, lineup.getUri(), getHash(), getUserAgent(), getBaseUrl()).submitForJson(null);
	}
	
	@Override
	public Lineup getLineupByUriPath(String path) throws IOException {
		for(Lineup l : getLineups())
			if(l.getUri().equals(UriUtils.stripApiVersion(path)))
				return l;
		throw new IOException(String.format("Unable to locate lineup for path: %s", path));
	}
}