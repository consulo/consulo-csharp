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

import gnu.trove.THashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpUserTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetPointerTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;

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

	@RequiredReadAction
	public static boolean isErrorTypeRef(@Nonnull DotNetTypeRef typeRef)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return true;
		}
		else if(typeRef instanceof CSharpUserTypeRef)
		{
			return typeRef.resolve().getElement() == null;
		}
		return false;
	}

	@RequiredReadAction
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
						DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();
						if(typeResolveResult.isNullable())
						{
							return true;
						}
					}
				}
			}
			return false;
		}
		return true;
	}

	@Nullable
	@RequiredReadAction
	public static Pair<DotNetTypeDeclaration, DotNetGenericExtractor> findTypeInSuper(@Nonnull DotNetTypeRef typeRef, @Nonnull String vmQName)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
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
			Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = findTypeInSuper(superType, vmQName);
			if(typeInSuper != null)
			{
				return typeInSuper;
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeResolveResult findTypeRefFromExtends(@Nonnull DotNetTypeRef typeRef, @Nonnull DotNetTypeRef otherTypeRef, @Nonnull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = otherTypeRef.resolve();

		PsiElement element = typeResolveResult.getElement();
		if(!(element instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		return findTypeRefFromExtends(typeRef, (DotNetTypeDeclaration) element, scope, new THashSet<String>());
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeResolveResult findTypeRefFromExtends(@Nonnull final DotNetTypeRef typeRef,
			@Nonnull final DotNetTypeDeclaration typeDeclaration,
			@Nonnull final PsiElement scope,
			@Nonnull Set<String> processed)
	{
		final DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		final PsiElement resolvedElement = typeResolveResult.getElement();
		if(!(resolvedElement instanceof DotNetTypeDeclaration))
		{
			return null;
		}

		if(typeDeclaration.isEquivalentTo(resolvedElement))
		{
			return typeResolveResult;
		}

		if(!processed.add(((DotNetTypeDeclaration) resolvedElement).getVmQName()))
		{
			return null;
		}

		DotNetTypeRef[] extendTypeRefs = ((DotNetTypeDeclaration) resolvedElement).getExtendTypeRefs();

		for(DotNetTypeRef extendTypeRef : extendTypeRefs)
		{
			extendTypeRef = GenericUnwrapTool.exchangeTypeRef(extendTypeRef, typeResolveResult.getGenericExtractor(), scope);

			DotNetTypeResolveResult findTypeRefFromExtends = findTypeRefFromExtends(extendTypeRef, typeDeclaration, scope, processed);
			if(findTypeRefFromExtends != null)
			{
				return findTypeRefFromExtends;
			}
		}
		return null;
	}

	@RequiredReadAction
	public static boolean isInheritableWithImplicit(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope)
	{
		return isInheritable(top, target, scope, CSharpCastType.IMPLICIT).isSuccess();
	}

	@RequiredReadAction
	public static boolean isInheritableWithExplicit(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope)
	{
		return isInheritable(top, target, scope, CSharpCastType.EXPLICIT).isSuccess();
	}

	/**
	 * We have expression
	 * int a = "test";
	 * <p/>
	 * "test" - string type, ill be 'target' parameter
	 * int - int type, ill 'top'
	 * return false due it not be casted
	 */
	@RequiredReadAction
	public static boolean isInheritable(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope)
	{
		return isInheritable(top, target, scope, null).isSuccess();
	}

	@Nonnull
	@RequiredReadAction
	public static InheritResult isInheritable(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull PsiElement scope, @Nullable CSharpCastType castType)
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
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), scope, castType);
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
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), scope, castType);
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
					if(!isInheritable(topParameter, targetParameter, scope, castType).isSuccess())
					{
						return fail();
					}
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaResolveResult) targetTypeResolveResult).getReturnTypeRef();
			DotNetTypeRef topReturnType = ((CSharpLambdaResolveResult) topTypeResolveResult).getReturnTypeRef();

			boolean result = targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope, castType).isSuccess();
			return result ? SIMPLE_SUCCESS : FAIL;
		}

		PsiElement topElement = topTypeResolveResult.getElement();
		PsiElement targetElement = targetTypeResolveResult.getElement();
		if(topElement == null && targetElement == null && top instanceof CSharpUserTypeRef && target instanceof CSharpUserTypeRef)
		{
			return ((CSharpUserTypeRef) top).getReferenceText().equals(((CSharpUserTypeRef) target).getReferenceText()) ? SIMPLE_SUCCESS : FAIL;
		}

		if(topTypeResolveResult.isNullable() && target instanceof CSharpNullTypeRef)
		{
			return SIMPLE_SUCCESS;
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
				InheritResult inheritResult = haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) topElement, topGenericExtractor, scope, castType);
				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}

			if(targetElement instanceof DotNetTypeDeclaration)
			{
				InheritResult inheritResult = haveImplicitOrExplicitOperatorTo(top, target, (DotNetTypeDeclaration) targetElement, targetTypeResolveResult.getGenericExtractor(), scope,
						castType);

				if(inheritResult.isSuccess())
				{
					return inheritResult;
				}
			}
		}

		// dont allow not nullable type to nullable
		if(!topTypeResolveResult.isNullable() && targetTypeResolveResult.isNullable())
		{
			return fail();
		}

		if(topGenericExtractor != DotNetGenericExtractor.EMPTY && topElement instanceof DotNetTypeDeclaration)
		{
			DotNetTypeDeclaration topTypeDeclaration = (DotNetTypeDeclaration) topElement;
			DotNetTypeResolveResult typeFromSuper = findTypeRefFromExtends(target, topTypeDeclaration, scope, new THashSet<String>());

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
			return isInheritable(((CSharpTypeDefStatement) topElement).toTypeRef(), target, scope, castType);
		}

		if(targetElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(top, ((CSharpTypeDefStatement) targetElement).toTypeRef(), scope, castType);
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			if(((DotNetTypeDeclaration) targetElement).isInheritor(((DotNetTypeDeclaration) topElement).getVmQName(), true))
			{
				return SIMPLE_SUCCESS;
			}
		}

		if(topElement instanceof CSharpGenericParameter)
		{
			DotNetTypeRef[] extendTypes = ((CSharpGenericParameter) topElement).getExtendTypeRefs();

			for(DotNetTypeRef extendType : extendTypes)
			{
				InheritResult inheritable = isInheritable(extendType, target, scope, castType);
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
				InheritResult inheritable = isInheritable(top, extendType, scope, castType);
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
		//return FAIL;
		return new InheritResult(false, null);
	}

	@Nonnull
	@RequiredReadAction
	private static InheritResult haveImplicitOrExplicitOperatorTo(@Nonnull DotNetTypeRef to,
			@Nonnull DotNetTypeRef from,
			@Nonnull DotNetTypeDeclaration typeDeclaration,
			@Nonnull DotNetGenericExtractor extractor,
			@Nonnull PsiElement scope,
			@Nonnull CSharpCastType explicitOrImplicit)
	{
		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope.getResolveScope(), typeDeclaration);

		CSharpElementGroup<CSharpConversionMethodDeclaration> conversionMethodGroup = context.findConversionMethodGroup(explicitOrImplicit, true);
		if(conversionMethodGroup == null)
		{
			return fail();
		}

		// we need swap to vs from for explicit
		if(explicitOrImplicit == CSharpCastType.EXPLICIT)
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

	@Nonnull
	@RequiredReadAction
	public static List<DotNetTypeRef> getImplicitOrExplicitTypeRefs(@Nonnull DotNetTypeRef fromTypeRef,
			@Nonnull DotNetTypeRef leftTypeRef,
			@Nonnull CSharpCastType explicitOrImplicit,
			@Nonnull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = fromTypeRef.resolve();

		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return Collections.emptyList();
		}

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope.getResolveScope(), typeResolveResultElement);

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

	@RequiredReadAction
	public static boolean isTypeEqual(@Nonnull DotNetTypeRef t1, @Nonnull DotNetTypeRef t2, @Nonnull PsiElement scope)
	{
		if(t1 == DotNetTypeRef.ERROR_TYPE || t2 == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		t1 = GenericUnwrapTool.exchangeTypeRef(t1, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE, scope);
		t2 = GenericUnwrapTool.exchangeTypeRef(t2, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE, scope);

		if(t1 instanceof CSharpArrayTypeRef && t2 instanceof CSharpArrayTypeRef)
		{
			return ((CSharpArrayTypeRef) t1).getDimensions() == ((CSharpArrayTypeRef) t2).getDimensions() && isTypeEqual(((CSharpArrayTypeRef) t1).getInnerTypeRef(),
					((CSharpArrayTypeRef) t2).getInnerTypeRef(), scope);
		}

		DotNetTypeResolveResult resolveResult1 = t1.resolve();
		DotNetTypeResolveResult resolveResult2 = t2.resolve();

		if(resolveResult1.isNullable() != resolveResult2.isNullable())
		{
			return false;
		}
		PsiElement element1 = resolveResult1.getElement();
		PsiElement element2 = resolveResult2.getElement();

		if(element1 == null && element2 == null && t1 instanceof CSharpUserTypeRef && t2 instanceof CSharpUserTypeRef)
		{
			return ((CSharpUserTypeRef) t1).getReferenceText().equals(((CSharpUserTypeRef) t2).getReferenceText());
		}

		if(element1 == null || element2 == null)
		{
			return false;
		}

		if(element1 instanceof DotNetGenericParameter && element2 instanceof DotNetGenericParameter)
		{
			if(isMethodGeneric(element1) && isMethodGeneric(element2) && ((DotNetGenericParameter) element1).getIndex() == ((DotNetGenericParameter) element2).getIndex())
			{
				return true;
			}

			if(isTypeGeneric(element1) && isTypeGeneric(element2) && ((DotNetGenericParameter) element1).getIndex() == ((DotNetGenericParameter) element2).getIndex())
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

				if(!isTypeEqual(ObjectUtil.notNull(extractedRef1, DotNetTypeRef.ERROR_TYPE), ObjectUtil.notNull(extractedRef2, DotNetTypeRef.ERROR_TYPE), scope))
				{
					return false;
				}
			}
		}
		return true;
	}

	@RequiredReadAction
	private static boolean isMethodGeneric(PsiElement element)
	{
		if(!(element instanceof DotNetGenericParameter))
		{
			return false;
		}
		PsiElement parent = element.getParent();
		if(!(parent instanceof DotNetGenericParameterList))
		{
			return false;
		}
		PsiElement parentParent = parent.getParent();
		return parentParent instanceof DotNetLikeMethodDeclaration;
	}

	@RequiredReadAction
	private static boolean isTypeGeneric(PsiElement element)
	{
		if(!(element instanceof DotNetGenericParameter))
		{
			return false;
		}
		PsiElement parent = element.getParent();
		if(!(parent instanceof DotNetGenericParameterList))
		{
			return false;
		}
		PsiElement parentParent = parent.getParent();
		return parentParent instanceof CSharpTypeDeclaration;
	}

	@Nullable
	@RequiredReadAction
	public static Pair<String, DotNetTypeDeclaration> resolveTypeElement(@Nonnull DotNetTypeRef typeRef)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(typeResolveResultElement instanceof DotNetTypeDeclaration)
		{
			return Pair.create(((DotNetTypeDeclaration) typeResolveResultElement).getVmQName(), (DotNetTypeDeclaration) typeResolveResultElement);
		}
		return null;
	}
}
