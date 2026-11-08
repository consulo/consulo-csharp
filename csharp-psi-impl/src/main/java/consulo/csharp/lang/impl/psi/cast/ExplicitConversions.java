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
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpInheritableChecker;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpDynamicTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.collection.ArrayUtil;

import java.util.Set;

/**
 * Whether a cast {@code (T) e} is valid: an explicit conversion from the type of {@code e} to {@code T} exists -
 * C# spec 10.3 (explicit conversions), in the order of Roslyn's {@code ClassifyExplicitConversion}:
 * <ol>
 * <li>an implicit conversion (every implicit conversion is also explicit)</li>
 * <li>explicit numeric, enumeration and nullable conversions</li>
 * <li>explicit reference conversions and unboxing, including the explicit type parameter conversions</li>
 * <li>user-defined explicit conversions: an implicit or explicit operator with standard explicit conversions around it</li>
 * </ol>
 * The answer is the one of the compiler (CS0030 when false); every case the model cannot decide is answered true,
 * so that an error is never reported for valid code.
 *
 * @author VISTALL
 */
public final class ExplicitConversions {
    private static final Set<String> ourEnumConvertible = Set.of(DotNetTypes.System.Char, DotNetTypes.System.SByte, DotNetTypes.System.Byte,
        DotNetTypes.System.Int16, DotNetTypes.System.UInt16, DotNetTypes.System.Int32, DotNetTypes.System.UInt32, DotNetTypes.System.Int64,
        DotNetTypes.System.UInt64, DotNetTypes.System.Single, DotNetTypes.System.Double, DotNetTypes.System.Decimal,
        DotNetTypes.System.IntPtr, DotNetTypes.System.UIntPtr);

    private ExplicitConversions() {
    }

    /**
     * @return true if {@code (target) source} compiles - an explicit conversion from {@code source} to {@code target} exists
     */
    @RequiredReadAction
    public static boolean isCastable(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        return isCastable(source, target, scope, true);
    }

    @RequiredReadAction
    private static boolean isCastable(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope, boolean userDefined) {
        if (CSharpTypeUtil.isErrorTypeRef(source) || CSharpTypeUtil.isErrorTypeRef(target)
            || source == DotNetTypeRef.AUTO_TYPE || target == DotNetTypeRef.AUTO_TYPE) {
            return true;
        }
        // dynamic converts explicitly to every type
        if (source instanceof CSharpDynamicTypeRef || target instanceof CSharpDynamicTypeRef) {
            return true;
        }
        // (int) null is CS0037, not CS0030 - another check reports it
        if (source instanceof CSharpNullTypeRef) {
            return true;
        }

        // arrays and type parameters are decided here: the implicit check accepts covariance of value type elements
        // (int[] -> long[]) and converts to a type parameter by its constraint
        if (source instanceof CSharpArrayTypeRef sourceArray && target instanceof CSharpArrayTypeRef targetArray) {
            return isArrayCastable(sourceArray, targetArray, scope);
        }
        Boolean typeParameter = classifyTypeParameter(source, target, scope);
        if (typeParameter != null) {
            return typeParameter;
        }

        if (isImplicit(source, target, scope)) {
            return true;
        }

        PredefinedConversions.Kind predefined = PredefinedConversions.classify(source, target);
        if (predefined != null) {
            return predefined.isExplicit() || userDefined && isUserDefined(source, target, scope);
        }

        if (isEnumerationOrNullable(source, target, scope)) {
            return true;
        }

        Boolean reference = classifyReference(source, target, scope);
        if (reference == null || reference) {
            return true;
        }

        return userDefined && isUserDefined(source, target, scope);
    }

    @RequiredReadAction
    private static boolean isImplicit(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        // the checker answers "converts target to top"
        return CSharpInheritableChecker.create(target, source).withCastType(CSharpCastType.IMPLICIT, scope).check().isSuccess();
    }

    /**
     * Explicit enumeration conversions (spec 10.3.3) and explicit nullable conversions (spec 10.3.4)
     */
    @RequiredReadAction
    private static boolean isEnumerationOrNullable(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        DotNetTypeRef sourceUnderlying = CSharpNullableTypeUtil.unbox(source);
        DotNetTypeRef targetUnderlying = CSharpNullableTypeUtil.unbox(target);
        boolean sourceEnum = isEnum(sourceUnderlying);
        boolean targetEnum = isEnum(targetUnderlying);
        if ((sourceEnum || targetEnum)
            && (sourceEnum || isNumeric(sourceUnderlying))
            && (targetEnum || isNumeric(targetUnderlying))) {
            // S? -> T is explicit like S -> T; T? and T both take an enumeration conversion
            return true;
        }
        // explicit nullable: S -> T?, S? -> T, S? -> T? when S -> T is an explicit conversion of value types
        if (sourceUnderlying != source || targetUnderlying != target) {
            if (isValueType(sourceUnderlying) && isValueType(targetUnderlying)) {
                return isCastable(sourceUnderlying, targetUnderlying, scope, false);
            }
        }
        return false;
    }

