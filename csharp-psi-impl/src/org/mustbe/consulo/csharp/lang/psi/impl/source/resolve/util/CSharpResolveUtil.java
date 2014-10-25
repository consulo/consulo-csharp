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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util;

import gnu.trove.THashSet;

import java.util.Collections;
import java.util.Set;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.NullableFunction;
import com.intellij.util.SmartList;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
@Logger
public class CSharpResolveUtil
{
	public static final KeyWithDefaultValue<DotNetGenericExtractor> EXTRACTOR = new KeyWithDefaultValue<DotNetGenericExtractor>("dot-net-extractor")
	{
		@Override
		public DotNetGenericExtractor getDefaultValue()
		{
			return DotNetGenericExtractor.EMPTY;
		}
	};

	public static final Key<NullableFunction<CSharpResolveContext, PsiElement>> ELEMENT_SELECTOR = Key.create("element-selector");

	public static final Key<Boolean> ACCESSOR_VALUE_VARIABLE = Key.create("accessor.value.variable");
	public static final Key<CSharpMethodDeclaration> EXTENSION_METHOD_WRAPPER = Key.create("extension.method.wrapper");
	public static final Key<CSharpResolveSelector> SELECTOR = Key.create("resolve.selector");
	public static final Key<Boolean> NO_USING_LIST = new KeyWithDefaultValue<Boolean>("no.using.list")
	{
		@Override
		public Boolean getDefaultValue()
		{
			return Boolean.FALSE;
		}
	};

	public static boolean treeWalkUp(@NotNull PsiScopeProcessor processor,
			@NotNull PsiElement entrance,
			@NotNull PsiElement sender,
			@Nullable PsiElement maxScope)
	{
		return treeWalkUp(processor, entrance, sender, maxScope, ResolveState.initial());
	}

	public static boolean treeWalkUp(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			@NotNull final PsiElement sender,
			@Nullable PsiElement maxScope,
			@NotNull final ResolveState state)
	{
		if(!entrance.isValid())
		{
			CSharpResolveUtil.LOGGER.error(new PsiInvalidElementAccessException(entrance));
		}

		PsiElement prevParent = entrance;
		PsiElement scope = entrance;

		if(maxScope == null)
		{
			maxScope = sender.getContainingFile();
		}

		while(scope != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(entrance != sender && scope instanceof PsiFile)
			{
				break;
			}

			if(!scope.processDeclarations(processor, state, prevParent, entrance))
			{
				return false; // resolved
			}

			if(entrance != sender)
			{
				break;
			}

			if(scope == maxScope)
			{
				break;
			}

			prevParent = scope;
			scope = prevParent.getContext();
			if(scope != null && scope != prevParent.getParent() && !scope.isValid())
			{
				break;
			}
		}

		return true;
	}

	public static boolean walkUsing(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			@Nullable PsiElement maxScope,
			@NotNull final ResolveState state)
	{
		if(!entrance.isValid())
		{
			CSharpResolveUtil.LOGGER.error(new PsiInvalidElementAccessException(entrance));
		}

		PsiElement prevParent = entrance;
		PsiElement scope = entrance;

		if(maxScope == null)
		{
			maxScope = entrance.getContainingFile();
		}

		while(scope != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(scope instanceof CSharpUsingListOwner)
			{
				CSharpUsingList usingList = ((CSharpUsingListOwner) scope).getUsingList();
				if(usingList != null)
				{
					if(!processor.execute(usingList, state))
					{
						return false;
					}
				}

				if(scope instanceof CSharpFile)
				{
					DotNetNamespaceAsElement root = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace("",
							entrance.getResolveScope());

					assert root != null;

					if(!processor.execute(root, state))
					{
						return false;
					}
				}
			}

			if(scope == maxScope)
			{
				break;
			}

			prevParent = scope;
			scope = prevParent.getContext();
			if(scope != null && scope != prevParent.getParent() && !scope.isValid())
			{
				break;
			}
		}

		return true;
	}

