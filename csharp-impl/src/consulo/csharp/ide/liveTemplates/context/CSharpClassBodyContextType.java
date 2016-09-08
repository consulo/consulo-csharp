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

package consulo.csharp.ide.liveTemplates.context;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class CSharpClassBodyContextType extends TemplateContextType
{
	protected CSharpClassBodyContextType()
	{
		super("CSHARP_CLASS_BODY", "C# Class Body");
	}

	@Override
	@RequiredReadAction
	public boolean isInContext(@NotNull PsiFile file, int offset)
	{
		PsiElement elementAt = file.findElementAt(offset);

		CSharpTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(elementAt, CSharpTypeDeclaration.class);
		if(typeDeclaration == null)
		{
			return false;
		}

		CSharpFieldDeclaration fieldDeclaration = PsiTreeUtil.getParentOfType(elementAt, CSharpFieldDeclaration.class);
		if(fieldDeclaration != null && fieldDeclaration.getNameIdentifier() == null)
		{
			return true;
		}
		return false;
	}
}
