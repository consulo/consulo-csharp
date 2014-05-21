/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0449 extends AbstractCompilerCheck<CSharpGenericConstraintKeywordValue>
{
	@Override
	public boolean accept(@NotNull CSharpGenericConstraintKeywordValue element)
	{
		if(element.getKeywordElementType() != CSharpTokens.CLASS_KEYWORD && element.getKeywordElementType() != CSharpTokens.STRUCT_KEYWORD)
		{
			return false;
		}

		CSharpGenericConstraint parent = (CSharpGenericConstraint) element.getParent();

		CSharpGenericConstraintValue[] genericConstraintValues = parent.getGenericConstraintValues();
		return genericConstraintValues[0] != element;
	}
}
