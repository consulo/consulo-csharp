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
package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTupleTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpSearchUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetRefTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

/**
 * The type of an element of a deconstruction - {@code var (a, b) = x}, {@code foreach (var (k, v) in map)},
 * a positional pattern {@code Point(var x, var y)}
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpDeconstructionUtil {
    /**
     * @param source the deconstructed type
     * @param index  the index of the element
     * @param count  the count of the elements - the arity of Deconstruct
     */
    @RequiredReadAction
    public static DotNetTypeRef getElementTypeRef(DotNetTypeRef source, int index, int count) {
        if (source == DotNetTypeRef.ERROR_TYPE || source == DotNetTypeRef.AUTO_TYPE) {
            return DotNetTypeRef.ERROR_TYPE;
        }

        // (1, "a") - a tuple literal or a tuple type
        if (source instanceof CSharpTupleTypeRef tupleTypeRef) {
            DotNetTypeRef[] typeRefs = tupleTypeRef.getTypeRefs();
            return index < typeRefs.length ? typeRefs[index] : DotNetTypeRef.ERROR_TYPE;
        }

        DotNetTypeResolveResult resolveResult = source.resolve();
        PsiElement element = resolveResult.getElement();
        // System.ValueTuple<T1, T2> from a library - the type arguments are the elements
        if (element instanceof DotNetTypeDeclaration typeDeclaration && "System.ValueTuple".equals(typeDeclaration.getPresentableQName())) {
            DotNetGenericParameter[] genericParameters = typeDeclaration.getGenericParameters();
            if (genericParameters.length == count && index < count) {
                DotNetTypeRef typeRef = resolveResult.getGenericExtractor().extract(genericParameters[index]);
                return typeRef == null ? DotNetTypeRef.ERROR_TYPE : typeRef;
            }
        }

        // void Deconstruct(out T1 a, out T2 b)
        DotNetMethodDeclaration deconstruct = CSharpSearchUtil.findMethodByName("Deconstruct", null, source, count);
        if (deconstruct != null) {
            DotNetParameter[] parameters = deconstruct.getParameters();
            if (index < parameters.length) {
                DotNetTypeRef typeRef = parameters[index].toTypeRef(true);
                return typeRef instanceof DotNetRefTypeRef refTypeRef ? refTypeRef.getInnerTypeRef() : typeRef;
            }
        }
        return DotNetTypeRef.ERROR_TYPE;
    }
}
