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

package consulo.csharp.it.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public final class ResultElementBuilder
{
	private ResultElementBuilder()
	{
	}

	@RequiredReadAction
	public static String build(ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();
		assert element != null;
		if(element instanceof CSharpTypeDeclaration typeDeclaration)
		{
			return build("type", typeDeclaration.getVmQName(), resolveResult);
		}
		else if(element instanceof DotNetNamespaceAsElement namespaceAsElement)
		{
			return build("namespace", namespaceAsElement.getPresentableQName(), resolveResult);
		}
		else if(element instanceof CSharpFieldDeclaration fieldDeclaration)
		{
			return build("field", fieldDeclaration.getName(), resolveResult);
		}
		else if(element instanceof CSharpPropertyDeclaration propertyDeclaration)
		{
			return build("property", propertyDeclaration.getName(), resolveResult);
		}
		else if(element instanceof CSharpEnumConstantDeclaration enumConstantDeclaration)
		{
			return build("enum-constant", enumConstantDeclaration.getName(), resolveResult);
		}
		else if(element instanceof CSharpMethodDeclaration methodDeclaration)
		{
			String prefix = methodDeclaration.isDelegate() ? "delegate-method" : "method";
			return build(prefix, formatMethod(methodDeclaration), resolveResult);
		}
		else if(element instanceof CSharpConstructorDeclaration constructorDeclaration)
		{
			return build("constructor", formatMethod(constructorDeclaration), resolveResult);
		}
		else if(element instanceof DotNetGenericParameter genericParameter)
		{
			return build("generic-parameter", genericParameter.getName(), resolveResult);
		}
		else if(element instanceof DotNetParameter parameter)
		{
			return build("parameter", parameter.getName(), resolveResult);
		}
		else if(element instanceof CSharpLocalVariable localVariable)
		{
			return build("local-variable", localVariable.getName(), resolveResult);
		}
		else if(element instanceof CSharpOperatorReferenceImpl operatorReference)
		{
			return build("operator", operatorReference.getOperatorElementType().toString(), resolveResult);
		}
		throw new IllegalArgumentException(element.getClass() + " is not handled");
	}

	@RequiredReadAction
	private static String formatMethod(DotNetLikeMethodDeclaration declaration)
	{
		return CSharpElementPresentationUtil.formatMethod(declaration, CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL);
	}

	private static String build(String prefix, String elementText, ResolveResult result)
	{
		return prefix + "[" + elementText + "]=" + result.isValidResult() + "|" + CSharpResolveUtil.isAssignable(result);
	}
}
