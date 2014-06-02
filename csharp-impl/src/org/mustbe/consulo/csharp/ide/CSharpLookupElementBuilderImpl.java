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
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionData;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
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
			array[i] = buildLookupElement(argument);
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
			array[i] = buildLookupElement(argument.getElement());
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
			array[i] = buildLookupElement(elements.get(i));
		}

		return array;
	}

	private LookupElement buildLookupElement(PsiElement element)
	{
		if(element instanceof CSharpMethodDeclaration)
		{
			final CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;
			final DotNetTypeRef[] parameterTypes = methodDeclaration.getParameterTypeRefs();

			String parameterText = "(" + StringUtil.join(parameterTypes, new Function<DotNetTypeRef, String>()
			{
				@Override
				public String fun(DotNetTypeRef parameter)
				{
					return parameter.getPresentableText();
				}
			}, ", ") + ")";

			LookupElementBuilder builder = LookupElementBuilder.create(methodDeclaration);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(methodDeclaration.getReturnTypeRef().getPresentableText());
			builder = builder.withTailText(parameterText, false);
			if(CSharpMethodImplUtil.isExtensionWrapper(methodDeclaration))
			{
				builder = builder.withItemTextUnderlined(true);
			}
			if(!methodDeclaration.isDelegate())
			{
				builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
				{
					@Override
					public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement)
					{
						int offset = insertionContext.getEditor().getCaretModel().getOffset();

						PsiElement elementAt = insertionContext.getFile().findElementAt(offset);
						// dont insert () if it inside method call
						if(elementAt == null || elementAt.getNode().getElementType() != CSharpTokens.LPAR)
						{
							insertionContext.getDocument().insertString(offset, "();");
							int step = 1; // step inside ()
							if(parameterTypes.length == 0)
							{
								step = 3; // if no parameters step out ();
							}
							insertionContext.getEditor().getCaretModel().moveToOffset(offset + step);
							AutoPopupController.getInstance(insertionContext.getProject()).autoPopupParameterInfo(insertionContext.getEditor(), null);

						}
					}
				});
			}
			return builder;
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetVariable dotNetVariable = (DotNetVariable) element;
			LookupElementBuilder builder = LookupElementBuilder.create(dotNetVariable);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(((DotNetVariable) element).toTypeRef(true).getPresentableText());

			return builder;
		}
		else if(element instanceof CSharpMacroDefine)
		{
			LookupElementBuilder builder = LookupElementBuilder.create((CSharpMacroDefine) element);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			return builder;
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			LookupElementBuilder builder = LookupElementBuilder.create(typeDeclaration);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(typeDeclaration.getPresentableParentQName());

			builder = builder.withTailText(DotNetElementPresentationUtil.formatGenericParameters(typeDeclaration), true);

			return builder;
		}
		else
		{
			return CompletionData.objectToLookupItem(element);
		}
	}
}
