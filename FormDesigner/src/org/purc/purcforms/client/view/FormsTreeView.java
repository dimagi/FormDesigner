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
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.widget.CompositeTreeItem;
import org.purc.purcforms.client.widget.TreeItemWidget;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;


/**
 * Displays questions in a tree view.
 * 
 * @author daniel
 *
 */
public class FormsTreeView extends Composite implements SelectionHandler<TreeItem>,IFormChangeListener,IFormActionListener{

	/**
	 * Specifies the images that will be bundled for this Composite and specify
	 * that tree's images should also be included in the same bundle.
	 */
	public interface Images extends Toolbar.Images, Tree.Resources {
		ImageResource drafts();
		ImageResource markRead();
		ImageResource templates();
		ImageResource note();
		ImageResource lookup();
	}

	/** The main or root widget for displaying the list of forms and their contents
	 * in a tree view.
	 */
	private Tree tree;

	/** The tree images. */
	private final Images images;

	/** Pop up for displaying tree item context menu. */
	private PopupPanel popup;

	/** The item that has been copied to the clipboard. */
	private Object clipboardItem;
	private boolean inCutMode = false;

	/** The currently selected tree item. */
	private TreeItem item;

	/** Flag determining whether to set the form node as the root tree node. */
	private boolean showFormAsRoot;

	/** The currently selected form. */
	private FormDef formDef;

	/** List of form item selection listeners. */
	private List<IFormSelectionListener> formSelectionListeners = new ArrayList<IFormSelectionListener>();

	/** The next available form id. */
	private int nextFormId = 0;

	/** The next available page id. */
	private int nextPageId = 0;

	/** The next available question id. */
	private int nextQuestionId = 0;

	/** The next available question option id. */
	private int nextOptionId = 0;

	/** The listener to form designer global events. */
	private IFormDesignerListener formDesignerListener;


	/**
	 * Creates a new instance of the forms tree view widget.
	 * 
	 * @param images the tree images.
	 * @param formSelectionListener the form item selection events listener.
	 */
	public FormsTreeView(Images images,IFormSelectionListener formSelectionListener) {

		this.images = images;
		this.formSelectionListeners.add(formSelectionListener);

		tree = new Tree(images);

		initWidget(tree);
		FormUtil.maximizeWidget(tree);

		tree.addSelectionHandler(this);
		tree.ensureSelectedItemVisible();

		//This is just for solving a wiered behaviour when one changes a node text
		//and the click another node which gets the same text as the previously
		//selected text. Just comment it out and you will see what happens.
		tree.addMouseDownHandler(new MouseDownHandler(){
			public void onMouseDown(MouseDownEvent event){
				tree.setSelectedItem(tree.getSelectedItem());
				scrollToLeft();
			}
		});

		tree.addMouseUpHandler(new MouseUpHandler(){
			public void onMouseUp(MouseUpEvent event){
				scrollToLeft();
			}
		});

		initContextMenu();
	}


	private void scrollToLeft(){
		DeferredCommand.addCommand(new Command(){
			public void execute(){
				Element element = (Element)getParent().getParent().getParent().getElement().getChildNodes().getItem(0).getChildNodes().getItem(0);
				DOM.setElementPropertyInt(element, "scrollLeft", 0);
			}
		});
	}


	/**
	 * Sets the listener for form designer global events.
	 * 
	 * @param formDesignerListener the listener.
	 */
	public void setFormDesignerListener(IFormDesignerListener formDesignerListener){
		this.formDesignerListener = formDesignerListener;
	}

	/**
	 * Adds a listener to form item selection events.
	 * 
	 * @param formSelectionListener the listener to add.
	 */
	public void addFormSelectionListener(IFormSelectionListener formSelectionListener){
		this.formSelectionListeners.add(formSelectionListener);
	}

	public void showFormAsRoot(boolean showFormAsRoot){
		this.showFormAsRoot = showFormAsRoot;
	}

