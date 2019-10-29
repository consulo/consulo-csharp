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

package consulo.csharp.ide.highlight;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public class CSharpHighlightUtil
{
	@Nullable
	@RequiredReadAction
	public static HighlightInfo highlightNamed(@Nonnull HighlightInfoHolder holder, @Nullable PsiElement element, @Nullable PsiElement target, @Nullable PsiElement owner)
	{
		if(target == null || element == null)
		{
			return null;
		}

		IElementType elementType = target.getNode().getElementType();
		if(CSharpTokenSets.KEYWORDS.contains(elementType))  // don't highlight keywords
		{
			return null;
		}

		if(isMethodRef(owner, element))
		{
			HighlightInfo highlightInfo = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(CSharpHighlightKey.METHOD_REF).create();
			holder.add(highlightInfo);
		}

		TextAttributesKey defaultTextAttributeKey = getDefaultTextAttributeKey(element, target);
		if(defaultTextAttributeKey == null)
		{
			return null;
		}

		HighlightInfo info = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(defaultTextAttributeKey).create();
		holder.add(info);
		if(!(target instanceof CSharpIdentifier) && DotNetAttributeUtil.hasAttribute(element, DotNetTypes.System.ObsoleteAttribute))
		{
			holder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(target).textAttributes(CodeInsightColors.DEPRECATED_ATTRIBUTES).create());
		}

		return info;
	}

	@RequiredReadAction
	private static TextAttributesKey getDefaultTextAttributeKey(@Nonnull PsiElement element, @Nonnull PsiElement target)
	{
		TextAttributesKey key = null;
		if(element instanceof CSharpTypeDeclaration)
		{
			if(element.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE) != null)
			{
				return CSharpHighlightKey.DELEGATE_METHOD_NAME;
			}

			if(((CSharpTypeDeclaration) element).isEnum())
			{
				key = CSharpHighlightKey.ENUM_NAME;
			}
			else if(((CSharpTypeDeclaration) element).isInterface())
			{
				key = CSharpHighlightKey.INTERFACE_NAME;
			}
			else if(DotNetInheritUtil.isAttribute((CSharpTypeDeclaration) element))
			{
				key = CSharpHighlightKey.ATTRIBUTE_NAME;
			}
			else
			{
				key = CSharpHighlightKey.CLASS_NAME;
			}
		}
		else if(element instanceof CSharpConstructorDeclaration)
		{
			if(target == ((CSharpConstructorDeclaration) element).getNameIdentifier())
			{
				return CSharpHighlightKey.CONSTRUCTOR_NAME;
			}

			PsiElement parent = element.getParent();
			return getDefaultTextAttributeKey(parent, target);
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
			if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isOperator())
			{
				return null;
			}

			if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate())
			{
				return CSharpHighlightKey.DELEGATE_METHOD_NAME;
			}

			if(target == ((DotNetMethodDeclaration) element).getNameIdentifier())
			{
				return CSharpHighlightKey.METHOD_NAME;
			}

			if(CSharpMethodImplUtil.isExtensionWrapper(element))
			{
				key = CSharpHighlightKey.EXTENSION_METHOD_CALL;
			}
			else
			{
				key = ((DotNetModifierListOwner) element).hasModifier(CSharpModifier.STATIC) ? CSharpHighlightKey.STATIC_METHOD_CALL : CSharpHighlightKey.INSTANCE_METHOD_CALL;
			}
		}
		else if(element instanceof CSharpPreprocessorDefine)
		{
			key = CSharpHighlightKey.MACRO_VARIABLE;
		}
		else if(element instanceof CSharpLocalVariable)
		{
			DotNetQualifiedElement owner = element.getUserData(CSharpResolveUtil.ACCESSOR_VALUE_VARIABLE_OWNER);
			if(owner != null)
			{
				key = EditorColors.INJECTED_LANGUAGE_FRAGMENT;
			}
			else
			{
				key = DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
			}
		}
		else if(element instanceof CSharpLinqVariable || element instanceof CSharpOutRefVariableImpl || element instanceof CSharpIsVariableImpl || element instanceof CSharpCaseVariableImpl)
		{
			key = DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
		}
		else if(element instanceof CSharpEventDeclaration)
		{
			key = ((DotNetVariable) element).hasModifier(CSharpModifier.STATIC) ? CSharpHighlightKey.STATIC_EVENT : CSharpHighlightKey.INSTANCE_EVENT;
		}
		else if(element instanceof DotNetVariable)
		{
			key = ((DotNetVariable) element).hasModifier(CSharpModifier.STATIC) ? CSharpHighlightKey.STATIC_FIELD_OR_PROPERTY : CSharpHighlightKey.INSTANCE_FIELD_OR_PROPERTY;
		}
		return key;
	}

	@RequiredReadAction
	public static boolean isMethodRef(PsiElement owner, PsiElement element)
	{
		if(owner instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) owner).kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
		{
			if(element == null)
			{
				element = ((CSharpReferenceExpression) owner).resolve();
			}
			return element instanceof CSharpMethodDeclaration && !((CSharpMethodDeclaration) element).isDelegate();
		}
		else
		{
			return false;
		}
	}

	@Nullable
	public static HighlightInfo createError(PsiElement element, @PropertyKey(resourceBundle = "messages.CSharpErrorBundle") String b)
	{
		return HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).descriptionAndTooltip(b).range(element).create();
	}
}
