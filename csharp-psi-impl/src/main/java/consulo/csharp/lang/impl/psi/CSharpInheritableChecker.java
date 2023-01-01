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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationProperties;
import consulo.application.progress.ProgressManager;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefAutoTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.lang.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

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
	public static CSharpInheritableChecker create(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target)
	{
		return new CSharpInheritableChecker(top, target);
	}

	private final DotNetTypeRef myTop;
	private final DotNetTypeRef myTarget;
	private CSharpInheritableCheckerContext myContext;

	private Pair<CSharpCastType, GlobalSearchScope> myCastTypeResolver;

	private CSharpInheritableChecker(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target)
	{
		myTop = top;
		myTarget = target;
	}

	@Nonnull
	public CSharpInheritableChecker withCastType(@Nonnull CSharpCastType castType, @Nonnull GlobalSearchScope resolveScope)
	{
		if(myCastTypeResolver != null)
		{
			throw new IllegalArgumentException("already set");
		}

		myCastTypeResolver = Pair.createNonNull(castType, resolveScope);
		return this;
	}

	@Nonnull
	public CSharpInheritableChecker withContext(@Nonnull CSharpInheritableCheckerContext context)
	{
		myContext = context;
		return this;
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTypeUtil.InheritResult check()
	{
		if(myTop == DotNetTypeRef.ERROR_TYPE || myTarget == DotNetTypeRef.ERROR_TYPE)
		{
			return fail();
		}

		if(myTop == DotNetTypeRef.AUTO_TYPE || myTarget == DotNetTypeRef.AUTO_TYPE)
		{
			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(myTop.equals(myTarget))
		{
			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(myTop instanceof DotNetTypeRef.AdapterInternal || myTarget instanceof DotNetTypeRef.AdapterInternal)
		{
			return CSharpTypeUtil.SIMPLE_SUCCESS;
		}

		if(myTop.getProject() != myTarget.getProject())
		{
			throw new IllegalArgumentException("different projects");
		}

		CSharpInheritableCheckerCacher checkerCacher = CSharpInheritableCheckerCacher.getInstance(myTop.getProject());

		return checkerCacher.getOrCheck(myTop, myTarget, myCastTypeResolver, myContext);
	}

	@Nonnull
	@RequiredReadAction
	static CSharpTypeUtil.InheritResult isInheritable(@Nonnull DotNetTypeRef top,
													  @Nonnull DotNetTypeRef target,
													  @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo,
													  @Nonnull CSharpInheritableCheckerContext context)
	{
		ProgressManager.checkCanceled();

		if(!context.mark(top, target, castResolvingInfo))
		{
			return fail();
		}

		if(target instanceof CSharpFastImplicitTypeRef)
		{
			DotNetTypeRef implicitTypeRef = ((CSharpFastImplicitTypeRef) target).doMirror(top);
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
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), castResolvingInfo, context);
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
			if(DotNetTypeRefUtil.isVmQNameEqual(topInnerTypeRef, DotNetTypes.System.Void))
			{
				return CSharpTypeUtil.SIMPLE_SUCCESS;
			}
			return CSharpTypeUtil.isTypeEqual(topInnerTypeRef, ((DotNetPointerTypeRef) target).getInnerTypeRef()) ? CSharpTypeUtil.SIMPLE_SUCCESS : fail();
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

				CSharpTypeUtil.InheritResult inheritable = isInheritable(topTypeRef, targetTypeRef, castResolvingInfo, context);
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
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), castResolvingInfo, context);
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
					if(!isInheritable(topParameter, targetParameter, castResolvingInfo, context).isSuccess())
					{
						return fail();
					}
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaResolveResult) targetTypeResolveResult).getReturnTypeRef();
			DotNetTypeRef topReturnType = ((CSharpLambdaResolveResult) topTypeResolveResult).getReturnTypeRef();

			boolean result = targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, castResolvingInfo, context).isSuccess();
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

		if(topGenericExtractor != DotNetGenericExtractor.EMPTY && topElement instanceof DotNetTypeDeclaration)
		{
			DotNetTypeDeclaration topTypeDeclaration = (DotNetTypeDeclaration) topElement;
			DotNetTypeResolveResult typeFromSuper = CSharpTypeUtil.findTypeRefFromExtends(target, topTypeDeclaration, new HashSet<>());

			if(typeFromSuper != null)
			{
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
							if(!isInheritable(topExtractedTypeRef, superExtractedTypeRef, null, context).isSuccess())
							{
								return fail();
							}
						}
						else if(genericParameter.hasModifier(CSharpModifier.IN))
						{
							if(!isInheritable(superExtractedTypeRef, topExtractedTypeRef, null, context).isSuccess())
							{
								return fail();
							}
						}
						else
						{
							if(!CSharpTypeUtil.isTypeEqual(topExtractedTypeRef, superExtractedTypeRef))
							{
								return fail();
							}
						}
					}

					return CSharpTypeUtil.SIMPLE_SUCCESS;
				}
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
			return isInheritable(((CSharpTypeDefStatement) topElement).toTypeRef(), target, castResolvingInfo, context);
		}

		if(targetElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(top, ((CSharpTypeDefStatement) targetElement).toTypeRef(), castResolvingInfo, context);
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
				CSharpTypeUtil.InheritResult inheritable = isInheritable(extendType, target, castResolvingInfo, context);
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
				CSharpTypeUtil.InheritResult inheritable = isInheritable(extendType, top, castResolvingInfo, context);
				if(inheritable.isSuccess())
				{
					return inheritable;
				}
			}
		}

		if(castResolvingInfo != null)
		{
			if(topElement instanceof DotNetTypeDeclaration)
			{
				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) topElement, topGenericExtractor,
						castResolvingInfo, context);
				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}

			if(targetElement instanceof DotNetTypeDeclaration)
			{
				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) targetElement, targetTypeResolveResult
						.getGenericExtractor(), castResolvingInfo, context);

				if(inheritResult.isSuccess())
				{
					return inheritResult;
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
