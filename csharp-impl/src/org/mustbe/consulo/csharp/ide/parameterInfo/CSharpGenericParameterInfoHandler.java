package org.mustbe.consulo.csharp.ide.parameterInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.lang.psi.impl.DotNetPsiCountUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 12.11.14
 */
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
	public Object[] getParametersForDocumentation(DotNetGenericParameterListOwner p, ParameterInfoContext context)
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
	public void showParameterInfo(@NotNull PsiElement place, CreateParameterInfoContext context)
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
	public void updateParameterInfo(@NotNull PsiElement place, UpdateParameterInfoContext context)
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
}
