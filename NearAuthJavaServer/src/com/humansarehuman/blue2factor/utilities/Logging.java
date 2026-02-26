package com.humansarehuman.blue2factor.utilities;

import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@SuppressWarnings("ucd")
public class Logging {
	private Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final boolean logOn = true;
	private final String logPropFile = "log4j.properties";

	public Logging(Class<?> cls) {
		Properties p = new Properties();
		try {
			InputStream is = getClass().getResourceAsStream(logPropFile);
			if (p != null && is != null) {
				p.load(is);
			}
		} catch (Exception e) {
			consoleLogImportant("There was an error writing to the log.");
			e.printStackTrace();
		}
	}

	public void debug(String text) {
		if (logOn) {
			// logger.debug(text);
			System.out.println(formatter.format(new Date()) + " debug: " + text);
		}
	}

	public void debug(String className, String text) {
		debug(className + ": " + text);
	}

	public void info(String text) {
		if (logOn) {
			// logger.info(text);
			System.out.println(formatter.format(new Date()) + " info: " + text);
		}
	}

	public void info(String className, String text) {
		info(className + ": " + text);
	}

	public void warn(String text) {
		if (logOn) {
			// logger.warn(text);
			System.out.println(formatter.format(new Date()) + " warn: " + text);
		}
	}

	public void warn(String className, String text) {
		warn(className + ": " + text);
	}

	public void warn(String text, Exception e) {
		if (logOn) {
			// logger.warn(text, e);
			System.out.println(formatter.format(new Date()) + " warn: " + text + " --- " + e.getMessage());
		}
	}

	public void error(String text) {
		if (logOn) {
			System.out.println(formatter.format(new Date()) + " error: " + text);
		}
	}

	public void error(String className, String text) {
		error(className + ": " + text);
	}

	public void error(String text, Exception e) {
		if (logOn) {
//            logger.error(text, e);
			System.out.println(formatter.format(new Date()) + " error: " + text + " --- " + e.getMessage());
		}
	}

	public void trace(String text) {
		if (logOn) {
			// logger.trace(text);
			System.out.println(formatter.format(new Date()) + " trace: " + text);
		}
	}

	public void trace(String className, String text) {
		trace(className + ": " + text);
	}

	public void consoleLogImportant(String text) {
		System.out.println("******************************************************");
		System.out.println("******************************************************");
		System.out.println();
		System.out.println();
		System.out.println(text);
		System.out.println();
		System.out.println();
		System.out.println("******************************************************");
		System.out.println("******************************************************");

	}
}
