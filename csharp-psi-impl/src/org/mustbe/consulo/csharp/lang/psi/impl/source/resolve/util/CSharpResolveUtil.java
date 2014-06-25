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

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintOwnerUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceHelper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromQualifiedName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.debugger.DotNetVirtualMachineUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.SmartList;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
@Logger
public class CSharpResolveUtil
{
	public static final KeyWithDefaultValue<DotNetGenericExtractor> EXTRACTOR_KEY = new KeyWithDefaultValue<DotNetGenericExtractor>
			("dot-net-extractor")
	{
		@Override
		public DotNetGenericExtractor getDefaultValue()
		{
			return DotNetGenericExtractor.EMPTY;
		}
	};

	public static final Key<PsiFile> CONTAINS_FILE_KEY = Key.create("contains.file");
	public static final Key<Condition<PsiElement>> CONDITION_KEY = Key.create("condition");

	public static boolean treeWalkUp(
			@NotNull PsiScopeProcessor processor, @NotNull PsiElement entrance, @NotNull PsiElement sender, @Nullable PsiElement maxScope)
	{
		return treeWalkUp(processor, entrance, sender, maxScope, ResolveState.initial());
	}

	public static boolean treeWalkUp(
			@NotNull final PsiScopeProcessor processor,
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

	public static boolean walkChildren(
			@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			boolean gotoParent,
			@Nullable PsiElement maxScope,
			@NotNull ResolveState state)
	{
		return walkChildrenImpl(processor, entrance, gotoParent, maxScope, state, new HashSet<String>());
	}

	private static boolean walkChildrenImpl(
			@NotNull final PsiScopeProcessor processor,
			@NotNull final PsiElement entrance,
			boolean gotoParent,
			@Nullable PsiElement maxScope,
			@NotNull ResolveState state,
			@NotNull Set<String> typeVisited)
	{
		ProgressIndicatorProvider.checkCanceled();
		if(entrance instanceof DotNetTypeDeclaration)
		{
			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR_KEY);

			val typeDeclaration = (DotNetTypeDeclaration) entrance;

			val superTypes = new SmartList<DotNetTypeRef>();

			if(typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				val types = DotNetPsiFacade.getInstance(entrance.getProject()).findTypes(typeDeclaration.getPresentableQName(),
						entrance.getResolveScope(), typeDeclaration.getGenericParametersCount());

				for(val type : types)
				{
					if(!type.hasModifier(CSharpModifier.PARTIAL))
					{
						continue;
					}

					if(!processTypeDeclaration(processor, type, state, superTypes, extractor, typeVisited, gotoParent))
					{
						return false;
					}
				}
			}
			else
			{
				if(!processTypeDeclaration(processor, typeDeclaration, state, superTypes, extractor, typeVisited, gotoParent))
				{
					return false;
				}
			}

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				PsiElement resolve = dotNetTypeRef.resolve(entrance);

				if(resolve != null && !resolve.isEquivalentTo(entrance))
				{
					DotNetGenericExtractor genericExtractor = dotNetTypeRef.getGenericExtractor(resolve, entrance);
					ResolveState newState = ResolveState.initial().put(EXTRACTOR_KEY, genericExtractor);

					if(!walkChildrenImpl(processor, resolve, false, maxScope, newState, typeVisited))
					{
						return false;
					}
				}
			}

			if(gotoParent)
			{
				if(!walkChildrenImpl(processor, entrance.getParent(), gotoParent, maxScope, state, typeVisited))
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
						superTypes.add(new CSharpTypeRefFromQualifiedName(DotNetTypes.System_ValueType, 0));
					}
					else if(((CSharpGenericConstraintKeywordValue) value).getKeywordElementType() == CSharpTokens.CLASS_KEYWORD)
					{
						superTypes.add(new CSharpTypeRefFromQualifiedName(DotNetTypes.System_Object, 0));
					}
				}
			}

			for(DotNetTypeRef dotNetTypeRef : superTypes)
			{
				PsiElement resolve = dotNetTypeRef.resolve(entrance);

				if(resolve != null && resolve != entrance)
				{
					DotNetGenericExtractor genericExtractor = dotNetTypeRef.getGenericExtractor(resolve, entrance);
					ResolveState newState = ResolveState.initial().put(EXTRACTOR_KEY, genericExtractor);

					if(!walkChildrenImpl(processor, resolve, false, maxScope, newState, typeVisited))
					{
						return false;
					}
				}
			}
		}
		else if(entrance instanceof CSharpNamespaceAsElement)
		{
			if(!entrance.processDeclarations(processor, state, maxScope, entrance))
			{
				return false;
			}

			String pQName = CSharpNamespaceHelper.getNamespaceForIndexing(((CSharpNamespaceAsElement) entrance).getPresentableParentQName());
			if(Comparing.equal(pQName, CSharpNamespaceHelper.ROOT))
			{
				return true;
			}

			CSharpNamespaceAsElement parentNamespace = new CSharpNamespaceAsElement(entrance.getProject(), pQName, entrance.getResolveScope());
			if(!walkChildrenImpl(processor, parentNamespace, gotoParent, maxScope, state, typeVisited))
			{
				return false;
			}
		}
		else if(entrance instanceof DotNetNamespaceDeclaration)
		{
			String presentableQName = ((DotNetNamespaceDeclaration) entrance).getPresentableQName();
			if(presentableQName == null)
			{
				return true;
			}
			CSharpNamespaceAsElement parentNamespace = new CSharpNamespaceAsElement(entrance.getProject(), presentableQName,
					entrance.getResolveScope());
			if(!walkChildrenImpl(processor, parentNamespace, gotoParent, maxScope, state, typeVisited))
			{
				return false;
			}
		}
		else if(entrance instanceof PsiFile)
		{
			CSharpNamespaceAsElement parentNamespace = new CSharpNamespaceAsElement(entrance.getProject(), CSharpNamespaceHelper.ROOT,
					entrance.getResolveScope());
			return walkChildrenImpl(processor, parentNamespace, gotoParent, maxScope, state, typeVisited);
		}

		PsiFile psiFile = state.get(CONTAINS_FILE_KEY);
		return psiFile == null || walkChildrenImpl(processor, psiFile, gotoParent, maxScope, state, typeVisited);
	}

	private static boolean processTypeDeclaration(
			@NotNull final PsiScopeProcessor processor,
			DotNetTypeDeclaration typeDeclaration,
			ResolveState state,
			List<DotNetTypeRef> supers,
			DotNetGenericExtractor genericExtractor,
			Set<String> typeVisited,
			boolean typeResolving)
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

		for(DotNetNamedElement namedElement : typeDeclaration.getMembers())
		{
			if(!checkConditionKey(processor, namedElement))
			{
				continue;
			}

			DotNetNamedElement extracted = GenericUnwrapTool.extract(namedElement, genericExtractor);

			if(!processor.execute(extracted, state))
			{
				return false;
			}
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

	public static boolean checkConditionKey(@NotNull PsiScopeProcessor processor, @NotNull PsiElement element)
	{
		Condition<PsiElement> hint = processor.getHint(CONDITION_KEY);
		return hint == null || hint.value(element);
	}
}