    /**
     * Explicit reference conversions (spec 10.3.5), unboxing (10.3.7) and the explicit conversions of type parameters (10.3.8)
     *
     * @return true or false when decided, null when the model cannot tell
     */
    @RequiredReadAction
    private static Boolean classifyReference(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        // the reverse of an implicit reference or boxing conversion: downcasts, unboxing, from the effective base class of a type
        // parameter to the type parameter
        if (isImplicit(target, source, scope)) {
            return true;
        }

        if (source instanceof CSharpArrayTypeRef sourceArray && target instanceof CSharpArrayTypeRef targetArray) {
            return isArrayCastable(sourceArray, targetArray, scope);
        }

        DotNetTypeResolveResult sourceResult = source.resolve();
        DotNetTypeResolveResult targetResult = target.resolve();
        PsiElement sourceElement = sourceResult.getElement();
        PsiElement targetElement = targetResult.getElement();
        if (sourceElement == null || targetElement == null) {
            return null;
        }

        if (source instanceof CSharpArrayTypeRef sourceArray) {
            // S[] -> IList<T> and its base interfaces when S -> T is an explicit reference conversion
            DotNetTypeRef element = singleTypeArgument(targetResult, targetElement);
            return element != null && isReferenceType(sourceArray.getInnerTypeRef()) && isReferenceType(element)
                && isReferenceCastable(sourceArray.getInnerTypeRef(), element, scope);
        }
        if (target instanceof CSharpArrayTypeRef targetArray) {
            // IList<S> and its base interfaces -> T[] when S -> T is an explicit reference conversion
            DotNetTypeRef element = singleTypeArgument(sourceResult, sourceElement);
            return element != null && isReferenceType(element) && isReferenceType(targetArray.getInnerTypeRef())
                && isReferenceCastable(element, targetArray.getInnerTypeRef(), scope);
        }

        // delegates: only to the same generic delegate by variance (Handler -> Handler2 is not a conversion)
        if (sourceResult instanceof CSharpLambdaResolveResult || targetResult instanceof CSharpLambdaResolveResult) {
            return isVariantCastable(sourceResult, targetResult, scope);
        }

        if (!(sourceElement instanceof DotNetTypeDeclaration sourceType) || !(targetElement instanceof DotNetTypeDeclaration targetType)) {
            return null;
        }

        if (sourceType.isInterface() && targetType.isInterface()) {
            // interface -> interface: always explicit, also between two constructions of one generic interface (IGen<object> -> IGen<string>)
            return true;
        }
        // unboxing from System.ValueType to any value type, from System.Enum to any enum (spec 10.3.7)
        if ((targetType.isStruct() || targetType.isEnum()) && DotNetTypes.System.ValueType.equals(sourceType.getVmQName())
            || targetType.isEnum() && DotNetTypes.System.Enum.equals(sourceType.getVmQName())) {
            return true;
        }
        if (targetType.isInterface()) {
            // a class that is not sealed may have a subclass implementing the interface; a sealed class or a struct must implement it
            return isClass(sourceType) && !isSealed(sourceType);
        }
        if (sourceType.isInterface()) {
            // the same for an interface to a class; a struct must implement the interface (unboxing, the reverse implicit above)
            return isClass(targetType) && !isSealed(targetType);
        }
        return false;
    }

    /**
     * Arrays of the same rank (spec 10.3.5): value type elements must be identical, reference type elements must have
     * an explicit reference conversion
     */
    @RequiredReadAction
    private static boolean isArrayCastable(CSharpArrayTypeRef source, CSharpArrayTypeRef target, GlobalSearchScope scope) {
        if (source.getDimensions() != target.getDimensions()) {
            return false;
        }
        DotNetTypeRef sourceElement = source.getInnerTypeRef();
        DotNetTypeRef targetElement = target.getInnerTypeRef();
        if (CSharpTypeUtil.isTypeEqual(sourceElement, targetElement)) {
            return true;
        }
        if (!isReferenceType(sourceElement) || !isReferenceType(targetElement)) {
            return false;
        }
        return isReferenceCastable(sourceElement, targetElement, scope);
    }

