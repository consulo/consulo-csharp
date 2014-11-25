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

import java.util.ArrayList;
import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
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
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.NullableFunction;

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

	public static final Key<DotNetQualifiedElement> ACCESSOR_VALUE_VARIABLE_OWNER = Key.create("accessor.value.variable");
	public static final Key<CSharpMethodDeclaration> EXTENSION_METHOD_WRAPPER = Key.create("extension.method.wrapper");
	public static final Key<CSharpMethodDeclaration> DELEGATE_METHOD_TYPE = Key.create("delegate.method.type");
	public static final Key<CSharpResolveSelector> SELECTOR = Key.create("resolve.selector");
	public static final Key<Boolean> WALK_DEEP = Key.create("walk.deep");


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
			LOGGER.error(new PsiInvalidElementAccessException(entrance));
		}

		DotNetNamespaceAsElement root = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace("", entrance.getResolveScope());

		assert root != null;

		if(!processor.execute(root, state))
		{
			return false;
		}

		// we cant go to use list
		if(PsiTreeUtil.getParentOfType(entrance, CSharpUsingList.class) != null)
		{
			return true;
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
							String name = genericParameter.getName();
							if(name == null)
							{
								continue;
							}
							if(((CSharpNamedResolveSelector) selector).isNameEqual(name))
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
			boolean walkDeep,
			@NotNull ResolveState state)
	{
		if(walkDeep)
		{
			state = state.put(WALK_DEEP, Boolean.TRUE);
		}

		ProgressIndicatorProvider.checkCanceled();
		GlobalSearchScope resolveScope = entrance.getResolveScope();
		if(entrance instanceof CSharpTypeDeclaration)
		{
			if(!processor.execute(entrance, state))
			{
				return false;
			}

			if(walkParent)
			{
				PsiElement parent = entrance.getParent();
				if(parent == null)
				{
					return true;
				}

				if(!walkChildren(processor, parent, walkParent, walkDeep, state))
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
			return walkChildren(processor, element, walkParent, walkDeep, newState);
		}
		else if(entrance instanceof DotNetGenericParameter)
		{
			if(!processor.execute(entrance, state))
			{
				return false;
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
				if(parentNamespace != null && !walkChildren(processor, parentNamespace, walkParent, walkDeep, state))
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
			if(namespace != null && !walkChildren(processor, namespace, walkParent, walkDeep, state))
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

	@NotNull
	public static <T extends PsiElement> List<T> mergeGroupsToIterable(@NotNull PsiElement[] elements)
	{
		List<T> list = new ArrayList<T>();
		for(PsiElement element : elements)
		{
			if(element instanceof CSharpElementGroup)
			{
				list.addAll(((CSharpElementGroup) element).getElements());
			}
		}
		return list;
	}

	@Nullable
	public static PsiElement findFirstValidElement(ResolveResult[] resolveResults)
	{
		ResolveResult firstValidResult = findFirstValidResult(resolveResults);
		return firstValidResult == null ? null : firstValidResult.getElement();
	}

	@Nullable
	public static ResolveResult findFirstValidResult(ResolveResult[] resolveResults)
	{
		if(resolveResults.length == 0)
		{
			return null;
		}
		for(ResolveResult resolveResult : resolveResults)
		{
			if(resolveResult.isValidResult())
			{
				return resolveResult;
			}
		}
		return null;
	}
}
