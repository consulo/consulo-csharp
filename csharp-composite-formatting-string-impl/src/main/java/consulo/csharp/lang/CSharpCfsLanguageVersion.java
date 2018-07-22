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

package consulo.csharp.lang;

import javax.annotation.Nonnull;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.csharp.cfs.lang.BaseExpressionCfsLanguageVersion;
import consulo.csharp.cfs.lang.CfsLanguage;
import consulo.csharp.lang.lexer.CSharpInterpolationStringLexer;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.source.injection.CSharpInjectExpressionElementType;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
public class CSharpCfsLanguageVersion extends BaseExpressionCfsLanguageVersion
{
	@Nonnull
	public static CSharpCfsLanguageVersion getInstance()
	{
		return CfsLanguage.INSTANCE.findVersionByClass(CSharpCfsLanguageVersion.class);
	}

	public CSharpCfsLanguageVersion()
	{
		super(CSharpLanguage.INSTANCE);
	}

	@Nonnull
	public IElementType getExpressionElementType()
	{
		if(myExpressionElementType == null)
		{
			myExpressionElementType = createExpressionElementType();
		}
		return myExpressionElementType;
	}

	@Override
	public IElementType createExpressionElementType()
	{
		return new CSharpInjectExpressionElementType("EXPRESSION", CfsLanguage.INSTANCE, CSharpReferenceExpression.ResolveToKind.ANY_MEMBER);
	}

	@Nonnull
	@Override
	public Lexer createInnerLexer()
	{
		if(myExpressionElementType == null)
		{
			myExpressionElementType = createExpressionElementType();
		}
		return new MergingLexerAdapter(new CSharpInterpolationStringLexer(myExpressionElementType), TokenSet.create(myExpressionElementType));
	}
}