    /**
     * The conversions of type parameters (spec 10.2.12 and 10.3.8): a type parameter converts to and from any interface,
     * to its effective base class and its base classes, from them, and to and from the type parameters it depends on
     *
     * @return null if neither type is a type parameter
     */
    @RequiredReadAction
    private static Boolean classifyTypeParameter(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        PsiElement sourceElement = source.resolve().getElement();
        PsiElement targetElement = target.resolve().getElement();
        boolean sourceTypeParameter = sourceElement instanceof DotNetGenericParameter;
        boolean targetTypeParameter = targetElement instanceof DotNetGenericParameter;
        if (!sourceTypeParameter && !targetTypeParameter) {
            return null;
        }
        if (sourceTypeParameter && targetTypeParameter) {
            return sourceElement.isEquivalentTo(targetElement)
                || dependsOn((DotNetGenericParameter) sourceElement, targetElement, 0)
                || dependsOn((DotNetGenericParameter) targetElement, sourceElement, 0);
        }
        if (sourceTypeParameter && isInterface(targetElement) || targetTypeParameter && isInterface(sourceElement)) {
            return true;
        }
        // T -> X and X -> T: X must be the effective base class of T or one of its base classes
        DotNetGenericParameter parameter = (DotNetGenericParameter) (sourceTypeParameter ? sourceElement : targetElement);
        DotNetTypeRef other = sourceTypeParameter ? target : source;
        for (DotNetTypeRef constraint : constraints(parameter)) {
            if (!(constraint.resolve().getElement() instanceof DotNetGenericParameter) && !isInterface(constraint.resolve().getElement())
                && isImplicit(constraint, other, scope)) {
                return true;
            }
        }
        // the effective base class of a type parameter without a class constraint is object (value types: System.ValueType)
        return isObject(other);
    }

