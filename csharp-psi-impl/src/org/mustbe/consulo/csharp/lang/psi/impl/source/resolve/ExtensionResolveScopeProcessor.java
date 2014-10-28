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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameterList;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import lombok.val;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class ExtensionResolveScopeProcessor extends AbstractScopeProcessor
{
	private final CSharpReferenceExpression myExpression;
	private final boolean myCompletion;
	private final DotNetTypeRef myQualifierTypeRef;

	private final List<CSharpMethodDeclaration> myResolvedElements = new SmartList<CSharpMethodDeclaration>();

	public ExtensionResolveScopeProcessor(@NotNull DotNetTypeRef qualifierTypeRef, @NotNull CSharpReferenceExpression expression, boolean completion)
	{
		myQualifierTypeRef = qualifierTypeRef;
		myExpression = expression;
		myCompletion = completion;
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		if(myCompletion)
		{
			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
			assert extractor != null;

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myExpression.getResolveScope(), element);

			for(CSharpElementGroup<CSharpMethodDeclaration> elementGroup : context.getExtensionMethodGroups())
			{
				Collection<CSharpMethodDeclaration> elements = elementGroup.getElements();
				for(CSharpMethodDeclaration psiElement : elements)
				{
					DotNetTypeRef firstParameterTypeRef = getFirstTypeRefOrParameter(psiElement);

					if(!CSharpTypeUtil.isInheritableWithImplicit(firstParameterTypeRef, myQualifierTypeRef, myExpression))
					{
						continue;
					}

					addElement(transform(psiElement));
				}
			}
		}
		else
		{
			CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
			if(selector == null)
			{
				return true;
			}

			DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
			assert extractor != null;

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myExpression.getResolveScope(), element);

			PsiElement[] psiElements = selector.doSelectElement(context);

			for(PsiElement e : psiElements)
			{
				CSharpElementGroup<?> elementGroup = (CSharpElementGroup<?>) e;

				for(PsiElement psiElement : elementGroup.getElements())
				{
					CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) psiElement;

					DotNetTypeRef firstParameterTypeRef = getFirstTypeRefOrParameter(methodDeclaration);

					if(!CSharpTypeUtil.isInheritableWithImplicit(firstParameterTypeRef, myQualifierTypeRef, myExpression))
					{
						continue;
					}

					myResolvedElements.add(transform(methodDeclaration));
				}
			}
		}

		return true;
	}

	@NotNull
	@Override
	public ResolveResult[] toResolveResults()
	{
		ResolveResult[] resolveResults = super.toResolveResults();
		if(myResolvedElements.isEmpty())
		{
			return resolveResults;
		}

		val element = new CSharpElementGroupImpl(myExpression.getProject(), myResolvedElements.get(0).getName(), myResolvedElements);
		return ArrayUtil.mergeArrays(resolveResults, new ResolveResult[]{new PsiElementResolveResult(element)});
	}

	@NotNull
	private static DotNetTypeRef getFirstTypeRefOrParameter(DotNetParameterListOwner owner)
	{
		DotNetParameter[] parameters = owner.getParameters();
		assert parameters.length != 0;
		return parameters[0].toTypeRef(false);
	}

	/*private static DotNetExpression[] getNewExpressions(CSharpReferenceExpression ex, DotNetTypeRef qType)
	{
		PsiElement parent = ex.getParent();
		if(parent instanceof CSharpCallArgumentListOwner)
		{
			DotNetExpression[] parameterExpressions = ((CSharpCallArgumentListOwner) parent).getParameterExpressions();

			DotNetExpression[] newParameters = new DotNetExpression[parameterExpressions.length + 1];
			System.arraycopy(parameterExpressions, 0, newParameters, 1, parameterExpressions.length);

			newParameters[0] = new DummyExpression(ex.getProject(), qType);

			return newParameters;
		}
		else
		{
			return DotNetExpression.EMPTY_ARRAY;
		}
	}    */

	private static CSharpLightMethodDeclaration transform(final CSharpMethodDeclaration methodDeclaration)
	{
		DotNetParameterList parameterList = methodDeclaration.getParameterList();
		assert parameterList != null;
		DotNetParameter[] oldParameters = methodDeclaration.getParameters();

		DotNetParameter[] parameters = new DotNetParameter[oldParameters.length - 1];
		System.arraycopy(oldParameters, 1, parameters, 0, parameters.length);

		CSharpLightParameterList lightParameterList = new CSharpLightParameterList(parameterList, parameters);
		CSharpLightMethodDeclaration declaration = new CSharpLightMethodDeclaration(methodDeclaration, lightParameterList)
		{
			@Override
			public boolean canNavigate()
			{
				return true;
			}

			@Override
			public void navigate(boolean requestFocus)
			{
				((Navigatable)methodDeclaration).navigate(requestFocus);
			}
		};

		declaration.putUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER, methodDeclaration);
		return declaration;
	}
}
