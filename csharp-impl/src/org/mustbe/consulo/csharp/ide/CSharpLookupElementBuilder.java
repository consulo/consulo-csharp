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
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.completion.CSharpCompletionSorting;
import org.mustbe.consulo.csharp.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import org.mustbe.consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLookupElementBuilder
{
	@NotNull
	@RequiredReadAction
	public static LookupElement[] buildToLookupElements(@NotNull PsiElement[] arguments)
	{
		if(arguments.length == 0)
		{
			return LookupElement.EMPTY_ARRAY;
		}

		List<LookupElement> list = new ArrayList<LookupElement>(arguments.length);
		for(PsiElement argument : arguments)
		{
			ContainerUtil.addIfNotNull(list, buildLookupElementWithContextType(argument, null, DotNetGenericExtractor.EMPTY, null));
		}
		return list.toArray(new LookupElement[list.size()]);
	}

	@Nullable
	@RequiredReadAction
	public static LookupElement buildLookupElementWithContextType(final PsiElement element,
			@Nullable final CSharpTypeDeclaration contextType,
			@NotNull DotNetGenericExtractor extractor,
			@Nullable PsiElement expression)
	{
		LookupElementBuilder builder = createLookupElementBuilder(element, extractor, expression);
		if(builder == null)
		{
			return null;
		}
		if(contextType != null && contextType.isEquivalentTo(element.getParent()))
		{
			LookupElementBuilder oldBuilder = builder;
			// don't bold lookup like '[int key]' looks ugly
			if(!(element instanceof CSharpIndexMethodDeclaration))
			{
				builder = oldBuilder.bold();
			}
			CSharpCompletionSorting.copyForce(oldBuilder, builder);
		}

		if(CSharpPsiUtilImpl.isTypeLikeElement(element))
		{
			return CSharpTypeLikeLookupElement.create(builder, extractor, expression);
		}
		return builder;
	}

	@RequiredReadAction
	public static LookupElementBuilder createLookupElementBuilder(@NotNull final PsiElement element, @NotNull DotNetGenericExtractor extractor, @Nullable final PsiElement completionParent)
	{
		LookupElementBuilder builder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			final CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

			if(!methodDeclaration.isDelegate())
			{
				String name = methodDeclaration.getName();
				if(name == null)
				{
					return null;
				}

				CSharpMethodUtil.Result inheritGeneric = CSharpMethodUtil.isCanInheritGeneric(methodDeclaration);

				String lookupString = inheritGeneric == CSharpMethodUtil.Result.CAN ? name + "<>()" : name;
				builder = LookupElementBuilder.create(methodDeclaration, lookupString);
				builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

				final CSharpSimpleParameterInfo[] parameterInfos = methodDeclaration.getParameterInfos();

				String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element);

				String parameterText = genericText + "(" + StringUtil.join(parameterInfos, new Function<CSharpSimpleParameterInfo, String>()
				{
					@Override
					@RequiredReadAction
					public String fun(CSharpSimpleParameterInfo parameter)
					{
						return CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef(), element) + " " + parameter.getNotNullName();
					}
				}, ", ") + ")";

				if(inheritGeneric == CSharpMethodUtil.Result.CAN)
				{
					builder = builder.withPresentableText(name);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							CaretModel caretModel = context.getEditor().getCaretModel();
							caretModel.moveToOffset(caretModel.getOffset() - 3);
						}
					});
				}
				else
				{
					builder = builder.withInsertHandler(new CSharpParenthesesWithSemicolonInsertHandler(methodDeclaration));
				}

				if(CSharpMethodImplUtil.isExtensionWrapper(methodDeclaration))
				{
					builder = builder.withItemTextUnderlined(true);
				}
				builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(methodDeclaration.getReturnTypeRef(), element));
				builder = builder.withTailText(parameterText, false);
			}
			else
			{
				builder = buildTypeLikeElement((CSharpMethodDeclaration) element, extractor);
			}
		}
		else if(element instanceof CSharpIndexMethodDeclaration)
		{
			builder = LookupElementBuilder.create(element, "[]");
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			final CSharpSimpleParameterInfo[] parameterInfos = ((CSharpIndexMethodDeclaration) element).getParameterInfos();
			String parameterText = "[" + StringUtil.join(parameterInfos, new Function<CSharpSimpleParameterInfo, String>()
			{
				@Override
				@RequiredReadAction
				public String fun(CSharpSimpleParameterInfo parameter)
				{
					return CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef(), element) + " " + parameter.getNotNullName();
				}
			}, ", ") + "]";
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(((CSharpIndexMethodDeclaration) element).getReturnTypeRef(), element));
			builder = builder.withPresentableText(parameterText);
			builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
			{
				@Override
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					CharSequence charSequence = context.getDocument().getImmutableCharSequence();

					int start = -1, end = -1;
					for(int i = context.getTailOffset(); i != 0; i--)
					{
						char c = charSequence.charAt(i);
						if(c == '.')
						{
							start = i;
							break;
						}
						else if(c == '[')
						{
							end = i;
						}
					}

					if(start != -1 && end != -1)
					{
						// .[ -> [ replace
						context.getDocument().replaceString(start, end + 1, "[");
					}

					context.getEditor().getCaretModel().moveToOffset(end);
				}
			});
		}
		/*else if(element instanceof DotNetXXXAccessor)
		{
			DotNetNamedElement parent = (DotNetNamedElement) element.getParent();

			DotNetXXXAccessor.Kind accessorKind = ((DotNetXXXAccessor) element).getAccessorKind();
			if(accessorKind == null)
			{
				return null;
			}
			String ownerName = parent.getName();
			if(ownerName == null)
			{
				return null;
			}
			String accessorPrefix = accessorKind.name().toLowerCase(Locale.US);
			builder = LookupElementBuilder.create(element, ownerName);
			builder = builder.withPresentableText(accessorPrefix + "::" + parent.getName());
			builder = builder.withLookupString(accessorPrefix + "::" + parent.getName());
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, Iconable.ICON_FLAG_VISIBILITY));

			if(parent instanceof DotNetVariable)
			{
				builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(((DotNetVariable) parent).toTypeRef(true), parent));
			}
			switch(accessorKind)
			{
				case SET:
					builder = builder.withTailText(" = ", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							if(context.getCompletionChar() != '=')
							{
								int offset = context.getEditor().getCaretModel().getOffset();
								context.getDocument().insertString(offset, " = ");
								context.getEditor().getCaretModel().moveToOffset(offset + 3);
							}
						}
					});
					break;
				case ADD:
					builder = builder.withTailText(" += ", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							int offset = context.getEditor().getCaretModel().getOffset();
							if(context.getCompletionChar() == '+')
							{
								context.getDocument().insertString(offset, "= ");
							}
							else
							{
								context.getDocument().insertString(offset, " += ");
							}
							context.getEditor().getCaretModel().moveToOffset(offset + 4);
						}
					});
					break;
				case REMOVE:
					builder = builder.withTailText(" -= ", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							int offset = context.getEditor().getCaretModel().getOffset();
							if(context.getCompletionChar() == '-')
							{
								context.getDocument().insertString(offset, "= ");
							}
							else
							{
								context.getDocument().insertString(offset, " -= ");
							}
							context.getEditor().getCaretModel().moveToOffset(offset + 4);
						}
					});
					break;
			}
		}   */
		else if(element instanceof DotNetNamespaceAsElement)
		{
			DotNetNamespaceAsElement namespaceAsElement = (DotNetNamespaceAsElement) element;
			String name = namespaceAsElement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.namespace);
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			CSharpTypeDefStatement typeDefStatement = (CSharpTypeDefStatement) element;
			String name = typeDefStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(typeDefStatement.toTypeRef(), typeDefStatement));
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			CSharpLabeledStatementImpl labeledStatement = (CSharpLabeledStatementImpl) element;
			String name = labeledStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof DotNetGenericParameter)
		{
			DotNetGenericParameter typeDefStatement = (DotNetGenericParameter) element;
			String name = typeDefStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetVariable dotNetVariable = (DotNetVariable) element;
			builder = LookupElementBuilder.create(dotNetVariable);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(dotNetVariable.toTypeRef(true), dotNetVariable));

			builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
			{
				@Override
				@RequiredDispatchThread
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					char completionChar = context.getCompletionChar();
					switch(completionChar)
					{
						case '=':
							context.setAddCompletionChar(false);
							TailType.EQ.processTail(context.getEditor(), context.getTailOffset());
							break;
						case ',':
							if(completionParent != null && completionParent.getParent() instanceof CSharpCallArgument)
							{
								context.setAddCompletionChar(false);
								TailType.COMMA.processTail(context.getEditor(), context.getTailOffset());
							}
							break;
					}
				}
			});
		}
		else if(element instanceof CSharpMacroDefine)
		{
			builder = LookupElementBuilder.create((CSharpMacroDefine) element);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			builder = buildTypeLikeElement((CSharpTypeDeclaration) element, extractor);
		}

		if(builder != null && DotNetAttributeUtil.hasAttribute(element, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);
		}
		return builder;
	}

	@RequiredReadAction
	private static <E extends DotNetGenericParameterListOwner & DotNetQualifiedElement> LookupElementBuilder buildTypeLikeElement(@NotNull E element, @NotNull DotNetGenericExtractor extractor)
	{
		String genericText = CSharpElementPresentationUtil.formatGenericParameters(element, extractor);

		String name = element.getName();

		LookupElementBuilder builder = LookupElementBuilder.create(element, name + (extractor == DotNetGenericExtractor.EMPTY ? "" : genericText));

		builder = builder.withPresentableText(name); // always show only name

		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

		builder = builder.withTypeText(element.getPresentableParentQName());

		builder = builder.withTailText(genericText, true);

		if(extractor == DotNetGenericExtractor.EMPTY)
		{
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
