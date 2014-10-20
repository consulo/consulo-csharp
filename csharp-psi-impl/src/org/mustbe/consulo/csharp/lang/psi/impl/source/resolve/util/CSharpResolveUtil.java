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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.consulo.ide.eap.EarlyAccessProgramDescriptor;
import org.consulo.ide.eap.EarlyAccessProgramManager;
import org.consulo.lombok.annotations.LazyInstance;
import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintOwnerUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.debugger.DotNetVirtualMachineUtil;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
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
	public static final KeyWithDefaultValue<DotNetGenericExtractor> EXTRACTOR = new KeyWithDefaultValue<DotNetGenericExtractor>
			("dot-net-extractor")
	{
		@Override
		public DotNetGenericExtractor getDefaultValue()
		{
			return DotNetGenericExtractor.EMPTY;
		}
	};

	public static final Key<NullableFunction<CSharpResolveContext, PsiElement>> ELEMENT_SELECTOR = Key.create("element-selector");

	public static class CSharpResolvingEapDescriptor extends EarlyAccessProgramDescriptor
	{
		@NotNull
		@Override
		public String getName()
		{
			return "C# Resolving";
		}

		@Override
		public boolean isRestartRequired()
		{
			return true;
		}

		@Override
		public boolean getDefaultState()
		{
			return false;
		}

		@NotNull
		@Override
		public String getDescription()
		{
			return "Currently C# Resolving is in progress and be slow(and bugged).";
		}
	}

	public static class CSharpReferenceCompletionEapDescriptor extends EarlyAccessProgramDescriptor
	{
		@NotNull
		@Override
		public String getName()
		{
			return "C# Reference Completion";
		}

		@Override
		public boolean isRestartRequired()
		{
			return true;
		}

		@Override
		public boolean getDefaultState()
		{
			return false;
		}

		@NotNull
		@Override
		public String getDescription()
		{
			return "Currently C# Reference Completion is in progress and be slow(and bugged).";
		}
	}

	public static final Key<Boolean> ACCESSOR_VALUE_VARIABLE = Key.create("accessor.value.variable");
	public static final Key<Boolean> EXTENSION_METHOD_WRAPPER = Key.create("extension.method.wrapper");
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

	public static boolean walkChildren(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			boolean gotoParent,
			@Nullable PsiElement maxScope,
			@NotNull ResolveState state)
	{
		return walkChildrenImpl(processor, entrance, gotoParent, maxScope, state, new HashSet<String>());
	}

	private static boolean walkChildrenImpl(@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			boolean walkParent,
			@Nullable PsiElement maxScope,
			@NotNull ResolveState state,
			@NotNull Set<String> typeVisited)
	{
		ProgressIndicatorProvider.checkCanceled();
		GlobalSearchScope resolveScope = entrance.getResolveScope();
		if(entrance instanceof CSharpTypeDeclaration)
		{
			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);

			val typeDeclaration = (CSharpTypeDeclaration) entrance;

			val superTypes = new SmartList<DotNetTypeRef>();

			if(!processTypeDeclaration(processor, typeDeclaration, state, resolveScope, superTypes, extractor, typeVisited))
			{
				return false;
			}

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(entrance);
				PsiElement resolve = typeResolveResult.getElement();

				if(resolve != null && !resolve.isEquivalentTo(entrance))
				{
					DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
					ResolveState newState = ResolveState.initial().put(EXTRACTOR, genericExtractor);

					if(!walkChildrenImpl(processor, resolve, false, maxScope, newState, typeVisited))
					{
						return false;
					}
				}
			}

			if(walkParent)
			{
				if(!walkChildrenImpl(processor, entrance.getParent(), walkParent, maxScope, state, typeVisited))
				{
					return false;
				}
			}
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

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(entrance);
				PsiElement resolve = typeResolveResult.getElement();

				if(resolve != null && resolve != entrance)
				{
					DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
					ResolveState newState = ResolveState.initial().put(EXTRACTOR, genericExtractor);

					if(!walkChildrenImpl(processor, resolve, false, maxScope, newState, typeVisited))
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
				if(parentNamespace != null && !walkChildrenImpl(processor, parentNamespace, walkParent, maxScope, state, typeVisited))
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

			DotNetNamespaceAsElement namespace = DotNetPsiSearcher.getInstance(entrance.getProject()).findNamespace(presentableQName,
					resolveScope);
			if(namespace != null && !walkChildrenImpl(processor, namespace, walkParent, maxScope, state, typeVisited))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean processTypeDeclaration(@NotNull final PsiScopeProcessor processor,
			@NotNull CSharpTypeDeclaration typeDeclaration,
			@NotNull ResolveState state,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull List<DotNetTypeRef> supers,
			@NotNull DotNetGenericExtractor genericExtractor,
			@Nullable Set<String> typeVisited)
	{
		if(typeVisited != null)
		{
			String vmName = DotNetVirtualMachineUtil.toVMQualifiedName(typeDeclaration);
			if(typeVisited.contains(vmName))
			{
				return true;
			}
			else
			{
				typeVisited.add(vmName);
			}
		}


		if(!processor.execute(typeDeclaration, state))
		{
			return false;
		}
		Collections.addAll(supers, typeDeclaration.getExtendTypeRefs());
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

	@LazyInstance
	@NotNull
	public static Boolean isResolvingEnabled()
	{
		return EarlyAccessProgramManager.is(CSharpResolvingEapDescriptor.class);
	}

	@LazyInstance
	@NotNull
	public static Boolean isReferenceCompletionEnabled()
	{
		return EarlyAccessProgramManager.is(CSharpReferenceCompletionEapDescriptor.class);
	}
}
