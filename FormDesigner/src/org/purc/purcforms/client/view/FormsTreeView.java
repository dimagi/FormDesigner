package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.purc.purcforms.client.Toolbar;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.OptionDef;
import org.purc.purcforms.client.model.PageDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.widget.CompositeTreeItem;
import org.purc.purcforms.client.widget.TreeItemWidget;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;


/**
 * Displays questions in a tree view.
 * 
 * @author daniel
 *
 */
public class FormsTreeView extends Composite implements TreeListener,IFormChangeListener,IFormActionListener{

	/**
	 * Specifies the images that will be bundled for this Composite and specify
	 * that tree's images should also be included in the same bundle.
	 */
	public interface Images extends Toolbar.Images, TreeImages {
		AbstractImagePrototype drafts();
		AbstractImagePrototype markRead();
		AbstractImagePrototype templates();
		AbstractImagePrototype note();
		AbstractImagePrototype lookup();
	}

	private ScrollPanel scrollPanel = new ScrollPanel();
	private Tree tree;
	private final Images images;
	private PopupPanel popup;
	private Object clipboardItem;
	private boolean inCutMode = false;
	private TreeItem item;
	private boolean showFormAsRoot;
	private FormDef formDef;
	private List<IFormSelectionListener> formSelectionListeners = new ArrayList<IFormSelectionListener>();

	private int nextFormId = 0;
	private int nextPageId = 0;
	private int nextQuestionId = 0;
	private int nextOptionId = 0;
	private IFormDesignerListener formDesignerListener;
	

