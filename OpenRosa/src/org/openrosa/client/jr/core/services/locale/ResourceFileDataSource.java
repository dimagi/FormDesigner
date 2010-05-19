/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * 
 */
package org.openrosa.client.jr.core.services.locale;

import java.io.IOException;

import org.openrosa.client.java.io.DataInputStream;
import org.openrosa.client.java.io.DataOutputStream;
import org.openrosa.client.jr.core.util.OrderedHashtable;
import org.openrosa.client.jr.core.util.externalizable.DeserializationException;
import org.openrosa.client.jr.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date Jun 1, 2009 
 *
 */
public class ResourceFileDataSource implements LocaleDataSource {
	
	String resourceURI;
	
	/**
	 * NOTE: FOR SERIALIZATION ONLY!
	 */
	public ResourceFileDataSource() {
		
	}
	
	/**
	 * Creates a new Data Source for Locale data with the given resource URI.
	 * 
	 * @param resourceURI a URI to the resource file from which data should be loaded
	 * @throws NullPointerException if resourceURI is null
	 */
	public ResourceFileDataSource(String resourceURI) {
		if(resourceURI == null) {
			throw new NullPointerException("Resource URI cannot be null when creating a Resource File Data Source");
		}
		this.resourceURI = resourceURI;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.locale.LocaleDataSource#getLocalizedText()
	 */
	public OrderedHashtable getLocalizedText() {
		return null;//loadLocaleResource(resourceURI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		resourceURI = in.readUTF();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(resourceURI);
	}

	private void parseAndAdd(OrderedHashtable locale, String line, int curline) {

		//trim whitespace.
		line = line.trim();
		
		//clear comments
		while(line.indexOf("#") != -1) {
			line = line.substring(0, line.indexOf("#"));
		}
		if(line.indexOf('=') == -1) {
			// TODO: Invalid line. Empty lines are fine, especially with comments,
			// but it might be hard to get all of those.
			if(line.trim().equals("")) {
				//Empty Line
			} else {
				System.out.println("Invalid line (#" + curline + ") read: " + line);
			}
		} else {
			//Check to see if there's anything after the '=' first. Otherwise there
			//might be some big problems.
			if(line.indexOf('=') != line.length()-1) {
				String value = line.substring(line.indexOf('=') + 1,line.length());
				locale.put(line.substring(0, line.indexOf('=')), value);
			}
			 else {
				System.out.println("Invalid line (#" + curline + ") read: '" + line + "'. No value follows the '='.");
			}
		}
	}

}
