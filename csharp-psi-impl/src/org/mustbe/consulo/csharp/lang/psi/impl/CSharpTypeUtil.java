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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithInnerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 08.01.14
 */
public class CSharpTypeUtil
{
	public static class InheritResult
	{
		private final boolean mySuccess;
		private final boolean myConversion;
		private final CSharpConversionMethodDeclaration myConversionMethod;
		//private final String myExceptionText = ExceptionUtil.getThrowableText(new Exception());

		public InheritResult(boolean success, boolean conversion)
		{
			this(success, conversion, null);
		}

		public InheritResult(boolean success, CSharpConversionMethodDeclaration conversionMethod)
		{
			this(success, conversionMethod != null, conversionMethod);
		}

		public InheritResult(boolean success, boolean conversion, CSharpConversionMethodDeclaration conversionMethod)
		{
			mySuccess = success;
			myConversion = conversion;
			myConversionMethod = conversionMethod;
		}

		public boolean isConversion()
		{
			return myConversion;
		}

		@Nullable
		public CSharpConversionMethodDeclaration getConversionMethod()
		{
			return myConversionMethod;
		}

		public boolean isSuccess()
		{
			return mySuccess;
		}
	}

	public static final InheritResult FAIL = new InheritResult(false, null);
	public static final InheritResult SIMPLE_SUCCESS = new InheritResult(true, null);

