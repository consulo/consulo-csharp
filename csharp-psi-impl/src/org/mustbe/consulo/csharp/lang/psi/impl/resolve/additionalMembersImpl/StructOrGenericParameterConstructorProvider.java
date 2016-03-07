package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class StructOrGenericParameterConstructorProvider implements CSharpAdditionalMemberProvider
{
	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@NotNull DotNetElement element,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull Consumer<PsiElement> consumer)
	{
		if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isStruct())
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			DotNetNamedElement parameterlessConstructor = ContainerUtil.find(typeDeclaration.getMembers(), new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					if(element instanceof CSharpConstructorDeclaration && !((CSharpConstructorDeclaration) element).isDeConstructor())
					{
						if(((CSharpConstructorDeclaration) element).getParameters().length == 0)
						{
							return true;
						}
					}
					return false;
				}
			});

			if(parameterlessConstructor == null)
			{
				buildDefaultConstructor((DotNetNamedElement) element, CSharpModifier.PUBLIC, extractor, consumer);
			}
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			DotNetNamedElement anyConstructor = ContainerUtil.find(typeDeclaration.getMembers(), new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					return element instanceof CSharpConstructorDeclaration && !((CSharpConstructorDeclaration) element).isDeConstructor();
				}
			});

			if(anyConstructor == null)
			{
				buildDefaultConstructor((DotNetNamedElement) element, CSharpModifier.PUBLIC, extractor, consumer);
			}
		}
		else if(element instanceof DotNetGenericParameter)
		{
			// we need always create constructor, it ill check in CS0304
			buildDefaultConstructor((DotNetNamedElement) element, CSharpModifier.PUBLIC, extractor, consumer);
		}
	}

	@NotNull
	@Override
	public Target getTarget()
	{
		return Target.CONSTRUCTOR;
	}

	private static void buildDefaultConstructor(@NotNull DotNetNamedElement element,
			@NotNull CSharpModifier modifier,
			@NotNull DotNetGenericExtractor extractor,
			@NotNull Consumer<PsiElement> consumer)
	{
		String name = element.getName();
		if(name == null)
		{
			return;
		}
		CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(element.getProject());
		builder.addModifier(modifier);
		builder.setNavigationElement(element);
		builder.withParent(element);
		builder.withName(name);

		CSharpMethodDeclaration delegatedMethod = element.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
		if(delegatedMethod != null)
		{
			CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(element.getProject());
			parameter = parameter.withName("p");

			CSharpMethodDeclaration extractedMethod = GenericUnwrapTool.extract(delegatedMethod, extractor);
			parameter = parameter.withTypeRef(new CSharpLambdaTypeRef(extractedMethod));
			builder.addParameter(parameter);
		}
		consumer.consume(builder);
	}
}
