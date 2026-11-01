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

package consulo.csharp.impl.ide.msil.representation.builder;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTupleTypeRef;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetPointerTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.impl.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.msil.lang.psi.MsilMethodEntry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Nullable reference types of compiled code. The compiler keeps {@code string?} vs {@code string} in attributes:
 * {@code [Nullable(b)]} / {@code [Nullable(new byte[] { ... })]} on a member, parameter or return value, and
 * {@code [NullableContext(b)]} on a method or type as the default of everything inside. A byte is {@code 0} oblivious,
 * {@code 1} not annotated, {@code 2} annotated ({@code ?}); an array has one byte per type of the signature in pre-order
 * (an array before its element, a generic type before its arguments) where value types - except type parameters - have
 * none, a single byte applies to all of them.
 * <p>
 * The stubs show these as {@code ?} on the types and hide the attributes.
 */
final class CSharpNullableStubUtil {
    private static final String NULLABLE_ATTRIBUTE = "System.Runtime.CompilerServices.NullableAttribute";
    private static final String NULLABLE_PUBLIC_ONLY_ATTRIBUTE = "System.Runtime.CompilerServices.NullablePublicOnlyAttribute";

    private static final Set<String> METADATA_ATTRIBUTES =
        Set.of(NULLABLE_ATTRIBUTE, DotNetTypes.System.Runtime.CompilerServices.NullableContextAttribute, NULLABLE_PUBLIC_ONLY_ATTRIBUTE);

    private static final byte OBLIVIOUS = 0;
    private static final byte ANNOTATED = 2;

    private static final int PRESENTATION_FLAGS = CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD;

    private CSharpNullableStubUtil() {
    }

    /**
     * @return the attributes without the nullable metadata ones, which are shown as {@code ?}
     */
    @RequiredReadAction
    static DotNetAttribute[] withoutNullableAttributes(DotNetAttribute[] attributes) {
        List<DotNetAttribute> result = new ArrayList<>(attributes.length);
        for (DotNetAttribute attribute : attributes) {
            if (!isNullableAttribute(attribute)) {
                result.add(attribute);
            }
        }
        return result.size() == attributes.length ? attributes : result.toArray(DotNetAttribute.EMPTY_ARRAY);
    }

    /**
     * Nullability of the type of a field, property, event or parameter: its own {@code [Nullable]}, else the context.
     */
    @RequiredReadAction
    static byte[] getFlags(DotNetModifierListOwner owner) {
        DotNetModifierList modifierList = owner.getModifierList();
        byte[] flags = modifierList == null ? null : readFlags(modifierList.getAttributes(), NULLABLE_ATTRIBUTE);
        return flags != null ? flags : new byte[]{getContextFlag(owner)};
    }

    /**
     * Nullability of the return type of a method: the {@code [Nullable]} of its return value ({@code .param [0]}), else
     * the context.
     */
    @RequiredReadAction
    static byte[] getReturnFlags(DotNetModifierListOwner method) {
        if (method.getOriginalElement() instanceof MsilMethodEntry methodEntry) {
            // parameter index -1 is the return value
            byte[] flags = readFlags(methodEntry.getParameterAttributes(-1), NULLABLE_ATTRIBUTE);
            if (flags != null) {
                return flags;
            }
        }
        return new byte[]{getContextFlag(method)};
    }

