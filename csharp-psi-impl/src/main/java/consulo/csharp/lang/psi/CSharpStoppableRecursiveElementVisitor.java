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

package consulo.csharp.lang.psi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import consulo.ui.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public class CSharpStoppableRecursiveElementVisitor<T> extends CSharpRecursiveElementVisitor implements PsiRecursiveVisitor
{
	private T myValue;

	@Override
	@RequiredUIAccess
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