	public FormsTreeView(Images images,IFormSelectionListener formSelectionListener) {

		this.images = images;
		this.formSelectionListeners.add(formSelectionListener);

		tree = new Tree(images);
		tree.ensureSelectedItemVisible();

		scrollPanel.setWidget(tree);
		initWidget(scrollPanel);
		FormDesignerUtil.maximizeWidget(scrollPanel);

		tree.addTreeListener(this);

		//This is just for solving a wiered behaviour when one changes a node text
		//and the click another node which gets the same text as the previously
		//selected text. Just comment it out and you will see what happens.
		tree.addMouseListener(new MouseListenerAdapter(){
			public void onMouseDown(Widget sender, int x, int y){
				tree.setSelectedItem(tree.getSelectedItem());
			}
		});

		initContextMenu();
	}

	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		this.formDesignerListener = formDesignerListener;
	}

	public void addFormSelectionListener(IFormSelectionListener formSelectionListener){
		this.formSelectionListeners.add(formSelectionListener);
	}

	public void showFormAsRoot(boolean showFormAsRoot){
		this.showFormAsRoot = showFormAsRoot;
	}

	private void initContextMenu(){
		popup = new PopupPanel(true,true);

		MenuBar menuBar = new MenuBar(true);
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),"Add New"),true, new Command(){
			public void execute() {popup.hide(); addNewItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),"Add Child"),true, new Command(){
			public void execute() {popup.hide(); addNewChildItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),"Delete"),true,new Command(){
			public void execute() {popup.hide(); deleteSelectedItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),"Move Up"),true, new Command(){
			public void execute() {popup.hide(); moveItemUp();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),"Move Down"),true, new Command(){
			public void execute() {popup.hide(); moveItemDown();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),"Cut"),true,new Command(){
			public void execute() {popup.hide(); cutItem();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),"Copy"),true,new Command(){
			public void execute() {popup.hide(); copyItem();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),"Paste"),true,new Command(){
			public void execute() {popup.hide(); pasteItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.save(),"Save"),true,new Command(){
			public void execute() {popup.hide(); /*saveItem()*/;}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),"Refresh"),true,new Command(){
			public void execute() {popup.hide(); refreshItem();}});

		popup.setWidget(menuBar);
	}


	/**
	 * A helper method to simplify adding tree items that have attached images.
	 * {@link #addImageItem(TreeItem, String) code}
	 * 
	 * @param root the tree item to which the new item will be added.
	 * @param title the text associated with this item.
	 */
	private TreeItem addImageItem(TreeItem root, String title,AbstractImagePrototype imageProto, Object userObj,String helpText) {
		TreeItem item = new CompositeTreeItem(new TreeItemWidget(imageProto, title,popup,this));
		item.setUserObject(userObj);
		item.setTitle(helpText);
		if(root != null)
			root.addItem(item);
		else
			tree.addItem(item);
		return item;
	}

	/**
	 * Generates HTML for a tree item with an attached icon.
	 * 
	 * @param imageUrl the url of the icon image
	 * @param title the title of the item
	 * @return the resultant HTML
	 */
	/*private String imageItemHTML(AbstractImagePrototype imageProto, String title) {
	    return "<span>" + imageProto.getHTML() + title + "</span>";
	  }*/


	public void onTreeItemSelected(TreeItem item) {
		
		//Should not call this more than once for the same selected item.
		if(item != this.item){
			fireFormItemSelected(item.getUserObject());
			this.item = item;
		}
	}

	private void fireFormItemSelected(Object formItem){
		for(int i=0; i<formSelectionListeners.size(); i++)
			formSelectionListeners.get(i).onFormItemSelected(formItem);
	}

	public void onTreeItemStateChanged(TreeItem item) {
		// TODO Auto-generated method stub

	}

	public void loadForm(FormDef formDef,boolean select){	
		this.formDef = formDef;

		//A temporary hack to ensure top level object is accessed.
		fireFormItemSelected(formDef);

		TreeItem formRoot = null;
		if(showFormAsRoot){
			formRoot = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
			formRoot.setUserObject(formDef);
			tree.addItem(formRoot);
		}

		for(int currentPageNo =0; currentPageNo<formDef.getPages().size(); currentPageNo++){
			TreeItem pageRoot = loadPage((PageDef)formDef.getPages().elementAt(currentPageNo),formRoot);

			//We expand only the first page.
			if(currentPageNo == 0)
				pageRoot.setState(true);    
		}

		if(select && formRoot != null){
			tree.setSelectedItem(formRoot);
			formRoot.setState(true);
		}

	}
	
	public void refreshForm(FormDef formDef){
		tree.clear();
		loadForm(formDef,true);
	}

	private TreeItem loadPage(PageDef pageDef,TreeItem formRoot){
		TreeItem pageRoot = addImageItem(formRoot, pageDef.getName(), images.drafts(),pageDef,null);
		loadQuestions(pageDef.getQuestions(),pageRoot);
		return pageRoot;
	}

	private void loadQuestions(Vector questions,TreeItem root){
		if(questions != null){
			for(int currentQtnNo=0; currentQtnNo<questions.size(); currentQtnNo++)
				loadQuestion((QuestionDef)questions.elementAt(currentQtnNo),root);
		}
	}

	private TreeItem loadQuestion(QuestionDef questionDef,TreeItem root){
		TreeItem questionRoot = addImageItem(root, questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			Vector options = questionDef.getOptions();
			for(int currentOptionNo=0; currentOptionNo < options.size(); currentOptionNo++){
				OptionDef optionDef = (OptionDef)options.elementAt(currentOptionNo);
				addImageItem(questionRoot, optionDef.getText(), images.markRead(),optionDef,null);
			}
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
			addImageItem(questionRoot, QuestionDef.TRUE_DISPLAY_VALUE, images.markRead(),null,null);
			addImageItem(questionRoot, QuestionDef.FALSE_DISPLAY_VALUE, images.markRead(),null,null);
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			loadQuestions(questionDef.getRepeatQtnsDef().getQuestions(),questionRoot);

		return questionRoot;
	}

	/**
	 * Deletes the selected question.
	 */
	public void deleteSelectedItem(){
		TreeItem item = tree.getSelectedItem();
		if(item == null){
			Window.alert("Please first select the item to delete");
			return;
		}

		if(!inCutMode && !Window.confirm("Do you really want to delete the selected item and all its children (if any) ?"))
			return;

		deleteItem(item);
	}

	private void deleteItem(TreeItem item){
		TreeItem parent = item.getParentItem();
		if(parent != null){
			int index = parent.getChildIndex(item);

			//If last item is the one selected, the select the previous, else the next.
			if(index == parent.getChildCount()-1)
				index -= 1;

			removeFormDefItem(item,parent);

			//Remove the selected item.
			item.remove();

			//If no more kids, then select the parent.
			if(parent.getChildCount() == 0)
				tree.setSelectedItem(parent);
			else
				tree.setSelectedItem(parent.getChild(index));
		}
		else{ //Must be the form root
			item.remove();

			if(tree.getItemCount() > 0)
				tree.setSelectedItem(tree.getItem(0));
		}

		if(tree.getSelectedItem() == null)
			fireFormItemSelected(null);
	}

	private void removeFormDefItem(TreeItem item, TreeItem parent){
		Object userObj = item.getUserObject();
		Object parentUserObj = parent.getUserObject();

		if(userObj instanceof QuestionDef){
			if(parentUserObj instanceof QuestionDef)
				((QuestionDef)parentUserObj).getRepeatQtnsDef().removeQuestion((QuestionDef)userObj);
			else
				((PageDef)parentUserObj).removeQuestion((QuestionDef)userObj);			
		}
		else if(userObj instanceof OptionDef){
			((QuestionDef)parentUserObj).removeOption((OptionDef)userObj);
		}
		else if(userObj instanceof PageDef)
			((FormDef)parentUserObj).removePage((PageDef)userObj);	
	}

	/**
	 * Adds a new item.
	 */
	public void addNewItem(){
		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item != null){
			Object userObj = item.getUserObject();
			if(userObj instanceof QuestionDef){
				int id = item.getParentItem().getChildCount()+1;
				QuestionDef questionDef = new QuestionDef(id,"Question"+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,item.getParentItem().getUserObject());
				item = addImageItem(item.getParentItem(), questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
				addFormDefItem(questionDef,item.getParentItem());
				tree.setSelectedItem(item);
			}
			else if(userObj instanceof OptionDef){
				int id = item.getParentItem().getChildCount()+1;
				OptionDef optionDef = new OptionDef(id,"Option"+id,"option"+id,(QuestionDef)item.getParentItem().getUserObject());
				item = addImageItem(item.getParentItem(), optionDef.getText(), images.markRead(),optionDef,null);
				addFormDefItem(optionDef,item.getParentItem());
				tree.setSelectedItem(item);	
			}
			else if(userObj instanceof PageDef){
				int id = item.getParentItem().getChildCount()+1;
				PageDef pageDef = new PageDef("Page"+id,id,null,(FormDef)item.getParentItem().getUserObject());
				item = addImageItem(item.getParentItem(), pageDef.getName(), images.drafts(),pageDef,null);
				addFormDefItem(pageDef,item.getParentItem());
				tree.setSelectedItem(item);
			}
			else if(userObj instanceof FormDef)
				addNewForm();
		}
		else
			addNewForm();
	}

	private void addFormDefItem(Object obj,TreeItem parentItem){
		Object parentUserObj = parentItem.getUserObject();
		if(parentUserObj instanceof QuestionDef){
			if(obj instanceof OptionDef)
				((QuestionDef)parentUserObj).addOption((OptionDef)obj);
			else
				((QuestionDef)parentUserObj).getRepeatQtnsDef().addQuestion((QuestionDef)obj);
		}
		else if(parentUserObj instanceof PageDef)
			((PageDef)parentUserObj).addQuestion((QuestionDef)obj);
		else if(parentUserObj instanceof FormDef)
			((FormDef)parentUserObj).addPage((PageDef)obj);

	}

	public void addNewForm(){
		int id = tree.getItemCount()+1;
		FormDef formDef = new FormDef(id,"New Form"+id, "newform"+id,null,null,null);
		TreeItem item = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
		item.setUserObject(formDef);
		tree.addItem(item);
		tree.setSelectedItem(item);
	}

	/**
	 * Adds a new child item.
	 */
	public void addNewChildItem(){
		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item == null)
			return;

		Object userObj = item.getUserObject();
		if(userObj instanceof PageDef || 
				(userObj instanceof QuestionDef && ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_REPEAT) ){

			int id = item.getChildCount()+1;
			QuestionDef questionDef = new QuestionDef(id,"Question"+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,userObj);
			item = addImageItem(item, questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
			addFormDefItem(questionDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
		}
		else if(userObj instanceof QuestionDef && 
				( ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
						((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_MULTIPLE ) ){

			int id = item.getChildCount()+1;
			OptionDef optionDef = new OptionDef(id,"Option"+id,"option"+id,(QuestionDef)userObj);
			item = addImageItem(item, optionDef.getText(), images.markRead(),optionDef,null);
			addFormDefItem(optionDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
		}
		else if(userObj instanceof FormDef){
			int id = item.getChildCount()+1;
			PageDef pageDef = new PageDef("Page"+id,id,null,(FormDef)userObj);
			item = addImageItem(item, pageDef.getName(), images.drafts(),pageDef,null);
			addFormDefItem(pageDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
		}
	}

	public void moveItemUp() {
		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item == null)
			return;

		TreeItem parent = item.getParentItem();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.getChildIndex(item);
		if(index == 0)
			return; //Can't move any further upwards.

		//move the item in the form object model.
		moveFormItemUp(item,parent);

		TreeItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		item.remove();

		while(parent.getChildCount() >= index){
			currentItem = parent.getChild(index-1);
			list.add(currentItem);
			currentItem.remove();
		}

		parent.addItem(item);
		for(int i=0; i<list.size(); i++)
			parent.addItem((TreeItem)list.get(i));

		tree.setSelectedItem(item);
	}

	private void moveFormItemUp(TreeItem item,TreeItem parent){
		Object userObj = item.getUserObject();
		Object parentObj = parent.getUserObject();

		//Normal question
		if(userObj instanceof QuestionDef && parentObj instanceof PageDef)
			((PageDef)parentObj).moveQuestionUp((QuestionDef)userObj);
		else if(userObj instanceof QuestionDef && parentObj instanceof QuestionDef)
			((QuestionDef)parentObj).getRepeatQtnsDef().moveQuestionUp((QuestionDef)userObj);
		else if(userObj instanceof PageDef)
			((FormDef)parentObj).movePageUp((PageDef)userObj);
		else if(userObj instanceof OptionDef)
			((QuestionDef)parentObj).moveOptionUp((OptionDef)userObj);
	}

	private void moveFormItemDown(TreeItem item,TreeItem parent){
		Object userObj = item.getUserObject();
		Object parentObj = parent.getUserObject();

		//Normal question
		if(userObj instanceof QuestionDef && parentObj instanceof PageDef)
			((PageDef)parentObj).moveQuestionDown((QuestionDef)userObj);
		else if(userObj instanceof QuestionDef && parentObj instanceof QuestionDef)
			((QuestionDef)parentObj).getRepeatQtnsDef().moveQuestionDown((QuestionDef)userObj);
		else if(userObj instanceof PageDef)
			((FormDef)parentObj).movePageDown((PageDef)userObj);
		else if(userObj instanceof OptionDef)
			((QuestionDef)parentObj).moveOptionDown((OptionDef)userObj);
	}

	public void moveItemDown(){
		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item == null)
			return;

		TreeItem parent = item.getParentItem();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.getChildIndex(item);
		if(index == count - 1)
			return; //Can't move any further downwards.

		//move the item in the form object model.
		moveFormItemDown(item,parent);

		TreeItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		item.remove();

		while(parent.getChildCount() > 0 && parent.getChildCount() > index){
			currentItem = parent.getChild(index);
			list.add(currentItem);
			currentItem.remove();
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1)
				parent.addItem(item); //Add after the first item.
			parent.addItem((TreeItem)list.get(i));
		}

		if(list.size() == 1)
			parent.addItem(item);

		tree.setSelectedItem(item);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormChangeListener#onFormItemChanged(java.lang.Object)
	 */
	public void onFormItemChanged(Object formItem) {
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return; //How can this happen?

		if(formItem instanceof QuestionDef){
			QuestionDef questionDef = (QuestionDef)formItem;
			//item.setHTML(FormsDesignerUtil.createHeaderHTML(images.lookup(), questionDef.getText()));
			//new TreeItemWidget(imageProto, title,popup)
			item.setWidget(new TreeItemWidget(images.lookup(), questionDef.getText(),popup,this));
			item.setTitle(questionDef.getHelpText());
		}
		else if(formItem instanceof OptionDef){
			OptionDef optionDef = (OptionDef)formItem;
			item.setWidget(new TreeItemWidget(images.markRead(), optionDef.getText(),popup,this));
		}
		else if(formItem instanceof PageDef){
			PageDef pageDef = (PageDef)formItem;
			item.setWidget(new TreeItemWidget(images.drafts(), pageDef.getName(),popup,this));
		}
		else if(formItem instanceof FormDef){
			FormDef formDef = (FormDef)formItem;
			item.setWidget(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
		}
	}

	public void onDeleteChildren(Object formItem){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return; //How can this happen?

		if(formItem instanceof QuestionDef){
			while(item.getChildCount() > 0)
				deleteItem(item.getChild(0));
		}
	}

	public void cutItem(){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		clipboardItem = item.getUserObject();  

		inCutMode = true;
		deleteSelectedItem();
		inCutMode = false;
	}

	public void copyItem() {
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		clipboardItem = item.getUserObject();
	}

	/**
	 * Paste clipboard item as a child of the selected item.
	 */
	public void pasteItem(){
		//Check if we have anything in the clipboard.
		if(clipboardItem == null)
			return;

		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		Object userObj = item.getUserObject();

		if(clipboardItem instanceof QuestionDef){
			//Questions can be pasted only as kids of pages or repeat questions.
			if(! ( (userObj instanceof PageDef) || 
					(userObj instanceof QuestionDef && 
							((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_REPEAT) 
			) 
			)
				return;

			//create a copy of the clipboard question.
			QuestionDef questionDef = new QuestionDef((QuestionDef)clipboardItem,userObj);

			//Repeat question can only be child of a page but not another question.
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && userObj instanceof QuestionDef)
				return;

			questionDef.setId(item.getChildCount()+1);

			if(userObj instanceof PageDef)
				((PageDef)userObj).addQuestion(questionDef);
			else
				((QuestionDef)userObj).getRepeatQtnsDef().addQuestion(questionDef);

			item = loadQuestion(questionDef, item);

			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
			item.setState(true);
		}
		else if(clipboardItem instanceof PageDef){		
			//Pages can be pasted only as kids of forms.
			if(!(userObj instanceof FormDef))
				return;

			//create a copy of the clipboard page.
			PageDef pageDef = new PageDef((PageDef)clipboardItem,(FormDef)userObj);

			pageDef.setPageNo(item.getChildCount()+1);
			((FormDef)userObj).addPage(pageDef);
			item = loadPage(pageDef, item);

			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
			item.setState(true);
		}
		else if(clipboardItem instanceof OptionDef){
			//Question options can be pasted only as kids of single and multi select questions.
			if(!(userObj instanceof QuestionDef 
					&& (((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)||
					((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE))
				return;

//			create a copy of the clipboard page.
			OptionDef optionDef = new OptionDef((OptionDef)clipboardItem,(QuestionDef)userObj);
			optionDef.setId(item.getChildCount()+1);
			((QuestionDef)userObj).addOption(optionDef);
			item = addImageItem(item, optionDef.getText(), images.markRead(),optionDef,null);

			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
			item.setState(true);
		}
	}

	public void refreshItem(){
		formDesignerListener.refresh(this);
	}

	public Object getSelectedForm(){
		TreeItem  item = tree.getSelectedItem();
		if(item != null)
			return getSelectedForm(item);
		return null;
	}

	private Object getSelectedForm(TreeItem item){
		Object obj = item.getUserObject();
		if(obj instanceof FormDef)
			return obj;
		return getSelectedForm(item.getParentItem());
	}
}
