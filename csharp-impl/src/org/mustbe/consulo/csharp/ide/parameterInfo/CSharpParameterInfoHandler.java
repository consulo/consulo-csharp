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

package org.mustbe.consulo.csharp.ide.parameterInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpParameterInfoHandler implements ParameterInfoHandler<PsiElement, Object>
{
	@Override
	public boolean couldShowInLookup()
	{
		return true;
	}

	@Nullable
	@Override
	public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context)
	{
		return new Object[0];
	}

	@Nullable
	@Override
	public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context)
	{
		return new Object[0];
	}

	@Nullable
	@Override
	public PsiElement findElementForParameterInfo(CreateParameterInfoContext context)
	{
		final PsiElement at = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
		return PsiTreeUtil.getParentOfType(at, CSharpCallArgumentListOwner.class);
	}

	@Override
	public void showParameterInfo(@NotNull PsiElement element, CreateParameterInfoContext context)
	{
		Pair<Object, Integer> callableInfo = resolveToCallableInfo(element);

		if(callableInfo != null)
		{
			context.setItemsToShow(new Object[]{callableInfo.getFirst()});
			context.showHint(element, element.getTextRange().getStartOffset(), this);
		}
	}

	private static Pair<Object, Integer> resolveToCallableInfo(PsiElement element)
	{
		Object callable = null;
		int count = 0;
		if(element instanceof CSharpCallArgumentListOwner)
		{
			ResolveResult[] resolveResults = ((CSharpCallArgumentListOwner) element).multiResolve(false);
			if(resolveResults.length > 0)
			{
				callable = resolveResults[0].getElement();
				if(callable instanceof DotNetLikeMethodDeclaration)
				{
					count = ((DotNetLikeMethodDeclaration) callable).getParameters().length;
				}
				else if(callable instanceof DotNetVariable)
				{
					DotNetTypeRef typeRef = ((DotNetVariable) callable).toTypeRef(false);
					if(typeRef instanceof CSharpLambdaTypeRef)
					{
						PsiElement resolve = ((CSharpLambdaTypeRef) typeRef).getTarget();
						if(resolve instanceof DotNetLikeMethodDeclaration)
						{
							callable = resolve;
						}
						else
						{
							callable = typeRef;
						}
						count = ((CSharpLambdaTypeRef) typeRef).getParameterTypes().length;
					}
				}
			}
		}

		if(callable == null)
		{
			return null;
		}
		return Pair.create(callable, count);
	}

	@Nullable
	@Override
	public PsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context)
	{
		return context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
	}

	@Override
	public void updateParameterInfo(@NotNull PsiElement place, UpdateParameterInfoContext context)
	{
		int parameterIndex = getArgumentIndex(place);
		context.setCurrentParameter(parameterIndex);

		if(context.getParameterOwner() == null)
		{
			context.setParameterOwner(place);
		}
		else if(context.getParameterOwner() != PsiTreeUtil.getParentOfType(place, CSharpCallArgumentListOwner.class))
		{
			context.removeHint();
			return;
		}
		final Object[] objects = context.getObjectsToView();

		for(int i = 0; i < objects.length; i++)
		{
			context.setUIComponentEnabled(i, true);
		}
	}

	@Nullable
	@Override
	public String getParameterCloseChars()
	{
		return ",)";
	}

	@Override
	public boolean tracksParameterIndex()
	{
		return true;
	}

	@Override
	public void updateUI(Object p, ParameterInfoUIContext context)
	{
		if(p == null)
		{
			context.setUIComponentEnabled(false);
			return;
		}
		CSharpParametersInfo build = CSharpParametersInfo.build(p);
		if(build == null)
		{
			context.setUIComponentEnabled(false);
			return;
		}

		String text = build.getText();

		TextRange parameterRange = build.getParameterRange(context.getCurrentParameterIndex());

		context.setupUIComponentPresentation(text, parameterRange.getStartOffset(), parameterRange.getEndOffset(), !context.isUIComponentEnabled(),
				false, false, context.getDefaultParameterColor());
	}

	// from google-dart plugin
	// author Fedor.Korotkov license apache 2
	public static int getArgumentIndex(PsiElement place)
	{
		int parameterIndex = -1;
		final CSharpCallArgumentList argumentList = PsiTreeUtil.getParentOfType(place, CSharpCallArgumentList.class, false);
		if(place == argumentList)
		{
			assert argumentList != null;
			final Pair<Object, Integer> info = resolveToCallableInfo(argumentList.getParent());
			// the last one
			parameterIndex = info == null ? -1 : info.getSecond() - 1;
		}
		else if(argumentList != null)
		{
			for(DotNetExpression expression : argumentList.getExpressions())
			{
				++parameterIndex;
				if(expression.getTextRange().getEndOffset() >= place.getTextOffset())
				{
					break;
				}
			}
		}
		else if(UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(place, true) instanceof CSharpCallArgumentList)
		{
			// seems foo(param1, param2<caret>)
			final CSharpCallArgumentList prevSibling = (CSharpCallArgumentList) UsefulPsiTreeUtil
					.getPrevSiblingSkipWhiteSpacesAndComments(place, true);
			assert prevSibling != null;
			// callExpression -> arguments -> argumentList
			parameterIndex = prevSibling.getExpressions().length/* + prevSibling.getNamedArgumentList().size()*/;
		}
		return parameterIndex;
	}
}
