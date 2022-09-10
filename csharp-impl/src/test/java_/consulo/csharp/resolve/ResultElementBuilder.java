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

package consulo.csharp.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.CSharpElementPresentationUtil;
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
public class ResultElementBuilder implements consulo.ide.impl.idea.util.Function<ResolveResult, String>
{
	public static final ResultElementBuilder INSTANCE = new ResultElementBuilder();

	@Override
	@RequiredReadAction
	public String fun(ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();
		assert element != null;
		if(element instanceof CSharpTypeDeclaration)
		{
			return build("type", ((CSharpTypeDeclaration) element).getVmQName(), resolveResult);
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			return build("namespace", ((DotNetNamespaceAsElement) element).getPresentableQName(), resolveResult);
		}
		else if(element instanceof CSharpFieldDeclaration)
		{
			return build("field", ((CSharpFieldDeclaration) element).getName(), resolveResult);
		}
		else if(element instanceof CSharpPropertyDeclaration)
		{
			return build("property", ((CSharpPropertyDeclaration) element).getName(), resolveResult);
		}
		else if(element instanceof CSharpEnumConstantDeclaration)
		{
			return build("enum-constant", ((CSharpEnumConstantDeclaration) element).getName(), resolveResult);
		}
		else if(element instanceof CSharpMethodDeclaration)
		{
			boolean delegate = ((CSharpMethodDeclaration) element).isDelegate();
			return build((delegate ? "delegate-" : "") + "method", CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) element,
					CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL), resolveResult);
		}
		else if(element instanceof CSharpConstructorDeclaration)
		{
			return build("constructor", CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) element, CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL), resolveResult);
		}
		else if(element instanceof DotNetGenericParameter)
		{
			return build("generic-parameter", ((DotNetGenericParameter) element).getName(), resolveResult);
		}
		else if(element instanceof DotNetParameter)
		{
			return build("parameter", ((DotNetParameter) element).getName(), resolveResult);
		}
		else if(element instanceof CSharpLocalVariable)
		{
			return build("local-variable", ((CSharpLocalVariable) element).getName(), resolveResult);
		}
		else if(element instanceof CSharpOperatorReferenceImpl)
		{
			return build("operator", ((CSharpOperatorReferenceImpl) element).getOperatorElementType().toString(), resolveResult);
		}
		throw new IllegalArgumentException(element.getClass() + " is not handled");
	}

	private String build(String prefix, String elementText, ResolveResult result)
	{
		return prefix + "[" + elementText + "]=" + result.isValidResult() + "|" + CSharpResolveUtil.isAssignable(result);
	}
}
