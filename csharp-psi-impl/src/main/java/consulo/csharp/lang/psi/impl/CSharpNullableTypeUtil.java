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

package consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpQualifiedNonReference;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.10.2015
 */
public class CSharpNullableTypeUtil
{
	@RequiredReadAction
	public static boolean containsNullableCalls(@NotNull CSharpQualifiedNonReference expression)
	{
		if(expression instanceof CSharpReferenceExpression)
		{
			if(((CSharpReferenceExpression) expression).getMemberAccessType() == CSharpReferenceExpression.AccessType.NULLABLE_CALL)
			{
				return true;
			}

		}
		else if(expression instanceof CSharpIndexAccessExpressionImpl)
		{
			if(((CSharpIndexAccessExpressionImpl) expression).isNullable())
			{
				return true;
			}
		}
		PsiElement qualifier = expression.getQualifier();
		return qualifier instanceof CSharpQualifiedNonReference && containsNullableCalls((CSharpQualifiedNonReference) qualifier);
	}

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef boxIfNeed(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		if(typeResolveResult.isNullable())
		{
			return typeRef;
		}
		return new CSharpGenericWrapperTypeRef(scope.getProject(), new CSharpTypeRefByQName(scope, DotNetTypes.System.Nullable$1), typeRef);
	}

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef box(@NotNull PsiElement scope, @NotNull DotNetTypeRef typeRef)
	{
		return new CSharpGenericWrapperTypeRef(scope.getProject(), new CSharpTypeRefByQName(scope, DotNetTypes.System.Nullable$1), typeRef);
	}

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef unbox(@NotNull DotNetTypeRef typeRef)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		PsiElement element = typeResolveResult.getElement();
		if(element == null)
		{
			return typeRef;
		}

		if(element instanceof CSharpTypeDeclaration && Comparing.equal(((CSharpTypeDeclaration) element).getVmQName(), DotNetTypes.System.Nullable$1))
		{
			DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

			DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) element).getGenericParameters();
			if(genericParameters.length == 1)
			{
				DotNetTypeRef extract = genericExtractor.extract(genericParameters[0]);
				if(extract != null)
				{
					return extract;
				}
			}
		}
		return typeRef;
	}
}
