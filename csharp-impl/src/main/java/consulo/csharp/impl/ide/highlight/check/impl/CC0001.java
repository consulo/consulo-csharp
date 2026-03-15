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
import consulo.csharp.impl.ide.codeInsight.actions.CastNArgumentToTypeRefFix;
import consulo.csharp.impl.ide.codeInsight.actions.CreateUnresolvedConstructorFix;
import consulo.csharp.impl.ide.codeInsight.actions.CreateUnresolvedMethodFix;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.impl.ide.parameterInfo.CSharpParametersInfo;
import consulo.csharp.lang.doc.CSharpDocUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NErrorCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.QuickFixAction;
import consulo.language.editor.intention.QuickFixActionRegistrar;
import consulo.language.editor.intention.UnresolvedReferenceQuickFixProvider;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.*;
import consulo.localize.LocalizeValue;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.awt.util.ColorUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.xml.XmlStringUtil;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2014-11-19
 */
public class CC0001 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Override
    public List<HighlightInfoFactory> check(
        CSharpLanguageVersion languageVersion,
        CSharpHighlightContext highlightContext,
        CSharpReferenceExpression expression
    ) {
        PsiElement referenceElement = expression.getReferenceElement();
        if (referenceElement == null || expression.isSoft() || CSharpDocUtil.isInsideDoc(expression)) {
            return Collections.emptyList();
        }

        // if parent is call skip
        PsiElement parent = expression.getParent();
        if (parent instanceof CSharpMethodCallExpressionImpl || parent instanceof CSharpConstructorSuperCallImpl) {
            return Collections.emptyList();
        }

        return checkReference(expression, Arrays.asList(referenceElement));
    }

    @RequiredReadAction
    public static List<HighlightInfoFactory> checkReference(
        final PsiElement callElement,
        List<? extends PsiElement> ranges
    ) {
        if (ranges.isEmpty()) {
            return Collections.emptyList();
        }
        ResolveResult[] resolveResults = ResolveResult.EMPTY_ARRAY;

        boolean insideDoc = CSharpDocUtil.isInsideDoc(callElement);

        if (callElement instanceof PsiPolyVariantReference polyVariantRef) {
            resolveResults = polyVariantRef.multiResolve(true);
        }
        else if (callElement instanceof CSharpCallArgumentListOwner argListOwner) {
            resolveResults = argListOwner.multiResolve(true);
        }

        ResolveResult goodResult = CSharpResolveUtil.findFirstValidResult(resolveResults);

        List<HighlightInfoFactory> list = new ArrayList<>(2);
        if (goodResult == null) {
			if (callElement instanceof CSharpReferenceExpression refExpr
				&& refExpr.isPlaceholderReference()
				&& callElement.getParent() instanceof CSharpOutRefWrapExpressionImpl) {
				return Collections.emptyList();
			}

            if (resolveResults.length == 0) {
                for (PsiElement range : ranges) {
                    CompilerCheckBuilder result = new CompilerCheckBuilder() {
						@Nullable
                        @Override
                        @RequiredReadAction
                        public HighlightInfo create(boolean insideDoc) {
                            HighlightInfo highlightInfo = super.create(insideDoc);
                            if (highlightInfo != null) {
                                PsiReference reference = null;
                                if (callElement instanceof PsiReference ref) {
                                    reference = ref;
                                }
                                else if (callElement instanceof CSharpMethodCallExpressionImpl call
									&& call.getCallExpression() instanceof PsiReference ref) {
									reference = ref;
								}

                                if (reference != null) {
                                    UnresolvedReferenceQuickFixProvider.registerReferenceFixes(
                                        reference,
                                        QuickFixActionRegistrar.create(highlightInfo)
                                    );
                                }
                            }
                            return highlightInfo;
                        }
                    };
                    result.withHighlightInfoType(insideDoc ? HighlightInfoType.WEAK_WARNING : HighlightInfoType.WRONG_REF);

                    String unresolvedText = getUnresolvedText(callElement, range);
                    if (isCalleInsideCalle(callElement)) {
                        result.withText(LocalizeValue.localizeTODO("Expression cant be invoked"));
                        // remap to error, due we want make all exp red
                        result.withHighlightInfoType(insideDoc ? HighlightInfoType.WEAK_WARNING : HighlightInfoType.ERROR);
                    }
                    else {
                        result.withText(LocalizeValue.localizeTODO("'" + StringUtil.unescapeXml(unresolvedText) + "' is not resolved"));
                    }

                    result.withTextRange(range.getTextRange());
                    list.add(result);
                }
            }
            else {
                HighlightInfo highlightInfo = createHighlightInfo(callElement, resolveResults[0], insideDoc);
                if (highlightInfo == null) {
                    return list;
                }

                list.add((it) -> highlightInfo);
            }
        }
        return list;
    }

    @Contract("null -> false")
    @RequiredReadAction
    public static boolean isCalleInsideCalle(@Nullable PsiElement callElement) {
		return callElement instanceof CSharpMethodCallExpressionImpl call
			&& !(call.getCallExpression() instanceof CSharpReferenceExpression);
    }

    @RequiredReadAction
    private static String getUnresolvedText(PsiElement element, PsiElement range) {
        CSharpCallArgumentListOwner callOwner = findCallOwner(element);
        if (callOwner != null) {
            String name;
            if (element instanceof CSharpIndexAccessExpressionImpl) {
                name = "this";
            }
            else {
                name = range.getText();
            }

            StringBuilder builder = new StringBuilder();
            builder.append(name);
            char[] openAndCloseTokens = CSharpParametersInfo.getOpenAndCloseTokens(element);
            builder.append(openAndCloseTokens[0]);
            CSharpCallArgument[] arguments = callOwner.getCallArguments();
            for (int i = 0; i < arguments.length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }

                CSharpCallArgument callArgument = arguments[i];

                DotNetExpression argumentExpression = callArgument.getArgumentExpression();
                appendType(builder, argumentExpression == null ? DotNetTypeRef.ERROR_TYPE : argumentExpression.toTypeRef(false), element);
            }
            builder.append(openAndCloseTokens[1]);
            return builder.toString();
        }
        else {
            return range.getText();
        }
    }

    @Nullable
    @RequiredReadAction
    public static HighlightInfo createHighlightInfo(PsiElement element, ResolveResult resolveResult, boolean insideDoc) {
        if (!(resolveResult instanceof MethodResolveResult methodResolveResult)) {
            return null;
        }

        char[] openAndCloseTokens = CSharpParametersInfo.getOpenAndCloseTokens(element);

        PsiElement resolveElement = resolveResult.getElement();
        assert resolveElement != null;

        MethodResolvePriorityInfo calcResult = methodResolveResult.getCalcResult();
        List<NCallArgument> arguments = calcResult.getArguments();
        for (NCallArgument argument : arguments) {
            // missed arguments returning error type too - but we don't need to hide call error
            if (argument instanceof NErrorCallArgument) {
                continue;
            }

            if (CSharpTypeUtil.isErrorTypeRef(argument.getTypeRef())) {
                return null;
            }
        }

        CSharpCallArgumentListOwner callOwner = findCallOwner(element);
        if (callOwner != null) {
            StringBuilder tooltipBuilder = new StringBuilder();
            tooltipBuilder.append("<b>");
            // sometimes name can be null
            if (element instanceof CSharpOperatorReferenceImpl operatorRef) {
				XmlStringUtil.escapeText(operatorRef.getCanonicalText(), tooltipBuilder);
            }
            else if (element instanceof CSharpIndexAccessExpressionImpl) {
                tooltipBuilder.append(""); //FIXME [VISTALL] some name?
            }
            else {
                String name = ((PsiNamedElement) resolveElement).getName();
                tooltipBuilder.append(name);
            }

            tooltipBuilder.append("&#x9;").append(openAndCloseTokens[0]);

            if (resolveElement instanceof DotNetVariable variable) {
				if (!(variable.toTypeRef(false).resolve() instanceof CSharpLambdaResolveResult lambdaResolveResult)) {
                    return null;
                }
                DotNetTypeRef[] parameterTypes = lambdaResolveResult.getParameterTypeRefs();
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (i != 0) {
                        tooltipBuilder.append(", ");
                    }
                    appendType(tooltipBuilder, parameterTypes[i], element);
                }
            }
            else if (resolveElement instanceof DotNetLikeMethodDeclaration likeMethodDeclaration) {
                DotNetParameter[] parameters = likeMethodDeclaration.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (i != 0) {
                        tooltipBuilder.append(", ");
                    }
                    tooltipBuilder.append(parameters[i].getName()).append(" : ");
                    appendType(tooltipBuilder, parameters[i].toTypeRef(false), element);
                }
            }
            tooltipBuilder.append(openAndCloseTokens[1]).append("</b> cannot be applied<br>");

            tooltipBuilder.append("to&#x9;<b>").append(openAndCloseTokens[0]);

            for (int i = 0; i < arguments.size(); i++) {
                if (i != 0) {
                    tooltipBuilder.append(", ");
                }

                NCallArgument nCallArgument = arguments.get(i);

                if (!nCallArgument.isValid()) {
                    tooltipBuilder.append("<font color=\"").append(ColorUtil.toHex(JBColor.RED)).append("\">");
                }

                String parameterName = nCallArgument.getParameterName();
                if (parameterName != null) {
                    tooltipBuilder.append(parameterName).append(" : ");
                }

                appendType(tooltipBuilder, nCallArgument.getTypeRef(), element);
                if (!nCallArgument.isValid()) {
                    tooltipBuilder.append("</font>");
                }
            }

            tooltipBuilder.append(openAndCloseTokens[1]).append("</b>");

            PsiElement parameterList = callOwner.getParameterList();
            if (parameterList == null) {
                parameterList = callOwner;
            }

            HighlightInfo.Builder builder =
                HighlightInfo.newHighlightInfo(insideDoc ? HighlightInfoType.WEAK_WARNING : HighlightInfoType.ERROR);
            builder = builder.description(LocalizeValue.empty());
            builder = builder.escapedToolTip(tooltipBuilder.toString());
            builder = builder.range(parameterList);

            HighlightInfo highlightInfo = builder.create();
            if (highlightInfo != null) {
                registerQuickFixes(callOwner, resolveElement, arguments, highlightInfo);
            }
            return highlightInfo;
        }
        return null;
    }

    private static void registerQuickFixes(
        CSharpCallArgumentListOwner element,
        PsiElement resolveElement,
        List<NCallArgument> arguments,
        HighlightInfo highlightInfo
    ) {
        if (element instanceof CSharpMethodCallExpressionImpl call
			&& call.getCallExpression() instanceof CSharpReferenceExpression refExpr) {
            if (resolveElement instanceof CSharpMethodDeclaration) {
                QuickFixAction.registerQuickFixAction(highlightInfo, new CreateUnresolvedMethodFix(refExpr));
            }
			else if (resolveElement instanceof CSharpConstructorDeclaration) {
                QuickFixAction.registerQuickFixAction(highlightInfo, new CreateUnresolvedConstructorFix(refExpr));
            }
        }

        for (NCallArgument argument : arguments) {
            if (!argument.isValid()) {
                CSharpCallArgument callArgument = argument.getCallArgument();
                if (callArgument == null) {
                    continue;
                }
                DotNetExpression argumentExpression = callArgument.getArgumentExpression();
                if (argumentExpression == null) {
                    continue;
                }
                DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
                if (parameterTypeRef == null) {
                    continue;
                }
                String parameterName = argument.getParameterName();
                if (parameterName == null) {
                    continue;
                }
                QuickFixAction.registerQuickFixAction(
                    highlightInfo,
                    new CastNArgumentToTypeRefFix(argumentExpression, parameterTypeRef, parameterName)
                );
            }
        }
    }

    @RequiredReadAction
    private static void appendType(StringBuilder builder, DotNetTypeRef typeRef, PsiElement scope) {
        if (typeRef == DotNetTypeRef.ERROR_TYPE) {
            builder.append("?");
        }
        else {
            XmlStringUtil.escapeText(CSharpTypeRefPresentationUtil.buildText(typeRef), builder);
        }
    }

    private static CSharpCallArgumentListOwner findCallOwner(PsiElement element) {
        PsiElement parent = element.getParent();
        if (element instanceof CSharpOperatorReferenceImpl ||
            element instanceof CSharpMethodCallExpressionImpl ||
            element instanceof CSharpConstructorSuperCallImpl ||
            element instanceof CSharpIndexAccessExpressionImpl) {
            return (CSharpCallArgumentListOwner) element;
        }
        else if (parent instanceof CSharpAttribute attribute) {
            return attribute;
        }
        else if (parent instanceof DotNetUserType && parent.getParent() instanceof CSharpNewExpression newExpr) {
            return newExpr;
        }
        return null;
    }
}
