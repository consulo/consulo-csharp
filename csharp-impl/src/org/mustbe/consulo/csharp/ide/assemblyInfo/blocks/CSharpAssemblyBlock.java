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

package org.mustbe.consulo.csharp.ide.assemblyInfo.blocks;

import javax.swing.JComponent;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDummyDeclarationImpl;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public abstract class CSharpAssemblyBlock
{
	private final String myTitle;
	private final String myAttributeType;

	public CSharpAssemblyBlock(String title, String attributeType)
	{
		myTitle = title;
		myAttributeType = attributeType;
	}

	public abstract JComponent createAndLoadComponent(PsiFile file, boolean mutable);

	@Nullable
	public <T> T getValue(PsiFile file, Class<T> clazz)
	{
		CSharpDummyDeclarationImpl childOfAnyType = PsiTreeUtil.findChildOfAnyType(file, CSharpDummyDeclarationImpl.class);
		if(childOfAnyType == null)
		{
			return null;
		}

		DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(childOfAnyType, myAttributeType);
		if(attribute == null)
		{
			return null;
		}

		if(!(attribute instanceof CSharpAttribute))
		{
			return null;
		}

		DotNetExpression[] parameterExpressions = ((CSharpAttribute) attribute).getParameterExpressions();
		if(parameterExpressions.length == 0)
		{
			return null;
		}
		return new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(clazz);
	}

	public String getTitle()
	{
		return myTitle;
	}

	public String getAttributeType()
	{
		return myAttributeType;
	}
}