	/**
	 * Prepares the tree item context menu.
	 */
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
		menuBar.addItem(FormDesignerUtil.createHeaderHTML(images.refresh(),LocaleText.get("refresh")),true,new Command(){
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
	private TreeItem addImageItem(TreeItem root, String title,ImageResource imageProto, Object userObj,String helpText) {
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
	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
	 */
	public void onSelection(SelectionEvent<TreeItem> event){

		scrollToLeft();
		
		TreeItem item = event.getSelectedItem();

		//Should not call this more than once for the same selected item.
		if(item != this.item){
			Context.setFormDef(FormDef.getFormDef(item.getUserObject()));
			formDef = Context.getFormDef();

			fireFormItemSelected(item.getUserObject());
			this.item = item;
			
			//Expand if has kids such that users do not have to click the plus
			//sign to expand. Besides, some are not even aware of that.
			//if(item.getChildCount() > 0)
			//	item.setState(true);
		}
	}

	/**
	 * Notifies all form item selection listeners about the currently
	 * selected form item.
	 * 
	 * @param formItem the selected form item.
	 */
	private void fireFormItemSelected(Object formItem){
		for(int i=0; i<formSelectionListeners.size(); i++)
			formSelectionListeners.get(i).onFormItemSelected(formItem);
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

	/**
	 * Check if a form with a given id is loaded.
	 * 
	 * @param formId the form id.
	 * @return true if it exists, else false.
	 */
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

	/**
	 * Gets the list of forms that have been loaded.
	 * 
	 * @return the form list.
	 */
	public List<FormDef> getForms(){
		List<FormDef> forms = new ArrayList<FormDef>();

		int count = tree.getItemCount();
		for(int index = 0; index < count; index++)
			forms.add((FormDef)tree.getItem(index).getUserObject());

		return forms;
	}

	/**
	 * Loads a list of forms and selects one of them.
	 * 
	 * @param forms the form list to load.
	 * @param selFormId the id of the form to select.
	 */
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
	 * @see org.purc.purcforms.client.controller.IFormActionListener#deleteSelectedItem()
	 */
	public void deleteSelectedItem(){
		TreeItem item = tree.getSelectedItem();
		if(item == null){
			Window.alert(LocaleText.get("selectDeleteItem"));
			return;
		}

		if(inReadOnlyMode() && !(item.getUserObject() instanceof FormDef))
			return;

		if(!inCutMode && !Window.confirm(LocaleText.get("deleteTreeItemPrompt")))
			return;

		deleteItem(item);
	}

	/**
	 * Removes a given tree item from the tree widget.
	 * 
	 * @param item the tree item to delete.
	 */
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

			//If we have any items left, select the one which was after
			//the one we have just removed.
			if(count > 0){

				//If we have deleted the last item, select the item which was before it.
				if(index == count)
					index--;

				tree.setSelectedItem(tree.getItem(index));
			}
		}

		if(tree.getSelectedItem() == null){
			formDef = null;
			fireFormItemSelected(null);
		}
	}

	/**
	 * Gets the index of the tree item which is at the root level.
	 * 
	 * @param item the tree root item whose index we are to get.
	 * @return the index of the tree item.
	 */
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
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewItem()
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
		addNewForm(LocaleText.get("newForm")+id,"new_form"+id,id);

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

		FormDef formDef = new FormDef(formId,name,varName, varName,null,null,null,null,null,null);
		TreeItem item = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
		item.setUserObject(formDef);
		tree.addItem(item);
		tree.setSelectedItem(item);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewChildItem()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemUp()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemDown()
	 */
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

		if(item.getUserObject() != formItem)
			return;

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

	/**
	 * @see org.purc.purcforms.client.controller.IFormChangeListener#onDeleteChildren(Object)
	 */
	public void onDeleteChildren(Object formItem){
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return; //How can this happen?

		if(formItem instanceof QuestionDef){
			while(item.getChildCount() > 0)
				deleteItem(item.getChild(0));
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#cutItem()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#copyItem()
	 */
	public void copyItem() {
		TreeItem item = tree.getSelectedItem();
		if(item == null)
			return;

		clipboardItem = item.getUserObject();
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#pasteItem()
	 */
	public void pasteItem(){
		if(inReadOnlyMode())
			return;

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
							((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_REPEAT) )){
				return;
			}

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

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerListener#refresh(Object)
	 */
	public void refreshItem(){
		if(inReadOnlyMode())
			return;

		formDesignerListener.refresh(this);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormDesignerListener#saveForm()
	 */
	public void saveItem(){
		formDesignerListener.saveForm();
	}

	/**
	 * Gets the selected form.
	 * 
	 * @return the selected form.
	 */
	public FormDef getSelectedForm(){
		TreeItem  item = tree.getSelectedItem();
		if(item != null)
			return getSelectedForm(item);
		return null;
	}

	/**
	 * Gets the form to which the selected tree item belongs.
	 * 
	 * @param item the tree item.
	 * @return the form definition object.
	 */
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

	/**
	 * Removes all forms.
	 */
	public void clear(){
		tree.clear();
	}

	/**
	 * Checks if the selected form is valid for saving.
	 * 
	 * @return true if valid, else false.
	 */
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

	private boolean isValidQuestionList(TreeItem  parent,Map<String,QuestionDef> bindings){
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

	private boolean isValidOptionList(TreeItem  parent){
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveUp()
	 */
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

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveDown()
	 */
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


	/**
	 * Selected the parent of the selected item.
	 */
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


	/**
	 * Selects the child of the selected item.
	 */
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


	/**
	 * Checks if the selected form is in read only mode. In read only mode
	 * we can only change the text and help text of items.
	 * 
	 * @return true if in read only mode, else false.
	 */
	private boolean inReadOnlyMode(){
		return Context.isStructureReadOnly();
	}
}
