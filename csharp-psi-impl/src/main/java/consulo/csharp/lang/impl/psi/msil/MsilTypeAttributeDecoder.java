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
package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpDynamicTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTupleTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.impl.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.msil.lang.psi.MsilMethodEntry;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The C# meaning of a metadata type which the CLR type does not have - stored by the compiler in attributes of the
 * declaration of the type, as Roslyn's {@code DynamicTypeDecoder}, {@code TupleTypeDecoder} and others decode it. A type the attributes do not
 * fit stays as it is.
 *
 * @author VISTALL
 */
public final class MsilTypeAttributeDecoder {
    private MsilTypeAttributeDecoder() {
    }

    /**
     * The C# type of the return value of a method - with the attributes of its {@code .param [0]}
     */
    @RequiredReadAction
    public static DotNetTypeRef decodeReturnType(MsilMethodEntry methodEntry) {
        return decode(MsilToCSharpUtil.extractToCSharp(methodEntry.getReturnTypeRef()), methodEntry.getParameterAttributes(-1));
    }

    /**
     * The C# type of a parameter - an in one has the type of the value
     */
    @RequiredReadAction
    public static DotNetTypeRef decodeParameterType(DotNetTypeRef typeRef, DotNetAttribute[] attributes) {
        DotNetTypeRef decoded = decode(typeRef, attributes);
        if (decoded instanceof CSharpRefTypeRef refTypeRef && isReadOnlyReference(attributes)) {
            return refTypeRef.getInnerTypeRef();
        }
        return decoded;
    }

    /**
     * in and ref readonly of a byref parameter: IsReadOnlyAttribute, or RequiresLocationAttribute - C# 12 ref readonly, which C# here
     * knows as in
     */
    @RequiredReadAction
    public static boolean isReadOnlyReference(DotNetAttribute[] attributes) {
        return findAttribute(attributes, DotNetTypes2.System.Runtime.CompilerServices.IsReadOnlyAttribute) != null
            || findAttribute(attributes, DotNetTypes2.System.Runtime.CompilerServices.RequiresLocationAttribute) != null;
    }

    /**
     * @param typeRef    the C# type of a field, property, parameter or return value - from {@link MsilToCSharpUtil#extractToCSharp}
     * @param attributes the attributes of that declaration
     */
    @RequiredReadAction
    public static DotNetTypeRef decode(DotNetTypeRef typeRef, DotNetAttribute[] attributes) {
        // dynamic first - it keeps the generic types, of which the tuples are made
        return decodeTupleNames(decodeDynamic(typeRef, attributes), attributes);
    }

    /**
     * object as dynamic: [Dynamic] is the type itself, [Dynamic(bool[])] a flag for each type in the type tree, in prefix order -
     * byref, array and pointer before their element, a generic type before its arguments
     */
    @RequiredReadAction
    private static DotNetTypeRef decodeDynamic(DotNetTypeRef typeRef, DotNetAttribute[] attributes) {
        List<MsiCustomAttributeValue> arguments = findArguments(attributes, DotNetTypes2.System.Runtime.CompilerServices.DynamicAttribute);
        if (arguments == null) {
            return typeRef;
        }

        boolean[] flags;
        if (arguments.isEmpty()) {
            flags = new boolean[]{true};
        }
        else if (arguments.get(0).getValue() instanceof List<?> values) {
            flags = new boolean[values.size()];
            for (int i = 0; i < flags.length; i++) {
                if (!(values.get(i) instanceof Boolean flag)) {
                    return typeRef;
                }
                flags[i] = flag;
            }
        }
        else {
            return typeRef;
        }

        int[] index = {0};
        DotNetTypeRef decoded = transformDynamic(typeRef, flags, index);
        return decoded != null && index[0] == flags.length ? decoded : typeRef;
    }

    @Nullable
    private static DotNetTypeRef transformDynamic(DotNetTypeRef typeRef, boolean[] flags, int[] index) {
        if (index[0] >= flags.length) {
            return null;
        }
        boolean dynamic = flags[index[0]++];

        if (typeRef instanceof CSharpTypeRefByQName qNameTypeRef && qNameTypeRef.isEqualToVmQName(DotNetTypes.System.Object)) {
            return dynamic ? new CSharpDynamicTypeRef(typeRef.getProject(), typeRef.getResolveScope()) : typeRef;
        }
        // only object can be dynamic
        if (dynamic) {
            return null;
        }

        if (typeRef instanceof CSharpRefTypeRef refTypeRef) {
            DotNetTypeRef inner = transformDynamic(refTypeRef.getInnerTypeRef(), flags, index);
            return inner == null ? null : new CSharpRefTypeRef(typeRef.getProject(), typeRef.getResolveScope(), refTypeRef.getType(), inner);
        }
        if (typeRef instanceof CSharpArrayTypeRef arrayTypeRef) {
            DotNetTypeRef inner = transformDynamic(arrayTypeRef.getInnerTypeRef(), flags, index);
            return inner == null ? null : new CSharpArrayTypeRef(typeRef.getProject(), typeRef.getResolveScope(), inner, arrayTypeRef.getDimensions());
        }
        if (typeRef instanceof CSharpPointerTypeRef pointerTypeRef) {
            DotNetTypeRef inner = transformDynamic(pointerTypeRef.getInnerTypeRef(), flags, index);
            return inner == null ? null : new CSharpPointerTypeRef(inner);
        }
        if (typeRef instanceof CSharpGenericWrapperTypeRef genericTypeRef) {
            DotNetTypeRef[] arguments = genericTypeRef.getArgumentTypeRefs();
            DotNetTypeRef[] transformed = new DotNetTypeRef[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                transformed[i] = transformDynamic(arguments[i], flags, index);
                if (transformed[i] == null) {
                    return null;
                }
            }
            return new CSharpGenericWrapperTypeRef(typeRef.getProject(), typeRef.getResolveScope(), genericTypeRef.getInnerTypeRef(), transformed);
        }
        return typeRef;
    }

