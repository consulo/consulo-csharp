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

package consulo.csharp.impl.ide.refactoring.changeSignature;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.EditorFontType;
import consulo.csharp.impl.ide.highlight.check.impl.CS1547;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFileImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.document.Document;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.ide.impl.idea.ui.TableColumnAnimator;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.language.editor.refactoring.BaseRefactoringProcessor;
import consulo.language.editor.refactoring.changeSignature.*;
import consulo.language.editor.refactoring.ui.ComboBoxVisibilityPanel;
import consulo.language.editor.refactoring.ui.JBListTableWitEditors;
import consulo.language.editor.refactoring.ui.VisibilityPanelBase;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiCodeFragment;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.ListCellRendererWrapper;
import consulo.ui.ex.awt.table.JBTableRow;
import consulo.ui.ex.awt.table.JBTableRowEditor;
import consulo.ui.ex.awt.table.TableView;
import consulo.ui.ex.awt.tree.Tree;
import consulo.usage.UsageInfo;
import consulo.usage.UsageViewDescriptor;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.PairFunction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

;

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
		return new CSharpParameterTableModel(method, myDefaultValueContext, myDefaultValueContext);
	}

	@Override
	@RequiredUIAccess
	protected BaseRefactoringProcessor createRefactoringProcessor()
	{
		CSharpChangeInfo changeInfo = generateChangeInfo();

		return new ChangeSignatureProcessorBase(getProject(), changeInfo)
		{
			@Nonnull
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
				final List<JComponent> focusable = new ArrayList<>();
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
	@RequiredUIAccess
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
		return JBListTableWitEditors.createEditorTextFieldPresentation(getProject(), getFileType(), " " + text, selected, focused);
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
		});
	}

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

	@Nonnull
	public DotNetLikeMethodDeclaration getMethodDeclaration()
	{
		return myMethod.getMethod();
	}

	@RequiredUIAccess
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
	private String typeText(@Nonnull DotNetTypeRef typeRef)
	{
		return CSharpTypeRefPresentationUtil.buildShortText(typeRef);
	}

	@Override
	@Nonnull
	@RequiredUIAccess
	public List<CSharpParameterInfo> getParameters()
	{
		List<CSharpParameterInfo> result = new ArrayList<>(myParametersTableModel.getRowCount());
		int i = 0;
		for(ParameterTableModelItemBase<CSharpParameterInfo> item : myParametersTableModel.getItems())
		{
			DotNetParameter parameter = item.parameter.getParameter();
			DotNetTypeRef typeRef = parameter == null ? new CSharpTypeRefByQName(myDefaultValueContext, DotNetTypes.System.Object) : parameter.toTypeRef(true);

			CSharpParameterInfo e = new CSharpParameterInfo(item.parameter.getName(), item.parameter.getParameter(), typeRef, i++);

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
	@RequiredUIAccess
	protected PsiCodeFragment createReturnTypeCodeFragment()
	{
		String text = CSharpTypeRefPresentationUtil.buildShortText(myMethod.getMethod().getReturnTypeRef());
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
	@RequiredUIAccess
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
	@RequiredUIAccess
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
