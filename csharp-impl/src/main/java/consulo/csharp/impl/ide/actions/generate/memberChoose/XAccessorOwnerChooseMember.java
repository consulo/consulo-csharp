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

package consulo.csharp.impl.ide.actions.generate.memberChoose;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpXAccessorOwner;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.psi.PsiElement;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.function.PairConsumer;
import jakarta.annotation.Nonnull;

import java.util.Locale;

/**
 * @author VISTALL
 * @since 02.04.2016
 */
public class XAccessorOwnerChooseMember extends ImplementMemberChooseObject<CSharpXAccessorOwner>
{
	public XAccessorOwnerChooseMember(CSharpXAccessorOwner declaration,
									  PairConsumer<PsiElement, StringBuilder> additionalModifiersAppender,
									  PairConsumer<PsiElement, StringBuilder> returnAppender,
									  boolean canGenerateBlock)
	{
		super(declaration, additionalModifiersAppender, returnAppender, canGenerateBlock);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	@RequiredUIAccess
	public String getPresentationText()
	{
		if(myDeclaration instanceof CSharpPropertyDeclaration)
		{
			return CSharpElementPresentationUtil.formatProperty((DotNetPropertyDeclaration) myDeclaration, 0);
		}
		else if(myDeclaration instanceof CSharpIndexMethodDeclaration)
		{
			return CSharpElementPresentationUtil.formatMethod((CSharpIndexMethodDeclaration) myDeclaration, CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE | CSharpElementPresentationUtil
					.METHOD_PARAMETER_NAME);
		}
		throw new UnsupportedOperationException(myDeclaration.getClass().getSimpleName() + " is not supported");
	}

	@Override
	@RequiredUIAccess
	public String getText()
	{
		StringBuilder builder = new StringBuilder();
		CSharpAccessModifier modifier = CSharpAccessModifier.findModifier(myDeclaration);
		boolean canGenerateCodeBlock = myCanGenerateBlock;
		if(modifier != CSharpAccessModifier.NONE && canGenerateCodeBlock)
		{
			builder.append(modifier.getPresentableText()).append(" ");
		}

		builder.append(getPresentationText());
		builder.append(" {\n");
		for(DotNetXAccessor accessor : myDeclaration.getAccessors())
		{
			DotNetXAccessor.Kind accessorKind = accessor.getAccessorKind();
			if(accessorKind == null)
			{
				continue;
			}
			builder.append(accessorKind.name().toLowerCase(Locale.US));
			if(myCanGenerateBlock)
			{
				builder.append(" {\n");
				myReturnAppender.consume(accessor, builder);
				builder.append("}");
			}
			else
			{
				builder.append(";");
			}
		}
		builder.append("}");
		return builder.toString();
	}
}
