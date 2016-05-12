package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class OperatorsProvider implements CSharpAdditionalMemberProvider
{
	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@NotNull DotNetElement element, @NotNull DotNetGenericExtractor extractor, @NotNull Consumer<PsiElement> consumer)
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
				selfTypeRef = new CSharpLambdaTypeRef(element, methodDeclaration, extractor);
			}
			else
			{
				selfTypeRef = new CSharpTypeRefByTypeDeclaration(typeDeclaration, extractor);
			}

			buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myTypeOperators.get(typeDeclaration.getVmQName()), consumer);

			if(typeDeclaration.isEnum())
			{
				buildOperators(project, resolveScope, selfTypeRef, element, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
			}

			if(DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName()))
			{
				buildNullableOperators(project, resolveScope, selfTypeRef, typeDeclaration, extractor, consumer);
			}

			if(methodDeclaration != null)
			{
				buildOperatorsForDelegates(consumer, project, typeDeclaration, selfTypeRef);
			}
		}
	}

	@NotNull
	@Override
	public Target getTarget()
	{
		return Target.OPERATOR_METHOD;
	}

	private static void buildOperatorsForDelegates(Consumer<PsiElement> consumer, Project project, CSharpTypeDeclaration typeDeclaration, DotNetTypeRef selfTypeRef)
	{
		for(IElementType elementType : new IElementType[]{
				CSharpTokens.PLUS,
				CSharpTokens.MINUS
		})
		{
			CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
			builder.setOperator(elementType);

			builder.withParent(typeDeclaration);

			builder.withReturnType(selfTypeRef);

			for(int i = 0; i < 2; i++)
			{
				CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
				parameterBuilder.withName("p" + i);
				parameterBuilder.withTypeRef(selfTypeRef);
				builder.addParameter(parameterBuilder);
			}

			consumer.consume(builder);
		}
	}

	@NotNull
	@RequiredReadAction
	private DotNetElement[] buildNullableOperators(@NotNull Project project,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull DotNetTypeRef selfTypeRef,
			@NotNull CSharpTypeDeclaration typeDeclaration,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull Consumer<PsiElement> consumer)
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

		buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myTypeOperators.get(forAddOperatorsElement.getVmQName()), consumer);

		if(forAddOperatorsElement.isEnum())
		{
			buildOperators(project, resolveScope, selfTypeRef, forAddOperatorsElement, OperatorStubsLoader.INSTANCE.myEnumOperators, consumer);
		}
		return ContainerUtil.toArray(elements, DotNetElement.ARRAY_FACTORY);
	}

	private static void buildOperators(@NotNull Project project,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull DotNetTypeRef selfTypeRef,
			@NotNull DotNetElement parent,
			@NotNull Collection<OperatorStubsLoader.Operator> operators,
			@NotNull Consumer<PsiElement> consumer)
	{
		if(operators.isEmpty())
		{
			return;
		}

		for(OperatorStubsLoader.Operator operator : operators)
		{
			if(operator.myOperatorToken == CSharpTokens.IMPLICIT_KEYWORD || operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD)
			{
				// ignore see ConversionMethodsProvider
			}
			else
			{
				CSharpLightMethodDeclarationBuilder temp = new CSharpLightMethodDeclarationBuilder(project);
				temp.setOperator(operator.myOperatorToken);

				temp.withParent(parent);

				temp.withReturnType(operator.myReturnTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, operator.myReturnTypeRef));

				int i = 0;
				for(OperatorStubsLoader.Operator.Parameter parameter : operator.myParameterTypes)
				{
					CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
					parameterBuilder.withName("p" + i);
					parameterBuilder.withTypeRef(parameter.myTypeRef == null ? selfTypeRef : new CSharpTypeRefByQName(project, resolveScope, parameter.myTypeRef));

					temp.addParameter(parameterBuilder);
					i++;
				}

				consumer.consume(temp);
			}
		}
	}
}
