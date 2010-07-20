package org.purc.purcforms.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.view.ErrorDialog;
import org.purc.purcforms.client.view.ProgressDialog;
import org.purc.purcforms.client.xforms.XformConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.Response;
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
	private static String fileOpenUrlSuffix;
	private static String fileSaveUrlSuffix;
	private static String gpsTypeName;
	private static String saveFormat;
	
	public static String JAVAROSA = "javarosa";
	

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

	/** The default font size, in pixels, used by the form designer. */
	private static String defaultFontSize;

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

				if( keyCode == '%' || keyCode == '&' || keyCode == '(')
					((TextBox) event.getSource()).cancelKey();

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

				if( keyCode == '%' || keyCode == '&' || keyCode == '(')
					((TextBox) event.getSource()).cancelKey();

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
		fileOpenUrlSuffix = getDivValue("fileOpenUrlSuffix");
		fileSaveUrlSuffix = getDivValue("fileSaveUrlSuffix");
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

		defaultFontSize = getDivValue("defaultFontSize");
		if(defaultFontSize == null || defaultFontSize.trim().length() == 0)
			defaultFontSize = "16";

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
		
		gpsTypeName = getDivValue("gpsTypeName");
		if(gpsTypeName == null || gpsTypeName.trim().length() == 0)
			gpsTypeName = XformConstants.DATA_TYPE_TEXT;
		
		s = getDivValue("formKeyAttributeName");
		if(s != null && s.trim().length() > 0)
			XformConstants.ATTRIBUTE_NAME_FORM_KEY = s;
		
		s = getDivValue("constraintMessageAttributeName");
		if(s != null && s.trim().length() > 0)
			XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE = s;
		
		saveFormat = getDivValue("saveFormat");
		
		if(JAVAROSA.equalsIgnoreCase(saveFormat)){
			gpsTypeName = "geopoint";
			XformConstants.ATTRIBUTE_NAME_FORM_KEY = "id";
			XformConstants.ATTRIBUTE_NAME_CONSTRAINT_MESSAGE = "jr:constraintMsg";
			XformConstants.DATA_TYPE_BINARY = "binary";
		}
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

	public static String getMultimediaUrl(){
		return getHostPageBaseURL()+ multimediaUrlSuffix;
	}

	public static String getFileOpenUrl(){
		return getHostPageBaseURL()+ fileOpenUrlSuffix;
	}

	public static String getFileSaveUrl(){
		return getHostPageBaseURL()+ fileSaveUrlSuffix;
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
	
	public static String getGpsTypeName(){
		return gpsTypeName;
	}
	
	public static String getSaveFormat(){
		return saveFormat;
	}
	
	public static boolean isJavaRosaSaveFormat(){
		return JAVAROSA.equalsIgnoreCase(saveFormat);
	}

	public static String getHostPageBaseURL(){
		//return "http://127.0.0.1:8080/openmrs/";
		//or http://dev.cell-life.org/openmrs/

		String s = GWT.getHostPageBaseURL();

		/*int pos = s.lastIndexOf(':');
		if(pos == -1)
			return s;

		pos = s.indexOf('/', pos+1);
		if(pos == -1)
			return s;

		pos = s.indexOf('/', pos+1);
		if(pos == -1)
			return s;

		return s.substring(0,pos+1);*/

		int pos = s.indexOf("//");
		if(pos == -1)
			return s;

		pos = s.indexOf('/', pos+2);
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

	public static String getDefaultFontSize(){
		return defaultFontSize + PurcConstants.UNITS;
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
		if(!(s != null && (s.contains("(NS_ERROR_DOM_NOT_SUPPORTED_ERR):") || s.contains("(Error): Permission denied for")))){
			ErrorDialog dialogBox = new ErrorDialog();
			dialogBox.setText(LocaleText.get("unexpectedFailure"));
			dialogBox.setErrorMessage(s);
			dialogBox.setCallStack(text);
			dialogBox.center();
		}//Window.prompt("Please enter the language", "Language");
		//else
		//	Window.alert("Trapped");
	}

	public static void displayReponseError(Response response){
		dlg.hide();

		ErrorDialog dialogBox = new ErrorDialog();
		dialogBox.setText(LocaleText.get("unexpectedFailure"));

		String errorMessage = response.getHeader("PURCFORMS-ERROR-MESSAGE");
		if(errorMessage == null || errorMessage.trim().length() == 0)
			errorMessage = response.getStatusText();

		dialogBox.setErrorMessage(errorMessage);
		String stackTrace = "NO STACK TRACE";
		if(response.getText() != null && response.getText().trim().length() > 0)
			stackTrace = response.getText().trim();
		dialogBox.setCallStack(stackTrace);
		dialogBox.center();
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
				
				String value = ((Element)parent).getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
				if(value != null)
					value = "[@id='" + value + "']";
				
				if(value == null){
					value = ((Element)parent).getAttribute(XformConstants.ATTRIBUTE_NAME_BIND);
					if(value != null)
						value = "[@bind='" + value + "']";
				}
				
				if(value == null){
					value = ((Element)parent).getAttribute(XformConstants.ATTRIBUTE_NAME_REF);
					if(value != null)
						value = "[@ref='" + value + "']";
				}
				
				path = removePrefix(parent.getNodeName()) + (value == null ? "" : value) + "/" + path;
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
			while(parent != null && !(parent instanceof Document)){
				if(parent.getNodeName().equals(parentNode.getNodeName())){
					if(parent.toString().equals(parentNode.toString()))
						break;
				}

				String tempPath = "";
				String id = ((Element)parent).getAttribute(XformConstants.ATTRIBUTE_NAME_ID);
				if(id != null && id.trim().length() > 0)
					tempPath = "[@id='" + id + "']";

				path = removePrefix(parent.getNodeName()) + tempPath + "/" + path;
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

	public static native void searchExternal(String key,String value,com.google.gwt.user.client.Element parentElement, com.google.gwt.user.client.Element textElement, com.google.gwt.user.client.Element valueElement, String filterField) /*-{
		return $wnd.searchExternal(key,value,parentElement.parentNode.parentNode,textElement,valueElement,filterField);
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

	/**
	 * Evaluate scripts in an HTML string. Will eval both <script src=""></script>
	 * and <script>javascript here</scripts>.
	 *
	 * @param element a new HTML(text).getElement() e.g evalScripts(new HTML(response.getText()).getElement())
	 */
	public static native void evalScripts(com.google.gwt.user.client.Element element) /*-{
        var scripts = element.getElementsByTagName("script");

        for (i=0; i < scripts.length; i++) {
            // if src, eval it, otherwise eval the body
            if (scripts[i].hasAttribute("src")) {
                var src = scripts[i].getAttribute("src");
                var script = $doc.createElement('script');
                script.setAttribute("src", src);
                $doc.getElementsByTagName('body')[0].appendChild(script);
            } else {
                $wnd.eval(scripts[i].innerHTML);
            }
        }
    }-*/;


	public static native String getElementValue(com.google.gwt.user.client.Element element) /*-{
    	return element.value;
    }-*/;

	public static native void setElementValue(com.google.gwt.user.client.Element element, String value1, String value2) /*-{
		//element.value = value;
		//element.onchange=function(){alert('yo men')};
		//element.addEventListener('change',function(){alert(Math.round(0.60))},false)
		//element.addEventListener('change',function(){alert(Math.round((new Date().getTime() - value.getTime())/365.25));},false)
		var val = Math.round(((Date.parse(value1) - Date.parse(value2))/86400000)/365.25);
		//alert(val);
		element.value = val;

		if(document.createEvent){
			// dispatch for firefox + others
			var evt = document.createEvent('HTMLEvents');
			evt.initEvent('change', true, true ); // event type,bubbling,cancelable
			element.dispatchEvent(evt);
		}
		else{
			// dispatch for IE
			var evt = document.createEventObject();
			element.fireEvent('onchange',evt)
		}

	}-*/;

	public static native void fireChangeEvent(com.google.gwt.user.client.Element element) /*-{
    	if(document.createEvent){
			// dispatch for firefox + others
			var evt = document.createEvent('HTMLEvents');
			evt.initEvent('change', true, true ); // event type,bubbling,cancelable
			element.dispatchEvent(evt);
		}
		else{
			// dispatch for IE
			var evt = document.createEventObject();
			element.fireEvent('onchange',evt)
		}
    }-*/;


	public static native int evaluateIntExpression(String expression) /*-{
	    return eval(expression);
	}-*/;

	public static native double evaluateDoubleExpression(String expression) /*-{
    	return eval(expression);
	}-*/;

	public static native String evaluateStringExpression(String expression) /*-{
    	return eval(expression);
	}-*/;


	public static void setElementFontSizeAndFamily(com.google.gwt.user.client.Element element){
		try{
			DOM.setStyleAttribute(element, "fontFamily", getDefaultFontFamily());
			DOM.setStyleAttribute(element, "fontSize", getDefaultFontSize());
		}catch(Exception ex){}
	}

	public static boolean isNumeric(String value){
		try{
			Integer.parseInt(value);
			return true;
		}
		catch(Exception ex){}

		return false;
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
