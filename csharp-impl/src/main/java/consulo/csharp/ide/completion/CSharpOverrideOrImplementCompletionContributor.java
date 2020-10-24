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

package consulo.csharp.ide.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
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
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.CSharpElementPresentationUtil;
import consulo.csharp.ide.actions.generate.GenerateImplementMemberHandler;
import consulo.csharp.ide.actions.generate.GenerateOverrideMemberHandler;
import consulo.csharp.ide.completion.expected.ExpectedUsingInfo;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.psi.icon.CSharpPsiIconGroup;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdaters;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 17.12.14
 */
public class CSharpOverrideOrImplementCompletionContributor implements CSharpMemberAddByCompletionContributor
{
	@RequiredReadAction
	@Override
	public void processCompletion(@Nonnull CompletionParameters parameters,
			@Nonnull ProcessingContext context,
			@Nonnull final Consumer<LookupElement> result,
			@Nonnull CSharpTypeDeclaration typeDeclaration)
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
		else if(element instanceof CSharpXAccessorOwner)
		{
			CSharpXAccessorOwner accessorOwner = (CSharpXAccessorOwner) element;
			StringBuilder builder = new StringBuilder();

			CSharpAccessModifier modifier = hide || typeDeclaration.isInterface() ? CSharpAccessModifier.NONE : CSharpAccessModifier.findModifier(accessorOwner);
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

		final Image rightIcon;
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
				rightIcon = CSharpPsiIconGroup.gutterHidingMethod();
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

		ExpectedUsingInfo expectedUsingInfo = ExpectedUsingInfo.calculateFrom(element);

		lookupElementBuilder = lookupElementBuilder.withInsertHandler((context, item) ->
		{
			CaretModel caretModel = context.getEditor().getCaretModel();

			PsiElement elementAt = context.getFile().findElementAt(caretModel.getOffset() - 1);
			if(elementAt == null)
			{
				return;
			}

			if(expectedUsingInfo != null)
			{
				expectedUsingInfo.insertUsingBefore(elementAt);
			}

			DotNetVirtualImplementOwner virtualImplementOwner = PsiTreeUtil.getParentOfType(elementAt, DotNetVirtualImplementOwner.class);
			if(virtualImplementOwner == null)
			{
				return;
			}

			if(virtualImplementOwner instanceof CSharpMethodDeclaration)
			{
				PsiElement codeBlock = ((CSharpMethodDeclaration) virtualImplementOwner).getCodeBlock().getElement();
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
		});

		return lookupElementBuilder;
	}

	@Nonnull
	@RequiredReadAction
	private static String buildAccessorTail(CSharpTypeDeclaration typeDeclaration, CSharpXAccessorOwner owner, boolean hide, boolean body)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(" { ");
		for(DotNetXAccessor accessor : owner.getAccessors())
		{
			DotNetXAccessor.Kind accessorKind = accessor.getAccessorKind();
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
	public static void formatNameElement(@Nonnull DotNetElement element, @Nonnull StringBuilder builder, boolean hide)
	{
		if(element instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration propertyDeclaration = (CSharpPropertyDeclaration) element;

			CSharpTypeRefPresentationUtil.appendTypeRef(builder, propertyDeclaration.toTypeRef(true), CSharpTypeRefPresentationUtil.TYPE_KEYWORD);

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

			CSharpTypeRefPresentationUtil.appendTypeRef(builder, likeMethodDeclaration.getReturnTypeRef(), CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
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
			CSharpElementPresentationUtil.formatParameters(likeMethodDeclaration, builder, CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE | CSharpElementPresentationUtil.METHOD_PARAMETER_NAME
					| CSharpElementPresentationUtil.NON_QUALIFIED_TYPE);
		}
		else
		{
			builder.append(((PsiNamedElement) element).getName());
		}
	}

	@Nonnull
	@RequiredReadAction
	public static Collection<DotNetModifierListOwner> getItemsImpl(@Nonnull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<PsiElement> allMembers = OverrideUtil.getAllMembers(typeDeclaration, typeDeclaration.getResolveScope(), DotNetGenericExtractor.EMPTY, false, true);

		List<DotNetModifierListOwner> elements = new ArrayList<>();
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

				if(element instanceof CSharpMethodDeclaration || element instanceof CSharpXAccessorOwner)
				{
					elements.add((DotNetModifierListOwner) element);
				}
			}
		}
		return elements;
	}
}
