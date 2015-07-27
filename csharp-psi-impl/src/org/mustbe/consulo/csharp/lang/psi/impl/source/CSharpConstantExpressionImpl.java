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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.math.BigInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpStringLiteralEscaper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpConstantTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetConstantExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpConstantExpressionImpl extends CSharpElementImpl implements DotNetConstantExpression, PsiLanguageInjectionHost
{
	private static class OurTypeRefResolver extends CSharpResolveCache.TypeRefResolver<CSharpConstantExpressionImpl>
	{
		public static final OurTypeRefResolver INSTANCE = new OurTypeRefResolver();

		@NotNull
		@Override
		@RequiredReadAction
		public DotNetTypeRef resolveTypeRef(@NotNull CSharpConstantExpressionImpl element, boolean resolveFromParent)
		{
			IElementType elementType = element.getLiteralType();

			PsiElement parent = element.getParent();
			if(parent instanceof DotNetVariable)
			{
				DotNetTypeRef typeRef = ((DotNetVariable) parent).toTypeRef(false);
				if(typeRef == DotNetTypeRef.AUTO_TYPE)
				{
					if(elementType == CSharpTokens.INTEGER_LITERAL)
					{
						CSharpTypeRefByQName another = new CSharpTypeRefByQName(DotNetTypes.System.Int32);
						if(CSharpConstantTypeRef.testNumberConstant(element, "", another, element) != null)
						{
							return another;
						}
						another = new CSharpTypeRefByQName(DotNetTypes.System.Int64);
						if(CSharpConstantTypeRef.testNumberConstant(element, "", another, element) != null)
						{
							return another;
						}
						return DotNetTypeRef.ERROR_TYPE;
					}
				}
			}

			if(elementType == CSharpTokens.INTEGER_LITERAL)
			{
				return new CSharpConstantTypeRef(element, new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Int32));
			}
			else if(elementType == CSharpTokens.DOUBLE_LITERAL)
			{
				String text = element.getText();
				// explicit type
				if(text.endsWith("d") || text.endsWith("D"))
				{
					return new CSharpConstantTypeRef(element, new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Double));
				}

				return new CSharpConstantTypeRef(element, new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Double));
			}

			if(elementType == CSharpTokens.STRING_LITERAL || elementType == CSharpTokens.VERBATIM_STRING_LITERAL ||
					elementType == CSharpTokensImpl.INTERPOLATION_STRING_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.String);
			}
			else if(elementType == CSharpTokens.CHARACTER_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Char);
			}
			else if(elementType == CSharpTokens.UINTEGER_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.UInt32);
			}
			else if(elementType == CSharpTokens.ULONG_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.UInt64);
			}
			else if(elementType == CSharpTokens.LONG_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Int64);
			}
			else if(elementType == CSharpTokens.FLOAT_LITERAL)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Single);
			}
			else if(elementType == CSharpTokens.NULL_LITERAL)
			{
				return CSharpNullTypeRef.INSTANCE;
			}
			else if(elementType == CSharpTokens.TRUE_KEYWORD || elementType == CSharpTokens.FALSE_KEYWORD)
			{
				return new CSharpLazyTypeRefByQName(element, DotNetTypes.System.Boolean);
			}
			else
			{
				throw new UnsupportedOperationException(element.toString());
			}
		}
	}

	public CSharpConstantExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitConstantExpression(this);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return CSharpResolveCache.getInstance(getProject()).resolveTypeRef(this, OurTypeRefResolver.INSTANCE, resolveFromParent);
	}

	@Nullable
	@RequiredReadAction
	public Object getValue(@NotNull String prefix)
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

	@NotNull
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
		return elementType == CSharpTokens.STRING_LITERAL || elementType == CSharpTokens.VERBATIM_STRING_LITERAL;
	}

	@Override
	@RequiredReadAction
	public PsiLanguageInjectionHost updateText(@NotNull String s)
	{
		LeafPsiElement first = (LeafPsiElement) getFirstChild();
		first.replaceWithText(s);
		return this;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper()
	{
		IElementType elementType = getLiteralType();
		if(elementType == CSharpTokens.STRING_LITERAL)
		{
			return new CSharpStringLiteralEscaper<CSharpConstantExpressionImpl>(this);
		}
		else if(elementType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			return LiteralTextEscaper.createSimple(this);
		}
		throw new IllegalArgumentException("Unknown " + elementType);
	}
}
