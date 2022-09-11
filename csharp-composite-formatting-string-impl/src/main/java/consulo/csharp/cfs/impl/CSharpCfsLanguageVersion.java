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

package consulo.csharp.cfs.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.cfs.impl.lexer.CSharpInterpolationStringLexer;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpInjectExpressionElementType;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.cfs.lang.BaseExpressionCfsLanguageVersion;
import consulo.dotnet.cfs.lang.CfsLanguage;
import consulo.dotnet.cfs.lang.CfsTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.Lexer;
import consulo.language.lexer.MergingLexerAdapter;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
@ExtensionImpl
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
		return new MergingLexerAdapter(new CSharpInterpolationStringLexer(myExpressionElementType), TokenSet.create(myExpressionElementType, CfsTokens.TEXT, CfsTokens.FORMAT));
	}
}
