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

package consulo.csharp.lang.impl.psi.source.resolve.genericInference;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * The bounds of the type parameters of a generic method collected from its arguments, and their fixing - C# spec 12.6.3
 * (Roslyn's {@code MethodTypeInference}): an exact bound for a ref argument or an invariant type argument, a lower bound
 * for an argument converted to the parameter, an upper bound where variance turns the direction around.
 *
 * @author VISTALL
 */
public final class InferenceBounds {
    private enum Kind {
        EXACT,
        LOWER,
        UPPER
    }

    private record Bound(Kind kind, DotNetTypeRef typeRef) {
    }

    /**
     * Generic interfaces an one-dimensional array implements, which infer from its element type.
     */
    private static final Set<String> ourArrayInterfaces = Set.of("System.Collections.Generic.IEnumerable`1", "System.Collections.Generic.ICollection`1",
        "System.Collections.Generic.IList`1", "System.Collections.Generic.IReadOnlyCollection`1", "System.Collections.Generic.IReadOnlyList`1");

    private final DotNetGenericParameter[] myGenericParameters;
    private final GlobalSearchScope myScope;
    private final Map<DotNetGenericParameter, List<Bound>> myBounds = new LinkedHashMap<>();

    public InferenceBounds(DotNetGenericParameter[] genericParameters, GlobalSearchScope scope) {
        myGenericParameters = genericParameters;
        myScope = scope;
    }

    /**
     * The inference from an argument of type {@code argumentTypeRef} to a parameter of type {@code parameterTypeRef}: exact
     * for a ref or out parameter, lower bound otherwise.
     */
    @RequiredReadAction
    public void inferFromArgument(DotNetTypeRef argumentTypeRef, DotNetTypeRef parameterTypeRef) {
        if (parameterTypeRef instanceof CSharpRefTypeRef parameterRef) {
            DotNetTypeRef argument = argumentTypeRef instanceof CSharpRefTypeRef argumentRef ? argumentRef.getInnerTypeRef() : argumentTypeRef;
            infer(Kind.EXACT, argument, parameterRef.getInnerTypeRef());
            return;
        }
        infer(Kind.LOWER, argumentTypeRef, parameterTypeRef);
    }

    /**
     * A lower bound inference - the return type of a lambda to the return type of the delegate.
     */
    @RequiredReadAction
    public void inferLower(DotNetTypeRef typeRef, DotNetTypeRef parameterTypeRef) {
        infer(Kind.LOWER, typeRef, parameterTypeRef);
    }

    /**
     * An exact inference - an explicitly typed lambda parameter to the parameter of the delegate.
     */
    @RequiredReadAction
    public void inferExact(DotNetTypeRef typeRef, DotNetTypeRef parameterTypeRef) {
        infer(Kind.EXACT, typeRef, parameterTypeRef);
    }

    @RequiredReadAction
    private void infer(Kind kind, DotNetTypeRef source, DotNetTypeRef target) {
        // the null literal has no type, it gives no bound
        if (source == DotNetTypeRef.AUTO_TYPE || source == DotNetTypeRef.UNKNOWN_TYPE || source == DotNetTypeRef.ERROR_TYPE || source instanceof CSharpNullTypeRef) {
            return;
        }

        ProgressManager.checkCanceled();

        if (source instanceof CSharpRefTypeRef sourceRef) {
            source = sourceRef.getInnerTypeRef();
        }
        if (target instanceof CSharpRefTypeRef targetRef) {
            target = targetRef.getInnerTypeRef();
        }

        DotNetTypeResolveResult targetResult = target.resolve();
        DotNetGenericParameter genericParameter = asMethodGenericParameter(targetResult.getElement());
        if (genericParameter != null) {
            myBounds.computeIfAbsent(genericParameter, it -> new ArrayList<>()).add(new Bound(kind, source));
            return;
        }

        DotNetTypeResolveResult sourceResult = source.resolve();

        // arrays: by the element type, exact unless a lower or upper bound of a reference type element
        if (targetResult instanceof CSharpArrayTypeRef.ArrayResolveResult targetArray) {
            if (sourceResult instanceof CSharpArrayTypeRef.ArrayResolveResult sourceArray) {
                inferElement(kind, sourceArray.getInnerTypeRef(), targetArray.getInnerTypeRef());
            }
            else if (kind == Kind.UPPER) {
                // an upper bound of IEnumerable<U> and its friends to an array
                DotNetTypeRef sourceElementTypeRef = arrayInterfaceElement(sourceResult);
                if (sourceElementTypeRef != null) {
                    inferElement(kind, sourceElementTypeRef, targetArray.getInnerTypeRef());
                }
            }
            return;
        }

        // nullable: by the underlying type, exactly
        DotNetTypeRef targetUnderlying = CSharpNullableTypeUtil.unbox(target);
        if (targetUnderlying != target) {
            DotNetTypeRef sourceUnderlying = CSharpNullableTypeUtil.unbox(source);
            if (sourceUnderlying != source || kind == Kind.LOWER) {
                infer(Kind.EXACT, sourceUnderlying, targetUnderlying);
            }
            return;
        }

        PsiElement targetElement = targetResult.getElement();
        if (targetElement == null || typeParameters(targetElement).isEmpty()) {
            return;
        }

        // a lower bound of an array to IEnumerable<T> and its friends
        if (kind == Kind.LOWER && sourceResult instanceof CSharpArrayTypeRef.ArrayResolveResult sourceArray) {
            DotNetTypeRef targetElementTypeRef = arrayInterfaceElement(targetResult);
            if (targetElementTypeRef != null) {
                inferElement(kind, sourceArray.getInnerTypeRef(), targetElementTypeRef);
            }
            return;
        }

        DotNetTypeResolveResult sourceMatch;
        DotNetTypeResolveResult targetMatch;
        switch (kind) {
            case EXACT:
                if (!targetElement.isEquivalentTo(sourceResult.getElement())) {
                    return;
                }
                sourceMatch = sourceResult;
                targetMatch = targetResult;
                break;
            case LOWER:
                // the base type of the source which is the generic type of the target
                sourceMatch = targetElement.isEquivalentTo(sourceResult.getElement()) ? sourceResult : CSharpTypeUtil.findTypeRefFromExtends(source, target);
                targetMatch = targetResult;
                break;
            default:
                // the base type of the target which is the generic type of the source
                sourceMatch = sourceResult;
                targetMatch = targetElement.isEquivalentTo(sourceResult.getElement()) ? targetResult : CSharpTypeUtil.findTypeRefFromExtends(target, source);
                break;
        }

        if (sourceMatch == null || targetMatch == null || targetMatch.getElement() == null) {
            return;
        }

        List<DotNetGenericParameter> typeParameters = typeParameters(targetMatch.getElement());
        DotNetGenericExtractor sourceExtractor = sourceMatch.getGenericExtractor();
        DotNetGenericExtractor targetExtractor = targetMatch.getGenericExtractor();
        for (DotNetGenericParameter typeParameter : typeParameters) {
            DotNetTypeRef sourceArgument = sourceExtractor.extract(typeParameter);
            DotNetTypeRef targetArgument = targetExtractor.extract(typeParameter);
            if (sourceArgument == null || targetArgument == null) {
                continue;
            }

            if (kind == Kind.EXACT || !isReferenceType(sourceArgument)) {
                infer(Kind.EXACT, sourceArgument, targetArgument);
            }
            else if (typeParameter.hasModifier(DotNetModifier.COVARIANT)) {
                infer(kind, sourceArgument, targetArgument);
            }
            else if (typeParameter.hasModifier(DotNetModifier.CONTRAVARIANT)) {
                infer(kind == Kind.LOWER ? Kind.UPPER : Kind.LOWER, sourceArgument, targetArgument);
            }
            else {
                infer(Kind.EXACT, sourceArgument, targetArgument);
            }
        }
    }

    /**
     * @return the element type of IEnumerable&lt;T&gt; and the other generic interfaces of a one-dimensional array
     */
    @Nullable
    @RequiredReadAction
    private static DotNetTypeRef arrayInterfaceElement(DotNetTypeResolveResult result) {
        if (result.getElement() instanceof DotNetTypeDeclaration type && ourArrayInterfaces.contains(type.getVmQName())) {
            return result.getGenericExtractor().extract(type.getGenericParameters()[0]);
        }
        return null;
    }

    /**
     * The type parameters of a generic type, and of the generic types it is nested in - Class&lt;T&gt;.Enum takes its
     * argument from Class.
     */
    @RequiredReadAction
    private static List<DotNetGenericParameter> typeParameters(PsiElement element) {
        List<DotNetGenericParameter> result = new ArrayList<>();
        PsiElement current = element;
        while (current instanceof DotNetGenericParameterListOwner owner) {
            result.addAll(List.of(owner.getGenericParameters()));
            current = current.getParent();
            if (!(current instanceof DotNetTypeDeclaration)) {
                break;
            }
        }
        return result;
    }

    @RequiredReadAction
    private void inferElement(Kind kind, DotNetTypeRef sourceElement, DotNetTypeRef targetElement) {
        infer(kind != Kind.EXACT && isReferenceType(sourceElement) ? kind : Kind.EXACT, sourceElement, targetElement);
    }

    /**
     * Fixes every type parameter with bounds: the candidates are the bound types; an exact bound keeps the identical
     * ones, a lower bound the ones it converts to, an upper bound the ones which convert to it; of those the one every
     * other converts to.
     */
    @RequiredReadAction
    public Map<DotNetGenericParameter, DotNetTypeRef> fix() {
        Map<DotNetGenericParameter, DotNetTypeRef> result = new LinkedHashMap<>();
        for (Map.Entry<DotNetGenericParameter, List<Bound>> entry : myBounds.entrySet()) {
            DotNetTypeRef fixedTypeRef = fix(entry.getValue());
            if (fixedTypeRef != null) {
                result.put(entry.getKey(), fixedTypeRef);
            }
        }
        return result;
    }

    @Nullable
    @RequiredReadAction
    private DotNetTypeRef fix(List<Bound> bounds) {
        List<DotNetTypeRef> candidates = new ArrayList<>();
        for (Bound bound : bounds) {
            if (candidates.stream().noneMatch(it -> CSharpTypeUtil.isTypeEqual(it, bound.typeRef()))) {
                candidates.add(bound.typeRef());
            }
        }

        for (Bound bound : bounds) {
            switch (bound.kind()) {
                case EXACT -> candidates.removeIf(candidate -> !CSharpTypeUtil.isTypeEqual(candidate, bound.typeRef()));
                case LOWER -> candidates.removeIf(candidate -> !convertsImplicitly(bound.typeRef(), candidate));
                case UPPER -> candidates.removeIf(candidate -> !convertsImplicitly(candidate, bound.typeRef()));
            }
        }

        if (candidates.isEmpty()) {
            // no candidate satisfies every bound - keep what the old inference gave, the first type seen
            return bounds.get(0).typeRef();
        }

        // the most general: the one every other candidate converts to
        for (DotNetTypeRef candidate : candidates) {
            if (candidates.stream().allMatch(other -> other == candidate || convertsImplicitly(other, candidate))) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    @RequiredReadAction
    private boolean convertsImplicitly(DotNetTypeRef from, DotNetTypeRef to) {
        return CSharpTypeUtil.isTypeEqual(from, to) || CSharpTypeUtil.isInheritable(to, from, CSharpCastType.IMPLICIT, myScope).isSuccess();
    }

    @Nullable
    private DotNetGenericParameter asMethodGenericParameter(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        for (DotNetGenericParameter genericParameter : myGenericParameters) {
            if (genericParameter.isEquivalentTo(element)) {
                return genericParameter;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isReferenceType(DotNetTypeRef typeRef) {
        PsiElement element = typeRef.resolve().getElement();
        if (element instanceof DotNetTypeDeclaration typeDeclaration) {
            return !typeDeclaration.isStruct() && !typeDeclaration.isEnum() && !DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName());
        }
        // arrays, delegates are reference types; a type parameter is not known to be one
        return element != null && !(element instanceof DotNetGenericParameter);
    }
}
