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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ParameterTableModelBase;
import com.intellij.refactoring.ui.StringTableCellEditor;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.SimpleTextAttributes;

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

	private final Project myProject;

	public CSharpParameterTableModel(Project project, PsiElement typeContext, PsiElement defaultValueContext)
	{
		super(typeContext, defaultValueContext,
				new TypeColumn<CSharpParameterInfo, CSharpParameterTableModelItem>(project, CSharpFileType.INSTANCE),
				new MyNameColumn(project),
				new DefaultValueColumn<CSharpParameterInfo, CSharpParameterTableModelItem>(project, CSharpFileType.INSTANCE, "Place value:"),
				new AnyVarColumn());
		myProject = project;
	}

	@Override
	protected CSharpParameterTableModelItem createRowItem(@Nullable CSharpParameterInfo parameterInfo)
	{
		if(parameterInfo == null)
		{
			parameterInfo = new CSharpParameterInfo("p" + getRowCount(), null, getRowCount());
		}
		PsiCodeFragment fragment = CSharpFragmentFactory.createTypeFragment(myProject, parameterInfo.getTypeText(), myDefaultValueContext);

		PsiCodeFragment defaultValue = CSharpFragmentFactory.createExpressionFragment(myProject, "", myDefaultValueContext);
		return new CSharpParameterTableModelItem(parameterInfo, fragment, defaultValue);
	}
}
