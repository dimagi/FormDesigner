package org.purc.purcforms.client.view;

import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.view.FormsTreeView.Images;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Contains the palette.
 * 
 * @author daniel
 *
 */
public class PaletteView extends Composite {

	  /**
	   * An image bundle for this widget and an example of the use of @gwt.resource.
	   */
	  public interface Images extends ImageBundle {
		/**
		 * ImageBundle.@resource org.purc.purcform.client.default_photo.jpg
		 */
	    AbstractImagePrototype defaultPhoto();
	  }
	  
	  private VerticalPanel verticalPanel = new VerticalPanel();
	  private ScrollPanel scrollPanel = new ScrollPanel();
	  private final Images images;
	  
	  public PaletteView(Images images) {
		  
		  this.images = images;
		    //PushButton b = new PushButton("input");
		    //verticalPanel.add(b);
		    
		    /*verticalPanel.add(new PushButton("secret"));
		    verticalPanel.add(new PushButton("select1"));
		    verticalPanel.add(new PushButton("select"));
		    verticalPanel.add(new PushButton("range"));
		    verticalPanel.add(new PushButton("group"));*/
		    
		  	HorizontalPanel hPanel = new HorizontalPanel();
			hPanel.setSpacing(0);
			hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			hPanel.add(images.defaultPhoto().createImage());
			hPanel.add(new PushButton("Label"));
			FormDesignerUtil.maximizeWidget(hPanel);
		  	verticalPanel.add(hPanel);
		  	
		  	
		  	verticalPanel.add(new PushButton("TextBox"));
		  	verticalPanel.add(new PushButton("CheckBox"));
		  	verticalPanel.add(new PushButton("RadioButton"));
		  	verticalPanel.add(new PushButton("Dropdown List"));
		  	verticalPanel.add(new PushButton("TextArea"));
		  	verticalPanel.add(new PushButton("Time Picker"));
		  	verticalPanel.add(new PushButton("Date Picker"));
		  	verticalPanel.add(new PushButton("Date Time Picker"));
		  	verticalPanel.add(new PushButton("Button"));
		  	verticalPanel.add(new PushButton("Picture"));
		  	verticalPanel.add(new PushButton("Repeat"));
		  	verticalPanel.add(new PushButton("Group Box"));
		  
		    verticalPanel.addStyleName("getting-started-blue");

		    scrollPanel.setWidget(verticalPanel);
		    
		    
		    
		    /*CheckBox checkbox = new CheckBox("Checkbox");
		    checkbox.addStyleName("getting-started-label");
		    dragController.makeDraggable(checkbox);
		    verticalPanel.add(checkbox);
		    
		    RadioButton radiobutton = new RadioButton("Radio Button");
		    radiobutton.addStyleName("getting-started-label");
		    dragController.makeDraggable(radiobutton);
		    verticalPanel.add(radiobutton);
		    
		    ListBox listbox = new ListBox(false);
		    listbox.addStyleName("getting-started-label");
		    dragController.makeDraggable(listbox);
		    verticalPanel.add(listbox);
		    
		    TextBox textbox = new TextBox();
		    textbox.addStyleName("getting-started-label");
		    dragController.makeDraggable(textbox);
		    verticalPanel.add(textbox);
		    
		    PushButton pushbutton = new PushButton("Button");
		    pushbutton.addStyleName("getting-started-label");
		    dragController.makeDraggable(pushbutton);
		    verticalPanel.add(pushbutton);*/
		      
		   // boundaryPanel.add(scrollPanel);
		   // FormsDesignerUtil.maximizeWidget(boundaryPanel);
		    
//		  Create a drop target on which we can drop labels
		    //AbsolutePanel targetPanel = new AbsolutePanel();
		    //targetPanel.setPixelSize(300, 200);
		    //targetPanel.addStyleName("getting-started-blue");
		    //targetPanel.setPixelSize(100, 100);
		    //verticalPanel.add(targetPanel);

		    //dropController = new AbsolutePositionDropController(targetPanel);
		    
		    //dragController.registerDropController(dropController);
		    
		    initWidget(scrollPanel);
		    
		    FormDesignerUtil.maximizeWidget(scrollPanel);
		    
		    setTitle("Form controls");
	}
}
