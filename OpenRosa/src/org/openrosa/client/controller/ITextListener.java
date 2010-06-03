package org.openrosa.client.controller;

import java.util.List;

import org.openrosa.client.model.ItextModel;

public interface ITextListener {
	
	void onSaveItext(List<ItextModel> itext);
}
