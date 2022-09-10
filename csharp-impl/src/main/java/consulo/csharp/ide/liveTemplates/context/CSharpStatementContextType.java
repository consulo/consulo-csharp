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

package consulo.csharp.ide.liveTemplates.context;

import javax.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class CSharpStatementContextType extends TemplateContextType
{
	public CSharpStatementContextType()
	{
		super("CSHARP_STATEMENT", "C# Statement");
	}

	@Override
	@RequiredReadAction
	public boolean isInContext(@Nonnull PsiFile file, int offset)
	{
		PsiElement elementAt = file.findElementAt(offset);

		CSharpLocalVariable localVariable = PsiTreeUtil.getParentOfType(elementAt, CSharpLocalVariable.class);
		return localVariable != null && CSharpPsiUtilImpl.isNullOrEmpty(localVariable);
	}
}
