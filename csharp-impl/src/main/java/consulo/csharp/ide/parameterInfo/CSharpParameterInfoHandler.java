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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.ParameterInfoUtils;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ArrayFactory;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.ui.RequiredUIAccess;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpDictionaryInitializerImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpParameterInfoHandler implements ParameterInfoHandler<PsiElement, CSharpParameterInfoHandler.ItemToShow>
{
	@Nonnull
	public static Object item(@Nonnull DotNetLikeMethodDeclaration e)
	{
		return new ItemToShow((CSharpSimpleLikeMethod) e, e);
	}

	public static class ItemToShow
	{
		public static final ItemToShow[] EMPTY_ARRAY = new ItemToShow[0];

		public static ArrayFactory<ItemToShow> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new ItemToShow[count];

		private CSharpSimpleLikeMethod myLikeMethod;
		private PsiElement myScope;
		private boolean myValid;

		public ItemToShow(@Nonnull CSharpSimpleLikeMethod likeMethod, @Nonnull PsiElement scope)
		{
			myLikeMethod = likeMethod;
			myScope = scope;
		}

		public boolean isValid()
		{
			return myValid;
		}

		public ItemToShow valid()
		{
			myValid = true;
			return this;
		}

		@RequiredReadAction
		public boolean isObsolete()
		{
			return myLikeMethod instanceof DotNetModifierListOwner && DotNetAttributeUtil.hasAttribute((PsiElement) myLikeMethod, DotNetTypes.System.ObsoleteAttribute);
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof ItemToShow)
			{
				CSharpSimpleLikeMethod likeMethod = ((ItemToShow) obj).myLikeMethod;
				if(likeMethod == myLikeMethod)
				{
					return true;
				}
				if(likeMethod instanceof PsiElement && myLikeMethod instanceof PsiElement)
				{
					return ((PsiElement) likeMethod).getOriginalElement() == ((PsiElement) myLikeMethod).getOriginalElement();
				}
			}
			return false;
		}
	}

	@Override
	public boolean couldShowInLookup()
	{
		return true;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public ItemToShow[] getParametersForLookup(LookupElement item, ParameterInfoContext context)
	{
		Object object = item.getObject();

		if(object instanceof DotNetLikeMethodDeclaration)
		{
			return new ItemToShow[]{new ItemToShow((CSharpSimpleLikeMethod) object, context.getFile())};
		}
		if(object instanceof DotNetVariable)
		{
			DotNetVariable variable = (DotNetVariable) object;
			DotNetTypeRef dotNetTypeRef = variable.toTypeRef(tracksParameterIndex());

			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return new ItemToShow[]{new ItemToShow((CSharpSimpleLikeMethod) typeResolveResult, variable)};
			}
		}
		return ItemToShow.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public Object[] getParametersForDocumentation(ItemToShow p, ParameterInfoContext context)
	{
		return new Object[0];
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement findElementForParameterInfo(CreateParameterInfoContext context)
	{
		final PsiElement at = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
		return resolveCallArgumentListOwner(at);
	}

	@Override
	@RequiredReadAction
	public void showParameterInfo(@Nonnull PsiElement element, CreateParameterInfoContext context)
	{
		ItemToShow[] itemsToShow = resolveToCallables(element, context);

		if(itemsToShow.length > 0)
		{
			context.setItemsToShow(itemsToShow);
			context.showHint(element, element.getTextRange().getStartOffset(), this);
		}
	}

	@Nonnull
	@RequiredReadAction
	private static ItemToShow[] resolveToCallables(PsiElement element, CreateParameterInfoContext context)
	{
		List<ItemToShow> list = new SmartList<>();
		if(element instanceof CSharpCallArgumentListOwner)
		{
			ResolveResult[] resolveResults = ((CSharpCallArgumentListOwner) element).multiResolve(false);

			for(ResolveResult resolveResult : resolveResults)
			{
				CSharpSimpleLikeMethod likeMethod = resolveSimpleMethod(resolveResult, element);
				if(likeMethod != null)
				{
					ItemToShow item = new ItemToShow(likeMethod, element);
					list.add(item);
					if(resolveResult.isValidResult() && context.getHighlightedElement() == null)
					{
						context.setHighlightedElement(item.valid());
					}
				}
			}
		}

		ContainerUtil.sort(list, (o1, o2) ->
		{
			if(o1.isValid())
			{
				return -1;
			}
			if(o2.isValid())
			{
				return 1;
			}
			return o1.myLikeMethod.getParameterInfos().length - o2.myLikeMethod.getParameterInfos().length;
		});
		return ContainerUtil.toArray(list, ItemToShow.ARRAY_FACTORY);
	}

	@Nullable
	@RequiredReadAction
	private static CSharpSimpleLikeMethod resolveSimpleMethod(ResolveResult resolveResult, PsiElement scope)
	{
		CSharpSimpleLikeMethod method = null;

		PsiElement resolveResultElement = resolveResult.getElement();
		if(resolveResultElement instanceof DotNetVariable)
		{
			DotNetTypeRef typeRef = ((DotNetVariable) resolveResultElement).toTypeRef(false);

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				CSharpMethodDeclaration resolve = ((CSharpLambdaResolveResult) typeResolveResult).getTarget();
				if(resolve != null)
				{
					method = GenericUnwrapTool.extract(resolve, typeResolveResult.getGenericExtractor());
				}
				else
				{
					method = (CSharpSimpleLikeMethod) typeResolveResult;
				}
			}
		}
		else if(resolveResultElement instanceof CSharpSimpleLikeMethod)
		{
			method = (CSharpSimpleLikeMethod) resolveResultElement;
		}
		return method;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context)
	{
		return context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
	}

	@Override
	public void updateParameterInfo(@Nonnull PsiElement place, UpdateParameterInfoContext context)
	{
		CSharpCallArgumentListOwner owner = resolveCallArgumentListOwner(place);
		int parameterIndex = -1;
		CSharpCallArgumentList callArgumentList = owner == null ? null : owner.getParameterList();
		if(callArgumentList != null)
		{
			IElementType delimiter = CSharpTokens.COMMA;
			if(callArgumentList instanceof CSharpDictionaryInitializerImpl)
			{
				delimiter = CSharpTokens.EQ;
			}
			parameterIndex = ParameterInfoUtils.getCurrentParameterIndex(callArgumentList.getNode(), context.getOffset(), delimiter);
		}

		context.setCurrentParameter(parameterIndex);

		if(context.getParameterOwner() == null)
		{
			context.setParameterOwner(place);
		}
		else if(context.getParameterOwner() != owner)
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
	private static CSharpCallArgumentListOwner resolveCallArgumentListOwner(@Nullable PsiElement place)
	{
		if(place == null)
		{
			return null;
		}

		boolean isDelegateKeyword = PsiUtilCore.getElementType(place) == CSharpTokens.DELEGATE_KEYWORD;

		DotNetElement element;
		// if we targeted delegate keyword = do not try to search delegate expression
		if(isDelegateKeyword)
		{
			element = PsiTreeUtil.getParentOfType(place, CSharpCallArgumentListOwner.class, CSharpLambdaExpressionImpl.class);
		}
		else
		{
			element = PsiTreeUtil.getParentOfType(place, CSharpCallArgumentListOwner.class, CSharpLambdaExpressionImpl.class, CSharpDelegateExpressionImpl.class);
		}

		if(element instanceof CSharpCallArgumentListOwner)
		{
			return (CSharpCallArgumentListOwner) element;
		}

		return null;
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
	@RequiredUIAccess
	public void updateUI(ItemToShow p, ParameterInfoUIContext context)
	{
		if(p == null)
		{
			context.setUIComponentEnabled(false);
			return;
		}
		CSharpParametersInfo build = CSharpParametersInfo.build(p.myLikeMethod, p.myScope);

		String text = build.getText();

		TextRange parameterRange = build.getParameterRange(context.getCurrentParameterIndex());

		context.setupUIComponentPresentation(text, parameterRange.getStartOffset(), parameterRange.getEndOffset(), !context.isUIComponentEnabled(), p.isObsolete(), false, context
				.getDefaultParameterColor());
	}
}
