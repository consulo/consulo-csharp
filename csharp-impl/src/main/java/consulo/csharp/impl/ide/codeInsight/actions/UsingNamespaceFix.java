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

package consulo.csharp.impl.ide.codeInsight.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processors;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.libraryAnalyzer.NamespaceReference;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.msil.MsilToCSharpUtil;
import consulo.csharp.lang.impl.psi.resolve.AttributeByNameSelector;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.impl.psi.stub.index.ExtensionMethodIndex;
import consulo.csharp.lang.impl.psi.stub.index.MethodIndex;
import consulo.csharp.lang.psi.*;
import consulo.document.util.TextRange;
import consulo.dotnet.DotNetBundle;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.impl.resolve.GlobalSearchScopeFilter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.AutoImportHelper;
import consulo.language.editor.hint.HintManager;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.editor.intention.HintAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Condition;
import consulo.util.lang.function.Conditions;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class UsingNamespaceFix implements HintAction, HighPriorityAction, SyntheticIntentionAction {
  enum PopupResult {
    NOT_AVAILABLE,
    SHOW_HIT,
    SHOW_ACTION
  }

  private final SmartPsiElementPointer<CSharpReferenceExpression> myRefPointer;

  public UsingNamespaceFix(@Nonnull CSharpReferenceExpression ref) {
    myRefPointer = SmartPointerManager.getInstance(ref.getProject()).createSmartPsiElementPointer(ref);
  }

  @Nonnull
  @RequiredReadAction
  public PopupResult doFix(Editor editor) {
    CSharpReferenceExpression element = myRefPointer.getElement();
    if (element == null) {
      return PopupResult.NOT_AVAILABLE;
    }

    CSharpReferenceExpression.ResolveToKind kind = element.kind();
    if (!isValidReference(kind, element)) {
      return PopupResult.NOT_AVAILABLE;
    }

    Set<NamespaceReference> references = collectAllAvailableNamespaces(element, kind);
    if (references.isEmpty()) {
      return PopupResult.NOT_AVAILABLE;
    }

    AddUsingAction action = new AddUsingAction(editor, element, references);
    String message = AutoImportHelper.getInstance(element.getProject())
                                     .getImportMessage(references.size() != 1,
                                                       DotNetBundle.message("use.popup",
                                                                            AddUsingAction.formatMessage(references.iterator().next())));

    PsiElement referenceElement = element.getReferenceElement();
    assert referenceElement != null;
    TextRange referenceTextRange = referenceElement.getTextRange();
    HintManager.getInstance()
               .showQuestionHint(editor, message, referenceTextRange.getStartOffset(), referenceTextRange.getEndOffset(), action);

    return PopupResult.SHOW_HIT;
  }

  @RequiredReadAction
  public static boolean isValidReference(CSharpReferenceExpression.ResolveToKind kind, CSharpReferenceExpression expression) {
    PsiElement resolvedElement = expression.resolve();
    if (resolvedElement != null) {
      return false;
    }

    switch (kind) {
      case TYPE_LIKE:
      case ANY_MEMBER:
        if (expression.getQualifier() != null) {
          return false;
        }
        return true;
      case CONSTRUCTOR:
        if (expression.getQualifier() != null) {
          return false;
        }
        PsiElement parent = expression.getParent();
        if (parent instanceof CSharpAttribute) {
          return true;
        }
        else if (parent instanceof CSharpUserType) {
          if (!(parent.getParent() instanceof CSharpNewExpression)) {
            return false;
          }
          else {
            return true;
          }
        }
        return false;
      case METHOD:
        return expression.getQualifier() != null;
    }
    return false;
  }

  @Nonnull
  @RequiredReadAction
  private static Set<NamespaceReference> collectAllAvailableNamespaces(CSharpReferenceExpression ref,
                                                                       CSharpReferenceExpression.ResolveToKind kind) {
    if (PsiTreeUtil.getParentOfType(ref, CSharpUsingListChild.class) != null || !ref.isValid()) {
      return Collections.emptySet();
    }
    String referenceName = ref.getReferenceName();
    if (StringUtil.isEmpty(referenceName)) {
      return Collections.emptySet();
    }
    Set<NamespaceReference> resultSet = new LinkedHashSet<>();
    if (kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || kind == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR || ref.getQualifier() == null) {
      collectAvailableNamespaces(ref, resultSet, referenceName);
    }
    if (kind == CSharpReferenceExpression.ResolveToKind.METHOD) {
      collectAvailableNamespacesForMethodExtensions(ref, resultSet, referenceName);
    }

//		Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(ref);
//		if(moduleForPsiElement != null)
//		{
//			resultSet.addAll(DotNetLibraryAnalyzerComponent.getInstance(moduleForPsiElement.getProject()).get(moduleForPsiElement, referenceName));
//		}
    return resultSet;
  }

  @RequiredReadAction
  private static void collectAvailableNamespaces(final CSharpReferenceExpression ref, Set<NamespaceReference> set, String referenceName) {
    if (ref.getQualifier() != null) {
      return;
    }
    Collection<DotNetTypeDeclaration> tempTypes;
    Collection<DotNetLikeMethodDeclaration> tempMethods;

    PsiElement parent = ref.getParent();
    if (parent instanceof CSharpAttribute) {
      final Condition<DotNetTypeDeclaration> cond = typeDeclaration -> DotNetInheritUtil.isAttribute(typeDeclaration);

      tempTypes = getTypesWithGeneric(ref, referenceName);
      collect(set, tempTypes, cond);

      tempTypes = getTypesWithGeneric(ref, referenceName + AttributeByNameSelector.AttributeSuffix);
      collect(set, tempTypes, cond);
    }
    else {
      tempTypes = getTypesWithGeneric(ref, referenceName);

      collect(set, tempTypes, Conditions.<DotNetTypeDeclaration>alwaysTrue());

      tempMethods = MethodIndex.getInstance().get(referenceName, ref.getProject(), ref.getResolveScope());

      collect(set,
              tempMethods,
              method -> (method.getParent() instanceof DotNetNamespaceDeclaration || method.getParent() instanceof PsiFile) && method instanceof CSharpMethodDeclaration && (
                (CSharpMethodDeclaration)method).isDelegate());
    }
  }

  private static Collection<DotNetTypeDeclaration> getTypesWithGeneric(CSharpReferenceExpression ref, final String refName) {
    List<DotNetTypeDeclaration> collection = new ArrayList<>();

    GlobalSearchScopeFilter filter = new GlobalSearchScopeFilter(ref.getResolveScope());

    DotNetShortNameSearcher.getInstance(ref.getProject())
                           .collectTypes(refName, ref.getResolveScope(), filter, Processors.cancelableCollectProcessor(collection));

    return collection;
  }

  @RequiredReadAction
  private static void collectAvailableNamespacesForMethodExtensions(CSharpReferenceExpression ref,
                                                                    Set<NamespaceReference> set,
                                                                    String referenceName) {
    PsiElement qualifier = ref.getQualifier();
    if (qualifier == null) {
      return;
    }

    PsiElement parent = ref.getParent();
    if (!(parent instanceof CSharpMethodCallExpressionImpl)) {
      return;
    }

    DotNetExpression callQualifier = ref.getQualifier();

    DotNetTypeRef qualifierTypeRef = callQualifier.toTypeRef(true);

    Collection<DotNetLikeMethodDeclaration> list =
      ExtensionMethodIndex.getInstance().get(referenceName.hashCode(), ref.getProject(), ref.getResolveScope());

    for (DotNetLikeMethodDeclaration it : list) {
      ProgressManager.checkCanceled();

      DotNetLikeMethodDeclaration method = null;

      if (it instanceof MsilMethodEntry) {
        PsiElement msilClass = it.getParent();

        if (msilClass instanceof MsilClassEntry) {
          PsiElement wrap = MsilToCSharpUtil.wrap(msilClass, null);

          if (wrap instanceof CSharpTypeDeclaration) {
            DotNetNamedElement[] members = ((CSharpTypeDeclaration)wrap).getMembers();
            for (DotNetNamedElement member : members) {
              if (member.getOriginalElement() == it) {
                method = (DotNetLikeMethodDeclaration)member;
                break;
              }
            }
          }
        }
      }
      else {
        method = it;
      }

      if (method == null) {
        continue;
      }

      DotNetParameter[] parameters = method.getParameters();
      if (parameters.length == 0) {
        continue;
      }

      DotNetParameter parameter = parameters[0];
      DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
      DotNetGenericParameter[] genericParameters = method.getGenericParameters();
      // if method have generic parameters - extract this by default generic parameter type for correct check #isInheritable()
      // due generic parameter T is not inheritable by other types
      if (genericParameters.length > 0) {
        Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<>(genericParameters.length);
        for (DotNetGenericParameter genericParameter : genericParameters) {
          DotNetTypeRef[] extendTypes = CSharpGenericConstraintUtil.getExtendTypes(genericParameter);

          map.put(genericParameter,
                  extendTypes.length == 1 ? extendTypes[0] : new CSharpTypeRefByQName(qualifier, DotNetTypes.System.Object));
        }
        extractor = CSharpGenericExtractor.create(map);
      }

      DotNetTypeRef extractedParameterTypeRef = GenericUnwrapTool.exchangeTypeRef(parameter.toTypeRef(true), extractor);

      if (CSharpTypeUtil.isInheritable(extractedParameterTypeRef, qualifierTypeRef)) {
        PsiElement parentOfMethod = method.getParent();
        if (parentOfMethod instanceof DotNetQualifiedElement) {
          set.add(new NamespaceReference(((DotNetQualifiedElement)parentOfMethod).getPresentableParentQName(), null));
        }
      }
    }
  }

  @RequiredReadAction
  private static <T extends DotNetQualifiedElement> void collect(Set<NamespaceReference> result,
                                                                 Collection<T> element,
                                                                 Condition<T> condition) {
    for (T type : element) {
      String presentableParentQName = type.getPresentableParentQName();
      if (StringUtil.isEmpty(presentableParentQName)) {
        continue;
      }

      ProgressManager.checkCanceled();

      if (!condition.value(type)) {
        continue;
      }

      result.add(new NamespaceReference(presentableParentQName, null));
    }
  }

  @Override
  public boolean showHint(@Nonnull Editor editor) {
    return doFix(editor) == PopupResult.SHOW_HIT;
  }

  @Nonnull
  @Override
  public String getText() {
    return DotNetBundle.message("add.using");
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile psiFile) {
    CSharpReferenceExpression element = myRefPointer.getElement();
    if (element == null) {
      return false;
    }
    CSharpReferenceExpression.ResolveToKind kind = element.kind();
    return !collectAllAvailableNamespaces(element, kind).isEmpty();
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {

  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