    @RequiredReadAction
    private static boolean dependsOn(DotNetGenericParameter parameter, PsiElement other, int depth) {
        if (depth > 10) {
            return false;
        }
        for (DotNetTypeRef constraint : constraints(parameter)) {
            PsiElement element = constraint.resolve().getElement();
            if (element != null && element.isEquivalentTo(other)) {
                return true;
            }
            if (element instanceof DotNetGenericParameter dependency && dependsOn(dependency, other, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static DotNetTypeRef[] constraints(DotNetGenericParameter parameter) {
        return parameter instanceof consulo.csharp.lang.psi.CSharpGenericParameter csharpParameter
            ? csharpParameter.getExtendTypeRefs() : DotNetTypeRef.EMPTY_ARRAY;
    }

    @RequiredReadAction
    private static boolean isObject(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration typeDeclaration
            && DotNetTypes.System.Object.equals(typeDeclaration.getVmQName());
    }

    /**
     * An explicit reference conversion between two reference types - for array elements and type arguments
     */
    @RequiredReadAction
    private static boolean isReferenceCastable(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        if (isImplicit(source, target, scope)) {
            return true;
        }
        Boolean reference = classifyReference(source, target, scope);
        return reference == null || reference;
    }

    /**
     * {@code S<A1..An> -> S<B1..Bn>} of a variant interface or delegate (spec 10.3.5): an invariant argument must be identical,
     * a covariant one must convert by a reference conversion, a contravariant one must be a reference type on both sides
     */
    @RequiredReadAction
    private static Boolean isVariantCastable(DotNetTypeResolveResult sourceResult, DotNetTypeResolveResult targetResult, GlobalSearchScope scope) {
        PsiElement sourceElement = sourceResult.getElement();
        PsiElement targetElement = targetResult.getElement();
        if (sourceElement == null || targetElement == null) {
            return null;
        }
        PsiElement sourceDeclaration = sourceResult instanceof CSharpLambdaResolveResult lambda && lambda.getDelegateDeclaration() != null
            ? lambda.getDelegateDeclaration() : sourceElement;
        PsiElement targetDeclaration = targetResult instanceof CSharpLambdaResolveResult lambda && lambda.getDelegateDeclaration() != null
            ? lambda.getDelegateDeclaration() : targetElement;
        if (!sourceDeclaration.isEquivalentTo(targetDeclaration)) {
            // two different delegate types: no conversion; anything else the model does not know
            return sourceResult instanceof CSharpLambdaResolveResult && targetResult instanceof CSharpLambdaResolveResult ? Boolean.FALSE : null;
        }
        if (!(sourceDeclaration instanceof consulo.dotnet.psi.DotNetGenericParameterListOwner owner)) {
            return null;
        }
        DotNetGenericExtractor sourceExtractor = sourceResult.getGenericExtractor();
        DotNetGenericExtractor targetExtractor = targetResult.getGenericExtractor();
        for (DotNetGenericParameter parameter : owner.getGenericParameters()) {
            DotNetTypeRef sourceArgument = sourceExtractor.extract(parameter);
            DotNetTypeRef targetArgument = targetExtractor.extract(parameter);
            if (sourceArgument == null || targetArgument == null) {
                return null;
            }
            if (CSharpTypeUtil.isTypeEqual(sourceArgument, targetArgument)) {
                continue;
            }
            if (parameter.hasModifier(CSharpModifier.OUT)) {
                if (!isReferenceType(sourceArgument) || !isReferenceType(targetArgument) || !isReferenceCastable(sourceArgument, targetArgument, scope)) {
                    return false;
                }
            }
            else if (parameter.hasModifier(CSharpModifier.IN)) {
                if (!isReferenceType(sourceArgument) || !isReferenceType(targetArgument)) {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * User-defined explicit conversions (spec 10.5.5): an implicit or explicit operator declared in the source or the target type
     * (or their base types), from a type the source converts to by a standard explicit conversion, to a type that converts to
     * the target by a standard explicit conversion
     */
    @RequiredReadAction
    private static boolean isUserDefined(DotNetTypeRef source, DotNetTypeRef target, GlobalSearchScope scope) {
        for (DotNetTypeRef owner : new DotNetTypeRef[]{CSharpNullableTypeUtil.unbox(source), CSharpNullableTypeUtil.unbox(target)}) {
            DotNetTypeResolveResult result = owner.resolve();
            if (!(result.getElement() instanceof DotNetTypeDeclaration typeDeclaration)) {
                continue;
            }
            CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope, typeDeclaration);
            for (CSharpCastType castType : CSharpCastType.values()) {
                CSharpElementGroup<CSharpConversionMethodDeclaration> group = context.findConversionMethodGroup(castType, true);
                if (group == null) {
                    continue;
                }
                for (CSharpConversionMethodDeclaration declaration : group.getElements()) {
                    CSharpConversionMethodDeclaration extracted = GenericUnwrapTool.extract(declaration, result.getGenericExtractor());
                    DotNetTypeRef parameter = ArrayUtil.getFirstElement(extracted.getParameterTypeRefs());
                    if (parameter == null) {
                        continue;
                    }
                    if (isCastable(source, parameter, scope, false) && isCastable(extracted.getReturnTypeRef(), target, scope, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @RequiredReadAction
    private static DotNetTypeRef singleTypeArgument(DotNetTypeResolveResult result, PsiElement element) {
        if (!(element instanceof DotNetTypeDeclaration typeDeclaration) || !typeDeclaration.isInterface()) {
            return null;
        }
        DotNetGenericParameter[] parameters = typeDeclaration.getGenericParameters();
        if (parameters.length != 1) {
            return null;
        }
        return result.getGenericExtractor().extract(parameters[0]);
    }

    @RequiredReadAction
    private static boolean isEnum(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration typeDeclaration && typeDeclaration.isEnum();
    }

    @RequiredReadAction
    private static boolean isNumeric(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration typeDeclaration
            && ourEnumConvertible.contains(typeDeclaration.getVmQName());
    }

    @RequiredReadAction
    private static boolean isValueType(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration typeDeclaration && (typeDeclaration.isStruct() || typeDeclaration.isEnum());
    }

    /**
     * @return true for classes, interfaces, delegates and arrays; a type parameter only with a reference type constraint
     */
    @RequiredReadAction
    private static boolean isReferenceType(DotNetTypeRef typeRef) {
        if (typeRef instanceof CSharpArrayTypeRef) {
            return true;
        }
        DotNetTypeResolveResult result = typeRef.resolve();
        if (result instanceof CSharpLambdaResolveResult) {
            return true;
        }
        PsiElement element = result.getElement();
        if (element instanceof DotNetTypeDeclaration typeDeclaration) {
            return !typeDeclaration.isStruct() && !typeDeclaration.isEnum();
        }
        return false;
    }

    private static boolean isInterface(PsiElement element) {
        return element instanceof DotNetTypeDeclaration typeDeclaration && typeDeclaration.isInterface();
    }

    private static boolean isClass(DotNetTypeDeclaration typeDeclaration) {
        return !typeDeclaration.isInterface() && !typeDeclaration.isStruct() && !typeDeclaration.isEnum();
    }

    @RequiredReadAction
    private static boolean isSealed(DotNetTypeDeclaration typeDeclaration) {
        return typeDeclaration.hasModifier(CSharpModifier.SEALED) || typeDeclaration.hasModifier(CSharpModifier.STATIC);
    }
}
