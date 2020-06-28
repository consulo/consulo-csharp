package consulo.csharp.ide.structureView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.FileStructureNodeProvider;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.PropertyOwner;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import consulo.csharp.lang.psi.impl.source.CSharpAnonymousMethodExpression;
import consulo.dotnet.psi.DotNetQualifiedElement;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-06-28
 */
public class CSharpLambdaNodeProvider implements FileStructureNodeProvider<CSharpLambdaTreeElement>, PropertyOwner, DumbAware
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
	public String getPropertyName()
	{
		return CSHARP_LAMBDA_PROPERTY_NAME;
	}
}
