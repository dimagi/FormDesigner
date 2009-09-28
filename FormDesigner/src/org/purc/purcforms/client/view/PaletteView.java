package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.view.FormsTreeView.Images;
import org.purc.purcforms.client.widget.PaletteWidget;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Contains the palette where the user can pick wdigets and drag drop
 *  them onto the design surface.
 * 
 * @author daniel
 *
 */
public class PaletteView extends Composite {

	private VerticalPanel verticalPanel = new VerticalPanel();
	private ScrollPanel scrollPanel = new ScrollPanel();
	private final Images images;
	
	private static FormDesignerDragController dragController;

	public PaletteView(Images images) {

		this.images = images;

		if(dragController == null)
			initDnd();
		
		verticalPanel.setSpacing(10);
		verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("label"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("textBox"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("checkBox"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("radioButton"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("listBox"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("textArea"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("button"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("datePicker"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("groupBox"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("repeatSection"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("picture"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("videoAudio"))));
		
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("searchServer"))));

		scrollPanel.setWidget(verticalPanel);

		initWidget(scrollPanel);
		FormDesignerUtil.maximizeWidget(scrollPanel);
	}
	
	private static void initDnd(){
		dragController = new FormDesignerDragController(RootPanel.get(), false,null);
		dragController.setBehaviorMultipleSelection(false);
		//dragController.setBehaviorCancelDocumentSelections(true);
	}

	private PaletteWidget createPaletteWidget(HTML html){
		PaletteWidget widget = new PaletteWidget(images,html);
		dragController.makeDraggable(widget);
		return widget;
	}
	
	public static void registerDropController(DropController dropController) {
		if(dragController == null)
			initDnd();
		dragController.registerDropController(dropController);
	}
	
	public static void unRegisterDropController(DropController dropController){
		dragController.unregisterDropController(dropController);
	}
	
	public static void unRegisterAllDropControllers(){
		dragController.unregisterAllDropControllers();
	}
}
