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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.01.14
 */
public class CSharpTypeUtil
{
	/**
	 * We have expression
	 * int a = "test";
	 * <p/>
	 * "test" - string type, ill be 'top' parameter
	 * int - int type, ill 'target'
	 * return false due it not be casted
	 */
	public static boolean isInheritable(@NotNull DotNetTypeRef top, @NotNull DotNetTypeRef target, @NotNull PsiElement scope)
	{
		if(top == DotNetTypeRef.ERROR_TYPE || target == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		if(top == DotNetTypeRef.NULL_TYPE && target.isNullable())
		{
			return true;
		}

		if(top.equals(target))
		{
			return true;
		}

		if(top instanceof CSharpLambdaTypeRef && target instanceof CSharpLambdaTypeRef)
		{
			DotNetTypeRef[] targetParameters = ((CSharpLambdaTypeRef) target).getParameterTypes();
			DotNetTypeRef[] topParameters = ((CSharpLambdaTypeRef) top).getParameterTypes();
			if(topParameters.length != targetParameters.length)
			{
				return false;
			}
			for(int i = 0; i < targetParameters.length; i++)
			{
				DotNetTypeRef targetParameter = targetParameters[i];
				DotNetTypeRef topParameter = topParameters[i];
				if(topParameter == DotNetTypeRef.AUTO_TYPE)
				{
					continue;
				}
				if(!isInheritable(topParameter, targetParameter, scope))
				{
					return false;
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaTypeRef) target).getReturnType();
			DotNetTypeRef topReturnType = ((CSharpLambdaTypeRef) top).getReturnType();
			return topReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope);
		}

		PsiElement topElement = top.resolve(scope);
		PsiElement targetElement = target.resolve(scope);
		if(topElement != null && topElement.isEquivalentTo(targetElement))
		{
			return true;
		}

		if(topElement instanceof DotNetTypeDeclaration && target instanceof CSharpNativeTypeRef)
		{
			if(Comparing.equal(((DotNetTypeDeclaration) topElement).getPresentableQName(), ((CSharpNativeTypeRef) target).getWrapperQualifiedClass
					()))
			{
				return true;
			}
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			return ((DotNetTypeDeclaration) topElement).isInheritor((DotNetTypeDeclaration) targetElement, true);
		}


		return false;
	}
}
