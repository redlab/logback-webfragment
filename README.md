logback-webfragment
===================
logback-webfragment provides a ServletContextListener for loading/reloading logback configuration from files in a servlet 3.x, Java 7 or Java 6 enabled web application.

[![Build Status](https://redlab.ci.cloudbees.com/job/logback-webfragment/badge/icon)](https://redlab.ci.cloudbees.com/job/logback-webfragment/)

Usage
===================
A ServletContextListener that can be used in web applications to define the location of the logback configuration.
This listener should be the first listener to be configured to configure logback before using it.
The location of a logback configuration file is defined by the 'be.redlab.logback.location' web.xml context param. Placeholders from System.properties (ex: ${user.home}) are supported.

Location property examples:
 * /WEB-INF/logback.xml -> loaded from servlet context
 * classpath:foo/logback.xml -> loaded from classpath
 * file:/opt/configs/app/logback.xml -> loaded as url
 * /opt/configs/app/logback.xml -> loaded as absolute file
 * logback.xml -> loaded as file relative to the servlet container working directory

Aditionally, it is possible to use the context param 'be.redlab.logback.default' with values OFF, ERROR, WARN, INFO, DEBUG, TRACE or no value. If the configured be.redlab.logback.location results in an unfindable configuration or does not exists, a default logger that logs the given level (or info as default) to the current console, is activated. If the property 'be.redlab.logback.default' is not available, no default logger is activated.

Servlet 3.x Environment
======================
The webfragment is configured to be loaded before all others. The jars are named aaa-logback-webfragment, the aaa increasing the probability to be the first webfragment loaded in a Servlet 3.x environment.

Servlet 2.x Environment
======================
In a Servlet 2.x environment, configure the listener in your web.xml.

Java 7 or 6
======================
By default the Java 7 version is build by maven. If you'd require a Java 6 version, use the pom-jdk6.xml pom file to build. The Java 6 artifact is named aaa-logback-webfragment-java6. The Java 7 artifact aaa-logback-webfragment.

Dependencies
======================
The pom is configured with logback 1.0.6 and a servlet 3.1-b01 artifacts as dependencies, both are set on scope 'provided' it is up to you to include the dependencies in your own jar/war/ear or let them be provided by your container. The code is compatible with logback 9.8.x to at least 1.0.6. However only in a servlet 3.x environment. The @WebListener annotation will normally not be found in lower versioned Servlet Containers.

Maven
======================
Java 7 version

	<dependency>
	    <groupId>be.redlab.logback</groupId>
	    <artifactId>aaa-logback-webfragment</artifactId>
	    <version>1.0.1</version>
	</dependency>
I can upload Java 6 version if requested.


example
=====================
extract from a web.xml

	<context-param>
		<param-name>be.redlab.logback.location</param-name>
		<param-value>classpath:logback.xml</param-value>
	</context-param>
	<context-param>
		<param-name>be.redlab.logback.default</param-name>
		<param-value>info</param-value>
	</context-param>

Note
===================
The code in this listener is inspired from <a href="http://jira.qos.ch/browse/LOGBACK-557">this issue</a> in logbacks jira. And altered by redlab. The code is under the same licenses as logback (EPL and LGPL, to the licensees choosing)
