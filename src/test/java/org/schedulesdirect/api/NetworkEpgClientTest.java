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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.schedulesdirect.api.exception.InvalidCredentialsException;
import org.schedulesdirect.api.exception.ServiceOfflineException;
import org.schedulesdirect.api.json.DefaultJsonRequest;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.api.MockJsonRequestFactory;
import org.schedulesdirect.test.utils.JsonResponseBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DefaultJsonRequest.class })
public class NetworkEpgClientTest extends SdjsonTestSuite {
	
	static private final MockJsonRequestFactory FACTORY = new MockJsonRequestFactory();
	
	@Before
	public void setup() {
		FACTORY.clear();
		Field f = Whitebox.getField(NetworkEpgClient.class, "CACHE");
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> cache = (Map<String, Object>)f.get(null);
			cache.clear();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testCtorUsesDefaultBaseUrl() throws Exception {
		FACTORY.addValidTokenResponse();
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		assertEquals(Config.DEFAULT_BASE_URL, c.getBaseUrl());
	}

	@Test(expected=InvalidCredentialsException.class)
	public void testCtorWithNullId() throws Exception {
		new NetworkEpgClient(null, "bar");
	}

	@Test(expected=InvalidCredentialsException.class)
	public void testCtorWithEmptyId() throws Exception {
		new NetworkEpgClient("", "bar");
	}

	@Test(expected=InvalidCredentialsException.class)
	public void testCtorWithNullPwd() throws Exception {
		new NetworkEpgClient("foo", null);
	}

	@Test(expected=InvalidCredentialsException.class)
	public void testCtorWithEmptyPwd() throws Exception {
		new NetworkEpgClient("foo", "");
	}

	@Test
	public void testAuthorizeSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		new NetworkEpgClient("foo", "bar", FACTORY); // authorize() is called by ctor
	}
	
	@Test(expected=ServiceOfflineException.class)
	public void testAuthorizeHandlesOfflineService() throws Exception {
		FACTORY.addErrorResponse(ApiResponse.SERVICE_OFFLINE);
		new NetworkEpgClient("foo", "bar", FACTORY); // authorize() is called by ctor
	}
	
	@Test(expected=InvalidCredentialsException.class)
	public void testAuthorizeHandlesServiceError() throws Exception {
		FACTORY.addErrorResponse();
		new NetworkEpgClient("foo", "bar", FACTORY); // authorize() is called by ctor
	}
	
	@Test
	public void testInitStatusObjects() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForJson(any(Object.class))).thenReturn(JsonResponseBuilder.buildStatusResponse(null));
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		c.initStatusObjects();
		verify(req).submitForJson(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test(expected=IOException.class)
	public void testInitStatusObjectsGetsErrResponse() throws Exception {
		FACTORY.addValidTokenResponse();
		FACTORY.addErrorResponse();
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		c.initStatusObjects();		
	}
	
	@Test
	public void testGetLineupsSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForJson(any(Object.class))).thenReturn(JsonResponseBuilder.buildLineupsResponse());
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		assertEquals(0, c.getLineups().length);
		verify(req).submitForJson(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test(expected=IOException.class)
	public void testGetLineupsHandlesError() throws Exception {
		FACTORY.addValidTokenResponse();
		FACTORY.addErrorResponse();
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		c.getLineups();
	}
	
	@Test
	public void testSearchForLineupsSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForJson(any(Object.class))).thenReturn(JsonResponseBuilder.buildSearchResponse());
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		assertEquals(0, c.searchForLineups("foo", "bar").length);
		verify(req).submitForJson(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test
	public void testSearchForLineupsHandlesError() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForJson(any(Object.class))).thenReturn(JsonResponseBuilder.buildErrorResponse());
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		assertEquals(0, c.searchForLineups("foo", "bar").length);
		verify(req).submitForJson(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test
	public void testFetchScheduleSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForInputStream(any(Object.class))).thenReturn(new ByteArrayInputStream("".getBytes("UTF-8")));
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		c.fetchSchedule(mock(Station.class));
		verify(req).submitForInputStream(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test
	public void testFetchScheduleFromCache() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		FACTORY.add(req);
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		Field f = Whitebox.getField(NetworkEpgClient.class, "CACHE");
		@SuppressWarnings("unchecked")
		Map<String, Object> cache = (Map<String, Object>)f.get(null);
		cache.put("__STAT__null", new Airing[0]);
		c.fetchSchedule(mock(Station.class));
		verifyZeroInteractions(req);
	}
	
	@Test
	public void testFetchSchedulesSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForInputStream(any(Object.class))).thenReturn(new ByteArrayInputStream("".getBytes("UTF-8")));
		FACTORY.add(req);
		
		Station s = mock(Station.class);
		when(s.getId()).thenReturn("foobar");
		Lineup l = mock(Lineup.class);
		when(l.getStations()).thenReturn(new Station[] {s});

		NetworkEpgClient c= new NetworkEpgClient("foo", "bar", FACTORY);
		c.fetchSchedules(l);
		verify(req).submitForInputStream(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test
	public void testFetchSchedulesFromCache() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		FACTORY.add(req);
		
		Lineup l = mock(Lineup.class);
		when(l.getStations()).thenReturn(new Station[0]);
		
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		Field f = Whitebox.getField(NetworkEpgClient.class, "CACHE");
		@SuppressWarnings("unchecked")
		Map<String, Object> cache = (Map<String, Object>)f.get(null);
		cache.put("__STAT__null", new Airing[0]);
		
		c.fetchSchedules(l);
		verifyZeroInteractions(req);
	}
	
	@Test
	public void testFetchProgramsSucceeds() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForInputStream(any(Object.class))).thenReturn(new ByteArrayInputStream("".getBytes("UTF-8")));
		FACTORY.add(req);
		
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		c.fetchPrograms(new String[] {"foo"});
		verify(req).submitForInputStream(any(Object.class));
		verifyNoMoreInteractions(req);
	}
	
	@Test
	public void testFetchProgramsFromCache() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		FACTORY.add(req);
		
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		Field f = Whitebox.getField(NetworkEpgClient.class, "CACHE");
		@SuppressWarnings("unchecked")
		Map<String, Object> cache = (Map<String, Object>)f.get(null);
		cache.put("__PROG__null", mock(Program.class));
		
		c.fetchPrograms(new String[] {null});
		verifyZeroInteractions(req);
	}
	
	@Test
	public void testFetchProgramsFromCacheAndNetwork() throws Exception {
		FACTORY.addValidTokenResponse();
		DefaultJsonRequest req = mock(DefaultJsonRequest.class);
		when(req.submitForInputStream(any(Object.class))).thenReturn(new ByteArrayInputStream("".getBytes("UTF-8")));
		FACTORY.add(req);
		
		NetworkEpgClient c = new NetworkEpgClient("foo", "bar", FACTORY);
		Field f = Whitebox.getField(NetworkEpgClient.class, "CACHE");
		@SuppressWarnings("unchecked")
		Map<String, Object> cache = (Map<String, Object>)f.get(null);
		cache.put("__PROG__null", mock(Program.class));
		
		Map<String, Program> progs = c.fetchPrograms(new String[] {null, "abcd"});
		verify(req).submitForInputStream(any(Object.class));
		verifyNoMoreInteractions(req);
		assertEquals(1, progs.keySet().size()); // b/c we "pull" one from cache and just mock an empty response to network req		
	}
}
