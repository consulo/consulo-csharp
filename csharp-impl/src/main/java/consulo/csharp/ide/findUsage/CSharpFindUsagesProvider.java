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

package consulo.csharp.ide.findUsage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.PsiUtilCore;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.navigation.NavigationItem;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
@ExtensionImpl
public class CSharpFindUsagesProvider implements FindUsagesProvider
{
	@Override
	public boolean canFindUsagesFor(@Nonnull PsiElement element)
	{
		return element instanceof DotNetNamedElement || element instanceof CSharpPreprocessorVariable;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getType(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			return "type";
		}
		else if(element instanceof CSharpLocalVariableDeclarationStatement)
		{
			return "local variable statement";
		}
		else if(element instanceof CSharpConstructorDeclaration)
		{
			return ((CSharpConstructorDeclaration) element).isDeConstructor() ? "deconstructor" : "constructor";
		}
		else if(element instanceof CSharpIndexMethodDeclaration)
		{
			return "index method";
		}
		else if(element instanceof CSharpTupleVariable || element instanceof CSharpTupleElementImpl)
		{
			return "tuple variable";
		}
		else if(element instanceof CSharpMethodDeclaration)
		{
			return "method";
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			return "namespace";
		}
		else if(element instanceof CSharpEventDeclaration)
		{
			return "event";
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			return "type alias";
		}
		else if(element instanceof CSharpPropertyDeclaration)
		{
			return "property";
		}
		else if(element instanceof CSharpLambdaParameter)
		{
			return "lambda parameter";
		}
		else if(element instanceof DotNetParameter)
		{
			return "parameter";
		}
		else if(element instanceof CSharpLocalVariable)
		{
			return "local variable";
		}
		else if(element instanceof CSharpLinqVariable)
		{
			return "linq local variable";
		}
		else if(element instanceof CSharpOutRefVariableImpl)
		{
			CSharpOutRefVariableExpressionImpl parent = (CSharpOutRefVariableExpressionImpl) element.getParent();
			return parent.getExpressionType() + " local variable";
		}
		else if(element instanceof CSharpIsVariableImpl)
		{
			return "is variable";
		}
		else if(element instanceof CSharpCaseVariableImpl)
		{
			return "case variable";
		}
		else if(element instanceof DotNetGenericParameter)
		{
			return "generic parameter";
		}
		else if(element instanceof CSharpFieldDeclaration)
		{
			return "field";
		}
		else if(element instanceof CSharpEnumConstantDeclarationImpl)
		{
			return "enum constant";
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			return "label";
		}
		else if(element instanceof CSharpPreprocessorVariable)
		{
			return "preprocessor variable";
		}
		return debugText("getType", element);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getDescriptiveName(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpPreprocessorVariable)
		{
			return ((CSharpPreprocessorVariable) element).getName();
		}
		if(element instanceof DotNetNamedElement)
		{
			String name = ((DotNetNamedElement) element).getName();
			return name == null ? "null" : name;
		}
		if(element instanceof CSharpLocalVariableDeclarationStatement)
		{
			return StringUtil.join(((CSharpLocalVariableDeclarationStatement) element).getVariables(), PsiNamedElement::getName, ", ");
		}
		return debugText("getDescriptiveName", element);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getNodeText(@Nonnull PsiElement element, boolean useFullName)
	{
		if(element instanceof CSharpPreprocessorVariable)
		{
			return ((CSharpPreprocessorVariable) element).getName();
		}

		CSharpMethodDeclaration original = element.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER);
		if(original != null)
		{
			return getNodeText(original, useFullName);
		}

		DotNetQualifiedElement accessorValueVariableOwner = element.getUserData(CSharpResolveUtil.ACCESSOR_VALUE_VARIABLE_OWNER);
		if(accessorValueVariableOwner != null)
		{
			return getNodeText(accessorValueVariableOwner, useFullName);
		}

		if(element instanceof CSharpTypeDefStatement)
		{
			String name = ((CSharpTypeDefStatement) element).getName();

			DotNetTypeRef dotNetTypeRef = ((CSharpTypeDefStatement) element).toTypeRef();

			StringBuilder builder = new StringBuilder();
			builder.append(name);

			if(dotNetTypeRef != DotNetTypeRef.ERROR_TYPE)
			{
				builder.append(" = ");
				builder.append(CSharpTypeRefPresentationUtil.buildText(dotNetTypeRef));
			}
			return builder.toString();
		}

		ItemPresentation itemPresentation = ItemPresentationProvider.getItemPresentation((NavigationItem) element);
		if(itemPresentation != null)
		{
			String presentableText = itemPresentation.getPresentableText();
			assert presentableText != null : element.getClass().getName();
			return presentableText;
		}

		if(element instanceof DotNetVariable)
		{
			String name = ((DotNetVariable) element).getName();

			DotNetTypeRef dotNetTypeRef = ((DotNetVariable) element).toTypeRef(false);

			StringBuilder builder = new StringBuilder();
			builder.append(CSharpTypeRefPresentationUtil.buildText(dotNetTypeRef)).append(" ").append(name);
			return builder.toString();
		}

		if(element instanceof DotNetXAccessor)
		{
			PsiElement parent = element.getParent();

			String nodeText = getNodeText(parent, useFullName);
			PsiElement accessorElement = ((DotNetXAccessor) element).getAccessorElement();
			if(accessorElement == null)
			{
				return nodeText;
			}
			return nodeText + "." + accessorElement.getText();
		}

		return debugText("getNodeText", element);
	}

	@Nonnull
	private String debugText(String prefix, @Nonnull PsiElement element)
	{
		IElementType type = PsiUtilCore.getElementType(element);
		String suffix = type == null ? element.getClass().getSimpleName() : type.toString();
		return prefix + " : " + suffix;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
