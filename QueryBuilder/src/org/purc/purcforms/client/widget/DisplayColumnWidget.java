package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.controller.AggregateFunctionListener;
import org.purc.purcforms.client.controller.DisplayColumnActionListener;
import org.purc.purcforms.client.controller.ItemSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.QuestionDef;
import org.purc.purcforms.client.model.SortField;

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
	private FieldNameWidget fieldNameWidget;
	private HorizontalPanel horizontalPanel;
	private ColumnActionHyperlink actionHyperlink;
	private AggregateFunctionHyperlink aggFuncHyperlink;

	private QuestionDef questionDef;
	private int sortOrder;
	private DisplayColumnActionListener view;
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
		fieldNameWidget = new FieldNameWidget(this);
		
		sortHyperlink = new SortHyperlink(SortHyperlink.SORT_TEXT_NOT_SORTED,null,this,true);

		horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		horizontalPanel.add(actionHyperlink);

		horizontalPanel.add(fieldWidget);

		horizontalPanel.add(new Label("as")); //LocaleText.get("???");
		horizontalPanel.add(fieldNameWidget);
		horizontalPanel.add(new Label("sort:")); //LocaleText.get("???");
		horizontalPanel.add(sortHyperlink);

		initWidget(horizontalPanel);

		fieldWidget.setFormDef(formDef);

		sortOrder = SortField.SORT_ASCENDING;
	}
	
	public void onItemSelected(Object sender, Object item) {
		if(sender == fieldWidget){
			questionDef = (QuestionDef)item;
			fieldNameWidget.setValue(questionDef.getText());
			aggFuncHyperlink.setQuestionDef(questionDef);
		}
		else if(sender == fieldWidget){
			
		}
		else if(sender == sortHyperlink){
			sortOrder = ((Integer)item).intValue();
			fieldWidget.stopSelection();
			view.changeSortOrder(this,sortOrder);
		}
		else if(sender == fieldNameWidget)
			view.changeDisplayText(this, (String)item);
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
	
	public String getText(){
		return fieldNameWidget.getValue();
	}
	
	public String getName(){
		return questionDef.getBinding();
	}
	
	public int getSortOrder(){
		return sortOrder;
	}
	
	public void setSortOrder(int sortOrder){
		this.sortOrder = sortOrder;
		sortHyperlink.setSortOrder(sortOrder);
	}
	
	public String getAggregateFunction(){
		if(horizontalPanel.getWidgetIndex(aggFuncHyperlink) > -1)
			return aggFuncHyperlink.getAggregateFunction();
		
		return null;
	}
	
	public void changeDisplayText(Widget sender, String text){
		view.changeDisplayText(sender, text);
	}
	
	public void setName(String name){
		questionDef = formDef.getQuestion(name);
		fieldWidget.setQuestion(questionDef);
		aggFuncHyperlink.setQuestionDef(questionDef);
	}
	
	public void setText(String text){
		fieldNameWidget.setValue(text);
	}
	
	public void setAggregateFunction(String aggFunc){
		if(aggFunc == null || aggFunc.trim().length() == 0)
			return;
		
		aggFuncHyperlink.setAggregateFunction(aggFunc);
		showAggregateFunctions(true);
		actionHyperlink.setAggregateFunctionMode(true);
	}
	
	public int getDataType(){
		return questionDef.getDataType();
	}
}
