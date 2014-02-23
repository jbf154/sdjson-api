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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.test.SdjsonTestSuite;

public class MessageTest extends SdjsonTestSuite {
	static private final EpgClient MOCK_CLNT = mock(EpgClient.class);
	
	@Test
	public void testConstruction() {
		JSONObject o = new JSONObject();
		Date d = new Date();
		o.put("date", Config.get().getDateTimeFormat().format(d));
		o.put("msgID", "foo");
		o.put("message", "My msg");
		Message m = new Message(o, MOCK_CLNT);
		assertEquals("foo", m.getId());
		assertEquals(d.getTime() - (d.getTime() % 1000), m.getDate().getTime());
		assertEquals("My msg", m.getContent());
	}
	
	@Test
	public void testDelete() throws IOException {
		JSONObject o = new JSONObject();
		o.put("date", Config.get().getDateTimeFormat().format(new Date()));
		o.put("msgID", "foo");
		Message m = new Message(o, MOCK_CLNT);
		m.delete();
		verify(MOCK_CLNT).deleteMessage(m);;
	}
	
	@Test(expected=InvalidJsonObjectException.class)
	public void verifyMsgIdRequired() {
		JSONObject o = new JSONObject();
		o.put("date", Config.get().getDateTimeFormat().format(new Date()));
		new Message(o, MOCK_CLNT);		
	}
	
	@Test(expected=InvalidJsonObjectException.class)
	public void verifyDateRequired() {
		JSONObject o = new JSONObject();
		o.put("msgID", "foo");
		new Message(o, MOCK_CLNT);		
	}
}
