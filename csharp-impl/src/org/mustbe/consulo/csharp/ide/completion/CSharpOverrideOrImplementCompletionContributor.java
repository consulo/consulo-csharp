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

package org.mustbe.consulo.csharp.ide.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.ide.actions.generate.GenerateImplementMemberHandler;
import org.mustbe.consulo.csharp.ide.actions.generate.GenerateOverrideMemberHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 17.12.14
 */
public class CSharpOverrideOrImplementCompletionContributor extends CSharpMemberAddByCompletionContributor
{
	private static final int ourMethodFlags = CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE | CSharpElementPresentationUtil
			.METHOD_PARAMETER_NAME;

	@RequiredReadAction
	@Override
	public void processCompletion(@NotNull CompletionParameters parameters,
			ProcessingContext context,
			@NotNull CompletionResultSet result,
			@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<DotNetModifierListOwner> overrideItems = getItemsImpl(typeDeclaration);
		for(DotNetModifierListOwner overrideItem : overrideItems)
		{
			LookupElementBuilder builder = buildLookupItem(typeDeclaration, overrideItem, false);

			if(builder != null)
			{
				result.addElement(builder);
			}

			if(!typeDeclaration.isInterface() && overrideItem.hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
			{
				builder = buildLookupItem(typeDeclaration, overrideItem, true);

				if(builder != null)
				{
					result.addElement(builder);
				}
			}
		}
	}

	@Nullable
	@RequiredReadAction
	private static LookupElementBuilder buildLookupItem(CSharpTypeDeclaration typeDeclaration, PsiElement element, boolean hide)
	{
		Icon rightIcon = null;
		LookupElementBuilder lookupElementBuilder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;
			StringBuilder builder = new StringBuilder();

			CSharpAccessModifier modifier = hide || typeDeclaration.isInterface() ? CSharpAccessModifier.NONE : CSharpAccessModifier.findModifier
					(methodDeclaration);
			if(modifier != CSharpAccessModifier.NONE)
			{
				builder.append(modifier.getPresentableText()).append(" ");
			}

			CSharpModifier requiredOverrideModifier = OverrideUtil.getRequiredOverrideModifier(methodDeclaration);
			if(requiredOverrideModifier != null)
			{
				builder.append(requiredOverrideModifier.getPresentableText()).append(" ");
			}

			formatMethod(methodDeclaration, builder, hide);

			String presentationText = builder.toString();
			if(typeDeclaration.isInterface())
			{
				builder.append(";");
				if(methodDeclaration.hasModifier(DotNetModifier.ABSTRACT))
				{
					rightIcon = AllIcons.Gutter.OverridingMethod;
				}
				else
				{
					return null;
				}
			}
			else
			{
				builder.append("{\n");
				if(methodDeclaration.hasModifier(DotNetModifier.ABSTRACT))
				{
					rightIcon = AllIcons.Gutter.ImplementingMethod;
					GenerateImplementMemberHandler.generateReturn(builder, element);
				}
				else
				{
					rightIcon = AllIcons.Gutter.OverridingMethod;
					GenerateOverrideMemberHandler.generateReturn(builder, element);
				}

				if(hide)
				{
					rightIcon = CSharpIcons.Gutter.HidingMethod;
				}

				builder.append("}");
			}

			lookupElementBuilder = LookupElementBuilder.create(builder.toString());
			lookupElementBuilder = lookupElementBuilder.withPresentableText(presentationText);
			lookupElementBuilder = lookupElementBuilder.withLookupString(methodDeclaration.getName());
			lookupElementBuilder = lookupElementBuilder.withTailText("{...}", true);
		}

		if(lookupElementBuilder == null)
		{
			return null;
		}

		IconDescriptor iconDescriptor = new IconDescriptor(IconDescriptorUpdaters.getIcon(element, 0));
		iconDescriptor.setRightIcon(rightIcon);

		lookupElementBuilder = lookupElementBuilder.withIcon(iconDescriptor.toIcon());

		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeDeclaration)
		{
			lookupElementBuilder = lookupElementBuilder.withTypeText(DotNetElementPresentationUtil.formatTypeWithGenericParameters(
					(DotNetTypeDeclaration) parent));
		}
		lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
		{
			@Override
			public void handleInsert(InsertionContext context, LookupElement item)
			{
				CaretModel caretModel = context.getEditor().getCaretModel();

				PsiElement elementAt = context.getFile().findElementAt(caretModel.getOffset() - 1);
				if(elementAt == null)
				{
					return;
				}

				DotNetVirtualImplementOwner virtualImplementOwner = PsiTreeUtil.getParentOfType(elementAt, DotNetVirtualImplementOwner.class);
				if(virtualImplementOwner == null)
				{
					return;
				}

				if(virtualImplementOwner instanceof CSharpMethodDeclaration)
				{
					PsiElement codeBlock = ((CSharpMethodDeclaration) virtualImplementOwner).getCodeBlock();
					if(codeBlock instanceof CSharpBlockStatementImpl)
					{
						DotNetStatement[] statements = ((CSharpBlockStatementImpl) codeBlock).getStatements();
						if(statements.length > 0)
						{
							caretModel.moveToOffset(statements[0].getTextOffset() + statements[0].getTextLength());
						}
						else
						{
							caretModel.moveToOffset(((CSharpBlockStatementImpl) codeBlock).getLeftBrace().getTextOffset() + 1);
						}
					}
				}

				context.commitDocument();

				CodeStyleManager.getInstance(context.getProject()).reformat(virtualImplementOwner);
			}
		});

		CSharpCompletionSorting.force(lookupElementBuilder, CSharpCompletionSorting.KindSorter.Type.overrideMember);
		return lookupElementBuilder;
	}

	public static void formatMethod(@NotNull DotNetLikeMethodDeclaration methodDeclaration, @NotNull StringBuilder builder, boolean hide)
	{
		if(!(methodDeclaration instanceof DotNetConstructorDeclaration))
		{
			CSharpTypeRefPresentationUtil.appendTypeRef(methodDeclaration, builder, methodDeclaration.getReturnTypeRef(),
					CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
			builder.append(" ");
		}

		if(methodDeclaration instanceof DotNetConstructorDeclaration && ((DotNetConstructorDeclaration) methodDeclaration).isDeConstructor())
		{
			builder.append("~");
		}

		if(methodDeclaration instanceof CSharpIndexMethodDeclaration)
		{
			builder.append("this");
		}
		else
		{
			if(hide)
			{
				builder.append(DotNetElementPresentationUtil.formatTypeWithGenericParameters((CSharpTypeDeclaration) methodDeclaration.getParent()));
				builder.append(".");
			}
			builder.append(methodDeclaration.getName());
		}

		CSharpElementPresentationUtil.formatTypeGenericParameters(methodDeclaration.getGenericParameters(), builder);
		CSharpElementPresentationUtil.formatParameters(methodDeclaration, builder, ourMethodFlags);
	}

	@NotNull
	@RequiredReadAction
	public static Collection<DotNetModifierListOwner> getItemsImpl(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<PsiElement> allMembers = OverrideUtil.getAllMembers(typeDeclaration, typeDeclaration.getResolveScope(),
				DotNetGenericExtractor.EMPTY, false, true);

		List<DotNetModifierListOwner> elements = new ArrayList<DotNetModifierListOwner>();
		for(PsiElement element : allMembers)
		{
			if(element instanceof DotNetModifierListOwner)
			{
				if(((DotNetModifierListOwner) element).hasModifier(DotNetModifier.STATIC))
				{
					continue;
				}

				if(!CSharpVisibilityUtil.isVisible(typeDeclaration, element))
				{
					continue;
				}

				if(element instanceof CSharpMethodDeclaration)
				{
					elements.add((DotNetModifierListOwner) element);
				}
			}
		}
		return elements;
	}
}
