package org.mustbe.consulo.csharp.ide.completion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeRefProvider;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNewExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
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

/**
 * @author VISTALL
 * @since 05.11.14
 */
public class CSharpSmartCompletionContributor extends CompletionContributor
{
	private static final ElementPattern<? extends PsiElement> ourNewTypeContributor = StandardPatterns.psiElement().afterLeaf(StandardPatterns
			.psiElement().withElementType(CSharpTokens.NEW_KEYWORD));

	public CSharpSmartCompletionContributor()
	{
		extend(CompletionType.BASIC, ourNewTypeContributor, new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				CSharpNewExpressionImpl newExpression = PsiTreeUtil.getParentOfType(position, CSharpNewExpressionImpl.class);
				assert newExpression != null;

				List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeRefProvider.findExpectedTypeRefs(newExpression);

				if(!expectedTypeRefs.isEmpty())
				{
					for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
					{
						DotNetTypeResolveResult typeResolveResult = expectedTypeInfo.getTypeRef().resolve(position);

						PsiElement element = typeResolveResult.getElement();
						if(element == null)
						{
							return;
						}

						DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
						CSharpResolveContext cSharpResolveContext = CSharpResolveContextUtil.createContext(genericExtractor,
								position.getResolveScope(), element);

						CSharpElementGroup<CSharpConstructorDeclaration> group = cSharpResolveContext.constructorGroup();
						Collection<CSharpConstructorDeclaration> objects = group == null ? Collections.<CSharpConstructorDeclaration>emptyList() :
								group.getElements();

						if(objects.isEmpty())
						{
							return;
						}

						for(CSharpConstructorDeclaration object : objects)
						{
							LookupElementBuilder builder = buildForConstructor(object, genericExtractor);
							if(builder != null)
							{
								result.addElement(PrioritizedLookupElement.withPriority(builder, CSharpCompletionUtil.EXPR_REF_PRIORITY));
							}
						}
					}
				}
			}
		});
	}

	@Nullable
	private static LookupElementBuilder buildForConstructor(final CSharpConstructorDeclaration declaration, final DotNetGenericExtractor extractor)
	{
		PsiElement parent = declaration.getParent();

		if(!(parent instanceof DotNetNamedElement))
		{
			return null;
		}

		String lookupString = ((DotNetNamedElement) parent).getName();
		if(parent instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) parent).getGenericParameters();
			if(genericParameters.length > 0)
			{
				lookupString += "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
				{
					@Override
					public String fun(DotNetGenericParameter parameter)
					{
						DotNetTypeRef extract = extractor.extract(parameter);
						if(extract != null)
						{
							return CSharpTypeRefPresentationUtil.buildShortText(extract, declaration);
						}
						return parameter.getName();
					}
				}, ", ") + ">";
			}
		}

		if(lookupString == null)
		{
			return null;
		}

		DotNetTypeRef[] parameters = declaration.getParameterTypeRefs();

		String parameterText = "(" + StringUtil.join(parameters, new Function<DotNetTypeRef, String>()
		{
			@Override
			public String fun(DotNetTypeRef parameter)
			{
				return CSharpTypeRefPresentationUtil.buildShortText(parameter, declaration);
			}
		}, ", ") + ")";

		LookupElementBuilder builder = LookupElementBuilder.create(parent, lookupString);
		builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, Iconable.ICON_FLAG_VISIBILITY));
		builder = builder.withTailText(parameterText, true);
		builder = builder.withInsertHandler(ParenthesesInsertHandler.getInstance(parameters.length > 0));
		return builder;
	}
}
