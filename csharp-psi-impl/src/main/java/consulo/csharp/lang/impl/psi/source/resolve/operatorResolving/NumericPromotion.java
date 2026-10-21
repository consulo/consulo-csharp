/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.operatorResolving;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Numeric promotion of the predefined operators (C# spec, 12.4.7): the operand types an operator of primitive numeric
 * operands is looked up with. It does not depend on how the base class library declares operators - net7+ declares
 * them through the generic math interfaces, earlier ones not at all.
 *
 * @author VISTALL
 */
public final class NumericPromotion
{
	private static final TokenSet ourBinaryOperators = TokenSet.create(CSharpTokens.PLUS, CSharpTokens.MINUS, CSharpTokens.MUL, CSharpTokens.DIV,
			CSharpTokens.PERC, CSharpTokens.AND, CSharpTokens.OR, CSharpTokens.XOR, CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.LTEQ,
			CSharpTokens.GTEQ, CSharpTokens.EQEQ, CSharpTokens.NTEQ);

	private static final TokenSet ourBitwiseOperators = TokenSet.create(CSharpTokens.AND, CSharpTokens.OR, CSharpTokens.XOR);

	private static final TokenSet ourEqualityOperators = TokenSet.create(CSharpTokens.EQEQ, CSharpTokens.NTEQ);

	private static final TokenSet ourShiftOperators = TokenSet.create(CSharpTokens.LTLT, CSharpTokens.GTGT);

	private static final TokenSet ourUnaryOperators = TokenSet.create(CSharpTokens.PLUS, CSharpTokens.MINUS, CSharpTokens.TILDE);

	private static final Set<String> ourPromotedToInt = Set.of(DotNetTypes.System.SByte, DotNetTypes.System.Byte, DotNetTypes.System.Int16,
			DotNetTypes.System.UInt16, DotNetTypes.System.Char);

	private static final Set<String> ourSigned = Set.of(DotNetTypes.System.SByte, DotNetTypes.System.Int16, DotNetTypes.System.Int32,
			DotNetTypes.System.Int64);

	private static final Set<String> ourNumeric = Set.of(DotNetTypes.System.SByte, DotNetTypes.System.Byte, DotNetTypes.System.Int16,
			DotNetTypes.System.UInt16, DotNetTypes.System.Char, DotNetTypes.System.Int32, DotNetTypes.System.UInt32, DotNetTypes.System.Int64,
			DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal);

	private NumericPromotion()
	{
	}

	/**
	 * @return the vm qualified names of the operator parameters; empty if the operands are numeric but C# has no such
	 * operator for them (long + ulong, -ulong, decimal + double...); null if the operator is not a predefined numeric one
	 * for these operands
	 */
	@Nullable
	@RequiredReadAction
	public static String[] promote(IElementType operator, DotNetTypeRef[] operandTypeRefs)
	{
		String[] operands = new String[operandTypeRefs.length];
		for(int i = 0; i < operandTypeRefs.length; i++)
		{
			operands[i] = numericVmQName(operandTypeRefs[i]);
			if(operands[i] == null)
			{
				return null;
			}
		}

		if(operands.length == 1 && ourUnaryOperators.contains(operator))
		{
			String type = promoteUnary(operator, operands[0]);
			return type == null ? ArrayUtil.EMPTY_STRING_ARRAY : new String[]{type};
		}

		if(operands.length == 2 && ourShiftOperators.contains(operator))
		{
			String left = promoteUnary(CSharpTokens.PLUS, operands[0]);
			String count = promoteUnary(CSharpTokens.PLUS, operands[1]);
			// no shift of floating point types, and the shift count must convert to int implicitly
			boolean valid = left != null && isIntegral(left) && DotNetTypes.System.Int32.equals(count);
			return valid ? new String[]{left, DotNetTypes.System.Int32} : ArrayUtil.EMPTY_STRING_ARRAY;
		}

		if(operands.length == 2 && ourBinaryOperators.contains(operator))
		{
			String type = promoteBinary(operands[0], operands[1]);
			if(type == null)
			{
				// no numeric == between decimal and a floating point type leaves the reference == (an error later, boxing), and
				// ulong with a signed operand is ambiguous between several numeric ones
				boolean noNumericCandidate = operands[0].equals(DotNetTypes.System.Decimal) || operands[1].equals(DotNetTypes.System.Decimal);
				return noNumericCandidate && ourEqualityOperators.contains(operator) ? null : ArrayUtil.EMPTY_STRING_ARRAY;
			}
			// no bitwise operators of floating point types
			if(ourBitwiseOperators.contains(operator) && !isIntegral(type))
			{
				return ArrayUtil.EMPTY_STRING_ARRAY;
			}
			return new String[]{type, type};
		}
		return null;
	}

	@Nullable
	private static String promoteUnary(IElementType operator, String operand)
	{
		String type = ourPromotedToInt.contains(operand) ? DotNetTypes.System.Int32 : operand;
		if(operator == CSharpTokens.MINUS)
		{
			// -uint is long, there is no -ulong
			if(type.equals(DotNetTypes.System.UInt32))
			{
				return DotNetTypes.System.Int64;
			}
			if(type.equals(DotNetTypes.System.UInt64))
			{
				return null;
			}
		}
		// no ~ of floating point types
		if(operator == CSharpTokens.TILDE && !isIntegral(type))
		{
			return null;
		}
		return type;
	}

	@Nullable
	private static String promoteBinary(String left, String right)
	{
		if(left.equals(DotNetTypes.System.Decimal) || right.equals(DotNetTypes.System.Decimal))
		{
			// no implicit conversion between decimal and floating point types
			return isFloatingPoint(left) || isFloatingPoint(right) ? null : DotNetTypes.System.Decimal;
		}
		if(left.equals(DotNetTypes.System.Double) || right.equals(DotNetTypes.System.Double))
		{
			return DotNetTypes.System.Double;
		}
		if(left.equals(DotNetTypes.System.Single) || right.equals(DotNetTypes.System.Single))
		{
			return DotNetTypes.System.Single;
		}
		if(left.equals(DotNetTypes.System.UInt64) || right.equals(DotNetTypes.System.UInt64))
		{
			// ulong with a signed operand has no common type
			return ourSigned.contains(left) || ourSigned.contains(right) ? null : DotNetTypes.System.UInt64;
		}
		if(left.equals(DotNetTypes.System.Int64) || right.equals(DotNetTypes.System.Int64))
		{
			return DotNetTypes.System.Int64;
		}
		if(left.equals(DotNetTypes.System.UInt32) || right.equals(DotNetTypes.System.UInt32))
		{
			return ourSigned.contains(left) || ourSigned.contains(right) ? DotNetTypes.System.Int64 : DotNetTypes.System.UInt32;
		}
		return DotNetTypes.System.Int32;
	}

	private static boolean isFloatingPoint(String type)
	{
		return type.equals(DotNetTypes.System.Single) || type.equals(DotNetTypes.System.Double);
	}

	private static boolean isIntegral(String type)
	{
		return !isFloatingPoint(type) && !type.equals(DotNetTypes.System.Decimal);
	}

	@Nullable
	@RequiredReadAction
	private static String numericVmQName(DotNetTypeRef typeRef)
	{
		PsiElement element = typeRef.resolve().getElement();
		if(element instanceof DotNetTypeDeclaration typeDeclaration && !typeDeclaration.isEnum())
		{
			String vmQName = typeDeclaration.getVmQName();
			return vmQName != null && ourNumeric.contains(vmQName) ? vmQName : null;
		}
		return null;
	}
}
