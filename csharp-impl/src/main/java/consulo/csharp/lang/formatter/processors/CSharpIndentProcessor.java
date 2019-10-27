/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.formatter.processors;

import com.intellij.formatting.Indent;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.codeInsight.CommentUtilCore;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.formatter.CSharpFormattingBlock;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerCompositeValueImpl;
import consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerSingleValueImpl;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpIfStatementImpl;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpIndentProcessor implements CSharpTokens, CSharpElements
{
	private final CSharpFormattingBlock myBlock;
	private final CommonCodeStyleSettings myCodeStyleSettings;
	private final CSharpCodeStyleSettings myCustomSettings;

	public CSharpIndentProcessor(CSharpFormattingBlock block, CommonCodeStyleSettings codeStyleSettings, CSharpCodeStyleSettings customSettings)
	{
		myBlock = block;
		myCodeStyleSettings = codeStyleSettings;
		myCustomSettings = customSettings;
	}

	@RequiredReadAction
	public Indent getIndent()
	{
		ASTNode node = myBlock.getNode();
		PsiElement psi = node.getPsi();
		PsiElement parent = psi.getParent();
		if(parent instanceof PsiFile)
		{
			return Indent.getNoneIndent();
		}

		final IElementType elementType = node.getElementType();

		if(elementType == NAMESPACE_DECLARATION ||
				elementType == TYPE_DECLARATION ||
				elementType == METHOD_DECLARATION ||
				elementType == CONVERSION_METHOD_DECLARATION ||
				elementType == FIELD_DECLARATION ||
				elementType == NAMED_FIELD_OR_PROPERTY_SET ||
				elementType == ARRAY_METHOD_DECLARATION ||
				elementType == PROPERTY_DECLARATION ||
				elementType == XXX_ACCESSOR ||
				elementType == EVENT_DECLARATION ||
				elementType == ENUM_CONSTANT_DECLARATION ||
				elementType == USING_TYPE_STATEMENT ||
				elementType == USING_NAMESPACE_STATEMENT ||
				elementType == TYPE_DEF_STATEMENT ||
				elementType == CONSTRUCTOR_DECLARATION)
		{
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
		else if(elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
		{
			return myCustomSettings.PREPROCESSOR_DIRECTIVES_AT_FIRST_COLUMN ? Indent.getAbsoluteNoneIndent() : Indent.getNormalIndent();
		}
		else if(elementType == DICTIONARY_INITIALIZER)
		{
			return Indent.getNormalIndent();
		}
		else if(CommentUtilCore.isComment(node))
		{
			if(elementType == CSharpTokens.LINE_COMMENT)
			{
				return /*myCodeStyleSettings.KEEP_FIRST_COLUMN_COMMENT ? Indent.getAbsoluteNoneIndent() :*/ Indent.getNormalIndent();
			}
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
			if(CSharpFormattingUtil.wantContinuationIndent(psi))
			{
				return Indent.getContinuationIndent();
			}

			if(psi instanceof CSharpBlockStatementImpl)
			{
				BlockWithParent parentBlock = myBlock.getParent();
				if(parentBlock != null &&
						(((CSharpFormattingBlock) parentBlock).getElementType() == CSharpElements.CASE_OR_DEFAULT_STATEMENT ||
						((CSharpFormattingBlock) parentBlock).getElementType() == CSharpElements.CASE_PATTERN_STATEMENT))
				{
					return Indent.getNoneIndent();
				}

				if(parent instanceof CSharpBlockStatementImpl)
				{
					return Indent.getNormalIndent();
				}
				return Indent.getNoneIndent();
			}

			if(psi instanceof DotNetStatement && parent instanceof CSharpIfStatementImpl)
			{
				if(psi instanceof CSharpIfStatementImpl)
				{
					return Indent.getNoneIndent();
				}
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

	@RequiredReadAction
	@Nonnull
	public Indent getChildIndent()
	{
		IElementType elementType = myBlock.getNode().getElementType();
		if(elementType == CSharpStubElements.FILE ||
				elementType == CSharpElements.MODIFIER_LIST ||
				elementType == CSharpElements.IF_STATEMENT ||
				elementType == CSharpElements.TRY_STATEMENT ||
				elementType == CSharpStubElements.MODIFIER_LIST)
		{
			return Indent.getNoneIndent();
		}

		return Indent.getNormalIndent();
	}
}
