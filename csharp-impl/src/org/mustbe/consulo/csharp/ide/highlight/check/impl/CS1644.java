/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDictionaryInitializerImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFinallyStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGenericParameterListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.csharp.module.extension.CSharpMutableModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS1644 extends CompilerCheck<PsiElement>
{
	public static class SetLanguageVersionFix extends PsiElementBaseIntentionAction
	{
		private CSharpLanguageVersion myLanguageVersion;

		public SetLanguageVersionFix(CSharpLanguageVersion languageVersion)
		{
			myLanguageVersion = languageVersion;
			setText("Set language version to '" + myLanguageVersion.getPresentableName() + "'");
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
		{
			CSharpModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpModuleExtension.class);
			if(extension == null)
			{
				return;
			}

			ModuleRootManager rootManager = ModuleRootManager.getInstance(extension.getModule());

			ModifiableRootModel modifiableModel = rootManager.getModifiableModel();

			val mutable = modifiableModel.getExtension(CSharpMutableModuleExtension.class);
			assert mutable != null;
			mutable.setLanguageVersion(myLanguageVersion);

			modifiableModel.commit();
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
		{
			CSharpModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpModuleExtension.class);
			return extension != null && extension.getLanguageVersion().ordinal() < myLanguageVersion.ordinal();
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	public static class Feature
	{
		private String myName;
		private CSharpLanguageVersion myLanguageVersion;
		private Function<PsiElement, PsiElement> myFunc;

		Feature(String name, CSharpLanguageVersion languageVersion, Function<PsiElement, PsiElement> processor)
		{
			myName = name;
			myLanguageVersion = languageVersion;
			myFunc = processor;
		}
	}

	private List<Feature> myFeatures = new ArrayList<Feature>()
	{
		{
			add(new Feature("lambda expressions", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpLambdaExpressionImpl)
					{
						return element;
					}
					return null;
				}
			}));
			add(new Feature("generics", CSharpLanguageVersion._2_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpGenericParameterListImpl)
					{
						return element;
					}
					else if(element.getNode() != null && (element.getNode().getElementType() == CSharpElements.TYPE_ARGUMENTS || element.getNode()
							.getElementType() == CSharpStubElements.TYPE_ARGUMENTS))
					{
						return element;
					}
					return null;
				}
			}));
			add(new Feature("implicitly typed local variable", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpLocalVariable && ((CSharpLocalVariable) element).toTypeRef(false) == DotNetTypeRef.AUTO_TYPE)
					{
						return ((CSharpLocalVariable) element).getType();
					}
					return null;
				}
			}));
			add(new Feature("extension methods", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpMethodDeclaration)
					{
						DotNetParameter[] parameters = ((CSharpMethodDeclaration) element).getParameters();
						if(parameters.length > 0)
						{
							DotNetModifierList modifierList = parameters[0].getModifierList();
							if(modifierList != null)
							{
								PsiElement modifier = modifierList.getModifierElement(CSharpModifier.THIS);
								if(modifier != null)
								{
									return modifier;
								}
							}
						}
					}
					return null;
				}
			}));
			add(new Feature("using static members", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					return element instanceof CSharpUsingTypeStatement ? element : null;
				}
			}));
			add(new Feature("parameterless struct ctors", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpConstructorDeclaration)
					{
						PsiElement parent = element.getParent();
						if(parent instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent).isStruct() && ((CSharpConstructorDeclaration)
								element).getParameters().length == 0)
						{
							return ((CSharpConstructorDeclaration) element).getNameIdentifier();
						}
					}
					return null;
				}
			}));
			add(new Feature("property initializer", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpPropertyDeclaration)
					{
						DotNetExpression initializer = ((CSharpPropertyDeclaration) element).getInitializer();
						if(initializer != null)
						{
							return initializer;
						}
					}
					return null;
				}
			}));
			add(new Feature("expression-bodied members", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpMethodDeclaration)
					{
						PsiElement codeBlock = ((CSharpMethodDeclaration) element).getCodeBlock();
						if(codeBlock instanceof DotNetExpression)
						{
							return codeBlock;
						}
					}
					else if(element instanceof CSharpFieldDeclaration)
					{
						ASTNode darrowNode = element.getNode().findChildByType(CSharpTokens.DARROW);
						if(darrowNode != null)
						{
							return darrowNode.getPsi();
						}
					}
					return null;
				}
			}));
			add(new Feature("exception filters", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpCatchStatementImpl)
					{
						return ((CSharpCatchStatementImpl) element).getFilterExpression();
					}
					return null;
				}
			}));
			add(new Feature("null propagation", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpReferenceExpression)
					{
						PsiElement memberAccessElement = ((CSharpReferenceExpression) element).getMemberAccessElement();
						if(memberAccessElement != null && memberAccessElement.getNode().getElementType() == CSharpTokens.NULLABE_CALL)
						{
							return memberAccessElement;
						}
					}
					return null;
				}
			}));
			add(new Feature("string interpolation", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpConstantExpressionImpl)
					{
						return ((CSharpConstantExpressionImpl) element).getLiteralType() == CSharpTokensImpl.INTERPOLATION_STRING_LITERAL ? element
								: null;
					}
					return null;
				}
			}));
			add(new Feature("dictionary initializer", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpDictionaryInitializerImpl)
					{
						return element;
					}
					return null;
				}
			}));
			add(new Feature("await in catch/finally", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					if(element instanceof CSharpAwaitExpressionImpl)
					{
						DotNetStatement statement = PsiTreeUtil.getParentOfType(element, CSharpFinallyStatementImpl.class,
								CSharpCatchStatementImpl.class);
						if(statement != null)
						{
							return ((CSharpAwaitExpressionImpl) element).getAwaitKeywordElement();
						}
						return null;
					}
					return null;
				}
			}));
			add(new Feature("asynchronous functions", CSharpLanguageVersion._4_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					return CS1998.getAsyncModifier(element);
				}
			}));
			add(new Feature("named arguments", CSharpLanguageVersion._4_0, new Function<PsiElement, PsiElement>()
			{
				@Override
				public PsiElement fun(PsiElement element)
				{
					return element instanceof CSharpNamedCallArgument ? element : null;
				}
			}));
		}
	};

	private TokenSet myAllKeywords = TokenSet.orSet(CSharpTokenSets.KEYWORDS, CSharpSoftTokens.ALL);

	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull PsiElement element)
	{
		for(Feature feature : myFeatures)
		{
			if(languageVersion.ordinal() < feature.myLanguageVersion.ordinal())
			{
				PsiElement fun = feature.myFunc.fun(element);
				if(fun == null)
				{
					continue;
				}

				CompilerCheckBuilder result = newBuilder(fun, feature.myName, languageVersion.getPresentableName());

				result.addQuickFix(new SetLanguageVersionFix(feature.myLanguageVersion));

				IElementType elementType = fun.getNode().getElementType();
				if(!myAllKeywords.contains(elementType))
				{
					boolean foundKeywordAndItSolo = false;
					ASTNode[] children = fun.getNode().getChildren(null);
					for(ASTNode child : children)
					{
						if(CSharpTokenSets.COMMENTS.contains(child.getElementType()) || CSharpTokenSets.WHITE_SPACE == child.getElementType())
						{
							continue;
						}

						if(myAllKeywords.contains(child.getElementType()))
						{
							foundKeywordAndItSolo = true;
						}
						else if(foundKeywordAndItSolo)  // if we found keyword but parent have other elements - we cant highlight as error
						{
							return result;
						}
					}

					if(foundKeywordAndItSolo)
					{
						result.setHighlightInfoType(HighlightInfoType.WRONG_REF);
					}
				}
				else
				{
					result.setHighlightInfoType(HighlightInfoType.WRONG_REF);
				}
				return result;
			}
		}
		return null;
	}
}
