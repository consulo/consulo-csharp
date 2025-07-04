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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;

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
    @RequiredReadAction
    @Override
    public void processAdditionalMembers(@Nonnull DotNetElement element,
                                         @Nonnull DotNetGenericExtractor extractor,
                                         @Nonnull Consumer<PsiElement> consumer) {
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

            buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myTypeOperators.get(typeDeclaration.getVmQName()), consumer);

            if (typeDeclaration.isEnum()) {
                buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
            }

            if (DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName())) {
                buildNullableOperators(project, resolveScope, selfTypeRef, typeDeclaration, extractor, consumer);
            }

            if (methodDeclaration != null) {
                buildOperatorsForDelegates(consumer, project, typeDeclaration, selfTypeRef);
            }
        }
    }

    @Nonnull
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

    @Nonnull
    @RequiredReadAction
    private DotNetElement[] buildNullableOperators(@Nonnull Project project,
                                                   @Nonnull GlobalSearchScope resolveScope,
                                                   @Nonnull DotNetTypeRef selfTypeRef,
                                                   @Nonnull CSharpTypeDeclaration typeDeclaration,
                                                   @Nonnull DotNetGenericExtractor extractor,
                                                   @Nonnull Consumer<PsiElement> consumer) {
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

        buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()), consumer);

        if (forAddOperatorsElement.isEnum()) {
            buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
        }
        return ContainerUtil.toArray(elements, DotNetElement.ARRAY_FACTORY);
    }

    @RequiredReadAction
    private static void buildOperators(@Nonnull Project project,
                                       @Nonnull GlobalSearchScope resolveScope,
                                       @Nonnull DotNetTypeRef selfTypeRef,
                                       @Nonnull DotNetElement parent,
                                       @Nonnull Collection<OperatorStubsLoader.Operator> operators,
                                       @Nonnull Consumer<PsiElement> consumer) {
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
                    parameterBuilder.withTypeRef(parameter.myTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, parameter.myTypeRef));

                    temp.addParameter(parameterBuilder);
                    i++;
                }

                consumer.accept(temp);
            }
        }
    }
}
