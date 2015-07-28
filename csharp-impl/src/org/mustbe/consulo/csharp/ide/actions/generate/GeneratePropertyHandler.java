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

package org.mustbe.consulo.csharp.ide.actions.generate;

import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.ide.util.ChooseElementsDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.util.Function;

/**
* @author VISTALL
* @since 24.07.2015
*/
public class GeneratePropertyHandler implements CodeInsightActionHandler
{
	private boolean myReadonly;

	public GeneratePropertyHandler(boolean readonly)
	{
		myReadonly = readonly;
	}

	@RequiredDispatchThread
	@Override
	public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file)
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

		ChooseElementsDialog<DotNetFieldDeclaration> dialog = new ChooseElementsDialog<DotNetFieldDeclaration>(project, fields,
				"Choose field(s)", "Choose field(s) for property generate", true)
		{
			@Override
			protected String getItemText(DotNetFieldDeclaration item)
			{
				return item.getName();
			}

			@Nullable
			@Override
			protected Icon getItemIcon(DotNetFieldDeclaration item)
			{
				return IconDescriptorUpdaters.getIcon(item, Iconable.ICON_FLAG_VISIBILITY);
			}
		};

		List<DotNetFieldDeclaration> fieldDeclarations = dialog.showAndGetResult();
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

		ApplicationManager.getApplication().runWriteAction(new Runnable()
		{
			@Override
			public void run()
			{
				editor.getDocument().insertString(startOffset, allText);

				PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

				CodeStyleManager.getInstance(project).reformatText(file, startOffset, startOffset + allText.length());
			}
		});
	}

	@NotNull
	public static String getClearFieldName(@NotNull Project project, boolean isStatic, @NotNull String name)
	{
		CodeStyleSettingsManager settingsManager = CodeStyleSettingsManager.getInstance(project);

		CSharpCodeGenerationSettings customSettings = settingsManager.getCurrentSettings().getCustomSettings(CSharpCodeGenerationSettings.class);

		String prefix = isStatic ? customSettings.STATIC_FIELD_PREFIX : customSettings.FIELD_PREFIX;
		String suffix = isStatic? customSettings.STATIC_FIELD_SUFFIX : customSettings.FIELD_SUFFIX;

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

	public static String getPropertyName(@NotNull Project project, boolean isStatic, @NotNull String fieldName)
	{
		CodeStyleSettingsManager settingsManager = CodeStyleSettingsManager.getInstance(project);

		CSharpCodeGenerationSettings customSettings = settingsManager.getCurrentSettings().getCustomSettings(CSharpCodeGenerationSettings.class);

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
