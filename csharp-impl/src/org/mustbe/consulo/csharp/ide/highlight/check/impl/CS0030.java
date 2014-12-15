package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.consulo.ide.eap.EarlyAccessProgramDescriptor;
import org.consulo.ide.eap.EarlyAccessProgramManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightKey;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeCastExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class CS0030 extends CompilerCheck<PsiElement>
{
	public static class CS0030TypeCast extends EarlyAccessProgramDescriptor
	{
		@NotNull
		@Override
		public String getName()
		{
			return "CS0030 Type cast checks";
		}

		@NotNull
		@Override
		public String getDescription()
		{
			return "";
		}
	}

	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull final CSharpLanguageVersion languageVersion, @NotNull PsiElement element)
	{
		val ref = new Ref<CompilerCheckBuilder>();
		element.accept(new CSharpElementVisitor()
		{
			@Override
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

				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.isInheritable(variableTypeRef, iterableTypeRef, statement, null);
				if(!inheritResult.isSuccess())
				{
					CompilerCheckBuilder builder = newBuilder(type, CSharpTypeRefPresentationUtil.buildText(iterableTypeRef, statement,
							CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(variableTypeRef, statement, CS0029.TYPE_FLAGS));

					if(languageVersion.isAtLeast(CSharpLanguageVersion._3_0))
					{
						builder.addQuickFix(new ReplaceTypeQuickFix(type, DotNetTypeRef.AUTO_TYPE));
					}
					builder.addQuickFix(new ReplaceTypeQuickFix(type, iterableTypeRef));

					ref.set(builder);
				}
			}

			@Override
			public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
			{
				if(!EarlyAccessProgramManager.is(CS0030TypeCast.class))
				{
					return;
				}
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

				CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.isInheritable(expressionTypeRef, castTypeRef, expression,
						CSharpStaticTypeRef.EXPLICIT);

				if(!inheritResult.isSuccess())
				{
					inheritResult = CSharpTypeUtil.isInheritable(expressionTypeRef, castTypeRef, expression, CSharpStaticTypeRef.IMPLICIT);

					if(!inheritResult.isSuccess())
					{
						CompilerCheckBuilder builder = newBuilder(type, CSharpTypeRefPresentationUtil.buildText(expressionTypeRef, expression,
								CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(castTypeRef, expression, CS0029.TYPE_FLAGS));

						ref.set(builder);
					}
					else if(inheritResult.getConversionMethod() != null)
					{
						CompilerCheckBuilder builder = newBuilder(innerExpression);
						builder.setTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
						builder.setText(CSharpErrorBundle.message("impicit.cast.from.0.to.1", CSharpTypeRefPresentationUtil.buildText
								(expressionTypeRef, expression, CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(castTypeRef, expression,
								CS0029.TYPE_FLAGS)));
						builder.setHighlightInfoType(HighlightInfoType.INFORMATION);
						ref.set(builder);
					}
				}
				else if(inheritResult.getConversionMethod() != null)
				{
					CompilerCheckBuilder builder = newBuilder(type);
					builder.setTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
					builder.setText(CSharpErrorBundle.message("explicit.cast.from.0.to.1", CSharpTypeRefPresentationUtil.buildText
							(expressionTypeRef, expression, CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(castTypeRef, expression,
							CS0029.TYPE_FLAGS)));
					builder.setHighlightInfoType(HighlightInfoType.INFORMATION);
					ref.set(builder);
				}
			}
		});

		return ref.get();
	}
}
