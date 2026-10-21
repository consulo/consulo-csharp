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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.light.CSharpLightCallArgument;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.*;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.operatorResolving.ImplicitCastInfo;
import consulo.csharp.lang.impl.psi.source.resolve.operatorResolving.ImplicitOperatorArgumentAsCallArgumentWrapper;
import consulo.csharp.lang.impl.psi.source.resolve.operatorResolving.NumericPromotion;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpOperatorNameHelper;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.resolve.OperatorByTokenSelector;
import consulo.document.util.TextRange;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.resolve.DotNetPointerTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.*;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.ObjectUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 12.03.14
 */
public class CSharpOperatorReferenceImpl extends CSharpElementImpl implements PsiReference, PsiPolyVariantReference, CSharpCallArgumentListOwner, CSharpQualifiedNonReference {
    private static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpOperatorReferenceImpl> {
        private static final OurResolver INSTANCE = new OurResolver();

        @Override
        @RequiredReadAction
        public ResolveResult[] resolve(CSharpOperatorReferenceImpl reference, boolean incompleteCode) {
            if (incompleteCode) {
                return multiResolveImpl(reference);
            }
            else {
                ResolveResult[] resolveResults = multiResolveImpl(reference);

                List<ResolveResult> filter = new SmartList<>();
                for (ResolveResult resolveResult : resolveResults) {
                    if (resolveResult.isValidResult()) {
                        filter.add(resolveResult);
                    }
                }
                return filter.toArray(ResolveResult.ARRAY_FACTORY);
            }
        }

        @RequiredReadAction
        private ResolveResult[] multiResolveImpl(CSharpOperatorReferenceImpl reference) {
            Object o = reference.resolveImpl();

            List<ResolveResult> elements = new SmartList<>();
            if (o instanceof MethodResolveResult[]) {
                MethodResolveResult[] array = (MethodResolveResult[]) o;
                ContainerUtil.addAll(elements, array);
            }
            else if (o instanceof PsiElement) {
                elements.add(new CSharpResolveResult((PsiElement) o));
            }
            else if (o instanceof DotNetTypeRef) {
                elements.add(new StubElementResolveResult(reference, true, (DotNetTypeRef) o));
            }

            return elements.toArray(ResolveResult.ARRAY_FACTORY);
        }
    }

    private static final TokenSet ourMergeSet = TokenSet.orSet(CSharpTokenSets.OVERLOADING_OPERATORS, CSharpTokenSets.ASSIGNMENT_OPERATORS, TokenSet.create(CSharpTokens.ANDAND, CSharpTokens.OROR));

    private static final String[] ourPointerArgumentTypes = new String[]{
        DotNetTypes.System.SByte,
        DotNetTypes.System.Byte,
        DotNetTypes.System.Int16,
        DotNetTypes.System.UInt16,
        DotNetTypes.System.Int32,
        DotNetTypes.System.UInt32,
        DotNetTypes.System.Int64,
        DotNetTypes.System.UInt64,
    };

    public static final Map<IElementType, IElementType> ourAssignmentOperatorMap = new HashMap<>();

    static {
        ourAssignmentOperatorMap.put(CSharpTokens.MULEQ, CSharpTokens.MUL);
        ourAssignmentOperatorMap.put(CSharpTokens.PERCEQ, CSharpTokens.PERC);
        ourAssignmentOperatorMap.put(CSharpTokens.PLUSEQ, CSharpTokens.PLUS);
        ourAssignmentOperatorMap.put(CSharpTokens.MINUSEQ, CSharpTokens.MINUS);
        ourAssignmentOperatorMap.put(CSharpTokens.DIVEQ, CSharpTokens.DIV);
        ourAssignmentOperatorMap.put(CSharpTokens.GTEQ, CSharpTokens.GT);
        ourAssignmentOperatorMap.put(CSharpTokens.LTEQ, CSharpTokens.LT);
        ourAssignmentOperatorMap.put(CSharpTokens.GTGTEQ, CSharpTokens.GTGT);
        ourAssignmentOperatorMap.put(CSharpTokens.LTLTEQ, CSharpTokens.LTLT);
        ourAssignmentOperatorMap.put(CSharpTokens.ANDEQ, CSharpTokens.AND);
        ourAssignmentOperatorMap.put(CSharpTokens.OREQ, CSharpTokens.OR);
        ourAssignmentOperatorMap.put(CSharpTokens.XOREQ, CSharpTokens.XOR);
    }

    public CSharpOperatorReferenceImpl(IElementType elementType) {
        super(elementType);
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitOperatorReference(this);
    }

    @RequiredReadAction
    @Override
    public PsiElement getElement() {
        return this;
    }