	public static boolean isNullableElement(@Nullable PsiElement element)
	{
		if(element instanceof DotNetTypeDeclaration)
		{
			if(DotNetTypes.System.Nullable$1.equals(((DotNetTypeDeclaration) element).getVmQName()))
			{
				// special case - compiler box element in new Nullable<int>(null);
				return true;
			}
			return !((DotNetTypeDeclaration) element).isStruct() && !((DotNetTypeDeclaration) element).isEnum();
		}
		else if(element instanceof DotNetGenericParameter)
		{
			CSharpGenericConstraint genericConstraint = CSharpGenericConstraintUtil.findGenericConstraint((DotNetGenericParameter) element);

			if(genericConstraint != null)
			{
				for(CSharpGenericConstraintValue genericConstraintValue : genericConstraint.getGenericConstraintValues())
				{
					if(genericConstraintValue instanceof CSharpGenericConstraintKeywordValue)
					{
						IElementType keywordElementType = ((CSharpGenericConstraintKeywordValue) genericConstraintValue).getKeywordElementType();
						if(keywordElementType == CSharpTokens.STRUCT_KEYWORD)
						{
							return false;
						}
						else if(keywordElementType == CSharpTokens.CLASS_KEYWORD)
						{
							return true;
						}
					}
					else if(genericConstraintValue instanceof CSharpGenericConstraintTypeValue)
					{
						DotNetTypeRef dotNetTypeRef = ((CSharpGenericConstraintTypeValue) genericConstraintValue).toTypeRef();
						DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(element);
						if(!typeResolveResult.isNullable())
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@Nullable
	public static Pair<DotNetTypeDeclaration, DotNetGenericExtractor> findTypeInSuper(@NotNull DotNetTypeRef typeRef,
			@NotNull String vmQName,
			@NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement resolve = typeResolveResult.getElement();
		if(!(resolve instanceof DotNetTypeDeclaration))
		{
			return null;
		}

		String otherVQName = ((DotNetTypeDeclaration) resolve).getVmQName();
		if(vmQName.equals(otherVQName))
		{
			return Pair.create((DotNetTypeDeclaration) resolve, typeResolveResult.getGenericExtractor());
		}

		for(DotNetTypeRef superType : ((DotNetTypeDeclaration) resolve).getExtendTypeRefs())
		{
			Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = findTypeInSuper(superType, vmQName, scope);
			if(typeInSuper != null)
			{
				return typeInSuper;
			}
		}
		return null;
	}

	@Nullable
	public static DotNetTypeResolveResult findTypeRefFromExtends(@NotNull DotNetTypeRef typeRef,
			@NotNull DotNetTypeRef otherTypeRef,
			@NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = otherTypeRef.resolve(scope);

		PsiElement element = typeResolveResult.getElement();
		if(!(element instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		return findTypeRefFromExtends(typeRef, (DotNetTypeDeclaration) element, scope);
	}

	@Nullable
	public static DotNetTypeResolveResult findTypeRefFromExtends(@NotNull final DotNetTypeRef typeRef,
			@NotNull final DotNetTypeDeclaration typeDeclaration,
			@NotNull final PsiElement scope)
	{
		final DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		final PsiElement resolvedElement = typeResolveResult.getElement();
		if(!(resolvedElement instanceof DotNetTypeDeclaration))
		{
			return null;
		}

		if(typeDeclaration.isEquivalentTo(resolvedElement))
		{
			return typeResolveResult;
		}

		return RecursionManager.doPreventingRecursion(typeDeclaration, false,new Computable<DotNetTypeResolveResult>()
		{
			@Override
			public DotNetTypeResolveResult compute()
			{
				for(DotNetTypeRef extendTypeRef : ((DotNetTypeDeclaration) resolvedElement).getExtendTypeRefs())
				{
					extendTypeRef = GenericUnwrapTool.exchangeTypeRef(extendTypeRef, typeResolveResult.getGenericExtractor(), scope);

					DotNetTypeResolveResult findTypeRefFromExtends = findTypeRefFromExtends(extendTypeRef, typeDeclaration, scope);
					if(findTypeRefFromExtends != null)
					{
						return findTypeRefFromExtends;
					}
				}
				return null;
			}
		}) ;
	}

	public static boolean isInheritableWithImplicit(@NotNull DotNetTypeRef top, @NotNull DotNetTypeRef target, @NotNull PsiElement scope)
	{
		return isInheritable(top, target, scope, CSharpStaticTypeRef.IMPLICIT).isSuccess();
	}

	public static boolean isInheritableWithExplicit(@NotNull DotNetTypeRef top, @NotNull DotNetTypeRef target, @NotNull PsiElement scope)
	{
		return isInheritable(top, target, scope, CSharpStaticTypeRef.EXPLICIT).isSuccess();
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
		return isInheritable(top, target, scope, null).isSuccess();
	}

	@NotNull
	@RequiredReadAction
	public static InheritResult isInheritable(@NotNull DotNetTypeRef top,
			@NotNull DotNetTypeRef target,
			@NotNull PsiElement scope,
			@Nullable CSharpStaticTypeRef explicitOrImplicit)
	{
		if(top == DotNetTypeRef.ERROR_TYPE || target == DotNetTypeRef.ERROR_TYPE)
		{
			return fail();
		}

		if(top.equals(target))
		{
			return SIMPLE_SUCCESS;
		}

		if(target instanceof CSharpFastImplicitTypeRef)
		{
			DotNetTypeRef implicitTypeRef = ((CSharpFastImplicitTypeRef) target).doMirror(top, scope);
			if(implicitTypeRef != null)
			{
				return new InheritResult(true, ((CSharpFastImplicitTypeRef) target).isConversion());
			}
		}

		if(target instanceof CSharpRefTypeRef && top instanceof CSharpRefTypeRef)
		{
			if(((CSharpRefTypeRef) target).getType() != ((CSharpRefTypeRef) top).getType())
			{
				return fail();
			}
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), scope,
					explicitOrImplicit);
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
				return SIMPLE_SUCCESS;
			}
			return isTypeEqual(topInnerTypeRef, ((DotNetPointerTypeRef) target).getInnerTypeRef(), scope) ? SIMPLE_SUCCESS : fail();
		}

		if(target instanceof CSharpArrayTypeRef && top instanceof CSharpArrayTypeRef)
		{
			if(((CSharpArrayTypeRef) target).getDimensions() != ((CSharpArrayTypeRef) top).getDimensions())
			{
				return fail();
			}
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), scope,
					explicitOrImplicit);
		}

		DotNetTypeResolveResult topTypeResolveResult = top.resolve(scope);
		DotNetTypeResolveResult targetTypeResolveResult = target.resolve(scope);
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
					if(!isInheritable(topParameter, targetParameter, scope, explicitOrImplicit).isSuccess())
					{
						return fail();
					}
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaResolveResult) targetTypeResolveResult).getReturnTypeRef();
			DotNetTypeRef topReturnType = ((CSharpLambdaResolveResult) topTypeResolveResult).getReturnTypeRef();

			boolean result = targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope,
					explicitOrImplicit).isSuccess();
			return result ? SIMPLE_SUCCESS : FAIL;
		}

		PsiElement topElement = topTypeResolveResult.getElement();
		PsiElement targetElement = targetTypeResolveResult.getElement();

		if(topTypeResolveResult.isNullable() && target == CSharpNullTypeRef.INSTANCE)
		{
			return SIMPLE_SUCCESS;
		}

		if(!topTypeResolveResult.isNullable() && target == CSharpNullTypeRef.INSTANCE)
		{
			return fail();
		}

		DotNetGenericExtractor topGenericExtractor = topTypeResolveResult.getGenericExtractor();

		if(explicitOrImplicit != null)
		{
			if(topElement instanceof DotNetTypeDeclaration)
			{
				InheritResult inheritResult = haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) topElement, topGenericExtractor,
						scope, explicitOrImplicit);
				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}

			if(targetElement instanceof DotNetTypeDeclaration)
			{
				InheritResult inheritResult = haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) targetElement,
						targetTypeResolveResult.getGenericExtractor(), scope, explicitOrImplicit);

				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}
		}

		if(topGenericExtractor != DotNetGenericExtractor.EMPTY && topElement instanceof DotNetTypeDeclaration)
		{
			DotNetTypeDeclaration topTypeDeclaration = (DotNetTypeDeclaration) topElement;
			DotNetTypeResolveResult typeFromSuper = findTypeRefFromExtends(target, topTypeDeclaration, scope);

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
						if(!isInheritable(topExtractedTypeRef, superExtractedTypeRef, scope, null).isSuccess())
						{
							return fail();
						}
					}
					else if(genericParameter.hasModifier(CSharpModifier.IN))
					{
						if(!isInheritable(superExtractedTypeRef, topExtractedTypeRef, scope, null).isSuccess())
						{
							return fail();
						}
					}
					else
					{
						if(!isTypeEqual(topExtractedTypeRef, superExtractedTypeRef, scope))
						{
							return fail();
						}
					}
				}

				return SIMPLE_SUCCESS;
			}
		}
		else
		{
			if(topElement != null && topElement.isEquivalentTo(targetElement))
			{
				return SIMPLE_SUCCESS;
			}
		}

		if(topElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(((CSharpTypeDefStatement) topElement).toTypeRef(), target, scope, explicitOrImplicit);
		}

		if(targetElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(top, ((CSharpTypeDefStatement) targetElement).toTypeRef(), scope, explicitOrImplicit);
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			if(((DotNetTypeDeclaration) targetElement).isInheritor(((DotNetTypeDeclaration) topElement).getVmQName(), true))
			{
				return SIMPLE_SUCCESS;
			}
		}

		if(topElement instanceof DotNetGenericParameter)
		{
			List<DotNetTypeRef> extendTypes = CSharpGenericConstraintUtil.getExtendTypes((DotNetGenericParameter) topElement);

			for(DotNetTypeRef extendType : extendTypes)
			{
				InheritResult inheritable = isInheritable(extendType, target, scope, explicitOrImplicit);
				if(inheritable.isSuccess())
				{
					return inheritable;
				}
			}
		}

		if(targetElement instanceof DotNetGenericParameter)
		{
			List<DotNetTypeRef> extendTypes = CSharpGenericConstraintUtil.getExtendTypes((DotNetGenericParameter) targetElement);

			for(DotNetTypeRef extendType : extendTypes)
			{
				InheritResult inheritable = isInheritable(top, extendType, scope, explicitOrImplicit);
				if(inheritable.isSuccess())
				{
					return inheritable;
				}
			}
		}

		return fail();
	}

	private static InheritResult fail()
	{
		return FAIL;
		//return new InheritResult(false, null);
	}

	@NotNull
	private static InheritResult haveImplicitOrExplicitOperatorTo(@NotNull DotNetTypeRef to,
			@NotNull DotNetTypeRef from,
			@NotNull DotNetTypeDeclaration typeDeclaration,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull PsiElement scope,
			@NotNull DotNetTypeRef explicitOrImplicit)
	{
		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope.getResolveScope(),
				typeDeclaration);

		CSharpElementGroup<CSharpConversionMethodDeclaration> conversionMethodGroup = context.findConversionMethodGroup(explicitOrImplicit, true);
		if(conversionMethodGroup == null)
		{
			return fail();
		}

		// we need swap to vs from for explicit
		if(explicitOrImplicit == CSharpStaticTypeRef.EXPLICIT)
		{
			DotNetTypeRef temp = to;
			to = from;
			from = temp;
		}

		for(CSharpConversionMethodDeclaration declaration : conversionMethodGroup.getElements())
		{
			// extract here
			declaration = GenericUnwrapTool.extract(declaration, extractor);

			if(!isInheritable(declaration.getReturnTypeRef(), to, scope))
			{
				continue;
			}

			DotNetTypeRef[] parameters = declaration.getParameterTypeRefs();
			DotNetTypeRef parameterTypeRef = ArrayUtil2.safeGet(parameters, 0);
			if(parameterTypeRef == null)
			{
				continue;
			}

			if(isInheritable(parameterTypeRef, from, scope))
			{
				return new InheritResult(true, declaration);
			}
		}
		return fail();
	}

	@NotNull
	public static List<DotNetTypeRef> getImplicitOrExplicitTypeRefs(@NotNull DotNetTypeRef fromTypeRef,
			@NotNull DotNetTypeRef leftTypeRef,
			@NotNull CSharpStaticTypeRef explicitOrImplicit,
			@NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = fromTypeRef.resolve(scope);

		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return Collections.emptyList();
		}

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope.getResolveScope(),
				typeResolveResultElement);

		CSharpElementGroup<CSharpConversionMethodDeclaration> conversionMethodGroup = context.findConversionMethodGroup(explicitOrImplicit, true);
		if(conversionMethodGroup == null)
		{
			return Collections.emptyList();
		}

		DotNetGenericExtractor extractor = typeResolveResult.getGenericExtractor();

		List<DotNetTypeRef> list = new SmartList<DotNetTypeRef>();
		for(CSharpConversionMethodDeclaration declaration : conversionMethodGroup.getElements())
		{
			// extract here
			declaration = GenericUnwrapTool.extract(declaration, extractor);

			DotNetTypeRef[] parameters = declaration.getParameterTypeRefs();
			DotNetTypeRef parameterTypeRef = ArrayUtil2.safeGet(parameters, 0);
			if(parameterTypeRef == null)
			{
				continue;
			}

			if(!isInheritable(parameterTypeRef, leftTypeRef, scope))
			{
				continue;
			}

			list.add(declaration.getReturnTypeRef());
		}
		return list;
	}

	public static boolean isTypeEqual(@NotNull DotNetTypeRef t1, @NotNull DotNetTypeRef t2, @NotNull PsiElement scope)
	{
		if(t1 == DotNetTypeRef.ERROR_TYPE || t2 == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		t1 = GenericUnwrapTool.exchangeTypeRef(t1, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE, scope);
		t2 = GenericUnwrapTool.exchangeTypeRef(t2, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE, scope);

		if(t1 instanceof CSharpArrayTypeRef && t2 instanceof CSharpArrayTypeRef)
		{
			return ((CSharpArrayTypeRef) t1).getDimensions() == ((CSharpArrayTypeRef) t2).getDimensions() && isTypeEqual(((CSharpArrayTypeRef) t1)
					.getInnerTypeRef(), ((CSharpArrayTypeRef) t2).getInnerTypeRef(), scope);
		}

		DotNetTypeResolveResult resolveResult1 = t1.resolve(scope);
		DotNetTypeResolveResult resolveResult2 = t2.resolve(scope);

		if(resolveResult1.isNullable() != resolveResult2.isNullable())
		{
			return false;
		}
		PsiElement element1 = resolveResult1.getElement();
		PsiElement element2 = resolveResult2.getElement();

		if(element1 == null || element2 == null)
		{
			return false;
		}


		if(element1 instanceof DotNetGenericParameter && element2 instanceof DotNetGenericParameter)
		{
			if(isMethodGeneric(element1) && isMethodGeneric(element2) && ((DotNetGenericParameter) element1).getIndex() == ((DotNetGenericParameter)
					element2).getIndex())
			{
				return true;
			}
		}

		if(!element1.isEquivalentTo(element2))
		{
			return false;
		}

		if(element1 instanceof DotNetGenericParameterListOwner)
		{
			assert element2 instanceof DotNetGenericParameterListOwner;

			DotNetGenericParameter[] genericParameters1 = ((DotNetGenericParameterListOwner) element1).getGenericParameters();
			DotNetGenericParameter[] genericParameters2 = ((DotNetGenericParameterListOwner) element2).getGenericParameters();

			if(genericParameters1.length != genericParameters2.length)
			{
				return false;
			}

			DotNetGenericExtractor genericExtractor1 = resolveResult1.getGenericExtractor();
			DotNetGenericExtractor genericExtractor2 = resolveResult2.getGenericExtractor();

			for(int i = 0; i < genericParameters1.length; i++)
			{
				DotNetGenericParameter genericParameter1 = genericParameters1[i];
				DotNetGenericParameter genericParameter2 = genericParameters2[i];

				DotNetTypeRef extractedRef1 = genericExtractor1.extract(genericParameter1);
				DotNetTypeRef extractedRef2 = genericExtractor2.extract(genericParameter2);

				if(extractedRef1 == null && extractedRef2 == null)
				{
					continue;
				}

				if(!isTypeEqual(ObjectUtil.notNull(extractedRef1, DotNetTypeRef.ERROR_TYPE), ObjectUtil.notNull(extractedRef2,
						DotNetTypeRef.ERROR_TYPE), scope))
				{
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isMethodGeneric(PsiElement element)
	{
		PsiElement originalElement = element.getOriginalElement();

		if(!(originalElement instanceof DotNetGenericParameter))
		{
			return false;
		}
		PsiElement parent = originalElement.getParent();
		if(!(parent instanceof DotNetGenericParameterList))
		{
			return false;
		}
		return parent.getParent() instanceof DotNetLikeMethodDeclaration;
	}

	public static boolean haveErrorType(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return true;
		}

		if(typeRef instanceof DotNetTypeRefWithInnerTypeRef)
		{
			return haveErrorType(((DotNetTypeRefWithInnerTypeRef) typeRef).getInnerTypeRef(), scope);
		}

		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);

		PsiElement element = typeResolveResult.getElement();
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		if(element instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) element).getGenericParameters();
			for(DotNetGenericParameter genericParameter : genericParameters)
			{
				DotNetTypeRef extractedTypeRef = genericExtractor.extract(genericParameter);
				if(extractedTypeRef != null && haveErrorType(extractedTypeRef, scope))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	public static Pair<String, DotNetTypeDeclaration> resolveTypeElement(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);

		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(typeResolveResultElement instanceof DotNetTypeDeclaration)
		{
			return Pair.create(((DotNetTypeDeclaration) typeResolveResultElement).getVmQName(), (DotNetTypeDeclaration) typeResolveResultElement);
		}
		return null;
	}
}
