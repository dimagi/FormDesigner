package org.purc.purcforms.client.util;

import java.util.List;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.view.ErrorDialog;
import org.purc.purcforms.client.view.ProgressDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


//TODO May need to separate form designer specific utilities from those used by the form runner.

/**
 * Utilities used by the form designer and runtime engine.
 *
 * @author daniel
 *
 */
public class FormUtil {

	/** The date time format used in the xforms model xml. */
	private static DateTimeFormat dateTimeSubmitFormat;

	/** The date time format used for display purposes. */
	private static DateTimeFormat dateTimeDisplayFormat;

	/** The date format used in the xforms model xml. */
	private static DateTimeFormat dateSubmitFormat;

	/** The date format used for display purposes. */
	private static DateTimeFormat dateDisplayFormat;

	/** The time format used in the xforms model xml. */
	private static DateTimeFormat timeSubmitFormat;

	/** The time format used for display purposes. */
	private static DateTimeFormat timeDisplayFormat;

	private static String formDefDownloadUrlSuffix;
	private static String formDefUploadUrlSuffix;
	private static String entityFormDefDownloadUrlSuffix;
	private static String formDataUploadUrlSuffix;
	private static String afterSubmitUrlSuffix;
	private static String formDefRefreshUrlSuffix;
	private static String externalSourceUrlSuffix;
	private static String multimediaUrlSuffix;

	/** 
	 * The url to navigate to when one closes the form designer by selecting
	 * Close from the file menu. 
	 */
	private static String closeUrl;

	/** The name for the formId field. */
	private static String formIdName;

	/** The name for the entityId field. */
	private static String entityIdName;

	/** The form identifier. */
	private static String formId;

	/** The entity identifier. eg patientId, individualId. */
	private static String entityId;

	/** The default font family used by the form designer. */
	private static String defaultFontFamily;

	/** Flag determining whether to append the entity id to the url 
	 * we go to after a form submission. eg ........?patientId=13
	 */
	private static boolean appendEntityIdAfterSubmit;

	/** 
	 * Flag determining whether to display the language xml tab or not.
	 */
	//private static boolean showLanguageTab = false;

	/**
	 * Flag determining whether to display the form submitted successfully message or not.
	 */
	private static boolean showSubmitSuccessMsg = false;

	/** The dialog used to show all progress messages. */
	public static ProgressDialog dlg = new ProgressDialog();

	/**
	 * Maximizes a widget.
	 * 
	 * @param widget the widget to maximize.
	 */
	public static void maximizeWidget(Widget widget){
		widget.setSize("100%", "100%");
	}

