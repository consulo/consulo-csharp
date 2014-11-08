package org.mustbe.consulo.csharp.ide.completion;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import lombok.val;

/**
 * @author VISTALL
 * @since 05.11.14
 */
public class CSharpSmartCompletionContributor extends CompletionContributor
{
	private static final ElementPattern<? extends PsiElement> ourNewTypeContributor = StandardPatterns.psiElement().afterLeaf("new");

	public CSharpSmartCompletionContributor()
	{
		extend(CompletionType.BASIC, ourNewTypeContributor, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				PsiElement parentOfType = PsiTreeUtil.getParentOfType(position, CSharpAssignmentExpressionImpl.class,
						CSharpLocalVariable.class);

				DotNetTypeRef typeRef = null;
				if(parentOfType instanceof CSharpLocalVariable)
				{
					typeRef = ((CSharpLocalVariable) parentOfType).toTypeRef(false);
				}
				else if(parentOfType instanceof CSharpAssignmentExpressionImpl)
				{
					DotNetExpression dotNetExpression = ((CSharpAssignmentExpressionImpl) parentOfType).getParameterExpressions()[0];

					typeRef = dotNetExpression.toTypeRef(false);
				}

				if(typeRef != null)
				{
					LookupElementBuilder builder = buildForTypeRef(typeRef, parentOfType);
					if(builder != null)
					{
						DotNetTypeResolveResult resolve = typeRef.resolve(parentOfType);
						if(resolve.getGenericExtractor() == DotNetGenericExtractor.EMPTY)
						{
							return;
						}
						builder = builder.withInsertHandler(new ParenthesesInsertHandler<LookupElement>()
						{
							@Override
							protected boolean placeCaretInsideParentheses(InsertionContext context, LookupElement item)
							{
								return true;
							}
						});
						result.addElement(PrioritizedLookupElement.withPriority(builder, 100));
					}
				}
			}
		});
	}

	private static LookupElementBuilder buildForTypeRef(DotNetTypeRef typeRef, PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.AUTO_TYPE || typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return null;
		}
		val typeResolveResult = typeRef.resolve(scope);

		PsiElement element = typeResolveResult.getElement();
		if(element instanceof CSharpTypeDeclaration)
		{
			String lookupString = ((CSharpTypeDeclaration) element).getName();
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) element).getGenericParameters();

			if(genericParameters.length > 0)
			{
				lookupString += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
				{
					@Override
					public String fun(DotNetGenericParameter parameter)
					{
						DotNetTypeRef extract = typeResolveResult.getGenericExtractor().extract(parameter);
						if(extract != null)
						{
							return extract.getPresentableText();
						}
						return parameter.getName();
					}
				}, ", ") + ">";
			}
			LookupElementBuilder builder = LookupElementBuilder.create(element, lookupString);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			return builder;
		}
		return null;
	}
}
