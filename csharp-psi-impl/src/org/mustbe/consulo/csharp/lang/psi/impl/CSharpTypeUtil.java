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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithInnerTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 08.01.14
 */
public class CSharpTypeUtil
{
	private static final DotNetTypeRef[] ourNumberRanks = new DotNetTypeRef[]{
			CSharpNativeTypeRef.BYTE,
			CSharpNativeTypeRef.SBYTE,
			CSharpNativeTypeRef.SHORT,
			CSharpNativeTypeRef.USHORT,
			CSharpNativeTypeRef.INT,
			CSharpNativeTypeRef.UINT,
			CSharpNativeTypeRef.LONG,
			CSharpNativeTypeRef.ULONG,
			CSharpNativeTypeRef.FLOAT,
			CSharpNativeTypeRef.DOUBLE,
	};

	public static int getNumberRank(DotNetTypeRef typeRef)
	{
		return ArrayUtil.find(ourNumberRanks, typeRef);
	}

	/**
	 * We have expression
	 * int a = "test";
	 * <p/>
	 * "test" - string type, ill be 'target' parameter
	 * int - int type, ill 'top'
	 * return false due it not be casted
	 */
	public static boolean isInheritable(@NotNull DotNetTypeRef top, @NotNull DotNetTypeRef target, @NotNull PsiElement scope)
	{
		if(top == DotNetTypeRef.ERROR_TYPE || target == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		if(top.isNullable() && target == DotNetTypeRef.NULL_TYPE)
		{
			return true;
		}

		if(top.equals(target))
		{
			return true;
		}

		int topRank = ArrayUtil.find(ourNumberRanks, top);
		int targetRank = ArrayUtil.find(ourNumberRanks, target);

		if(topRank != -1 && targetRank != -1)
		{
			if(targetRank <= topRank)
			{
				return true;
			}
		}

		if(target instanceof CSharpRefTypeRef && top instanceof CSharpRefTypeRef)
		{
			if(((CSharpRefTypeRef) target).getType() != ((CSharpRefTypeRef) top).getType())
			{
				return false;
			}
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), scope);
		}

		if(target instanceof CSharpArrayTypeRef && top instanceof CSharpArrayTypeRef)
		{
			if(((CSharpArrayTypeRef) target).getDimensions() != ((CSharpArrayTypeRef) top).getDimensions())
			{
				return false;
			}
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), scope);
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
				if(targetParameter == DotNetTypeRef.AUTO_TYPE)
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
			return targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope);
		}

		PsiElement topElement = top.resolve(scope);
		PsiElement targetElement = target.resolve(scope);
		if(topElement != null && topElement.isEquivalentTo(targetElement))
		{
			return true;
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			return ((DotNetTypeDeclaration) targetElement).isInheritor((DotNetTypeDeclaration) topElement, true);
		}

		return false;
	}

	public static boolean haveErrorType(DotNetTypeRef typeRef)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return true;
		}

		if(typeRef instanceof DotNetTypeRefWithInnerTypeRef)
		{
			if(typeRef instanceof DotNetGenericWrapperTypeRef)
			{
				for(DotNetTypeRef dotNetTypeRef : ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs())
				{
					if(haveErrorType(dotNetTypeRef))
					{
						return true;
					}
				}
			}
			return haveErrorType(((DotNetTypeRefWithInnerTypeRef) typeRef).getInnerTypeRef());
		}

		return false;
	}
}
