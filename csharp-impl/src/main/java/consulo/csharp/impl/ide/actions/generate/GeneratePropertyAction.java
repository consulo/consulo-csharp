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

package consulo.csharp.impl.ide.actions.generate;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class GeneratePropertyAction extends CSharpGenerateAction
{
	public GeneratePropertyAction()
	{
		this(new GeneratePropertyHandler(false));
	}

	public GeneratePropertyAction(CodeInsightActionHandler handler)
	{
		super(handler);
	}

	@Override
	@RequiredReadAction
	protected boolean isValidForFile(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file)
	{
		CSharpTypeDeclaration typeDeclaration = findTypeDeclaration(editor, file);

		return typeDeclaration != null && !getFields(typeDeclaration).isEmpty();
	}

	@RequiredReadAction
	public static List<DotNetFieldDeclaration> getFields(CSharpTypeDeclaration typeDeclaration)
	{
		List<DotNetFieldDeclaration> fieldDeclarations = new ArrayList<>(5);
		for(DotNetNamedElement dotNetNamedElement : typeDeclaration.getMembers())
		{
			if(dotNetNamedElement instanceof DotNetFieldDeclaration)
			{
				DotNetFieldDeclaration fieldDeclaration = (DotNetFieldDeclaration) dotNetNamedElement;
				if(CSharpPsiUtilImpl.isNullOrEmpty(fieldDeclaration))
				{
					continue;
				}
				fieldDeclarations.add(fieldDeclaration);
			}
		}
		return fieldDeclarations;
	}
}
