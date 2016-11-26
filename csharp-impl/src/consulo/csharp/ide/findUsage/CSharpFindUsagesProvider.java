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

package consulo.csharp.ide.findUsage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Function;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.lexer.CSharpLexer;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpEnumConstantDeclarationImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpTupleElementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.DotNetXXXAccessor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpFindUsagesProvider implements FindUsagesProvider
{
	@Nullable
	@Override
	public WordsScanner getWordsScanner()
	{
		return new DefaultWordsScanner(new CSharpLexer(), TokenSet.create(CSharpTokens.IDENTIFIER), CSharpTokenSets.COMMENTS,
				CSharpTokenSets.LITERALS);
	}

	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement element)
	{
		return element instanceof DotNetNamedElement;
	}

	@Nullable
	@Override
	public String getHelpId(@NotNull PsiElement element)
	{
		return null;
	}

	@NotNull
	@Override
	public String getType(@NotNull PsiElement element)
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
		return "getType " + element.getNode().getElementType();
	}

	@NotNull
	@Override
	public String getDescriptiveName(@NotNull PsiElement element)
	{
		if(element instanceof DotNetNamedElement)
		{
			String name = ((DotNetNamedElement) element).getName();
			return name == null ? "null" : name;
		}
		if(element instanceof CSharpLocalVariableDeclarationStatement)
		{
			return StringUtil.join(((CSharpLocalVariableDeclarationStatement) element).getVariables(), new Function<CSharpLocalVariable, String>()
			{
				@Override
				public String fun(CSharpLocalVariable cSharpLocalVariable)
				{
					return cSharpLocalVariable.getName();
				}
			}, ", ");
		}
		return "getDescriptiveName " + element.getNode().getElementType();
	}

	@NotNull
	@Override
	@RequiredReadAction
	public String getNodeText(@NotNull PsiElement element, boolean useFullName)
	{
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
				builder.append(CSharpTypeRefPresentationUtil.buildText(dotNetTypeRef, element));
			}
			return builder.toString();
		}

		ItemPresentation itemPresentation = ItemPresentationProviders.getItemPresentation((NavigationItem) element);
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
			builder.append(CSharpTypeRefPresentationUtil.buildText(dotNetTypeRef, element)).append(" ").append(name);
			return builder.toString();
		}

		if(element instanceof DotNetXXXAccessor)
		{
			PsiElement parent = element.getParent();

			String nodeText = getNodeText(parent, useFullName);
			PsiElement accessorElement = ((DotNetXXXAccessor) element).getAccessorElement();
			if(accessorElement == null)
			{
				return nodeText;
			}
			return nodeText + "." + accessorElement.getText();
		}

		return "getNodeText : " + element.getClass().getSimpleName();
	}
}