    @Override
    @RequiredReadAction
    public TextRange getRangeInElement() {
        PsiElement operator = getOperatorElement();
        return new TextRange(0, operator.getTextLength());
    }

    @RequiredReadAction
    public PsiElement getOperatorElement() {
        return findNotNullChildByFilter(ourMergeSet);
    }

    @RequiredReadAction
    public IElementType getOperatorElementType() {
        return PsiUtilCore.getElementType(getOperatorElement());
    }

    @RequiredReadAction
    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return CSharpResolveUtil.findFirstValidElement(resolveResults);
    }

    @RequiredReadAction
    private Object resolveImpl() {
        final IElementType temp = getOperatorElementType();
        final IElementType elementType = ObjectUtil.notNull(ourAssignmentOperatorMap.get(temp), temp);
        boolean isAssignmentOperator = ourAssignmentOperatorMap.containsKey(temp);

        PsiElement parent = getParent();

        if (parent instanceof CSharpPrefixExpressionImpl) {
            if (elementType == CSharpTokens.AND) {
                DotNetExpression dotNetExpression = ArrayUtil2.safeGet(getParameterExpressions(), 0);
                if (dotNetExpression == null) {
                    return DotNetTypeRef.ERROR_TYPE;
                }
                return new CSharpPointerTypeRef(dotNetExpression.toTypeRef(true));
            }
            else if (elementType == CSharpTokens.MUL) {
                DotNetExpression dotNetExpression = ArrayUtil2.safeGet(getParameterExpressions(), 0);
                if (dotNetExpression == null) {
                    return DotNetTypeRef.ERROR_TYPE;
                }
                DotNetTypeRef expressionTypeRef = dotNetExpression.toTypeRef(true);
                if (expressionTypeRef instanceof DotNetPointerTypeRef) {
                    return ((DotNetPointerTypeRef) expressionTypeRef).getInnerTypeRef();
                }
                else {
                    return DotNetTypeRef.ERROR_TYPE;
                }
            }
        }

        if (parent instanceof CSharpPostfixExpressionImpl) {
            if (elementType == CSharpTokens.PLUSPLUS || elementType == CSharpTokens.MINUSMINUS) {
                DotNetExpression expression = ArrayUtil2.safeGet(getParameterExpressions(), 0);
                if (expression == null) {
                    return DotNetTypeRef.ERROR_TYPE;
                }
                DotNetTypeRef expressionTypeRef = expression.toTypeRef(true);
                if (expressionTypeRef instanceof DotNetPointerTypeRef) {
                    return expressionTypeRef;
                }
            }
        }

        if (parent instanceof CSharpExpressionWithOperatorImpl) {
            if (elementType == CSharpTokenSets.OROR || elementType == CSharpTokens.ANDAND) {
                return new CSharpTypeRefByQName(this, DotNetTypes.System.Boolean);
            }

            DotNetExpression[] parameterExpressions = getParameterExpressions();
            if (elementType == CSharpTokenSets.EQ) {
                if (parameterExpressions.length > 0) {
                    return parameterExpressions[0].toTypeRef(false);
                }
                return new CSharpTypeRefByQName(this, DotNetTypes.System.Void);
            }

            MethodResolveResult[] predefined = resolvePredefinedNumericOperator(elementType, parameterExpressions);
            if (predefined != null) {
                return predefined;
            }

            final Set<MethodResolveResult> resolveResults = new LinkedHashSet<>();

            for (final DotNetExpression dotNetExpression : parameterExpressions) {
                final DotNetTypeRef expressionTypeRef = dotNetExpression.toTypeRef(true);
                if (expressionTypeRef == DotNetTypeRef.UNKNOWN_TYPE) {
                    return new MethodResolveResult[]{MethodResolveResult.createResult(MethodResolvePriorityInfo.TOP, this, CSharpUndefinedResolveResult.INSTANCE)};
                }

                resolveUserDefinedOperators(elementType, expressionTypeRef, expressionTypeRef, resolveResults, null);

                processImplicitCasts(expressionTypeRef, parent, new Consumer<DotNetTypeRef>() {
                    @Override
                    @RequiredReadAction
                    public void accept(DotNetTypeRef implicitTypeRef) {
                        resolveUserDefinedOperators(elementType, expressionTypeRef, implicitTypeRef, resolveResults, dotNetExpression);
                    }
                });
            }

            // += -= and others have some hack for nullable types
            // A? + A is not work - but A? += A work
            if (isAssignmentOperator && parameterExpressions.length > 0) {
                DotNetExpression parameterExpression = parameterExpressions[0];
                DotNetTypeRef expressionTypeRef = parameterExpression.toTypeRef(true);
                DotNetTypeRef unboxTypeRef = CSharpNullableTypeUtil.unbox(expressionTypeRef);

                // we have extracted type
                if (unboxTypeRef != expressionTypeRef) {
                    resolveUserDefinedOperators(elementType, expressionTypeRef, unboxTypeRef, resolveResults, parameterExpression);
                }
            }

            MethodResolveResult[] results = ContainerUtil.toArray(resolveResults, MethodResolveResult.ARRAY_FACTORY);
            Arrays.sort(results, WeightUtil.ourComparator);
            return dropSameOperators(results);
        }

        return null;
    }

    /**
     * Operands of primitive numeric types are converted to the type numeric promotion gives, and the operator of that
     * type is the one - no search over every implicit conversion, which ties when more than one type has the operator.
     * A nullable operand makes it the lifted operator of that type.
     *
     * @return null if the operands are not numeric, empty if C# has no operator for them, else the operator
     */
    @RequiredReadAction
    private MethodResolveResult @Nullable [] resolvePredefinedNumericOperator(IElementType elementType, DotNetExpression[] parameterExpressions) {
        DotNetTypeRef[] operandTypeRefs = new DotNetTypeRef[parameterExpressions.length];
        DotNetTypeRef[] underlyingTypeRefs = new DotNetTypeRef[parameterExpressions.length];
        boolean lifted = false;
        for (int i = 0; i < parameterExpressions.length; i++) {
            operandTypeRefs[i] = parameterExpressions[i].toTypeRef(true);
            underlyingTypeRefs[i] = CSharpNullableTypeUtil.unbox(operandTypeRefs[i]);
            lifted |= underlyingTypeRefs[i] != operandTypeRefs[i];
        }

        String[] parameterTypes = NumericPromotion.promote(elementType, underlyingTypeRefs);
        if (parameterTypes == null) {
            return null;
        }
        if (parameterTypes.length == 0) {
            return MethodResolveResult.EMPTY_ARRAY;
        }

        DotNetTypeRef ownerTypeRef = parameterTypeRef(parameterTypes[0], lifted);
        Set<PsiElement> psiElements = resolveElements(elementType, ownerTypeRef);
        if (psiElements == null) {
            return null;
        }

        CSharpCallArgument[] arguments = new CSharpCallArgument[parameterExpressions.length];
        for (int i = 0; i < parameterExpressions.length; i++) {
            DotNetTypeRef parameterTypeRef = parameterTypeRef(parameterTypes[i], lifted);
            ImplicitOperatorArgumentAsCallArgumentWrapper wrapper = new ImplicitOperatorArgumentAsCallArgumentWrapper(parameterExpressions[i], parameterTypeRef);
            wrapper.putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(operandTypeRefs[i], parameterTypeRef));
            arguments[i] = wrapper;
        }

        List<DotNetLikeMethodDeclaration> methods = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
        List<MethodResolveResult> results = new ArrayList<>();
        for (DotNetLikeMethodDeclaration method : methods) {
            if (method instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null) {
                continue;
            }

            // the predefined operator is the promoted type's own - not object == from its base types; lifted ones live in Nullable<T>
            String ownerVmQName = lifted ? DotNetTypes.System.Nullable$1 : parameterTypes[0];
            if (!(method.getParent() instanceof DotNetTypeDeclaration owner) || !ownerVmQName.equals(owner.getVmQName())) {
                continue;
            }

            MethodResolvePriorityInfo calc = NCallArgumentBuilder.calc(arguments, method, getResolveScope(), true);
            if (calc.isValidResult()) {
                results.add(MethodResolveResult.createResult(calc, method, null));
            }
        }

        if (results.isEmpty()) {
            return null;
        }

        MethodResolveResult[] array = results.toArray(MethodResolveResult.ARRAY_FACTORY);
        Arrays.sort(array, WeightUtil.ourComparator);
        return array;
    }

    /**
     * The same operator is found through more than one operand - a lifted one through the nullable operand and through the
     * implicit conversion of the other to nullable - as different light elements; the best weighted valid one stays.
     */
    @RequiredReadAction
    private static MethodResolveResult[] dropSameOperators(MethodResolveResult[] sortedResults) {
        Map<String, MethodResolveResult> unique = new LinkedHashMap<>();
        for (MethodResolveResult result : sortedResults) {
            PsiElement element = result.getElement();
            if (!(element instanceof DotNetLikeMethodDeclaration method)) {
                unique.put(String.valueOf(System.identityHashCode(result)), result);
                continue;
            }

            StringBuilder key = new StringBuilder();
            PsiElement parent = method.getParent();
            key.append(parent instanceof DotNetTypeDeclaration owner ? owner.getVmQName() : String.valueOf(System.identityHashCode(parent)));
            key.append(' ').append(method.getName());
            for (DotNetTypeRef parameterTypeRef : method.getParameterTypeRefs()) {
                key.append(' ').append(CSharpTypeRefPresentationUtil.buildText(parameterTypeRef, CSharpTypeRefPresentationUtil.QUALIFIED_NAME));
            }
            // a valid result wins over an invalid one of the same operator
            unique.merge(key.toString(), result, (old, it) -> !old.isValidResult() && it.isValidResult() ? it : old);
        }
        return unique.size() == sortedResults.length ? sortedResults : unique.values().toArray(MethodResolveResult.ARRAY_FACTORY);
    }

    @RequiredReadAction
    private DotNetTypeRef parameterTypeRef(String vmQName, boolean lifted) {
        DotNetTypeRef typeRef = new CSharpTypeRefByQName(this, vmQName);
        return lifted ? CSharpNullableTypeUtil.boxIfNeed(typeRef) : typeRef;
    }

    @RequiredReadAction
    private void processImplicitCasts(DotNetTypeRef expressionTypeRef, PsiElement parent, Consumer<DotNetTypeRef> consumer) {
        for (DotNetExpression dotNetExpression : ((CSharpExpressionWithOperatorImpl) parent).getParameterExpressions()) {
            List<DotNetTypeRef> implicitOrExplicitTypeRefs = CSharpTypeUtil.getImplicitOrExplicitTypeRefs(dotNetExpression.toTypeRef(true), expressionTypeRef, CSharpCastType.IMPLICIT, this);

            for (DotNetTypeRef implicitOrExplicitTypeRef : implicitOrExplicitTypeRefs) {
                consumer.accept(implicitOrExplicitTypeRef);
            }
        }
    }

    @RequiredReadAction
    public DotNetTypeRef resolveToTypeRef() {
        ResolveResult[] resolveResults = multiResolve(true);
        if (resolveResults.length == 0) {
            return DotNetTypeRef.ERROR_TYPE;
        }

        ResolveResult resolveResult = CSharpResolveUtil.findValidOrFirstMaybeResult(resolveResults);

        if (resolveResult instanceof StubElementResolveResult) {
            return ((StubElementResolveResult) resolveResult).getTypeRef();
        }

        assert resolveResult != null;

        PsiElement element = resolveResult.getElement();

        if (element instanceof CSharpSimpleLikeMethodAsElement) {
            return ((CSharpSimpleLikeMethodAsElement) element).getReturnTypeRef();
        }
        else {
            return CSharpReferenceExpressionImplUtil.toTypeRef(getResolveScope(), element);
        }
    }

    @RequiredReadAction
    public void resolveUserDefinedOperators(IElementType elementType,
                                            DotNetTypeRef originalTypeRef,
                                            DotNetTypeRef typeRef,
                                            Set<MethodResolveResult> last,
                                            @Nullable DotNetExpression implicitExpression) {
        Set<PsiElement> psiElements = resolveElements(elementType, typeRef);
        if (psiElements == null) {
            return;
        }

        CSharpCallArgument[] arguments = getCallArguments(originalTypeRef, implicitExpression, typeRef);

        List<DotNetLikeMethodDeclaration> elements = CSharpResolveUtil.mergeGroupsToIterable(psiElements);
        for (DotNetLikeMethodDeclaration psiElement : elements) {
            // an explicit interface implementation (static int IAdditionOperators<int, int, int>.operator +) is not reachable by the operator
            if (psiElement instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null) {
                continue;
            }

            MethodResolvePriorityInfo calc = NCallArgumentBuilder.calc(arguments, psiElement, getResolveScope(), true);
            if (implicitExpression != null) {
                calc = calc.dupWithResult(-3000000);
            }

            last.add(MethodResolveResult.createResult(calc, psiElement, null));
        }
    }

    @Nullable
    @RequiredReadAction
    private Set<PsiElement> resolveElements(IElementType elementType, DotNetTypeRef typeRef) {
        if (typeRef instanceof DotNetPointerTypeRef) {
            if (elementType != CSharpTokens.PLUS && elementType != CSharpTokens.MINUS) {
                return Collections.emptySet();
            }
            Set<PsiElement> elements = new HashSet<>();
            for (String pointerArgumentType : ourPointerArgumentTypes) {
                elements.add(buildOperatorForPointer(elementType, typeRef, pointerArgumentType));
            }
            return elements;
        }
        else {
            DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

            PsiElement element = typeResolveResult.getElement();
            if (element == null) {
                return null;
            }

            AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();

            // operators of primitive types are the language ones (see operatorStubs.xml) on every SDK - the generic math
            // interfaces of net7+ declare them as static abstract members, which are no candidates for a concrete type
            MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(
                this,
                psiElementProcessor,
                new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP},
                OverrideProcessor.ALWAYS_TRUE
            );

            ResolveState state = ResolveState.initial();
            state = state.put(CSharpResolveUtil.SELECTOR, new OperatorByTokenSelector(elementType));
            state = state.put(CSharpResolveUtil.EXTRACTOR, typeResolveResult.getGenericExtractor());
            CSharpResolveUtil.walkChildren(processor, element, false, true, state);

            Set<PsiElement> psiElements = psiElementProcessor.getElements();
            if (psiElements.isEmpty()) {
                return null;
            }
            return psiElements;
        }
    }

    @RequiredReadAction
    private PsiElement buildOperatorForPointer(IElementType operatorElementType, DotNetTypeRef leftTypeRef, String typeVmQName) {
        Project project = getProject();
        CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
        builder.withReturnType(leftTypeRef);
        builder.addParameter(new CSharpLightParameterBuilder(project).withName("p0").withTypeRef(leftTypeRef));
        builder.addParameter(new CSharpLightParameterBuilder(project).withName("p0").withTypeRef(new CSharpTypeRefByQName(this, typeVmQName)));
        builder.setOperator(operatorElementType);
        return builder;
    }

    @Override
    @RequiredReadAction
    public String getCanonicalText() {
        String operatorName = CSharpOperatorNameHelper.getOperatorName(getOperatorElementType());
        assert operatorName != null : getOperatorElementType();
        return operatorName;
    }

    @RequiredWriteAction
    @Override
    public PsiElement handleElementRename(String s) throws IncorrectOperationException {
        return null;
    }

    @RequiredWriteAction
    @Override
    public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
        return null;
    }

    @RequiredReadAction
    @Override
    public boolean isReferenceTo(PsiElement element) {
        return resolve() == element;
    }

    @RequiredReadAction
    @Override
    public boolean isSoft() {
        return resolve() == this;
    }

    public DotNetTypeRef[] getTypeRefs() {
        DotNetExpression[] parameterExpressions = getParameterExpressions();
        DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameterExpressions.length];
        for (int i = 0; i < parameterExpressions.length; i++) {
            DotNetExpression parameterExpression = parameterExpressions[i];
            typeRefs[i] = parameterExpression.toTypeRef(true);
        }
        return typeRefs;
    }

    @Nullable
    @Override
    public CSharpCallArgumentList getParameterList() {
        return null;
    }

    @Nullable
    @Override
    @RequiredReadAction
    public PsiElement resolveToCallable() {
        return resolve();
    }

    @RequiredReadAction
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        if (CSharpReferenceExpressionImplUtil.isCacheDisabled(this)) {
            return OurResolver.INSTANCE.resolve(this, incompleteCode);
        }
        return ResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, false, incompleteCode);
    }

    @Override
    public DotNetExpression[] getParameterExpressions() {
        PsiElement parent = getParent();
        if (parent instanceof CSharpExpressionWithOperatorImpl) {
            return ((CSharpExpressionWithOperatorImpl) parent).getParameterExpressions();
        }
        return DotNetExpression.EMPTY_ARRAY;
    }

    @Override
    public CSharpCallArgument[] getCallArguments() {
        return getCallArguments(null, null, null);
    }

    public CSharpCallArgument[] getCallArguments(DotNetTypeRef originalTypeRef, DotNetExpression wrapExpression, DotNetTypeRef toTypeRef) {
        DotNetExpression[] parameterExpressions = getParameterExpressions();
        CSharpCallArgument[] array = new CSharpCallArgument[parameterExpressions.length];
        for (int i = 0; i < parameterExpressions.length; i++) {
            DotNetExpression parameterExpression = parameterExpressions[i];
            if (parameterExpression == wrapExpression) {
                ImplicitOperatorArgumentAsCallArgumentWrapper wrapper = new ImplicitOperatorArgumentAsCallArgumentWrapper(wrapExpression, toTypeRef);

                wrapper.putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(originalTypeRef, toTypeRef));
                array[i] = wrapper;
            }
            else {
                array[i] = new CSharpLightCallArgument(parameterExpression);
            }
        }
        return array;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getReferenceName() {
        throw new UnsupportedOperationException();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getReferenceNameWithAt() {
        throw new UnsupportedOperationException();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public PsiElement getQualifier() {
        return null;
    }
}
