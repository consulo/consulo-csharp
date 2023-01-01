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

import java.awt.Component;
import java.awt.Font;

import javax.annotation.Nullable;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import consulo.language.psi.PsiCodeFragment;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.ide.impl.idea.refactoring.changeSignature.ParameterTableModelBase;
import consulo.language.editor.refactoring.ui.StringTableCellEditor;
import consulo.ui.ex.awt.ColoredTableCellRenderer;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpParameterTableModel extends ParameterTableModelBase<CSharpParameterInfo, CSharpParameterTableModelItem>
{
	private static class VariableCompletionTableCellEditor extends StringTableCellEditor
	{
		public VariableCompletionTableCellEditor(Project project)
		{
			super(project);
		}

		@Override
		public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column)
		{
			final EditorTextField textField = (EditorTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			/*textField.registerKeyboardAction(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					PsiType type = getRowType(table, row);
					if(type != null)
					{
						completeVariable(textField, type);
					}
				}
			}, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);*/
			textField.setBorder(new LineBorder(table.getSelectionBackground()));
			return textField;
		}

		/*private static void completeVariable(EditorTextField editorTextField, PsiType type)
		{
			Editor editor = editorTextField.getEditor();
			String prefix = editorTextField.getText();
			if(prefix == null)
			{
				prefix = "";
			}
			Set<LookupElement> set = new LinkedHashSet<LookupElement>();
			JavaCompletionUtil.completeVariableNameForRefactoring(editorTextField.getProject(), set, prefix, type, VariableKind.PARAMETER);

			LookupElement[] lookupItems = set.toArray(new LookupElement[set.size()]);
			editor.getCaretModel().moveToOffset(prefix.length());
			editor.getSelectionModel().removeSelection();
			LookupManager.getInstance(editorTextField.getProject()).showLookup(editor, lookupItems, prefix);
		} */
	}

	private static class MyNameColumn extends NameColumn<CSharpParameterInfo, CSharpParameterTableModelItem>
	{
		private Project myProject;

		public MyNameColumn(Project project)
		{
			super(project);
			myProject = project;
		}

		@Override
		public TableCellEditor doCreateEditor(CSharpParameterTableModelItem o)
		{
			return new VariableCompletionTableCellEditor(myProject);
		}

		@Override
		public TableCellRenderer doCreateRenderer(CSharpParameterTableModelItem cSharpParameterTableModelItem)
		{
			return new ColoredTableCellRenderer()
			{
				@Override
				public void customizeCellRenderer(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value == null)
					{
						return;
					}
					if(isSelected || hasFocus)
					{
						acquireState(table, true, false, row, column);
						getCellState().updateRenderer(this);
						setPaintFocusBorder(false);
					}
					append((String) value, new SimpleTextAttributes(Font.PLAIN, null));
				}
			};
		}
	}

	private static class ModifierColumn extends ColumnInfoBase<CSharpParameterInfo, CSharpParameterTableModelItem, CSharpModifier>
	{
		public ModifierColumn()
		{
			super("Modifier");
		}

		@Nullable
		@Override
		public CSharpModifier valueOf(CSharpParameterTableModelItem item)
		{
			return item.parameter.getModifier();
		}

		@Override
		public void setValue(CSharpParameterTableModelItem tableModelItem, CSharpModifier value)
		{
			tableModelItem.parameter.setModifier(value);
		}

		@Override
		protected TableCellRenderer doCreateRenderer(CSharpParameterTableModelItem tableModelItem)
		{
			return new ColoredTableCellRenderer()
			{
				@Override
				protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column)
				{
					append(value == null ? "" : value.toString());
				}
			};
		}

		@Override
		protected TableCellEditor doCreateEditor(CSharpParameterTableModelItem item)
		{
			throw new UnsupportedOperationException();
		}
	}

	private final CSharpMethodDescriptor myMethodDescriptor;

	public CSharpParameterTableModel(CSharpMethodDescriptor methodDescriptor, PsiElement typeContext, PsiElement defaultValueContext)
	{
		super(typeContext, defaultValueContext, new TypeColumn<CSharpParameterInfo, CSharpParameterTableModelItem>(methodDescriptor.getMethod().getProject(), CSharpFileType.INSTANCE), new
						MyNameColumn(methodDescriptor.getMethod().getProject()),
				new DefaultValueColumn<CSharpParameterInfo, CSharpParameterTableModelItem>(methodDescriptor.getMethod().getProject(), CSharpFileType.INSTANCE, "Place value:"), new ModifierColumn());
		myMethodDescriptor = methodDescriptor;
	}

	@Override
	protected CSharpParameterTableModelItem createRowItem(@Nullable CSharpParameterInfo parameterInfo)
	{
		Project project = myMethodDescriptor.getMethod().getProject();

		if(parameterInfo == null)
		{
			parameterInfo = new CSharpParameterInfo("", null, new CSharpTypeRefByQName(myDefaultValueContext, DotNetTypes.System.Object), getRowCount());
		}
		PsiCodeFragment fragment = CSharpFragmentFactory.createTypeFragment(project, parameterInfo.getTypeText(), myDefaultValueContext);

		PsiCodeFragment defaultValue = CSharpFragmentFactory.createExpressionFragment(project, "", myDefaultValueContext);
		return new CSharpParameterTableModelItem(parameterInfo, fragment, defaultValue);
	}
}
