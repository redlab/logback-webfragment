/*
 * Copyright (c) 2013 Balder VC and others. All rights reserved. This program and the accompanying materials are
 * dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package be.redlab.logback.listener;

import java.net.URL;

/**
 * @author redlab
 *
 */
public interface FileToUrl {

	URL fileToUrl(final String location, URL url);
}
