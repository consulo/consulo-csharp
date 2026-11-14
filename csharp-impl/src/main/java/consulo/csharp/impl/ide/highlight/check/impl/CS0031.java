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
package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.Map;

/**
 * {@code byte b = 300;}, {@code enum E : byte { A = 300 }} - an integral constant out of the range of the integral type it converts to;
 * the targets of CS0029
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0031 extends CompilerCheck<PsiElement> {
    private static final Map<String, BigInteger[]> RANGES = Map.of(
        DotNetTypes.System.Byte, range(0, 255), DotNetTypes.System.SByte, range(-128, 127),
        DotNetTypes.System.Int16, range(Short.MIN_VALUE, Short.MAX_VALUE), DotNetTypes.System.UInt16, range(0, 65535),
        DotNetTypes.System.Int32, range(Integer.MIN_VALUE, Integer.MAX_VALUE), DotNetTypes.System.UInt32, range(0, 4294967295L),
        DotNetTypes.System.Int64, range(Long.MIN_VALUE, Long.MAX_VALUE),
        DotNetTypes.System.UInt64, new BigInteger[]{BigInteger.ZERO, BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)});

    private static BigInteger[] range(long min, long max) {
        return new BigInteger[]{BigInteger.valueOf(min), BigInteger.valueOf(max)};
    }

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, PsiElement element) {
        CS0029.CheckTarget target = CS0029.resolve(element);
        if (target == null || !(target.myTarget instanceof DotNetExpression expression)) {
            return null;
        }
        DotNetTypeRef expected = ContainerUtil.getFirstItem(target.myExpectedTypeRefs);
        if (expected == null || !isOutOfRange(expression, expected)) {
            return null;
        }
        return newBuilder(expression, expression.getText(), formatTypeRef(expected));
    }

    /**
     * @return true for a constant of an integral value out of the range of the integral type - of an enum, its underlying one
     */
    @RequiredReadAction
    static boolean isOutOfRange(DotNetExpression expression, DotNetTypeRef expected) {
        String vmQName = integralVmQName(expected);
        BigInteger[] range = vmQName == null ? null : RANGES.get(vmQName);
        if (range == null) {
            return false;
        }
        Object value = new ConstantExpressionEvaluator(expression).getValue();
        if (!(value instanceof Number number) || value instanceof Double || value instanceof Float || value instanceof java.math.BigDecimal) {
            return false;
        }
        BigInteger big = value instanceof BigInteger bigInteger ? bigInteger : BigInteger.valueOf(number.longValue());
        return big.compareTo(range[0]) < 0 || big.compareTo(range[1]) > 0;
    }

    /**
     * @return true for an integral constant of an integral type, in its range or not - a constant conversion
     */
    @RequiredReadAction
    static boolean isIntegralConstant(DotNetExpression expression, DotNetTypeRef expected) {
        String vmQName = integralVmQName(expected);
        if (vmQName == null || !RANGES.containsKey(vmQName)) {
            return false;
        }
        Object value = new ConstantExpressionEvaluator(expression).getValue();
        return value instanceof Number && !(value instanceof Double || value instanceof Float || value instanceof java.math.BigDecimal);
    }

    @Nullable
    @RequiredReadAction
    private static String integralVmQName(DotNetTypeRef typeRef) {
        if (typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type) {
            if (type.isEnum()) {
                return integralVmQName(type.getTypeRefForEnumConstants());
            }
            return type.getVmQName();
        }
        return null;
    }
}
