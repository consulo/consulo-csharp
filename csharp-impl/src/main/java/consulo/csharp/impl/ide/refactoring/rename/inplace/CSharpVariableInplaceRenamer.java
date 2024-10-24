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

package consulo.csharp.impl.ide.refactoring.rename.inplace;

import consulo.codeEditor.Editor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.editor.refactoring.rename.inplace.VariableInplaceRenamer;
import consulo.language.psi.PsiNamedElement;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14-Nov-17
 */
public class CSharpVariableInplaceRenamer extends VariableInplaceRenamer
{
	@RequiredReadAction
	public CSharpVariableInplaceRenamer(@Nonnull PsiNamedElement elementToRename, Editor editor)
	{
		super(elementToRename, editor, elementToRename.getProject(), CSharpNamedElement.getEscapedName((DotNetNamedElement) elementToRename), CSharpNamedElement.getEscapedName((DotNetNamedElement)
				elementToRename));
	}
}
