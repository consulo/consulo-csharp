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

package consulo.csharp.ide.highlight.check.impl;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.CSharpHighlightKey;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpTypeCastExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.ide.eap.EarlyAccessProgramDescriptor;
import consulo.ide.eap.EarlyAccessProgramManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class CS0030 extends CompilerCheck<PsiElement>
{
	public static class CS0030TypeCast extends EarlyAccessProgramDescriptor
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "CS0030 Type cast checks";
		}

		@Nonnull
		@Override
		public String getDescription()
		{
			return "";
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull final CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull PsiElement element)
	{
		final Ref<CompilerCheckBuilder> ref = new Ref<CompilerCheckBuilder>();
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

				boolean success = CSharpTypeUtil.isInheritable(iterableTypeRef, variableTypeRef, statement) || CSharpTypeUtil.isInheritable(variableTypeRef, iterableTypeRef, statement);
				if(!success)
				{
					CompilerCheckBuilder builder = newBuilder(type, formatTypeRef(iterableTypeRef, statement), formatTypeRef(variableTypeRef, statement));

					if(languageVersion.isAtLeast(CSharpLanguageVersion._3_0))
					{
						builder.addQuickFix(new ReplaceTypeQuickFix(type, DotNetTypeRef.AUTO_TYPE));
					}
					builder.addQuickFix(new ReplaceTypeQuickFix(type, iterableTypeRef));

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

				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.isInheritable(expressionTypeRef, castTypeRef, expression, CSharpCastType.EXPLICIT);

				if(!inheritResult.isSuccess())
				{
					inheritResult = CSharpTypeUtil.isInheritable(expressionTypeRef, castTypeRef, expression, CSharpCastType.IMPLICIT);

					if(!inheritResult.isSuccess())
					{
						CompilerCheckBuilder builder = newBuilder(type, formatTypeRef(expressionTypeRef, expression), formatTypeRef(castTypeRef, expression));

						if(EarlyAccessProgramManager.is(CS0030TypeCast.class))
						{
							ref.set(builder);
						}
					}
					else if(inheritResult.isConversion())
					{
						CompilerCheckBuilder builder = newBuilder(innerExpression);
						builder.setTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
						if(inheritResult.isImplicit())
						{
							builder.setText(CSharpErrorLocalize.impicitCastFrom0To1(formatTypeRef(expressionTypeRef, expression), formatTypeRef(castTypeRef, expression)).getValue());
						}
						else
						{
							builder.setText(CSharpErrorLocalize.explicitCastFrom0To1(formatTypeRef(expressionTypeRef, expression), formatTypeRef(castTypeRef, expression)).getValue());
						}
						builder.setHighlightInfoType(HighlightInfoType.INFORMATION);
						ref.set(builder);
					}
				}
				else if(inheritResult.isConversion())
				{
					CompilerCheckBuilder builder = newBuilder(type);
					builder.setTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
					builder.setText(CSharpErrorLocalize.impicitCastFrom0To1(formatTypeRef(expressionTypeRef, expression), formatTypeRef(castTypeRef, expression)).getValue());
					builder.setHighlightInfoType(HighlightInfoType.INFORMATION);
					ref.set(builder);
				}
			}
		});

		return ref.get();
	}
}
