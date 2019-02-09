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

package consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.codeInsight.daemon.NavigateAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import consulo.ui.RequiredUIAccess;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 01.03.2016
 */
public class LambdaLineMarkerCollector implements LineMarkerCollector
{
	public static class MarkerInfo extends MergeableLineMarkerInfo<PsiElement>
	{
		public MarkerInfo(@Nonnull PsiElement element,
				@Nonnull TextRange textRange,
				Image icon,
				int updatePass,
				@Nullable Function<? super PsiElement, String> tooltipProvider,
				@Nullable GutterIconNavigationHandler<PsiElement> navHandler,
				@Nonnull GutterIconRenderer.Alignment alignment)
		{
			super(element, textRange, icon, updatePass, tooltipProvider, navHandler, alignment);
		}

		@Override
		public boolean canMergeWith(@Nonnull MergeableLineMarkerInfo<?> info)
		{
			return info instanceof MarkerInfo;
		}

		@Nonnull
		@Override
		public Image getCommonIcon(@Nonnull List<MergeableLineMarkerInfo> infos)
		{
			return myIcon;
		}

		@Nonnull
		@Override
		public Function<? super PsiElement, String> getCommonTooltip(@Nonnull List<MergeableLineMarkerInfo> infos)
		{
			return new ConstantFunction<>("Navigate to lambda delegate");
		}
	}

	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> lineMarkerInfos)
	{
		IElementType elementType = PsiUtilCore.getElementType(psiElement);
		if(elementType == CSharpTokens.DARROW)
		{
			PsiElement parent = psiElement.getParent();
			if(!(parent instanceof CSharpLambdaExpressionImpl))
			{
				return;
			}

			MarkerInfo markerInfo = new MarkerInfo(parent, psiElement.getTextRange(), AllIcons.Gutter.ImplementingFunctional, Pass.UPDATE_ALL, new ConstantFunction<>("Navigate to lambda delegate"),
					new GutterIconNavigationHandler<PsiElement>()
			{
				@Override
				@RequiredUIAccess
				public void navigate(MouseEvent e, PsiElement elt)
				{
					if(!(elt instanceof CSharpLambdaExpressionImpl))
					{
						return;
					}
					CSharpLambdaResolveResult lambdaResolveResult = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(elt);
					if(lambdaResolveResult == null)
					{
						return;
					}

					PsiElement element = lambdaResolveResult.getElement();
					if(element instanceof Navigatable)
					{
						((Navigatable) element).navigate(true);
					}
				}
			}, GutterIconRenderer.Alignment.RIGHT);
			NavigateAction.setNavigateAction(markerInfo, "Navigate to lambda delegate", IdeActions.ACTION_GOTO_SUPER);
			lineMarkerInfos.consume(markerInfo);
		}
	}
}
