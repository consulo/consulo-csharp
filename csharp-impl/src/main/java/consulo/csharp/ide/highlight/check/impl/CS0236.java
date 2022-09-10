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
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 02-Nov-17
 */
public class CS0236 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpReferenceExpression element)
	{
		if(element.kind() == CSharpReferenceExpression.ResolveToKind.FIELD_OR_PROPERTY)
		{
			return null;
		}

		CSharpFieldDeclaration fieldDeclaration = PsiTreeUtil.getParentOfType(element, CSharpFieldDeclaration.class);
		if(fieldDeclaration == null)
		{
			return null;
		}

		DotNetExpression initializer = fieldDeclaration.getInitializer();
		if(initializer == null || !initializer.getTextRange().contains(element.getTextRange()))
		{
			return null;
		}

		PsiElement parent = fieldDeclaration.getParent();
		if(!(parent instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		PsiElement target = element.resolve();
		if(PsiTreeUtil.getParentOfType(element, DotNetType.class) != null || target instanceof CSharpTypeDeclaration)
		{
			return null;
		}

		if(target instanceof DotNetModifierListOwner && !((DotNetModifierListOwner) target).hasModifier(CSharpModifier.STATIC) && parent.isEquivalentTo(target.getParent()))
		{
			return newBuilder(element, formatElement(target));
		}
		return null;
	}
}
