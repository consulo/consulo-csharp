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

package org.mustbe.consulo.csharp.ide.refactoring.changeSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.changeSignature.CallerChooserBase;
import com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase;
import com.intellij.refactoring.changeSignature.ChangeSignatureProcessorBase;
import com.intellij.refactoring.changeSignature.MethodDescriptor;
import com.intellij.refactoring.changeSignature.ParameterInfo;
import com.intellij.refactoring.changeSignature.ParameterTableModelItemBase;
import com.intellij.refactoring.ui.ComboBoxVisibilityPanel;
import com.intellij.refactoring.ui.VisibilityPanelBase;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.Consumer;
import com.intellij.util.Function;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpChangeSignatureDialog extends ChangeSignatureDialogBase<CSharpParameterInfo, DotNetLikeMethodDeclaration, CSharpAccessModifier,
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
		CSharpChangeInfo changeInfo = generateChangeInfo();

		return new ChangeSignatureProcessorBase(getProject(), changeInfo)
		{
			@NotNull
			@Override
			protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usages)
			{
				return new ChangeSignatureViewDescriptor(myMethod.getMethod());
			}
		};
	}

	@NotNull
	public DotNetLikeMethodDeclaration getMethodDeclaration()
	{
		return myMethod.getMethod();
	}

	@RequiredDispatchThread
	private CSharpChangeInfo generateChangeInfo()
	{
		DotNetLikeMethodDeclaration methodDeclaration = getMethodDeclaration();
		String newName = null;
		if(myMethod.canChangeName())
		{
			String methodName = getMethodName();
			if(!Comparing.equal(methodName, methodDeclaration.getName()))
			{
				newName = methodName;
			}
		}

		String newReturnType = null;
		if(myMethod.canChangeReturnType() == MethodDescriptor.ReadWriteOption.ReadWrite)
		{
			String returnType = myReturnTypeField.getText();
			if(!Comparing.equal(typeText(methodDeclaration.getReturnTypeRef()), returnType))
			{
				newReturnType = returnType;
			}
		}

		CSharpAccessModifier newVisibility = null;
		if(myMethod.canChangeVisibility())
		{
			CSharpAccessModifier visibility = getVisibility();
			if(myMethod.getVisibility() != visibility)
			{
				newVisibility = visibility;
			}
		}

		boolean parametersChanged = false;
		List<CSharpParameterInfo> parameters = getParameters();
		DotNetParameter[] psiParameters = methodDeclaration.getParameters();
		if(parameters.size() != psiParameters.length)
		{
			parametersChanged = true;
		}
		else
		{
			for(int i = 0; i < parameters.size(); i++)
			{
				DotNetParameter psiParameter = psiParameters[i];
				CSharpParameterInfo newParameter = parameters.get(i);
				if(!Comparing.equal(newParameter.getName(), psiParameter.getName()))
				{
					parametersChanged = true;
					break;
				}
				if(!Comparing.equal(newParameter.getTypeText(), typeText(psiParameter.toTypeRef(false))))
				{
					parametersChanged = true;
					break;
				}
			}
		}
		return new CSharpChangeInfo(methodDeclaration, parameters, parametersChanged, newName, newReturnType, newVisibility);
	}

	@RequiredReadAction
	private String typeText(@NotNull DotNetTypeRef typeRef)
	{
		return CSharpTypeRefPresentationUtil.buildShortText(typeRef, myDefaultValueContext);
	}

	@Override
	@NotNull
	@RequiredDispatchThread
	public List<CSharpParameterInfo> getParameters()
	{
		List<CSharpParameterInfo> result = new ArrayList<CSharpParameterInfo>(myParametersTableModel.getRowCount());
		int i = 0;
		for(ParameterTableModelItemBase<CSharpParameterInfo> item : myParametersTableModel.getItems())
		{
			CSharpParameterInfo e = new CSharpParameterInfo(item.parameter.getName(), item.parameter.getParameter(), i++);

			DotNetType type = PsiTreeUtil.getChildOfType(item.typeCodeFragment, DotNetType.class);
			e.setTypeText(type == null ? DotNetTypes.System.Object : type.getText());
			e.setTypeRef(type == null ? new CSharpTypeRefByQName(DotNetTypes.System.Object) : type.toTypeRef());

			DotNetExpression expression = PsiTreeUtil.getChildOfType(item.defaultValueCodeFragment, DotNetExpression.class);
			e.setDefaultValue(expression == null ? "" : expression.getText());

			result.add(e);
		}
		return result;
	}

	@Override
	@RequiredDispatchThread
	protected PsiCodeFragment createReturnTypeCodeFragment()
	{
		String text = CSharpTypeRefPresentationUtil.buildShortText(myMethod.getMethod().getReturnTypeRef(), myDefaultValueContext);
		return CSharpFragmentFactory.createTypeFragment(getProject(), text, myDefaultValueContext);
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
	@RequiredDispatchThread
	protected String calculateSignature()
	{
		DotNetLikeMethodDeclaration methodDeclaration = getMethodDeclaration();
		CSharpChangeInfo sharpChangeInfo = generateChangeInfo();
		CSharpAccessModifier newVisibility = sharpChangeInfo.getNewVisibility();
		StringBuilder builder = new StringBuilder();
		if(newVisibility != null)
		{
			builder.append(newVisibility.getPresentableText()).append(" ");
		}
		else
		{
			builder.append(myMethod.getVisibility().getPresentableText()).append(" ");
		}

		if(methodDeclaration instanceof CSharpMethodDeclaration)
		{
			if(sharpChangeInfo.isReturnTypeChanged())
			{
				builder.append(sharpChangeInfo.getNewReturnType()).append(" ");
			}
			else
			{
				builder.append(typeText(methodDeclaration.getReturnTypeRef())).append(" ");
			}
		}

		if(sharpChangeInfo.isNameChanged())
		{
			builder.append(sharpChangeInfo.getNewName());
		}
		else
		{
			builder.append(methodDeclaration.getName());
		}
		builder.append("(");
		builder.append(StringUtil.join(sharpChangeInfo.getNewParameters(), new Function<ParameterInfo, String>()
		{
			@Override
			public String fun(ParameterInfo parameterInfo)
			{
				return parameterInfo.getTypeText() + " " + parameterInfo.getName();
			}
		}, ", "));
		builder.append(");");

		return builder.toString();
	}

	@Override
	protected VisibilityPanelBase<CSharpAccessModifier> createVisibilityControl()
	{
		return new ComboBoxVisibilityPanel<CSharpAccessModifier>(CSharpAccessModifier.VALUES)
		{
			@Override
			protected ListCellRendererWrapper<CSharpAccessModifier> getRenderer()
			{
				return new ListCellRendererWrapper<CSharpAccessModifier>()
				{
					@Override
					public void customize(JList list, CSharpAccessModifier value, int index, boolean selected, boolean hasFocus)
					{
						setText(value.getPresentableText());
					}
				};
			}
		};
	}
}
