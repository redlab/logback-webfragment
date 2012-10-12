/*
 * Copyright 2012 Balder Van Camp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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
