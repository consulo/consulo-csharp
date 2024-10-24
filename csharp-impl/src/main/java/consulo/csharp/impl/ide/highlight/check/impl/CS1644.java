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
import consulo.annotation.access.RequiredWriteAction;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS1644 extends CompilerCheck<PsiElement> {
  public static class SetLanguageVersionFix extends PsiElementBaseIntentionAction implements SyntheticIntentionAction {
    private CSharpLanguageVersion myLanguageVersion;

    public SetLanguageVersionFix(CSharpLanguageVersion languageVersion) {
      myLanguageVersion = languageVersion;
      setText("Set language version to '" + myLanguageVersion.getPresentableName() + "'");
    }

    @Override
    @RequiredWriteAction
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
      CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpSimpleModuleExtension.class);
      if (extension == null || !extension.isSupportedLanguageVersion(myLanguageVersion)) {
        return;
      }

      ModuleRootManager rootManager = ModuleRootManager.getInstance(extension.getModule());

      ModifiableRootModel modifiableModel = rootManager.getModifiableModel();

      final CSharpMutableModuleExtension mutable = modifiableModel.getExtension(CSharpMutableModuleExtension.class);
      assert mutable != null;
      mutable.setLanguageVersion(myLanguageVersion);

      modifiableModel.commit();
    }

    @Override
    @RequiredUIAccess
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
      CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpSimpleModuleExtension.class);
      return extension != null && extension.isSupportedLanguageVersion(myLanguageVersion) && extension.getLanguageVersion()
                                                                                                      .ordinal() < myLanguageVersion.ordinal();
    }
  }

  public static class Feature {
    private String myName;
    private CSharpLanguageVersion myLanguageVersion;
    private Function<PsiElement, PsiElement> myFunc;

    Feature(String name, CSharpLanguageVersion languageVersion, @RequiredReadAction Function<PsiElement, PsiElement> processor) {
      myName = name;
      myLanguageVersion = languageVersion;
      myFunc = processor;
    }
  }

  private List<Feature> myFeatures = new ArrayList<Feature>() {
    {
      add(new Feature("lambda expressions", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpLambdaExpressionImpl) {
            return element;
          }
          return null;
        }
      }));
      add(new Feature("generics", CSharpLanguageVersion._2_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpGenericParameterListImpl) {
            return element;
          }
          else if (element.getNode() != null && (element.getNode().getElementType() == CSharpElements.TYPE_ARGUMENTS || element.getNode()
                                                                                                                               .getElementType() == CSharpStubElements
            .TYPE_ARGUMENTS)) {
            return element;
          }
          return null;
        }
      }));
      add(new Feature("implicitly typed local variable", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpLocalVariable && ((CSharpLocalVariable)element).toTypeRef(false) == DotNetTypeRef.AUTO_TYPE) {
            return ((CSharpLocalVariable)element).getType();
          }
          return null;
        }
      }));
      add(new Feature("extension methods", CSharpLanguageVersion._3_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpMethodDeclaration) {
            DotNetParameter[] parameters = ((CSharpMethodDeclaration)element).getParameters();
            if (parameters.length > 0) {
              DotNetModifierList modifierList = parameters[0].getModifierList();
              if (modifierList != null) {
                PsiElement modifier = modifierList.getModifierElement(CSharpModifier.THIS);
                if (modifier != null) {
                  return modifier;
                }
              }
            }
          }
          return null;
        }
      }));
      add(new Feature("using static members", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          return element instanceof CSharpUsingTypeStatement ? element : null;
        }
      }));
      add(new Feature("parameterless struct ctors", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpConstructorDeclaration) {
            PsiElement parent = element.getParent();
            if (parent instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration)parent).isStruct() && ((CSharpConstructorDeclaration)element)
              .getParameters().length == 0) {
              return ((CSharpConstructorDeclaration)element).getNameIdentifier();
            }
          }
          return null;
        }
      }));
      add(new Feature("property initializer", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpPropertyDeclaration) {
            DotNetExpression initializer = ((CSharpPropertyDeclaration)element).getInitializer();
            if (initializer != null) {
              return initializer;
            }
          }
          return null;
        }
      }));
      add(new Feature("expression-bodied members", CSharpLanguageVersion._6_0, element -> {
        if (element instanceof CSharpMethodDeclaration) {
          return ((CSharpMethodDeclaration)element).getCodeBlock().asExpression();
        }
        else if (element instanceof CSharpFieldDeclaration) {
          ASTNode darrowNode = element.getNode().findChildByType(CSharpTokens.DARROW);
          if (darrowNode != null) {
            return darrowNode.getPsi();
          }
        }
        return null;
      }));
      add(new Feature("exception filters", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpCatchStatementImpl) {
            return ((CSharpCatchStatementImpl)element).getFilterExpression();
          }
          return null;
        }
      }));
      add(new Feature("null propagation", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpReferenceExpression) {
            PsiElement memberAccessElement = ((CSharpReferenceExpression)element).getMemberAccessElement();
            if (memberAccessElement != null && memberAccessElement.getNode().getElementType() == CSharpTokens.NULLABE_CALL) {
              return memberAccessElement;
            }
          }
          return null;
        }
      }));
      add(new Feature("string interpolation", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpConstantExpressionImpl) {
            return ((CSharpConstantExpressionImpl)element).getLiteralType() == CSharpTokensImpl.INTERPOLATION_STRING_LITERAL ? element : null;
          }
          return null;
        }
      }));
      add(new Feature("dictionary initializer", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpDictionaryInitializerImpl) {
            return element;
          }
          return null;
        }
      }));
      add(new Feature("await in catch/finally", CSharpLanguageVersion._6_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpAwaitExpressionImpl) {
            DotNetStatement statement =
              PsiTreeUtil.getParentOfType(element, CSharpFinallyStatementImpl.class, CSharpCatchStatementImpl.class);
            if (statement != null) {
              return ((CSharpAwaitExpressionImpl)element).getAwaitKeywordElement();
            }
            return null;
          }
          return null;
        }
      }));
      add(new Feature("asynchronous functions", CSharpLanguageVersion._4_0, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement element) {
          if (element instanceof CSharpSimpleLikeMethodAsElement) {
            DotNetModifierList modifierList = ((CSharpSimpleLikeMethodAsElement)element).getModifierList();
            if (modifierList == null) {
              return null;
            }
            return modifierList.getModifierElement(CSharpModifier.ASYNC);
          }
          return null;
        }
      }));
      add(new Feature("named arguments", CSharpLanguageVersion._4_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement element) {
          return element instanceof CSharpNamedCallArgument ? element : null;
        }
      }));
      add(new Feature("tuples", CSharpLanguageVersion._7_0, new Function<PsiElement, PsiElement>() {
        @Override
        public PsiElement apply(PsiElement psiElement) {
          return psiElement instanceof CSharpTupleType || psiElement instanceof CSharpTupleExpressionImpl ? psiElement : null;
        }
      }));
      add(new Feature("default literal", CSharpLanguageVersion._7_1, new Function<PsiElement, PsiElement>() {
        @Override
        @RequiredReadAction
        public PsiElement apply(PsiElement psiElement) {
          return psiElement instanceof CSharpDefaultExpressionImpl && ((CSharpDefaultExpressionImpl)psiElement).isSimplified() ? psiElement : null;
        }
      }));
      add(new Feature("expression body property accessor", CSharpLanguageVersion._7_0, element -> {
        if (element instanceof DotNetXAccessor) {
          return ((DotNetXAccessor)element).getCodeBlock().asExpression();
        }
        return null;
      }));
      add(new Feature("pattern matching", CSharpLanguageVersion._7_0, element ->
      {
        if (element instanceof CSharpIsVariableImpl) {
          return element;
        }
        else if (element instanceof CSharpCasePatternStatementImpl) {
          return element;
        }
        else {
          return null;
        }
      }));
    }
  };

  private TokenSet myAllKeywords = TokenSet.orSet(CSharpTokenSets.KEYWORDS, CSharpSoftTokens.ALL);

  @RequiredReadAction
  @Nullable
  @Override
  public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull PsiElement element) {
    for (Feature feature : myFeatures) {
      if (languageVersion.ordinal() < feature.myLanguageVersion.ordinal()) {
        PsiElement fun = feature.myFunc.apply(element);
        if (fun == null) {
          continue;
        }

        CompilerCheckBuilder result = newBuilder(fun, feature.myName, languageVersion.getPresentableName());

        result.withQuickFix(new SetLanguageVersionFix(feature.myLanguageVersion));

        IElementType elementType = fun.getNode().getElementType();
        if (!myAllKeywords.contains(elementType)) {
          boolean foundKeywordAndItSolo = false;
          ASTNode[] children = fun.getNode().getChildren(null);
          for (ASTNode child : children) {
            if (CSharpTokenSets.COMMENTS.contains(child.getElementType()) || child.getElementType() == CSharpTokenSets.WHITE_SPACE) {
              continue;
            }

            if (myAllKeywords.contains(child.getElementType())) {
              foundKeywordAndItSolo = true;
            }
            else if (foundKeywordAndItSolo)  // if we found keyword but parent have other elements - we cant highlight as error
            {
              return result;
            }
          }

          if (foundKeywordAndItSolo) {
            result.withHighlightInfoType(HighlightInfoType.WRONG_REF);
          }
        }
        else {
          result.withHighlightInfoType(HighlightInfoType.WRONG_REF);
        }
        return result;
      }
    }
    return null;
  }
}
