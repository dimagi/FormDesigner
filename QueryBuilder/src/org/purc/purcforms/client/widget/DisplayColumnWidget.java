package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.AggregateFunctionListener;
import org.purc.purcforms.client.controller.DisplayColumnActionListener;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QueryBuilderConstants;
import org.purc.purcforms.client.model.QuestionDef;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class DisplayColumnWidget extends Composite implements ItemSelectionListener, AggregateFunctionListener, DisplayColumnActionListener{

	private static final int HORIZONTAL_SPACING = 5;

	private FormDef formDef;
	private FieldWidget fieldWidget;
	private SortHyperlink sortHyperlink;
	private FieldNameWidget fieldNameWidget = new FieldNameWidget();
	private HorizontalPanel horizontalPanel;
	private ColumnActionHyperlink actionHyperlink;
	private AggregateFunctionHyperlink aggFuncHyperlink;

	private QuestionDef questionDef;
	private int sortOrder;
	private DisplayColumnActionListener view;
	//private Condition condition;
	private Label lbLabel = new Label(LocaleText.get("value"));
	private Label lbOf = new Label("of"); //LocaleText.get("???")
	
	public DisplayColumnWidget(FormDef formDef,QuestionDef questionDef,DisplayColumnActionListener view){
		this.formDef = formDef;
		this.questionDef = questionDef;
		this.view = view;
		
		setupWidgets();
	}
	
	private void setupWidgets(){
		actionHyperlink = new ColumnActionHyperlink("<>",null,this);
		aggFuncHyperlink = new AggregateFunctionHyperlink(AggregateFunctionHyperlink.FUNC_TEXT_COUNT,null,this);
		fieldWidget = new FieldWidget(this);

		sortHyperlink = new SortHyperlink(SortHyperlink.SORT_TEXT_NOT_SORTED,null,this);

		horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);

		if(true)
			horizontalPanel.add(fieldWidget);
		else{
			if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
				lbLabel.setText(LocaleText.get("count"));
			horizontalPanel.add(lbLabel);
		}

		horizontalPanel.add(new Label("as")); //LocaleText.get("???");
		horizontalPanel.add(fieldNameWidget);
		horizontalPanel.add(new Label("sort:")); //LocaleText.get("???");
		horizontalPanel.add(sortHyperlink);

		initWidget(horizontalPanel);

		fieldWidget.setFormDef(formDef);

		sortOrder = QueryBuilderConstants.SORT_ASCENDING;
	}
	
	public void onItemSelected(Object sender, Object item) {
		if(sender == fieldWidget){
			questionDef = (QuestionDef)item;
			fieldNameWidget.setValue(questionDef.getText());
			aggFuncHyperlink.setQuestionDef(questionDef);
		}
		else if(sender == sortHyperlink){
			sortOrder = ((Integer)item).intValue();
			fieldWidget.stopSelection();
			view.changeSortOrder(this,sortOrder);
		}
	}

	public void onStartItemSelection(Object sender){
		if(sender != fieldNameWidget)
			fieldNameWidget.stopEdit(false); //Temporary hack to turn off edits when focus goes off the edit widget

		if(sender != fieldWidget)
			fieldWidget.stopSelection();
	}
	
	public void showAggregateFunctions(boolean show){
		if(show){
			horizontalPanel.insert(lbOf,1);
			horizontalPanel.insert(aggFuncHyperlink,1);
		}
		else{
			horizontalPanel.remove(aggFuncHyperlink);
			horizontalPanel.remove(lbOf);
		}
	}
	
	public void onSum(Widget sender){
		
	}
	
	public void onAverage(Widget sender){
		
	}
	
	public void onMinimum(Widget sender){
		
	}
	
	public void onMaximum(Widget sender){
		
	}
	
	public void onCount(Widget sender){
		
	}
	
	public void moveColumnUp(Widget sender){
		view.moveColumnUp(sender);
	}
	
	public void moveColumnDown(Widget sender){
		view.moveColumnDown(sender);
	}
	
	public void deleteColumn(Widget sender){
		view.deleteColumn(sender);
	}
	
	public void showSimpleColum(Widget sender){
		showAggregateFunctions(false);
	}
	
	public void showAggregateColumn(Widget sender){
		showAggregateFunctions(true);
	}
	
	public void changeSortOrder(Widget sender, int sortOrder){
		view.changeSortOrder(this,sortOrder);
	}
}
