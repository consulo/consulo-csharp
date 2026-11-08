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

package consulo.csharp.lang.impl.psi.source.resolve.handlers;


import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.SimpleNamedScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import org.jspecify.annotations.Nullable;

/**
 * The labels a goto sees - of its block and the blocks around it, up to the method, lambda or local function
 * (C# spec 7.7.1: the scope of a label is the block declaring it, nested blocks included - not the ones in them)
 *
 * @author VISTALL
 * @since 27.07.2015
 */
public class LabelKindProcessor implements KindProcessor {
    @RequiredReadAction
    @Override
    public void process(CSharpResolveOptions options,
                        DotNetGenericExtractor defaultExtractor,
                        @Nullable PsiElement forceQualifierElement,
                        Processor<ResolveResult> processor) {
        PsiElement element = options.getElement();

        SimpleNamedScopeProcessor scopeProcessor = new SimpleNamedScopeProcessor(processor, options.isCompletion(), ExecuteTarget.LABEL);

        ResolveState state = ResolveState.initial();
        state = state.put(CSharpResolveUtil.SELECTOR, options.getSelector());
        for (PsiElement parent = element.getParent(); parent != null && !(parent instanceof CSharpSimpleLikeMethodAsElement); parent = parent.getParent()) {
            if (parent instanceof CSharpBlockStatementImpl && !processLabels(scopeProcessor, parent, state)) {
                return;
            }
        }
    }

    /**
     * The labeled statements of the block - a labeled statement holds the statements after it, the next labels too
     */
    @RequiredReadAction
    private static boolean processLabels(SimpleNamedScopeProcessor processor, PsiElement parent, ResolveState state) {
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpLabeledStatementImpl label && (!processor.execute(label, state) || !processLabels(processor, label, state))) {
                return false;
            }
        }
        return true;
    }
}
