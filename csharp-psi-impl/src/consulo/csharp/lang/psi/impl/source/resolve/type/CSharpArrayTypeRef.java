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

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightIndexMethodDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.resolve.DotNetArrayTypeRef;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

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

	private final PsiElement myScope;
	private final DotNetTypeRef myInnerTypeRef;
	private final int myDimensions;

	@RequiredReadAction
	public CSharpArrayTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef innerTypeRef, int dimensions)
	{
		super(scope.getProject());
		myScope = scope;
		myInnerTypeRef = innerTypeRef;
		myDimensions = dimensions;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(myScope);
		builder.withParentQName("System");
		builder.withName("ArrayImpl[" + CSharpTypeRefPresentationUtil.buildText(myInnerTypeRef, myScope) + "]");
		builder.addModifier(DotNetModifier.PUBLIC);

		builder.addExtendType(new CSharpTypeRefByQName(myScope, "System.Array"));

		if(myDimensions == 0)
		{
			builder.addExtendType(new CSharpGenericWrapperTypeRef(myScope.getProject(), new CSharpTypeRefByQName(myScope, DotNetTypes.System.Collections.Generic.IEnumerable$1), myInnerTypeRef));

			builder.addExtendType(new CSharpGenericWrapperTypeRef(myScope.getProject(), new CSharpTypeRefByQName(myScope, DotNetTypes.System.Collections.Generic.IList$1), myInnerTypeRef));
		}

		addIndexMethodWithType(DotNetTypes.System.Int32, builder, myScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.UInt32, builder, myScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.Int64, builder, myScope, myDimensions, myInnerTypeRef);
		addIndexMethodWithType(DotNetTypes.System.UInt64, builder, myScope, myDimensions, myInnerTypeRef);

		return new ArrayResolveResult(builder, myDimensions, myInnerTypeRef);
	}

	@RequiredReadAction
	private static void addIndexMethodWithType(String parameterQName, CSharpLightTypeDeclarationBuilder builder, PsiElement scope, int dimensions, DotNetTypeRef innerType)
	{
		int parameterCount = dimensions + 1;

		CSharpLightIndexMethodDeclarationBuilder methodDeclarationBuilder = new CSharpLightIndexMethodDeclarationBuilder(builder.getProject(), dimensions);
		methodDeclarationBuilder.addModifier(DotNetModifier.PUBLIC);

		for(int i = 0; i < parameterCount; i++)
		{
			CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(scope);
			parameter.withName("index" + i);
			parameter.withTypeRef(new CSharpTypeRefByQName(scope, parameterQName));

			methodDeclarationBuilder.addParameter(parameter);
		}

		methodDeclarationBuilder.withReturnType(innerType);

		builder.addMember(methodDeclarationBuilder);
	}

	@RequiredReadAction
	@NotNull
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

	@Override
	@NotNull
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}

	public int getDimensions()
	{
		return myDimensions;
	}
}
