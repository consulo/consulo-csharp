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

package consulo.csharp.impl.ide.liveTemplates.context;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.editor.template.context.BaseTemplateContextType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.06.14
 */
@ExtensionImpl
public class CSharpExpressionContextType extends BaseTemplateContextType
{
	public CSharpExpressionContextType()
	{
		super("CSHARP_EXPRESSION", LocalizeValue.localizeTODO("C# Expression"));
	}

	@Override
	@RequiredReadAction
	public boolean isInContext(@Nonnull PsiFile file, int offset)
	{
		PsiElement elementAt = file.findElementAt(offset);

		if(PsiTreeUtil.getParentOfType(elementAt, CSharpUserType.class) != null)
		{
			return false;
		}
		return PsiTreeUtil.getParentOfType(elementAt, DotNetExpression.class) != null;
	}
}
