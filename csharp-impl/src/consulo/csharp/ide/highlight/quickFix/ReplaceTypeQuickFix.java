package consulo.csharp.ide.highlight.quickFix;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class ReplaceTypeQuickFix extends BaseIntentionAction
{
	private final SmartPsiElementPointer<DotNetType> myPointer;
	private final String myTypeText;

	public ReplaceTypeQuickFix(@NotNull DotNetType type, @NotNull DotNetTypeRef typeRef)
	{
		myPointer = SmartPointerManager.getInstance(type.getProject()).createSmartPsiElementPointer(type);

		myTypeText = CSharpTypeRefPresentationUtil.buildShortText(typeRef, type);
		setText("Replace '" + type.getText() + "' by '" + myTypeText + "'");
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
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		DotNetType element = myPointer.getElement();
		if(element == null)
		{
			return;
		}

		CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(project, myTypeText, element);

		DotNetType newType = PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
		if(newType == null)
		{
			return;
		}

		element.replace(newType);
	}
}
