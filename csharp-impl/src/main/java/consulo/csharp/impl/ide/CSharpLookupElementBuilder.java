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

package consulo.csharp.impl.ide;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.CaretModel;
import consulo.csharp.impl.ide.completion.CSharpCompletionSorting;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.impl.ide.completion.insertHandler.CSharpParenthesesWithSemicolonInsertHandler;
import consulo.csharp.impl.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.impl.ide.completion.util.LtGtInsertHandler;
import consulo.csharp.lang.impl.psi.CSharpMethodUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.completion.lookup.*;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLookupElementBuilder
{
	public static final Key<Boolean> OBSOLETE_FLAG = Key.create("obsolete");

	@Nonnull
	@RequiredReadAction
	public static LookupElement[] buildToLookupElements(@Nonnull PsiElement[] arguments)
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
			@Nonnull DotNetGenericExtractor extractor,
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
	public static LookupElementBuilder createLookupElementBuilder(@Nonnull final PsiElement element, @Nonnull DotNetGenericExtractor extractor, @Nullable final PsiElement completionParent)
	{
		LookupElementBuilder builder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			final CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

			if(!methodDeclaration.isDelegate())
			{
				String name = methodDeclaration.getNameWithAt();
				if(name == null)
				{
					return null;
				}

				CSharpMethodUtil.Result inheritGeneric = CSharpMethodUtil.isCanInheritGeneric(methodDeclaration);

				String lookupString = inheritGeneric == CSharpMethodUtil.Result.CAN ? name + "<>()" : name;
				builder = LookupElementBuilder.create(methodDeclaration, lookupString);
				builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));

				final CSharpSimpleParameterInfo[] parameterInfos = methodDeclaration.getParameterInfos();

				String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element);

				String parameterText = genericText + "(" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef()) + " " +
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
				builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(methodDeclaration.getReturnTypeRef()));
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
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
			final CSharpSimpleParameterInfo[] parameterInfos = ((CSharpIndexMethodDeclaration) element).getParameterInfos();
			String parameterText = "[" + StringUtil.join(parameterInfos, parameter -> CSharpTypeRefPresentationUtil.buildShortText(parameter.getTypeRef()) + " " + parameter.getNotNullName()
					, ", ") + "]";
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(((CSharpIndexMethodDeclaration) element).getReturnTypeRef()));
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
		/*else if(element instanceof DotNetXAccessor)
		{
			DotNetNamedElement parent = (DotNetNamedElement) element.getParent();

			DotNetXAccessor.Kind accessorKind = ((DotNetXAccessor) element).getAccessorKind();
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
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, 0));

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

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
			CSharpCompletionSorting.force(builder, CSharpCompletionSorting.KindSorter.Type.namespace);
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			CSharpTypeDefStatement typeDefStatement = (CSharpTypeDefStatement) element;
			String name = CSharpNamedElement.getEscapedName(typeDefStatement);
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(typeDefStatement.toTypeRef()));
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			CSharpLabeledStatementImpl labeledStatement = (CSharpLabeledStatementImpl) element;
			String name = CSharpNamedElement.getEscapedName(labeledStatement);
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
		}
		else if(element instanceof DotNetGenericParameter)
		{
			DotNetGenericParameter genericParameter = (DotNetGenericParameter) element;
			String name = CSharpNamedElement.getEscapedName(genericParameter);
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetVariable variable = (DotNetVariable) element;
			String name = CSharpNamedElement.getEscapedName(variable);
			if((variable instanceof CSharpFieldDeclaration || variable instanceof CSharpPropertyDeclaration) && needAddThisPrefix(variable, completionParent))
			{
				builder = LookupElementBuilder.create(variable, "this." + name);
				builder = builder.withLookupString(name);
			}
			else
			{
				builder = LookupElementBuilder.create(variable, name);
			}

			builder = decapitalizeLookup(builder, name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));

			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(variable.toTypeRef(true)));

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
				@RequiredUIAccess
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
							EqTailType.INSTANCE.processTail(context.getEditor(), context.getTailOffset());
							break;
						case ',':
							if(completionParent != null && completionParent.getParent() instanceof CSharpCallArgument)
							{
								context.setAddCompletionChar(false);
								CommaTailType.INSTANCE.processTail(context.getEditor(), context.getTailOffset());
							}
							break;
					}
				}
			});
		}
		else if(element instanceof CSharpPreprocessorDefine)
		{
			builder = LookupElementBuilder.create(element);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			builder = buildTypeLikeElement((CSharpTypeDeclaration) element, extractor);
		}

		if(builder != null && DotNetAttributeUtil.hasAttribute(element, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);

			builder.putCopyableUserData(OBSOLETE_FLAG, Boolean.TRUE);
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
	private static <E extends DotNetGenericParameterListOwner & DotNetQualifiedElement> LookupElementBuilder buildTypeLikeElement(@Nonnull E element, @Nonnull DotNetGenericExtractor extractor)
	{
		String genericText = CSharpElementPresentationUtil.formatGenericParameters(element, extractor);

		String name = CSharpNamedElement.getEscapedName(element);

		LookupElementBuilder builder = LookupElementBuilder.create(element, name + (extractor == DotNetGenericExtractor.EMPTY ? "" : genericText));

		builder = builder.withPresentableText(name); // always show only name

		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, 0));

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
	private static boolean needAddThisPrefix(@Nonnull DotNetVariable variable, @Nullable PsiElement parentCompletion)
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
