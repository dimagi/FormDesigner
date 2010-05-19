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

package org.openrosa.client.jr.xpath.expr;

import java.io.IOException;

import org.openrosa.client.java.io.DataInputStream;
import org.openrosa.client.java.io.DataOutputStream;
import org.openrosa.client.jr.core.model.condition.EvaluationContext;
import org.openrosa.client.jr.core.model.instance.FormInstance;
import org.openrosa.client.jr.core.util.externalizable.DeserializationException;
import org.openrosa.client.jr.core.util.externalizable.PrototypeFactory;
import org.openrosa.client.jr.xpath.XPathUnsupportedException;

public class XPathUnionExpr extends XPathBinaryOpExpr {
	public XPathUnionExpr () { } //for deserialization

	public XPathUnionExpr (XPathExpression a, XPathExpression b) {
		super(a, b);
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		throw new XPathUnsupportedException("nodeset union operation");
	}

	public String toString () {
		return super.toString("union");
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathUnionExpr) {
			return super.equals(o);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
	}
}
