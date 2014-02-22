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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

/**
 * Encapsulates a request being sent to the Schedules Direct JSON service.
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class JsonRequest {
	static private final Log LOG = LogFactory.getLog(JsonRequest.class);

	/**
	 * Defines the supported action types for all requests to the service
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum Action {
		GET,
		PUT,
		POST,
		DELETE,
		HEAD,
		OPTIONS
	}
		
	private String baseUrl;
	private String hash;
	private String userAgent;
	private String targetUrl;
	private String resource;
	private Action action;
	private boolean valid = true;
	
	/**
	 * Constructor
	 * @param resource The resource to be accessed for this request
	 * @param hash The user's hash secret obtained from the SD service
	 * @param userAgent The user agent string to use for the web request to SD
	 * @param baseUrl The base URL to submit the request to; default used if null
	 */
	public JsonRequest(Action action, String resource, String hash, String userAgent, String baseUrl) {
		this.hash = hash;
		this.userAgent = userAgent;
		targetUrl = null;
		this.resource = resource;
		this.baseUrl = String.format("%s/%s/%s", baseUrl != null ? baseUrl : Config.DEFAULT_BASE_URL, EpgClient.API_VERSION, this.resource);
		this.action = action;
	}

	/**
	 * Constructor
	 * <p>
	 * 	Such instances cannot be executed and will fail; these are skeleton
	 *  instances suited for use in NetworkEpgClient.submitRequest()
	 * </p>
	 * @param resource The resource to be accessed for this request
	 * @param hash The user's hash secret obtained from the SD service
	 */
	public JsonRequest(Action action, String resource) {
		this(action, resource, null, null, null);
		valid = false;
	}
	
	private Request initRequest() {
		Request r = null;
		switch(action) {
			case GET: r = Request.Get(baseUrl); break;
			case PUT: r = Request.Put(baseUrl); break;
			case POST: r = Request.Post(baseUrl); break;
			case DELETE: r = Request.Delete(baseUrl); break;
			case OPTIONS: r = Request.Options(baseUrl); break;
			case HEAD: r = Request.Head(baseUrl); break;
		}
		return r.userAgent(userAgent);
	}
	
	/**
	 * Return's the target URL this request was sent to; is null until the request is actually submitted
	 * @return The URL the request was sent to or null if the request has not been submitted yet
	 */
	public String getTargetUrl() { return targetUrl; }
	
	/**
	 * Submit this request; returns the JSON object response received; only call if the request is expected to return a JSON object in response
	 * @param reqData The supporting data for the request; this is dependent on the action and obj target specified
	 * @return The JSON encoded response received from the SD service
	 * @throws IOException Thrown on any IO error encountered
	 */
	public String submitForJson(Object reqData) throws IOException {
		try(InputStream ins = submitRaw(reqData)) {
			return IOUtils.toString(ins, "UTF-8");
		}
	}

	/**
	 * Submit this request; returns the raw input stream of the content; caller responsible for closing stream when done.
	 * @param reqData The supporting data for the request; this is dependent on the action and obj target specified
	 * @return The InputStream of data received in response to the request
	 * @throws IOException Thrown on any IO error encountered
	 * @throws IllegalStateException Thrown if called on a partially constructed object (the 2 arg ctor)
	 */
	public InputStream submitForInputStream(Object reqData) throws IOException {
		return submitRaw(reqData);
	}
	
	private InputStream submitRaw(Object reqData) throws IOException {
		if(!valid)
			throw new IllegalStateException("Cannot submit a partially constructed request!");
		targetUrl = baseUrl.toString();
		Executor exe = Executor.newInstance(new DecompressingHttpClient(new DefaultHttpClient()));
		Request req = initRequest();
		if(hash != null)
			req.addHeader(new BasicHeader("token", hash));
		if(reqData != null)
			req.bodyString(reqData.toString(), ContentType.APPLICATION_JSON);
		HttpResponse resp = exe.execute(req).returnResponse();
		StatusLine status = resp.getStatusLine();
		if(status.getStatusCode() != 200)
			LOG.debug(String.format("%s returned error! [rc=%d]", req, status.getStatusCode()));
		return resp.getEntity().getContent();
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}
}
