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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpAsExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0413 extends CompilerCheck<PsiElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull PsiElement element)
	{
		if(element instanceof CSharpAsExpressionImpl)
		{
			CSharpAsExpressionImpl asExpression = (CSharpAsExpressionImpl) element;
			DotNetTypeRef typeRef = asExpression.toTypeRef(false);

			PsiElement resolve = typeRef.resolve().getElement();
			if(!(resolve instanceof DotNetGenericParameter))
			{
				return null;
			}

			DotNetGenericParameterListOwner parent = PsiTreeUtil.getParentOfType(resolve, DotNetGenericParameterListOwner.class);
			if(!(parent instanceof CSharpGenericConstraintOwner))
			{
				return null;
			}

			boolean findReferenceOrClass = false;
			final CSharpGenericConstraint constraint = CSharpGenericConstraintUtil.forParameter((CSharpGenericConstraintOwner) parent, (DotNetGenericParameter) resolve);
			if(constraint != null)
			{
				for(CSharpGenericConstraintValue value : constraint.getGenericConstraintValues())
				{
					if(value instanceof CSharpGenericConstraintKeywordValue && ((CSharpGenericConstraintKeywordValue) value).getKeywordElementType() == CSharpTokens.CLASS_KEYWORD || value instanceof
							CSharpGenericConstraintTypeValue)
					{
						findReferenceOrClass = true;
						break;
					}
				}
			}

			if(!findReferenceOrClass)
			{
				return newBuilder(asExpression.getAsKeyword(), "as", ((DotNetGenericParameter) resolve).getName());
			}
		}
		return null;
	}
}
