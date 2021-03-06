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

package consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightConversionMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightLikeMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class ConversionMethodsProvider implements CSharpAdditionalMemberProvider
{
	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@Nonnull DotNetElement element, @Nonnull DotNetGenericExtractor extractor, @Nonnull Consumer<PsiElement> consumer)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			Project project = element.getProject();
			GlobalSearchScope resolveScope = element.getResolveScope();

			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;

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

			buildConversionMethods(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myTypeOperators.get(typeDeclaration.getVmQName()), consumer);

			if(typeDeclaration.isEnum())
			{
				buildConversionMethods(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
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

	@Nonnull
	@RequiredReadAction
	private DotNetElement[] buildNullableConversionMethods(Project project,
			@Nonnull DotNetTypeRef selfTypeRef,
			GlobalSearchScope resolveScope,
			@Nonnull CSharpTypeDeclaration typeDeclaration,
			@Nonnull DotNetGenericExtractor extractor,
			@Nonnull Consumer<PsiElement> consumer)
	{
		DotNetGenericParameter[] genericParameters = typeDeclaration.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return DotNetElement.EMPTY_ARRAY;
		}
		DotNetGenericParameter genericParameter = genericParameters[0];

		DotNetTypeRef extract = extractor.extract(genericParameter);
		if(extract == null)
		{
			return DotNetElement.EMPTY_ARRAY;
		}

		DotNetTypeResolveResult typeResolveResult = extract.resolve();
		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return DotNetElement.EMPTY_ARRAY;
		}

		List<DotNetElement> elements = new ArrayList<DotNetElement>();

		DotNetTypeDeclaration forAddOperatorsElement = (DotNetTypeDeclaration) typeResolveResultElement;

		buildConversionMethods(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()), consumer);

		if(forAddOperatorsElement.isEnum())
		{
			buildConversionMethods(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
		}
		return ContainerUtil.toArray(elements, DotNetElement.ARRAY_FACTORY);
	}

	private static void buildConversionMethods(@Nonnull Project project,
			GlobalSearchScope resolveScope,
			@Nonnull DotNetTypeRef selfTypeRef,
			@Nonnull DotNetElement parent,
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

				consumer.consume(builder);
			}
		}
	}
}
