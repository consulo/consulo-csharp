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

package consulo.csharp.lang.impl.psi;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiRecursiveVisitor;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public class CSharpStoppableRecursiveElementVisitor<T> extends CSharpRecursiveElementVisitor implements PsiRecursiveVisitor
{
	private T myValue;

	@Override
	@RequiredReadAction
	public void visitElement(PsiElement element)
	{
		ProgressManager.checkCanceled();
		if(myValue != null)
		{
			return;
		}
		PsiElement child = element.getFirstChild();
		while(child != null)
		{
			if(myValue != null)
			{
				return;
			}
			child.accept(this);
			child = child.getNextSibling();
		}
	}

	@Nullable
	public T getValue()
	{
		return myValue;
	}

	public void stopWalk(@Nonnull T value)
	{
		myValue = value;
	}
}
