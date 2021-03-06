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

package consulo.csharp.ide;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.inject.Inject;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.lineMarkerProvider.HidedOrOverridedElementCollector;
import consulo.csharp.ide.lineMarkerProvider.HidingOrOverridingElementCollector;
import consulo.csharp.ide.lineMarkerProvider.LambdaLineMarkerCollector;
import consulo.csharp.ide.lineMarkerProvider.LineMarkerCollector;
import consulo.csharp.ide.lineMarkerProvider.OverrideTypeCollector;
import consulo.csharp.ide.lineMarkerProvider.PartialTypeCollector;
import consulo.csharp.ide.lineMarkerProvider.RecursiveCallCollector;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
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
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.FunctionUtil;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLineMarkerProvider implements LineMarkerProvider, DumbAware
{
	private static final LineMarkerCollector[] ourSingleCollector = {
			new LambdaLineMarkerCollector()
	};

	private static final LineMarkerCollector[] ourCollectors = {
			new OverrideTypeCollector(),
			new PartialTypeCollector(),
			new HidingOrOverridingElementCollector(),
			new HidedOrOverridedElementCollector(),
			new RecursiveCallCollector()
	};

	protected final DaemonCodeAnalyzerSettings myDaemonCodeAnalyzerSettings;
	protected final EditorColorsManager myEditorColorsManager;

	@Inject
	public CSharpLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager)
	{
		myDaemonCodeAnalyzerSettings = daemonSettings;
		myEditorColorsManager = colorsManager;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@Nonnull PsiElement element)
	{
		if(myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS && (element instanceof DotNetQualifiedElement))
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

			if(ArrayUtil.getFirstElement(((DotNetMemberOwner) parent).getMembers()) == element)
			{
				return null;
			}

			LineMarkerInfo info = new LineMarkerInfo<PsiElement>(element, element.getTextRange(), null, Pass.UPDATE_ALL, FunctionUtil.<Object, String>nullConstant(), null,
					GutterIconRenderer.Alignment.RIGHT);
			EditorColorsScheme scheme = myEditorColorsManager.getGlobalScheme();
			info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
			info.separatorPlacement = SeparatorPlacement.TOP;
			return info;
		}

		final Ref<LineMarkerInfo> ref = Ref.create();
		Consumer<LineMarkerInfo> consumer = new Consumer<LineMarkerInfo>()
		{
			@Override
			public void consume(LineMarkerInfo markerInfo)
			{
				ref.set(markerInfo);
			}
		};

		//noinspection ForLoopReplaceableByForEach
		for(int j = 0; j < ourSingleCollector.length; j++)
		{
			LineMarkerCollector ourCollector = ourSingleCollector[j];
			ourCollector.collect(element, consumer);
		}

		return ref.get();
	}

	@RequiredReadAction
	@Override
	public void collectSlowLineMarkers(@Nonnull List<PsiElement> elements, @Nonnull final Collection<LineMarkerInfo> lineMarkerInfos)
	{
		ApplicationManager.getApplication().assertReadAccessAllowed();

		if(elements.isEmpty() || DumbService.getInstance(elements.get(0).getProject()).isDumb())
		{
			return;
		}

		Consumer<LineMarkerInfo> consumer = new Consumer<LineMarkerInfo>()
		{
			@Override
			public void consume(LineMarkerInfo lineMarkerInfo)
			{
				lineMarkerInfos.add(lineMarkerInfo);
			}
		};

		//noinspection ForLoopReplaceableByForEach
		for(int i = 0; i < elements.size(); i++)
		{
			PsiElement psiElement = elements.get(i);

			//noinspection ForLoopReplaceableByForEach
			for(int j = 0; j < ourCollectors.length; j++)
			{
				LineMarkerCollector ourCollector = ourCollectors[j];
				ourCollector.collect(psiElement, consumer);
			}
		}
	}
}
