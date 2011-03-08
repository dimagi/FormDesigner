package org.purc.purcforms.client.view;

import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.controller.OpenFileDialogEventListener;
import org.purc.purcforms.client.controller.QuestionChangeListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.RuntimeWidgetWrapper;

import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class RuntimeGroupView extends Composite implements OpenFileDialogEventListener,QuestionChangeListener{

	public interface Images extends ClientBundle {
		ImageResource error();
	}

	/** Images reference where we get the error icon for widgets with errors. */
	protected final Images images;
	
	/** Reference to the form definition. */
	protected FormDef formDef;
	
	/** The currently selected tab panel. */
	protected AbsolutePanel selectedPanel;
	
	protected Image image;
	protected HTML html;
	
	protected HashMap<QuestionDef,List<Widget>> labelMap;
	protected HashMap<Widget,String> labelText;
	protected HashMap<Widget,String> labelReplaceText;

	protected HashMap<QuestionDef,List<CheckBox>> checkBoxGroupMap;
	
	protected HashMap<String,RuntimeWidgetWrapper> widgetMap;
	
	/** 
	 * The first invalid widget. This is used when we validate more than one widget in a group
	 * and at the end of the list we want to set focus to the first widget that we found invalid.
	 */
	protected RuntimeWidgetWrapper firstInvalidWidget;
	
	
	public RuntimeGroupView(Images images){
		this.images = images;
	}
	
	public void onSetFileContents(String contents) {
		if(contents != null && contents.trim().length() > 0){
			contents = contents.replace("<pre>", "");
			contents = contents.replace("</pre>", "");
			RuntimeWidgetWrapper widgetWrapper = null;

			if(image != null)
				widgetWrapper = (RuntimeWidgetWrapper)image.getParent().getParent();
			else
				widgetWrapper = (RuntimeWidgetWrapper)html.getParent().getParent();

			String xpath = widgetWrapper.getBinding();
			if(!xpath.startsWith(formDef.getBinding()))
				xpath = "/" + formDef.getBinding() + "/" + widgetWrapper.getBinding();

			if(image != null)
				image.setUrl(FormUtil.getMultimediaUrl()+"?action=recentbinary&time="+ new java.util.Date().getTime()+"&formId="+formDef.getId()+"&xpath="+xpath);
			else{
				String extension = "";//.3gp ".mpeg";
				String contentType = "&contentType=video/3gpp";
				if(widgetWrapper.getQuestionDef().getDataType() == QuestionDef.QTN_TYPE_AUDIO)
					contentType = "&contentType=audio/3gpp"; //"&contentType=audio/x-wav";
				//extension = ".wav";

				contentType += "&name="+widgetWrapper.getQuestionDef().getBinding()+".3gp";

				html.setVisible(true);
				html.setHTML("<a href=" + URL.encode(FormUtil.getMultimediaUrl()+extension + "?formId="+formDef.getId()+"&xpath="+xpath+contentType+"&time="+ new java.util.Date().getTime()) + ">"+html.getText()+"</a>");				
			}

			widgetWrapper.getQuestionDef().setAnswer(contents);
		}
	}
	
	
	public void onEnabledChanged(QuestionDef sender,boolean enabled){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;

		for(CheckBox checkBox : list){
			checkBox.setEnabled(enabled);
			if(!enabled)
				checkBox.setValue(false);
		}
	}

	public void onVisibleChanged(QuestionDef sender,boolean visible){
		List<CheckBox> list = checkBoxGroupMap.get(sender);
		if(list == null)
			return;

		for(CheckBox checkBox : list){
			checkBox.setVisible(visible);
			if(!visible)
				checkBox.setValue(false);
		}
	}

	public void onRequiredChanged(QuestionDef sender,boolean required){

	}

	public void onLockedChanged(QuestionDef sender,boolean locked){

	}

	public void onBindingChanged(QuestionDef sender,String newValue){

	}

	public void onDataTypeChanged(QuestionDef sender,int dataType){

	}

	public void onOptionsChanged(QuestionDef sender,List<OptionDef> optionList){

	}

}
