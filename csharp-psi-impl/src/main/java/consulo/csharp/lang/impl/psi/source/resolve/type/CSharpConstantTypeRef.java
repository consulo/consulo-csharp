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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.component.ProcessCanceledException;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpConstantBaseTypeRef;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import org.joou.UByte;
import org.joou.UInteger;
import org.joou.ULong;
import org.joou.UShort;

import jakarta.annotation.Nonnull;

import java.math.BigInteger;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CSharpConstantTypeRef extends CSharpConstantBaseTypeRef
{
	public CSharpConstantTypeRef(CSharpConstantExpressionImpl element, @Nonnull DotNetTypeRef defaultTypeRef)
	{
		super(element, defaultTypeRef);
	}

	@RequiredReadAction
	public static boolean isNumberLiteral(CSharpConstantExpressionImpl expression)
	{
		IElementType literalType = expression.getLiteralType();
		return literalType == CSharpTokenSets.INTEGER_LITERAL ||
				literalType == CSharpTokenSets.UINTEGER_LITERAL ||
				literalType == CSharpTokenSets.ULONG_LITERAL ||
				literalType == CSharpTokenSets.FLOAT_LITERAL ||
				literalType == CSharpTokenSets.DOUBLE_LITERAL ||
				literalType == CSharpTokenSets.LONG_LITERAL;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeRef testNumberConstant(@Nonnull CSharpConstantExpressionImpl expression,
												   @Nonnull String prefix,
												   @Nonnull DotNetTypeRef another)
	{
		if(isNumberLiteral(expression))
		{
			PsiElement element = another.resolve().getElement();
			String qName = element instanceof CSharpTypeDeclaration ? ((CSharpTypeDeclaration) element).getVmQName() : null;
			if(qName == null)
			{
				return null;
			}

			Object value;
			try
			{
				value = expression.getValue(prefix);
			}
			catch(ProcessCanceledException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				return null;
			}

			DotNetTypeRef anotherRef = testNumber(value, qName, another, expression.getProject(), expression.getResolveScope());
			if(anotherRef != null)
			{
				return anotherRef;
			}
			else
			{
				return null;
			}
		}

		return null;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeRef testNumber(@Nullable Object value, @Nonnull String qName, @Nonnull DotNetTypeRef another, @Nonnull Project project, @Nonnull GlobalSearchScope resolveScope)
	{
		if(value instanceof Number)
		{
			Number numberValue = (Number) value;

			if(testInteger(DotNetTypes.System.Byte, qName, numberValue, UByte.MIN_VALUE, UByte.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.SByte, qName, numberValue, Byte.MIN_VALUE, Byte.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.Int16, qName, numberValue, Short.MIN_VALUE, Short.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.UInt16, qName, numberValue, UShort.MIN_VALUE, UShort.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.Int32, qName, numberValue, Integer.MIN_VALUE, Integer.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.UInt32, qName, numberValue, UInteger.MIN_VALUE, UInteger.MAX_VALUE))
			{
				return another;
			}

			if(testInteger(DotNetTypes.System.Int64, qName, numberValue, Long.MIN_VALUE, Long.MAX_VALUE))
			{
				return another;
			}

			if(testBigInteger(DotNetTypes.System.UInt64, qName, numberValue, ULong.MIN_VALUE, ULong.MAX_VALUE))
			{
				return another;
			}

			CSharpTypeRefByQName enumTypeRef = new CSharpTypeRefByQName(project, resolveScope, DotNetTypes.System.Enum);
			if(CSharpTypeUtil.isInheritable(enumTypeRef, another) && numberValue.longValue() == 0)
			{
				return another;
			}

			if(testDouble(DotNetTypes.System.Single, qName, numberValue, Float.MIN_VALUE, Float.MAX_VALUE))
			{
				if(numberValue instanceof Double)
				{
					double fraction = numberValue.doubleValue() % 1.0d;
					if(fraction == 0)
					{
						return another;
					}
				}
			}

			if(testDouble(DotNetTypes.System.Double, qName, numberValue, Double.MIN_VALUE, Double.MAX_VALUE))
			{
				return another;
			}
		}
		return null;
	}

	private static boolean testInteger(@Nonnull String leftTypeQName, String qName, @Nonnull Number value, long min, long max)
	{
		return testBigInteger(leftTypeQName, qName, value, BigInteger.valueOf(min), BigInteger.valueOf(max));
	}

	private static boolean testBigInteger(@Nonnull String leftTypeQName, String qName, @Nonnull Number value, BigInteger min, BigInteger max)
	{
		if(!(leftTypeQName.equals(qName)))
		{
			return false;
		}

		if(!(value instanceof BigInteger))
		{
			return false;
		}

		BigInteger bigInteger = (BigInteger) value;
		int toMax = bigInteger.compareTo(max);
		int toMin = bigInteger.compareTo(min);
		return toMax <= 0 && toMin >= 0;
	}

	private static boolean testDouble(@Nonnull String leftTypeQName, String qName, @Nonnull Number value, double min, double max)
	{
		if(!(leftTypeQName.equals(qName)))
		{
			return false;
		}
		double l = value.doubleValue();
		return l <= max && l >= min;
	}
}
