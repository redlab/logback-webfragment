/*
 * Copyright (c) 2013 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener.file2url;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import be.redlab.logback.listener.FileToUrl;

/**
 * @author redlab
 *
 */
public abstract class AbstractFileToUrlTest {

	@Rule
	public final TemporaryFolder f = new TemporaryFolder();
	private File file;

	@Before
	public final void setup() throws IOException {
		file = f.newFile("afiletolocate");
	}

	@Test
	public void fromFile() throws MalformedURLException, URISyntaxException {
		URL url = getFileToUrl().fileToUrl(file.getPath(), URI.create("file://logback.redlab.be").toURL());
		Assert.assertEquals(file.getPath(), Paths.get(url.toURI()).toString());
	}

	/**
	 * @return
	 */
	abstract FileToUrl getFileToUrl();

	@Test
	public void original() throws MalformedURLException, URISyntaxException {
		URL url = getFileToUrl().fileToUrl("hopefullynoonewillnameadirectorylikethis/unexisting.file",
				URI.create("http://logback.redlab.be")
				.toURL());
		Assert.assertEquals("http://logback.redlab.be", url.toString());
	}
}
