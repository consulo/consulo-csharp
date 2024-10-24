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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.eap.EarlyAccessProgramManager;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.CSharpHighlightKey;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.impl.ide.highlight.quickFix.ReplaceTypeQuickFix;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpInheritableChecker;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTypeCastExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.PsiElement;
import consulo.util.lang.ref.SimpleReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class CS0030 extends CompilerCheck<PsiElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull final CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull PsiElement element)
	{
		final SimpleReference<CompilerCheckBuilder> ref = SimpleReference.create();
		element.accept(new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitForeachStatement(CSharpForeachStatementImpl statement)
			{
				CSharpLocalVariable variable = statement.getVariable();
				if(variable == null)
				{
					return;
				}

				DotNetType type = variable.getType();
				if(type == null)
				{
					return;
				}
				DotNetTypeRef variableTypeRef = type.toTypeRef();
				if(variableTypeRef == DotNetTypeRef.AUTO_TYPE || variableTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}
				DotNetTypeRef iterableTypeRef = CSharpResolveUtil.resolveIterableType(statement);
				if(iterableTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}

				boolean success = CSharpTypeUtil.isInheritable(iterableTypeRef, variableTypeRef) || CSharpTypeUtil.isInheritable(variableTypeRef, iterableTypeRef);
				if(!success)
				{
					CompilerCheckBuilder builder = newBuilder(type, formatTypeRef(iterableTypeRef), formatTypeRef(variableTypeRef));

					if(languageVersion.isAtLeast(CSharpLanguageVersion._3_0))
					{
						builder.withQuickFix(new ReplaceTypeQuickFix(type, DotNetTypeRef.AUTO_TYPE));
					}
					builder.withQuickFix(new ReplaceTypeQuickFix(type, iterableTypeRef));

					ref.set(builder);
				}
			}

			@Override
			@RequiredReadAction
			public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
			{
				DotNetType type = expression.getType();
				DotNetTypeRef castTypeRef = type.toTypeRef();
				if(castTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}

				DotNetExpression innerExpression = expression.getInnerExpression();
				if(innerExpression == null)
				{
					return;
				}
				DotNetTypeRef expressionTypeRef = innerExpression.toTypeRef(false);

				CSharpTypeUtil.InheritResult inheritResult = CSharpInheritableChecker.create(expressionTypeRef, castTypeRef).withCastType(CSharpCastType.EXPLICIT, expression.getResolveScope())
						.check();

				if(!inheritResult.isSuccess())
				{
					inheritResult = CSharpInheritableChecker.create(expressionTypeRef, castTypeRef).withCastType(CSharpCastType.IMPLICIT, expression.getResolveScope()).check();

					if(!inheritResult.isSuccess())
					{
						CompilerCheckBuilder builder = newBuilder(type, formatTypeRef(expressionTypeRef), formatTypeRef(castTypeRef));

						if(EarlyAccessProgramManager.is(CS0030TypeCastEapDescriptor.class))
						{
							ref.set(builder);
						}
					}
					else if(inheritResult.isConversion())
					{
						CompilerCheckBuilder builder = newBuilder(innerExpression);
						builder.withTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
						if(inheritResult.isImplicit())
						{
							builder.withText(CSharpErrorLocalize.impicitCastFrom0To1(formatTypeRef(expressionTypeRef), formatTypeRef(castTypeRef)));
						}
						else
						{
							builder.withText(CSharpErrorLocalize.explicitCastFrom0To1(formatTypeRef(expressionTypeRef), formatTypeRef(castTypeRef)));
						}
						builder.withHighlightInfoType(HighlightInfoType.INFORMATION);
						ref.set(builder);
					}
				}
				else if(inheritResult.isConversion())
				{
					CompilerCheckBuilder builder = newBuilder(type);
					builder.withTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
					builder.withText(CSharpErrorLocalize.impicitCastFrom0To1(formatTypeRef(expressionTypeRef), formatTypeRef(castTypeRef)));
					builder.withHighlightInfoType(HighlightInfoType.INFORMATION);
					ref.set(builder);
				}
			}
		});

		return ref.get();
	}
}
