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

package org.mustbe.consulo.csharp.ide;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.HideOrOverrideMethodCollector;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.LineMarkerCollector;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.OverrideTypeCollector;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.PartialTypeCollector;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.RecursiveCallCollector;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.FunctionUtil;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLineMarkerProvider implements LineMarkerProvider, DumbAware
{
	private static final LineMarkerCollector[] ourCollectors = {
			new OverrideTypeCollector(),
			new PartialTypeCollector(),
			new RecursiveCallCollector(),
			new HideOrOverrideMethodCollector(),
	};

	protected final DaemonCodeAnalyzerSettings daemonCodeAnalyzerSettings;
	protected final EditorColorsManager editorColorsManager;

	public CSharpLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager)
	{
		daemonCodeAnalyzerSettings = daemonSettings;
		editorColorsManager = colorsManager;
	}

	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		if(daemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS && (element instanceof DotNetQualifiedElement))
		{
			if(element.getNode().getTreeParent() == null)
			{
				return null;
			}

			final PsiElement parent = element.getParent();
			if(!(parent instanceof DotNetMemberOwner))
			{
				return null;
			}

			if(((DotNetMemberOwner) parent).getMembers()[0] == element)
			{
				return null;
			}

			LineMarkerInfo info = new LineMarkerInfo<PsiElement>(element, element.getTextRange(), null, Pass.UPDATE_ALL, FunctionUtil.<Object,
					String>nullConstant(), null, GutterIconRenderer.Alignment.RIGHT);
			EditorColorsScheme scheme = editorColorsManager.getGlobalScheme();
			info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
			info.separatorPlacement = SeparatorPlacement.TOP;
			return info;
		}

		return null;
	}

	@Override
	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		ApplicationManager.getApplication().assertReadAccessAllowed();

		if(elements.isEmpty() || DumbService.getInstance(elements.get(0).getProject()).isDumb())
		{
			return;
		}

		//noinspection ForLoopReplaceableByForEach
		for(int i = 0; i < elements.size(); i++)
		{
			PsiElement psiElement = elements.get(i);

			//noinspection ForLoopReplaceableByForEach
			for(int j = 0; j < ourCollectors.length; j++)
			{
				LineMarkerCollector ourCollector = ourCollectors[j];
				ourCollector.collect(psiElement, lineMarkerInfos);
			}
		}
	}
}
