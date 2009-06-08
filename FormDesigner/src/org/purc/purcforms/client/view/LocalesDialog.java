package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author daniel
 *
 */
public class LocalesDialog  extends DialogBox {
	
	public LocalesDialog(){
		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		
		Button btn = new Button(LocaleText.get("close"), new ClickListener() {
		      public void onClick(Widget sender) {
		        hide();
		      }
		    });
		    
		panel.add(btn);
		panel.setCellHorizontalAlignment(btn, HasAlignment.ALIGN_CENTER);
		
		setText(LocaleText.get("languages"));
	}
}
