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
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightConstructorDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Condition;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 26.10.14
 */
@ExtensionImpl
public class StructOrGenericParameterConstructorProvider implements CSharpAdditionalMemberProvider
{
	@RequiredReadAction
	@Override
	public void processAdditionalMembers(@Nonnull DotNetElement element,
			@Nonnull DotNetGenericExtractor extractor,
			@Nonnull Consumer<PsiElement> consumer)
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
				buildDefaultConstructor((DotNetNamedElement) element, extractor, consumer);
			}
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			DotNetNamedElement anyConstructor = ContainerUtil.find(typeDeclaration.getMembers(), e -> e instanceof CSharpConstructorDeclaration && !((CSharpConstructorDeclaration) e).isDeConstructor());

			if(anyConstructor == null)
			{
				buildDefaultConstructor((DotNetNamedElement) element, extractor, consumer);
			}
		}
		else if(element instanceof DotNetGenericParameter)
		{
			// we need always create constructor, it ill check in CS0304
			buildDefaultConstructor((DotNetNamedElement) element, extractor, consumer);
		}
	}

	@Nonnull
	@Override
	public Target getTarget()
	{
		return Target.CONSTRUCTOR;
	}

	@RequiredReadAction
	private static void buildDefaultConstructor(@Nonnull DotNetNamedElement element,
			@Nonnull DotNetGenericExtractor extractor,
			@Nonnull Consumer<PsiElement> consumer)
	{
		String name = element.getName();
		if(name == null)
		{
			return;
		}
		CSharpLightConstructorDeclarationBuilder builder = buildDefaultConstructor(element, name);

		CSharpMethodDeclaration delegatedMethod = element.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
		if(delegatedMethod != null)
		{
			CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(element.getProject());
			parameter = parameter.withName("p");

			CSharpMethodDeclaration extractedMethod = GenericUnwrapTool.extract(delegatedMethod, extractor);
			parameter = parameter.withTypeRef(new CSharpLambdaTypeRef(extractedMethod));
			builder.addParameter(parameter);
		}
		consumer.accept(builder);
	}

	@RequiredReadAction
	public static CSharpLightConstructorDeclarationBuilder buildDefaultConstructor(@Nonnull DotNetNamedElement element, @Nonnull String name)
	{
		CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(element);
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.setNavigationElement(element);
		builder.withParent(element);
		builder.withName(name);
		return builder;
	}
}
