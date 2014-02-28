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
package org.schedulesdirect.api.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpRequestBase;
import org.schedulesdirect.api.Config;

public final class HttpUtils {
	static private final Log LOG = LogFactory.getLog(HttpUtils.class);
	
	static private Path AUDIT_LOG;
	static private volatile boolean auditSetup = false;
	static private void setupAudit() {
		try {
			Path root = Paths.get(Config.get().captureRoot().getAbsolutePath(), "http");
			Files.createDirectories(root);
			AUDIT_LOG = Files.createTempFile(root, String.format("%s_", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())), ".log");
			auditSetup = true;
		} catch (IOException e) {
			LOG.error("Unable to create HTTP audit log!", e);
			AUDIT_LOG = null;
		}
	}

	static public String prettyPrintHeaders(Header[] input) {
		return prettyPrintHeaders(input, "");
	}
	
	static public String prettyPrintHeaders(Header[] input, String prefix) {
		StringBuilder sb = new StringBuilder();
		for(Header h : input)
			sb.append(String.format("%s%s: %s%n", prefix, h.getName(), h.getValue()));
		return sb.toString();
	}
	
	//TODO: This really is a bad idea, but I'm desperate! :(
	static public Header[] scrapeHeaders(Request r) {
		try {
			Field f = r.getClass().getDeclaredField("request");
			f.setAccessible(true);
			HttpRequestBase req = (HttpRequestBase)f.get(r);
			return req.getAllHeaders();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOG.error("Error accessing request headers!", e);
			return new Header[0];
		}
	}
	
	static public void captureToDisk(String msg) {
		Config conf = Config.get();
		if(conf.captureHttpComm()) {
			if(!auditSetup)
				setupAudit();
			
			try {
				if(AUDIT_LOG != null)
					synchronized(HttpUtils.class) { 
						Files.write(AUDIT_LOG, msg.getBytes("UTF-8"), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
					}
			} catch (IOException e) {
				LOG.error("Unable to write capture file, logging at trace level instead!", e);
				LOG.trace(msg);
			}
		}
	}
	
	static public Path captureContentToDisk(InputStream ins) {
		Config conf = Config.get();
		Path p = Paths.get(conf.captureRoot().getAbsolutePath(), "http", "content");
		Path f;
		
		try {
			Files.createDirectories(p);
			f = Files.createTempFile(p, "sdjson_content_", ".dat");
		} catch(IOException e) {
			LOG.error("Unable to create http content file!", e);
			return null;
		}
		
		try(OutputStream os = new FileOutputStream(f.toFile())) {
			IOUtils.copy(ins, os);
		} catch (IOException e) {
			LOG.error("Unable to write http content to file!", e);
			return null;
		}
		
		return f;
	}
	
	private HttpUtils() {}
}
