package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConversionMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightLikeMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
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
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Consumer;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class OperatorsProvider implements CSharpAdditionalMemberProvider
{
	public static class Operator
	{
		public static class Parameter
		{
			private DotNetTypeRef myTypeRef;

			public Parameter(String type)
			{
				myTypeRef = type == null ? null : new CSharpTypeRefByQName(type);
			}
		}

		private final IElementType myOperatorToken;
		private final DotNetTypeRef myReturnTypeRef;
		private final List<Parameter> myParameterTypes = new ArrayList<Parameter>(5);

		public Operator(String name, String returnType)
		{
			Field declaredField = ReflectionUtil.getDeclaredField(CSharpTokens.class, name);
			assert declaredField != null;
			try
			{
				myOperatorToken = (IElementType) declaredField.get(null);
			}
			catch(IllegalAccessException e)
			{
				throw new Error();
			}
			myReturnTypeRef = returnType == null ? null : new CSharpTypeRefByQName(returnType);
		}
	}

	private MultiMap<String, Operator> myTypeOperators = new MultiMap<String, Operator>();

	private List<Operator> myEnumOperators = new ArrayList<Operator>();

	private OperatorsProvider()
	{
		try
		{
			Document document = JDOMUtil.loadDocument(getClass(), "/stub/operatorStubs.xml");
			for(Element e : document.getRootElement().getChildren())
			{
				Collection<Operator> list = null;
				if("type".equals(e.getName()))
				{
					String className = e.getAttributeValue("name");
					list = myTypeOperators.getModifiable(className);
				}
				else if("enum".equals(e.getName()))
				{
					list = myEnumOperators;
				}
				assert list != null;

				for(Element opElement : e.getChildren())
				{
					String operatorName = opElement.getAttributeValue("name");
					String returnType = opElement.getAttributeValue("type");

					Operator operator = new Operator(operatorName, returnType);

					for(Element parameterElement : opElement.getChildren())
					{
						String parameterType = parameterElement.getAttributeValue("type");
						operator.myParameterTypes.add(new Operator.Parameter(parameterType));
					}
					list.add(operator);
				}
			}
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@NotNull DotNetElement element,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull Consumer<DotNetElement> consumer)
	{
		Project project = element.getProject();

		if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;

			CSharpMethodDeclaration methodDeclaration = typeDeclaration.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
			DotNetTypeRef selfTypeRef;
			if(methodDeclaration != null)
			{
				selfTypeRef = new CSharpLambdaTypeRef(methodDeclaration, extractor);
			}
			else
			{
				selfTypeRef = new CSharpTypeRefByTypeDeclaration(typeDeclaration, extractor);
			}

			buildOperators(project, selfTypeRef, element, myTypeOperators.get(typeDeclaration.getVmQName()), consumer);
			if(typeDeclaration.isEnum())
			{
				buildOperators(project, selfTypeRef, element, myEnumOperators, consumer);
			}

			if(DotNetTypes.System.Nullable$1.equals(typeDeclaration.getVmQName()))
			{
				buildNullableOperators(selfTypeRef, typeDeclaration, extractor, consumer);
			}

			if(methodDeclaration != null)
			{
				buildOperatorsForDelegates(consumer, project, typeDeclaration, selfTypeRef);
			}
		}
	}

	private static void buildOperatorsForDelegates(Consumer<DotNetElement> consumer,
			Project project,
			CSharpTypeDeclaration typeDeclaration,
			DotNetTypeRef selfTypeRef)
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
	private DotNetElement[] buildNullableOperators(@NotNull DotNetTypeRef selfTypeRef,
			@NotNull CSharpTypeDeclaration typeDeclaration,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull Consumer<DotNetElement> consumer)
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

		DotNetTypeResolveResult typeResolveResult = extract.resolve(typeDeclaration);
		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return DotNetElement.EMPTY_ARRAY;
		}

		Project project = typeDeclaration.getProject();
		List<DotNetElement> elements = new ArrayList<DotNetElement>();

		DotNetTypeDeclaration forAddOperatorsElement = (DotNetTypeDeclaration) typeResolveResultElement;
		buildOperators(project, selfTypeRef, forAddOperatorsElement, myTypeOperators.get(forAddOperatorsElement.getVmQName()), consumer);

		if(forAddOperatorsElement.isEnum())
		{
			buildOperators(project, selfTypeRef, forAddOperatorsElement, myEnumOperators, consumer);
		}
		return ContainerUtil.toArray(elements, DotNetElement.ARRAY_FACTORY);
	}

	private static void buildOperators(@NotNull Project project,
			@NotNull DotNetTypeRef selfTypeRef,
			@NotNull DotNetElement parent,
			@NotNull Collection<Operator> operators,
			@NotNull Consumer<DotNetElement> consumer)
	{
		if(operators.isEmpty())
		{
			return;
		}

		for(Operator operator : operators)
		{
			CSharpLightLikeMethodDeclarationBuilder builder;
			if(operator.myOperatorToken == CSharpTokens.IMPLICIT_KEYWORD || operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD)
			{
				CSharpStaticTypeRef staticTypeRef = CSharpStaticTypeRef.IMPLICIT;
				if(operator.myOperatorToken == CSharpTokens.EXPLICIT_KEYWORD)
				{
					staticTypeRef = CSharpStaticTypeRef.EXPLICIT;
				}
				builder = new CSharpLightConversionMethodDeclarationBuilder(project, staticTypeRef);
			}
			else
			{
				CSharpLightMethodDeclarationBuilder temp = new CSharpLightMethodDeclarationBuilder(project);
				temp.setOperator(operator.myOperatorToken);

				builder = temp;
			}

			builder.withParent(parent);

			builder.withReturnType(operator.myReturnTypeRef == null ? selfTypeRef : operator.myReturnTypeRef);

			int i = 0;
			for(Operator.Parameter parameter : operator.myParameterTypes)
			{
				CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
				parameterBuilder.withName("p" + i);
				parameterBuilder.withTypeRef(parameter.myTypeRef == null ? selfTypeRef : parameter.myTypeRef);

				builder.addParameter(parameterBuilder);
				i++;
			}

			consumer.consume(builder);
		}
	}
}
