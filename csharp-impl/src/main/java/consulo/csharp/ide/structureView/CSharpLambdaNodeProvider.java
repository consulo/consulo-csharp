package consulo.csharp.ide.structureView;

import consulo.application.AllIcons;
import consulo.fileEditor.structureView.tree.ActionPresentation;
import consulo.fileEditor.structureView.tree.FileStructureNodeProvider;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.fileEditor.structureView.tree.ActionPresentationData;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.SyntaxTraverser;
import consulo.ui.ex.action.KeyboardShortcut;
import consulo.ui.ex.action.Shortcut;
import consulo.application.dumb.DumbAware;
import consulo.application.util.SystemInfo;
import consulo.language.psi.PsiElement;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.dotnet.psi.DotNetQualifiedElement;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-06-28
 */
public class CSharpLambdaNodeProvider implements FileStructureNodeProvider<CSharpLambdaTreeElement>, DumbAware
{
	public static final CSharpLambdaNodeProvider INSTANCE = new CSharpLambdaNodeProvider();

	public static final String ID = "SHOW_LAMBDA";
	public static final String CSHARP_LAMBDA_PROPERTY_NAME = "csharp.lambda.provider";

	@Override
	public String getCheckBoxText()
	{
		return "Lambdas & Delegates";
	}

	@Override
	public Shortcut[] getShortcut()
	{
		return new Shortcut[]{KeyboardShortcut.fromString(SystemInfo.isMac ? "meta L" : "control L")};
	}

	@Override
	public Collection<CSharpLambdaTreeElement> provideNodes(TreeElement node)
	{
		if(!(node instanceof PsiTreeElementBase))
		{
			return Collections.emptyList();
		}
		PsiElement element = ((PsiTreeElementBase) node).getElement();
		return SyntaxTraverser.psiTraverser(element)
				.expand(o -> o == element || !(o instanceof DotNetQualifiedElement || o instanceof CSharpAnonymousMethodExpression))
				.filter(CSharpAnonymousMethodExpression.class)
				.filter(o -> o != element)
				.map(CSharpLambdaTreeElement::new)
				.toList();
	}

	@Nonnull
	@Override
	public ActionPresentation getPresentation()
	{
		return new ActionPresentationData(getCheckBoxText(), null, AllIcons.Nodes.Lambda);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return ID;
	}

	@Nonnull
	@Override
	public String getSerializePropertyName()
	{
		return CSHARP_LAMBDA_PROPERTY_NAME;
	}
}
