/*
 * Copyright (c) 2012 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * <p>
 * A ServletContextListener that can be used in web applications to define the location of the logback configuration.
 * This listener should be the first listener to be configured to configure logback before using it. The location of a
 * logback configuration file is defined by the 'be.redlab.logback.location' web.xml context param. Placeholders from
 * System.properties (ex: ${user.home}) are supported.
 * </p>
 * <p>
 * Location property examples:
 * </p>
 * <li>/WEB-INF/logback.xml -> loaded from servlet context</li> <li>classpath:foo/logback.xml -> loaded from classpath</li>
 * <li>file:/opt/configs/app/logback.xml -> loaded as url</li> <li>/opt/configs/app/logback.xml -> loaded as absolute
 * file</li> <li>logback.xml -> loaded as file relative to the servlet container working directory</li>
 *
 * <p>
 * Aditionally, it is possible to use the context param 'be.redlab.logback.default' with values OFF, ERROR, WARN, INFO,
 * DEBUG, TRACE or no value. If the configured be.redlab.logback.location results in an unfindable configuration or does
 * not exists, a default logger that logs the given level (or info as default) to the current console, is activated. If
 * the property 'be.redlab.logback.default' is not available, no default logger is activated.
 * </p>
 *
 * <p>
 * Servlet 3.x Environment<br />
 * The webfragment is configured to be loaded before all others. The jars are named aaa-logback-webfragment, the aaa
 * increasing the probability to be the first webfragment loaded in a Servlet 3.x environment.
 * </p>
 * <p>
 * Servlet 2.x Environment<br />
 * In a Servlet 2.x environment, configure the listener in your web.xml.
 * </p>
 * example extract from a web.xml
 *
 * <pre>
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;be.redlab.logback.location&lt;/param-name&gt;
 * 		&lt;param-value>classpath:logback.xml&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name>be.redlab.logback.default&lt;/param-name&gt;
 * 		&lt;param-value>info&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * </pre>
 * <p>
 * Note<br />
 * The code in this listener is inspired from <a href="http://jira.qos.ch/browse/LOGBACK-557">this issue</a> in logbacks
 * jira. And altered by redlab. The code is under the same licenses as logback (EPL and LGPL, to the licensees choosing)
 * </p>
 *
 */
@WebListener
public class LogbackConfigListener implements ServletContextListener {

	private static final String INFO = "info";
	private static final String WARN = "warn";
	private static final String ERROR = "error.";
	private static final String DEBUG = "debug";
	private static final String TRACE = "trace";
	private static final String OFF = "off";
	private static final String FILE_TO_URL_IMPL_KEY = "fileToUrlImpl";
	/**
	 * Context parameter name for the location.
	 */
	public static final String CONFIG_LOCATION_PARAM = "be.redlab.logback.location";
	/**
	 * Setting this as context parameter with an value info, warn, error, debug, trace (or off). Activates a default
	 * logger if no other configuration is found.
	 */
	public static final String CONFIG_DEFAULTS_ON = "be.redlab.logback.default";

	/**
	 * Prefix for classpath urls.
	 */
	public static final String LOCATION_PREFIX_CLASSPATH = "classpath:";

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		ILoggerFactory ilc = LoggerFactory.getILoggerFactory();

		if (!(ilc instanceof LoggerContext)) {
			sc.log(new StringBuilder("Can not configure logback. ").append(LoggerFactory.class).append(" is using ").append(ilc)
					.append(" which is not an instance of ").append(LoggerContext.class).toString());
			return;
		}
		LoggerContext lc = (LoggerContext) ilc;
		String location = sc.getInitParameter(CONFIG_LOCATION_PARAM);
		String defaultConfigOn = sc.getInitParameter(CONFIG_DEFAULTS_ON);
		boolean useDefault = (null != defaultConfigOn && !defaultConfigOn.isEmpty());
		URL url = null;
		if (location != null) {
			location = OptionHelper.substVars(location, lc);
		}
		if (null != location) {
			url = toUrl(sc, location);
		}
		if (url != null) {
			sc.log(new StringBuilder("Configuring logback. Config location = \"").append(location).append("\", full url = \"")
					.append(url).append("\".").toString());
			configure(sc, url, lc);
		}
		if (location == null || url == null) {
			if (useDefault) {
				String level = toLevel(defaultConfigOn);
				sc.log(new StringBuilder("Configuring logback default config for level[").append(level)
						.append("]. Could not find logback config, Config location = \"")
						.append(location)
						.append("\".").toString());
				configure(sc, toUrl(sc, LOCATION_PREFIX_CLASSPATH + "be/redlab/logback/listener/logbackwebfragment-" + level + ".xml"), lc);
			} else {
				sc.log(new StringBuilder("Can not configure logback. Could not find logback config, Config location = \"").append(location)
						.append("\".").toString());
			}
		}
	}

	/**
	 * @param defaultConfigOn
	 * @return
	 */
	private String toLevel(final String defaultConfigOn) {
		if (null != defaultConfigOn) {
			if (TRACE.equalsIgnoreCase(defaultConfigOn)) {
				return TRACE;
			} else if (DEBUG.equalsIgnoreCase(defaultConfigOn)) {
				return DEBUG;
			} else if (ERROR.equalsIgnoreCase(defaultConfigOn)) {
				return ERROR;
			} else if (WARN.equalsIgnoreCase(defaultConfigOn)) {
				return WARN;
			} else if (OFF.equalsIgnoreCase(defaultConfigOn)) {
				return OFF;
			}
		}
		return INFO;

	}

	protected void configure(final ServletContext sc, final URL location, final LoggerContext lc) {
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.stop();
		try {
			configurator.doConfigure(location);
		} catch (JoranException e) {
			sc.log("Failed to configure logback.", e);
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
	}

	protected URL toUrl(final ServletContext sc, final String location) {
		URL url = null;
		if (location.startsWith("/")) {
			try {
				url = sc.getResource(location);
			} catch (MalformedURLException e1) {
			}
		}
		if (url == null && location.startsWith(LOCATION_PREFIX_CLASSPATH)) {
			url = Thread
					.currentThread()
					.getContextClassLoader()
					.getResource(
							location.substring(LOCATION_PREFIX_CLASSPATH
									.length()));
		}
		if (url == null) {
			try {
				url = new URL(location);
			} catch (MalformedURLException e) {
			}
		}
		if (url == null) {
			url = fileToUrl(location, url);
		}
		return url;
	}

	public URL fileToUrl(final String location, final URL url) {
		Properties p = new Properties();
		try {
			p.load(LogbackConfigListener.class.getResourceAsStream("/be/redlab/logback/listener/setup.properties"));
			String property = p.getProperty(FILE_TO_URL_IMPL_KEY);
			FileToUrl newInstance = (FileToUrl) Class.forName(property).newInstance();
			return newInstance.fileToUrl(location, url);
		} catch (IOException e) {
			throw new RuntimeException("Unable to detect implementation for FileToUrl", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to instantiate implementation for FileToUrl", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to access implementation for FileToUrl", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to find implementation for FileToUrl", e);
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		ILoggerFactory ilc = LoggerFactory.getILoggerFactory();
		if (ilc instanceof LoggerContext) {
			LoggerContext lc = (LoggerContext) ilc;
			lc.stop();
		}
	}
}
