package org.purc.purcforms.client.dnd;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class DragSelectWidget extends Composite {

    private final VerticalPanel fromPanel = new VerticalPanel();
    private final VerticalPanel toPanel = new VerticalPanel();
    private DragWidget dragWidgets[];
    private final DockPanel outer = new DockPanel();
    private PopupPanel dragPanel;
    private DragWidget dragWidget;
    private MouseListener dragListener = new DragListener();

    public DragSelectWidget(){
            draw();
    }

    public DragSelectWidget(DragWidget[] widgets){
            draw();
            setDragWidgets(widgets);
    }

    private void draw(){
            initWidget(outer);
            outer.add(fromPanel,DockPanel.WEST);
            outer.add(toPanel, DockPanel.EAST);
            fromPanel.setStyleName("DragContainer");
            toPanel.setStyleName("DragContainer");
            outer.setCellHorizontalAlignment(fromPanel,HasAlignment.ALIGN_LEFT);
            outer.setCellHorizontalAlignment(toPanel, HasAlignment.ALIGN_RIGHT);
            outer.setWidth("100%");
    }

    private void drop(int x, int y){
            if(dragWidget != null && !dragWidget.selected){
                    int toLeft = toPanel.getAbsoluteLeft();
                    int toTop = toPanel.getAbsoluteTop();
                    if((x > toLeft && x < (toLeft + toPanel.getOffsetWidth())) &&
                       (y > toTop  && y < (toTop  + toPanel.getOffsetHeight()))) {
                            for(int i = 0; i < toPanel.getWidgetCount(); i++){
                                    if(toPanel.getWidget(i) instanceof HTML) {
                                            toPanel.remove(toPanel.getWidget(i));
                                            DragWidget dropWidget = new DragWidget(dragWidget);
                                            dropWidget.addMouseListener(dragListener);
                                            dropWidget.selected = true;
                                            toPanel.insert(dropWidget, i);
                                            break;
                                    }
                            }
                            dragWidget.removeMouseListener(dragListener);
                            dragWidget.setStyleName("DragItemInactive");
                            dragWidget.selected = true;
                    }
            }
            if(dragWidget != null && dragWidget.selected){
                    int fromLeft = fromPanel.getAbsoluteLeft();
                    int fromTop = fromPanel.getAbsoluteTop();
                    if((x > fromLeft && x < (fromLeft + fromPanel.getOffsetWidth())) &&
                       (y > fromTop  && y < (fromTop  + fromPanel.getOffsetHeight()))) {
                            for(int i = 0; i < fromPanel.getWidgetCount(); i++){
                                    DragWidget fromWidget = (DragWidget)fromPanel.getWidget(i);
                                    if(fromWidget.value == dragWidget.value) {
                                            toPanel.remove(dragWidget);
                                            toPanel.add(new HTML("&nbsp;"));
                                            fromWidget.addMouseListener(dragListener);
                                            fromWidget.removeStyleName("DragItemInactive");
                                            fromWidget.selected = false;
                                            break;
                                    }
                            }
                    }
            }
    }

    public DragWidget[] getDragWidgets(){
            return dragWidgets;
    }

    public void setDragWidgets(DragWidget[] dragWidgets) {
            this.dragWidgets = dragWidgets;
            fromPanel.clear();
            toPanel.clear();
            for(int i=0; i < dragWidgets.length; i++){
                    fromPanel.add(dragWidgets[i]);
                    if(dragWidgets[i].selected){
                            DragWidget toWidget = new DragWidget(dragWidgets[i]);
                            toWidget.addMouseListener(dragListener);
                            toPanel.insert(toWidget,0);
                            dragWidgets[i].setStyleName("DragItemInactive");
                    }else{
                            dragWidgets[i].addMouseListener(dragListener);
                            toPanel.add(new HTML("&nbsp;"));
                    }
            }
    }

    private class DragListener extends MouseListenerAdapter {

            public int xpos;
            public int ypos;
            public boolean dragging;

            public void onMouseDown(Widget sender, int x, int y){
                    xpos = x;
                    ypos = y;
                    dragPanel = new PopupPanel();
                    dragWidget = (DragWidget)sender;
                    HTML dragHTML = new HTML(dragWidget.getText());
                    //HTML dragHTML = new HTML(dragWidget.getElement().getInnerHTML());
                    dragHTML.addMouseListener(dragListener);
                    dragPanel.add(dragHTML);
                    dragging = true;
                    dragPanel.setPopupPosition(sender.getAbsoluteLeft(),sender.getAbsoluteTop());
                    dragPanel.show();
            }

            public void onMouseUp(Widget sender, int x, int y){
                    int absX = x + ((PopupPanel)sender.getParent()).getAbsoluteLeft();
                int absY = y + ((PopupPanel)sender.getParent()).getAbsoluteTop();
                    drop(absX,absY);
                dragging = false;
                ((PopupPanel)sender.getParent()).hide();
            }

            public void onMouseMove(Widget sender, int x, int y){
            if (dragging) {
                          int absX = x +
((PopupPanel)sender.getParent()).getAbsoluteLeft();
                          int absY = y +
((PopupPanel)sender.getParent()).getAbsoluteTop();
                          ((PopupPanel)sender.getParent()).setPopupPosition(absX - xpos,
absY - ypos);
                     }
            }
    } 
}
