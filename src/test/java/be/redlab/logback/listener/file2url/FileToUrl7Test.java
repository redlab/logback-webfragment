/*
 * Copyright (c) 2013 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener.file2url;

import org.junit.Before;

import be.redlab.logback.listener.FileToUrl;

/**
 * @author redlab
 *
 */
public class FileToUrl7Test extends AbstractFileToUrlTest {

	private FileToUrl fileToUrl6;

	@Before
	public void init() {
		fileToUrl6 = new FileToUrl6();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see be.redlab.logback.listener.file2url.AbstractFileToUrlTest#getFileToUrl()
	 */
	@Override
	FileToUrl getFileToUrl() {
		return fileToUrl6;
	}


}