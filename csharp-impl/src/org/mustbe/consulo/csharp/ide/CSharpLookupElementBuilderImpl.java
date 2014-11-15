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

package org.mustbe.consulo.csharp.ide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.proximity.PsiProximityComparator;
import com.intellij.util.Function;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLookupElementBuilderImpl extends CSharpLookupElementBuilder
{
	@NotNull
	@Override
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull PsiElement[] arguments)
	{
		if(arguments.length == 0)
		{
			return LookupElement.EMPTY_ARRAY;
		}
		if(sender != null)
		{
			Arrays.sort(arguments, new PsiProximityComparator(sender));
		}

		LookupElement[] array = new LookupElement[arguments.length];
		for(int i = 0; i < arguments.length; i++)
		{
			PsiElement argument = arguments[i];
			array[i] = buildLookupElementImpl(argument);
		}
		return array;
	}

	@NotNull
	@Override
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull ResolveResult[] arguments)
	{
		if(arguments.length == 0)
		{
			return LookupElement.EMPTY_ARRAY;
		}

		//FIXME [VISTALL] sorter?

		LookupElement[] array = new LookupElement[arguments.length];
		for(int i = 0; i < arguments.length; i++)
		{
			ResolveResult argument = arguments[i];
			array[i] = buildLookupElementImpl(argument.getElement());
		}
		return array;
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull Collection<? extends PsiElement> arguments)
	{
		if(arguments.isEmpty())
		{
			return LookupElement.EMPTY_ARRAY;
		}
		List<? extends PsiElement> elements;
		if(arguments instanceof List)
		{
			elements = (List<? extends PsiElement>)arguments;
		}
		else
		{
			elements = new ArrayList<PsiElement>(arguments);
		}

		if(sender != null)
		{
			Collections.sort(elements, new PsiProximityComparator(sender));
		}

		LookupElement[] array = new LookupElement[arguments.size()];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = buildLookupElementImpl(elements.get(i));
		}

		return array;
	}

	@NotNull
	public LookupElement buildLookupElementImpl(PsiElement element)
	{
		LookupElementBuilder builder = buildLookupElement(element);
		if(builder == null)
		{
			throw new IllegalArgumentException("Element " + element.getClass().getSimpleName() + " is not handled");
		}
		if(DotNetAttributeUtil.hasAttribute(element, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);

			return PrioritizedLookupElement.withPriority(builder, -1.0);
		}

		return builder;
	}

	@Nullable
	@Override
	public LookupElementBuilder buildLookupElement(PsiElement element)
	{
		LookupElementBuilder builder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			final CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

			builder = LookupElementBuilder.create(methodDeclaration);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			if(!methodDeclaration.isDelegate())
			{
				final DotNetTypeRef[] parameterTypes = methodDeclaration.getParameterTypeRefs();

				String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element);

				String parameterText = genericText + "(" + StringUtil.join(parameterTypes, new Function<DotNetTypeRef, String>()
				{
					@Override
					public String fun(DotNetTypeRef parameter)
					{
						return parameter.getPresentableText();
					}
				}, ", ") + ")";

				builder = builder.withInsertHandler(ParenthesesInsertHandler.getInstance(parameterTypes.length > 0));

				if(CSharpMethodImplUtil.isExtensionWrapper(methodDeclaration))
				{
					builder = builder.withItemTextUnderlined(true);
				}
				builder = builder.withTypeText(methodDeclaration.getReturnTypeRef().getPresentableText());
				builder = builder.withTailText(parameterText, false);
			}
			else
			{
				builder = builder.withTailText(DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element), true);

				builder = withGenericInsertHandler(element, builder);
			}
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			DotNetNamespaceAsElement namespaceAsElement = (DotNetNamespaceAsElement) element;
			builder = LookupElementBuilder.create(namespaceAsElement.getName());

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			CSharpTypeDefStatement typeDefStatement = (CSharpTypeDefStatement) element;
			builder = LookupElementBuilder.create(typeDefStatement.getName());

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			builder = builder.withTypeText(typeDefStatement.toTypeRef().getPresentableText());
		}
		else if(element instanceof DotNetGenericParameter)
		{
			DotNetGenericParameter typeDefStatement = (DotNetGenericParameter) element;
			builder = LookupElementBuilder.create(typeDefStatement.getName());

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetVariable dotNetVariable = (DotNetVariable) element;
			builder = LookupElementBuilder.create(dotNetVariable);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(dotNetVariable.toTypeRef(true).getPresentableText());
		}
		else if(element instanceof CSharpMacroDefine)
		{
			builder = LookupElementBuilder.create((CSharpMacroDefine) element);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			builder = LookupElementBuilder.create(typeDeclaration);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(typeDeclaration.getPresentableParentQName());

			builder = builder.withTailText(DotNetElementPresentationUtil.formatGenericParameters(typeDeclaration), true);

			builder = withGenericInsertHandler(element, builder);
		}

		return builder;
	}

	private static LookupElementBuilder withGenericInsertHandler(PsiElement element, LookupElementBuilder builder)
	{
		if(!(element instanceof DotNetGenericParameterListOwner))
		{
			return builder;
		}

		int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
		if(genericParametersCount == 0)
		{
			return builder;
		}

		builder = builder.withInsertHandler(LtGtInsertHandler.getInstance(true));
		return builder;
	}
}
