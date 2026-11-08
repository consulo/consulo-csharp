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

package consulo.csharp.lang.impl.psi.controlFlow;

import consulo.dotnet.psi.DotNetVariable;
import consulo.language.controlFlow.ControlFlowBuilder;
import consulo.language.psi.PsiElement;

/**
 * A read or a write of a variable at a reference or a declaration, or a declaration without a value - the variable is
 * unassigned again, each time its scope is entered (a local of a loop body)
 *
 * @author VISTALL
 */
public class CSharpReadWriteInstruction extends CSharpInstruction {
    public enum Access {
        READ,
        WRITE,
        DECLARE
    }

    private final DotNetVariable myVariable;
    private final Access myAccess;

    public CSharpReadWriteInstruction(ControlFlowBuilder builder, PsiElement element, DotNetVariable variable, Access access) {
        super(builder, element, null);
        myVariable = variable;
        myAccess = access;
    }

    public DotNetVariable getVariable() {
        return myVariable;
    }

    public Access getAccess() {
        return myAccess;
    }

    @Override
    public String getElementPresentation() {
        return myAccess + " " + myVariable.getName();
    }
}
