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

package consulo.csharp.ide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.completion.CSharpCompletionSorting;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.ide.completion.util.LtGtInsertHandler;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.ide.IconDescriptorUpdaters;

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

		List<LookupElement> list = new ArrayList<>(arguments.length);
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

				String parameterText = genericText + "(" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef(), element) + " " +
						parameter.getNotNullName(), ", ") + ")";

				builder = decapitalizeLookup(builder, name);

				if(inheritGeneric == CSharpMethodUtil.Result.CAN)
				{
					builder = builder.withPresentableText(name);
					builder = builder.withInsertHandler((context, item) ->
					{
						CaretModel caretModel = context.getEditor().getCaretModel();
						caretModel.moveToOffset(caretModel.getOffset() - 3);
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
			String parameterText = "[" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef(), element) + " " + parameter.getNotNullName()
					, ", ") + "]";
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(((CSharpIndexMethodDeclaration) element).getReturnTypeRef(), element));
			builder = builder.withPresentableText(parameterText);
			builder = builder.withInsertHandler((context, item) ->
			{
				CharSequence charSequence = context.getDocument().getImmutableCharSequence();

				int start = -1, end = -1;
				for(int i = context.getStartOffset(); i != 0; i--)
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
			DotNetVariable variable = (DotNetVariable) element;
			String name = variable.getName();
			if((variable instanceof CSharpFieldDeclaration || variable instanceof CSharpPropertyDeclaration) && needAddThisPrefix(variable, completionParent))
			{
				builder = LookupElementBuilder.create(variable, "this." + name);
				builder = builder.withLookupString(name);
			}
			else
			{
				builder = LookupElementBuilder.create(variable);
			}

			builder = decapitalizeLookup(builder, name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(variable.toTypeRef(true), variable));

			CSharpRefTypeRef.Type refType = null;
			if(completionParent != null)
			{
				PsiElement parent = completionParent.getParent();

				if(!(parent instanceof CSharpOutRefWrapExpressionImpl))
				{
					List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(completionParent);
					if(expectedTypeRefs.size() == 1)
					{
						ExpectedTypeInfo expectedTypeInfo = expectedTypeRefs.get(0);
						DotNetTypeRef typeRef = expectedTypeInfo.getTypeRef();
						if(typeRef instanceof CSharpRefTypeRef)
						{
							refType = ((CSharpRefTypeRef) typeRef).getType();
						}
					}
				}
			}

			final CSharpRefTypeRef.Type finalRefType = refType;
			builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
			{
				@Override
				@RequiredDispatchThread
				public void handleInsert(InsertionContext context, LookupElement item)
				{
					if(finalRefType != null)
					{
						context.getDocument().insertString(context.getStartOffset(), finalRefType + " ");
					}

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
		else if(element instanceof CSharpPreprocessorDefine)
		{
			builder = LookupElementBuilder.create(element);
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

	private static LookupElementBuilder decapitalizeLookup(LookupElementBuilder builder, String name)
	{
		if(name != null && name.length() > 0 && Character.isUpperCase(name.charAt(0)))
		{
			builder = builder.withLookupString(StringUtil.decapitalize(name));
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

	@RequiredReadAction
	private static boolean needAddThisPrefix(@NotNull DotNetVariable variable, @Nullable PsiElement parentCompletion)
	{
		if(parentCompletion == null || variable.hasModifier(CSharpModifier.STATIC))
		{
			return false;
		}

		if(parentCompletion instanceof CSharpReferenceExpression)
		{
			if(((CSharpReferenceExpression) parentCompletion).getQualifier() != null)
			{
				return false;
			}
		}

		PsiElement parent = parentCompletion;
		do
		{
			if(parent instanceof DotNetParameterListOwner)
			{
				for(DotNetParameter parameter : ((DotNetParameterListOwner) parent).getParameters())
				{
					if(Objects.equals(parameter.getName(), variable.getName()))
					{
						return true;
					}
				}
			}
			else if(parent instanceof CSharpLambdaExpressionImpl)
			{
				for(CSharpLambdaParameter parameter : ((CSharpLambdaExpressionImpl) parent).getParameters())
				{
					if(Objects.equals(parameter.getName(), variable.getName()))
					{
						return true;
					}
				}
			}
			else if(parent instanceof CSharpBlockStatementImpl)
			{
				for(DotNetStatement statement : ((CSharpBlockStatementImpl) parent).getStatements())
				{
					if(statement.getTextOffset() > parentCompletion.getTextOffset())
					{
						break;
					}

					if(statement instanceof CSharpLocalVariableDeclarationStatement)
					{
						CSharpLocalVariable[] variables = ((CSharpLocalVariableDeclarationStatement) statement).getVariables();
						for(CSharpLocalVariable localVariable : variables)
						{
							if(Objects.equals(localVariable.getName(), variable.getName()))
							{
								return true;
							}
						}
					}
				}
			}

			parent = parent.getParent();
		}
		while(parent != null);

		return false;
	}
}
