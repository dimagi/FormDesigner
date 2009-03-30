package org.purc.purcforms.client.util;

import java.util.Vector;

import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.view.ProgressDialog;

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

	public static int convertDimensionToInt(String dimension){
		if(dimension == null || dimension.trim().length() == 0)
			return 0;
		return Integer.parseInt(dimension.substring(0,dimension.length()-2));
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

	public static void setWidgetPosition(Widget w, String left, String top) {
		FormUtil.setWidgetPosition(w, left, top);
	}

	public static void loadQuestions(Vector questions, QuestionDef refQuestion, MultiWordSuggestOracle oracle){
		if(questions == null)
			return;

		for(int i=0; i<questions.size(); i++){
			QuestionDef questionDef = (QuestionDef)questions.elementAt(i);
			
			if(refQuestion != null && refQuestion.getDataType() != questionDef.getDataType())
				continue;
			
			oracle.add(questionDef.getText());	
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				;//loadQuestions(questionDef.getRepeatQtnsDef().getQuestions(),oracle); //TODO These have different id sets and hence we are leaving them out for now
		}
	}

	public static void loadOptions(Vector options, MultiWordSuggestOracle oracle){
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
}
