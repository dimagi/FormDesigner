package org.purc.purcforms.client.xforms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.xpath.XPathExpression;

import com.google.gwt.xml.client.Element;


/**
 * Utility methods used for getting default values in xforms xml documents
 * and updating their corresponding question definition objects in the object model.
 * 
 * @author daniel
 *
 */
public class DefaultValueUtil {

	/**
	 * All methods in this class are static and hence we expect no external
	 * Instantiation of this class.
	 */
	private DefaultValueUtil(){

	}


	/**
	 * Sets all default values of questions in a form definition object
	 * as per the xforms document being parsed.
	 * 
	 * @param dataNode the xforms instance data node.
	 * @param formDef the form definition object.
	 * @param id2VarNameMap a map between questions ids and their binding or variableName.
	 */
	public static void setDefaultValues(Element dataNode,FormDef formDef,HashMap id2VarNameMap){
		boolean valueSet = false;
		String id, val;
		Iterator keys = id2VarNameMap.keySet().iterator();
		while(keys.hasNext()){
			id = (String)keys.next();
			String variableName = (String)id2VarNameMap.get(id);

			QuestionDef def = formDef.getQuestion(variableName);
			if(def == null)
				continue;

			valueSet = false; val = null;

			if(variableName.contains("@"))
				setAttributeDefaultValue(def,variableName,dataNode);
			else{
				Element node = dataNode;
				if(!id.equals(variableName)){
					valueSet = true;
					node = XformUtil.getValueNode(dataNode,variableName);
					if(node != null)
						val = XmlUtil.getTextValue(node);
				}

				if(node != null){
					if(!valueSet)
						val = XmlUtil.getNodeTextValue(node,id);

					if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
						continue;

					def.setDefaultValue(val);
				}
			}
		}

		//Now do set default values for repeats since they are not part of the id2VarNameMap
		/*for(int pageNo=0; pageNo<formDef.getPageCount(); pageNo++){
			PageDef pageDef = formDef.getPageAt(pageNo);
			for(int qtnNo=0; qtnNo<pageDef.getQuestionCount(); qtnNo++){
				QuestionDef questionDef = pageDef.getQuestionAt(qtnNo);
				if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
					setRptQtnsDefaultValues(questionDef.getDataNode(),formDef,questionDef.getRepeatQtnsDef());
			}
		}*/
	}


	/**
	 * Sets a question's default value which comes from a node attribute value.
	 * 
	 * @param qtn the question definition object.
	 * @param variableName the binding or variable name of the question.
	 * @param dataNode the xforms instance data node.
	 */
	private static void setAttributeDefaultValue(QuestionDef qtn, String variableName,Element dataNode){
		String xpath = variableName;
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos == 0){
			attributeName = variableName.substring(1,variableName.length());
			String value = dataNode.getAttribute(attributeName);
			if(value != null && value.trim().length() > 0) //we are not allowing empty strings for now.
				qtn.setDefaultValue(value);
			return;
		}

		attributeName = xpath.substring(pos+1,xpath.length());
		xpath = xpath.substring(0,pos-1);

		XPathExpression xpls = new XPathExpression(dataNode, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				String value = ((Element) obj).getAttribute(attributeName);
				if(value != null && value.trim().length() > 0){ //we are not allowing empty strings for now.
					qtn.setDefaultValue(value);
					break;
				}
			}
		}
	}


	/**
	 * Sets the default values for child questions of a repeat question type.
	 * 
	 * @param dataNode the xforms instance data node.
	 * @param formDef the form definition object to which the repeat question belongs.
	 * @param repeatQtnsDef the repeat question definition object.
	 */
	/*private static void setRptQtnsDefaultValues(Element dataNode, FormDef formDef, RepeatQtnsDef repeatQtnsDef){
		for(int i=0; i<repeatQtnsDef.getQuestionsCount(); i++){
			QuestionDef questionDef = repeatQtnsDef.getQuestionAt(i);
			String id = questionDef.getVariableName();
			String val = XmlUtil.getNodeTextValue(dataNode,id);
			if(val == null || val.trim().length() == 0) //we are not allowing empty strings for now.
				continue;
			questionDef.setDefaultValue(val);
		}
	}*/
}
