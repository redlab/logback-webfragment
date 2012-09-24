/*
 * Copyright (c) 2012 Balder VC and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package be.redlab.logback.listener;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
 * {@link ServletContextListener} that can be used in web applications to define the location of the logback
 * configuration.
 *
 * <p>
 * Should be the first listener to configure logback before using it. Location is defined in the
 * <code>logbackConfigLocation</code> context param. Placeholders (ex: ${user.home}) are supported. Location examples:<br />
 * /WEB-INF/log.xml -> loaded from servlet context<br />
 * classpath:foo/log.xml -> loaded from classpath<br />
 * file:/configs/logfile.xml -> loaded as url<br />
 * D:/log-absfile.xml -> loaded as absolute file<br />
 * log-relfile.xml -> loaded as file relative to the servlet container working directory<br />
 * </p>
 * The code in this listener is taken from <a href="http://jira.qos.ch/browse/LOGBACK-557">this issue</a> in logback
 * jira.
 */
@WebListener
public class LogbackConfigListener implements ServletContextListener {

	private static final String INFO = "info";
	private static final String WARN = "warn";
	private static final String ERROR = "error.";
	private static final String DEBUG = "debug";
	private static final String TRACE = "trace";
	/**
	 * Context parameter name for the location.
	 */
	public static final String CONFIG_LOCATION_PARAM = "logbackWebfragment.config.location";
	/**
	 *
	 */
	public static final String CONFIG_DEFAULTS_ON = "logbackWebfragment.config.default";

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
				configure(sc, toUrl(sc, LOCATION_PREFIX_CLASSPATH + "logbackwebfragment-" + level + ".xml"), lc);
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
		if (location.startsWith("/"))
			try {
				url = sc.getResource(location);
			} catch (MalformedURLException e1) {
				// NO-OP
			}
		if (url == null && location.startsWith(LOCATION_PREFIX_CLASSPATH))
			url = Thread
					.currentThread()
					.getContextClassLoader()
					.getResource(
							location.substring(LOCATION_PREFIX_CLASSPATH
									.length()));
		if (url == null)
			try {
				url = new URL(location);
			} catch (MalformedURLException e) {
				// NO-OP
			}

		if (url == null) {
			Path file = Paths.get(location);
			if (!file.isAbsolute())
				file = file.toAbsolutePath();
			if (Files.isReadable(file))
				try {
					url = file.normalize().toUri().toURL();
				} catch (MalformedURLException e) {
					// NO-OP
				}
		}

		return url;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		ILoggerFactory ilc = LoggerFactory.getILoggerFactory();

		if (!(ilc instanceof LoggerContext))
			return;

		LoggerContext lc = (LoggerContext) ilc;
		lc.stop();
	}
}