    /**
     * Appends the type with {@code ?} where the flags annotate it; plain when the flags do not fit the type.
     */
    @RequiredReadAction
    static void appendTypeRef(StringBuilder builder, DotNetTypeRef typeRef, byte[] flags) {
        if (flags.length == 1 && flags[0] != ANNOTATED) {
            CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, PRESENTATION_FLAGS);
            return;
        }
        Appender appender = new Appender(flags);
        StringBuilder text = new StringBuilder();
        appender.append(text, typeRef);
        if (appender.isConsistent()) {
            builder.append(text);
        }
        else {
            CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, PRESENTATION_FLAGS);
        }
    }

    @RequiredReadAction
    private static boolean isNullableAttribute(DotNetAttribute attribute) {
        return attribute instanceof MsilCustomAttribute && METADATA_ATTRIBUTES.contains(attribute.toTypeRef().getVmQName());
    }

    /**
     * {@code [NullableContext]} of the closest method or type.
     */
    @RequiredReadAction
    private static byte getContextFlag(PsiElement element) {
        for (PsiElement current = element; current != null && !(current instanceof PsiFile); current = current.getParent()) {
            if (current instanceof DotNetModifierListOwner owner) {
                DotNetModifierList modifierList = owner.getModifierList();
                byte[] flags = modifierList == null
                    ? null
                    : readFlags(modifierList.getAttributes(), DotNetTypes.System.Runtime.CompilerServices.NullableContextAttribute);
                if (flags != null && flags.length == 1) {
                    return flags[0];
                }
            }
        }
        return OBLIVIOUS;
    }

    @RequiredReadAction
    private static byte @Nullable [] readFlags(DotNetAttribute[] attributes, String attributeVmQName) {
        for (DotNetAttribute attribute : attributes) {
            if (!(attribute instanceof MsilCustomAttribute customAttribute) || !attributeVmQName.equals(attribute.toTypeRef().getVmQName())) {
                continue;
            }
            List<MsiCustomAttributeValue> arguments = MsilCustomAttributeStubber.build(customAttribute).getConstructorArguments();
            if (arguments.size() != 1) {
                return null;
            }
            Object value = arguments.get(0).getValue();
            if (value instanceof Number number) {
                return new byte[]{number.byteValue()};
            }
            if (value instanceof List<?> elements) {
                byte[] flags = new byte[elements.size()];
                for (int i = 0; i < flags.length; i++) {
                    if (!(elements.get(i) instanceof Number number)) {
                        return null;
                    }
                    flags[i] = number.byteValue();
                }
                return flags;
            }
            return null;
        }
        return null;
    }

    /**
     * Walks a type in the order the compiler wrote the flags, taking one per reference type and type parameter.
     */
    private static final class Appender {
        private final byte[] myFlags;
        private int myIndex;
        private boolean myInconsistent;

        private Appender(byte[] flags) {
            myFlags = flags;
        }

        private boolean isConsistent() {
            return !myInconsistent && (myFlags.length == 1 || myIndex == myFlags.length);
        }

        private byte next() {
            if (myFlags.length == 1) {
                return myFlags[0];
            }
            if (myIndex < myFlags.length) {
                return myFlags[myIndex++];
            }
            myInconsistent = true;
            return OBLIVIOUS;
        }

        @RequiredReadAction
        private void append(StringBuilder builder, DotNetTypeRef typeRef) {
            if (typeRef instanceof CSharpArrayTypeRef arrayTypeRef) {
                // an array is a reference type, its flag comes before the one of its element
                byte flag = next();
                append(builder, arrayTypeRef.getInnerTypeRef());
                builder.append("[");
                for (int i = 0; i < arrayTypeRef.getDimensions(); i++) {
                    builder.append(",");
                }
                builder.append("]");
                appendQuestionMark(builder, flag);
            }
            else if (typeRef instanceof CSharpRefTypeRef refTypeRef) {
                builder.append(refTypeRef.getType().name()).append(" ");
                append(builder, refTypeRef.getInnerTypeRef());
            }
            else if (typeRef instanceof DotNetPointerTypeRef pointerTypeRef) {
                append(builder, pointerTypeRef.getInnerTypeRef());
                builder.append("*");
            }
            else if (typeRef instanceof CSharpTupleTypeRef) {
                // tuple elements are not walked - leave them plain, but keep the flags in step
                myInconsistent = true;
                CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, PRESENTATION_FLAGS);
            }
            else {
                appendResolved(builder, typeRef);
            }
        }

        @RequiredReadAction
        private void appendResolved(StringBuilder builder, DotNetTypeRef typeRef) {
            DotNetTypeResolveResult resolveResult = typeRef.resolve();
            PsiElement element = resolveResult.getElement();

            boolean valueType = element instanceof DotNetTypeDeclaration declaration && (declaration.isStruct() || declaration.isEnum());
            byte flag = valueType ? OBLIVIOUS : next();

            List<DotNetTypeRef> arguments = getGenericArguments(element, resolveResult.getGenericExtractor());
            if (arguments.isEmpty()) {
                CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, PRESENTATION_FLAGS);
            }
            else if (element instanceof DotNetTypeDeclaration declaration && DotNetTypes.System.Nullable$1.equals(declaration.getVmQName())) {
                // T? of a value type
                append(builder, arguments.get(0));
                builder.append("?");
            }
            else {
                CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, PRESENTATION_FLAGS | CSharpTypeRefPresentationUtil.NO_GENERIC_ARGUMENTS);
                builder.append("<");
                for (int i = 0; i < arguments.size(); i++) {
                    if (i != 0) {
                        builder.append(", ");
                    }
                    append(builder, arguments.get(i));
                }
                builder.append(">");
            }
            if (!valueType) {
                appendQuestionMark(builder, flag);
            }
        }

        @RequiredReadAction
        private List<DotNetTypeRef> getGenericArguments(@Nullable PsiElement element, DotNetGenericExtractor extractor) {
            if (!(element instanceof DotNetGenericParameterListOwner owner) || element instanceof DotNetGenericParameter) {
                return List.of();
            }
            if (element.getParent() instanceof DotNetTypeDeclaration outer && outer.getGenericParameters().length > 0) {
                // arguments of the outer type come first in the flags - not walked
                myInconsistent = true;
                return List.of();
            }
            DotNetGenericParameter[] parameters = owner.getGenericParameters();
            List<DotNetTypeRef> arguments = new ArrayList<>(parameters.length);
            for (DotNetGenericParameter parameter : parameters) {
                DotNetTypeRef argument = extractor.extract(parameter);
                if (argument == null) {
                    return List.of();
                }
                arguments.add(argument);
            }
            return arguments;
        }

        private static void appendQuestionMark(StringBuilder builder, byte flag) {
            if (flag == ANNOTATED) {
                builder.append("?");
            }
        }
    }
}
