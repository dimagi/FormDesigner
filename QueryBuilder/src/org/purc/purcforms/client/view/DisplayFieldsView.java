package org.purc.purcforms.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.purc.purcforms.client.controller.DisplayColumnActionListener;
import org.purc.purcforms.client.controller.SortColumnActionListener;
import org.purc.purcforms.client.model.DisplayField;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.SortField;
import org.purc.purcforms.client.sql.XmlBuilder;
import org.purc.purcforms.client.widget.ColumnActionHyperlink;
import org.purc.purcforms.client.widget.DisplayColumnWidget;
import org.purc.purcforms.client.widget.SortColumnActionHyperlink;
import org.purc.purcforms.client.widget.SortColumnWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * 
 * @author daniel
 *
 */
public class DisplayFieldsView  extends Composite implements DisplayColumnActionListener,SortColumnActionListener{

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 5;

	private HorizontalPanel horizontalPanel = new HorizontalPanel();
	private VerticalPanel columnPanel = new VerticalPanel();
	private VerticalPanel sortPanel = new VerticalPanel();
	private Anchor addColumnLink = new Anchor("Click to add new column", "#"); //LocaleText.get("????")

	private FormDef formDef;
	private boolean enabled = true;

	HashMap<DisplayColumnWidget,SortColumnWidget> sortColMap = new HashMap<DisplayColumnWidget,SortColumnWidget>();
	HashMap<SortColumnWidget,DisplayColumnWidget> dispColMap = new HashMap<SortColumnWidget,DisplayColumnWidget>();

	public DisplayFieldsView(){
		setupWidgets();
	}

