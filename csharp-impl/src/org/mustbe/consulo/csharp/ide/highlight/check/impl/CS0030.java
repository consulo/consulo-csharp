package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class CS0030 extends CompilerCheck<PsiElement>
{
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

		/*	@Override
			public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
			{
				DotNetType type = expression.getType();
				DotNetTypeRef dotNetTypeRef = type.toTypeRef();
				if(dotNetTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}

				DotNetExpression innerExpression = expression.getInnerExpression();
				if(innerExpression == null)
				{
					return;
				}
				Quaternary.create(innerExpression.toTypeRef(false), dotNetTypeRef, type, null);
			}  */
		});

		return ref.get();
	}
}
