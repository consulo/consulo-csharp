package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class CSharpStubTypeUtil
{
	public static boolean shouldCreateStub(ASTNode node)
	{
		ASTNode treeParent = node.getTreeParent();
		while(treeParent != null)
		{
			PsiElement psi = treeParent.getPsi();
			if(psi instanceof DotNetExpression || psi instanceof DotNetStatement)
			{
				return false;
			}
			treeParent = treeParent.getTreeParent();
		}
		return true;
	}

}
