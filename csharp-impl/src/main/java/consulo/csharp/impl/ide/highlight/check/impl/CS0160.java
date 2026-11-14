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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTryStatementImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import org.jspecify.annotations.Nullable;

/**
 * {@code catch (Exception) { } catch (IOException) { }} - a previous catch clause already catches all exceptions of this type
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0160 extends CompilerCheck<CSharpCatchStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpCatchStatementImpl element) {
        CSharpLocalVariable variable = element.getVariable();
        if (variable == null || variable.getType() == null || !(element.getParent() instanceof CSharpTryStatementImpl tryStatement)) {
            return null;
        }
        DotNetTypeRef typeRef = variable.toTypeRef(false);
        // a previous catch of this type or of a base type, without a filter
        for (CSharpCatchStatementImpl previous : tryStatement.getCatchStatements()) {
            if (previous == element) {
                return null;
            }
            CSharpLocalVariable previousVariable = previous.getVariable();
            if (previousVariable == null || previous.getFilterExpression() != null) {
                continue;
            }
            DotNetTypeRef previousTypeRef = previousVariable.toTypeRef(false);
            if (CSharpTypeUtil.isInheritable(previousTypeRef, typeRef)) {
                return newBuilder(variable.getType(), formatTypeRef(previousTypeRef));
            }
        }
        return null;
    }
}
