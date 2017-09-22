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
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpFileFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
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
	public static class Result implements DotNetTypeResolveResult
	{
		private final PsiElement myScope;
		private final int myDimensions;
		private final DotNetTypeRef myInnerType;

		private NullableLazyValue<PsiElement> myValue = new NullableLazyValue<PsiElement>()
		{
			@Nullable
			@Override
			protected PsiElement compute()
			{
				return createTypeDeclaration(myScope.getProject(), myScope.getResolveScope(), myDimensions);
			}
		};

		private NotNullLazyValue<DotNetGenericExtractor> myExtractorValue = new NotNullLazyValue<DotNetGenericExtractor>()
		{
			@NotNull
			@Override
			protected DotNetGenericExtractor compute()
			{
				PsiElement element = getElement();

				if(!(element instanceof DotNetGenericParameterListOwner))
				{
					return DotNetGenericExtractor.EMPTY;
				}
				return CSharpGenericExtractor.create(((DotNetGenericParameterListOwner) element).getGenericParameters(), new DotNetTypeRef[]{myInnerType});
			}
		};

		@NotNull
		private static DotNetTypeDeclaration createTypeDeclaration(@NotNull Project project, @NotNull GlobalSearchScope searchScope, int dimensions)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("public class ArrayImpl<T> : System.Array, System.Collections.Generic.IEnumerable<T>, System.Collections.Generic.IList<T>");
			builder.append("{");
			builder.append("private ArrayImpl() {}");
			builder.append("public T this[long index");
			for(int i = 0; i < dimensions; i++)
			{
				builder.append(", int index").append(i);
			}
			builder.append("] { get; set; }");
			builder.append("public T this[ulong index");
			for(int i = 0; i < dimensions; i++)
			{
				builder.append(", int index").append(i);
			}
			builder.append("] { get; set; }");
			builder.append("public T this[int index");
			for(int i = 0; i < dimensions; i++)
			{
				builder.append(", int index").append(i);
			}
			builder.append("] { get; set; }");
			builder.append("public T this[uint index");
			for(int i = 0; i < dimensions; i++)
			{
				builder.append(", int index").append(i);
			}
			builder.append("] { get; set; }");
			builder.append("public System.Collections.Generic.IEnumerator<T> System.Collections.Generic.IEnumerator<T>.GetEnumerator() {}");
			builder.append("}");

			DotNetTypeDeclaration typeDeclaration = CSharpFileFactory.createTypeDeclaration(project, builder.toString());
			PsiCodeFragment containingFile = (PsiCodeFragment) typeDeclaration.getContainingFile();
			containingFile.forceResolveScope(searchScope);
			return typeDeclaration;
		}

		public Result(PsiElement scope, int dimensions, DotNetTypeRef innerType)
		{
			myScope = scope;
			myDimensions = dimensions;
			myInnerType = innerType;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myValue.getValue();
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return myExtractorValue.getValue();
		}

		@Override
		public boolean isNullable()
		{
			return true;
		}
	}

	private final PsiElement myScope;
	private final DotNetTypeRef myInnerType;
	private final int myDimensions;

	public CSharpArrayTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef innerType, int dimensions)
	{
		super(scope.getProject());
		myScope = scope;
		myInnerType = innerType;
		myDimensions = dimensions;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new Result(myScope, myDimensions, myInnerType);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(myInnerType.toString());
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
		return myInnerType;
	}

	public int getDimensions()
	{
		return myDimensions;
	}
}
