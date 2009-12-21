package org.purc.purcforms.client.util;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Widget;


/**
 * Utilities used by the form designer.
 * 
 * @author daniel
 *
 */
public class FormDesignerUtil {

	/** The form designer title. */
	private static String title = "PurcForms FormDesigner";
	

	/**
	 * Creates an HTML fragment that places an image & caption together, for use
	 * in a group header.
	 * 
	 * @param imageProto an image prototype for an image
	 * @param caption the group caption
	 * @return the header HTML fragment
	 */
	public static String createHeaderHTML(ImageResource imageProto, String caption) {

		//Add the image and text to a horizontal panel
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);

		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(FormUtil.createImage(imageProto));
		HTML headerText = new HTML(caption);
		hPanel.add(headerText);

		return hPanel.getElement().getString();
	}


	/**
	 * Adds formatting to an XML string
	 */
	public static String formatXml(String xmlContent) {
		return FormUtil.formatXml(xmlContent);
	}

	/**
	 * Disables the browsers default context menu for the specified element.
	 *
	 * @param elem the element whose context menu will be disabled
	 */
	public static native void disableContextMenu(Element elem) /*-{
	    elem.oncontextmenu=function() {  return false};
	}-*/; 
	

	/**
	 * Puts a widget at a given position.
	 * 
	 * @param w the widget.
	 * @param left the left position in pixels.
	 * @param top the top position in pixels.
	 */
	public static void setWidgetPosition(Widget w, String left, String top) {
		FormUtil.setWidgetPosition(w, left, top);
	}
	
	/**
	 * Loads a list of questions into a MultiWordSuggestOracle for a given reference question.
	 * 
	 * @param questions the list of questions.
	 * @param refQuestion the reference question.
	 * @param oracle the MultiWordSuggestOracle.
	 * @param dynamicOptions set to true if we are loading for dynamic options.
	 * @param sameTypesOnly set to true if you want to load only questions of the same type
	 * 						as the referenced question.
	 */
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
			
			oracle.add(questionDef.getDisplayText());	
			
			//TODO Allowed for now since repeat questions will have ids which cant be equal to
			//those of parents. But test this to ensure it does not bring in bugs.
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				loadQuestions(questionDef.getRepeatQtnsDef().getQuestions(),refQuestion,oracle,dynamicOptions,sameTypesOnly); //TODO These have different id sets and hence we are leaving them out for now
		}
	}

	/**
	 * Loads a list of questions into a MultiWordSuggestOracle for a given reference question.
	 * 
	 * @param questions the list of questions.
	 * @param refQuestion the reference question.
	 * @param oracle the MultiWordSuggestOracle.
	 * @param dynamicOptions set to true if we are loading for dynamic options.
	 */
	public static void loadQuestions(Vector questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle, boolean dynamicOptions){
		loadQuestions(questions, refQuestion, oracle, dynamicOptions,true);
	}

	/**
	 * Draws a widget selection rubber band on the mouse down event.
	 * 
	 * @param event the current mouse down event.
	 * @param elem the rubber band widget.
	 */
	public static native void startRubber(Event event,Element elem) /*-{
		     elem.style.width = 0;
		     elem.style.height = 0;
		     elem.style.left = event.x;
		     elem.style.top  = event.y;
		     elem.style.visibility = 'visible';
	}-*/;

	/**
	 * Removes the widget selection rubber band on the mouse up event.
	 * 
	 * @param event the current mouse up event.
	 * @param elem the rubber band element.
	 */
	public static native void stopRubber(Event event,Element elem) /*-{
		   elem.style.visibility = 'hidden';
    }-*/;
	

	/**
	 * Moves the rubber band on the mouse move event.
	 * 
	 * @param event the current mouse move event.
	 * @param elem the rubber band element.
	 */
	public static native void moveRubber(Event event, Element elem) /*-{
		     elem.style.width = event.x - elem.style.left;
		     elem.style.height = event.y - elem.style.top;
	}-*/;
	
	
	/**
	 * Gets the title of the form designer.
	 * 
	 * @return the form designer title.
	 */
	public static String getTitle(){
		return title;
	}
	
	/**
	 * Sets the title of the form designer.
	 */
	public static void setDesignerTitle(){
		String s = FormUtil.getDivValue("title");
		if(s != null && s.trim().length() > 0)
			title = s;
		Window.setTitle(title);
	}

	/**
	 * Checks if the CTRL key is currently pressed.
	 * 
	 * @return true if pressed, else false.
	 */
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
		
		s = s.replace("'", "");
		
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
