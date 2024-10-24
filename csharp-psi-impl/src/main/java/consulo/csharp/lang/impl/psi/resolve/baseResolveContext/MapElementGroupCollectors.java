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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.impl.psi.resolve.CSharpBaseResolveContext;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpStaticTypeRef;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class MapElementGroupCollectors
{
	public static class ConversionMethod extends MapElementGroupCollector<CSharpCastType, CSharpConversionMethodDeclaration>
	{
		public ConversionMethod(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super(CSharpAdditionalMemberProvider.Target.CONVERSION_METHOD, context);
		}

		@Nonnull
		@Override
		public DotNetGenericExtractor getExtractor()
		{
			return DotNetGenericExtractor.EMPTY;
		}

		@RequiredReadAction
		@Nullable
		@Override
		protected CSharpCastType getKeyForElement(CSharpConversionMethodDeclaration element)
		{
			DotNetTypeRef conversionTypeRef = element.getConversionTypeRef();
			if(conversionTypeRef == CSharpStaticTypeRef.IMPLICIT)
			{
				return CSharpCastType.IMPLICIT;
			}
			return CSharpCastType.EXPLICIT;
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<CSharpConversionMethodDeclaration> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration element)
				{
					consumer.accept(element);
				}
			};
		}
	}

	public static class OperatorMethod extends MapElementGroupCollector<IElementType, CSharpMethodDeclaration>
	{
		public OperatorMethod(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super(CSharpAdditionalMemberProvider.Target.OPERATOR_METHOD, context);
		}

		@RequiredReadAction
		@Nullable
		@Override
		protected IElementType getKeyForElement(CSharpMethodDeclaration element)
		{
			return element.getOperatorElementType();
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<CSharpMethodDeclaration> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
				{
					if(declaration.isOperator())
					{
						consumer.accept(declaration);
					}
				}
			};
		}
	}

	public static class Other extends MapElementGroupCollector<String, PsiElement>
	{
		public Other(@Nonnull CSharpBaseResolveContext<?> context)
		{
			super(CSharpAdditionalMemberProvider.Target.OTHER, context);
		}

		@RequiredReadAction
		@Nullable
		@Override
		protected String getKeyForElement(PsiElement element)
		{
			if(element instanceof DotNetNamedElement)
			{
				return ((DotNetNamedElement) element).getName();
			}
			return null;
		}

		@Nonnull
		@Override
		protected CSharpElementVisitor createVisitor(@Nonnull final Consumer<PsiElement> consumer)
		{
			return new CSharpElementVisitor()
			{
				@Override
				public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
				{
					if(declaration.isOperator())
					{
						// ignore operators
					}
					else
					{
						consumer.accept(declaration);
					}
				}

				@Override
				public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
				{
					consumer.accept(declaration);
				}

				@Override
				public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
				{
					consumer.accept(declaration);
				}

				@Override
				public void visitEventDeclaration(CSharpEventDeclaration declaration)
				{
					consumer.accept(declaration);
				}

				@Override
				public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
				{
					consumer.accept(declaration);
				}

				@Override
				public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
				{
					consumer.accept(declaration);
				}
			};
		}
	}
}
