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

package org.mustbe.consulo.csharp.ide.highlight.check;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationVirtualFile;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CSharpCompilerCheckVisitor extends CSharpElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;

	@Override
	@RequiredReadAction
	public void visitElement(PsiElement element)
	{
		ProgressIndicatorProvider.checkCanceled();
		if(element instanceof DotNetElement)
		{
			CSharpLanguageVersion languageVersion = CSharpLanguageVersion.HIGHEST;
			CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpSimpleModuleExtension.class);
			if(extension != null)
			{
				languageVersion = extension.getLanguageVersion();
			}

			for(CSharpCompilerChecks classEntry : CSharpCompilerChecks.VALUES)
			{
				ProgressIndicatorProvider.checkCanceled();

				if(languageVersion.ordinal() >= classEntry.getLanguageVersion().ordinal() && classEntry.getTargetClass().isAssignableFrom(element
						.getClass()))
				{
					List<? extends CompilerCheck.HighlightInfoFactory> results = classEntry.check(languageVersion, element);
					if(results.isEmpty())
					{
						continue;
					}
					for(CompilerCheck.HighlightInfoFactory result : results)
					{
						HighlightInfo highlightInfo = result.create();
						if(highlightInfo != null)
						{
							myHighlightInfoHolder.add(highlightInfo);

							for(IntentionAction intentionAction : result.getQuickFixes())
							{
								QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean suitableForFile(@NotNull PsiFile psiFile)
	{
		VirtualFile virtualFile = psiFile.getVirtualFile();
		return !(virtualFile instanceof MsilFileRepresentationVirtualFile) && psiFile instanceof CSharpFile;
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@NotNull PsiFile psiFile, boolean b, @NotNull HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		return true;
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new CSharpCompilerCheckVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}
}
