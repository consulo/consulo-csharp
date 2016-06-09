package org.mustbe.consulo.csharp.ide.codeInspection.languageKeywordUsage;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.ide.completion.CSharpCompletionUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNativeTypeImplUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;

/**
 * @author VISTALL
 * @since 16.04.2016
 */
public class LanguageKeywordUsageInspection extends LocalInspectionTool
{
	public static class ReplaceByKeywordFix extends LocalQuickFixOnPsiElement
	{
		private IElementType myKeywordElementType;

		public ReplaceByKeywordFix(@NotNull PsiElement element, IElementType keywordElementType)
		{
			super(element);
			myKeywordElementType = keywordElementType;
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Replace by '" + CSharpCompletionUtil.textOfKeyword(myKeywordElementType) + "' keyword";
		}

		@Override
		public void invoke(@NotNull final Project project, @NotNull PsiFile file, @NotNull final PsiElement startElement, @NotNull PsiElement endElement)
		{
			new WriteCommandAction.Simple<Object>(project, file)
			{
				@Override
				@RequiredWriteAction
				protected void run() throws Throwable
				{
					CSharpReferenceExpression refExpression = (CSharpReferenceExpression) startElement.getParent();

					PsiElement parent = refExpression.getParent();
					String text = CSharpCompletionUtil.textOfKeyword(myKeywordElementType);

					if(parent instanceof CSharpUserType)
					{
						DotNetType stubType = CSharpFileFactory.createMaybeStubType(project, text, (DotNetType) parent);
						parent.replace(stubType);
					}
					else
					{
						// hack due simple keyword parsed not as expression
						CSharpReferenceExpression expression = (CSharpReferenceExpression) CSharpFileFactory.createExpression(project, text + ".call");

						DotNetExpression qualifier = expression.getQualifier();
						assert qualifier != null;

						refExpression.replace(qualifier);
					}
				}
			}.execute();
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	private static class Visitor extends CSharpElementVisitor
	{
		private ProblemsHolder myHolder;

		public Visitor(ProblemsHolder holder)
		{
			myHolder = holder;
		}

		@Override
		@RequiredReadAction
		public void visitReferenceExpression(CSharpReferenceExpression expression)
		{
			DotNetExpression qualifier = expression.getQualifier();
			CSharpReferenceExpression.ResolveToKind kind = expression.kind();
			if(qualifier != null || kind == CSharpReferenceExpression.ResolveToKind.BASE || kind == CSharpReferenceExpression.ResolveToKind.THIS)
			{
				return;
			}

			PsiElement referenceElement = expression.getReferenceElement();
			if(referenceElement == null)
			{
				return;
			}

			PsiElement resolved = expression.resolve();
			if(!(resolved instanceof CSharpTypeDeclaration))
			{
				return;
			}

			String vmQName = ((CSharpTypeDeclaration) resolved).getVmQName();
			assert vmQName != null;
			for(Map.Entry<IElementType, String> entry : CSharpNativeTypeImplUtil.ourElementToQTypes.entrySet())
			{
				if(vmQName.equals(entry.getValue()) && PsiUtilCore.getElementType(referenceElement) != entry.getKey())
				{
					myHolder.registerProblem(referenceElement, "Reference does not match the current code style, use '" + CSharpCompletionUtil.textOfKeyword(entry.getKey()) + "'", new ReplaceByKeywordFix(referenceElement, entry.getKey()));
					break;
				}
			}
		}
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		CSharpCodeGenerationSettings settings = CSharpCodeGenerationSettings.getInstance(holder.getProject());
		if(!settings.USE_LANGUAGE_DATA_TYPES)
		{
			return new PsiElementVisitor()
			{
			};
		}
		return new Visitor(holder);
	}
}
