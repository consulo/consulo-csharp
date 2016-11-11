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

package consulo.csharp.lang.psi.impl.source.resolve.util;

import java.util.ArrayList;
import java.util.List;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCodeFragment;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingListOwner;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetArrayTypeRef;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;
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
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

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

	public static final Key<DotNetQualifiedElement> ACCESSOR_VALUE_VARIABLE_OWNER = Key.create("accessor.value.variable");
	public static final Key<CSharpMethodDeclaration> EXTENSION_METHOD_WRAPPER = Key.create("extension.method.wrapper");
	public static final Key<CSharpMethodDeclaration> DELEGATE_METHOD_TYPE = Key.create("delegate.method.type");
	public static final Key<CSharpResolveSelector> SELECTOR = Key.create("resolve.selector");
	public static final Key<Boolean> WALK_DEEP = Key.create("walk.deep");


	public static boolean treeWalkUp(@NotNull PsiScopeProcessor processor, @NotNull PsiElement entrance, @NotNull PsiElement sender, @Nullable PsiElement maxScope)
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

	@RequiredReadAction
	public static boolean walkUsing(@NotNull final PsiScopeProcessor processor, @NotNull final PsiElement entrance, @Nullable PsiElement maxScope, @NotNull final ResolveState state)
	{
		if(!entrance.isValid())
		{
			CSharpResolveUtil.LOGGER.error(new PsiInvalidElementAccessException(entrance));
		}

		DotNetNamespaceAsElement root = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace("", entrance.getResolveScope());

		// skip null - indexing
		if(root == null)
		{
			return true;
		}

		if(!processor.execute(root, state))
		{
			return false;
		}

		// we cant go to use list
		if(PsiTreeUtil.getParentOfType(entrance, CSharpUsingListChild.class) != null)
		{
			return true;
		}

		CSharpResolveSelector selector = state.get(SELECTOR);
		if(selector instanceof MemberByNameSelector)
		{
			((MemberByNameSelector) selector).putUserData(BaseDotNetNamespaceAsElement.FILTER, DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS);
		}

		PsiElement prevParent = entrance;
		PsiElement scope = entrance;

		maxScope = validateMaxScope(entrance, maxScope);

		while(scope != null)
		{
			ProgressIndicatorProvider.checkCanceled();

			if(scope instanceof CSharpUsingListOwner)
			{
				CSharpUsingListChild[] usingStatements = ((CSharpUsingListOwner) scope).getUsingStatements();
				for(CSharpUsingListChild usingStatement : usingStatements)
				{
					if(!processor.execute(usingStatement, state))
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

	private static PsiElement validateMaxScope(@NotNull PsiElement entrance, @Nullable PsiElement maxScope)
	{
		if(maxScope == null)
		{
			maxScope = entrance.getContainingFile();
			if(maxScope instanceof CSharpCodeFragment)
			{
				PsiElement scopeElement = ((CSharpCodeFragment) maxScope).getScopeElement();
				if(scopeElement != null)
				{
					maxScope = scopeElement.getContainingFile();
				}
			}
		}
		return maxScope;
	}

	public static boolean walkGenericParameterList(@NotNull final PsiScopeProcessor processor,
			@NotNull Processor<ResolveResult> consumer,
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

		maxScope = validateMaxScope(entrance, maxScope);

		CSharpResolveSelector selector = state.get(SELECTOR);
		if(selector != null)
		{
			if(!(selector instanceof CSharpNamedResolveSelector))
			{
				return true;
			}
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
								consumer.process(new CSharpResolveResult(genericParameter));
								return false;
							}
						}
					}
					else
					{
						for(DotNetGenericParameter genericParameter : genericParameters)
						{
							consumer.process(new CSharpResolveResult(genericParameter));
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

	@RequiredReadAction
	public static boolean walkChildren(@NotNull final PsiScopeProcessor processor, @NotNull final PsiElement entrance, boolean walkParent, boolean walkDeep, @NotNull ResolveState state)
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
				PsiElement parent = entrance.getContext();
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

			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();

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
				DotNetNamespaceAsElement parentNamespace = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace(parentQName, resolveScope);
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

	public static boolean walkForLabel(@NotNull final PsiScopeProcessor processor, @NotNull final PsiElement entrance, @NotNull ResolveState state)
	{
		PsiElement[] children = entrance.getChildren();
		for(PsiElement child : children)
		{
			if(ExecuteTargetUtil.isMyElement(processor, child))
			{
				if(!processor.execute(child, state))
				{
					return false;
				}
			}

			if(!walkForLabel(processor, child, state))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@RequiredReadAction
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
	@RequiredReadAction
	public static DotNetTypeRef resolveIterableType(@NotNull PsiElement scope, @NotNull DotNetTypeRef typeRef)
	{
		if(typeRef instanceof DotNetArrayTypeRef)
		{
			return ((DotNetArrayTypeRef) typeRef).getInnerTypeRef();
		}
		DotNetMethodDeclaration method = CSharpSearchUtil.findMethodByName("GetEnumerator", DotNetTypes2.System.Collections.Generic.IEnumerable$1, typeRef, 0);
		if(method != null)
		{
			DotNetPropertyDeclaration current = CSharpSearchUtil.findPropertyByName("Current", DotNetTypes2.System.Collections.Generic.IEnumerator$1, method.getReturnTypeRef());
			if(current == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			return current.toTypeRef(false);
		}

		if(DotNetInheritUtil.isParentOrSelf(DotNetTypes2.System.Collections.IEnumerable, typeRef, scope, true))
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Object);
		}
		else
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public static <T extends PsiElement> List<T> mergeGroupsToIterable(@NotNull PsiElement[] elements)
	{
		List<T> list = new ArrayList<T>();
		for(PsiElement element : elements)
		{
			if(element instanceof CSharpElementGroup)
			{
				list.addAll(((CSharpElementGroup) element).getElements());
			}
			else
			{
				list.add((T) element);
			}
		}
		return list;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public static <T extends PsiElement> List<T> mergeGroupsToIterable(@NotNull Iterable<PsiElement> elements)
	{
		List<T> list = new ArrayList<T>();
		for(PsiElement element : elements)
		{
			if(element instanceof CSharpElementGroup)
			{
				list.addAll(((CSharpElementGroup) element).getElements());
			}
			else
			{
				list.add((T) element);
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
			if(resolveResult.isValidResult() && isAssignable(resolveResult))
			{
				return resolveResult;
			}
		}
		return null;
	}

	@Nullable
	public static ResolveResult findValidOrFirstMaybeResult(ResolveResult[] resolveResults)
	{
		ResolveResult firstValidResult = findFirstValidResult(resolveResults);
		if(firstValidResult != null)
		{
			return firstValidResult;
		}
		return ArrayUtil2.safeGet(resolveResults, 0);
	}

	@NotNull
	public static ResolveResult[] filterValidResults(@NotNull ResolveResult[] resolveResults)
	{
		List<ResolveResult> filter = new SmartList<ResolveResult>();
		for(ResolveResult resolveResult : resolveResults)
		{
			if(resolveResult.isValidResult() && isAssignable(resolveResult))
			{
				filter.add(resolveResult);
			}
		}
		return ContainerUtil.toArray(filter, ResolveResult.EMPTY_ARRAY);
	}

	public static boolean isAssignable(ResolveResult resolveResult)
	{
		if(resolveResult instanceof CSharpResolveResult)
		{
			return ((CSharpResolveResult) resolveResult).isAssignable();
		}
		return true;
	}
}