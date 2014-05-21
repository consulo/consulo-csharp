/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.formatter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.codeInsight.CommentUtilCore;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFormattingBlock extends TemplateLanguageBlock implements CSharpElements, CSharpTokens, CSharpTokenSets
{
	public CSharpFormattingBlock(
			@NotNull CSharpFormattingModelBuilder blockFactory,
			@NotNull CodeStyleSettings settings,
			@NotNull ASTNode node,
			@Nullable List<DataLanguageBlockWrapper> foreignChildren)
	{
		super(blockFactory, settings, node, foreignChildren);
	}

	@Nullable
	@Override
	public Wrap getWrap()
	{
		IElementType elementType = getNode().getElementType();
		if(elementType == FIELD_OR_PROPERTY_SET)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}
		else if(elementType == XXX_ACCESSOR)
		{
			return Wrap.createWrap(WrapType.ALWAYS, true);
		}
		return super.getWrap();
	}

	@Override
	public Indent getIndent()
	{
		PsiElement psiElement = getNode().getPsi().getParent();
		if(psiElement instanceof PsiFile)
		{
			return Indent.getNoneIndent();
		}

		val elementType = getNode().getElementType();
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
				elementType == USING_NAMESPACE_LIST ||
				elementType == CONSTRUCTOR_DECLARATION)
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == LBRACE || elementType == RBRACE)
		{
			return Indent.getNoneIndent();
		}
		else if(CommentUtilCore.isComment(getNode()))
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == MODIFIER_LIST)
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
			PsiElement psi = getNode().getPsi();
			PsiElement parent = psi.getParent();
			if(parent instanceof CSharpBlockStatementImpl)
			{
				return Indent.getNormalIndent();
			}
			return Indent.getNoneIndent();
		}
	}

	@Nullable
	@Override
	protected Indent getChildIndent()
	{
		val elementType = getNode().getElementType();
		if(elementType == CSharpStubElements.FILE)
		{
			return Indent.getNoneIndent();
		}
		return Indent.getNormalIndent();
	}

	@Override
	protected IElementType getTemplateTextElementType()
	{
		return CSharpTemplateTokens.MACRO_FRAGMENT;
	}
}
