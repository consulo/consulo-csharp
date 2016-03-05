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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import com.intellij.lang.ImportOptimizer;
import com.intellij.psi.PsiFile;

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
			@Nullable
			@Override
			public String getUserNotificationInfo()
			{
				return null;
			}

			@Override
			public void run()
			{
			}

		};
	}
}
