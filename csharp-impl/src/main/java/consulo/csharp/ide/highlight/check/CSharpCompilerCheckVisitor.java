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

package consulo.csharp.ide.highlight.check;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.lang.doc.CSharpDocUtil;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetElement;
import consulo.msil.representation.fileSystem.MsilFileRepresentationVirtualFile;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CSharpCompilerCheckVisitor extends CSharpElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;
	private CSharpHighlightContext myHighlightContext;
	private CSharpPragmaContext myPragmaContext;

	@Override
	@RequiredReadAction
	public void visitElement(PsiElement element)
	{
		ProgressIndicatorProvider.checkCanceled();
		if(element instanceof DotNetElement)
		{
			boolean insideDoc = element instanceof CSharpReferenceExpression && CSharpDocUtil.isInsideDoc(element);

			CSharpLanguageVersion languageVersion = myHighlightContext.getLanguageVersion();

			for(CSharpCompilerChecks classEntry : CSharpCompilerChecks.VALUES)
			{
				ProgressIndicatorProvider.checkCanceled();

				if(myPragmaContext.isSuppressed(classEntry, element))
				{
					continue;
				}

				if(languageVersion.ordinal() >= classEntry.getLanguageVersion().ordinal() && classEntry.getTargetClass().isAssignableFrom(element.getClass()))
				{
					List<? extends CompilerCheck.HighlightInfoFactory> results = classEntry.check(languageVersion, myHighlightContext, element);
					if(results.isEmpty())
					{
						continue;
					}
					for(CompilerCheck.HighlightInfoFactory result : results)
					{
						HighlightInfo highlightInfo = result.create(insideDoc);
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
	public boolean suitableForFile(@Nonnull PsiFile psiFile)
	{
		VirtualFile virtualFile = psiFile.getVirtualFile();
		return !(virtualFile instanceof MsilFileRepresentationVirtualFile) && psiFile instanceof CSharpFile;
	}

	@Override
	public void visit(@Nonnull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@Nonnull PsiFile psiFile, boolean b, @Nonnull HighlightInfoHolder highlightInfoHolder, @Nonnull Runnable runnable)
	{
		myPragmaContext = CSharpPragmaContext.get(psiFile);
		myHighlightContext = new CSharpHighlightContext(psiFile);
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		myHighlightContext = null;

		return true;
	}

	@Nonnull
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
