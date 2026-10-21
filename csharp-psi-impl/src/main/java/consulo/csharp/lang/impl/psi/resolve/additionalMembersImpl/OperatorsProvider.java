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

package consulo.csharp.lang.impl.psi.resolve.additionalMembersImpl;

import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.msil.MsilToCSharpUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 26.10.14
 */
@ExtensionImpl
public class OperatorsProvider implements CSharpAdditionalMemberProvider {
    private static final TokenSet ourComparisonOperators = TokenSet.create(CSharpTokens.EQEQ, CSharpTokens.NTEQ, CSharpTokens.LT, CSharpTokens.GT,
        CSharpTokens.LTEQ, CSharpTokens.GTEQ);

    @RequiredReadAction
    @Override
    public void processAdditionalMembers(DotNetElement element,
                                         DotNetGenericExtractor extractor,
                                         Consumer<PsiElement> consumer) {
        if (element instanceof CSharpTypeDeclaration typeDeclaration) {
            Project project = element.getProject();
            GlobalSearchScope resolveScope = element.getResolveScope();

            CSharpMethodDeclaration methodDeclaration = typeDeclaration.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
            DotNetTypeRef selfTypeRef;
            if (methodDeclaration != null) {
                selfTypeRef = new CSharpLambdaTypeRef(element.getProject(), element.getResolveScope(), methodDeclaration, extractor);
            }
            else {
                selfTypeRef = new CSharpTypeRefByTypeDeclaration(typeDeclaration, extractor);
            }

            buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myTypeOperators.get(typeDeclaration.getVmQName()), false, consumer);

            if (typeDeclaration.isEnum()) {
                buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myEnumOperators, false, consumer);
            }

