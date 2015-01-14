package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
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

	public CSharpWrappingProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings, CSharpCodeStyleSettings customSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
	}

	@Nullable
	public Wrap getWrap()
	{
		IElementType elementType = myNode.getElementType();

		if(elementType == CSharpTokens.LBRACE)
		{
			ASTNode treeParent = myNode.getTreeParent();

			PsiElement psi = treeParent.getPsi();

			int braceStyle = myCodeStyleSettings.BRACE_STYLE;
			if(psi instanceof CSharpTypeDeclaration)
			{
				braceStyle = myCodeStyleSettings.CLASS_BRACE_STYLE;
			}
			else if(psi instanceof CSharpBlockStatementImpl && psi.getParent() instanceof CSharpMethodDeclaration)
			{
				braceStyle = myCodeStyleSettings.METHOD_BRACE_STYLE;
			}

			switch(braceStyle)
			{
				case CommonCodeStyleSettings.NEXT_LINE:
					return Wrap.createWrap(WrapType.ALWAYS, true);
				case CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED:
					return Wrap.createWrap(WrapType.NORMAL, true);
				default:
					return Wrap.createWrap(WrapType.NONE, true);
			}
		}

		if(elementType == CSharpTokens.RBRACE || elementType == CSharpElements.XXX_ACCESSOR || elementType == CSharpElements
				.ENUM_CONSTANT_DECLARATION)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		PsiElement psi = myNode.getPsi();
		PsiElement parent = psi.getParent();
		if(psi instanceof CSharpFieldOrPropertySet && !(parent instanceof CSharpCallArgumentList))
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(psi instanceof DotNetStatement && parent instanceof CSharpBlockStatementImpl && ((CSharpBlockStatementImpl) parent).getStatements()[0] ==
				psi)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(elementType == CSharpTokens.ELSE_KEYWORD)
		{
			if(myCodeStyleSettings.ELSE_ON_NEW_LINE)
			{
				return Wrap.createWrap(WrapType.ALWAYS, true);
			}
		}
		return null;
	}
}
