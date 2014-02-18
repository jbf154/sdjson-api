package org.schedulesdirect.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.NullAppender;

public final class Logging {
	static public final String CAT_PREFIX = "sdjson.test";
	static public final String LAYOUT_FMT = "%-5p [%c{1}]: %m%n";
	static private boolean isLoggingSetup = false;
	
	static public void init() {
		synchronized(Logging.class) {
			if(!isLoggingSetup) {
				Date start = new Date();
				Layout layout = new PatternLayout(Logging.LAYOUT_FMT);
				Logger l = Logger.getLogger(Logging.CAT_PREFIX);
				l.removeAllAppenders();
				try {
					l.addAppender(new FileAppender(layout, new File("test.log").getAbsolutePath(), false));
				} catch (IOException e) {
					l.addAppender(new ConsoleAppender(layout));
				}
				l.setAdditivity(false);
				l.info("Log started on " + start);
				Logger root = Logger.getRootLogger();
				root.removeAllAppenders();
				try {
					root.addAppender(new FileAppender(layout, new File("app.log").getAbsolutePath(), false));
					root.info("Log started on " + start);
				} catch (IOException e) {
					root.addAppender(new NullAppender());
					l.warn("Failed to create app.log appender; ignoring app logging!");					
				}
				isLoggingSetup = true;
			}
		}
	}	
	
	static public Log getLogger(Class<?> cls) {
		init();
		String name = String.format("%s.%s", CAT_PREFIX, cls.getName());
		return LogFactory.getLog(name);
	}
}
