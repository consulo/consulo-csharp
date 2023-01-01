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

package consulo.csharp.lang.impl.psi.resolve.baseResolveContext;

import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.resolve.CSharpBaseResolveContext;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class SimpleElementGroupCollectors
{
	public static class Constructor extends SimpleElementGroupCollector<CSharpConstructorDeclaration>
	{
		public Constructor(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super(MsilHelper.CONSTRUCTOR_NAME, CSharpAdditionalMemberProvider.Target.CONSTRUCTOR, context);
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<CSharpConstructorDeclaration> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
				{
					if(!declaration.isDeConstructor())
					{
						consumer.accept(declaration);
					}
				}
			};
		}
	}

	public static class DeConstructor extends SimpleElementGroupCollector<CSharpConstructorDeclaration>
	{
		public DeConstructor(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super("~" + MsilHelper.CONSTRUCTOR_NAME, CSharpAdditionalMemberProvider.Target.DE_CONSTRUCTOR, context);
		}

		@Nonnull
		@Override
		public DotNetGenericExtractor getExtractor()
		{
			// don't allow extract
			return DotNetGenericExtractor.EMPTY;
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<CSharpConstructorDeclaration> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
				{
					if(declaration.isDeConstructor())
					{
						consumer.accept(declaration);
					}
				}
			};
		}
	}

	public static class IndexMethod extends SimpleElementGroupCollector<CSharpIndexMethodDeclaration>
	{
		public IndexMethod(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super("[]", CSharpAdditionalMemberProvider.Target.INDEX_METHOD, context);
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<CSharpIndexMethodDeclaration> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitIndexMethodDeclaration(CSharpIndexMethodDeclaration methodDeclaration)
				{
					consumer.accept(methodDeclaration);
				}
			};
		}
	}
}
