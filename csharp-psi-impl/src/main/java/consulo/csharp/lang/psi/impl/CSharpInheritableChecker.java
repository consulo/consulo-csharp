/*
 * Copyright 2013-2020 consulo.io
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

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationProperties;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.psi.impl.source.CSharpOutRefAutoTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.*;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-09-19
 */
public class CSharpInheritableChecker
{
	/**
	 * We have expression
	 * int a = "test";
	 * <p/>
	 * "test" - string type, ill be 'target' parameter
	 * int - int type, ill 'top'
	 * return false due it not be casted
	 */
	public static CSharpInheritableChecker create(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope)
	{
		return new CSharpInheritableChecker(top, target, scope);
	}

	private final DotNetTypeRef myTop;
	private final DotNetTypeRef myTarget;
	private final PsiElement myScope;

	private CSharpCastType myCastType;
	private boolean myDisableNullableCheck;

	private CSharpInheritableChecker(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope)
	{
		myTop = top;
		myTarget = target;
		myScope = scope;
	}

	@Nonnull
	public CSharpInheritableChecker withCastType(@Nonnull CSharpCastType castType)
	{
		myCastType = castType;
		return this;
	}

	public CSharpInheritableChecker withDisableNullableCheck()
	{
		myDisableNullableCheck = true;
		return this;
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTypeUtil.InheritResult check()
	{
		return isInheritable(myTop, myTarget, myScope, myCastType, myDisableNullableCheck);
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpTypeUtil.InheritResult isInheritable(@Nonnull DotNetTypeRef top,
															  @Nonnull DotNetTypeRef target,
															  @Nonnull PsiElement scope,
															  @Nullable CSharpCastType castType,
															  boolean disableNullableCheck)
	{
		if(top == DotNetTypeRef.ERROR_TYPE || target == DotNetTypeRef.ERROR_TYPE)
		{
			return fail();
		}

		if(top.equals(target))
		{
			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(target instanceof CSharpFastImplicitTypeRef)
		{
			DotNetTypeRef implicitTypeRef = ((CSharpFastImplicitTypeRef) target).doMirror(top, scope);
			if(implicitTypeRef != null)
			{
				return new CSharpTypeUtil.InheritResult(true, ((CSharpFastImplicitTypeRef) target).isConversion());
			}
		}

		if(target instanceof CSharpRefTypeRef && top instanceof CSharpRefTypeRef)
		{
			if(((CSharpRefTypeRef) target).getType() != ((CSharpRefTypeRef) top).getType())
			{
				return fail();
			}
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), scope, castType, disableNullableCheck);
		}

		if(top instanceof CSharpRefTypeRef && target instanceof CSharpOutRefAutoTypeRef)
		{
			if(((CSharpRefTypeRef) top).getType() != ((CSharpOutRefAutoTypeRef) target).getType())
			{
				return fail();
			}

			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(target instanceof CSharpRefTypeRef)
		{
			target = ((CSharpRefTypeRef) target).getInnerTypeRef();
		}

		if(top instanceof CSharpRefTypeRef)
		{
			top = ((CSharpRefTypeRef) top).getInnerTypeRef();
		}

		if(target instanceof DotNetPointerTypeRef || top instanceof DotNetPointerTypeRef)
		{
			if(target instanceof DotNetPointerTypeRef && !(top instanceof DotNetPointerTypeRef))
			{
				return fail();
			}

			if(top instanceof DotNetPointerTypeRef && !(target instanceof DotNetPointerTypeRef))
			{
				return fail();
			}

			DotNetTypeRef topInnerTypeRef = ((DotNetPointerTypeRef) top).getInnerTypeRef();
			// void* is unknown type for all
			if(DotNetTypeRefUtil.isVmQNameEqual(topInnerTypeRef, scope, DotNetTypes.System.Void))
			{
				return CSharpTypeUtil.SIMPLE_SUCCESS;
			}
			return CSharpTypeUtil.isTypeEqual(topInnerTypeRef, ((DotNetPointerTypeRef) target).getInnerTypeRef(), scope) ? CSharpTypeUtil.SIMPLE_SUCCESS : fail();
		}

		if(target instanceof CSharpTupleTypeRef && top instanceof CSharpTupleTypeRef)
		{
			DotNetTypeRef[] targetTypeRefs = ((CSharpTupleTypeRef) target).getTypeRefs();
			DotNetTypeRef[] topTypeRefs = ((CSharpTupleTypeRef) top).getTypeRefs();

			if(targetTypeRefs.length != topTypeRefs.length)
			{
				return fail();
			}

			for(int i = 0; i < topTypeRefs.length; i++)
			{
				DotNetTypeRef topTypeRef = topTypeRefs[i];
				DotNetTypeRef targetTypeRef = targetTypeRefs[i];

				CSharpTypeUtil.InheritResult inheritable = isInheritable(topTypeRef, targetTypeRef, scope, castType, disableNullableCheck);
				if(!inheritable.isSuccess())
				{
					return fail();
				}
			}

			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(target instanceof CSharpArrayTypeRef && top instanceof CSharpArrayTypeRef)
		{
			if(((CSharpArrayTypeRef) target).getDimensions() != ((CSharpArrayTypeRef) top).getDimensions())
			{
				return fail();
			}
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), scope, castType, disableNullableCheck);
		}

		DotNetTypeResolveResult topTypeResolveResult = top.resolve();
		DotNetTypeResolveResult targetTypeResolveResult = target.resolve();
		if(topTypeResolveResult instanceof CSharpLambdaResolveResult && targetTypeResolveResult instanceof CSharpLambdaResolveResult)
		{
			if(!((CSharpLambdaResolveResult) targetTypeResolveResult).isInheritParameters())
			{
				DotNetTypeRef[] targetParameters = ((CSharpLambdaResolveResult) targetTypeResolveResult).getParameterTypeRefs();
				DotNetTypeRef[] topParameters = ((CSharpLambdaResolveResult) topTypeResolveResult).getParameterTypeRefs();
				if(topParameters.length != targetParameters.length)
				{
					return fail();
				}
				for(int i = 0; i < targetParameters.length; i++)
				{
					DotNetTypeRef targetParameter = targetParameters[i];
					DotNetTypeRef topParameter = topParameters[i];
					if(targetParameter == DotNetTypeRef.AUTO_TYPE)
					{
						continue;
					}
					if(!isInheritable(topParameter, targetParameter, scope, castType, disableNullableCheck).isSuccess())
					{
						return fail();
					}
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaResolveResult) targetTypeResolveResult).getReturnTypeRef();
			DotNetTypeRef topReturnType = ((CSharpLambdaResolveResult) topTypeResolveResult).getReturnTypeRef();

			boolean result = targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope, castType, disableNullableCheck).isSuccess();
			return result ? CSharpTypeUtil.SIMPLE_SUCCESS : fail();
		}

		PsiElement topElement = topTypeResolveResult.getElement();
		PsiElement targetElement = targetTypeResolveResult.getElement();
		if(topElement == null && targetElement == null && top instanceof CSharpUserTypeRef && target instanceof CSharpUserTypeRef)
		{
			return ((CSharpUserTypeRef) top).getReferenceText().equals(((CSharpUserTypeRef) target).getReferenceText()) ? CSharpTypeUtil.SIMPLE_SUCCESS : fail();
		}

		if(topTypeResolveResult.isNullable() && target instanceof CSharpNullTypeRef)
		{
			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(!topTypeResolveResult.isNullable() && target instanceof CSharpNullTypeRef)
		{
			return fail();
		}

		DotNetGenericExtractor topGenericExtractor = topTypeResolveResult.getGenericExtractor();

		if(castType != null)
		{
			if(topElement instanceof DotNetTypeDeclaration)
			{
				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) topElement, topGenericExtractor, scope, castType);
				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}

			if(targetElement instanceof DotNetTypeDeclaration)
			{
				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) targetElement, targetTypeResolveResult
								.getGenericExtractor(), scope,
						castType);

				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}
		}

		if(!disableNullableCheck)
		{
			// dont allow not nullable type to nullable
			if(!topTypeResolveResult.isNullable() && targetTypeResolveResult.isNullable())
			{
				return fail();
			}
		}

		if(topGenericExtractor != DotNetGenericExtractor.EMPTY && topElement instanceof DotNetTypeDeclaration)
		{
			DotNetTypeDeclaration topTypeDeclaration = (DotNetTypeDeclaration) topElement;
			DotNetTypeResolveResult typeFromSuper = CSharpTypeUtil.findTypeRefFromExtends(target, topTypeDeclaration, scope, new THashSet<>());

			if(typeFromSuper == null)
			{
				return fail();
			}

			DotNetGenericExtractor superGenericExtractor = typeFromSuper.getGenericExtractor();

			if(targetElement instanceof DotNetTypeDeclaration)
			{
				// we already check for equals inside findTypeRefFromExtends

				DotNetGenericParameter[] genericParameters = ((DotNetTypeDeclaration) topElement).getGenericParameters();

				for(DotNetGenericParameter genericParameter : genericParameters)
				{
					DotNetTypeRef topExtractedTypeRef = topGenericExtractor.extract(genericParameter);
					DotNetTypeRef superExtractedTypeRef = superGenericExtractor.extract(genericParameter);

					if(topExtractedTypeRef == null || superExtractedTypeRef == null)
					{
						return fail();
					}

					if(genericParameter.hasModifier(CSharpModifier.OUT))
					{
						if(!isInheritable(topExtractedTypeRef, superExtractedTypeRef, scope, null, disableNullableCheck).isSuccess())
						{
							return fail();
						}
					}
					else if(genericParameter.hasModifier(CSharpModifier.IN))
					{
						if(!isInheritable(superExtractedTypeRef, topExtractedTypeRef, scope, null, disableNullableCheck).isSuccess())
						{
							return fail();
						}
					}
					else
					{
						if(!CSharpTypeUtil.isTypeEqual(topExtractedTypeRef, superExtractedTypeRef, scope))
						{
							return fail();
						}
					}
				}

				return CSharpTypeUtil.SIMPLE_SUCCESS;
			}
		}
		else
		{
			if(topElement != null && topElement.isEquivalentTo(targetElement))
			{
				return CSharpTypeUtil.SIMPLE_SUCCESS;
			}
		}

		if(topElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(((CSharpTypeDefStatement) topElement).toTypeRef(), target, scope, castType, disableNullableCheck);
		}

		if(targetElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(top, ((CSharpTypeDefStatement) targetElement).toTypeRef(), scope, castType, disableNullableCheck);
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			if(((DotNetTypeDeclaration) targetElement).isInheritor(((DotNetTypeDeclaration) topElement).getVmQName(), true))
			{
				return CSharpTypeUtil.SIMPLE_SUCCESS;
			}
		}

		if(topElement instanceof CSharpGenericParameter)
		{
			DotNetTypeRef[] extendTypes = ((CSharpGenericParameter) topElement).getExtendTypeRefs();

			for(DotNetTypeRef extendType : extendTypes)
			{
				CSharpTypeUtil.InheritResult inheritable = isInheritable(extendType, target, scope, castType, disableNullableCheck);
				if(inheritable.isSuccess())
				{
					return inheritable;
				}
			}
		}

		if(targetElement instanceof CSharpGenericParameter)
		{
			DotNetTypeRef[] extendTypes = ((CSharpGenericParameter) targetElement).getExtendTypeRefs();

			for(DotNetTypeRef extendType : extendTypes)
			{
				CSharpTypeUtil.InheritResult inheritable = isInheritable(extendType, top, scope, castType, disableNullableCheck);
				if(inheritable.isSuccess())
				{
					return inheritable;
				}
			}
		}

		return fail();
	}

	@Nonnull
	private static CSharpTypeUtil.InheritResult fail()
	{
		if(ApplicationProperties.isInSandbox())
		{
			return new CSharpTypeUtil.InheritResult(false, null);
		}
		return CSharpTypeUtil.FAIL;
	}
}
