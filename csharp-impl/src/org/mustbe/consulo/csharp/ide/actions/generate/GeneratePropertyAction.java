/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.actions.generate;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

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
	protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
	{
		CSharpTypeDeclaration typeDeclaration = findTypeDeclaration(editor, file);

		return typeDeclaration != null && !getFields(typeDeclaration).isEmpty();
	}

	@RequiredReadAction
	public static List<DotNetFieldDeclaration> getFields(CSharpTypeDeclaration typeDeclaration)
	{
		List<DotNetFieldDeclaration> fieldDeclarations = new ArrayList<DotNetFieldDeclaration>(5);
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
