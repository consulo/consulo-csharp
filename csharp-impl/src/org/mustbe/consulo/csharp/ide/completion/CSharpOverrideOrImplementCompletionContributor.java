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
import java.util.Locale;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.ide.actions.generate.GenerateImplementMemberHandler;
import org.mustbe.consulo.csharp.ide.actions.generate.GenerateOverrideMemberHandler;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.DotNetXXXAccessor;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 17.12.14
 */
public class CSharpOverrideOrImplementCompletionContributor extends CSharpMemberAddByCompletionContributor
{
	@RequiredReadAction
	@Override
	public void processCompletion(@NotNull CompletionParameters parameters,
			@NotNull ProcessingContext context,
			@NotNull final Consumer<LookupElement> result,
			@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<DotNetModifierListOwner> overrideItems = getItemsImpl(typeDeclaration);
		for(DotNetModifierListOwner overrideItem : overrideItems)
		{
			LookupElementBuilder builder = buildLookupItem(typeDeclaration, overrideItem, false);

			result.consume(builder);

			if(!typeDeclaration.isInterface() && overrideItem.hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
			{
				builder = buildLookupItem(typeDeclaration, overrideItem, true);

				result.consume(builder);
			}
		}
	}

	@Nullable
	@RequiredReadAction
	private static LookupElementBuilder buildLookupItem(CSharpTypeDeclaration typeDeclaration, DotNetModifierListOwner element, boolean hide)
	{
		LookupElementBuilder lookupElementBuilder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;
			StringBuilder builder = new StringBuilder();

			CSharpAccessModifier modifier = hide || typeDeclaration.isInterface() ? CSharpAccessModifier.NONE : CSharpAccessModifier.findModifier(methodDeclaration);
			if(modifier != CSharpAccessModifier.NONE)
			{
				builder.append(modifier.getPresentableText()).append(" ");
			}

			CSharpModifier requiredOverrideModifier = OverrideUtil.getRequiredOverrideModifier(methodDeclaration);
			if(requiredOverrideModifier != null)
			{
				builder.append(requiredOverrideModifier.getPresentableText()).append(" ");
			}

			formatNameElement(methodDeclaration, builder, hide);

			String presentationText = builder.toString();
			if(typeDeclaration.isInterface())
			{
				builder.append(";");
				if(!methodDeclaration.hasModifier(DotNetModifier.ABSTRACT))
				{
					return null;
				}
			}
			else
			{
				builder.append("{\n");
				if(methodDeclaration.hasModifier(DotNetModifier.ABSTRACT))
				{
					GenerateImplementMemberHandler.generateReturn(builder, element);
				}
				else
				{
					GenerateOverrideMemberHandler.generateReturn(builder, element);
				}

				builder.append("}");
			}

			lookupElementBuilder = LookupElementBuilder.create(builder.toString());
			lookupElementBuilder = lookupElementBuilder.withPresentableText(presentationText);
			lookupElementBuilder = lookupElementBuilder.withLookupString(methodDeclaration.getName());
			lookupElementBuilder = lookupElementBuilder.withTailText(" {...}", true);
		}
		else if(element instanceof CSharpXXXAccessorOwner)
		{
			CSharpXXXAccessorOwner accessorOwner = (CSharpXXXAccessorOwner) element;
			StringBuilder builder = new StringBuilder();

			CSharpAccessModifier modifier = hide || typeDeclaration.isInterface() ? CSharpAccessModifier.NONE : CSharpAccessModifier.findModifier((DotNetModifierListOwner) accessorOwner);
			if(modifier != CSharpAccessModifier.NONE)
			{
				builder.append(modifier.getPresentableText()).append(" ");
			}

			formatNameElement(accessorOwner, builder, hide);

			String presentationText = builder.toString();

			builder.append(buildAccessorTail(typeDeclaration, accessorOwner, hide, true));

			lookupElementBuilder = LookupElementBuilder.create(builder.toString());
			lookupElementBuilder = lookupElementBuilder.withPresentableText(presentationText);
			if(accessorOwner instanceof CSharpIndexMethodDeclaration)
			{
				lookupElementBuilder = lookupElementBuilder.withLookupString("this");
			}
			else
			{
				lookupElementBuilder = lookupElementBuilder.withLookupString(((PsiNamedElement) accessorOwner).getName());
			}
			lookupElementBuilder = lookupElementBuilder.withTailText(buildAccessorTail(typeDeclaration, accessorOwner, hide, false), true);
		}

		if(lookupElementBuilder == null)
		{
			return null;
		}

		final Icon rightIcon;
		if(typeDeclaration.isInterface())
		{
			if(element.hasModifier(DotNetModifier.ABSTRACT))
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
			if(hide)
			{
				rightIcon = CSharpIcons.Gutter.HidingMethod;
			}
			else if(element.hasModifier(DotNetModifier.ABSTRACT))
			{
				rightIcon = AllIcons.Gutter.ImplementingMethod;
			}
			else
			{
				rightIcon = AllIcons.Gutter.OverridingMethod;
			}

		}

		IconDescriptor iconDescriptor = new IconDescriptor(IconDescriptorUpdaters.getIcon(element, 0));
		iconDescriptor.setRightIcon(rightIcon);

		lookupElementBuilder = lookupElementBuilder.withIcon(iconDescriptor.toIcon());

		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeDeclaration)
		{
			lookupElementBuilder = lookupElementBuilder.withTypeText(DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) parent));
		}
		lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
		{
			@Override
			@RequiredDispatchThread
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

		return lookupElementBuilder;
	}

	@NotNull
	@RequiredReadAction
	private static String buildAccessorTail(CSharpTypeDeclaration typeDeclaration, CSharpXXXAccessorOwner owner, boolean hide, boolean body)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(" { ");
		for(DotNetXXXAccessor accessor : owner.getAccessors())
		{
			DotNetXXXAccessor.Kind accessorKind = accessor.getAccessorKind();
			if(accessorKind == null)
			{
				continue;
			}

			builder.append(accessorKind.name().toLowerCase(Locale.US));
			if(!body)
			{
				builder.append("; ");
			}
			else
			{
				if(typeDeclaration.isInterface())
				{
					builder.append("; ");
				}
				else
				{
					builder.append("{\n");
					if(owner.hasModifier(DotNetModifier.ABSTRACT))
					{
						GenerateImplementMemberHandler.generateReturn(builder, accessor);
					}
					else
					{
						GenerateOverrideMemberHandler.generateReturn(builder, accessor);
					}

					builder.append("}");
				}
			}
		}
		builder.append("}");
		return builder.toString();
	}

	@RequiredReadAction
	public static void formatNameElement(@NotNull DotNetElement element, @NotNull StringBuilder builder, boolean hide)
	{
		if(element instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration propertyDeclaration = (CSharpPropertyDeclaration) element;

			CSharpTypeRefPresentationUtil.appendTypeRef(element, builder, propertyDeclaration.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);

			builder.append(" ");

			if(hide)
			{
				builder.append(DotNetElementPresentationUtil.formatTypeWithGenericParameters((CSharpTypeDeclaration) element.getParent()));
				builder.append(".");
			}

			builder.append(((PsiNamedElement) element).getName());
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			DotNetLikeMethodDeclaration likeMethodDeclaration = (DotNetLikeMethodDeclaration) element;

			CSharpTypeRefPresentationUtil.appendTypeRef(element, builder, likeMethodDeclaration.getReturnTypeRef(), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
			builder.append(" ");

			if(hide)
			{
				builder.append(DotNetElementPresentationUtil.formatTypeWithGenericParameters((CSharpTypeDeclaration) element.getParent()));
				builder.append(".");
			}

			if(likeMethodDeclaration instanceof CSharpIndexMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				builder.append(((PsiNamedElement) element).getName());
			}
			CSharpElementPresentationUtil.formatTypeGenericParameters(likeMethodDeclaration.getGenericParameters(), builder);
			CSharpElementPresentationUtil.formatParameters(likeMethodDeclaration, builder, CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE | CSharpElementPresentationUtil
					.METHOD_PARAMETER_NAME);
		}
		else
		{
			builder.append(((PsiNamedElement) element).getName());
		}
	}

	@NotNull
	@RequiredReadAction
	public static Collection<DotNetModifierListOwner> getItemsImpl(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<PsiElement> allMembers = OverrideUtil.getAllMembers(typeDeclaration, typeDeclaration.getResolveScope(), DotNetGenericExtractor.EMPTY, false, true);

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

				if(element instanceof CSharpMethodDeclaration || element instanceof CSharpXXXAccessorOwner)
				{
					elements.add((DotNetModifierListOwner) element);
				}
			}
		}
		return elements;
	}
}
