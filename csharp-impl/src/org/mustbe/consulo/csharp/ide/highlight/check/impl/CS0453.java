package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNullableTypeImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CS0453 extends CompilerCheck<CSharpNullableTypeImpl>
{
	public static class DeleteQuestMarkQuickFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<CSharpNullableTypeImpl> myPointer;
		private String myText;

		public DeleteQuestMarkQuickFix(CSharpNullableTypeImpl nullableType, String text)
		{
			myText = text;
			myPointer = SmartPointerManager.getInstance(nullableType.getProject()).createSmartPsiElementPointer(nullableType);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Remove '" + myText + "'";
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public boolean startInWriteAction()
		{
			return true;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpNullableTypeImpl element = myPointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetType innerType = element.getInnerType();
			if(innerType == null)
			{
				return;
			}

			DotNetType type = CSharpFileFactory.createType(project, innerType.getText());
			element.replace(type);
		}
	}

	@Nullable
	@Override
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpNullableTypeImpl element)
	{
		DotNetType innerType = element.getInnerType();
		if(innerType == null)
		{
			return null;
		}
		DotNetTypeRef dotNetTypeRef = innerType.toTypeRef();
		if(!dotNetTypeRef.isNullable())
		{
			return null;
		}
		PsiElement questElement = element.getQuestElement();
		return result(questElement, dotNetTypeRef.getQualifiedText()).addQuickFix(new DeleteQuestMarkQuickFix(element, "?"));
	}
}
