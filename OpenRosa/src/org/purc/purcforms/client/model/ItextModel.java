package org.purc.purcforms.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ItextModel extends BaseModel {

	public ItextModel(){
		
	}
	
	public ItextModel(String id, String defaultVal, String english){
		setId(id);
		setDefault(defaultVal);
		setEnglish(english);
	}
	
	public void setId(String id) {
        set("id", id);
    }
    
	public void setDefault(String defaultVal) {
        set("default", defaultVal);
    }
    
    public void setEnglish(String english) {
        set("en", english);
    }
}
