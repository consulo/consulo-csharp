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

package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.highlight.check.impl.CS0023;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.ui.JBUI;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class CSharpIntroduceLocalVariableHandler extends CSharpIntroduceHandler
{
	public CSharpIntroduceLocalVariableHandler()
	{
		super(RefactoringBundle.message("introduce.variable.title"));
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected Collection<String> getSuggestedNames(@NotNull DotNetExpression initializer)
	{
		Collection<String> suggestedNames = super.getSuggestedNames(initializer);
		if(initializer instanceof CSharpMethodCallExpressionImpl)
		{
			DotNetExpression callExpression = ((CSharpMethodCallExpressionImpl) initializer).getCallExpression();
			if(callExpression instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) callExpression).getQualifier() == null)
			{
				removeCollisionOnNonQualifiedReferenceExpressions(suggestedNames, (CSharpReferenceExpression) callExpression);
			}
		}
		else if(initializer instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) initializer).getQualifier() == null)
		{
			removeCollisionOnNonQualifiedReferenceExpressions(suggestedNames, (CSharpReferenceExpression) initializer);
		}
		return suggestedNames;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected String getDeclarationString(CSharpIntroduceOperation operation, String initExpression)
	{
		StringBuilder builder = new StringBuilder();
		DotNetExpression initializer = operation.getInitializer();
		CSharpCodeGenerationSettings generationSettings = CSharpCodeGenerationSettings.getInstance(operation.getProject());
		buildVariableTypeString(operation.getProject(), initializer, builder, generationSettings.USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE);
		builder.append(" ").append(operation.getName()).append(" = ").append(initExpression);
		PsiElement parent = initializer.getParent();
		if(!(parent instanceof CSharpExpressionStatementImpl) || !StringUtil.endsWith(parent.getText(), ";") || ((CSharpExpressionStatementImpl) parent).getExpression() != initializer)
		{
			builder.append(";");
		}
		builder.append('\n');
		return builder.toString();
	}

	@NotNull
	@Override
	protected InplaceVariableIntroducer<PsiElement> createVariableIntroducer(CSharpLocalVariable target, CSharpIntroduceOperation operation, List<PsiElement> occurrences)
	{
		return new CSharpInplaceVariableIntroducer(target, operation, occurrences)
		{
			private JCheckBox myUseVarType;
			private JCheckBox myConstant;

			private boolean mySetVarAfterConstant;

			@RequiredReadAction
			@Override
			protected int getVariableEndOffset(DotNetVariable variable)
			{
				if(variable instanceof CSharpLocalVariable)
				{
					PsiElement parent = variable.getParent();
					return parent.getTextRange().getEndOffset();
				}
				return super.getVariableEndOffset(variable);
			}

			@Nullable
			@Override
			@RequiredDispatchThread
			protected JComponent getComponent()
			{
				CSharpLocalVariable variable = (CSharpLocalVariable) getVariable();
				assert variable != null;
				final DotNetExpression initializer = variable.getInitializer();
				assert initializer != null;

				int nextX = 0;
				if(canUseVar(initializer))
				{
					myUseVarType = new NonFocusableCheckBox("Use var type?");
					myUseVarType.setMnemonic('v');
					myUseVarType.setSelected(CSharpCodeGenerationSettings.getInstance(myProject).USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE);
					myUseVarType.addItemListener(new ItemListener()
					{
						@Override
						@RequiredDispatchThread
						public void itemStateChanged(ItemEvent e)
						{
							CSharpCodeGenerationSettings.getInstance(myProject).USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE = myUseVarType.isSelected();

							doVarType(initializer, myUseVarType.isSelected());
						}
					});
					nextX++;
				}

				if(isConstant(initializer))
				{
					myConstant = new NonFocusableCheckBox("Constant?");
					myConstant.setMnemonic('c');
					myConstant.addItemListener(new ItemListener()
					{
						@Override
						@RequiredDispatchThread
						public void itemStateChanged(ItemEvent e)
						{
							doConstantOrRemove(myConstant.isSelected());
						}
					});
				}

				if(myUseVarType == null && myConstant == null)
				{
					return null;
				}

				final JPanel panel = new JPanel(new GridBagLayout());
				panel.setBorder(null);

				if(myUseVarType != null)
				{
					panel.add(myUseVarType, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, JBUI.insets(5), 0, 0));
				}

				if(myConstant != null)
				{
					panel.add(myConstant, new GridBagConstraints(nextX, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, JBUI.insets(5), 0, 0));
				}

				panel.add(Box.createVerticalBox(), new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0));

				return panel;
			}

			@RequiredDispatchThread
			private void doConstantOrRemove(final boolean value)
			{
				PsiDocumentManager.getInstance(myProject).commitAllDocuments();

				if(value)
				{
					final CSharpLocalVariable temp = (CSharpLocalVariable) getVariable();
					assert temp != null;

					if(temp.toTypeRef(false) == DotNetTypeRef.AUTO_TYPE)
					{
						boolean oldValue = CSharpCodeGenerationSettings.getInstance(myProject).USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE;

						mySetVarAfterConstant = myUseVarType.isSelected();

						myUseVarType.setSelected(false);

						CSharpCodeGenerationSettings.getInstance(myProject).USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE = oldValue;
					}
				}
				else if(mySetVarAfterConstant)
				{
					myUseVarType.setSelected(true);
					mySetVarAfterConstant = false;
				}

				new WriteCommandAction(myProject, getCommandName(), getCommandName())
				{
					@Override
					@RequiredWriteAction
					protected void run(Result result) throws Throwable
					{
						final CSharpLocalVariable temp = (CSharpLocalVariable) getVariable();
						assert temp != null;
						if(value)
						{
							if(temp.isConstant())
							{
								return;
							}
							DotNetType type = temp.getType();
							CSharpLocalVariable localVariable = CSharpFileFactory.createLocalVariable(myProject, "const int b;");
							PsiElement first = localVariable.getConstantKeywordElement();
							PsiElement last = first.getNode().getTreeNext().getPsi();
							temp.addRangeBefore(first, last, type);
						}
						else
						{
							PsiElement constantKeywordElement = temp.getConstantKeywordElement();

							if(constantKeywordElement != null)
							{
								PsiElement nextSibling = constantKeywordElement.getNextSibling();

								constantKeywordElement.delete();
								if(nextSibling instanceof PsiWhiteSpace)
								{
									temp.getNode().removeChild(nextSibling.getNode());
								}
							}
						}
					}
				}.execute();
			}

			@RequiredDispatchThread
			private void doVarType(final DotNetExpression initializer, final boolean value)
			{
				PsiDocumentManager.getInstance(myProject).commitAllDocuments();

				final CSharpLocalVariable temp = (CSharpLocalVariable) getVariable();
				if(temp != null && temp.isConstant())
				{
					myConstant.setSelected(false);
				}

				new WriteCommandAction(myProject, getCommandName(), getCommandName())
				{
					@Override
					@RequiredWriteAction
					protected void run(Result result) throws Throwable
					{
						final CSharpLocalVariable temp = (CSharpLocalVariable) getVariable();
						assert temp != null;

						StringBuilder builder = new StringBuilder();

						buildVariableTypeString(myProject, initializer, builder, value);

						DotNetType varType = CSharpFileFactory.createType(myProject, builder);

						temp.getType().replace(varType);
					}
				}.execute();
			}
		};
	}

	@RequiredReadAction
	private static void buildVariableTypeString(@NotNull Project project, @NotNull DotNetExpression initializer, @NotNull StringBuilder builder, boolean value)
	{
		if(value && canUseVar(initializer))
		{
			builder.append("var");
		}
		else
		{
			DotNetTypeRef initalizerTypeRef = initializer.toTypeRef(true);
			if(initalizerTypeRef == DotNetTypeRef.AUTO_TYPE || initalizerTypeRef == DotNetTypeRef.ERROR_TYPE || initalizerTypeRef == DotNetTypeRef.UNKNOWN_TYPE)
			{
				builder.append(StringUtil.getShortName(DotNetTypes.System.Object));
			}
			else
			{
				DotNetTypeResolveResult typeResolveResult = initalizerTypeRef.resolve(initializer);
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(initializer);
					if(!expectedTypeRefs.isEmpty())
					{
						CSharpTypeRefPresentationUtil.appendTypeRef(initializer, builder, expectedTypeRefs.get(0).getTypeRef(), CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
						return;
					}
				}

				CSharpTypeRefPresentationUtil.appendTypeRef(initializer, builder, initalizerTypeRef, CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
			}
		}
	}

	@RequiredReadAction
	public static boolean canUseVar(@NotNull DotNetExpression initializer)
	{
		if(!CSharpModuleUtil.findLanguageVersion(initializer).isAtLeast(CSharpLanguageVersion._3_0))
		{
			return false;
		}
		if(CS0023.isNullConstant(initializer))
		{
			return false;
		}
		DotNetTypeRef initializerType = initializer.toTypeRef(false);
		DotNetTypeResolveResult typeResolveResult = initializerType.resolve(initializer);
		if(typeResolveResult instanceof CSharpLambdaResolveResult && ((CSharpLambdaResolveResult) typeResolveResult).getTarget() == null)
		{
			return false;
		}
		return true;
	}

	public boolean isConstant(DotNetExpression initializer)
	{
		if(initializer instanceof CSharpConstantExpressionImpl)
		{
			return true;
		}
		return false;
	}

	private void removeCollisionOnNonQualifiedReferenceExpressions(Collection<String> suggestedNames, CSharpReferenceExpression referenceExpression)
	{
		String referenceName = referenceExpression.getReferenceName();
		suggestedNames.remove(referenceName);

		int index = 1;
		String lastName = null;
		while(suggestedNames.contains(lastName = (referenceName + index)))
		{
			index++;
		}
		suggestedNames.add(lastName);
	}
}
