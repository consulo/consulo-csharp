package org.mustbe.consulo.csharp.ide.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.AbstractScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import lombok.val;

/**
 * @author VISTALL
 * @since 23.11.14
 */
public class CSharpReferenceCompletionContributor extends CompletionContributor
{
	public CSharpReferenceCompletionContributor()
	{

		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(DotNetReferenceExpression.class),
				new CompletionProvider<CompletionParameters>()
		{

			@Override
			protected void addCompletions(@NotNull final CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val parent = (DotNetReferenceExpression) completionParameters.getPosition().getParent();

				if(parent.getQualifier() == null)
				{
					CSharpCompletionUtil.tokenSetToLookup(completionResultSet, CSharpTokenSets.NATIVE_TYPES, null, new Condition<IElementType>()
					{
						@Override
						public boolean value(IElementType elementType)
						{
							if(elementType == CSharpTokens.EXPLICIT_KEYWORD || elementType == CSharpTokens.IMPLICIT_KEYWORD)
							{
								PsiElement invalidParent = PsiTreeUtil.getParentOfType(parent, DotNetStatement.class, DotNetParameterList
										.class);
								return invalidParent == null;
							}
							if(elementType == CSharpSoftTokens.VAR_KEYWORD)
							{
								if(!CSharpModuleUtil.findLanguageVersion(completionParameters.getOriginalFile()).isAtLeast(CSharpLanguageVersion
										._2_0))
								{
									return false;
								}
							}
							return true;
						}
					});

					val referenceName = parent.getReferenceName().replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");

					if(StringUtil.isEmpty(referenceName))
					{
						return;
					}

					val names = new ArrayList<String>();
					TypeIndex.getInstance().processAllKeys(parent.getProject(), new Processor<String>()
					{
						@Override
						public boolean process(String s)
						{
							if(s.startsWith(referenceName))
							{
								names.add(s);
							}
							return true;
						}
					});

					List<DotNetTypeDeclaration> typeDeclarations = new ArrayList<DotNetTypeDeclaration>(names.size());
					for(String name : names)
					{
						Collection<DotNetTypeDeclaration> temp = TypeIndex.getInstance().get(name, parent.getProject(), parent.getResolveScope());
						typeDeclarations.addAll(temp);
					}
					if(typeDeclarations.isEmpty())
					{
						return;
					}

					for(DotNetTypeDeclaration dotNetTypeDeclaration : typeDeclarations)
					{
						DotNetQualifiedElement wrap = (DotNetQualifiedElement) MsilToCSharpUtil.wrap(dotNetTypeDeclaration);

						String name = wrap.getName();
						String presentationText = name;

						if(isAlreadyResolved(wrap, parent))
						{
							continue;
						}

						if(wrap instanceof DotNetGenericParameterListOwner)
						{
							DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) wrap).getGenericParameters();
							if(genericParameters.length > 0)
							{
								name += "<>";
								presentationText += StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
								{
									@Override
									public String fun(DotNetGenericParameter parameter)
									{
										return parameter.getName();
									}
								}, ", ");
							}
						}

						LookupElementBuilder builder = LookupElementBuilder.create(name);
						builder = builder.withPresentableText(presentationText);
						builder = builder.withIcon(IconDescriptorUpdaters.getIcon(wrap, Iconable.ICON_FLAG_VISIBILITY));

						val parentQName = wrap.getPresentableParentQName();
						builder = builder.withTypeText(parentQName, true);
						builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
						{

							@Override
							public void handleInsert(InsertionContext context, LookupElement item)
							{
								new AddUsingAction(completionParameters.getEditor(), context.getFile(), Collections.<Couple<String>>singleton(Couple
										.of(null, parentQName))).execute();
							}
						});
						completionResultSet.addElement(builder);
					}
				}
			}
		});
	}

	public static boolean isAlreadyResolved(DotNetQualifiedElement element, PsiElement parent)
	{
		String parentQName = element.getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return true;
		}

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(element.getName()));
		resolveState = resolveState.put(MemberResolveScopeProcessor.BREAK_RULE, Boolean.TRUE);

		Couple<PsiElement> resolveLayers = CSharpReferenceExpressionImpl.getResolveLayers(parent, false);
		//PsiElement last = resolveLayers.getFirst();
		PsiElement targetToWalkChildren = resolveLayers.getSecond();

		AbstractScopeProcessor p = CSharpReferenceExpressionImpl.createMemberProcessor(element, CSharpReferenceExpression.ResolveToKind.TYPE_LIKE,
				ResolveResult.EMPTY_ARRAY, false, false);

		if(!CSharpResolveUtil.walkChildren(p, targetToWalkChildren, true, true, resolveState))
		{
			return true;
		}

		if(!CSharpResolveUtil.walkGenericParameterList(p, element, null, resolveState))
		{
			return true;
		}

		if(!CSharpResolveUtil.walkUsing(p, parent, null, resolveState))
		{
			return true;
		}

		return false;
	}
}
