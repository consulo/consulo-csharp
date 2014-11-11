package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpWrappingProcessor
{
	private final ASTNode myNode;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	public CSharpWrappingProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
	}

	@Nullable
	public Wrap getWrap()
	{
		IElementType elementType = myNode.getElementType();

		if(elementType == CSharpTokens.LBRACE || elementType == CSharpTokens.RBRACE || elementType == CSharpElements.XXX_ACCESSOR)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		PsiElement psi = myNode.getPsi();
		PsiElement parent = psi.getParent();
		if(psi instanceof CSharpFieldOrPropertySet && !(parent instanceof CSharpCallArgumentList))
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(psi instanceof DotNetStatement && parent instanceof CSharpBlockStatementImpl && ((CSharpBlockStatementImpl) parent).getStatements()[0]
				== psi)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}
		return null;
	}
}
