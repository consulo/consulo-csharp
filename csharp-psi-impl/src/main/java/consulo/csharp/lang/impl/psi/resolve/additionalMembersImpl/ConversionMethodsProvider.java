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
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightConversionMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightLikeMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpStaticTypeRef;
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
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
@ExtensionImpl
public class ConversionMethodsProvider implements CSharpAdditionalMemberProvider
{
	private record ConversionMethodEqualObject(DotNetTypeDeclaration parent, OperatorStubsLoader.Operator operator)
	{
	}

	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@Nonnull DotNetElement element, @Nonnull DotNetGenericExtractor extractor, @Nonnull Consumer<PsiElement> consumer)
	{
		if(element instanceof CSharpTypeDeclaration typeDeclaration)
		{
			Project project = element.getProject();
			GlobalSearchScope resolveScope = element.getResolveScope();

			CSharpMethodDeclaration methodDeclaration = typeDeclaration.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
			DotNetTypeRef selfTypeRef;
			if(methodDeclaration != null)
			{
				selfTypeRef = new CSharpLambdaTypeRef(element.getProject(), element.getResolveScope(), methodDeclaration, extractor);
			}
			else
			{
				selfTypeRef = new CSharpTypeRefByTypeDeclaration(typeDeclaration, extractor);
			}

			buildConversionMethods(project, resolveScope, selfTypeRef, typeDeclaration, OperatorStubsLoader.INSTANCE.myTypeOperators.get(typeDeclaration.getVmQName()), consumer);

			if(typeDeclaration.isEnum())
			{
				buildConversionMethods(project, resolveScope, selfTypeRef, typeDeclaration, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
			}

			if(DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName()))
			{
				buildNullableConversionMethods(project, selfTypeRef, resolveScope, typeDeclaration, extractor, consumer);
			}
		}
	}

	@Nonnull
	@Override
	public Target getTarget()
	{
		return Target.CONVERSION_METHOD;
	}

	@RequiredReadAction
	private void buildNullableConversionMethods(Project project,
												@Nonnull DotNetTypeRef selfTypeRef,
												GlobalSearchScope resolveScope,
												@Nonnull CSharpTypeDeclaration typeDeclaration,
												@Nonnull DotNetGenericExtractor extractor,
												@Nonnull Consumer<PsiElement> consumer)
	{
		DotNetGenericParameter[] genericParameters = typeDeclaration.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return;
		}
		DotNetGenericParameter genericParameter = genericParameters[0];

		DotNetTypeRef extract = extractor.extract(genericParameter);
		if(extract == null)
		{
			return;
		}

		DotNetTypeResolveResult typeResolveResult = extract.resolve();
		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return;
		}

		DotNetTypeDeclaration forAddOperatorsElement = (DotNetTypeDeclaration) typeResolveResultElement;

		buildConversionMethods(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()), consumer);

		if(forAddOperatorsElement.isEnum())
		{
			buildConversionMethods(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
		}
	}

	private static void buildConversionMethods(@Nonnull Project project,
											   GlobalSearchScope resolveScope,
											   @Nonnull DotNetTypeRef selfTypeRef,
											   @Nonnull DotNetTypeDeclaration parent,
											   @Nonnull Collection<OperatorStubsLoader.Operator> operators,
											   @Nonnull Consumer<PsiElement> consumer)
	{
		if(operators.isEmpty())
		{
			return;
		}

		for(OperatorStubsLoader.Operator operator : operators)
		{
			if(operator.myOperatorToken == CSharpTokens.IMPLICIT_KEYWORD || operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD)
			{
				CSharpStaticTypeRef staticTypeRef = CSharpStaticTypeRef.IMPLICIT;
				if(operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD)
				{
					staticTypeRef = CSharpStaticTypeRef.EXPLICIT;
				}
				CSharpLightLikeMethodDeclarationBuilder builder = new CSharpLightConversionMethodDeclarationBuilder(project, staticTypeRef);

				builder.withParent(parent);

				builder.withHashAndEqualObject(new ConversionMethodEqualObject(parent, operator));

				builder.withReturnType(operator.myReturnTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, operator.myReturnTypeRef));

				int i = 0;
				for(OperatorStubsLoader.Operator.Parameter parameter : operator.myParameterTypes)
				{
					CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
					parameterBuilder.withName("p" + i);
					parameterBuilder.withTypeRef(parameter.myTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, parameter.myTypeRef));

					builder.addParameter(parameterBuilder);
					i++;
				}

				consumer.accept(builder);
			}
		}
	}
}
