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

package consulo.csharp.ide.parameterInfo;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.impl.DotNetPsiCountUtil;
import consulo.language.Language;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.parameterInfo.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12.11.14
 */
@ExtensionImpl
public class CSharpGenericParameterInfoHandler implements ParameterInfoHandler<PsiElement, DotNetGenericParameterListOwner>
{
	@Override
	public boolean couldShowInLookup()
	{
		return false;
	}

	@Nullable
	@Override
	public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context)
	{
		return ArrayUtil.EMPTY_OBJECT_ARRAY;
	}

	@Nullable
	@Override
	public PsiElement findElementForParameterInfo(CreateParameterInfoContext context)
	{
		final PsiElement at = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
		return PsiTreeUtil.getParentOfType(at, CSharpReferenceExpression.class);
	}

	@Nullable
	@Override
	public PsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context)
	{
		return context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
	}

	@Nullable
	private static DotNetGenericParameterListOwner findGenericParameterOwner(ParameterInfoContext context)
	{
		final PsiElement at = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
		if(at == null)
		{
			return null;
		}

		DotNetTypeList typeList = PsiTreeUtil.getParentOfType(at, DotNetTypeList.class);
		if(typeList == null)
		{
			return null;
		}

		PsiElement parent = typeList.getParent();
		if(!(parent instanceof CSharpReferenceExpression))
		{
			return null;
		}

		int argumentsSize = DotNetPsiCountUtil.countChildrenOfType(typeList.getNode(), CSharpTokens.COMMA) + 1;

		CSharpReferenceExpression referenceExpression = (CSharpReferenceExpression) parent;
		ResolveResult[] resolveResults = referenceExpression.multiResolve(true);
		for(ResolveResult resolveResult : resolveResults)
		{
			PsiElement element = resolveResult.getElement();
			if(element instanceof DotNetGenericParameterListOwner)
			{
				int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
				if(genericParametersCount == argumentsSize)
				{
					return (DotNetGenericParameterListOwner) element;
				}
			}
		}

		return null;
	}

	@Override
	public void showParameterInfo(@Nonnull PsiElement place, CreateParameterInfoContext context)
	{
		DotNetGenericParameterListOwner genericParameterOwner = findGenericParameterOwner(context);
		if(genericParameterOwner == null)
		{
			return;
		}
		context.setItemsToShow(new Object[]{genericParameterOwner});
		context.showHint(place, place.getTextRange().getStartOffset(), this);
	}

	@Override
	public void updateParameterInfo(@Nonnull PsiElement place, UpdateParameterInfoContext context)
	{
		int parameterIndex = -1;
		DotNetTypeList typeList = PsiTreeUtil.getParentOfType(place, DotNetTypeList.class, false);

		if(typeList == null)
		{
			context.removeHint();
			return;
		}
		if(!(typeList.getParent() instanceof CSharpReferenceExpression))
		{
			context.removeHint();
			return;
		}

		parameterIndex = ParameterInfoUtils.getCurrentParameterIndex(typeList.getNode(), context.getOffset(), CSharpTokens.COMMA);

		context.setCurrentParameter(parameterIndex);

		if(context.getParameterOwner() == null)
		{
			context.setParameterOwner(place);
		}
		else if(context.getParameterOwner() != PsiTreeUtil.getParentOfType(place, CSharpReferenceExpression.class, false))
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
		return ",>";
	}

	@Override
	public boolean tracksParameterIndex()
	{
		return true;
	}

	@Override
	public void updateUI(DotNetGenericParameterListOwner p, ParameterInfoUIContext context)
	{
		if(p == null)
		{
			context.setUIComponentEnabled(false);
			return;
		}
		CSharpGenericParametersInfo build = CSharpGenericParametersInfo.build(p);
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
