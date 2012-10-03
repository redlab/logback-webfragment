/*
 * Copyright (c) 2012 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener.file2url;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import be.redlab.logback.listener.FileToUrl;

/**
 * @author redlab
 *
 */
public class FileToUrl6 implements FileToUrl {

	/* (non-Javadoc)
	 * @see be.redlab.logback.listener.FileToUrl#fileToUrl(java.lang.String, java.net.URL)
	 */
	@Override
	public URL fileToUrl(final String location, URL url) {
		File file = new File(location);
		if (!file.isAbsolute())
			file = file.getAbsoluteFile();
		if (file.canRead()) {
			try {
				url = file.toURI().normalize().toURL();
			} catch (MalformedURLException e) {
				// NO-OP
			}
		}
		return url;

	}

}
