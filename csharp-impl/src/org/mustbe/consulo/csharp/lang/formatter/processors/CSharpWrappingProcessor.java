package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerCompositeValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerValue;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpImplicitArrayInitializationExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
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
	private final CSharpCodeStyleSettings myCustomSettings;

	public CSharpWrappingProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings, CSharpCodeStyleSettings customSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
		myCustomSettings = customSettings;
	}

	@Nullable
	public Wrap getWrap()
	{
		IElementType elementType = myNode.getElementType();

		PsiElement psi = myNode.getPsi();
		PsiElement parentPsi = psi.getParent();

		if(elementType == CSharpTokens.LBRACE)
		{
			int braceStyle = myCodeStyleSettings.BRACE_STYLE;
			if(parentPsi instanceof CSharpTypeDeclaration)
			{
				braceStyle = myCodeStyleSettings.CLASS_BRACE_STYLE;
			}
			else if(parentPsi instanceof CSharpNamespaceDeclaration)
			{
				braceStyle = myCustomSettings.NAMESPACE_BRACE_STYLE;
			}
			else if(parentPsi instanceof CSharpPropertyDeclaration)
			{
				braceStyle = myCustomSettings.PROPERTY_BRACE_STYLE;
			}
			else if(parentPsi instanceof CSharpEventDeclaration)
			{
				braceStyle = myCustomSettings.EVENT_BRACE_STYLE;
			}
			else if(parentPsi instanceof CSharpArrayMethodDeclaration)
			{
				braceStyle = myCustomSettings.INDEX_METHOD_BRACE_STYLE;
			}
			else if(parentPsi instanceof CSharpBlockStatementImpl && parentPsi.getParent() instanceof DotNetLikeMethodDeclaration)
			{
				braceStyle = myCodeStyleSettings.METHOD_BRACE_STYLE;
			}
			else if(psi instanceof CSharpImplicitArrayInitializationExpressionImpl)
			{
				braceStyle = myCodeStyleSettings.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE ? CommonCodeStyleSettings.NEXT_LINE : CommonCodeStyleSettings
						.END_OF_LINE;
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

		if(elementType == CSharpTokens.RBRACE)
		{
			if(parentPsi instanceof CSharpImplicitArrayInitializationExpressionImpl || parentPsi instanceof CSharpArrayInitializerCompositeValueImpl)
			{
				return Wrap.createWrap(WrapType.NONE, true);
			}

			if(CSharpFormattingUtil.isAutoAccessorOwner(parentPsi))
			{
				if(myCustomSettings.KEEP_AUTO_PROPERTY_IN_ONE_LINE)
				{
					return Wrap.createWrap(WrapType.NONE, true);
				}
			}
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(psi instanceof CSharpArrayInitializerValue)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(elementType == CSharpElements.XXX_ACCESSOR)
		{
			if(CSharpFormattingUtil.isAutoAccessorOwner(parentPsi) && myCustomSettings.KEEP_AUTO_PROPERTY_IN_ONE_LINE)
			{
				return Wrap.createWrap(WrapType.NONE, true);
			}
			else
			{
				return Wrap.createWrap(WrapType.ALWAYS, true);
			}
		}

		if(elementType == CSharpElements.ENUM_CONSTANT_DECLARATION)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(psi instanceof CSharpFieldOrPropertySet && !(parentPsi instanceof CSharpCallArgumentList))
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}

		if(psi instanceof DotNetStatement && parentPsi instanceof CSharpBlockStatementImpl && ((CSharpBlockStatementImpl) parentPsi).getStatements()
				[0] == psi)
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
