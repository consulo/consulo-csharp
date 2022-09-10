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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.psi.ContributedReferenceHost;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceService;
import consulo.project.Project;
import consulo.document.util.TextRange;
import consulo.util.lang.StringUtil;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.injection.CSharpStringLiteralEscaper;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpConstantTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpFastImplicitTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetConstantExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpConstantExpressionImpl extends CSharpExpressionImpl implements DotNetConstantExpression, PsiLanguageInjectionHost, ContributedReferenceHost
{
	private static class FormattableStringTypeRef extends CSharpTypeRefByQName implements CSharpFastImplicitTypeRef
	{
		public FormattableStringTypeRef(@Nonnull Project project, @Nonnull GlobalSearchScope searchScope, @Nonnull String qualifiedName)
		{
			super(project, searchScope, qualifiedName);
		}

		@RequiredReadAction
		@Nullable
		@Override
		public DotNetTypeRef doMirror(@Nonnull DotNetTypeRef another)
		{
			if(DotNetTypeRefUtil.isVmQNameEqual(another, DotNetTypes2.System.FormattableString))
			{
				return another;
			}
			return null;
		}

		@Override
		public boolean isConversion()
		{
			return true;
		}
	}

	public CSharpConstantExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConstantExpression(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		IElementType elementType = getLiteralType();

		PsiElement parent = getParent();
		if(parent instanceof DotNetVariable)
		{
			DotNetTypeRef typeRef = ((DotNetVariable) parent).toTypeRef(false);
			if(typeRef == DotNetTypeRef.AUTO_TYPE)
			{
				if(elementType == CSharpTokens.INTEGER_LITERAL)
				{
					CSharpTypeRefByQName another = new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes.System.Int32);
					if(CSharpConstantTypeRef.testNumberConstant(this, "", another) != null)
					{
						return another;
					}
					another = new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes.System.Int64);
					if(CSharpConstantTypeRef.testNumberConstant(this, "", another) != null)
					{
						return another;
					}
					return DotNetTypeRef.ERROR_TYPE;
				}
			}
		}

		DotNetTypeRef defaultConstantTypeRef = getDefaultConstantTypeRef();
		if(defaultConstantTypeRef != null)
		{
			return defaultConstantTypeRef;
		}
		else
		{
			throw new UnsupportedOperationException(elementType.toString());
		}
	}

	@RequiredReadAction
	@Nullable
	public DotNetTypeRef getDefaultConstantTypeRef()
	{
		IElementType elementType = getLiteralType();
		if(elementType == CSharpTokens.INTEGER_LITERAL)
		{
			return new CSharpConstantTypeRef(this, new CSharpTypeRefByQName(this, DotNetTypes.System.Int32));
		}
		else if(elementType == CSharpTokens.DOUBLE_LITERAL)
		{
			String text = this.getText();
			// explicit type
			if(text.endsWith("d") || text.endsWith("D"))
			{
				return new CSharpConstantTypeRef(this, new CSharpTypeRefByQName(this, DotNetTypes.System.Double));
			}

			return new CSharpConstantTypeRef(this, new CSharpTypeRefByQName(this, DotNetTypes.System.Double));
		}
		else if(elementType == CSharpTokensImpl.INTERPOLATION_STRING_LITERAL)
		{
			return new FormattableStringTypeRef(getProject(), getResolveScope(), DotNetTypes.System.String);
		}
		else if(elementType == CSharpTokens.STRING_LITERAL || elementType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.String);
		}
		else if(elementType == CSharpTokens.CHARACTER_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Char);
		}
		else if(elementType == CSharpTokens.UINTEGER_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.UInt32);
		}
		else if(elementType == CSharpTokens.ULONG_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.UInt64);
		}
		else if(elementType == CSharpTokens.LONG_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Int64);
		}
		else if(elementType == CSharpTokens.FLOAT_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Single);
		}
		else if(elementType == CSharpTokens.DECIMAL_LITERAL)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Decimal);
		}
		else if(elementType == CSharpTokens.NULL_LITERAL)
		{
			return new CSharpNullTypeRef(getProject(), getResolveScope());
		}
		else if(elementType == CSharpTokens.TRUE_KEYWORD || elementType == CSharpTokens.FALSE_KEYWORD)
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Boolean);
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public Object getValue(@Nonnull String prefix)
	{
		PsiElement byType = getFirstChild();
		assert byType != null;
		IElementType elementType = getLiteralType();
		String text = prefix.isEmpty() ? getText() : prefix + getText();
		if(elementType == CSharpTokens.STRING_LITERAL)
		{
			return StringUtil.unquoteString(text);
		}
		else if(elementType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			return getText(); //TODO [VISTALL] unquote @ "" and escape \n \t
		}
		else if(elementType == CSharpTokens.CHARACTER_LITERAL)
		{
			return StringUtil.unquoteString(text).charAt(0);
		}
		else if(elementType == CSharpTokens.UINTEGER_LITERAL)
		{
			text = text.substring(0, text.length() - 1); //cut U
			return getBigInteger(text);
		}
		else if(elementType == CSharpTokens.ULONG_LITERAL)
		{
			text = text.substring(0, text.length() - 2); //cut UL
			return getBigInteger(text);
		}
		else if(elementType == CSharpTokens.INTEGER_LITERAL)
		{
			return getBigInteger(text);
		}
		else if(elementType == CSharpTokens.LONG_LITERAL)
		{
			text = text.substring(0, text.length() - 1); //cut L
			return getBigInteger(text);
		}
		else if(elementType == CSharpTokens.FLOAT_LITERAL)
		{
			return Double.parseDouble(text);
		}
		else if(elementType == CSharpTokens.DOUBLE_LITERAL)
		{
			return Double.parseDouble(text);
		}
		else if(elementType == CSharpTokens.NULL_LITERAL)
		{
			return null;
		}
		else if(elementType == CSharpTokens.TRUE_KEYWORD || elementType == CSharpTokens.FALSE_KEYWORD)
		{
			return Boolean.parseBoolean(text);
		}
		throw new IllegalArgumentException(elementType.toString());
	}

	private static BigInteger getBigInteger(String text)
	{
		int radix = 10;
		if(text.startsWith("0x") || text.startsWith("0X"))
		{
			radix = 16;
			text = text.substring(2, text.length());
		}
		return new BigInteger(text, radix);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Object getValue()
	{
		return getValue("");
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public IElementType getLiteralType()
	{
		PsiElement byType = getFirstChild();
		assert byType != null;
		return byType.getNode().getElementType();
	}

	@Override
	@RequiredReadAction
	public boolean isValidHost()
	{
		IElementType elementType = getLiteralType();
		return elementType != CSharpTokens.CHARACTER_LITERAL && CSharpTokenSets.STRINGS.contains(elementType);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiReference[] getReferences()
	{
		return PsiReferenceService.getService().getContributedReferences(this);
	}

	@Override
	@RequiredReadAction
	public PsiLanguageInjectionHost updateText(@Nonnull String s)
	{
		LeafPsiElement first = (LeafPsiElement) getFirstChild();
		first.replaceWithText(s);
		return this;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper()
	{
		IElementType elementType = getLiteralType();
		if(elementType == CSharpTokens.STRING_LITERAL)
		{
			return new CSharpStringLiteralEscaper<>(this);
		}
		else if(elementType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			return LiteralTextEscaper.createSimple(this);
		}
		throw new IllegalArgumentException("Unknown " + elementType);
	}

	@RequiredReadAction
	@Nonnull
	public static TextRange getStringValueTextRange(@Nonnull CSharpConstantExpressionImpl expression)
	{
		IElementType literalType = expression.getLiteralType();
		if(literalType == CSharpTokens.VERBATIM_STRING_LITERAL || literalType == CSharpTokenSets.INTERPOLATION_STRING_LITERAL)
		{
			return new TextRange(2, expression.getTextLength() - 1);
		}
		else if(literalType == CSharpTokens.STRING_LITERAL)
		{
			return new TextRange(1, expression.getTextLength() - 1);
		}
		return new TextRange(0, expression.getTextLength());
	}
}
