package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocRoot;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocText;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerCompositeValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerSingleValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIfStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.codeInsight.CommentUtilCore;
import lombok.val;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpIndentProcessor implements CSharpTokens, CSharpElements
{
	private final ASTNode myNode;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	public CSharpIndentProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings, CSharpCodeStyleSettings customSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
	}

	public Indent getIndent()
	{
		PsiElement psi = myNode.getPsi();
		PsiElement parent = psi.getParent();
		if(parent instanceof PsiFile)
		{
			return Indent.getNoneIndent();
		}

		val elementType = myNode.getElementType();
		if(elementType == NAMESPACE_DECLARATION ||
				elementType == TYPE_DECLARATION ||
				elementType == METHOD_DECLARATION ||
				elementType == CONVERSION_METHOD_DECLARATION ||
				elementType == FIELD_DECLARATION ||
				elementType == FIELD_OR_PROPERTY_SET ||
				elementType == ARRAY_METHOD_DECLARATION ||
				elementType == PROPERTY_DECLARATION ||
				elementType == XXX_ACCESSOR ||
				elementType == EVENT_DECLARATION ||
				elementType == ENUM_CONSTANT_DECLARATION ||
				elementType == USING_LIST ||
				elementType == CONSTRUCTOR_DECLARATION)
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == CSharpDocTokenType.DOC_LINE_START)
		{
			if(parent != null && parent.getParent() instanceof PsiFile)
			{
				return Indent.getNoneIndent();
			}
			if(parent instanceof CSharpDocText)
			{
				return Indent.getNoneIndent();
			}
			return Indent.getNormalIndent();
		}
		else if(psi instanceof CSharpDocText)
		{
			CSharpDocRoot docRoot = PsiTreeUtil.getParentOfType(psi, CSharpDocRoot.class);
			if(docRoot != null && docRoot.getParent() instanceof CSharpFile)
			{
				return Indent.getNoneIndent();
			}
			return Indent.getNormalIndent();
		}
		else if(parent instanceof CSharpArrayInitializerSingleValueImpl)
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == LBRACE || elementType == RBRACE)
		{
			if(parent instanceof CSharpArrayInitializerCompositeValueImpl)
			{
				return Indent.getNormalIndent();
			}
			return Indent.getNoneIndent();
		}
		else if(CommentUtilCore.isComment(myNode))
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == CSharpElements.MODIFIER_LIST || elementType == CSharpStubElements.MODIFIER_LIST)
		{
			return Indent.getNoneIndent();
		}
	/*	else if(elementType == CSharpParserDefinition.FILE_ELEMENT_TYPE)
		{
			return Indent.getNoneIndent();
		}  */
		else if(elementType == CSharpStubElements.FILE)
		{
			return Indent.getNoneIndent();
		}
		/*else if(elementType == MACRO_BLOCK_START || elementType == MACRO_BLOCK_STOP)
		{
			PsiElement psi = getNode().getPsi();
			if(psi.getParent() instanceof CSharpMacroBlockImpl)
			{
				return Indent.getNoneIndent();
			}
			return Indent.getNormalIndent();
		} */
		else
		{
			if(psi instanceof CSharpBlockStatementImpl)
			{
				return Indent.getNoneIndent();
			}

			if(psi instanceof DotNetStatement && parent instanceof CSharpIfStatementImpl)
			{
				return Indent.getNormalIndent();
			}

			if(parent instanceof CSharpStatementAsStatementOwner)
			{
				DotNetStatement childStatement = ((CSharpStatementAsStatementOwner) parent).getChildStatement();
				if(childStatement == psi)
				{
					return Indent.getNormalIndent();
				}
			}

			if(parent instanceof CSharpBlockStatementImpl)
			{
				return Indent.getNormalIndent();
			}

			return Indent.getNoneIndent();
		}
	}

	public Indent getChildIndent()
	{
		val elementType = myNode.getElementType();
		if(elementType == CSharpStubElements.FILE)
		{
			return Indent.getNoneIndent();
		}
		return Indent.getNormalIndent();
	}
}
