package org.purc.purcforms.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;


/**
 * 
 * @author daniel
 *
 */
public class VideoWidget extends Composite {

    private HTML html;

    public VideoWidget(String videoUrl, final int pixelWidth, final int pixelHeight) {
    	html = new HTML();                 
    	initWidget(html);

        StringBuffer sb = new StringBuffer();
        sb.append("<object width=\""+ pixelWidth + "\" height=\""+ pixelHeight + "\" type=\"video/x-ms-wmv\">");                 
        sb.append("<param value=\"1\" name=\"ShowStatusBar\"/>");                 
        sb.append("<param value=\""+ videoUrl + "\" name=\"src\"/>");                 
        sb.append("<param value=\"1\" name=\"autostart\"/>");                 
        sb.append("<param value=\"0\" name=\"volume\"/>");                 
        sb.append("</object>");                 
        sb.append("<br/>");                 
        html.setHTML(sb.toString());

        setPixelSize(pixelWidth, pixelHeight);
        html.setPixelSize(pixelWidth, pixelHeight);
    } 

}
