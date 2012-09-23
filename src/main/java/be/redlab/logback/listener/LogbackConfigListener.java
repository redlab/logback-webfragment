/*
 * Copyright (c) 2012 Balder VC and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package be.redlab.logback.listener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
 * /WEB-INF/log-sc.xml -> loaded from servlet context<br />
 * classpath:foo/log-cp.xml -> loaded from classpath<br />
 * file:/D:/log-absfile.xml -> loaded as url<br />
 * D:/log-absfile.xml -> loaded as absolute file<br />
 * log-relfile.xml -> loaded as file relative to the servlet container working directory<br />
 * </p>
 * The code in this listener is taken from <a href="http://jira.qos.ch/browse/LOGBACK-557">this issue</a> in logback
 * jira.
 */
@WebListener
public class LogbackConfigListener implements ServletContextListener {

	/**
	 * Context param name.
	 */
	public static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";

	/**
	 * Prefix for classpath urls.
	 */
	public static final String LOCATION_PREFIX_CLASSPATH = "classpath:";

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		ILoggerFactory ilc = LoggerFactory.getILoggerFactory();

		if (!(ilc instanceof LoggerContext)) {
			sc.log("Can not configure logback. " + LoggerFactory.class
					+ " is using " + ilc + " which is not an instance of "
					+ LoggerContext.class);
			return;
		}

		LoggerContext lc = (LoggerContext) ilc;

		String location = sc.getInitParameter(CONFIG_LOCATION_PARAM);

		if (location != null)
			location = OptionHelper.substVars(location, lc);

		if (location == null) {
			sc.log("Can not configure logback. Location is null."
					+ " Maybe context param \"" + CONFIG_LOCATION_PARAM
					+ "\" is not set or is not correct.");
			return;
		}

		URL url = toUrl(sc, location);

		if (url == null) {
			sc.log("Can not configure logback. Could not find logback"
					+ " config neither as servlet context-, nor as"
					+ " classpath-, nor as url-, nor as file system"
					+ " resource. Config location = \"" + location + "\".");
			return;
		}

		sc.log("Configuring logback. Config location = \"" + location
				+ "\", full url = \"" + url + "\".");

		configure(sc, url, lc);
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
			File file = new File(location);
			if (!file.isAbsolute())
				file = file.getAbsoluteFile();
			if (file.isFile())
				try {
					url = file.toURI().normalize().toURL();
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
