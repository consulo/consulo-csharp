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

package org.mustbe.consulo.csharp.lang;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.lang.ImportOptimizer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
public class CSharpImportOptimizer implements ImportOptimizer
{
	@Override
	public boolean supports(PsiFile psiFile)
	{
		return psiFile instanceof CSharpFile;
	}

	@NotNull
	@Override
	public Runnable processFile(final PsiFile psiFile)
	{
		return new CollectingInfoRunnable()
		{
			private int myCount = 0;
			@Nullable
			@Override
			public String getUserNotificationInfo()
			{
				if(myCount > 0)
				{
					return "removed " + myCount + " using statement" + (myCount != 1 ? "s" : "");
				}
				return null;
			}

			@Override
			public void run()
			{
				final UnusedUsingVisitor unusedUsingVisitor = new UnusedUsingVisitor();
				PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor()
				{
					@Override
					public void visitElement(PsiElement element)
					{
						element.accept(unusedUsingVisitor);
						super.visitElement(element);
					}
				};

				psiFile.accept(visitor);

				for(Map.Entry<CSharpUsingListChild, Boolean> entry : unusedUsingVisitor.getUsingContext().entrySet())
				{
					if(entry.getValue())
					{
						continue;
					}

					myCount ++;
					entry.getKey().delete();
				}
			}
		};
	}
}
