package org.purc.purcforms.client.xforms;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;


/**
 * Utility methods used when manipulating xforms documents.
 * 
 * @author daniel
 *
 */
public class XformUtil {

	/**
	 * Gets the text value of a node.
	 * 
	 * @param node the node whose text value to get.
	 * @return the text value.
	 */
	public static String getTextValue(Element node){
		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE){

				//These iterations are for particularly firefox which when comes accross
				//text bigger than 4096, splits it into multiple adjacent text nodes
				//each not exceeding the maximum 4096. This is as of 04/04/2009
				//and for Firefox version 3.0.8
				String s = "";

				for(int index = i; index<numOfEntries; index++){
					Node currentNode = node.getChildNodes().item(index);
					String value = currentNode.getNodeValue();
					if(currentNode.getNodeType() == Node.TEXT_NODE && value != null)
						s += value;
					else
						break;
				}

				return s;
				//return node.getChildNodes().item(i).getNodeValue();
			}

			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				String val = getTextValue((Element)node.getChildNodes().item(i));
				if(val != null)
					return val;
			}
		}

		return null;
	}


	/**
	 * Sets the text value of a node.
	 * 
	 * @param node the node whose text value to set.
	 * @param value the text value.
	 * @return true if the value was set successfully, else false.
	 */
	public static boolean setTextNodeValue(Element node, String value){
		if(node == null)
			return false;

		int numOfEntries = node.getChildNodes().getLength();
		for (int i = 0; i < numOfEntries; i++) {
			if (node.getChildNodes().item(i).getNodeType() == Node.TEXT_NODE){
				node.getChildNodes().item(i).setNodeValue(value);
				return true;
			}

			if(node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE){
				if(setTextNodeValue((Element)node.getChildNodes().item(i),value))
					return true;
			}
		}
		return false;
	}
}
