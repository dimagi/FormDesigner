package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Creates the main toolbar for the form designer.
 * 
 * @author daniel
 *
 */
public class Toolbar extends Composite{

	public interface Images extends ImageBundle {
		AbstractImagePrototype newform();
		AbstractImagePrototype open();
		AbstractImagePrototype save();
		AbstractImagePrototype moveup();
		AbstractImagePrototype movedown();
		AbstractImagePrototype add();
		AbstractImagePrototype addchild();
		AbstractImagePrototype delete();
		AbstractImagePrototype justifyleft();
		AbstractImagePrototype justifyright();
		AbstractImagePrototype cut();
		AbstractImagePrototype copy();
		AbstractImagePrototype paste();
		AbstractImagePrototype loading();
		AbstractImagePrototype alignTop();
		AbstractImagePrototype alignBottom();
		AbstractImagePrototype samewidth();
		AbstractImagePrototype sameheight();
		AbstractImagePrototype samesize();
	}
	    
	private HorizontalPanel panel = new HorizontalPanel();
	
	private PushButton btnAddNewItem;
	private PushButton btnAddNewChildItem;
	private PushButton btnDeleteItem;
	private PushButton btnMoveItemUp;
	private PushButton btnMoveItemDown;
	private PushButton btnNewForm;
	private PushButton btnOpenForm;
	private PushButton btnSaveForm;
	private PushButton btnAlignLeft;
	private PushButton btnAlignRight;
	private PushButton btnAlignTop;
	private PushButton btnAlignBottom;
	private PushButton btnSameWidth;
	private PushButton btnSameHeight;
	private PushButton btnSameSize;
	private PushButton btnOptions;
	private PushButton btnLanguages;
	private PushButton btnAboutInfo;
	private PushButton btnHelpContents;
	private PushButton btnCut;
	private PushButton btnCopy;
	private PushButton btnPaste;
	private PushButton btnRefresh;
	
	private String separatorText = "  ";
	
	private ListBox cbLanguanges = new ListBox(false);
	
	private final Images images;
	private IFormDesignerListener controller;
	
	public Toolbar(Images images,IFormDesignerListener controller){
		this.images = images;
		this.controller = controller;
		setupToolbar();
		setupClickListeners();
		initWidget(panel);
	}
	
	private void setupToolbar(){
		btnNewForm = new PushButton(images.newform().createImage());
		btnOpenForm = new PushButton(images.open().createImage());
		btnSaveForm = new PushButton(images.save().createImage());
		
		btnAddNewItem = new PushButton(images.add().createImage());
		btnAddNewChildItem = new PushButton(images.addchild().createImage());
		btnDeleteItem = new PushButton(images.delete().createImage());
		btnMoveItemUp = new PushButton(images.moveup().createImage());
		btnMoveItemDown = new PushButton(images.movedown().createImage());
		
		btnAlignLeft = new PushButton(images.justifyleft().createImage());
		btnAlignRight = new PushButton(images.justifyright().createImage());
		btnAlignTop = new PushButton(images.alignTop().createImage());
		btnAlignBottom = new PushButton(images.alignBottom().createImage());
		btnSameWidth = new PushButton(images.samewidth().createImage());
		btnSameHeight = new PushButton(images.sameheight().createImage());
		btnSameSize = new PushButton(images.samesize().createImage());
		
		btnCut = new PushButton(images.cut().createImage());
		btnCopy = new PushButton(images.copy().createImage());
		btnPaste = new PushButton(images.paste().createImage());
		btnRefresh = new PushButton(images.loading().createImage());
		
		btnNewForm.setTitle("New Form");
		btnOpenForm.setTitle("Open form from Xforms Source Tab");
		btnSaveForm.setTitle("Save Form");
		
		btnAddNewItem.setTitle("Add New");
		btnAddNewChildItem.setTitle("Add New Child");
		btnDeleteItem.setTitle("Delete Selected");
		btnMoveItemUp.setTitle("Move Up");
		btnMoveItemDown.setTitle("Move Down");
		
		btnCut.setTitle("Cut");
		btnCopy.setTitle("Copy");
		btnPaste.setTitle("Paste");
		btnRefresh.setTitle("Refresh");
		
		btnAlignLeft.setTitle("Align Left");
		btnAlignRight.setTitle("Align Right");
		btnAlignTop.setTitle("Align Top");
		btnAlignBottom.setTitle("Align Bottom");
		btnSameWidth.setTitle("Make Same Width");
		btnSameHeight.setTitle("Make Same Height");
		btnSameSize.setTitle("Make Same Size");
		
		panel.add(btnNewForm);
		panel.add(btnOpenForm);
		panel.add(btnSaveForm);
		
		panel.add(new Label(separatorText));
		
		panel.add(btnAddNewItem);
		panel.add(btnAddNewChildItem);
		panel.add(btnDeleteItem);
		panel.add(new Label(separatorText));
		panel.add(btnMoveItemUp);
		panel.add(btnMoveItemDown);
		
		panel.add(new Label(separatorText));
		panel.add(btnCut);
		panel.add(btnCopy);
		panel.add(btnPaste);
		
		panel.add(new Label(separatorText));
		panel.add(btnRefresh);
		
		panel.add(new Label(separatorText));
		panel.add(btnAlignLeft);
		panel.add(btnAlignRight);
		panel.add(btnAlignTop);
		panel.add(btnAlignBottom);
		
		panel.add(new Label(separatorText));
		panel.add(btnSameWidth);
		panel.add(btnSameHeight);
		panel.add(btnSameSize);
		
		//panel.add(new PushButton(images.loading().createImage()));
		
		//Just to push the languages list to the extreme right
		Label l = new Label(FormDesignerUtil.getTitle());
		panel.add(l);
		panel.setCellWidth(l,"100%");
		panel.setCellHorizontalAlignment(l,HasHorizontalAlignment.ALIGN_CENTER);
		
		Label label = new Label("Language:");
		panel.add(label);
		panel.setCellHorizontalAlignment(label,HasHorizontalAlignment.ALIGN_RIGHT);
		
		cbLanguanges.addItem("English");
		cbLanguanges.addItem("Luganda");
		cbLanguanges.addItem("Swahili");
		cbLanguanges.addItem("Lusoga");
		cbLanguanges.addItem("Runyankole");
		panel.add(cbLanguanges);
		panel.setCellHorizontalAlignment(cbLanguanges,HasHorizontalAlignment.ALIGN_RIGHT);
		
		panel.setSpacing(3);
	}
	
	private void setupClickListeners(){
		btnNewForm.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.newForm();}});
		
		btnOpenForm.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.openForm();}});
		
		btnSaveForm.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.saveForm();}});
		
		btnAddNewItem.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.addNewItem();}});
		
		btnAddNewChildItem.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.addNewChildItem();}});
		
		btnDeleteItem.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.deleteSelectedItem();}});
		
		btnMoveItemUp.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.moveItemUp();}});
		
		btnMoveItemDown.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.moveItemDown();}});
		
		btnAlignLeft.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.alignLeft();}});
		
		btnAlignRight.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.alignRight();}});
		
		btnAlignTop.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.alignTop();}});
		
		btnAlignBottom.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.alignBottom();}});
		
		btnCut.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.cutItem();}});
		
		btnCopy.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.copyItem();}});
		
		btnPaste.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.pasteItem();}});
		
		btnSameWidth.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.makeSameWidth();}});
		
		btnSameHeight.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.makeSameHeight();}});
		
		btnSameSize.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.makeSameSize();}});
		
		/*btnHelpContents.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.showHelpContents();}});*/
	}
}