	public static boolean walkGenericParameterList(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			@Nullable PsiElement maxScope,
			@NotNull final ResolveState state)
	{
		if(!ExecuteTargetUtil.canProcess(processor, ExecuteTarget.GENERIC_PARAMETER))
		{
			return true;
		}

		if(!entrance.isValid())
		{
			CSharpResolveUtil.LOGGER.error(new PsiInvalidElementAccessException(entrance));
		}

		PsiElement prevParent = entrance;
		PsiElement scope = entrance;

		if(maxScope == null)
		{
			maxScope = entrance.getContainingFile();
		}

		CSharpResolveSelector selector = state.get(SELECTOR);
		if(selector != null)
		{
			if(!(selector instanceof CSharpNamedResolveSelector))
			{
				return true;
			}
		}

		if(!(processor instanceof AbstractScopeProcessor))
		{
			return true;
		}

		while(scope != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(scope instanceof DotNetGenericParameterListOwner)
			{
				DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) scope).getGenericParameters();
				if(genericParameters.length > 0)
				{
					if(selector != null)
					{
						for(DotNetGenericParameter genericParameter : genericParameters)
						{
							if(((CSharpNamedResolveSelector) selector).isNameEqual(genericParameter.getName()))
							{
								((AbstractScopeProcessor) processor).addElement(genericParameter);
								return false;
							}
						}
					}
					else
					{
						for(DotNetGenericParameter genericParameter : genericParameters)
						{
							((AbstractScopeProcessor) processor).addElement(genericParameter);
						}
					}
				}
			}

			if(scope == maxScope)
			{
				break;
			}

