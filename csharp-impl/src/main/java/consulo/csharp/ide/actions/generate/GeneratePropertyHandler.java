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

package consulo.csharp.ide.actions.generate;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import consulo.ui.RequiredUIAccess;
import consulo.annotations.RequiredReadAction;
import consulo.awt.TargetAWT;
import consulo.csharp.ide.codeInsight.actions.AddAccessModifierFix;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class GeneratePropertyHandler implements CodeInsightActionHandler
{
	private static final String ourReplaceReferencesToProperty = "csharp.replace.references.to.property";

	private boolean myReadonly;

	public GeneratePropertyHandler(boolean readonly)
	{
		myReadonly = readonly;
	}

	@RequiredUIAccess
	@Override
	public void invoke(@Nonnull final Project project, @Nonnull final Editor editor, @Nonnull final PsiFile file)
	{
		final CSharpTypeDeclaration typeDeclaration = CSharpGenerateAction.findTypeDeclaration(editor, file);
		if(typeDeclaration == null)
		{
			return;
		}

		List<DotNetFieldDeclaration> fields = GeneratePropertyAction.getFields(typeDeclaration);
		if(fields.isEmpty())
		{
			return;
		}

		ChooseElementsDialog<DotNetFieldDeclaration> dialog = new ChooseElementsDialog<DotNetFieldDeclaration>(project, fields, "Choose field(s)", "Choose field(s) for property generate", true)
		{
			@Override
			protected JComponent createCenterPanel()
			{
				JComponent centerPanel = super.createCenterPanel();
				assert centerPanel != null;
				final JCheckBox replaceUsageToProperty = new JCheckBox("Replace references to property?", PropertiesComponent.getInstance().getBoolean(ourReplaceReferencesToProperty, true));
				replaceUsageToProperty.addItemListener(new ItemListener()
				{
					@Override
					public void itemStateChanged(ItemEvent e)
					{
						PropertiesComponent.getInstance().setValue(ourReplaceReferencesToProperty, replaceUsageToProperty.isSelected(), true);
					}
				});
				centerPanel.add(replaceUsageToProperty, BorderLayout.SOUTH);
				return centerPanel;
			}

			@Override
			protected String getItemText(DotNetFieldDeclaration item)
			{
				return item.getName();
			}

			@Nullable
			@Override
			protected Icon getItemIcon(DotNetFieldDeclaration item)
			{
				return TargetAWT.to(IconDescriptorUpdaters.getIcon(item, Iconable.ICON_FLAG_VISIBILITY));
			}
		};

		final List<DotNetFieldDeclaration> fieldDeclarations = dialog.showAndGetResult();
		if(fieldDeclarations.isEmpty())
		{
			return;
		}

		final int startOffset = editor.getCaretModel().getOffset();

		final String lineIndent = CodeStyleManager.getInstance(project).getLineIndent(editor.getDocument(), startOffset);

		final String allText = StringUtil.join(fieldDeclarations, new Function<DotNetFieldDeclaration, String>()
		{
			@Override
			@RequiredReadAction
			public String fun(DotNetFieldDeclaration fieldDeclaration)
			{
				return generatePropertyTextFromField(lineIndent, fieldDeclaration);
			}
		}, "\n\n");

		PsiDocumentManager.getInstance(project).commitAllDocuments();

		new Task.Backgroundable(project, "Searching references", true)
		{
			@Override
			public void run(@Nonnull ProgressIndicator indicator)
			{
				new WriteCommandAction(project, "Generate property", file)
				{
					@Override
					@RequiredUIAccess
					protected void run(Result result) throws Throwable
					{
						if(PropertiesComponent.getInstance().getBoolean(ourReplaceReferencesToProperty, true))
						{
							for(final DotNetFieldDeclaration fieldDeclaration : fieldDeclarations)
							{
								new AddAccessModifierFix(CSharpModifier.PRIVATE).invoke(project, editor, fieldDeclaration.getNameIdentifier());

								final String propertyName = getPropertyName(project, fieldDeclaration.hasModifier(DotNetModifier.STATIC), fieldDeclaration.getName());
								Query<PsiReference> search = ReferencesSearch.search(fieldDeclaration);
								search.forEach(new Processor<PsiReference>()
								{
									@Override
									public boolean process(PsiReference psiReference)
									{
										if(psiReference instanceof CSharpReferenceExpression)
										{
											psiReference.handleElementRename(propertyName);
										}
										return true;
									}
								});
							}
						}

						PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

						editor.getDocument().insertString(startOffset, allText);

						PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

						CodeStyleManager.getInstance(project).reformatText(file, startOffset, startOffset + allText.length());
					}
				}.execute();
			}
		}.queue();
	}

	@Nonnull
	public static String getClearFieldName(@Nonnull Project project, boolean isStatic, @Nonnull String name)
	{
		CSharpCodeGenerationSettings customSettings = CSharpCodeGenerationSettings.getInstance(project);

		String prefix = isStatic ? customSettings.STATIC_FIELD_PREFIX : customSettings.FIELD_PREFIX;
		String suffix = isStatic ? customSettings.STATIC_FIELD_SUFFIX : customSettings.FIELD_SUFFIX;

		if(!prefix.isEmpty())
		{
			if(name.startsWith(prefix))
			{
				name = name.substring(prefix.length(), name.length());
			}
		}

		if(!suffix.isEmpty())
		{
			if(name.endsWith(suffix))
			{
				name = name.substring(0, name.length() - suffix.length());
			}
		}
		return name;
	}

	public static String getPropertyName(@Nonnull Project project, boolean isStatic, @Nonnull String fieldName)
	{
		CSharpCodeGenerationSettings customSettings = CSharpCodeGenerationSettings.getInstance(project);

		String prefix = isStatic ? customSettings.STATIC_PROPERTY_PREFIX : customSettings.PROPERTY_PREFIX;
		String suffix = isStatic ? customSettings.STATIC_PROPERTY_SUFFIX : customSettings.PROPERTY_SUFFIX;
		return prefix + StringUtil.toTitleCase(getClearFieldName(project, isStatic, fieldName)) + suffix;
	}

	@RequiredReadAction
	private String generatePropertyTextFromField(String lineIndent, DotNetFieldDeclaration fieldDeclaration)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(lineIndent);
		CSharpAccessModifier accessModifier = CSharpAccessModifier.findModifier(fieldDeclaration);
		if(accessModifier != CSharpAccessModifier.NONE)
		{
			builder.append(accessModifier.getPresentableText()).append(" ");
		}

		if(fieldDeclaration.hasModifier(DotNetModifier.STATIC))
		{
			builder.append("static ");
		}

		builder.append(fieldDeclaration.getType().getText()).append(" ");
		String fieldName = fieldDeclaration.getName();
		builder.append(getPropertyName(fieldDeclaration.getProject(), fieldDeclaration.hasModifier(DotNetModifier.STATIC), fieldName));
		builder.append("{get { return ").append(fieldName).append("; }");
		if(!myReadonly)
		{
			builder.append("set { ").append(fieldName).append(" = value; }");
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}
}
