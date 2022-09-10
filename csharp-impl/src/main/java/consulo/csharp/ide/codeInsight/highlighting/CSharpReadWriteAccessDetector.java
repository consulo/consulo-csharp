/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.ide.codeInsight.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.editor.highlight.ReadWriteAccessDetector;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;

/**
 * @author VISTALL
 * @since 15.04.2015
 */
@ExtensionImpl
public class CSharpReadWriteAccessDetector extends ReadWriteAccessDetector
{
	@Override
	public boolean isReadWriteAccessible(PsiElement element)
	{
		if(element instanceof DotNetVariable && element.getLanguage() == CSharpLanguage.INSTANCE)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isDeclarationWriteAccess(PsiElement element)
	{
		if(element instanceof DotNetVariable)
		{
			return ((DotNetVariable) element).getInitializer() != null;
		}
		return false;
	}

	@Override
	public Access getReferenceAccess(PsiElement referencedElement, PsiReference reference)
	{
		if(reference instanceof DotNetExpression)
		{
			Access expressionAccess = getExpressionAccess((PsiElement) reference);
			if(expressionAccess != null)
			{
				return expressionAccess;
			}
		}
		return null;
	}

	@Override
	public Access getExpressionAccess(PsiElement expression)
	{
		if(expression instanceof CSharpReferenceExpression)
		{
			PsiElement parent = expression.getParent();
			if(parent instanceof CSharpAssignmentExpressionImpl)
			{
				return ((CSharpAssignmentExpressionImpl) parent).getParameterExpressions()[0] == expression ? Access.Write : Access.Read;
			}
			else if(parent instanceof CSharpNamedFieldOrPropertySet)
			{
				return ((CSharpNamedFieldOrPropertySet) parent) .getNameElement() == expression ? Access.Write : Access.Read;
			}
			return Access.Read;
		}
		return null;
	}
}
