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

package org.mustbe.consulo.csharp.ide.reflactoring;

import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CSharpRefactoringUtil
{
	public static void replaceNameIdentifier(PsiNameIdentifierOwner owner, String newName)
	{
		PsiElement nameIdentifier = owner.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return;
		}

		PsiElement newIdentifier = CSharpFileFactory.createIdentifier(owner.getProject(), newName);

		nameIdentifier.replace(newIdentifier);
	}
}
