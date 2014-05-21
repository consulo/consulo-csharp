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

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpCodeFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.changeSignature.CallerChooserBase;
import com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase;
import com.intellij.refactoring.ui.ComboBoxVisibilityPanel;
import com.intellij.refactoring.ui.VisibilityPanelBase;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpChangeSignatureDialog extends ChangeSignatureDialogBase<CSharpParameterInfo, DotNetLikeMethodDeclaration, CSharpModifier,
		CSharpMethodDescriptor, CSharpParameterTableModelItem, CSharpParameterTableModel>
{
	public CSharpChangeSignatureDialog(Project project, CSharpMethodDescriptor method, boolean allowDelegation, PsiElement defaultValueContext)
	{
		super(project, method, allowDelegation, defaultValueContext);
	}

	@Override
	protected LanguageFileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Override
	protected CSharpParameterTableModel createParametersInfoModel(CSharpMethodDescriptor method)
	{
		return new CSharpParameterTableModel(getProject(), myDefaultValueContext, myDefaultValueContext);
	}

	@Override
	protected BaseRefactoringProcessor createRefactoringProcessor()
	{
		return null;
	}

	@Override
	protected PsiCodeFragment createReturnTypeCodeFragment()
	{
		return CSharpCodeFragmentFactory.createFragment(myDefaultValueContext, calculateSignature());
	}

	@Nullable
	@Override
	protected CallerChooserBase<DotNetLikeMethodDeclaration> createCallerChooser(
			String title, Tree treeToReuse, Consumer<Set<DotNetLikeMethodDeclaration>> callback)
	{
		return null;
	}

	@Nullable
	@Override
	protected String validateAndCommitData()
	{
		return null;
	}

	@Override
	protected String calculateSignature()
	{
		return myMethod.getMethod().getReturnTypeRef().getQualifiedText();
	}

	@Override
	protected VisibilityPanelBase<CSharpModifier> createVisibilityControl()
	{
		return new ComboBoxVisibilityPanel<CSharpModifier>(CSharpMethodDescriptor.ourAccessModifiers);
	}
}
