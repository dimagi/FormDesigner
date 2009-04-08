package org.purc.purcforms.server.xpath;

import java.io.Serializable;
import java.util.Vector;

import org.w3c.dom.Node;


/**
 * @author Cosmin
 * 
 */
public class XPathExpression implements Serializable
{
	String[] locationStepStringsArray;
	XPathLocationStep[] locationStepArray;
	Vector resultNodeSet;
	String expression = null;
	Node startNode = null;
		
	public XPathExpression (Node startNode, String expression)
	{
		Vector tmp = new Vector();

		this.startNode = startNode;
		this.expression = expression;
		
		//I do not support function name in the start
		//of an xpath expression
		
		//parse
		if(expression.startsWith("//")) {
			//this way of handling "//" is obviously incomplete
			//but we allow it like this because of the lacking resources
			tmp.addElement("//");
			expression = new String(expression.toCharArray(), 2, expression.length()-2);
		} else if(expression.startsWith("/")) {
			tmp.addElement("/");
			//trace the root element
			expression = new String(expression.toCharArray(), 1, expression.length()-1);
		} 
		//System.out.println("Expression "+expression+" start node is "+start);
		
		//because there is no support for StringTokenizer
		//on j2me we remove this
		/*
		StringTokenizer st = new StringTokenizer(expression, "/");
		locationStepStringsArray = new String[st.countTokens()];
		for(int i = 0; i < locationStepStringsArray.length; i++) {
			locationStepStringsArray[i] = st.nextToken();
			System.out.println("location step: "+locationStepStringsArray[i]);
		}
		*/
		for(int start = 0, end = 0; end < expression.length()-1 && end!=-1; start = end+1) {
			end = expression.indexOf("/", start);
			//System.out.println("start = "+start+" end = "+end);
			tmp.addElement(new String(expression.toCharArray(), start, 
				(end!=-1?end:expression.length())-start));
		}
		locationStepStringsArray = new String[tmp.size()];
		tmp.copyInto(locationStepStringsArray);
		tmp = null;
		
		//the result node set should contain nodes
		//with regard to the starting poing of the xpath expression
		//for now just pass the root of the document
		resultNodeSet = new Vector();
		resultNodeSet.addElement(startNode);
		
		//start processing every location
		for(int j=0; j < locationStepStringsArray.length; j++)
		{
			String locationStepString = locationStepStringsArray[j];
			XPathLocationStep locationStep = new XPathLocationStep(locationStepString);

			resultNodeSet = locationStep.getResult(resultNodeSet);
		}
	}
	
	public Vector getResult()
	{
		return resultNodeSet;
	}
}