	//TODO These two functions need to be merged.
	public static void allowNumericOnly(TextBox textBox, boolean allowDecimal){
		final boolean allowDecimalPoints = allowDecimal;
		textBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if ((!Character.isDigit(keyCode)) && (keyCode != (char) KeyCodes.KEY_TAB)
						&& (keyCode != (char) KeyCodes.KEY_BACKSPACE) && (keyCode != (char) KeyCodes.KEY_LEFT)
						&& (keyCode != (char) KeyCodes.KEY_UP) && (keyCode != (char) KeyCodes.KEY_RIGHT)
						&& (keyCode != (char) KeyCodes.KEY_DOWN)) {

					if(keyCode == '.' && allowDecimalPoints && !((TextBox)event.getSource()).getText().contains("."))
						return;

					String text = ((TextBox) event.getSource()).getText().trim();
					if(keyCode == '-'){
						if(text.length() == 0 || ((TextBox)event.getSource()).getCursorPos() == 0)
							return;
					}

					((TextBox) event.getSource()).cancelKey(); 
				}
			}
		});

		textBox.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event){
				try{
					if(allowDecimalPoints)
						Double.parseDouble(((TextBox) event.getSource()).getText().trim());
					else
						Integer.parseInt(((TextBox) event.getSource()).getText().trim());
				}
				catch(Exception ex){
					((TextBox) event.getSource()).setText(null);
				}
			}
		});
	}

	public static KeyPressHandler getAllowNumericOnlyKeyboardListener(TextBox textBox, boolean allowDecimal){
		final boolean allowDecimalPoints = allowDecimal;
		return new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				char keyCode = event.getCharCode();
				if ((!Character.isDigit(keyCode)) && (keyCode != (char) KeyCodes.KEY_TAB)
						&& (keyCode != (char) KeyCodes.KEY_BACKSPACE) && (keyCode != (char) KeyCodes.KEY_LEFT)
						&& (keyCode != (char) KeyCodes.KEY_UP) && (keyCode != (char) KeyCodes.KEY_RIGHT)
						&& (keyCode != (char) KeyCodes.KEY_DOWN)) {

					if(keyCode == '.' && allowDecimalPoints && !((TextBox)event.getSource()).getText().contains("."))
						return;

					String text = ((TextBox) event.getSource()).getText().trim();
					if((text.length() == 0 && keyCode == '-') || (keyCode == '-' && ((TextBox)event.getSource()).getCursorPos() == 0))
						return;

					((TextBox) event.getSource()).cancelKey(); 
				}
			}
		};
	}

	public static void setWidgetPosition(Widget w, String left, String top) {
		com.google.gwt.user.client.Element h = w.getElement();
		DOM.setStyleAttribute(h, "position", "absolute");
		DOM.setStyleAttribute(h, "left", left);
		DOM.setStyleAttribute(h, "top", top);
	}

	public static void loadOptions(List options, MultiWordSuggestOracle oracle){
		if(options == null)
			return;

		for(int i=0; i<options.size(); i++){
			OptionDef optionDef = (OptionDef)options.get(i);
			oracle.add(optionDef.getText());	
		}
	}

	private static String indent(String text, int indentLevel) {
		for( int count = indentLevel ; count > 0 ; count--)
			text += "  ";

		return text;
	}

	/**
	 * Add formatting to an XML string
	 */

	public static String formatXml(String xmlContent){
		if(xmlContent == null)
			return null;

		return formatXmlPrivate(formatXmlPrivate(xmlContent));
	}

	private static String formatXmlPrivate(String xmlContent) {

		String result = "";

		try {
			String prevBeginSection = "";
			int prevIndex = 0;

			for(int indentLevel = 0, index = 0 ; index < xmlContent.length() ; index++) {

				//Seek to next "<"
				index = xmlContent.indexOf("<", index);

				if(index < 0 || index >= xmlContent.length())
					break;

				//Trim out XML block
				String section = xmlContent.substring(index, xmlContent.indexOf(">", index) + 1);

				if(section.matches("<!--.*-->")) {
					//Is comment <!--....-->
					result = indent(result, indentLevel);
				}
				else if(section.matches("<!.*>")) {
					//Directive
					result = indent(result, indentLevel);
				}
				else if(section.matches("<\\?.*\\?>")) {
					//Is directive <?...?>
					result = indent(result, indentLevel);
				}
				else if(section.matches("<[\\s]*[/\\\\].*>")) {
					//Is closing tag </...>
					result = indent(result, --indentLevel);
				}
				else if(section.matches("<.*[/\\\\][\\s]*>")) {
					//Is standalone tag <.../>
					result = indent(result, indentLevel);
					prevBeginSection = section;
				}
				else {
					//Is begin tag <....>
					result = indent(result, indentLevel++);
					prevBeginSection = section;
				}

				//My addition of making <> and </> be on same line and include text between
				//if(prevSection.equalsIgnoreCase(section.replace("/", ""))){
				//and we do this when we come accross a closing tag.
				if(section.matches("<[\\s]*[/\\\\].*>")) {
					if(prevIndex > 0){
						int len = 1+(indentLevel*2);
						if(result.substring(result.length()-len).contains("\n")){
							if(isClosingPreviousBeginTag(prevBeginSection,section))
								result = result.substring(0,result.length()-len);
							String s = xmlContent.substring(prevIndex+1,index);
							if(s.contains("\r\n")){
								if(!s.trim().equals(""))
									result += s.replace("\r\n", " ");
							}
							else if(s.contains("\n")){
								if(!s.trim().equals(""))
									result += s.replace("\n", " ");
							}
							else
								result += s;

							prevIndex = 0;
						}
					}
				}
				else
					prevIndex = xmlContent.indexOf(">", index);

				result += section + "\n";
			}
		}
		catch(StringIndexOutOfBoundsException s) {
			s.printStackTrace();
			return "Invalid XML";
		}

		return result;
	}

	private static boolean isClosingPreviousBeginTag(String prevBeginSection, String currentEndSection){
		int pos = prevBeginSection.indexOf(' ');
		if(pos < 0)
			pos = prevBeginSection.length()-1;
		String s = "</" + prevBeginSection.substring(1,pos) + ">";
		return s.equalsIgnoreCase(currentEndSection);
	}

	/**
	 * Sets up the GWT uncaught exception handler.
	 *
	 */
	public static void setupUncaughtExceptionHandler(){

		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable throwable) {
				displayException(throwable);
			}
		});
	}

	/**
	 * Gets the parameters passed in the host html file as divs (preferably hidden divs).
	 * For now this is the way of passing parameters to the form designer and runtime widget.
	 */
	public static void retrieveUserDivParameters(){
		formDefDownloadUrlSuffix = getDivValue("formDefDownloadUrlSuffix");
		formDefUploadUrlSuffix = getDivValue("formDefUploadUrlSuffix");
		entityFormDefDownloadUrlSuffix = getDivValue("entityFormDefDownloadUrlSuffix");
		formDataUploadUrlSuffix = getDivValue("formDataUploadUrlSuffix");
		afterSubmitUrlSuffix = getDivValue("afterSubmitUrlSuffix");
		formDefRefreshUrlSuffix = getDivValue("formDefRefreshUrlSuffix");
		externalSourceUrlSuffix = getDivValue("externalSourceUrlSuffix");
		multimediaUrlSuffix = getDivValue("multimediaUrlSuffix");
		closeUrl = getDivValue("closeUrl");

		if(multimediaUrlSuffix == null || multimediaUrlSuffix.trim().length() == 0)
			multimediaUrlSuffix = "multimedia";

		formIdName = getDivValue("formIdName");
		if(formIdName == null || formIdName.trim().length() == 0)
			formIdName = "formId";

		entityIdName = getDivValue("entityIdName");
		if(entityIdName == null || entityIdName.trim().length() == 0)
			entityIdName = "patientId";

		formId = getDivValue(formIdName);
		entityId = getDivValue(entityIdName);

		String format = getDivValue("dateTimeSubmitFormat");
		if(format != null && format.trim().length() > 0)
			setDateTimeSubmitFormat(format);

		format = getDivValue("dateTimeDisplayFormat");
		if(format != null && format.trim().length() > 0)
			setDateTimeDisplayFormat(format);

		format = getDivValue("timeDisplayFormat");
		if(format != null && format.trim().length() > 0)
			setTimeDisplayFormat(format);

		format = getDivValue("timeSubmitFormat");
		if(format != null && format.trim().length() > 0)
			setTimeSubmitFormat(format);

		format = getDivValue("dateDisplayFormat");
		if(format != null && format.trim().length() > 0)
			setDateDisplayFormat(format);

		format = getDivValue("dateSubmitFormat");
		if(format != null && format.trim().length() > 0)
			setDateSubmitFormat(format);

		defaultFontFamily = getDivValue("defaultFontFamily");
		if(defaultFontFamily == null || defaultFontFamily.trim().length() == 0)
			defaultFontFamily = "Verdana, 'Lucida Grande', 'Trebuchet MS', Arial, Sans-Serif";

		String s = getDivValue("appendEntityIdAfterSubmit");
		if(s == null || s.trim().length() == 0)
			appendEntityIdAfterSubmit = false;
		else
			appendEntityIdAfterSubmit = !s.equals("0");

		s = getDivValue("showSubmitSuccessMsg");
		if("1".equals(s) || "true".equals(s))
			showSubmitSuccessMsg = true;

		/*s = getDivValue("showLanguageTab");
		if("1".equals(s) || "true".equals(s))
			showLanguageTab = true;*/
	}

	public static String getDivValue(String id){
		//RootPanel p = RootPanel.get(id);

		com.google.gwt.dom.client.Element p = com.google.gwt.dom.client.Document.get().getElementById(id);
		if(p != null){
			NodeList<Node> nodes = p.getChildNodes();
			if(nodes != null && nodes.getLength() > 0){
				Node node = nodes.getItem(0);
				String s = node.getNodeValue();
				p.removeChild(node);
				return s;
			}
		}

		return null;
	}

	public static void setDateTimeSubmitFormat(String format){
		dateTimeSubmitFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getDateTimeSubmitFormat(){
		return dateTimeSubmitFormat;
	}

	public static void setDateTimeDisplayFormat(String format){
		dateTimeDisplayFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getDateTimeDisplayFormat(){
		return dateTimeDisplayFormat;
	}

	public static void setTimeDisplayFormat(String format){
		timeDisplayFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getTimeDisplayFormat(){
		return timeDisplayFormat;
	}

	public static void setDateDisplayFormat(String format){
		dateDisplayFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getDateDisplayFormat(){
		return dateDisplayFormat;
	}

	public static void setTimeSubmitFormat(String format){
		timeSubmitFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getTimeSubmitFormat(){
		return timeSubmitFormat;
	}

	public static void setDateSubmitFormat(String format){
		dateSubmitFormat = DateTimeFormat.getFormat(format);
	}

	public static DateTimeFormat getDateSubmitFormat(){
		return dateSubmitFormat;
	}

	public static String getFormDefDownloadUrlSuffix(){
		return formDefDownloadUrlSuffix;
	}

	public static String getFormDefUploadUrlSuffix(){
		return formDefUploadUrlSuffix;
	}

	public static String getEntityFormDefDownloadUrlSuffix(){
		return entityFormDefDownloadUrlSuffix;
	}

	public static String getFormDataUploadUrlSuffix(){
		return formDataUploadUrlSuffix;
	}

	public static String getAfterSubmitUrlSuffix(){
		return afterSubmitUrlSuffix;
	}

	public static String getFormDefRefreshUrlSuffix(){
		return formDefRefreshUrlSuffix;
	}

	public static String getExternalSourceUrlSuffix(){
		return externalSourceUrlSuffix;
	}

	public static String getMultimediaUrlSuffix(){
		return getHostPageBaseURL()+ multimediaUrlSuffix;
	}

	public static String getCloseUrl(){
		return closeUrl;
	}

	public static String getFormIdName(){
		return formIdName;
	}

	public static String getEntityIdName(){
		return entityIdName;
	}

	public static String getFormId(){
		return formId;
	}

	public static String getEntityId(){
		return entityId;
	}

	/*public static boolean getShowLanguageTab(){
		return showLanguageTab;
	}*/

	public static String getHostPageBaseURL(){
		//return "http://127.0.0.1:8080/openmrs/";

		String s = GWT.getHostPageBaseURL();

		int pos = s.lastIndexOf(':');
		if(pos == -1)
			return s;

		pos = s.indexOf('/', pos+1);
		if(pos == -1)
			return s;

		pos = s.indexOf('/', pos+1);
		if(pos == -1)
			return s;

		return s.substring(0,pos+1);
	}

	public static String getDefaultFontFamily(){
		return defaultFontFamily;
	}

	public static boolean appendEntityIdAfterSubmit(){
		return appendEntityIdAfterSubmit;
	}

	public static boolean showSubmitSuccessMsg(){
		return showSubmitSuccessMsg;
	}

	/**
	 * Displays an exception to the user.
	 * 
	 * @param ex the exception to display.
	 */
	public static void displayException(Throwable ex){
		FormUtil.dlg.hide(); //TODO Some how when an exception is thrown, this may stay on. So needs a fix.

		ex.printStackTrace();

		String text = LocaleText.get("uncaughtException");
		String s = text;
		while (ex != null) {
			s = ex.getMessage();
			StackTraceElement[] stackTraceElements = ex.getStackTrace();
			text += ex.toString() + "\n";
			for (int i = 0; i < stackTraceElements.length; i++) {
				text += "    at " + stackTraceElements[i] + "\n";
			}
			ex = (Exception)ex.getCause();
			if (ex != null) {
				text += LocaleText.get("causedBy");
			}
		}

		//This check is a temporary workaround for firefox 3.5 which
		//throws this error on certain mouse moves which i have not
		//yet got the exact cause for.
		if(!(s != null && s.contains("(NS_ERROR_DOM_NOT_SUPPORTED_ERR):"))){
			ErrorDialog dialogBox = new ErrorDialog();
			dialogBox.setText(LocaleText.get("unexpectedFailure"));
			dialogBox.setErrorMessage(s);
			dialogBox.setCallStack(text);
			dialogBox.center();
		}
		//else
		//	Window.alert("Trapped");
	}

	/**
	 * Converts string dimension in say pixels to integer.
	 * 
	 * @param dimension the dimension text.
	 * @return the integer value.
	 */
	public static int convertDimensionToInt(String dimension){
		if(dimension == null || dimension.trim().length() == 0)
			return 0;

		try{
			return Integer.parseInt(dimension.substring(0,dimension.length()-2));
		}catch(Exception ex){}

		return 1;
	}

	public static String getNodePath(com.google.gwt.xml.client.Node node){
		String path = removePrefix(node.getNodeName());

		if(node.getNodeType() == Node.ELEMENT_NODE){
			com.google.gwt.xml.client.Node parent = node.getParentNode();
			while(parent != null && !(parent instanceof Document)){
				path = removePrefix(parent.getNodeName()) + "/" + path;
				parent = parent.getParentNode();
			}
		}

		return path;
	}

	/**
	 * Gets the xpath expression pointing to a node starting from a given parent node.
	 * 
	 * @param node the node whose xpath expression to get.
	 * @param parentNode the parent node.
	 * @return the xpath expression.
	 */
	public static String getNodePath(com.google.gwt.xml.client.Node node, com.google.gwt.xml.client.Node parentNode){
		String path = removePrefix(node.getNodeName());

		if(node.getNodeType() == Node.ELEMENT_NODE){
			com.google.gwt.xml.client.Node parent = node.getParentNode();
			while(parent != null && !parent.getNodeName().equals(parentNode.getNodeName()) && !(parent instanceof Document)){
				path = removePrefix(parent.getNodeName()) + "/" + path;
				parent = parent.getParentNode();
			}
		}

		return path;
	}

	/**
	 * Removes a namespace prefix from a name.
	 * 
	 * @param name the name. eg xf:instance
	 * @return the new name without the prefix. For the above the value would be instance.
	 */
	private static String removePrefix(String name){
		int pos = name.indexOf(':');
		if(pos >= 0)
			name = name.substring(pos + 1);
		return name;
	}

	/**
	 * Gets the name of a node.
	 * 
	 * @param node the node whose name to get.
	 * @return the node name.
	 */
	public static String getNodeName(Element node){
		return removePrefix(node.getNodeName());
	}

	/**
	 * Tells form runner or designer widget user that we are done displaying the widgets
	 * and hence they can do initialization stuff if they have any.
	 */
	public static native void initialize() /*-{
		return $wnd.initialize();
	}-*/;

	public static native void searchExternal(String key,String value,com.google.gwt.user.client.Element parentElement, com.google.gwt.user.client.Element textElement, com.google.gwt.user.client.Element valueElement) /*-{
		return $wnd.searchExternal(key,value,parentElement.parentNode.parentNode,textElement,valueElement);
	}-*/;

	/**
	 * Checks if the current used is authenticated by the server.
	 * This method is called every time a user tries to submit form data in non preview mode.
	 * 
	 * @return
	 */
	public static native boolean isAuthenticated() /*-{
		return $wnd.isUserAuthenticated();
	}-*/;

	/**
	 * Checks if a user is logged on the server.
	 * 
	 * @param username the user name.
	 * @param password the password.
	 * @return This return value if value is not used because we are getting the result via a
	 *         callback due to the asyncrounous nature of this call.
	 */
	public static native boolean authenticate(String username, String password) /*-{
		return $wnd.authenticateUser(username,password);
	}-*/;
	
	
	public static Image createImage(ImageResource resource){
		return AbstractImagePrototype.create(resource).createImage();
	}
}
