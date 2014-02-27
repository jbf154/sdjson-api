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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.schedulesdirect.api.Config;

/**
 * When String input is expected to be JSON encoded data, but isn't then this exception is thrown in response
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class JsonEncodingException extends IOException {

	private static final long serialVersionUID = 1L;
	static private final Log LOG = LogFactory.getLog(JsonEncodingException.class);
	static private volatile boolean targetCleaned = false;
	
	private String src;
	
	/**
	 * 
	 * @param message The message to associate with the exception
	 * @param src The input that caused the exception
	 */
	public JsonEncodingException(String message, String src) {
		super(message);
		this.src = src;
		capture();
	}

	/**
	 * 
	 * @param cause The cause of the exception
	 * @param src The input that caused the exception
	 */
	public JsonEncodingException(Throwable cause, String src) {
		super(cause);
		this.src = src;
		capture();
	}

	/**
	 * 
	 * @param message The message to associate with the exception
	 * @param cause The cause of the exception
	 * @param src The input that caused the exception
	 */
	public JsonEncodingException(String message, Throwable cause, String src) {
		super(message, cause);
		this.src = src;
		capture();
	}
	
	/**
	 * Generate the capture file for this exception, if requested
	 */
	protected void capture() {
		Config conf = Config.get();
		if(conf.captureJsonEncodingErrors()) {
			String msg = generateMsg();
			try {
				Path p = Paths.get(conf.captureRoot().getAbsolutePath(), "encode");
				if(!targetCleaned && Files.exists(p))
					try {
						FileUtils.deleteDirectory(p.toFile());
					} catch(IOException e) {
						LOG.warn(String.format("Unable to clean target dir! [%s]", p));
					}
				targetCleaned = true;
				Files.createDirectories(p);
				Path f = Files.createTempFile(p, prefix(), ".err");
				Files.write(f, msg.getBytes("UTF-8"));
			} catch (IOException e) {
				LOG.error("Unable to write capture file, logging it instead!", e);
				LOG.error(String.format("Invalid JSON received!%n%s", msg), this);
			}
		}
	}
	
	private String prefix() {
		return String.format("%s_", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
	}
	
	private String generateMsg() {
		StringWriter sw = new StringWriter();
		sw.append(String.format("*** S T A C K  T R A C E ***%n"));
		try(PrintWriter pw = new PrintWriter(sw)) {
			printStackTrace(pw);
		}
		sw.append(String.format("%n*** I N P U T ***%n%s", src));
		return sw.toString();
	}
}
