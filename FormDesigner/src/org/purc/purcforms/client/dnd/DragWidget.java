package org.purc.purcforms.client.dnd;

import com.google.gwt.user.client.ui.Label;


/**
 * 
 * @author daniel
 *
 */
public class DragWidget extends Label {
    public boolean selected;
    public Object value;

    public DragWidget(String label, Object value, boolean selected){
            super(label);
            this.value = value;
            this.selected = selected;
    }

    public DragWidget(DragWidget drag){
            super(drag.getText());
            this.selected = drag.selected;
            this.value = drag.value;
    } 
}