            if (DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName())) {
                buildNullableOperators(project, resolveScope, selfTypeRef, typeDeclaration, extractor, consumer);
            }

            if (methodDeclaration != null) {
                buildOperatorsForDelegates(consumer, project, typeDeclaration, selfTypeRef);
            }
        }
    }

    @Override
    public Target getTarget() {
        return Target.OPERATOR_METHOD;
    }

    private static void buildOperatorsForDelegates(Consumer<PsiElement> consumer, Project project, CSharpTypeDeclaration typeDeclaration, DotNetTypeRef selfTypeRef) {
        for (IElementType elementType : new IElementType[]{
            CSharpTokens.PLUS,
            CSharpTokens.MINUS
        }) {
            CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
            builder.setOperator(elementType);

            builder.withParent(typeDeclaration);

            builder.withReturnType(selfTypeRef);

            for (int i = 0; i < 2; i++) {
                CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
                parameterBuilder.withName("p" + i);
                parameterBuilder.withTypeRef(selfTypeRef);
                builder.addParameter(parameterBuilder);
            }

            consumer.accept(builder);
        }
    }

    @RequiredReadAction
    private DotNetElement[] buildNullableOperators(Project project,
                                                   GlobalSearchScope resolveScope,
                                                   DotNetTypeRef selfTypeRef,
                                                   CSharpTypeDeclaration typeDeclaration,
                                                   DotNetGenericExtractor extractor,
                                                   Consumer<PsiElement> consumer) {
        DotNetGenericParameter[] genericParameters = typeDeclaration.getGenericParameters();
        if (genericParameters.length == 0) {
            return DotNetElement.EMPTY_ARRAY;
        }
        DotNetGenericParameter genericParameter = genericParameters[0];

        DotNetTypeRef extract = extractor.extract(genericParameter);
        if (extract == null) {
            return DotNetElement.EMPTY_ARRAY;
        }

        DotNetTypeResolveResult typeResolveResult = extract.resolve();
        PsiElement typeResolveResultElement = typeResolveResult.getElement();
        if (!(typeResolveResultElement instanceof DotNetTypeDeclaration)) {
            return DotNetElement.EMPTY_ARRAY;
        }

        List<DotNetElement> elements = new ArrayList<>();

        DotNetTypeDeclaration forAddOperatorsElement = (DotNetTypeDeclaration) typeResolveResultElement;

        buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()), true, consumer);

        if (forAddOperatorsElement.isEnum()) {
            buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myEnumOperators, true, consumer);
        }
        else if (OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()).isEmpty()) {
            liftDeclaredOperators(project, selfTypeRef, forAddOperatorsElement, consumer);
        }
        return ContainerUtil.toArray(elements, DotNetElement.ARRAY_FACTORY);
    }

    /**
     * C# lifts the operators a struct declares itself - decimal ones, user ones - like the predefined ones of the
     * stubs: {@code decimal? + decimal?} for {@code decimal + decimal}. Comparisons stay bool.
     */
    @RequiredReadAction
    private static void liftDeclaredOperators(Project project, DotNetTypeRef selfTypeRef, DotNetTypeDeclaration underlying, Consumer<PsiElement> consumer) {
        if (!(MsilToCSharpUtil.wrap(underlying, null) instanceof DotNetTypeDeclaration declaration)) {
            return;
        }

        for (DotNetNamedElement member : declaration.getMembers()) {
            if (!(member instanceof CSharpMethodDeclaration method) || !method.isOperator() || method.getTypeForImplement() != null) {
                continue;
            }

            IElementType operatorToken = method.getOperatorElementType();
            if (operatorToken == null || operatorToken == CSharpTokens.IMPLICIT_KEYWORD || operatorToken == CSharpTokens.EXPLICIT_KEYWORD) {
                continue;
            }

            CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
            builder.setOperator(operatorToken);
            builder.withParent(underlying);
            builder.withReturnType(ourComparisonOperators.contains(operatorToken) ? method.getReturnTypeRef() : selfTypeRef);

            int i = 0;
            for (DotNetTypeRef parameterTypeRef : method.getParameterTypeRefs()) {
                CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
                parameterBuilder.withName("p" + i++);
                parameterBuilder.withTypeRef(CSharpNullableTypeUtil.boxIfNeed(parameterTypeRef));
                builder.addParameter(parameterBuilder);
            }

            consumer.accept(builder);
        }
    }

    @RequiredReadAction
    private static void buildOperators(Project project,
                                       GlobalSearchScope resolveScope,
                                       DotNetTypeRef selfTypeRef,
                                       DotNetElement parent,
                                       Collection<OperatorStubsLoader.Operator> operators,
                                       boolean lifted,
                                       Consumer<PsiElement> consumer) {
        if (operators.isEmpty()) {
            return;
        }

        for (OperatorStubsLoader.Operator operator : operators) {
            if (operator.myOperatorToken == CSharpTokens.IMPLICIT_KEYWORD || operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD) {
                // ignore see ConversionMethodsProvider
            }
            else {
                if (operator.myBlocker != null
                    && parent instanceof CSharpTypeDeclaration typeDeclaration
                    && typeDeclaration.isInheritor(operator.myBlocker, true)) {
                    continue;
                }

                CSharpLightMethodDeclarationBuilder temp = new CSharpLightMethodDeclarationBuilder(project);
                temp.setOperator(operator.myOperatorToken);

                temp.withParent(parent);

                temp.withReturnType(operator.myReturnTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, operator.myReturnTypeRef));

                int i = 0;
                for (OperatorStubsLoader.Operator.Parameter parameter : operator.myParameterTypes) {
                    CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
                    parameterBuilder.withName("p" + i);
                    DotNetTypeRef parameterTypeRef = selfTypeRef;
                    if (parameter.myTypeRef != null) {
                        parameterTypeRef = new CSharpTypeRefByQName(project, resolveScope, parameter.myTypeRef);
                        // the lifted operator lifts every operand - int? << int? for int << int
                        if (lifted) {
                            parameterTypeRef = CSharpNullableTypeUtil.boxIfNeed(parameterTypeRef);
                        }
                    }
                    parameterBuilder.withTypeRef(parameterTypeRef);

                    temp.addParameter(parameterBuilder);
                    i++;
                }

                consumer.accept(temp);
            }
        }
    }
}
