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

package org.mustbe.consulo.csharp.ide.reflactoring.changeSignature;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCodeFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCodeFragmentImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ParameterTableModelBase;
import com.intellij.util.ui.ColumnInfo;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpParameterTableModel extends ParameterTableModelBase<CSharpParameterInfo, CSharpParameterTableModelItem>
{
	public CSharpParameterTableModel(Project project, PsiElement typeContext, PsiElement defaultValueContext)
	{
		super(typeContext, defaultValueContext, new ColumnInfo[]{new NameColumn<CSharpParameterInfo, CSharpParameterTableModelItem>(project)});
	}

	@Override
	protected CSharpParameterTableModelItem createRowItem(@Nullable CSharpParameterInfo parameterInfo)
	{
		CSharpCodeFragmentImpl fragment = CSharpCodeFragmentFactory.createFragment(parameterInfo.getParameter(), parameterInfo.getTypeText());
		return new CSharpParameterTableModelItem(parameterInfo, fragment, null);
	}
}
