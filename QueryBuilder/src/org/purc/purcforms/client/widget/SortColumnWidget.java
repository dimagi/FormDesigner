package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * 
 * @author daniel
 *
 */
public class SortColumnWidget extends Composite{

	private HorizontalPanel horizontalPanel = new HorizontalPanel();
	private DisplayColumnWidget displayColumnWidget;
	
	public SortColumnWidget(DisplayColumnWidget displayColumnWidget){
		this.displayColumnWidget = displayColumnWidget;
		
		setupWidgets();
	}
	
	private void setupWidgets(){
		/*actionHyperlink = new ColumnActionHyperlink("<>",null,this);
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

		fieldWidget.setFormDef(formDef);

		sortOrder = QueryBuilderConstants.SORT_ASCENDING;*/
		
		initWidget(horizontalPanel);
	}
}
