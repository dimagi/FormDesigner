package org.openrosa.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrosa.client.Context;
import org.openrosa.client.dnd.JrTreePanelDropTarget;
import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.TreeModelItem;
import org.purc.purcforms.client.controller.IFormActionListener;
import org.purc.purcforms.client.controller.IFormChangeListener;
import org.purc.purcforms.client.controller.IFormDesignerListener;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.ModelConstants;
import org.purc.purcforms.client.util.FormDesignerUtil;
import org.purc.purcforms.client.xforms.XformConstants;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.DND.Feedback;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
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
public class FormsTreeView extends com.extjs.gxt.ui.client.widget.Composite implements SelectionHandler<TreeItem>,IFormChangeListener,IFormActionListener, ModelIconProvider<TreeModelItem> {

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
	//private Tree tree;

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

	/** The next available form id. We always have one form for OpenRosa form designer. */
	private int nextFormId = 1;

	/** The next available question id. */
	private int nextQuestionId = 0;

	/** The next available question option id. */
	private int nextOptionId = 0;

	/** The listener to form designer global events. */
	private IFormDesignerListener formDesignerListener;

	private TreePanel treePanel;


	/**
	 * Creates a new instance of the forms tree view widget.
	 * 
	 * @param images the tree images.
	 * @param formSelectionListener the form item selection events listener.
	 */
	public FormsTreeView(Images images,IFormSelectionListener formSelectionListener) {

		this.images = images;
		this.formSelectionListeners.add(formSelectionListener);


		treePanel = new TreePanel(new TreeStore<TreeModelItem>());
		treePanel.getStyle().setLeafIcon(AbstractImagePrototype.create(images.newform())); 
		//treePanel.getStyle().setJointExpandedIcon(jointExpandedIcon)
		treePanel.setDisplayProperty("text");  
		treePanel.setAutoLoad(true);
		//treePanel.setAutoHeight(true);
		treePanel.setIconProvider(this);
		initComponent(treePanel);

		treePanel.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<TreeModelItem>>(){
			public void handleEvent(SelectionChangedEvent<TreeModelItem> te) {
				if(te.getSelectedItem() == null)
					return;

				Object selObject = te.getSelectedItem().getUserObject();
				Context.setFormDef(FormDef.getFormDef((IFormElement)selObject));
				formDef = Context.getFormDef();
				fireFormItemSelected(selObject);
			}
		});


		//add drag and drop
		TreePanelDragSource source = new TreePanelDragSource(treePanel);  
		source.addDNDListener(new DNDListener() {  
			@Override  
			public void dragStart(DNDEvent e) {  
				ModelData sel = treePanel.getSelectionModel().getSelectedItem();  
				if (sel != null && sel == treePanel.getStore().getRootItems().get(0)) {  
					e.setCancelled(true);  
					e.getStatus().setStatus(false);  
					return;  
				}  
				super.dragStart(e);  
			}  

			/*@Override
			public void dragDrop(DNDEvent e) {
				super.dragDrop(e);

				List<TreeStoreModel> models = e.getData();
				TreeStoreModel storeModel = (TreeStoreModel) models.get(0);
				final TreeModelItem data = (TreeModelItem) storeModel.getModel();
				System.out.println(data.getText());
				System.out.println(treePanel.getStore().indexOf(data));
				System.out.println(data.getParent().indexOf(data));

				treePanel.getStore().rejectChanges();
			}*/
		});  

		JrTreePanelDropTarget target = new JrTreePanelDropTarget(treePanel,this);  
		target.setAllowSelfAsSource(true);  
		target.setFeedback(Feedback.BOTH); 




		/*tree = new Tree(images);

		//initWidget(tree);
		FormUtil.maximizeWidget(tree);

		tree.addSelectionHandler(this);
		tree.ensureSelectedItemVisible();

		//This is just for solving an abnormal behavior when one changes a node text
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
		});*/

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
	/*private TreeItem addImageItem(TreeItem root, String title,ImageResource imageProto, Object userObj,String helpText) {
		TreeItem item = new CompositeTreeItem(new TreeItemWidget(imageProto, title,popup,this));
		item.setUserObject(userObj);
		item.setTitle(helpText);
		if(root != null)
			root.addItem(item);
		else
			tree.addItem(item);

		return item;
	}*/


