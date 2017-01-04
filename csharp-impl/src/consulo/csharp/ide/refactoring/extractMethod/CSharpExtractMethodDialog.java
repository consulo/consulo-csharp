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

package consulo.csharp.ide.refactoring.extractMethod;

import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.ide.refactoring.changeSignature.CSharpChangeSignatureDialog;
import consulo.csharp.ide.refactoring.changeSignature.CSharpMethodDescriptor;
import consulo.csharp.ide.refactoring.changeSignature.CSharpParameterInfo;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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
	@RequiredDispatchThread
	protected void invokeRefactoring(BaseRefactoringProcessor processor)
	{
		CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(getProject());
		builder.withName(getMethodName());
		CSharpAccessModifier visibility = getVisibility();
		if(visibility != null)
		{
			for(CSharpModifier modifier : visibility.getModifiers())
			{
				builder.addModifier(modifier);
			}
		}

		if(myMethod.getMethod().hasModifier(CSharpModifier.STATIC))
		{
			builder.addModifier(CSharpModifier.STATIC);
		}

		DotNetTypeRef returnTypeRef = new CSharpTypeRefByQName(myMethod.getMethod(), DotNetTypes.System.Void);
		DotNetType returnType = PsiTreeUtil.getChildOfType(myReturnTypeCodeFragment, DotNetType.class);
		if(returnType != null)
		{
			returnTypeRef = returnType.toTypeRef();
		}

		builder.withReturnType(returnTypeRef);

		List<CSharpParameterInfo> parameters = getParameters();
		for(CSharpParameterInfo parameter : parameters)
		{
			CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(getProject());
			parameterBuilder.withName(parameter.getName());
			parameterBuilder.withTypeRef(parameter.getTypeRef());
			CSharpModifier modifier = parameter.getModifier();
			if(modifier != null)
			{
				parameterBuilder.addModifier(modifier);
			}

			builder.addParameter(parameterBuilder);
		}

		myProcessor.process(builder);

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

	@RequiredDispatchThread
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
}
