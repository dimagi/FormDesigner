package org.purc.purcforms.client;

import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormDesignerUtil;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ChangeListener;
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
 * This widget is the main tool bar for the form designer.
 * 
 * @author daniel
 *
 */
public class Toolbar extends Composite{

	/**
	 * Tool bar images.
	 */
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
	 
	/** Main widget for this tool bar. */
	private HorizontalPanel panel = new HorizontalPanel();
	
	/** The tool bar buttons. */
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
	private PushButton btnCut;
	private PushButton btnCopy;
	private PushButton btnPaste;
	private PushButton btnRefresh;
	
	/** Widget for separating tool bar buttons from each other. */
	private Label separatorWidget = new Label("  ");
	
	/** Widget to display the list of languages or locales. */
	private ListBox cbLanguages = new ListBox(false);
	
	/** The images for the tool bar icons. */
	private final Images images;
	
	/** Listener to the tool bar button click events. */
	private IFormDesignerListener controller;
	
	
	/**
	 * Creates a new instance of the tool bar.
	 * 
	 * @param images the images for tool bar icons.
	 * @param controller listener to the tool bar button click events.
	 */
	public Toolbar(Images images,IFormDesignerListener controller){
		this.images = images;
		this.controller = controller;
		setupToolbar();
		setupClickListeners();
		initWidget(panel);
	}
	
	/**
	 * Sets up the tool bar.
	 */
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
		
		btnNewForm.setTitle(LocaleText.get("newForm"));
		btnSaveForm.setTitle(LocaleText.get("save"));
		
		btnAddNewItem.setTitle(LocaleText.get("addNew"));
		btnAddNewChildItem.setTitle(LocaleText.get("addNewChild"));
		btnDeleteItem.setTitle(LocaleText.get("deleteSelected"));
		btnMoveItemUp.setTitle(LocaleText.get("moveUp"));
		btnMoveItemDown.setTitle(LocaleText.get("moveDown"));
		
		btnCut.setTitle(LocaleText.get("cut"));
		btnCopy.setTitle(LocaleText.get("copy"));
		btnPaste.setTitle(LocaleText.get("paste"));
		btnRefresh.setTitle(LocaleText.get("refresh"));
		
		btnAlignLeft.setTitle(LocaleText.get("alignLeft"));
		btnAlignRight.setTitle(LocaleText.get("alignRight"));
		btnAlignTop.setTitle(LocaleText.get("alignTop"));
		btnAlignBottom.setTitle(LocaleText.get("alignBottom"));
		btnSameWidth.setTitle(LocaleText.get("makeSameWidth"));
		btnSameHeight.setTitle(LocaleText.get("makeSameHeight"));
		btnSameSize.setTitle(LocaleText.get("makeSameSize"));
		
		panel.add(btnNewForm);
		panel.add(btnOpenForm);
		panel.add(btnSaveForm);
		
		panel.add(separatorWidget);
		
		panel.add(btnAddNewItem);
		panel.add(btnAddNewChildItem);
		panel.add(btnDeleteItem);
		panel.add(separatorWidget);
		panel.add(btnMoveItemUp);
		panel.add(btnMoveItemDown);
		
		panel.add(separatorWidget);
		panel.add(btnCut);
		panel.add(btnCopy);
		panel.add(btnPaste);
		
		panel.add(separatorWidget);
		panel.add(btnRefresh);
		
		panel.add(separatorWidget);
		panel.add(btnAlignLeft);
		panel.add(btnAlignRight);
		panel.add(btnAlignTop);
		panel.add(btnAlignBottom);
		
		panel.add(separatorWidget);
		panel.add(btnSameWidth);
		panel.add(btnSameHeight);
		panel.add(btnSameSize);
		
		Label label = new Label(FormDesignerUtil.getTitle());
		panel.add(label);
		panel.setCellWidth(label,"100%");
		panel.setCellHorizontalAlignment(label,HasHorizontalAlignment.ALIGN_CENTER);
		
		label = new Label(LocaleText.get("language"));
		panel.add(label);
		panel.setCellHorizontalAlignment(label,HasHorizontalAlignment.ALIGN_RIGHT);
		
		//TODO These need not be hard coded. They could come from the html host file.
		cbLanguages.addItem("English","en");
		cbLanguages.addItem("Luganda","lug");
		cbLanguages.addItem("Swahili","swa");
		cbLanguages.addItem("Lusoga","lus");
		cbLanguages.addItem("Runyankole","rny");
		cbLanguages.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender){
				controller.changeLocale(((ListBox)sender).getValue(((ListBox)sender).getSelectedIndex()));
			}
		});
		
		panel.add(cbLanguages);
		panel.setCellHorizontalAlignment(cbLanguages,HasHorizontalAlignment.ALIGN_RIGHT);
		
		//Set a 3 pixels spacing between tool bar buttons.
		panel.setSpacing(3);
	}
	
	/**
	 * Setup button click event handlers.
	 */
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
		
		btnRefresh.addClickListener(new ClickListener(){
			public void onClick(Widget widget){controller.refresh(this);}});
	}
}
