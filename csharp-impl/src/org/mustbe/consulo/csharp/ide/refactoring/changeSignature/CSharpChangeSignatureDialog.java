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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.impl.CS1547;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.changeSignature.CallerChooserBase;
import com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase;
import com.intellij.refactoring.changeSignature.ChangeSignatureProcessorBase;
import com.intellij.refactoring.changeSignature.MethodDescriptor;
import com.intellij.refactoring.changeSignature.ParameterTableModelItemBase;
import com.intellij.refactoring.ui.ComboBoxVisibilityPanel;
import com.intellij.refactoring.ui.VisibilityPanelBase;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.TableColumnAnimator;
import com.intellij.ui.table.TableView;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.Consumer;
import com.intellij.util.PairFunction;
import com.intellij.util.ui.table.JBListTable;
import com.intellij.util.ui.table.JBTableRow;
import com.intellij.util.ui.table.JBTableRowEditor;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpChangeSignatureDialog extends ChangeSignatureDialogBase<CSharpParameterInfo, DotNetLikeMethodDeclaration, CSharpAccessModifier, CSharpMethodDescriptor,
		CSharpParameterTableModelItem, CSharpParameterTableModel>
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
	@RequiredDispatchThread
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

	@Override
	protected boolean isListTableViewSupported()
	{
		return true;
	}

	@Nullable
	@Override
	protected JBTableRowEditor getTableEditor(final JTable t, final ParameterTableModelItemBase<CSharpParameterInfo> item)
	{
		return new JBTableRowEditor()
		{
			private EditorTextField myTypeEditor;
			private EditorTextField myNameEditor;
			private EditorTextField myDefaultValueEditor;
			private ComboBox myModifierComboBox;

			@Override
			public void prepareEditor(JTable table, int row)
			{
				setLayout(new BorderLayout());

				JPanel topPanel = new JPanel(new GridLayout(1, 3));
				add(topPanel, BorderLayout.NORTH);

				myModifierComboBox = new ComboBox();
				myModifierComboBox.addItem(null);
				for(CSharpModifier modifier : CSharpParameterInfo.ourParameterModifiers)
				{
					myModifierComboBox.addItem(modifier);
				}
				myModifierComboBox.addItemListener(new ItemListener()
				{
					@Override
					public void itemStateChanged(ItemEvent e)
					{
						if(e.getStateChange() == ItemEvent.SELECTED)
						{
							item.parameter.setModifier((CSharpModifier) myModifierComboBox.getSelectedItem());
							updateSignature();
						}
					}
				});
				myModifierComboBox.setRenderer(new ListCellRendererWrapper<CSharpModifier>()
				{
					@Override
					public void customize(JList list, CSharpModifier value, int index, boolean selected, boolean hasFocus)
					{
						setText(value == null ? "" : value.getPresentableText());
					}
				});

				myModifierComboBox.setSelectedItem(item.parameter.getModifier());

				topPanel.add(createLabeledPanel("Modifier:", myModifierComboBox));

				final Document document = PsiDocumentManager.getInstance(getProject()).getDocument(item.typeCodeFragment);
				myTypeEditor = new EditorTextField(document, getProject(), getFileType());
				myTypeEditor.addDocumentListener(getSignatureUpdater());
				myTypeEditor.setPreferredWidth(t.getWidth() / 2);
				myTypeEditor.addDocumentListener(new RowEditorChangeListener(0));
				topPanel.add(createLabeledPanel("Type:", myTypeEditor));

				myNameEditor = new EditorTextField(item.parameter.getName(), getProject(), getFileType());
				myNameEditor.addDocumentListener(getSignatureUpdater());
				myNameEditor.addDocumentListener(new RowEditorChangeListener(1));
				topPanel.add(createLabeledPanel("Name:", myNameEditor));

				if(item.parameter.getOldIndex() == -1)
				{
					final JPanel additionalPanel = new JPanel(new BorderLayout());
					final Document doc = PsiDocumentManager.getInstance(getProject()).getDocument(item.defaultValueCodeFragment);
					myDefaultValueEditor = new EditorTextField(doc, getProject(), getFileType());
					myDefaultValueEditor.setPreferredWidth(t.getWidth() / 3);
					myDefaultValueEditor.addDocumentListener(new RowEditorChangeListener(2));
					additionalPanel.add(createLabeledPanel("Argument value:", myDefaultValueEditor), BorderLayout.EAST);

					add(additionalPanel, BorderLayout.SOUTH);
				}
			}

			@Override
			public JBTableRow getValue()
			{
				return new JBTableRow()
				{
					@Override
					public Object getValueAt(int column)
					{
						switch(column)
						{
							case 0:
								return item.typeCodeFragment;
							case 1:
								return myNameEditor.getText().trim();
							case 2:
								return item.defaultValueCodeFragment;
							case 3:
								return myModifierComboBox.getSelectedItem();
						}
						return null;
					}
				};
			}

			@Override
			public JComponent getPreferredFocusedComponent()
			{
				final MouseEvent me = getMouseEvent();
				if(me == null)
				{
					return myTypeEditor.getFocusTarget();
				}
				final double x = me.getPoint().getX();
				return x <= getTypesColumnWidth() ? myTypeEditor.getFocusTarget() : myDefaultValueEditor == null || x <= getNamesColumnWidth() ? myNameEditor.getFocusTarget() : myDefaultValueEditor
						.getFocusTarget();
			}

			@Override
			public JComponent[] getFocusableComponents()
			{
				final List<JComponent> focusable = new ArrayList<JComponent>();
				focusable.add(myTypeEditor.getFocusTarget());
				focusable.add(myNameEditor.getFocusTarget());
				if(myDefaultValueEditor != null)
				{
					focusable.add(myDefaultValueEditor.getFocusTarget());
				}
				focusable.add(myModifierComboBox);
				return focusable.toArray(new JComponent[focusable.size()]);
			}
		};
	}

	@Override
	protected boolean postponeValidation()
	{
		return false;
	}

	@Override
	protected boolean mayPropagateParameters()
	{
		return false;
	}

	@Override
	protected boolean isEmptyRow(ParameterTableModelItemBase<CSharpParameterInfo> row)
	{
		if(!StringUtil.isEmpty(row.parameter.getName()))
		{
			return false;
		}
		if(!StringUtil.isEmpty(row.parameter.getTypeText()))
		{
			return false;
		}
		return true;
	}

	@Override
	@RequiredDispatchThread
	protected JComponent getRowPresentation(ParameterTableModelItemBase<CSharpParameterInfo> item, boolean selected, final boolean focused)
	{
		final String typeText = item.typeCodeFragment.getText();
		CSharpModifier modifier = item.parameter.getModifier();
		String text = "";
		if(modifier != null)
		{
			text = modifier.getPresentableText() + " ";
		}
		final String separator = StringUtil.repeatSymbol(' ', getTypesMaxLength() - typeText.length() + 1);

		text += typeText + separator + item.parameter.getName();
		final String defaultValue = item.defaultValueCodeFragment.getText();
		String tail = "";
		if(StringUtil.isNotEmpty(defaultValue))
		{
			tail += " argument value = " + defaultValue;
		}
		if(!StringUtil.isEmpty(tail))
		{
			text += " //" + tail;
		}
		return JBListTable.createEditorTextFieldPresentation(getProject(), getFileType(), " " + text, selected, focused);
	}

	@Override
	protected void customizeParametersTable(TableView<CSharpParameterTableModelItem> table)
	{
		final JTable t = table.getComponent();
		final TableColumn defaultValue = t.getColumnModel().getColumn(2);
		final TableColumn varArg = t.getColumnModel().getColumn(3);
		t.removeColumn(defaultValue);
		t.removeColumn(varArg);
		t.getModel().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent e)
			{
				if(e.getType() == TableModelEvent.INSERT)
				{
					t.getModel().removeTableModelListener(this);
					final TableColumnAnimator animator = new TableColumnAnimator(t);
					animator.setStep(48);
					animator.addColumn(defaultValue, (t.getWidth() - 48) / 3);
					animator.addColumn(varArg, 48);
					animator.startAndDoWhenDone(new Runnable()
					{
						@Override
						public void run()
						{
							t.editCellAt(t.getRowCount() - 1, 0);
						}
					});
					animator.start();
				}
			}
		});	}

	private int getTypesColumnWidth()
	{
		return getColumnWidth(0);
	}

	private int getNamesColumnWidth()
	{
		return getColumnWidth(1);
	}

	private int getTypesMaxLength()
	{
		int len = 0;
		for(ParameterTableModelItemBase<CSharpParameterInfo> item : myParametersTableModel.getItems())
		{
			final String text = item.typeCodeFragment == null ? null : item.typeCodeFragment.getText();
			len = Math.max(len, text == null ? 0 : text.length());
		}
		return len;
	}

	private int getNamesMaxLength()
	{
		int len = 0;
		for(ParameterTableModelItemBase<CSharpParameterInfo> item : myParametersTableModel.getItems())
		{
			final String text = item.parameter.getName();
			len = Math.max(len, text == null ? 0 : text.length());
		}
		return len;
	}

	private int getColumnWidth(int index)
	{
		int letters = getTypesMaxLength() + (index == 0 ? 1 : getNamesMaxLength() + 2);
		Font font = EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN);
		font = new Font(font.getFontName(), font.getStyle(), 12);
		return letters * Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth("W");
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
				if(!Comparing.equal(newParameter.getModifier(), CSharpParameterInfo.findModifier(psiParameter)))
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
			e.setTypeText(type == null ? "" : type.getText());
			e.setModifier(item.parameter.getModifier());
			e.setTypeRef(type == null ? null : type.toTypeRef());

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
		CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(getProject(), text, myDefaultValueContext);
		typeFragment.putUserData(CS1547.ourReturnTypeFlag, Boolean.TRUE);
		return typeFragment;
	}

	@Nullable
	@Override
	protected CallerChooserBase<DotNetLikeMethodDeclaration> createCallerChooser(String title, Tree treeToReuse, Consumer<Set<DotNetLikeMethodDeclaration>> callback)
	{
		return null;
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	protected String validateAndCommitData()
	{
		String methodName = getMethodName();
		if(StringUtil.isEmpty(methodName) || CSharpNameSuggesterUtil.isKeyword(methodName))
		{
			return "Bad method name";
		}

		for(CSharpParameterInfo parameterInfo : getParameters())
		{
			String name = parameterInfo.getName();
			if(StringUtil.isEmpty(name) || CSharpNameSuggesterUtil.isKeyword(name))
			{
				return "Bad parameter name";
			}

			if(parameterInfo.getTypeRef() == null)
			{
				return "Parameter '" + name + "' have bad type";
			}
		}
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
		StubBlockUtil.join(builder, sharpChangeInfo.getNewParameters(), new PairFunction<StringBuilder, CSharpParameterInfo, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder b, CSharpParameterInfo parameterInfo)
			{
				CSharpModifier modifier = parameterInfo.getModifier();
				if(modifier != null)
				{
					b.append(modifier.getPresentableText()).append(" ");
				}
				b.append(parameterInfo.getTypeText());
				b.append(" ");
				b.append(parameterInfo.getName());
				return null;
			}
		}, ", ");

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
