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

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpUserTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;
import consulo.util.lang.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		public boolean isExplicit()
		{
			return myConversionMethod != null && !myConversionMethod.isImplicit();
		}

		public boolean isImplicit()
		{
			return myConversionMethod != null && myConversionMethod.isImplicit();
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
			String vmQName = ((DotNetTypeDeclaration) element).getVmQName();
			if(DotNetTypes.System.Nullable$1.equals(vmQName))
			{
				// special case - compiler box element in new Nullable<int>(null);
				return true;
			}
			if(DotNetTypes.System.Enum.equals(vmQName))
			{
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
	public static DotNetTypeResolveResult findTypeRefFromExtends(@Nonnull DotNetTypeRef typeRef, @Nonnull DotNetTypeRef otherTypeRef)
	{
		DotNetTypeResolveResult typeResolveResult = otherTypeRef.resolve();

		PsiElement element = typeResolveResult.getElement();
		if(!(element instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		return findTypeRefFromExtends(typeRef, (DotNetTypeDeclaration) element, new HashSet<>());
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeResolveResult findTypeRefFromExtends(@Nonnull final DotNetTypeRef typeRef,
																 @Nonnull final DotNetTypeDeclaration typeDeclaration,
																 @Nonnull Set<PsiElement> processed)
	{
		final DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		final PsiElement resolvedElement = typeResolveResult.getElement();
		if(typeDeclaration.isEquivalentTo(resolvedElement))
		{
			return typeResolveResult;
		}

		if(resolvedElement instanceof DotNetTypeDeclaration)
		{
			if(!processed.add(resolvedElement))
			{
				return null;
			}

			DotNetTypeRef[] extendTypeRefs = ((DotNetTypeDeclaration) resolvedElement).getExtendTypeRefs();

			for(DotNetTypeRef extendTypeRef : extendTypeRefs)
			{
				extendTypeRef = GenericUnwrapTool.exchangeTypeRef(extendTypeRef, typeResolveResult.getGenericExtractor());

				DotNetTypeResolveResult findTypeRefFromExtends = findTypeRefFromExtends(extendTypeRef, typeDeclaration, processed);
				if(findTypeRefFromExtends != null)
				{
					return findTypeRefFromExtends;
				}
			}
		}
		else if(resolvedElement instanceof CSharpGenericParameter)
		{
			if(!processed.add(resolvedElement))
			{
				return null;
			}

			DotNetTypeRef[] extendTypeRefs = ((CSharpGenericParameter) resolvedElement).getExtendTypeRefs();

			for(DotNetTypeRef extendTypeRef : extendTypeRefs)
			{
				extendTypeRef = GenericUnwrapTool.exchangeTypeRef(extendTypeRef, typeResolveResult.getGenericExtractor());

				DotNetTypeResolveResult findTypeRefFromExtends = findTypeRefFromExtends(extendTypeRef, typeDeclaration, processed);
				if(findTypeRefFromExtends != null)
				{
					return findTypeRefFromExtends;
				}
			}
		}
		return null;
	}

	@RequiredReadAction
	@Deprecated
	public static boolean isInheritableWithImplicit(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull GlobalSearchScope implicitCastScope)
	{
		return CSharpInheritableChecker.create(top, target).withCastType(CSharpCastType.IMPLICIT, implicitCastScope).check().isSuccess();
	}

	@RequiredReadAction
	@Deprecated
	public static boolean isInheritableWithExplicit(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull GlobalSearchScope explicitCastScope)
	{
		return CSharpInheritableChecker.create(top, target).withCastType(CSharpCastType.EXPLICIT, explicitCastScope).check().isSuccess();
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
	public static boolean isInheritable(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target)
	{
		return CSharpInheritableChecker.create(top, target).check().isSuccess();
	}

	@Nonnull
	@RequiredReadAction
	public static InheritResult isInheritable(@Nonnull DotNetTypeRef top, @Nonnull DotNetTypeRef target, @Nonnull CSharpCastType castType, @Nonnull GlobalSearchScope castResolveScope)
	{
		return CSharpInheritableChecker.create(top, target).withCastType(castType, castResolveScope).check();
	}

	private static InheritResult fail()
	{
		return FAIL;
		//return new InheritResult(false, null);
	}

	@Nonnull
	@RequiredReadAction
	protected static InheritResult haveImplicitOrExplicitOperatorTo(@Nonnull DotNetTypeRef to,
																	@Nonnull DotNetTypeRef from,
																	@Nonnull DotNetTypeDeclaration typeDeclaration,
																	@Nonnull DotNetGenericExtractor extractor,
																	@Nonnull Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo,
																	@Nonnull CSharpInheritableCheckerContext context)
	{
		CSharpCastType castType = castResolvingInfo.getFirst();
		GlobalSearchScope resolveScope = castResolvingInfo.getSecond();

		CSharpResolveContext resolveContext = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, resolveScope, typeDeclaration);

		CSharpElementGroup<CSharpConversionMethodDeclaration> conversionMethodGroup = resolveContext.findConversionMethodGroup(castType, true);
		if(conversionMethodGroup == null)
		{
			return fail();
		}

		// we need swap to vs from for explicit
		if(castType == CSharpCastType.EXPLICIT)
		{
			DotNetTypeRef temp = to;
			to = from;
			from = temp;
		}

		for(CSharpConversionMethodDeclaration declaration : conversionMethodGroup.getElements())
		{
			// extract here
			declaration = GenericUnwrapTool.extract(declaration, extractor);

			if(!CSharpInheritableChecker.create(declaration.getReturnTypeRef(), to).withCastType(CSharpCastType.IMPLICIT, resolveScope).withContext(context).check().isSuccess())
			{
				continue;
			}

			DotNetTypeRef[] parameters = declaration.getParameterTypeRefs();
			DotNetTypeRef parameterTypeRef = ArrayUtil2.safeGet(parameters, 0);
			if(parameterTypeRef == null)
			{
				continue;
			}

			if(CSharpInheritableChecker.create(from, parameterTypeRef).withCastType(CSharpCastType.IMPLICIT, resolveScope).withContext(context).check().isSuccess())
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

		List<DotNetTypeRef> list = new SmartList<>();
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

			if(!isInheritable(parameterTypeRef, leftTypeRef))
			{
				continue;
			}

			list.add(declaration.getReturnTypeRef());
		}
		return list;
	}

	@RequiredReadAction
	public static boolean isTypeEqual(@Nonnull DotNetTypeRef t1, @Nonnull DotNetTypeRef t2)
	{
		if(t1 == DotNetTypeRef.ERROR_TYPE || t2 == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		t1 = GenericUnwrapTool.exchangeTypeRef(t1, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE);
		t2 = GenericUnwrapTool.exchangeTypeRef(t2, GenericUnwrapTool.TypeDefCleanFunction.INSTANCE);

		if(t1.equals(t2))
		{
			return true;
		}

		if(t1 instanceof CSharpRefTypeRef && !(t2 instanceof CSharpRefTypeRef) || t2 instanceof CSharpRefTypeRef && !(t1 instanceof CSharpRefTypeRef))
		{
			return false;
		}

		if(t1 instanceof CSharpArrayTypeRef && t2 instanceof CSharpArrayTypeRef)
		{
			return ((CSharpArrayTypeRef) t1).getDimensions() == ((CSharpArrayTypeRef) t2).getDimensions() && isTypeEqual(((CSharpArrayTypeRef) t1).getInnerTypeRef(),
					((CSharpArrayTypeRef) t2).getInnerTypeRef());
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

				if(!isTypeEqual(ObjectUtil.notNull(extractedRef1, DotNetTypeRef.ERROR_TYPE), ObjectUtil.notNull(extractedRef2, DotNetTypeRef.ERROR_TYPE)))
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
