package org.purc.purcforms.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * Utilities used by the form designer.
 * 
 * @author daniel
 *
 */
public class FormDesignerUtil {

	private static String title = "PurcForms FormDesigner";
	
	/**
	 * Maximizes a widget.
	 * 
	 * @param widget the widget to maximize.
	 */
	public static void maximizeWidget(Widget widget){
		FormUtil.maximizeWidget(widget);
	}

	/**
	 * Creates an HTML fragment that places an image & caption together, for use
	 * in a group header.
	 * 
	 * @param imageProto an image prototype for an image
	 * @param caption the group caption
	 * @return the header HTML fragment
	 */
	public static String createHeaderHTML(AbstractImagePrototype imageProto, String caption) {

		//Add the image and text to a horizontal panel
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);

		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(imageProto.createImage());
		HTML headerText = new HTML(caption);
		hPanel.add(headerText);

		return hPanel.getElement().getString();
	}

	public static void showWait() {
		Element element = RootPanel.get().getElement();
		DOM.setStyleAttribute(element,"cursor","wait");
		//RootPanel.get().setsetFocus(true);
		DOM.setCapture(element);

	}

	public static void showUnwait() {
		Element element = RootPanel.get().getElement();
		DOM.releaseCapture(element);
		DOM.setStyleAttribute(element,"cursor","default");

	} 

	/**
	 * Add formatting to an XML string
	 */
	public static String formatXml(String xmlContent) {
		return FormUtil.formatXml(xmlContent);
	}

	/**
	 * Disables the browsers default context menu for the specified
	element.
	 *
	 * @param elem the element whos context menu will be disabled
	 */
	public static native void disableContextMenu(Element elem) /*-{
	    elem.oncontextmenu=function() {  return false};
	}-*/; 
	
	public static native void disableClick(Element elem) /*-{
    	elem.onclick=function() {  return false};
	}-*/; 

	public static void setWidgetPosition(Widget w, String left, String top) {
		FormUtil.setWidgetPosition(w, left, top);
	}
	
	public static void loadQuestions(Vector questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle, boolean dynamicOptions, boolean sameTypesOnly){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			
			if(!dynamicOptions && refQuestion != null && refQuestion.getDataType() != questionDef.getDataType() && sameTypesOnly)
				continue;
			
			if(dynamicOptions && !(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC))
				continue;
			
			if(dynamicOptions && refQuestion == questionDef)
				continue;
			
			if(!dynamicOptions && refQuestion == questionDef)
				continue;
			
			oracle.add(questionDef.getText());	
			
			//TODO Allowed for now since repeat questions will have ids which cant be equal to
			//those of parents. But test this to ensure it does not bring in bugs.
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				loadQuestions(questionDef.getRepeatQtnsDef().getQuestions(),refQuestion,oracle,dynamicOptions,sameTypesOnly); //TODO These have different id sets and hence we are leaving them out for now
		}
	}

	public static void loadQuestions(Vector questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle, boolean dynamicOptions){
		loadQuestions(questions, refQuestion, oracle, dynamicOptions,true);
	}

	public static void loadOptions(List options, MultiWordSuggestOracle oracle){
		FormUtil.loadOptions(options, oracle);
	}

	public static void allowNumericOnly(TextBox textBox, boolean allowDecimal){
		FormUtil.allowNumericOnly(textBox, allowDecimal);
	}

	public static KeyboardListenerAdapter getAllowNumericOnlyKeyboardListener(TextBox textBox, boolean allowDecimal){
		return FormUtil.getAllowNumericOnlyKeyboardListener(textBox, allowDecimal);
	}

	public static native void startRubber(Event event,Element elem) /*-{
		     elem.style.width = 0;
		     elem.style.height = 0;
		     elem.style.left = event.x;
		     elem.style.top  = event.y;
		     elem.style.visibility = 'visible';
	}-*/;

	/**
	 * Removes the widget selection rubber band.
	 * 
	 * @param event
	 * @param elem
	 */
	public static native void stopRubber(Event event,Element elem) /*-{
		   elem.style.visibility = 'hidden';
     }-*/;

	public static native void moveRubber(Event event, Element elem) /*-{
		     elem.style.width = event.x - elem.style.left;
		     elem.style.height = event.y - elem.style.top;
	}-*/;
	
	public static String getTitle(){
		return title;
	}
	
	public static void setTitle(String t){
		title = t;
	}
	
	public static void setDesignerTitle(){
		String s = FormUtil.getDivValue("title");
		if(s != null && s.trim().length() > 0)
			title = s;
		Window.setTitle(title);
	}
	
	/*public String formatDate(Date date){
		DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy-MM-dd");
		return formatter.format(date);
	}*/
	
	public static boolean getCtrlKey(){
		Event event = DOM.eventGetCurrentEvent();
		if(event == null)
			return false;
		return event.getCtrlKey();
	}
	
	/**
	 * Converts a string into a valid XML token (tag name)
	 * 
	 * @param s string to convert into XML token
	 * @return valid XML token based on s
	 */
	public static String getXmlTagName(String s) {
		// Converts a string into a valid XML token (tag name)
		// No spaces, start with a letter or underscore, not 'xml*'
		
		// if len(s) < 1, return '_blank'
		if (s == null || s.length() < 1)
			return "_blank";
		
		// xml tokens must start with a letter
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
		
		// after the leading letter, xml tokens may have
		// digits, period, or hyphen
		String nameChars = letters + "0123456789.-";
		
		// special characters that should be replaced with valid text
		// all other invalid characters will be removed
		HashMap<String, String> swapChars = new HashMap<String, String>();
		swapChars.put("!", "bang");
		swapChars.put("#", "pound");
		swapChars.put("\\*", "star");
		swapChars.put("'", "apos");
		swapChars.put("\"", "quote");
		swapChars.put("%", "percent");
		swapChars.put("<", "lt");
		swapChars.put(">", "gt");
		swapChars.put("=", "eq");
		swapChars.put("/", "slash");
		swapChars.put("\\\\", "backslash");
		
		// start by cleaning whitespace and converting to lowercase
		s = s.replaceAll("^\\s+", "").replaceAll("\\s+$", "").replaceAll("\\s+", "_").toLowerCase();
		
		// swap characters
		Set<Entry<String, String>> swaps = swapChars.entrySet();
		for (Entry<String, String> entry : swaps) {
			if (entry.getValue() != null)
				s = s.replaceAll(entry.getKey(), "_" + entry.getValue() + "_");
			else
				s = s.replaceAll(String.valueOf(entry.getKey()), "");
		}
		
		// ensure that invalid characters and consecutive underscores are
		// removed
		String token = "";
		boolean underscoreFlag = false;
		for (int i = 0; i < s.length(); i++) {
			if (nameChars.indexOf(s.charAt(i)) != -1) {
				if (s.charAt(i) != '_' || !underscoreFlag) {
					token += s.charAt(i);
					underscoreFlag = (s.charAt(i) == '_');
				}
			}
		}
		
		// remove extraneous underscores before returning token
		token = token.replaceAll("_+", "_");
		token = token.replaceAll("_+$", "");
		
		// make sure token starts with valid letter
		if (letters.indexOf(token.charAt(0)) == -1 || token.startsWith("xml"))
			token = "_" + token;
		
		// return token
		return token;
	}
}
