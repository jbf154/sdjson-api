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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.schedulesdirect.test.SdjsonTestSuite;
import org.schedulesdirect.test.utils.JsonResponseBuilder;

public class ZipEpgClientTest extends SdjsonTestSuite {

	private Path src;
	private FileSystem vfs;
		
	@Before
	public void setup() throws Exception {
		src = Files.createTempFile("sdjson_", ".zip");
		Files.delete(src);
		vfs = FileSystems.newFileSystem(new URI(String.format("jar:%s", src.toUri().toString())), Collections.singletonMap("create", "true"));
	}
	
	@After
	public void teardown() {
		try {
			Files.deleteIfExists(src);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void initVfs(int zipVer, boolean close) {
		try {
			Path p = vfs.getPath(ZipEpgClient.ZIP_VER_FILE);
			Files.write(p, Integer.toString(zipVer).getBytes(ZipEpgClient.ZIP_CHARSET));
			p = vfs.getPath(ZipEpgClient.LINEUPS_LIST);
			Files.write(p, JsonResponseBuilder.buildLineupsResponse().getBytes(ZipEpgClient.ZIP_CHARSET));
			Files.createDirectory(vfs.getPath("schedules"));
			Files.createDirectory(vfs.getPath("programs"));
			if(close)
				vfs.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	private void initVfs(int zipVer) {
		initVfs(zipVer, true);
		
	}
	private void initVfs(boolean close) {
		initVfs(ZipEpgClient.ZIP_VER, close);
	}
	
	private void initVfs() {
		initVfs(ZipEpgClient.ZIP_VER, true);
	}

	@Test
	public void testSuccessfulConstruction() throws Exception {
		initVfs();
		new ZipEpgClient(src);
	}
	
	@Test(expected=IOException.class)
	public void testHandleNoVersionFileInZip() throws Exception {
		new ZipEpgClient(src);
	}
	
	@Test(expected=IOException.class)
	public void testHandleWrongVersionFileInZip() throws Exception {
		initVfs(ZipEpgClient.ZIP_VER - 1);
		new ZipEpgClient(src);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testGetUserStatusThrowsWhenClosed() throws Exception {
		initVfs();
		EpgClient c = new ZipEpgClient(src);
		c.close();
		c.getUserStatus();
	}
	
	@Test(expected=IOException.class)
	public void testGetUserStatusReceivesInvalidJson() throws Exception {
		initVfs(false);
		try {
			Path p = vfs.getPath(ZipEpgClient.USER_DATA);
			Files.write(p, "f!o!o".getBytes());
			vfs.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		EpgClient c = new ZipEpgClient(src);
		c.getUserStatus();
	}
	
	@Test
	public void testCloseClosesVfs() throws Exception {
		initVfs();
		EpgClient c = new ZipEpgClient(src);
		FileSystem fs = (FileSystem)Whitebox.getInternalState(c, "vfs");
		assertTrue(fs.isOpen());
		c.close();
		assertFalse(fs.isOpen());
	}
	
	@Test
	public void testCloseOnlyClosesOnce() throws Exception {
		initVfs();
		EpgClient c = new ZipEpgClient(src);
		c.close();
		assertTrue((Boolean)Whitebox.getInternalState(c, "closed"));
		c.close();
		assertTrue((Boolean)Whitebox.getInternalState(c, "closed"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testFetchScheduleFailsWhenClosed() throws Exception {
		initVfs();
		EpgClient c = new ZipEpgClient(src);
		c.close();
		c.fetchSchedule(mock(Station.class));
	}
	
	@Test
	public void testFetchSchedule() throws Exception {
		initVfs(false);
		Station s = mock(Station.class);
		when(s.getId()).thenReturn("abc");
		try {
			Path p = vfs.getPath("schedules", String.format("%s.txt", s.getId()));
			Files.write(p, JsonResponseBuilder.buildScheduleResponse().getBytes(ZipEpgClient.ZIP_CHARSET));
			vfs.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		EpgClient c = new ZipEpgClient(src);
		assertEquals(0, c.fetchSchedule(s).length);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testFetchProgramFailsWhenClosed() throws Exception {
		initVfs();
		EpgClient c = new ZipEpgClient(src);
		c.close();
		c.fetchProgram("foo");
	}
	
	@Test
	public void testFetchProgram() throws Exception {
		initVfs(false);
		String pId = "abc";
		try {
			Path p = vfs.getPath("programs", String.format("%s.txt", pId));
			Files.write(p, JsonResponseBuilder.buildProgramResponse().getBytes(ZipEpgClient.ZIP_CHARSET));
			vfs.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		EpgClient c = new ZipEpgClient(src);
		assertNull(c.fetchProgram(pId));
	}
}
