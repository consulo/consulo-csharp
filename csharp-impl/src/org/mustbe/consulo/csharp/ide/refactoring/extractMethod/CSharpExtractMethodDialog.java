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

package org.mustbe.consulo.csharp.ide.refactoring.extractMethod;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.refactoring.changeSignature.CSharpChangeSignatureDialog;
import org.mustbe.consulo.csharp.ide.refactoring.changeSignature.CSharpMethodDescriptor;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 07.11.2015
 */
public class CSharpExtractMethodDialog extends CSharpChangeSignatureDialog
{
	private Processor<DotNetLikeMethodDeclaration> myProcessor;

	public CSharpExtractMethodDialog(Project project, CSharpMethodDescriptor method, boolean allowDelegation, PsiElement defaultValueContext,
			@NotNull Processor<DotNetLikeMethodDeclaration> processor)
	{
		super(project, method, allowDelegation, defaultValueContext);
		myProcessor = processor;

		setOKButtonText(CommonBundle.getOkButtonText());
		setTitle("Extract Method");
		getRefactorAction().putValue(Action.NAME, CommonBundle.getOkButtonText());
	}

	@Override
	protected void invokeRefactoring(BaseRefactoringProcessor processor)
	{
		CSharpLightMethodDeclarationBuilder builder = (CSharpLightMethodDeclarationBuilder) getMethodDeclaration();

		builder.withName(getMethodName());
		builder.removeModifier(CSharpModifier.PRIVATE); // we need remove old modifier
		CSharpAccessModifier visibility = getVisibility();
		if(visibility != null)
		{
			for(CSharpModifier modifier : visibility.getModifiers())
			{
				builder.addModifier(modifier);
			}
		}

		myProcessor.process(getMethodDeclaration());

		close(DialogWrapper.OK_EXIT_CODE);
	}

	@Override
	protected boolean hasPreviewButton()
	{
		return false;
	}

	@Override
	protected boolean areButtonsValid()
	{
		return !StringUtil.isEmpty(getMethodName());
	}

	@Override
	protected BaseRefactoringProcessor createRefactoringProcessor()
	{
		return null;
	}

	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myNameField;
	}

	@Nullable
	@Override
	protected String validateAndCommitData()
	{
		if(StringUtil.isEmpty(getMethodName()))
		{
			return "Method name cant be empty";
		}
		return super.validateAndCommitData();
	}
}
