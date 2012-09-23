logback-webfragment
===================

logback-webfragment provides a ServletContextListener for loading/reloading logback configuration from files in a servlet 3.x, Java7 enabled web application


ServletContextListener that can be used in web applications to define the location of the logback configuration.
Should be the first listener to configure logback before using it. The webfragment is configured to be loaded before all others.
Location is defined in the 'logbackConfigLocation' context param. Placeholders (ex: ${user.home}) are supported. 
Location examples:
 * /WEB-INF/log-sc.xml -> loaded from servlet context
 * classpath:foo/log-cp.xml -> loaded from classpath
 * file:/D:/log-absfile.xml -> loaded as url
 * D:/log-absfile.xml -> loaded as absolute file
 * log-relfile.xml -> loaded as file relative to the servlet container working directory

The code in this listener is taken from <a href="http://jira.qos.ch/browse/LOGBACK-557">this issue</a> in logback jira.