	private void setupWidgets(){
		horizontalPanel.setSpacing(HORIZONTAL_SPACING);
		columnPanel.setSpacing(VERTICAL_SPACING);
		sortPanel.setSpacing(VERTICAL_SPACING);

		horizontalPanel.add(columnPanel);
		horizontalPanel.add(sortPanel);

		columnPanel.add(new Label("Query Columns")); //LocaleText.get("????")
		columnPanel.add(addColumnLink);

		sortPanel.add(new Label("Column Sorting Order")); //LocaleText.get("????")

		initWidget(horizontalPanel);

		addColumnLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				addColumn((Widget)event.getSource());
			}
		});
	}

	public DisplayColumnWidget addColumn(Widget sender){
		DisplayColumnWidget widget = null;

		if(formDef != null && enabled){
			widget = new DisplayColumnWidget(formDef,null,this);

			columnPanel.remove(addColumnLink);
			columnPanel.add(widget);
			columnPanel.add(addColumnLink);
		}

		return widget;
	}

	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		clearConditions();
		addAddColumnLink();
	}

	public void addAddColumnLink(){
		columnPanel.add(addColumnLink);
	}

	private void clearConditions(){
		while(columnPanel.getWidgetCount() > 1)
			columnPanel.remove(columnPanel.getWidget(1));
		
		while(sortPanel.getWidgetCount() > 1)
			sortPanel.remove(sortPanel.getWidget(1));
	}

	public void moveColumnUp(Widget sender){
		moveItemUp(sender instanceof ColumnActionHyperlink ? columnPanel : sortPanel,sender.getParent().getParent());
	}

	private void moveItemUp(VerticalPanel vertialPanel,Widget widget){
		int index = vertialPanel.getWidgetIndex(widget);
		if(index == 1)
			return;
		vertialPanel.remove(widget);
		vertialPanel.insert(widget,index-1);
	}

	public void moveColumnDown(Widget sender){
		moveItemDown(sender instanceof ColumnActionHyperlink ? columnPanel : sortPanel,sender.getParent().getParent());
	}

	private void moveItemDown(VerticalPanel vertialPanel,Widget widget){
		int index = vertialPanel.getWidgetIndex(widget);
		
		if(widget instanceof SortColumnWidget && index == vertialPanel.getWidgetCount() - 1)
			return;
		else if(widget instanceof DisplayColumnWidget && index == vertialPanel.getWidgetCount() - 2)
			return;
		
		vertialPanel.remove(widget);
		vertialPanel.insert(widget,index+1);
	}

	public void showSimpleColum(Widget sender){

	}

	public void showAggregateColumn(Widget sender){

	}

	public void deleteColumn(Widget sender){
		if(sender instanceof ColumnActionHyperlink){
			DisplayColumnWidget displayColumnWidget = (DisplayColumnWidget)sender.getParent().getParent();
			columnPanel.remove(displayColumnWidget);
			dispColMap.remove(sortColMap.remove(displayColumnWidget));
		}
		else{
			SortColumnWidget sortColumnWidget = (SortColumnWidget)sender.getParent().getParent();
			sortPanel.remove(sortColumnWidget);

			DisplayColumnWidget displayColumnWidget = dispColMap.remove(sortColumnWidget);
			sortColMap.remove(displayColumnWidget);
			displayColumnWidget.setSortOrder(SortField.SORT_NULL);
		}
	}

	public void changeSortOrder(Widget sender, int sortOrder){

		if(sender instanceof DisplayColumnWidget){
			DisplayColumnWidget displayColumnWidget = (DisplayColumnWidget)sender;
			SortColumnWidget sortWidget = sortColMap.get(displayColumnWidget);

			if(sortOrder != SortField.SORT_NULL){
				if(sortWidget == null){
					sortWidget = new SortColumnWidget(sortOrder,this);
					sortPanel.add(sortWidget);
					sortColMap.put(displayColumnWidget,sortWidget);
					dispColMap.put(sortWidget, displayColumnWidget);
				}
				else
					sortWidget.setSortOrder(sortOrder);

				sortWidget.setText(displayColumnWidget.getText());
			}
			else if(sortPanel.getWidgetIndex(sortWidget) > -1){
				sortPanel.remove(sortWidget);
				dispColMap.remove(sortColMap.remove(displayColumnWidget));
			}
		}
		else if(sender instanceof SortColumnActionHyperlink){
			SortColumnWidget sortWidget = (SortColumnWidget)sender.getParent().getParent();
			sortWidget.setSortOrder(sortOrder);
			dispColMap.get(sortWidget).setSortOrder(sortOrder);
		}
		else
			dispColMap.get((SortColumnWidget)sender).setSortOrder(sortOrder);
	}

	public void changeDisplayText(Widget sender, String text){
		DisplayColumnWidget displayColumnWidget = (DisplayColumnWidget)sender;
		SortColumnWidget sortWidget = sortColMap.get(displayColumnWidget);
		if(sortWidget != null)
			sortWidget.setText(text);
	}

	public List<DisplayField> getDisplayFields(){

		List<DisplayField> displayFields = new ArrayList<DisplayField>();

		int count = columnPanel.getWidgetCount();
		for(int i=1; i<count; i++){
			Widget widget = columnPanel.getWidget(i);
			if(widget instanceof DisplayColumnWidget){
				DisplayColumnWidget displayColumnWidget = (DisplayColumnWidget)widget;
				displayFields.add(new DisplayField(displayColumnWidget.getName(),displayColumnWidget.getText(),displayColumnWidget.getAggregateFunction(),displayColumnWidget.getDataType()));
			}
		}

		return displayFields;
	}

	public List<SortField> getSortFields(){

		List<SortField> sortFields = new ArrayList<SortField>();

		int count = sortPanel.getWidgetCount();
		for(int i=1; i<count; i++){
			DisplayColumnWidget displayColumnWidget = dispColMap.get(sortPanel.getWidget(i));
			sortFields.add(new SortField(displayColumnWidget.getName(),displayColumnWidget.getSortOrder()));
		}

		return sortFields;
	}
	
	public void loadQueryDef(String xml){
		Document doc = XMLParser.parse(xml);
		Element rootNode = doc.getDocumentElement();
		if(!rootNode.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_QUERYDEF))
			return;
		
		HashMap<String,DisplayColumnWidget> displayCols = new HashMap<String,DisplayColumnWidget>();
		
		NodeList nodes = rootNode.getElementsByTagName(XmlBuilder.NODE_NAME_DISPLAY_FIELDS);
		if(nodes != null && nodes.getLength() > 0)
			loadDisplayFields((Element)nodes.item(0),displayCols);
		
		nodes = rootNode.getElementsByTagName(XmlBuilder.NODE_NAME_SORT_FIELDS);
		if(nodes != null && nodes.getLength() > 0)
			loadSortFields((Element)nodes.item(0),displayCols);
	}
	
	private HashMap<String,DisplayColumnWidget> loadDisplayFields(Element rootNode,HashMap<String,DisplayColumnWidget> displayCols){
		
		NodeList nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_FIELD)){
				Element element = (Element)node;
				DisplayColumnWidget widget = addColumn(this);
				widget.setName(element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_NAME));
				widget.setText(element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_TEXT));
				widget.setAggregateFunction(element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_AGG_FUNC));
				
				displayCols.put(widget.getName(), widget);
			}
		}
		
		return displayCols;
	}
	
	private void loadSortFields(Element rootNode,HashMap<String,DisplayColumnWidget> displayCols){
		NodeList nodes = rootNode.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++){
			Node node = nodes.item(index);
			if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(XmlBuilder.NODE_NAME_FIELD)){
				Element element = (Element)node;
				DisplayColumnWidget widget = displayCols.get(element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_NAME));
				int sortOrder = Integer.parseInt(element.getAttribute(XmlBuilder.ATTRIBUTE_NAME_SORT_ORDER));
				widget.setSortOrder(sortOrder);
				changeSortOrder(widget,sortOrder);
			}
		}
	}
}