			prevParent = scope;
			scope = prevParent.getContext();
			if(scope != null && scope != prevParent.getParent() && !scope.isValid())
			{
				break;
			}
		}

		return true;
	}

	public static boolean walkChildren(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			boolean walkParent,
			@Nullable PsiElement maxScope,
			@NotNull ResolveState state)
	{
		ProgressIndicatorProvider.checkCanceled();
		GlobalSearchScope resolveScope = entrance.getResolveScope();
		if(entrance instanceof CSharpTypeDeclaration)
		{
			val typeDeclaration = (CSharpTypeDeclaration) entrance;

			val superTypes = new SmartList<DotNetTypeRef>();

			if(!processor.execute(typeDeclaration, state))
			{
				return false;
			}

			if(typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				DotNetTypeDeclaration[] types = DotNetPsiSearcher.getInstance(typeDeclaration.getProject()).findTypes(typeDeclaration.getVmQName(),
						typeDeclaration.getResolveScope());

				for(DotNetTypeDeclaration type : types)
				{
					DotNetTypeList extendList = type.getExtendList();
					if(extendList != null)
					{
						DotNetTypeRef[] typeRefs = extendList.getTypeRefs();
						Collections.addAll(superTypes, typeRefs);
					}
				}

				if(superTypes.isEmpty())
				{
					Set<String> set = new THashSet<String>();
					for(DotNetTypeDeclaration type : types)
					{
						set.add(CSharpTypeDeclarationImplUtil.getDefaultSuperType(type));
					}

					if(set.contains(DotNetTypes.System.ValueType))
					{
						superTypes.add(new DotNetTypeRefByQName(DotNetTypes.System.ValueType, CSharpTransform.INSTANCE));
					}
					else
					{
						superTypes.add(new DotNetTypeRefByQName(DotNetTypes.System.Object, CSharpTransform.INSTANCE));
					}
				}
			}
			else
			{
				Collections.addAll(superTypes, typeDeclaration.getExtendTypeRefs());
			}

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(entrance);
				PsiElement resolve = typeResolveResult.getElement();

				if(resolve != null && !resolve.isEquivalentTo(entrance))
				{
					DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

					CSharpResolveSelector selector = state.get(SELECTOR);

					ResolveState newState = ResolveState.initial().put(SELECTOR, selector).put(EXTRACTOR, genericExtractor);

					if(!walkChildren(processor, resolve, false, maxScope, newState))
					{
						return false;
					}
				}
			}

			if(walkParent)
			{
				if(!walkChildren(processor, entrance.getParent(), walkParent, maxScope, state))
				{
					return false;
				}
			}
		}
		else if(entrance instanceof CSharpTypeDefStatement)
		{
			DotNetTypeRef dotNetTypeRef = ((CSharpTypeDefStatement) entrance).toTypeRef();

			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(entrance);

			PsiElement element = typeResolveResult.getElement();
			if(element == null)
			{
				return true;
			}

			CSharpResolveSelector selector = state.get(SELECTOR);
			ResolveState newState = ResolveState.initial().put(SELECTOR, selector).put(EXTRACTOR, typeResolveResult.getGenericExtractor());
			return walkChildren(processor, element, walkParent, maxScope, newState);
		}
		else if(entrance instanceof DotNetGenericParameter)
		{
			DotNetGenericParameterList parameterList = (DotNetGenericParameterList) entrance.getParent();

			PsiElement parent = parameterList.getParent();
			if(!(parent instanceof CSharpGenericConstraintOwner))
			{
				return true;
			}

			val constraint = CSharpGenericConstraintOwnerUtil.forParameter((CSharpGenericConstraintOwner) parent, (DotNetGenericParameter) entrance);
			if(constraint == null)
			{
				return true;
			}

			val superTypes = new SmartList<DotNetTypeRef>();
			for(CSharpGenericConstraintValue value : constraint.getGenericConstraintValues())
			{
				if(value instanceof CSharpGenericConstraintTypeValue)
				{
					DotNetTypeRef typeRef = ((CSharpGenericConstraintTypeValue) value).toTypeRef();
					superTypes.add(typeRef);
				}
				else if(value instanceof CSharpGenericConstraintKeywordValue)
				{
					if(((CSharpGenericConstraintKeywordValue) value).getKeywordElementType() == CSharpTokens.STRUCT_KEYWORD)
					{
						superTypes.add(new DotNetTypeRefByQName(DotNetTypes.System.ValueType, CSharpTransform.INSTANCE));
					}
					else if(((CSharpGenericConstraintKeywordValue) value).getKeywordElementType() == CSharpTokens.CLASS_KEYWORD)
					{
						superTypes.add(new DotNetTypeRefByQName(DotNetTypes.System.Object, CSharpTransform.INSTANCE));
					}
				}
			}

			CSharpResolveSelector selector = state.get(SELECTOR);

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(entrance);
				PsiElement resolve = typeResolveResult.getElement();

				if(resolve != null && resolve != entrance)
				{
					DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
					ResolveState newState = ResolveState.initial().put(SELECTOR, selector).put(EXTRACTOR, genericExtractor);

					if(!walkChildren(processor, resolve, walkParent, maxScope, newState))
					{
						return false;
					}
				}
			}
		}
		else if(entrance instanceof DotNetNamespaceAsElement)
		{
			state = state.put(BaseDotNetNamespaceAsElement.RESOLVE_SCOPE, resolveScope);
			state = state.put(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.NONE);

			if(!processor.execute(entrance, state))
			{
				return false;
			}

			String parentQName = ((DotNetNamespaceAsElement) entrance).getPresentableParentQName();
			// dont go to root namespace
			if(StringUtil.isEmpty(parentQName))
			{
				return true;
			}

			if(walkParent)
			{
				DotNetNamespaceAsElement parentNamespace = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace(parentQName,
						resolveScope);
				if(parentNamespace != null && !walkChildren(processor, parentNamespace, walkParent, maxScope, state))
				{
					return false;
				}
			}
		}
		else if(entrance instanceof DotNetNamespaceDeclaration)
		{
			String presentableQName = ((DotNetNamespaceDeclaration) entrance).getPresentableQName();
			if(presentableQName == null)
			{
				return true;
			}

			state = state.put(BaseDotNetNamespaceAsElement.RESOLVE_SCOPE, resolveScope);
			state = state.put(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.NONE);

			DotNetNamespaceAsElement namespace = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace(presentableQName, resolveScope);
			if(namespace != null && !walkChildren(processor, namespace, walkParent, maxScope, state))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	public static DotNetTypeRef resolveIterableType(@NotNull CSharpForeachStatementImpl foreachStatement)
	{
		DotNetExpression iterableExpression = foreachStatement.getIterableExpression();
		if(iterableExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		return resolveIterableType(iterableExpression, iterableExpression.toTypeRef(false));
	}

	@NotNull
	public static DotNetTypeRef resolveIterableType(@NotNull PsiElement scope, @NotNull DotNetTypeRef typeRef)
	{
		DotNetMethodDeclaration method = CSharpSearchUtil.findMethodByName("GetEnumerator", typeRef, scope);
		if(method == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetPropertyDeclaration current = CSharpSearchUtil.findPropertyByName("Current", method.getReturnTypeRef(), scope);
		if(current == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		return current.toTypeRef(false);
	}
}
