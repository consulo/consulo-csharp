/*
 * Copyright 2013-2026 consulo.io
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
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.source.CSharpArrayInitializerCompositeValueImpl;
import consulo.csharp.lang.impl.psi.source.CSharpArrayInitializerImpl;
import consulo.csharp.lang.impl.psi.source.CSharpArrayInitializerValue;
import consulo.csharp.lang.impl.psi.source.CSharpNewArrayLengthImpl;
import consulo.csharp.lang.impl.psi.source.CSharpNewExpressionImpl;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * {@code new int[2] { 1, 2, 3 }} - an array initializer of the length given is expected
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0847 extends CompilerCheck<CSharpNewExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpNewExpressionImpl element) {
        CSharpNewArrayLengthImpl[] lengths = element.getNewArrayLengths();
        CSharpArrayInitializerImpl initializer = element.getArrayInitializer();
        if (lengths.length != 1 || initializer == null) {
            return null;
        }
        DotNetExpression[] dimensions = lengths[0].getDimensionValues();
        // new int[] { 1, 2 } - no length given
        if (dimensions.length == 0) {
            return null;
        }
        int[] sizes = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            if (!(new ConstantExpressionEvaluator(dimensions[i]).getValue() instanceof Number number)) {
                return null;
            }
            sizes[i] = number.intValue();
        }
        CSharpArrayInitializerValue[] values = initializer.getValues();
        if (values.length != sizes[0]) {
            return newBuilder(initializer, sizes[0]);
        }
        // { { 1, 2 }, { 3 } } - the values of the second dimension
        if (sizes.length == 2) {
            for (CSharpArrayInitializerValue value : values) {
                if (value instanceof CSharpArrayInitializerCompositeValueImpl composite && PsiTreeUtil.getChildrenOfTypeAsList(composite, CSharpCallArgument.class).size() != sizes[1]) {
                    return newBuilder(composite, sizes[1]);
                }
            }
        }
        return null;
    }
}
