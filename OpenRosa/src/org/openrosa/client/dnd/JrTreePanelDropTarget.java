package org.openrosa.client.dnd;

import java.util.List;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.OptionDef;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.model.TreeModelItem;
import org.openrosa.client.view.FormsTreeView;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
import com.google.gwt.core.client.GWT;


/**
 * This class acts as our drop target during movement of questions in the tree widget on the form outline.
 * 
 * @author daniel
 *
 */
public class JrTreePanelDropTarget extends TreePanelDropTarget{

	FormsTreeView formsTreeView;
	

	
	
	public JrTreePanelDropTarget(TreePanel tree, FormsTreeView formsTreeView) {
		super(tree);
		
		this.formsTreeView = formsTreeView;
	}

	protected void handleInsertDrop(DNDEvent event, TreeNode item, int index) {
		IFormElement targetNeighborDef = ((IFormElement)((TreeModelItem)item.getModel()).getUserObject());
		List<ModelData> models = event.getData();
		if (models.size() == 0 || !(models.get(0) instanceof TreeStoreModel)){
			return; //No item to be dropped.
		}
		TreeModelItem modelItem = (TreeModelItem)((ModelData)models.get(0)).get("model");
		
		IFormElement droppedDef = ((IFormElement) modelItem.getUserObject());
		
		//do some protection for illegal moves...
		if(targetNeighborDef instanceof FormDef && index == FormsTreeView.INSERT_BEFORE){
			formsTreeView.recalculate();
			return; //don't want anything outside of the main formDef.
		}
		
		formsTreeView.moveItem(droppedDef, targetNeighborDef, index);
		super.handleInsertDrop(event, item, index);
	}
	
	/**
	 * This will cause elements to be appended as children, if possible.
	 */
	protected void handleAppendDrop(DNDEvent event, TreeNode item) {
		IFormElement parentDef = ((IFormElement)((TreeModelItem)item.getModel()).getUserObject());
		List<ModelData> models = event.getData();
		if (models.size() == 0 || !(models.get(0) instanceof TreeStoreModel)){
			return; //No item to be dropped.
		}
		TreeModelItem modelItem = (TreeModelItem)((ModelData)models.get(0)).get("model");
		
		IFormElement droppedDef = ((IFormElement) modelItem.getUserObject());
		boolean isParentSelectQuestion = parentDef instanceof QuestionDef && (parentDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE ||
																			parentDef.getDataType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC ||
																			parentDef.getDataType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE);
		
		boolean parentHasPotentialChildren = parentDef instanceof FormDef ||
										parentDef instanceof GroupDef ||
										(parentDef instanceof QuestionDef && (parentDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT))||
										isParentSelectQuestion;
																			
		if(!parentHasPotentialChildren){ return; } //you shouldn't be trying to add children to an IFormElement that doesn't support it.
		if(isParentSelectQuestion && !(droppedDef instanceof OptionDef)){return;} //can't add anything but option to (1)Select type questions.
		
		List<IFormElement> children = parentDef.getChildren();
		if(children == null){
			parentDef.addChild(droppedDef);
		}
		IFormElement targetDef = children.get(children.size()-1);
		
		formsTreeView.moveItem(droppedDef, targetDef, FormsTreeView.INSERT_AFTER);
		
		super.handleAppendDrop(event, item);
	}
	
//	protected void handleInsertDrops(DNDEvent event, TreeNode item, int index) {
//		if(index == BEFORE){
//			GWT.log("DROPPED BEFORE ITEM");
//		
//		}else if(index == AFTER){
//			GWT.log("DROPPED AFTER ITEM");
//		}else{
//			GWT.log("DROPPED SOMEWHERE?? INDEX="+index);
//		}
//		List<ModelData> models = event.getData();
//		if (models.size() == 0 || !(models.get(0) instanceof TreeStoreModel)) 
//			return; //No item to be dropped.
//		    	
//		//Get the model of the item being dropped and its position in the parent list.
//		TreeModelItem modelItem = (TreeModelItem)((ModelData)models.get(0)).get("model");
//		int orgpos = modelItem.getParent().indexOf(modelItem);
//		
//		//Check if item the same item but not moved anywhere
//		if(modelItem.getUserObject() == ((TreeModelItem)item.getModel()).getUserObject())
//			return;
//			
//		int newpos = item.getParent().indexOf(item);		
//		boolean moveup = newpos < orgpos;
//		
//		if(moveup){
//			int count = Math.abs(orgpos - newpos);
//			while(count-- > 0)
//				formsTreeView.dragMove(modelItem, FormsTreeView.DRAG_MOVE_UP);
//		}
//		else{
//			int count = Math.abs(newpos - orgpos) + 1;
//			while(count-- > 0)
//				formsTreeView.dragMove(modelItem, FormsTreeView.DRAG_MOVE_DOWN);
//		}
//		
//		TreeModelItem parent = (TreeModelItem)item.getParent().getModel();
//		parent.remove(modelItem);
//		tree.getStore().remove(modelItem);
//		parent.insert(modelItem, moveup ? newpos : newpos+1);
//		
//		super.handleInsertDrop(event, item, index);
//	}
}
