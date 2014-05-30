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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessor;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpPsiTreeChangePreprocessor implements PsiTreeChangePreprocessor
{
	private final PsiModificationTrackerImpl myTracker;

	public CSharpPsiTreeChangePreprocessor(PsiModificationTracker tracker)
	{
		myTracker = (PsiModificationTrackerImpl) tracker;
	}

	@Override
	public void treeChanged(@NotNull PsiTreeChangeEventImpl psiTreeChangeEvent)
	{
		switch(psiTreeChangeEvent.getCode())
		{
			case BEFORE_CHILD_ADDITION:
			case BEFORE_CHILD_REMOVAL:
			case BEFORE_CHILD_REPLACEMENT:
			case BEFORE_CHILD_MOVEMENT:
			case BEFORE_CHILDREN_CHANGE:
			case BEFORE_PROPERTY_CHANGE:
				break;
			case CHILD_ADDED:
			case CHILD_REMOVED:
			case CHILD_REPLACED:
			case CHILD_MOVED:
			case CHILDREN_CHANGED:
			case PROPERTY_CHANGED:
				PsiFile file = psiTreeChangeEvent.getFile();
				if(file == null || file.getFileType() != CSharpFileType.INSTANCE)
				{
					return;
				}
				PsiElement element = psiTreeChangeEvent.getElement();
				if(element == null)
				{
					return;
				}
				DotNetStatement statement = PsiTreeUtil.getParentOfType(element, DotNetStatement.class);
				if(statement == null)
				{
					return;
				}
				myTracker.incCounter();
				break;
		}
	}
}
