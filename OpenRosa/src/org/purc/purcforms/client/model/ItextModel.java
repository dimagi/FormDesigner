package org.purc.purcforms.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ItextModel extends BaseModel {

	public ItextModel(){
		
	}
	
	public void setId(String id) {
        set("id", id);
    }
    
	public void setDefault(String defaultVal) {
        set("default", defaultVal);
    }
    
    public void setEnglish(String english) {
        set("english", english);
    }
}
