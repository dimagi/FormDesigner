package org.openrosa.client.controller;

import java.util.List;

import org.openrosa.client.model.ItextModel;

import com.extjs.gxt.ui.client.store.ListStore;

public interface ITextListener {
	
	void onSaveItext(ListStore<ItextModel> itextrows);
}
