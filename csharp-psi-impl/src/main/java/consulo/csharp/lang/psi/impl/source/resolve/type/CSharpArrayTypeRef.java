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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightIndexMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.resolve.*;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpArrayTypeRef extends DotNetTypeRefWithCachedResult implements DotNetArrayTypeRef
{
	public static class ArrayResolveResult extends CSharpUserTypeRef.Result<CSharpTypeDeclaration>
	{
		private int myDimensions;
		private DotNetTypeRef myInnerTypeRef;

		public ArrayResolveResult(CSharpTypeDeclaration element, int dimensions, DotNetTypeRef innerTypeRef)
		{
			super(element, DotNetGenericExtractor.EMPTY);
			myDimensions = dimensions;
			myInnerTypeRef = innerTypeRef;
		}

		public int getDimensions()
		{
			return myDimensions;
		}

		public DotNetTypeRef getInnerTypeRef()
		{
			return myInnerTypeRef;
		}
	}

	private final DotNetTypeRef myInnerTypeRef;
	private final int myDimensions;

	@RequiredReadAction
	public CSharpArrayTypeRef(@Nonnull DotNetTypeRef innerTypeRef, int dimensions)
	{
		super(innerTypeRef.getProject(), innerTypeRef.getResolveScope());
		myInnerTypeRef = innerTypeRef;
		myDimensions = dimensions;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(myProject, myResolveScope);
		builder.withParentQName("System");
		builder.withName("ArrayImpl[" + CSharpTypeRefPresentationUtil.buildText(myInnerTypeRef) + "]");
		builder.addModifier(DotNetModifier.PUBLIC);

		builder.addExtendType(new CSharpTypeRefByQName(myProject, myResolveScope, "System.Array"));

		if(myDimensions == 0)
		{
			builder.addExtendType(new CSharpGenericWrapperTypeRef(myProject, myResolveScope, new CSharpTypeRefByQName(myProject, myResolveScope , DotNetTypes.System.Collections.Generic.IEnumerable$1), myInnerTypeRef));

			builder.addExtendType(new CSharpGenericWrapperTypeRef(myProject, myResolveScope, new CSharpTypeRefByQName(myProject, myResolveScope, DotNetTypes.System.Collections.Generic.IList$1), myInnerTypeRef));
		}

		addIndexMethodWithType(DotNetTypes.System.Int32, builder, myProject, myResolveScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.UInt32, builder, myProject, myResolveScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.Int64, builder, myProject, myResolveScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.UInt64, builder, myProject, myResolveScope, myDimensions, myInnerTypeRef);

		return new ArrayResolveResult(builder, myDimensions, myInnerTypeRef);
	}

	@RequiredReadAction
	private static void addIndexMethodWithType(String parameterQName, CSharpLightTypeDeclarationBuilder builder, Project project, GlobalSearchScope scope, int dimensions, DotNetTypeRef innerType)
	{
		int parameterCount = dimensions + 1;

		CSharpLightIndexMethodDeclarationBuilder methodDeclarationBuilder = new CSharpLightIndexMethodDeclarationBuilder(builder.getProject(), dimensions);
		methodDeclarationBuilder.addModifier(DotNetModifier.PUBLIC);

		for(int i = 0; i < parameterCount; i++)
		{
			CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(project);
			parameter.withName("index" + i);
			parameter.withTypeRef(new CSharpTypeRefByQName(project, scope, parameterQName));

			methodDeclarationBuilder.addParameter(parameter);
		}

		methodDeclarationBuilder.withReturnType(innerType);

		builder.addMember(methodDeclarationBuilder);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(myInnerTypeRef.toString());
		builder.append("[");
		for(int i = 0; i < myDimensions; i++)
		{
			builder.append(",");
		}
		builder.append("]");
		return builder.toString();
	}

	@Nonnull
	@Override
	public String getVmQName()
	{
		return CSharpTypeRefPresentationUtil.buildText(this);
	}

	@Override
	@Nonnull
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}

	@Nonnull
	public GlobalSearchScope getResolveScope()
	{
		return myResolveScope;
	}

	public int getDimensions()
	{
		return myDimensions;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		CSharpArrayTypeRef that = (CSharpArrayTypeRef) o;
		return myDimensions == that.myDimensions &&
				Objects.equals(myInnerTypeRef, that.myInnerTypeRef);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myInnerTypeRef, myDimensions);
	}
}