	/**
	 * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(SelectionEvent)
	 */
	public void onSelection(SelectionEvent<TreeItem> event){

		scrollToLeft();

		TreeItem item = event.getSelectedItem();

		//Should not call this more than once for the same selected item.
		if(item != this.item){
			Context.setFormDef(FormDef.getFormDef((IFormElement)item.getUserObject()));
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

		//We do not support loading of more than one form at the same time.
		treePanel.getStore().removeAll();

		if(formDef.getId() == ModelConstants.NULL_ID)
			formDef.setId(nextFormId);

		if(!langRefresh){
			int count = formDef.getQuestionCount();
			if(nextQuestionId <= count)
				nextQuestionId = count;

			this.formDef = formDef;

			if(formExists(formDef.getId()))
				return;

			//A temporary hack to ensure top level object is accessed.
			//fireFormItemSelected(formDef);
		}

		TreeModelItem formRoot = null;
		if(showFormAsRoot){
			/*formRoot = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
			formRoot.setUserObject(formDef);
			tree.addItem(formRoot);*/

			formRoot = new TreeModelItem(formDef.getName(),formDef,null);
			treePanel.getStore().add(formRoot, true);
		}

		if(formDef.getChildren() != null){
			for(int index = 0; index < formDef.getChildren().size(); index++){
				IFormElement element = formDef.getChildAt(index);
				TreeModelItem pageRoot = null;

				if(element instanceof GroupDef)
					loadGroup((GroupDef)element,formRoot);
				else
					this.loadQuestion((QuestionDef)element, formRoot);

				//We expand only the first page.
				if(index == 0)
					treePanel.setExpanded(pageRoot, true);
				//pageRoot.setState(true);    
			}
		}

		if(select && formRoot != null){
			//tree.setSelectedItem(formRoot);
			//formRoot.setState(true);
			treePanel.setExpanded(formRoot, true);
			treePanel.getSelectionModel().select(formRoot, true);
		}

	}

	/**
	 * Check if a form with a given id is loaded.
	 * 
	 * @param formId the form id.
	 * @return true if it exists, else false.
	 */
	public boolean formExists(int formId){
		int count = treePanel.getStore().getChildCount(); //.getItemCount();
		for(int index = 0; index < count; index++){
			TreeModelItem item = (TreeModelItem)treePanel.getStore().getChild(index);
			if(((FormDef)item.getUserObject()).getId() == formId){
				//tree.setSelectedItem(item);
				treePanel.getSelectionModel().select(item, false);
				return true;
			}
		}

		return false;
	}

	public void refreshForm(FormDef formDef){
		//tree.clear();
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item != null){
			TreeModelItem root = getSelectedItemRoot(item);
			formDef.setId(((FormDef)root.getUserObject()).getId());

			//tree.removeItem(root);
			treePanel.getStore().remove(root);
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

		int count = treePanel.getStore().getChildCount(); //.getItemCount();
		for(int index = 0; index < count; index++)
			forms.add((FormDef)((TreeModelItem)treePanel.getStore().getChild(index)).getUserObject());

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

		//tree.clear();
		treePanel.getStore().removeAll();
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

	private TreeModelItem loadGroup(GroupDef pageDef,TreeModelItem formRoot){
		//TreeItem pageRoot = addImageItem(formRoot, pageDef.getName(), images.drafts(),pageDef,null);

		TreeModelItem pageRoot = addImageItem(formRoot, pageDef.getName(),pageDef);
		loadQuestions(pageDef.getChildren(),pageRoot);
		return pageRoot;
	}

	private void loadQuestions(List<IFormElement> questions,TreeModelItem root){
		if(questions != null){
			for(int currentQtnNo=0; currentQtnNo<questions.size(); currentQtnNo++){
				IFormElement element = questions.get(currentQtnNo);

				if(element instanceof GroupDef)
					loadGroup((GroupDef)element,root);
				else
					loadQuestion((QuestionDef)element,root);
			}
		}
	}

	private TreeModelItem addImageItem(TreeModelItem root,String text, Object userObject){
		TreeModelItem item = new TreeModelItem(text,userObject,root);
		treePanel.getStore().add(root,item, true);
		return item;
	}


	private TreeModelItem loadQuestion(QuestionDef questionDef,TreeModelItem root){
		//TreeItem questionRoot = addImageItem(root, questionDef.getDisplayText(), images.lookup(),questionDef,questionDef.getHelpText());
		TreeModelItem questionRoot = addImageItem(root, questionDef.getDisplayText(), questionDef);

		if(questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || 
				questionDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
			List options = questionDef.getOptions();
			for(int currentOptionNo=0; currentOptionNo < options.size(); currentOptionNo++){
				OptionDef optionDef = (OptionDef)options.get(currentOptionNo);
				//addImageItem(questionRoot, optionDef.getText(), images.markRead(),optionDef,null);
				addImageItem(questionRoot, optionDef.getText(), optionDef);
			}
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_BOOLEAN){
			//addImageItem(questionRoot, QuestionDef.TRUE_DISPLAY_VALUE, images.markRead(),null,null);
			//addImageItem(questionRoot, QuestionDef.FALSE_DISPLAY_VALUE, images.markRead(),null,null);
			OptionDef optionDef = new OptionDef(1, QuestionDef.TRUE_DISPLAY_VALUE, QuestionDef.TRUE_VALUE,questionDef);
			addImageItem(questionRoot, QuestionDef.TRUE_DISPLAY_VALUE, optionDef);

			optionDef = new OptionDef(2, QuestionDef.FALSE_DISPLAY_VALUE, QuestionDef.FALSE_VALUE,questionDef);
			addImageItem(questionRoot, QuestionDef.FALSE_DISPLAY_VALUE, optionDef);
		}
		else if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			loadQuestions(questionDef.getChildren(),questionRoot);

		return questionRoot;
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#deleteSelectedItem()
	 */
	public void deleteSelectedItem(){
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
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
	private void deleteItem(TreeModelItem item){		
		TreeModelItem parent = (TreeModelItem)item.getParent();
		int index;
		if(parent != null){
			index = parent.indexOf(item);

			//If last item is the one selected, the select the previous, else the next.
			if(index == parent.getChildCount()-1)
				index -= 1;

			removeFormDefItem(item,parent);

			//Remove the selected item.
			//item.remove();
			parent.remove(item);
			treePanel.getStore().remove(item);
			//treePanel.getStore().update(parent);

			//If no more kids, then select the parent.
			if(parent.getChildCount() == 0)
				treePanel.getSelectionModel().select(parent, false);
			//tree.setSelectedItem(parent);
			else
				treePanel.getSelectionModel().select(parent.getChild(index), false);
			//tree.setSelectedItem(parent.getChild(index));
		}
		else{ //Must be the form root
			index = getRootItemIndex(item);
			//item.remove();
			treePanel.getStore().remove(item);

			int count = treePanel.getStore().getChildCount(); //tree.getItemCount();

			//If we have any items left, select the one which was after
			//the one we have just removed.
			if(count > 0){

				//If we have deleted the last item, select the item which was before it.
				if(index == count)
					index--;

				//tree.setSelectedItem(tree.getItem(index));
				treePanel.getSelectionModel().select(treePanel.getStore().getChild(index), false);
			}
		}

		//if(tree.getSelectedItem() == null){
		if(treePanel.getSelectionModel().getSelectedItem() == null){
			Context.setFormDef(null);
			formDef = null;
			fireFormItemSelected(null);

			nextFormId = 1;
			nextQuestionId = 0;
			nextOptionId = 0;
		}
	}

	/**
	 * Gets the index of the tree item which is at the root level.
	 * 
	 * @param item the tree root item whose index we are to get.
	 * @return the index of the tree item.
	 */
	/*private int getRootItemIndex(TreeItem item){
		int count = tree.getItemCount();
		for(int index = 0; index < count; index++){
			if(item == tree.getItem(index))
				return index;
		}

		return 0;
	}*/

	/**
	 * Gets the index of the tree item which is at the root level.
	 * 
	 * @param item the tree root item whose index we are to get.
	 * @return the index of the tree item.
	 */
	private int getRootItemIndex(TreeModelItem item){
		int count = treePanel.getStore().getChildCount(); //getItemCount()
		for(int index = 0; index < count; index++){
			if(item == treePanel.getStore().getChild(index))
				return index;
		}

		return 0;
	}


	private void removeFormDefItem(TreeModelItem item, TreeModelItem parent){
		Object userObj = item.getUserObject();
		Object parentUserObj = parent.getUserObject();

		if(userObj instanceof QuestionDef){
			if(parentUserObj instanceof QuestionDef)
				((QuestionDef)parentUserObj).getRepeatQtnsDef().removeQuestion((QuestionDef)userObj,formDef);
			else
				((IFormElement)parentUserObj).removeChild((IFormElement)userObj);			
		}
		else if(userObj instanceof OptionDef){
			((QuestionDef)parentUserObj).removeOption((OptionDef)userObj);
		}
		else if(userObj instanceof GroupDef)
			((IFormElement)parentUserObj).removeChild((IFormElement)userObj); //((FormDef)parentUserObj).removePage((PageDef)userObj);	
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#addNewItem()
	 */
	public void addNewItem(){
		if(inReadOnlyMode())
			return;

		/*TreeItem item = tree.getSelectedItem();

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
			addNewForm();*/

		TreeModelItem selModelItem = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();

		//Check if there is any selection.
		if(selModelItem != null){
			Object userObj = selModelItem.getUserObject();
			if(userObj instanceof QuestionDef){
				int id = ++nextQuestionId;
				QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)((TreeModelItem)selModelItem.getParent()).getUserObject());
				questionDef.setItextId(questionDef.getBinding());
				TreeModelItem modelItem = new TreeModelItem(questionDef.getText(),questionDef,selModelItem.getParent());
				addFormDefItem(questionDef,(TreeModelItem)selModelItem.getParent());
				treePanel.getStore().add(selModelItem.getParent(),modelItem, true);
				treePanel.getSelectionModel().select(modelItem, false);
			}
			else if(userObj instanceof OptionDef){
				int id = ++nextOptionId;
				OptionDef optionDef = new OptionDef(id,LocaleText.get("option")+id,"option"+id,(QuestionDef)((TreeModelItem)selModelItem.getParent()).getUserObject());
				optionDef.setItextId(optionDef.getBinding());
				TreeModelItem modelItem = new TreeModelItem(optionDef.getText(),optionDef,selModelItem.getParent());
				addFormDefItem(optionDef,(TreeModelItem)selModelItem.getParent());
				treePanel.getStore().add(selModelItem.getParent(),modelItem, true);
				treePanel.getSelectionModel().select(modelItem, false);
			}
			else if(userObj instanceof GroupDef){
				int id = ++nextQuestionId;
				GroupDef pageDef = new GroupDef(LocaleText.get("page")+id,null,(FormDef)((TreeModelItem)selModelItem.getParent()).getUserObject());
				pageDef.setItextId(FormDesignerUtil.getXmlTagName(pageDef.getName()));
				TreeModelItem modelItem = new TreeModelItem(pageDef.getName(),pageDef,selModelItem.getParent());
				addFormDefItem(pageDef,(TreeModelItem)selModelItem.getParent());
				treePanel.getStore().add(selModelItem.getParent(),modelItem, true);
				treePanel.getSelectionModel().select(modelItem, false);
			}
			else if(userObj instanceof FormDef)
				addNewForm();
		}
		else
			addNewForm();
	}

	public void addNewQuestion(int dataType){
		if(inReadOnlyMode())
			return;

		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();

		//Check if there is any selection.
		if(item != null){
			Object userObj = item.getUserObject();
			if(userObj instanceof QuestionDef){
				int id = ++nextQuestionId;
				if(dataType == QuestionDef.QTN_TYPE_GROUP){
					GroupDef pageDef = new GroupDef("Group "+id,null,(IFormElement)((TreeModelItem)item.getParent()).getUserObject());
					item = addImageItem((TreeModelItem)item.getParent(), pageDef.getText() ,pageDef);
					addFormDefItem(pageDef, (TreeModelItem)item.getParent());

					//if(dataType == QuestionDef.QTN_TYPE_GROUP)
					//	addNewQuestion(QuestionDef.QTN_TYPE_TEXT);
				}
				else{
					QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)((TreeModelItem)item.getParent()).getUserObject());
					questionDef.setDataType(dataType);
					questionDef.setItextId(questionDef.getBinding());
					//item = addImageItem(item.getParent(), questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
					item = addImageItem((TreeModelItem)item.getParent(), questionDef.getText(), questionDef);
					addFormDefItem(questionDef,(TreeModelItem)item.getParent());

					if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
						addNewOptionDef(questionDef, item);
				}

				//tree.setSelectedItem(item);
				treePanel.getSelectionModel().select(item, false);
			}
			else if(userObj instanceof OptionDef){
				//addNewOptionDef();

				int id = ++nextQuestionId;
				QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)((TreeModelItem)item.getParent().getParent()).getUserObject());
				questionDef.setDataType(dataType);
				questionDef.setItextId(questionDef.getBinding());
				//item = addImageItem(item.getParent(), questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
				item = addImageItem((TreeModelItem)item.getParent().getParent(), questionDef.getText(), questionDef);
				addFormDefItem(questionDef,(TreeModelItem)item.getParent());

				if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
					addNewOptionDef(questionDef, item);
				else if(dataType == QuestionDef.QTN_TYPE_GROUP)
					addNewQuestion(QuestionDef.QTN_TYPE_TEXT);

				//tree.setSelectedItem(item);
				treePanel.getSelectionModel().select(item, false);
			}
			else if(userObj instanceof GroupDef){				
				int id = ++nextQuestionId;
				QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)((TreeModelItem)item.getParent()).getUserObject());
				questionDef.setDataType(dataType);
				questionDef.setItextId(questionDef.getBinding());
				//item = addImageItem(item.getParent(), questionDef.getText(), images.lookup(),questionDef,questionDef.getHelpText());
				item = addImageItem((TreeModelItem)item.getParent(), questionDef.getText(), questionDef);
				addFormDefItem(questionDef,(TreeModelItem)item.getParent());

