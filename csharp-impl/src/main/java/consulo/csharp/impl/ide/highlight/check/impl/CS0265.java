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
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;

import java.util.*;

/**
 * Partial declarations have inconsistent constraints for type parameter - two parts of other constraints of it; one
 * without any is of the others; see {@link CS0260}
 *
 * @author VISTALL
 */
public class CS0265 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<CSharpTypeDeclaration> parts = CS0260.parts(element);
        if (!CS0260.isFirst(element, parts)) {
            return List.of();
        }
        DotNetGenericParameter[] parameters = element.getGenericParameters();
        List<Set<String>> constraints = new ArrayList<>(Collections.nCopies(parameters.length, null));
        Set<Integer> conflicts = new TreeSet<>();
        for (CSharpTypeDeclaration part : parts) {
            for (CSharpGenericConstraint constraint : part.getGenericConstraints()) {
                DotNetGenericParameter parameter = constraint.resolve();
                int index = parameter == null ? -1 : parameter.getIndex();
                if (index < 0 || index >= parameters.length) {
                    continue;
                }
                Set<String> values = values(constraint);
                if (constraints.get(index) == null) {
                    constraints.set(index, values);
                }
                else if (!constraints.get(index).equals(values)) {
                    conflicts.add(index);
                }
            }
        }

        List<HighlightInfoFactory> results = new ArrayList<>();
        for (int index : conflicts) {
            results.add(newBuilder(getNameIdentifier(element), formatElement(element), parameters[index].getName()));
        }
        return results;
    }

    @RequiredReadAction
    private static Set<String> values(CSharpGenericConstraint constraint) {
        Set<String> values = new HashSet<>();
        for (CSharpGenericConstraintValue value : constraint.getGenericConstraintValues()) {
            if (value instanceof CSharpGenericConstraintTypeValue typeValue) {
                values.add(formatTypeRef(typeValue.toTypeRef()));
            }
            else if (value instanceof CSharpGenericConstraintKeywordValue keywordValue) {
                values.add(String.valueOf(keywordValue.getKeywordElementType()));
            }
        }
        return values;
    }
}
