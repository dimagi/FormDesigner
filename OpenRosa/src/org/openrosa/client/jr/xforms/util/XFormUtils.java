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

package org.openrosa.client.jr.xforms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.openrosa.client.java.io.DataInputStream;
import org.openrosa.client.jr.core.model.FormDef;
import org.openrosa.client.jr.core.util.externalizable.DeserializationException;
import org.openrosa.client.jr.core.util.externalizable.ExtUtil;
import org.openrosa.client.jr.xforms.parse.XFormParser;

import com.google.gwt.xml.client.Element;

/**
 * Static Utility methods pertaining to XForms.
 *
 * @author Clayton Sims
 *
 */
public class XFormUtils {
	
	/*public static FormDef getFormFromResource (String resource) {
		InputStream is = System.class.getResourceAsStream(resource);
		if (is == null) {
			System.err.println("Can't find form resource \"" + resource + "\". Is it in the JAR?");
			return null;
		}
		
		return getFormFromInputStream(is);
	}*/

	/*public static FormDef getFormFromInputStream(InputStream is) {
		FormDef returnForm = null;
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(is,"UTF-8");
		} catch(UnsupportedEncodingException uee) {
			System.out.println("UTF 8 encoding unavailable, trying default encoding");
			isr = new InputStreamReader(is); 
		}
		
		if(isr != null) {
			returnForm = XFormParser.getFormDef(isr);
		}
		try {
			isr.close();
		}
		catch(IOException e) {
			System.err.println("IO Exception while closing stream.");
			e.printStackTrace();
		}
		return returnForm;
	}*/

	/*public static FormDef getFormFromSerializedResource(String resource) {
		FormDef returnForm = null;
		InputStream is = System.class.getResourceAsStream(resource);
		try {
			if(is != null) {
				DataInputStream dis = new DataInputStream();
				returnForm = (FormDef)ExtUtil.read(dis, FormDef.class);
				dis.close();
				is.close();
			}else{
				//#if debug.output==verbose
				System.out.println("ResourceStream NULL");
				//#endif
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return returnForm;
	}*/
	
	
	/////Parser Attribute warning stuff
	
	public static Vector getAttributeList(Element e){
		Vector atts = new Vector();
		//for(int i=0;i<e.getAttributeCount();i++){ ?????????????
		for(int i=0;i<e.getAttributes().getLength();i++){
			atts.addElement(e.getAttributes().item(i).getNodeName()); //getAttributeName(i));
		}
		
		return atts;
	}
	
	public static Vector getUnusedAttributes(Element e,Vector usedAtts){
		Vector unusedAtts = getAttributeList(e);
		for(int i=0;i<usedAtts.size();i++){
			if(unusedAtts.contains(usedAtts.elementAt(i))){
				unusedAtts.removeElement(usedAtts.elementAt(i));
			}
		}
		
		return unusedAtts;
	}
	
	public static String unusedAttWarning(Element e, Vector usedAtts){
		String warning = "Warning: ";
		Vector ua = getUnusedAttributes(e,usedAtts);
		warning+=ua.size()+" Unrecognized attributes found in Element ["+e.getNodeName()+"] and will be ignored: ";
		warning+="[";
		for(int i=0;i<ua.size();i++){
			warning+=ua.elementAt(i);
			if(i!=ua.size()-1) warning+=",";
		}
		warning+="] ";
		warning+="Location:\n"+XFormParser.getVagueLocation(e);
		
		return warning;
	}
	
	public static boolean showUnusedAttributeWarning(Element e, Vector usedAtts){
		return getUnusedAttributes(e,usedAtts).size()>0;
	}
	
	/**
	 * Is this element an Output tag?
	 * @param e
	 * @return
	 */
	public static boolean isOutput(Element e){
		if(e.getNodeName().toLowerCase().equals("output")) return true;
		else return false;
	}
	
}
