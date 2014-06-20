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

package org.mustbe.consulo.csharp.ide.highlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public class CSharpHighlightUtil
{
	public static boolean isGeneratedElement(@NotNull PsiElement element)
	{
		if(element instanceof CSharpLocalVariable)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof DotNetXXXAccessor)
			{
				IElementType accessorType = ((DotNetXXXAccessor) parent).getAccessorType();
				if(accessorType == CSharpSoftTokens.SET_KEYWORD && Comparing.equal(((CSharpLocalVariable) element).getName(),
						DotNetXXXAccessor.VALUE))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Nullable
	public static HighlightInfo highlightNamed(@NotNull HighlightInfoHolder holder, @Nullable PsiElement element, @Nullable PsiElement target,
			@Nullable PsiElement owner)
	{
		if(target == null || element == null)
		{
			return null;
		}

		IElementType elementType = target.getNode().getElementType();
		if(CSharpTokenSets.KEYWORDS.contains(elementType))  // dont highlight keywords
		{
			return null;
		}

		TextAttributesKey defaultTextAttributeKey = getDefaultTextAttributeKey(element);
		if(defaultTextAttributeKey == null)
		{
			return null;
		}

		HighlightInfo info = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(defaultTextAttributeKey)
				.create();
		holder.add(info);
		if(element instanceof DotNetModifierListOwner && DotNetAttributeUtil.hasAttribute((DotNetModifierListOwner) element,
				DotNetTypes.System_ObsoleteAttribute))
		{
			holder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(CodeInsightColors
					.DEPRECATED_ATTRIBUTES).create());
		}

		if(owner instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) owner).kind() == CSharpReferenceExpression.ResolveToKind
				.ANY_MEMBER && element instanceof CSharpMethodDeclaration)
		{
			holder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(CSharpHighlightKey.METHOD_REF)
					.create());
		}

		if(CSharpHighlightUtil.isGeneratedElement(element))
		{
			holder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(EditorColors
					.INJECTED_LANGUAGE_FRAGMENT).create());
		}
		return info;
	}

	private static TextAttributesKey getDefaultTextAttributeKey(PsiElement element)
	{
		TextAttributesKey key = null;
		if(element instanceof CSharpTypeDeclaration)
		{
			if(DotNetInheritUtil.isAttribute((DotNetTypeDeclaration) element))
			{
				key = CSharpHighlightKey.ATTRIBUTE_NAME;
			}
			else
			{
				key = CSharpHighlightKey.CLASS_NAME;
			}
		}
		else if(element instanceof DotNetConstructorDeclaration)
		{
			PsiElement parent = element.getParent();
			return getDefaultTextAttributeKey(parent);
		}
		else if(element instanceof DotNetGenericParameter || element instanceof CSharpTypeDefStatementImpl)
		{
			key = CSharpHighlightKey.GENERIC_PARAMETER_NAME;
		}
		else if(element instanceof DotNetParameter || element instanceof CSharpLambdaParameter)
		{
			key = CSharpHighlightKey.PARAMETER;
		}
		else if(element instanceof DotNetMethodDeclaration)
		{
			if(CSharpMethodImplUtil.isExtensionWrapper(element))
			{
				key = CSharpHighlightKey.EXTENSION_METHOD;
			}
			else
			{
				key = ((DotNetModifierListOwner) element).hasModifier(CSharpModifier.STATIC) ? CSharpHighlightKey.STATIC_METHOD : CSharpHighlightKey
						.INSTANCE_METHOD;
			}
		}
		else if(element instanceof CSharpMacroDefine)
		{
			key = CSharpHighlightKey.MACRO_VARIABLE;
		}
		else if(element instanceof CSharpLocalVariable)
		{
			key = DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
		}
		else if(element instanceof DotNetVariable)
		{
			key = ((DotNetVariable) element).hasModifier(CSharpModifier.STATIC) ? CSharpHighlightKey.STATIC_FIELD : CSharpHighlightKey
					.INSTANCE_FIELD;
		}
		return key;
	}

	@Nullable
	public static HighlightInfo createError(PsiElement element, @PropertyKey(resourceBundle = "messages.CSharpErrorBundle") String b)
	{
		return HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).descriptionAndTooltip(b).range(element).create();
	}
}
