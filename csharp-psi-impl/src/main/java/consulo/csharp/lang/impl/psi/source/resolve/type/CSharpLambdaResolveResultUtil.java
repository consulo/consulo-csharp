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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpLambdaResolveResultUtil
{
	@Contract("null -> null")
	public static CSharpMethodDeclaration getDelegateMethodTypeWrapper(@Nullable PsiElement element)
	{
		return element != null ? element.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE) : null;
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpTypeDeclaration createTypeFromDelegate(@Nonnull CSharpMethodDeclaration declaration, @Nonnull DotNetGenericExtractor extractor)
	{
		Project project = declaration.getProject();

		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(declaration.getProject(), declaration.getResolveScope());
		builder.withParentQName(declaration.getPresentableParentQName());
		builder.withName(Objects.requireNonNullElse(declaration.getName(), "Unknown"));
		builder.addModifier(DotNetModifier.SEALED);
		CSharpAccessModifier accessModifier = CSharpAccessModifier.findModifierOrDefault(declaration);
		for(CSharpModifier modifier : accessModifier.getModifiers())
		{
			builder.addModifier(modifier);
		}

		builder.setNavigationElement(declaration);

		builder.putUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE, GenericUnwrapTool.extract(declaration, extractor));

		builder.addExtendType(new CSharpTypeRefByQName(declaration, DotNetTypes.System.MulticastDelegate));

		for(DotNetGenericParameter parameter : declaration.getGenericParameters())
		{
			builder.addGenericParameter(parameter);
		}

		CSharpLightMethodDeclarationBuilder invokeMethodBuilder = new CSharpLightMethodDeclarationBuilder(project);
		invokeMethodBuilder.withName("Invoke");
		invokeMethodBuilder.addModifier(CSharpModifier.PUBLIC);
		invokeMethodBuilder.withReturnType(declaration.getReturnTypeRef());

		DotNetParameter[] parameters = declaration.getParameters();
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
			String name = parameter.getName();
			if(name == null)
			{
				parameterBuilder.withName("p" + i);
			}
			else
			{
				parameterBuilder.withName(name);
			}
			parameterBuilder.withTypeRef(parameter.toTypeRef(true));
			invokeMethodBuilder.addParameter(parameterBuilder);
		}
		builder.addMember(invokeMethodBuilder);
		return builder;
	}
}