    /**
     * ValueTuple as a tuple with element names: [TupleElementNames(string[])] has the names of all tuples of the type tree, in prefix
     * order - a tuple before the tuples of its elements, null for an unnamed element
     */
    @RequiredReadAction
    private static DotNetTypeRef decodeTupleNames(DotNetTypeRef typeRef, DotNetAttribute[] attributes) {
        MsilCustomAttribute attribute = findAttribute(attributes, DotNetTypes2.System.Runtime.CompilerServices.TupleElementNamesAttribute);
        if (attribute == null) {
            return typeRef;
        }

        List<MsiCustomAttributeValue> arguments = MsilCustomAttributeStubber.build(attribute).getConstructorArguments();
        if (arguments.size() != 1 || !(arguments.get(0).getValue() instanceof List<?> values)) {
            return typeRef;
        }

        String[] names = new String[values.size()];
        for (int i = 0; i < names.length; i++) {
            // a null string of the blob is read as an empty one
            Object value = values.get(i);
            names[i] = value == null || value.toString().isEmpty() ? null : value.toString();
        }

        int[] index = {0};
        DotNetTypeRef decoded = transformTupleNames(typeRef, names, index, attribute);
        return decoded != null && index[0] == names.length ? decoded : typeRef;
    }

    @Nullable
    @RequiredReadAction
    private static DotNetTypeRef transformTupleNames(DotNetTypeRef typeRef, String[] names, int[] index, PsiElement attribute) {
        if (typeRef instanceof CSharpRefTypeRef refTypeRef) {
            DotNetTypeRef inner = transformTupleNames(refTypeRef.getInnerTypeRef(), names, index, attribute);
            return inner == null ? null : new CSharpRefTypeRef(typeRef.getProject(), typeRef.getResolveScope(), refTypeRef.getType(), inner);
        }
        if (typeRef instanceof CSharpArrayTypeRef arrayTypeRef) {
            DotNetTypeRef inner = transformTupleNames(arrayTypeRef.getInnerTypeRef(), names, index, attribute);
            return inner == null ? null : new CSharpArrayTypeRef(typeRef.getProject(), typeRef.getResolveScope(), inner, arrayTypeRef.getDimensions());
        }
        if (typeRef instanceof CSharpPointerTypeRef pointerTypeRef) {
            DotNetTypeRef inner = transformTupleNames(pointerTypeRef.getInnerTypeRef(), names, index, attribute);
            return inner == null ? null : new CSharpPointerTypeRef(inner);
        }
        if (!(typeRef instanceof CSharpGenericWrapperTypeRef genericTypeRef)) {
            return typeRef;
        }

        DotNetTypeRef[] arguments = genericTypeRef.getArgumentTypeRefs();
        // ValueTuple`8 has seven elements, and the rest as a tuple of its own
        int elements = isValueTuple(genericTypeRef.getInnerTypeRef(), arguments.length) ? Math.min(arguments.length, 7) : 0;
        int start = index[0];
        index[0] += elements;
        if (index[0] > names.length) {
            return null;
        }

        DotNetTypeRef[] transformed = new DotNetTypeRef[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            transformed[i] = transformTupleNames(arguments[i], names, index, attribute);
            if (transformed[i] == null) {
                return null;
            }
        }

        boolean named = false;
        for (int i = start; i < start + elements; i++) {
            named |= names[i] != null;
        }
        // a tuple of 8 and more elements is a generic type for C# here
        if (named && arguments.length < 8) {
            MsilTupleElement[] variables = new MsilTupleElement[elements];
            for (int i = 0; i < elements; i++) {
                variables[i] = new MsilTupleElement(attribute, names[start + i]);
            }
            return new CSharpTupleTypeRef(typeRef.getProject(), typeRef.getResolveScope(), transformed, variables);
        }
        return new CSharpGenericWrapperTypeRef(typeRef.getProject(), typeRef.getResolveScope(), genericTypeRef.getInnerTypeRef(), transformed);
    }

    @RequiredReadAction
    private static boolean isValueTuple(DotNetTypeRef typeRef, int arity) {
        return ("System.ValueTuple`" + arity).equals(typeRef.getVmQName());
    }

    /**
     * @return the constructor arguments of the attribute, or null without it
     */
    @Nullable
    @RequiredReadAction
    private static List<MsiCustomAttributeValue> findArguments(DotNetAttribute[] attributes, String vmQName) {
        MsilCustomAttribute attribute = findAttribute(attributes, vmQName);
        return attribute == null ? null : MsilCustomAttributeStubber.build(attribute).getConstructorArguments();
    }

    @Nullable
    @RequiredReadAction
    private static MsilCustomAttribute findAttribute(DotNetAttribute[] attributes, String vmQName) {
        for (DotNetAttribute attribute : attributes) {
            if (attribute instanceof MsilCustomAttribute customAttribute && vmQName.equals(attribute.toTypeRef().getVmQName())) {
                return customAttribute;
            }
        }
        return null;
    }
}
