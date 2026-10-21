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

package consulo.csharp.lang.impl.psi.cast;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Conversions between the predefined types (object, string, bool, char, the numeric types and their nullable forms),
 * classified like Roslyn's {@code ConversionEasyOut} - C# spec 10.2 and 10.3. They do not depend on how the base class
 * library declares conversion operators.
 *
 * @author VISTALL
 */
public final class PredefinedConversions {
    public enum Kind {
        IDENTITY(true, false),
        IMPLICIT_NUMERIC(true, true),
        IMPLICIT_NULLABLE(true, true),
        IMPLICIT_REFERENCE(true, false),
        BOXING(true, false),
        EXPLICIT_NUMERIC(false, true),
        EXPLICIT_NULLABLE(false, true),
        EXPLICIT_REFERENCE(false, false),
        UNBOXING(false, false),
        NONE(false, false);

        private final boolean myImplicit;
        private final boolean myConversion;

        Kind(boolean implicit, boolean conversion) {
            myImplicit = implicit;
            myConversion = conversion;
        }

        public boolean isImplicit() {
            return myImplicit;
        }

        public boolean isExplicit() {
            return this != NONE;
        }

        /**
         * @return true if the value changes its representation - not identity, reference or boxing
         */
        public boolean isConversion() {
            return myConversion;
        }
    }

    private static final Set<String> ourNumeric = Set.of(DotNetTypes.System.Char, DotNetTypes.System.SByte, DotNetTypes.System.Byte,
        DotNetTypes.System.Int16, DotNetTypes.System.UInt16, DotNetTypes.System.Int32, DotNetTypes.System.UInt32, DotNetTypes.System.Int64,
        DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal);

    /**
     * Implicit numeric conversions, C# spec 10.2.3.
     */
    private static final Map<String, Set<String>> ourImplicitNumeric = Map.ofEntries(
        Map.entry(DotNetTypes.System.SByte, Set.of(DotNetTypes.System.Int16, DotNetTypes.System.Int32, DotNetTypes.System.Int64,
            DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Byte, Set.of(DotNetTypes.System.Int16, DotNetTypes.System.UInt16, DotNetTypes.System.Int32,
            DotNetTypes.System.UInt32, DotNetTypes.System.Int64, DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double,
            DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Int16, Set.of(DotNetTypes.System.Int32, DotNetTypes.System.Int64, DotNetTypes.System.Single,
            DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.UInt16, Set.of(DotNetTypes.System.Int32, DotNetTypes.System.UInt32, DotNetTypes.System.Int64,
            DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Int32, Set.of(DotNetTypes.System.Int64, DotNetTypes.System.Single, DotNetTypes.System.Double,
            DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.UInt32, Set.of(DotNetTypes.System.Int64, DotNetTypes.System.UInt64, DotNetTypes.System.Single,
            DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Int64, Set.of(DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.UInt64, Set.of(DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Char, Set.of(DotNetTypes.System.UInt16, DotNetTypes.System.Int32, DotNetTypes.System.UInt32,
            DotNetTypes.System.Int64, DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal)),
        Map.entry(DotNetTypes.System.Single, Set.of(DotNetTypes.System.Double))
    );

    private record PredefinedType(String vmQName, boolean nullable) {
        boolean isValueType() {
            return !vmQName.equals(DotNetTypes.System.Object) && !vmQName.equals(DotNetTypes.System.String);
        }
    }

    private PredefinedConversions() {
    }

    /**
     * @return the conversion from {@code source} to {@code target}, or null if one of them is not a predefined type
     */
    @Nullable
    @RequiredReadAction
    public static Kind classify(DotNetTypeRef source, DotNetTypeRef target) {
        PredefinedType from = predefined(source);
        if (from == null) {
            return null;
        }
        PredefinedType to = predefined(target);
        if (to == null) {
            return null;
        }

        if (from.equals(to)) {
            return Kind.IDENTITY;
        }

        if (from.vmQName().equals(DotNetTypes.System.Object)) {
            return to.vmQName().equals(DotNetTypes.System.String) ? Kind.EXPLICIT_REFERENCE : Kind.UNBOXING;
        }
        if (to.vmQName().equals(DotNetTypes.System.Object)) {
            return from.vmQName().equals(DotNetTypes.System.String) ? Kind.IMPLICIT_REFERENCE : Kind.BOXING;
        }
        if (!from.isValueType() || !to.isValueType()) {
            return Kind.NONE;
        }

        Kind underlying = classifyUnderlying(from.vmQName(), to.vmQName());
        if (underlying == Kind.NONE) {
            return Kind.NONE;
        }
        if (!from.nullable() && !to.nullable()) {
            return underlying;
        }
        // S -> T? and S? -> T? follow S -> T; S? -> T is explicit always
        if (to.nullable() && underlying.isImplicit()) {
            return Kind.IMPLICIT_NULLABLE;
        }
        return Kind.EXPLICIT_NULLABLE;
    }

    private static Kind classifyUnderlying(String from, String to) {
        if (from.equals(to)) {
            return Kind.IDENTITY;
        }
        if (!ourNumeric.contains(from) || !ourNumeric.contains(to)) {
            // bool has no conversion to or from the numeric types
            return Kind.NONE;
        }
        Set<String> implicitTargets = ourImplicitNumeric.get(from);
        return implicitTargets != null && implicitTargets.contains(to) ? Kind.IMPLICIT_NUMERIC : Kind.EXPLICIT_NUMERIC;
    }

    @Nullable
    @RequiredReadAction
    private static PredefinedType predefined(DotNetTypeRef typeRef) {
        DotNetTypeRef underlying = CSharpNullableTypeUtil.unbox(typeRef);
        String vmQName = vmQName(underlying);
        if (vmQName == null) {
            return null;
        }

        boolean nullable = underlying != typeRef;
        boolean predefined = vmQName.equals(DotNetTypes.System.Boolean) || ourNumeric.contains(vmQName)
            || !nullable && (vmQName.equals(DotNetTypes.System.Object) || vmQName.equals(DotNetTypes.System.String));
        return predefined ? new PredefinedType(vmQName, nullable) : null;
    }

    @Nullable
    @RequiredReadAction
    private static String vmQName(DotNetTypeRef typeRef) {
        PsiElement element = typeRef.resolve().getElement();
        if (element instanceof DotNetTypeDeclaration typeDeclaration && !typeDeclaration.isEnum()) {
            return typeDeclaration.getVmQName();
        }
        return null;
    }
}