				if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
					addNewOptionDef(questionDef, item);
				else if(dataType == QuestionDef.QTN_TYPE_GROUP)
					addNewQuestion(QuestionDef.QTN_TYPE_TEXT);

				//tree.setSelectedItem(item);
				treePanel.getSelectionModel().select(item, false);
			}
			else if(userObj instanceof FormDef){
				//addNewForm();

				//If not yet got pages, just quit.
				if(item.getChildCount() == 0)
					return;

				TreeModelItem parentItem = (TreeModelItem)item.getChild(0);

				int id = ++nextQuestionId;
				QuestionDef questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)parentItem.getUserObject());
				questionDef.setDataType(dataType);
				questionDef.setItextId(questionDef.getBinding());
				item = addImageItem(item, questionDef.getText(), questionDef);
				addFormDefItem(questionDef, (TreeModelItem)item.getParent());

				if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
					addNewOptionDef(questionDef, item);
				else if(dataType == QuestionDef.QTN_TYPE_GROUP)
					addNewQuestion(QuestionDef.QTN_TYPE_TEXT);

				treePanel.getSelectionModel().select(item, false);
			}
		}
		else{
			addNewForm(dataType);

			//Get the newly added question and set its appropriate type.
			item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
			IFormElement questionDef = (IFormElement)item.getUserObject();
			questionDef.setDataType(dataType);
			questionDef.setItextId(questionDef.getBinding());

			if(dataType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || dataType == QuestionDef.QTN_TYPE_LIST_MULTIPLE)
				addNewOptionDef((QuestionDef)questionDef, item);
			else if(dataType == QuestionDef.QTN_TYPE_GROUP)
				;//addNewQuestion(QuestionDef.QTN_TYPE_TEXT);

			treePanel.getSelectionModel().select(item, false);
		}

	}


	private void addNewOptionDef(QuestionDef questionDef, TreeModelItem parentItem){
		int id = ++nextOptionId;
		OptionDef optionDef = new OptionDef(id,LocaleText.get("option")+id,"option"+id,questionDef);
		optionDef.setItextId(optionDef.getBinding());
		//addImageItem(parentItem, optionDef.getText(), images.markRead(),optionDef,null);
		addImageItem(parentItem, optionDef.getText(),optionDef);
		addFormDefItem(optionDef,parentItem);
	}

	private void addFormDefItem(Object obj,TreeModelItem parentItem){
		Object parentUserObj = parentItem.getUserObject();
		if(parentUserObj instanceof QuestionDef){
			if(obj instanceof OptionDef)
				((QuestionDef)parentUserObj).addOption((OptionDef)obj);
			else
				((IFormElement)parentUserObj).getParent().addChild((IFormElement)obj);
			//((QuestionDef)parentUserObj).getRepeatQtnsDef().addQuestion((QuestionDef)obj);
		}
		else if(parentUserObj instanceof GroupDef || parentUserObj instanceof FormDef)
			((IFormElement)parentUserObj).addChild((IFormElement)obj);
	}

	public void addNewForm(){
		addNewForm(QuestionDef.QTN_TYPE_TEXT);
	}

	public void addNewForm(int qtnDataType){
		int id = nextFormId;
		addNewForm(LocaleText.get("newForm")+id,"new_form"+id,id);

		//Automatically add a new question
		addNewChildItem(false, qtnDataType);
	}

	public void addNewChildItem(boolean addNewIfNoKids){
		addNewChildItem(addNewIfNoKids, QuestionDef.QTN_TYPE_TEXT);
	}

	public void addNewForm(String name, String varName, int formId){
		if(inReadOnlyMode())
			return;

		if(formExists(formId))
			return;

		FormDef formDef = new FormDef(formId,name,varName, varName,null,null,null,null,null,null);
		formDef.setItextId(formDef.getVariableName());
		/*TreeItem item = new CompositeTreeItem(new TreeItemWidget(images.note(), formDef.getName(),popup,this));
		item.setUserObject(formDef);
		tree.addItem(item);
		tree.setSelectedItem(item);*/

		TreeModelItem modelItem = new TreeModelItem(formDef.getName(),formDef,null);
		treePanel.getStore().add(modelItem, true);
		treePanel.getSelectionModel().select(modelItem, false);
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
	public void addNewChildItem(boolean addNewIfNoKids, int dataType){
		if(inReadOnlyMode())
			return;

		TreeModelItem selModelItem = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();

		//Check if there is any selection.
		if(selModelItem == null){
			if(addNewIfNoKids)
				addNewItem();
			return;
		}

		Object userObj = selModelItem.getUserObject();
		if(userObj instanceof GroupDef || userObj instanceof FormDef || 
				(userObj instanceof QuestionDef && ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_REPEAT) ){

			int id = ++nextQuestionId;
			IFormElement questionDef = new QuestionDef(id,LocaleText.get("question")+id,QuestionDef.QTN_TYPE_TEXT,"question"+id,(IFormElement)userObj);

			if(dataType == QuestionDef.QTN_TYPE_REPEAT){
				IFormElement groupDef = new GroupDef(questionDef.getText(),null,(IFormElement)userObj);
				groupDef.setDataType(dataType);
				groupDef.setId(questionDef.getId());
				groupDef.setBinding(questionDef.getBinding());
				questionDef = groupDef;
			}

			questionDef.setItextId(questionDef.getBinding());
			TreeModelItem modelItem = new TreeModelItem(questionDef.getText(),questionDef,selModelItem);
			addFormDefItem(questionDef,selModelItem);
			treePanel.getStore().add(selModelItem,modelItem, true);
			treePanel.getSelectionModel().select(modelItem, false);
		}
		else if(userObj instanceof QuestionDef && 
				( ((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
						((QuestionDef)userObj).getDataType() ==  QuestionDef.QTN_TYPE_LIST_MULTIPLE ) ){

			int id = ++nextOptionId;
			OptionDef optionDef = new OptionDef(id,LocaleText.get("option")+id,"option"+id,(QuestionDef)userObj);
			optionDef.setItextId(optionDef.getBinding());
			TreeModelItem modelItem = new TreeModelItem(optionDef.getText(),optionDef,selModelItem);
			addFormDefItem(optionDef,selModelItem);
			treePanel.getStore().add(selModelItem,modelItem, true);
			treePanel.getSelectionModel().select(modelItem, false);
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

		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();

		//Check if there is any selection.
		if(item == null)
			return;

		TreeModelItem parent = (TreeModelItem)item.getParent();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.indexOf(item);
		if(index == 0)
			return; //Can't move any further upwards.

		//move the item in the form object model.
		moveFormItemUp(item,parent);

		TreeModelItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//item.remove();
		parent.remove(item);
		treePanel.getStore().remove(item);

		//parent.insert(item, index -1);

		while(parent.getChildCount() >= index){
			currentItem = (TreeModelItem)parent.getChild(index-1);
			list.add(currentItem);
			//currentItem.remove();
			currentItem.getParent().remove(currentItem);
			treePanel.getStore().remove(currentItem);
		}

		parent.add(item);
		treePanel.getStore().add(parent,item,true);
		for(int i=0; i<list.size(); i++){
			parent.add((TreeModelItem)list.get(i));
			treePanel.getStore().add(parent,(TreeModelItem)list.get(i),true);
		}

		//treePanel.getStore().update(parent);

		//tree.setSelectedItem(item);
		treePanel.getSelectionModel().select(item, false);
	}

	private void moveFormItemUp(TreeModelItem item,TreeModelItem parent){
		IFormElement userObj = (IFormElement)item.getUserObject();
		IFormElement parentObj = (IFormElement)parent.getUserObject();

		//Normal question
		if(parentObj instanceof GroupDef)
			((GroupDef)parentObj).moveElementUp(userObj);
		/*else if(userObj instanceof QuestionDef && parentObj instanceof QuestionDef)
			((QuestionDef)parentObj).getRepeatQtnsDef().moveQuestionUp((QuestionDef)userObj);*/
		else if(parentObj instanceof FormDef)
			GroupDef.moveElementUp(((IFormElement)parentObj).getChildren(), userObj); //((FormDef)parentObj).movePageUp(userObj);
		else if(userObj instanceof OptionDef)
			((QuestionDef)parentObj).moveOptionUp((OptionDef)userObj);
	}

	private void moveFormItemDown(TreeModelItem item,TreeModelItem parent){
		IFormElement userObj = (IFormElement)item.getUserObject();
		IFormElement parentObj = (IFormElement)parent.getUserObject();

		//Normal question
		if(parentObj instanceof GroupDef)
			((GroupDef)parentObj).moveElementDown(userObj);
		/*else if(userObj instanceof QuestionDef && parentObj instanceof QuestionDef)
			((QuestionDef)parentObj).getRepeatQtnsDef().moveQuestionDown((QuestionDef)userObj);*/
		else if(parentObj instanceof FormDef)
			GroupDef.moveElementDown(((IFormElement)parentObj).getChildren(), userObj); //((FormDef)parentObj).movePageDown(userObj);
		else if(userObj instanceof OptionDef)
			((QuestionDef)parentObj).moveOptionDown((OptionDef)userObj);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveItemDown()
	 */
	public void moveItemDown(){
		if(inReadOnlyMode())
			return;

		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();

		//Check if there is any selection.
		if(item == null)
			return;

		TreeModelItem parent = (TreeModelItem)item.getParent();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.indexOf(item);
		if(index == count - 1)
			return; //Can't move any further downwards.

		//move the item in the form object model.
		moveFormItemDown(item,parent);

		TreeModelItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//item.remove();
		parent.remove(item);
		treePanel.getStore().remove(item);

		while(parent.getChildCount() > 0 && parent.getChildCount() > index){
			currentItem = (TreeModelItem)parent.getChild(index);
			list.add(currentItem);
			//currentItem.remove();
			currentItem.getParent().remove(currentItem);
			treePanel.getStore().remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				parent.add(item); //Add after the first item.
				treePanel.getStore().add(parent, item, true);
			}

			parent.add((TreeModelItem)list.get(i));
			treePanel.getStore().add(parent, (TreeModelItem)list.get(i), true);
		}

		if(list.size() == 1){
			parent.add(item);
			treePanel.getStore().add(parent, item, true);
		}

		//tree.setSelectedItem(item);
		treePanel.getSelectionModel().select(item, false);
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormChangeListener#onFormItemChanged(java.lang.Object)
	 */
	public Object onFormItemChanged(Object formItem) {
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return formItem; //How can this happen?

		if(item.getUserObject() != formItem)
			return formItem;

		if(formItem instanceof QuestionDef){
			IFormElement element = (IFormElement)formItem;
			//item.setWidget(new TreeItemWidget(images.lookup(), questionDef.getDisplayText(),popup,this));
			//item.setTitle(questionDef.getHelpText());

			if(element.getDataType() == QuestionDef.QTN_TYPE_GROUP || element.getDataType() == QuestionDef.QTN_TYPE_REPEAT){
				IFormElement newElement = new GroupDef(element.getText(), element.getChildren(), element.getParent());
				copyElementValues(newElement, element);
				item.setUserObject(newElement);
				formItem = newElement;
			}

			item.setText(element.getDisplayText());
			treePanel.getStore().update(item);
		}
		else if(formItem instanceof OptionDef){
			OptionDef optionDef = (OptionDef)formItem;
			//item.setWidget(new TreeItemWidget(images.markRead(), optionDef.getText(),popup,this));

			item.setText(optionDef.getText());
			treePanel.getStore().update(item);
		}
		else if(formItem instanceof GroupDef){
			IFormElement element = (IFormElement)formItem;
			//item.setWidget(new TreeItemWidget(images.drafts(), pageDef.getName(),popup,this));

			if(!(element.getDataType() == QuestionDef.QTN_TYPE_GROUP || element.getDataType() == QuestionDef.QTN_TYPE_REPEAT)){
				IFormElement newElement = new QuestionDef(element.getParent());
				copyElementValues(newElement, element);
				item.setUserObject(newElement);
				formItem = newElement;
			}

			item.setText(element.getText());
			treePanel.getStore().update(item);
		}
		else if(formItem instanceof FormDef){
			FormDef formDef = (FormDef)formItem;
			//item.setWidget(new TreeItemWidget(images.note(), formDef.getName(),popup,this));

			item.setText(formDef.getName());
			treePanel.getStore().update(item);
		}

		return formItem;
	}


	private void copyElementValues(IFormElement newElement, IFormElement element){
		newElement.setId(element.getId());
		newElement.setItextId(element.getItextId());
		newElement.setBinding(element.getBinding());
		newElement.setBindNode(element.getBindNode());
		newElement.setDataNode(element.getDataNode());
		newElement.setDataType(element.getDataType());
		newElement.setHelpText(element.getHelpText());
		newElement.setHintNode(element.getHintNode());
		newElement.setLabelNode(element.getLabelNode());
		newElement.setText(element.getText());
		newElement.setControlNode(element.getControlNode());
		newElement.setParent(element.getParent());
		newElement.setChildren(element.getChildren());

		IFormElement parent = element.getParent();
		int index = parent.getChildren().indexOf(element);
		parent.getChildren().remove(element);
		parent.getChildren().add(index, newElement);

		if(newElement.getControlNode() != null){
			if(newElement instanceof GroupDef){
				newElement.getControlNode().setAttribute(XformConstants.ATTRIBUTE_NAME_ID, newElement.getControlNode().getAttribute(XformConstants.ATTRIBUTE_NAME_BIND));
				newElement.getControlNode().removeAttribute(XformConstants.ATTRIBUTE_NAME_BIND);
				newElement.getControlNode().removeAttribute("mediatype");
			}
			else{
				assert(newElement instanceof QuestionDef);

				newElement.getControlNode().setAttribute(XformConstants.ATTRIBUTE_NAME_BIND, newElement.getControlNode().getAttribute(XformConstants.ATTRIBUTE_NAME_ID));
				newElement.getControlNode().removeAttribute(XformConstants.ATTRIBUTE_NAME_ID);
				newElement.getControlNode().removeAttribute("mediatype");
			}
		}
	}


	/**
	 * @see org.purc.purcforms.client.controller.IFormChangeListener#onDeleteChildren(Object)
	 */
	public void onDeleteChildren(Object formItem){
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return; //How can this happen?

		if(formItem instanceof QuestionDef){
			while(item.getChildCount() > 0)
				deleteItem((TreeModelItem)item.getChild(0));
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#cutItem()
	 */
	public void cutItem(){
		if(inReadOnlyMode())
			return;

		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
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
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
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

		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null){
			if(clipboardItem instanceof FormDef)
				this.loadForm((FormDef)clipboardItem, true, false);
			return;
		}

		Object userObj = item.getUserObject();

		String message = "The clipboard item cannot be pasted as a child of the selected item";

		if(clipboardItem instanceof QuestionDef){
			//Questions can be pasted only as kids of pages or repeat questions.
			if(! ( (userObj instanceof GroupDef) || userObj instanceof FormDef ||
					(userObj instanceof QuestionDef && ((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_REPEAT) )){
				Window.alert(message);
				return;
			}

			//create a copy of the clipboard question.
			QuestionDef questionDef = new QuestionDef((QuestionDef)clipboardItem,(IFormElement)userObj);

			//Repeat question can only be child of a page but not another question.
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT && userObj instanceof QuestionDef)
				return;

			questionDef.setId(item.getChildCount()+1);

			/*if(userObj instanceof GroupDef)
				((GroupDef)userObj).addChild(questionDef);
			else
				((QuestionDef)userObj).getRepeatQtnsDef().addQuestion(questionDef);*/

			((IFormElement)userObj).addChild(questionDef);

			item = loadQuestion(questionDef, item);

			//tree.setSelectedItem(item);
			treePanel.getSelectionModel().select(item, false);

			//item.getParentItem().setState(true);
			//item.setState(true);
		}
		else if(clipboardItem instanceof GroupDef){		
			//Pages can be pasted only as kids of forms.
			if(!(userObj instanceof FormDef)){
				Window.alert(message);
				return;
			}

			//create a copy of the clipboard page.
			GroupDef pageDef = new GroupDef((GroupDef)clipboardItem,(FormDef)userObj);
			pageDef.setId(item.getChildCount()+1);

			((IFormElement)userObj).addChild(pageDef);

			item = loadGroup(pageDef, item);

			//tree.setSelectedItem(item);
			treePanel.getSelectionModel().select(item, false);

			//item.getParentItem().setState(true);
			//item.setState(true);
		}
		else if(clipboardItem instanceof OptionDef){
			//Question options can be pasted only as kids of single and multi select questions.
			if(!(userObj instanceof QuestionDef 
					&& (((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)||
					((QuestionDef)userObj).getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE)){
				Window.alert(message);
				return;
			}

			//			create a copy of the clipboard page.
			OptionDef optionDef = new OptionDef((OptionDef)clipboardItem,(QuestionDef)userObj);
			optionDef.setId(item.getChildCount()+1);
			((QuestionDef)userObj).addOption(optionDef);
			//item = addImageItem(item, optionDef.getText(), images.markRead(),optionDef,null);
			item = addImageItem(item, optionDef.getText(),optionDef);

			//tree.setSelectedItem(item);
			treePanel.getSelectionModel().select(item, false);

			//item.getParentItem().setState(true);
			//item.setState(true);
		}
		else
			Window.alert(message);
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
		return formDef; //we always have one form in openrosa form designer.

		/*TreeModelItem  item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item != null)
			return getSelectedForm(item);
		return null;*/
	}

	/**
	 * Gets the form to which the selected tree item belongs.
	 * 
	 * @param item the tree item.
	 * @return the form definition object.
	 */
	private FormDef getSelectedForm(TreeModelItem item){
		Object obj = item.getUserObject();
		if(obj instanceof FormDef)
			return (FormDef)obj;
		return getSelectedForm((TreeModelItem)item.getParent());
	}

	private TreeModelItem getSelectedItemRoot(TreeModelItem item){
		if(item == null)
			return null;

		if(item.getParent() == null)
			return item;
		return getSelectedItemRoot((TreeModelItem)item.getParent());
	}

	/**
	 * Removes all forms.
	 */
	public void clear(){
		//tree.clear();
		treePanel.getStore().removeAll();
	}

	/**
	 * Checks if the selected form is valid for saving.
	 * 
	 * @return true if valid, else false.
	 */
	public boolean isValidForm(){
		TreeModelItem  parent = getSelectedItemRoot((TreeModelItem)treePanel.getSelectionModel().getSelectedItem());
		if(parent == null)
			return true;

		Map<String,String> pageNos = new HashMap<String,String>();
		Map<String,QuestionDef> bindings = new HashMap<String,QuestionDef>();
		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			/*TreeModelItem child = (TreeModelItem)parent.getChild(index);
			GroupDef pageDef = (GroupDef)child.getUserObject();
			String pageNo = String.valueOf(pageDef.getPageNo());
			if(pageNos.containsKey(pageNo)){
				//tree.setSelectedItem(child);
				//tree.ensureSelectedItemVisible();
				treePanel.getSelectionModel().select(child, false);
				Window.alert(LocaleText.get("selectedPage") + pageDef.getName() +LocaleText.get("shouldNotSharePageBinding") + pageNos.get(pageNo)+ "]");
				return false;
			}
			else
				pageNos.put(pageNo, pageDef.getName());

			if(!isValidQuestionList(child,bindings))
				return false;*/
		}

		return true;
	}

	private boolean isValidQuestionList(TreeModelItem  parent,Map<String,QuestionDef> bindings){
		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			TreeModelItem child = (TreeModelItem)parent.getChild(index);
			QuestionDef questionDef = (QuestionDef)child.getUserObject();
			String variableName = questionDef.getBinding();
			if(bindings.containsKey(variableName) /*&& questionDef.getParent() == bindings.get(variableName).getParent()*/){
				//tree.setSelectedItem(child);
				//tree.ensureSelectedItemVisible();
				treePanel.getSelectionModel().select(child, false);
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

	private boolean isValidOptionList(TreeModelItem  parent){
		Map<String,String> bindings = new HashMap<String,String>();

		int count = parent.getChildCount();
		for(int index = 0; index < count; index++){
			TreeModelItem child = (TreeModelItem)parent.getChild(index);
			OptionDef optionDef = (OptionDef)child.getUserObject();
			String variableName = optionDef.getBinding();
			if(bindings.containsKey(variableName)){
				//tree.setSelectedItem(child);
				//tree.ensureSelectedItemVisible();
				treePanel.getSelectionModel().select(child, false);
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
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return;

		int index;
		TreeModelItem parent = (TreeModelItem)item.getParent();
		if(parent == null){
			index = getRootItemIndex(parent);
			if(index == 0)
				return;
			//tree.setSelectedItem(tree.getItem(index - 1));
			treePanel.getSelectionModel().select(treePanel.getStore().getChild(index - 1), false);
		}
		else{
			index = parent.indexOf(item);
			if(index == 0)
				return;
			//tree.setSelectedItem(parent.getChild(index - 1));
			treePanel.getSelectionModel().select(parent.getChild(index - 1), false);
		}
	}

	/**
	 * @see org.purc.purcforms.client.controller.IFormActionListener#moveDown()
	 */
	public void moveDown(){
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return;

		int index;
		TreeModelItem parent = (TreeModelItem)item.getParent();
		if(parent == null){
			index = getRootItemIndex(parent);
			if(index == treePanel.getStore().getChildCount() - 1)
				return;
			//tree.setSelectedItem(tree.getItem(index + 1));
			treePanel.getSelectionModel().select(treePanel.getStore().getChild(index + 1), false);
		}
		else{
			index = parent.indexOf(item);
			if(index == parent.getChildCount() - 1)
				return;
			//tree.setSelectedItem(parent.getChild(index + 1));
			treePanel.getSelectionModel().select(parent.getChild(index + 1), false);
		}
	}


	/**
	 * Selected the parent of the selected item.
	 */
	public void moveToParent(){
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return;

		TreeModelItem parent = (TreeModelItem)item.getParent();
		if(parent == null)
			return;

		treePanel.getSelectionModel().select(parent, false);
		//tree.ensureSelectedItemVisible();
	}


	/**
	 * Selects the child of the selected item.
	 */
	public void moveToChild(){
		TreeModelItem item = (TreeModelItem)treePanel.getSelectionModel().getSelectedItem();
		if(item == null)
			return;

		if(item.getChildCount() == 0){
			addNewChildItem(false);
			return;
		}

		TreeModelItem child = (TreeModelItem)item.getChild(0);
		treePanel.getSelectionModel().select(child, false);
		//tree.ensureSelectedItemVisible();
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


	public void dragMoveUp(TreeModelItem item ){
		if(inReadOnlyMode())
			return;

		//Check if there is any selection.
		if(item == null)
			return;

		TreeModelItem parent = (TreeModelItem)item.getParent();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.indexOf(item);
		if(index == 0)
			return; //Can't move any further upwards.

		//move the item in the form object model.
		moveFormItemUp(item,parent);

		/*TreeModelItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//item.remove();
		parent.remove(item);
		treePanel.getStore().remove(item);

		//parent.insert(item, index -1);

		while(parent.getChildCount() >= index){
			currentItem = (TreeModelItem)parent.getChild(index-1);
			list.add(currentItem);
			//currentItem.remove();
			currentItem.getParent().remove(currentItem);
			treePanel.getStore().remove(currentItem);
		}

		parent.add(item);
		treePanel.getStore().add(parent,item,true);
		for(int i=0; i<list.size(); i++){
			parent.add((TreeModelItem)list.get(i));
			treePanel.getStore().add(parent,(TreeModelItem)list.get(i),true);
		}

		treePanel.getSelectionModel().select(item, false);*/
	}


	public void dragMoveDown(TreeModelItem item ){
		if(inReadOnlyMode())
			return;

		//Check if there is any selection.
		if(item == null)
			return;

		TreeModelItem parent = (TreeModelItem)item.getParent();

		//We don't move root node (which has no parent, that is the form itself, since we design one form at a time)
		if(parent == null)
			return;

		//One item can't move against itself.
		int count = parent.getChildCount();
		if(count == 1)
			return;

		int index = parent.indexOf(item);
		if(index == count - 1)
			return; //Can't move any further downwards.

		//move the item in the form object model.
		moveFormItemDown(item,parent);

		/*TreeModelItem currentItem; // = parent.getChild(index - 1);
		List list = new ArrayList();

		//item.remove();
		parent.remove(item);
		treePanel.getStore().remove(item);

		while(parent.getChildCount() > 0 && parent.getChildCount() > index){
			currentItem = (TreeModelItem)parent.getChild(index);
			list.add(currentItem);
			//currentItem.remove();
			currentItem.getParent().remove(currentItem);
			treePanel.getStore().remove(currentItem);
		}

		for(int i=0; i<list.size(); i++){
			if(i == 1){
				parent.add(item); //Add after the first item.
				treePanel.getStore().add(parent, item, true);
			}

			parent.add((TreeModelItem)list.get(i));
			treePanel.getStore().add(parent, (TreeModelItem)list.get(i), true);
		}

		if(list.size() == 1){
			parent.add(item);
			treePanel.getStore().add(parent, item, true);
		}

		//tree.setSelectedItem(item);
		treePanel.getSelectionModel().select(item, false);*/
	}
	
	public AbstractImagePrototype getIcon(TreeModelItem model){
		ImageResource imageResource = images.newform();
		int type = ((IFormElement)model.getUserObject()).getDataType();
		
		if(type == QuestionDef.QTN_TYPE_GROUP)
			imageResource = images.note();
		else if(type == QuestionDef.QTN_TYPE_REPEAT)
			imageResource = images.drafts();
		
		return AbstractImagePrototype.create(imageResource);
	}
}
