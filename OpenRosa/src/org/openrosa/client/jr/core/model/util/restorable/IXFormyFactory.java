/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openrosa.client.jr.core.model.util.restorable;

import org.openrosa.client.jr.core.model.FormDef;
import org.openrosa.client.jr.core.model.condition.IConditionExpr;
import org.openrosa.client.jr.core.model.data.IAnswerData;
import org.openrosa.client.jr.core.model.instance.FormInstance;
import org.openrosa.client.jr.core.model.instance.TreeReference;
import org.openrosa.client.jr.core.services.transport.payload.IDataPayload;

public interface IXFormyFactory {
	TreeReference ref (String refStr);
	IDataPayload serializeInstance (FormInstance dm);
	FormInstance parseRestore (byte[] data, Class restorableType);
	IAnswerData parseData (String textVal, int dataType, TreeReference ref, FormDef f);
	String serializeData (IAnswerData data);

	//kinda ghetto
	IConditionExpr refToPathExpr (TreeReference ref);
}