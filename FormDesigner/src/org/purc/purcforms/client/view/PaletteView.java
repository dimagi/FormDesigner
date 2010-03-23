package org.purc.purcforms.client.view;

import org.purc.purcforms.client.controller.FormDesignerDragController;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormsTreeView.Images;
import org.purc.purcforms.client.widget.PaletteWidget;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Contains the palette where the user can pick widgets and drag drop
 *  them onto the design surface.
 * 
 * @author daniel
 *
 */
public class PaletteView extends Composite {

	/** The panel to contain the palette widgets. */
	private VerticalPanel verticalPanel = new VerticalPanel();
	
	/** The palette images. */
	private final Images images;
	
	/** The DND drag controller. */
	private static FormDesignerDragController dragController;

	
	/**
	 * Creates a new instance of the palette.
	 * 
	 * @param images the palette images.
	 */
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
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("dateTimeWidget"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("timeWidget"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("groupBox"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("repeatSection"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("picture"))));
		verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("videoAudio"))));
		
		//verticalPanel.add(createPaletteWidget(new HTML(LocaleText.get("searchServer"))));

		initWidget(verticalPanel);
		FormUtil.maximizeWidget(verticalPanel);
	}
	
	/**
	 * Sets up the DND drag controller.
	 */
	private static void initDnd(){
		dragController = new FormDesignerDragController(RootPanel.get(), false,null);
		dragController.setBehaviorMultipleSelection(false);
	}

	/**
	 * Creates a new palette widget.
	 * 
	 * @param html the name of the widget.
	 * @return the new palette widget.
	 */
	private PaletteWidget createPaletteWidget(HTML html){
		PaletteWidget widget = new PaletteWidget(images,html);
		dragController.makeDraggable(widget);
		return widget;
	}
	
	/**
	 * Registers a drop controller for widgets from this palette.
	 * 
	 * @param dropController the drop controller to register.
	 */
	public static void registerDropController(DropController dropController) {
		if(dragController == null)
			initDnd();
		dragController.registerDropController(dropController);
	}
	
	/**
	 * Removes a previously registered drop controller.
	 * 
	 * @param dropController the drop controller to un register.
	 */
	public static void unRegisterDropController(DropController dropController){
		dragController.unregisterDropController(dropController);
	}
	
	/**
	 * Removes all registered drop controllers.
	 */
	public static void unRegisterAllDropControllers(){
		dragController.unregisterAllDropControllers();
	}
}
