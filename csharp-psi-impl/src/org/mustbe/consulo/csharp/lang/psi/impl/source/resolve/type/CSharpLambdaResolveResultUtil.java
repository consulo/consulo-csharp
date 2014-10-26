package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpLambdaResolveResultUtil
{
	@NotNull
	public static CSharpTypeDeclaration createTypeFromDelegate(@NotNull CSharpMethodDeclaration declaration)
	{
		Project project = declaration.getProject();

		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(project);
		builder.withParentQName(declaration.getPresentableParentQName());
		builder.withName(declaration.getName());

		builder.putUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE, declaration);

		builder.addExtendType(new DotNetTypeRefByQName(DotNetTypes.System.MulticastDelegate, CSharpTransform.INSTANCE));

		for(DotNetGenericParameter parameter : declaration.getGenericParameters())
		{
			builder.addGenericParameter(parameter);
		}

		CSharpLightConstructorDeclarationBuilder cBuilder = new CSharpLightConstructorDeclarationBuilder(project);
		cBuilder.addModifier(CSharpModifier.PUBLIC);
		cBuilder.setNavigationElement(declaration);
		cBuilder.withParent(declaration);
		cBuilder.withName(declaration.getName());

		builder.addMember(cBuilder);

		CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(declaration.getProject());
		parameter = parameter.withName("p");
		parameter = parameter.withTypeRef(new CSharpLambdaTypeRef(declaration, declaration.getParameterTypeRefs(), declaration.getReturnTypeRef()));
		cBuilder.addParameter(parameter);

		return builder;
	}
}
