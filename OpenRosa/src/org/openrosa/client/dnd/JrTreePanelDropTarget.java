package org.openrosa.client.dnd;

import java.util.List;

import org.openrosa.client.model.TreeModelItem;
import org.openrosa.client.view.FormsTreeView;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;


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
		
		List<ModelData> models = event.getData();
		if (models.size() == 0 || !(models.get(0) instanceof TreeStoreModel)) 
			return; //No item to be dropped.
		    	
		//Get the model of the item being dropped and its position in the parent list.
		TreeModelItem modelItem = (TreeModelItem)((ModelData)models.get(0)).get("model");
		int orgpos = modelItem.getParent().indexOf(modelItem);
		
		//Check if item the same item but not moved anywhere
		if(modelItem.getUserObject() == ((TreeModelItem)item.getModel()).getUserObject())
			return;
			
		int newpos = item.getParent().indexOf(item);		
		boolean moveup = newpos < orgpos;
		
		if(moveup){
			int count = Math.abs(orgpos - newpos);
			while(count-- > 0)
				formsTreeView.dragMoveUp(modelItem);
		}
		else{
			int count = Math.abs(newpos - orgpos) + 1;
			while(count-- > 0)
				formsTreeView.dragMoveDown(modelItem);
		}
		
		TreeModelItem parent = (TreeModelItem)item.getParent().getModel();
		parent.remove(modelItem);
		tree.getStore().remove(modelItem);
		parent.insert(modelItem, moveup ? newpos : newpos+1);
		
		super.handleInsertDrop(event, item, index);
	}
}
