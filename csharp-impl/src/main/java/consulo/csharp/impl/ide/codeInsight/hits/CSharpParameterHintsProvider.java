package consulo.csharp.impl.ide.codeInsight.hits;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.lang.psi.CSharpConstantUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionWithOperatorImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NErrorCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NNamedCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NParamsCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.Language;
import consulo.language.editor.inlay.HintInfo;
import consulo.language.editor.inlay.InlayInfo;
import consulo.language.editor.inlay.InlayParameterHintsProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 16-Jan-17
 */
@ExtensionImpl
public class CSharpParameterHintsProvider implements InlayParameterHintsProvider {
    private static Set<String> ourDefaultBlacklist = Set.of(
        "*.Write(*)",
        "*.WriteLine(*)",
        "System.Func*",
        "System.Action*",
        "*.Add(value)",
        "*.Add(item)",
        "*.Add(key, value)",
        "*.TryParse(*, *)"
    );

    @Nonnull
    @Override
    @RequiredReadAction
    public List<InlayInfo> getParameterHints(@Nonnull PsiElement psiElement) {
        if (psiElement instanceof CSharpExpressionWithOperatorImpl || psiElement instanceof CSharpOperatorReferenceImpl) {
            return Collections.emptyList();
        }

        if (psiElement instanceof CSharpCallArgumentListOwner) {
            PsiElement callable = ((CSharpCallArgumentListOwner) psiElement).resolveToCallable();
            if (!(callable instanceof CSharpMethodDeclaration) && !(callable instanceof CSharpConstructorDeclaration) && !(callable instanceof DotNetVariable)) {
                return Collections.emptyList();
            }

            List<InlayInfo> list = new SmartList<>();
            CSharpCallArgument[] callArguments = ((CSharpCallArgumentListOwner) psiElement).getCallArguments();

            List<NCallArgument> argumentList = buildCallArguments(callArguments, callable, callable);

            for (NCallArgument nCallArgument : argumentList) {
                if (nCallArgument instanceof NNamedCallArgument || nCallArgument instanceof NErrorCallArgument || nCallArgument instanceof NParamsCallArgument) {
                    continue;
                }

                PsiElement parameterElement = nCallArgument.getParameterElement();
                DotNetExpression argumentExpression = nCallArgument.getCallArgument() == null ? null : nCallArgument.getCallArgument().getArgumentExpression();
                if (!CSharpConstantUtil.isConstant(argumentExpression) || parameterElement == null) {
                    continue;
                }

                String parameterName = nCallArgument.getParameterName();
                if (parameterName == null) {
                    continue;
                }

                list.add(new InlayInfo(parameterName, argumentExpression.getTextOffset()));
            }
            return list;
        }
        return Collections.emptyList();
    }

    @RequiredReadAction
    private static List<NCallArgument> buildCallArguments(@Nonnull CSharpCallArgument[] callArguments, @Nonnull PsiElement callable, @Nonnull PsiElement scopedElement) {
        if (callable instanceof DotNetVariable) {
            DotNetTypeRef ref = ((DotNetVariable) callable).toTypeRef(true);
            DotNetTypeResolveResult resolve = ref.resolve();
            if (resolve instanceof CSharpLambdaResolveResult) {
                return NCallArgumentBuilder.buildCallArguments(scopedElement.getProject(), callArguments, ((CSharpLambdaResolveResult) resolve).getParameterInfos(), scopedElement.getResolveScope());
            }
        }
        else if (callable instanceof DotNetParameterListOwner) {
            return NCallArgumentBuilder.buildCallArguments(callArguments, (DotNetParameterListOwner) callable, scopedElement.getResolveScope());
        }

        return Collections.emptyList();
    }

    @Nullable
    @Override
    @RequiredReadAction
    public HintInfo.MethodInfo getHintInfo(@Nonnull PsiElement call) {
        PsiElement callable;
        if (call instanceof CSharpCallArgumentListOwner) {
            callable = ((CSharpCallArgumentListOwner) call).resolveToCallable();
            if (!(callable instanceof CSharpMethodDeclaration) && !(callable instanceof DotNetVariable) && !(callable instanceof CSharpConstructorDeclaration)) {
                return null;
            }
        }
        else {
            return null;
        }

        String name = null;
        CSharpSimpleParameterInfo[] params = CSharpSimpleParameterInfo.EMPTY_ARRAY;
        if (callable instanceof DotNetVariable) {
            DotNetTypeRef ref = ((DotNetVariable) callable).toTypeRef(true);
            DotNetTypeResolveResult resolve = ref.resolve();
            if (resolve instanceof CSharpLambdaResolveResult) {
                name = ((CSharpLambdaResolveResult) resolve).getTarget().getPresentableQName();
                params = ((CSharpLambdaResolveResult) resolve).getParameterInfos();
            }
            else {
                return null;
            }
        }
        else if (callable instanceof CSharpMethodDeclaration || callable instanceof CSharpConstructorDeclaration) {
            params = ((CSharpSimpleLikeMethodAsElement) callable).getParameterInfos();

            CSharpTypeDeclaration declaration = PsiTreeUtil.getParentOfType(callable, CSharpTypeDeclaration.class);
            if (declaration != null) {
                name = declaration.getPresentableQName() + ".";
            }
            name += ((PsiNamedElement) callable).getName();
        }

        List<String> paramNames = ContainerUtil.map(params, CSharpSimpleParameterInfo::getNotNullName);
        return new HintInfo.MethodInfo(name, paramNames);
    }

    @Nonnull
    @Override
    public String getInlayPresentation(@Nonnull String inlayText) {
        return inlayText + "=";
    }

    @Nonnull
    @Override
    public LocalizeValue getPreviewFileText() {
        return LocalizeValue.of();
    }

    @Nonnull
    @Override
    public Set<String> getDefaultBlackList() {
        return ourDefaultBlacklist;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
