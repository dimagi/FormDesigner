package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.Toolbar;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ModelConstants;
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

		scrollPanel.setWidget(tree);
		initWidget(scrollPanel);
		FormDesignerUtil.maximizeWidget(scrollPanel);

		tree.addTreeListener(this);
		tree.ensureSelectedItemVisible();

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
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.add(),LocaleText.get("addNew")),true, new Command(){
			public void execute() {popup.hide(); addNewItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.addchild(),LocaleText.get("addNewChild")),true, new Command(){
			public void execute() {popup.hide(); addNewChildItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.delete(),LocaleText.get("deleteItem")),true,new Command(){
			public void execute() {popup.hide(); deleteSelectedItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.moveup(),LocaleText.get("moveUp")),true, new Command(){
			public void execute() {popup.hide(); moveItemUp();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.movedown(),LocaleText.get("moveDown")),true, new Command(){
			public void execute() {popup.hide(); moveItemDown();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.cut(),LocaleText.get("cut")),true,new Command(){
			public void execute() {popup.hide(); cutItem();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.copy(),LocaleText.get("copy")),true,new Command(){
			public void execute() {popup.hide(); copyItem();}});

		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.paste(),LocaleText.get("paste")),true,new Command(){
			public void execute() {popup.hide(); pasteItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.save(),LocaleText.get("save")),true,new Command(){
			public void execute() {popup.hide(); saveItem();}});

		menuBar.addSeparator();		  
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.loading(),LocaleText.get("refresh")),true,new Command(){
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
			Context.setFormDef(FormDef.getFormDef(item.getUserObject()));
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

	public void loadForm(FormDef formDef,boolean select, boolean langRefresh){
		if(formDef.getId() == ModelConstants.NULL_ID)
			formDef.setId(++nextFormId);

		if(!langRefresh){
			int count = formDef.getQuestionCount();
			if(nextQuestionId <= count)
				nextQuestionId = count;

			this.formDef = formDef;

			if(formExists(formDef.getId()))
				return;

			//A temporary hack to ensure top level object is accessed.
			fireFormItemSelected(formDef);
		}

		TreeItem formRoot = null;
		if(showFormAsRoot){
			formRoot = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
			formRoot.setUserObject(formDef);
			tree.addItem(formRoot);
		}

		if(formDef.getPages() != null){
			for(int currentPageNo =0; currentPageNo<formDef.getPages().size(); currentPageNo++){
				TreeItem pageRoot = loadPage((PageDef)formDef.getPages().elementAt(currentPageNo),formRoot);

				//We expand only the first page.
				if(currentPageNo == 0)
					pageRoot.setState(true);    
			}
		}

		if(select && formRoot != null){
			tree.setSelectedItem(formRoot);
			formRoot.setState(true);
		}

	}

	public boolean formExists(int formId){
		int count = tree.getItemCount();
		for(int index = 0; index < count; index++){
			TreeItem item = tree.getItem(index);
			if(((FormDef)item.getUserObject()).getId() == formId){
				tree.setSelectedItem(item);
				return true;
			}
		}

		return false;
	}

	public void refreshForm(FormDef formDef){
		//tree.clear();
		TreeItem item = tree.getSelectedItem();
		if(item != null){
			TreeItem root = getSelectedItemRoot(item);
			formDef.setId(((FormDef)root.getUserObject()).getId());

			tree.removeItem(root);
		}

		loadForm(formDef,true,false);
	}

	public List<FormDef> getForms(){
		List<FormDef> forms = new ArrayList<FormDef>();

		int count = tree.getItemCount();
		for(int index = 0; index < count; index++)
			forms.add((FormDef)tree.getItem(index).getUserObject());

		return forms;
	}

	public void loadForms(List<FormDef> forms, int selFormId){
		if(forms == null || forms.size() == 0)
			return;

		tree.clear();
		this.formDef = null;

		for(FormDef formDef : forms){
			loadForm(formDef,formDef.getId() == selFormId,true);

			if(formDef.getId() == selFormId){
				this.formDef = formDef;
				//A temporary hack to ensure top level object is accessed.
				fireFormItemSelected(this.formDef);
			}
		}
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
		TreeItem questionRoot = addImageItem(root, questionDef.getDisplayText(), images.lookup(),questionDef,questionDef.getHelpText());

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			List options = questionDef.getOptions();
			for(int currentOptionNo=0; currentOptionNo < options.size(); currentOptionNo++){
				OptionDef optionDef = (OptionDef)options.get(currentOptionNo);
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
		if(inReadOnlyMode())
			return;

		TreeItem item = tree.getSelectedItem();
		if(item == null){
			Window.alert(LocaleText.get("selectDeleteItem"));
			return;
		}

		if(!inCutMode && !Window.confirm(LocaleText.get("deleteTreeItemPrompt")))
			return;

		deleteItem(item);
	}

	private void deleteItem(TreeItem item){		
		TreeItem parent = item.getParentItem();
		int index;
		if(parent != null){
			index = parent.getChildIndex(item);

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
			index = getRootItemIndex(item);
			item.remove();

			int count = tree.getItemCount();
			if(count > 0){
				if(index == count)
					index--;
				tree.setSelectedItem(tree.getItem(index));
			}
			//tree.setSelectedItem(tree.getItem(0));
		}

		if(tree.getSelectedItem() == null){
			formDef = null;
			fireFormItemSelected(null);
		}
	}

	private int getRootItemIndex(TreeItem item){
		int count = tree.getItemCount();
		for(int index = 0; index < count; index++){
			if(item == tree.getItem(index))
				return index;
		}
		return 0;
	}

	private void removeFormDefItem(TreeItem item, TreeItem parent){
		Object userObj = item.getUserObject();
		Object parentUserObj = parent.getUserObject();

		if(userObj instanceof QuestionDef){
			if(parentUserObj instanceof QuestionDef)
				((QuestionDef)parentUserObj).getRepeatQtnsDef().removeQuestion((QuestionDef)userObj,formDef);
			else
				((PageDef)parentUserObj).removeQuestion((QuestionDef)userObj,formDef);			
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
		if(inReadOnlyMode())
			return;

		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item != null){
			Object userObj = item.getUserObject();
			if(userObj instanceof QuestionDef){
				int id = ++nextQuestionId;
				QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,item.getParentItem().getUserObject());
				item = addImageItem(item.getParentItem(), questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
				addFormDefItem(questionDef,item.getParentItem());
				tree.setSelectedItem(item);
			}
			else if(userObj instanceof OptionDef){
				int id = ++nextOptionId;
				OptionDef optionDef = new OptionDef(id,LocaleText.get("option")+id,"option"+id,(QuestionDef)item.getParentItem().getUserObject());
				item = addImageItem(item.getParentItem(), optionDef.getText(), images.markRead(),optionDef,null);
				addFormDefItem(optionDef,item.getParentItem());
				tree.setSelectedItem(item);	
			}
			else if(userObj instanceof PageDef){
				int id = ++nextPageId;
				PageDef pageDef = new PageDef(LocaleText.get("page")+id,id,null,(FormDef)item.getParentItem().getUserObject());
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
		int id = ++nextFormId;
		addNewForm("New Form"+id,"newform"+id,id);
		
		//Automatically add a new page
		addNewChildItem(false);
		
		//Automatically add a new question
		addNewChildItem(false);
	}

	public void addNewForm(String name, String varName, int formId){
		if(inReadOnlyMode())
			return;

		if(formExists(formId))
			return;

		FormDef formDef = new FormDef(formId,name, varName,null,null,null,null,null);
		TreeItem item = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
		item.setUserObject(formDef);
		tree.addItem(item);
		tree.setSelectedItem(item);
	}

	public void addNewChildItem(){
		addNewChildItem(true);
	}

	/**
	 * Adds a new child item.
	 */
	public void addNewChildItem(boolean addNewIfNoKids){
		if(inReadOnlyMode())
			return;

		TreeItem item = tree.getSelectedItem();

		//Check if there is any selection.
		if(item == null){
			if(addNewIfNoKids)
				addNewItem();
			return;
		}

		Object userObj = item.getUserObject();
		if(userObj instanceof PageDef || 
				(userObj instanceof QuestionDef && ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_REPEAT) ){

			int id = ++nextQuestionId;
			QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,userObj);
			item = addImageItem(item, questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
			addFormDefItem(questionDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
		}
		else if(userObj instanceof QuestionDef && 
				( ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
						((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_MULTIPLE ) ){

			int id = ++nextOptionId;
			OptionDef optionDef = new OptionDef(id,LocaleText.get("option")+id,"option"+id,(QuestionDef)userObj);
			item = addImageItem(item, optionDef.getText(), images.markRead(),optionDef,null);
			addFormDefItem(optionDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
		}
		else if(userObj instanceof FormDef){
			int id = ++nextPageId;
			PageDef pageDef = new PageDef(LocaleText.get("page")+id,id,null,(FormDef)userObj);
			item = addImageItem(item, pageDef.getName(), images.drafts(),pageDef,null);
			addFormDefItem(pageDef,item.getParentItem());
			tree.setSelectedItem(item);
			item.getParentItem().setState(true);
			
			//Automatically add a new question
			addNewChildItem(false);
		}
		else if(addNewIfNoKids)
			addNewItem();
	}

	public void moveItemUp() {
		if(inReadOnlyMode())
			return;

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
		if(inReadOnlyMode())
			return;

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
			item.setWidget(new TreeItemWidget(images.lookup(), questionDef.getDisplayText(),popup,this));
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
		if(inReadOnlyMode())
			return;

		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		clipboardItem = item.getUserObject();  

		inCutMode = true;
		deleteSelectedItem();
		inCutMode = false;
	}

	public void copyItem() {
		if(inReadOnlyMode())
			return;

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
		if(inReadOnlyMode())
			return;

		formDesignerListener.refresh(this);
	}

	public void saveItem(){
		formDesignerListener.saveForm();
	}

	public FormDef getSelectedForm(){
		TreeItem  item = tree.getSelectedItem();
		if(item != null)
			return getSelectedForm(item);
		return null;
	}

	private FormDef getSelectedForm(TreeItem item){
		Object obj = item.getUserObject();
		if(obj instanceof FormDef)
			return (FormDef)obj;
		return getSelectedForm(item.getParentItem());
	}

	private TreeItem getSelectedItemRoot(TreeItem item){
		if(item == null)
			return null;

		if(item.getParentItem() == null)
			return item;
		return getSelectedItemRoot(item.getParentItem());
	}

	public void clear(){
		tree.clear();
	}

	public boolean isValidForm(){
		TreeItem  parent = getSelectedItemRoot(tree.getSelectedItem());
		if(parent == null)
			return true;

		Map<String,String> pageNos = new HashMap<String,String>();
		Map<String,QuestionDef> bindings = new HashMap<String,QuestionDef>();
		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			TreeItem child = parent.getChild(index);
			PageDef pageDef = (PageDef)child.getUserObject();
			String pageNo = String.valueOf(pageDef.getPageNo());
			if(pageNos.containsKey(pageNo)){
				tree.setSelectedItem(child);
				tree.ensureSelectedItemVisible();
				Window.alert(LocaleText.get("selectedPage") + pageDef.getName() +LocaleText.get("shouldNotSharePageBinding") + pageNos.get(pageNo)+ "]");
				return false;
			}
			else
				pageNos.put(pageNo, pageDef.getName());

			if(!isValidQuestionList(child,bindings))
				return false;
		}

		return true;
	}

	public boolean isValidQuestionList(TreeItem  parent,Map<String,QuestionDef> bindings){
		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			TreeItem child = parent.getChild(index);
			QuestionDef questionDef = (QuestionDef)child.getUserObject();
			String variableName = questionDef.getVariableName();
			if(bindings.containsKey(variableName) /*&& questionDef.getParent() == bindings.get(variableName).getParent()*/){
				tree.setSelectedItem(child);
				tree.ensureSelectedItemVisible();
				Window.alert(LocaleText.get("selectedQuestion") + questionDef.getText()+LocaleText.get("shouldNotShareQuestionBinding") + bindings.get(variableName).getDisplayText()+ "]");
				return false;
			}
			else
				bindings.put(variableName, questionDef);

			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
				if(!isValidQuestionList(child,bindings))
					return false;
			}
			else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
					questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				if(!isValidOptionList(child))
					return false;
			}
		}

		return true;
	}

	public boolean isValidOptionList(TreeItem  parent){
		Map<String,String> bindings = new HashMap<String,String>();

		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			TreeItem child = parent.getChild(index);
			OptionDef optionDef = (OptionDef)child.getUserObject();
			String variableName = optionDef.getVariableName();
			if(bindings.containsKey(variableName)){
				tree.setSelectedItem(child);
				tree.ensureSelectedItemVisible();
				Window.alert(LocaleText.get("selectedOption") + optionDef.getText()+LocaleText.get("shouldNotShareOptionBinding") + bindings.get(variableName)+ "]");
				return false;
			}
			else
				bindings.put(variableName, optionDef.getText());
		}
		return true;
	}

	public void moveUp(){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		int index;
		TreeItem parent = item.getParentItem();
		if(parent == null){
			index = getRootItemIndex(parent);
			if(index == 0)
				return;
			tree.setSelectedItem(tree.getItem(index - 1));
		}
		else{
			index = parent.getChildIndex(item);
			if(index == 0)
				return;
			tree.setSelectedItem(parent.getChild(index - 1));
		}
	}

	public void moveDown(){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		int index;
		TreeItem parent = item.getParentItem();
		if(parent == null){
			index = getRootItemIndex(parent);
			if(index == tree.getItemCount() - 1)
				return;
			tree.setSelectedItem(tree.getItem(index + 1));
		}
		else{
			index = parent.getChildIndex(item);
			if(index == parent.getChildCount() - 1)
				return;
			tree.setSelectedItem(parent.getChild(index + 1));
		}
	}

	public void moveToParent(){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		TreeItem parent = item.getParentItem();
		if(parent == null)
			return;

		tree.setSelectedItem(parent);
		tree.ensureSelectedItemVisible();
	}

	public void moveToChild(){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		if(item.getChildCount() == 0){
			addNewChildItem(false);
			return;
		}

		TreeItem child = item.getChild(0);
		tree.setSelectedItem(child);
		tree.ensureSelectedItemVisible();
	}

	private boolean inReadOnlyMode(){
		return Context.isStructureReadOnly();
	}
}
