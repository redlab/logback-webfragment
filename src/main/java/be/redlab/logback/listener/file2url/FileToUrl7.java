/*
 * Copyright (c) 2012 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener.file2url;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import be.redlab.logback.listener.FileToUrl;

/**
 * @author redlab
 *
 */
public class FileToUrl7 implements FileToUrl {

	/*
	 * (non-Javadoc)
	 *
	 * @see be.redlab.logback.listener.FileToUrl#fileToUrl(java.lang.String, java.net.URL)
	 */
	@Override
	public URL fileToUrl(final String location, URL url) {
		Path file = Paths.get(location);
		if (!file.isAbsolute())
			file = file.toAbsolutePath();
		if (Files.isReadable(file)) {
			try {
				url = file.normalize().toUri().toURL();
			} catch (MalformedURLException e) {
				// NO-OP
			}
		}
		return url;
	}

}
