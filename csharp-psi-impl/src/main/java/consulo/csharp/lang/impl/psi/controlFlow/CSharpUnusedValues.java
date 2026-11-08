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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.csharp.lang.impl.psi.source.CSharpFixedStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpUsingStatementImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.controlFlow.Instruction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.*;

/**
 * The values assigned to a variable of the owner and never read after (Roslyn's IDE0059) - live variables computed
 * backward over the graph. A variable a lambda or a local function reads is not looked at (when they run is not
 * known; a write there is not seen, it can only keep a value used), nor a ref or out parameter (its caller reads it),
 * nor a using or fixed one.
 *
 * @author VISTALL
 */
public final class CSharpUnusedValues {
    /**
     * An assignment whose value is never read - at the declaration (an initializer, a pattern, out var) or a reference
     */
    public record Write(PsiElement element, DotNetVariable variable) {
    }

    private CSharpUnusedValues() {
    }

    @RequiredReadAction
    static List<Write> compute(CSharpControlFlow flow) {
        PsiElement owner = flow.getOwner();
        Instruction[] instructions = flow.getInstructions();

        Set<DotNetVariable> captured = captured(owner);
        Map<DotNetVariable, Integer> indexes = new HashMap<>();
        for (Instruction instruction : instructions) {
            if (instruction instanceof CSharpReadWriteInstruction readWrite && !indexes.containsKey(readWrite.getVariable())) {
                DotNetVariable variable = readWrite.getVariable();
                if (CSharpFlowVariables.ownerOf(variable) == owner && !captured.contains(variable) && isChecked(variable)) {
                    indexes.put(variable, indexes.size());
                }
            }
        }
        if (indexes.isEmpty()) {
            return List.of();
        }

        BitSet[] liveOut = liveOut(flow, instructions, indexes);
        if (liveOut == null) {
            // too complex
            return List.of();
        }

        // a write is unused when none of its copies (a finally block built more than once) is read after
        Map<Write, Boolean> writes = new LinkedHashMap<>();
        for (Instruction instruction : instructions) {
            if (!(instruction instanceof CSharpReadWriteInstruction readWrite) || readWrite.getAccess() != CSharpReadWriteInstruction.Access.WRITE
                || !flow.isReachable(instruction)) {
                continue;
            }
            Integer index = indexes.get(readWrite.getVariable());
            // the value of a parameter at the entry
            if (index == null || readWrite.getElement() == readWrite.getVariable() && readWrite.getVariable() instanceof DotNetParameter) {
                continue;
            }
            boolean unused = !liveOut[instruction.num()].get(index);
            writes.merge(new Write(readWrite.getElement(), readWrite.getVariable()), unused, Boolean::logicalAnd);
        }

        List<Write> result = new ArrayList<>();
        writes.forEach((write, unused) -> {
            if (unused) {
                result.add(write);
            }
        });
        return result;
    }

    /**
     * live out = the union of the live ins of the successors, live in = live out without the written variable, with the
     * read one - till nothing changes; null if it does not settle
     */
    private static BitSet[] liveOut(CSharpControlFlow flow, Instruction[] instructions, Map<DotNetVariable, Integer> indexes) {
        int length = instructions.length;
        BitSet[] liveIn = new BitSet[length];
        BitSet[] liveOut = new BitSet[length];
        for (int i = 0; i < length; i++) {
            liveIn[i] = new BitSet();
            liveOut[i] = new BitSet();
        }

        Deque<Instruction> queue = new ArrayDeque<>();
        boolean[] queued = new boolean[length];
        for (int i = length - 1; i >= 0; i--) {
            if (flow.isReachable(instructions[i])) {
                queue.add(instructions[i]);
                queued[i] = true;
            }
        }

        int limit = 20 * (length + 1) * Math.max(1, indexes.size());
        int count = 0;
        while (!queue.isEmpty()) {
            if (++count > limit) {
                return null;
            }
            if ((count & 0xFF) == 0) {
                ProgressManager.checkCanceled();
            }
            Instruction instruction = queue.poll();
            int num = instruction.num();
            queued[num] = false;

            BitSet out = new BitSet();
            for (Instruction successor : instruction.allSucc()) {
                out.or(liveIn[successor.num()]);
            }
            liveOut[num] = out;

            BitSet in = (BitSet) out.clone();
            if (instruction instanceof CSharpReadWriteInstruction readWrite) {
                Integer index = indexes.get(readWrite.getVariable());
                if (index != null) {
                    in.set(index, readWrite.getAccess() == CSharpReadWriteInstruction.Access.READ);
                }
            }
            if (!in.equals(liveIn[num])) {
                liveIn[num] = in;
                for (Instruction predecessor : instruction.allPred()) {
                    if (!queued[predecessor.num()] && flow.isReachable(predecessor)) {
                        queued[predecessor.num()] = true;
                        queue.add(predecessor);
                    }
                }
            }
        }
        return liveOut;
    }

    /**
     * The variables of the owner the lambdas, anonymous methods and local functions in it read
     */
    @RequiredReadAction
    private static Set<DotNetVariable> captured(PsiElement owner) {
        Set<DotNetVariable> captured = new HashSet<>();
        for (CSharpSimpleLikeMethodAsElement nested : PsiTreeUtil.findChildrenOfType(owner, CSharpSimpleLikeMethodAsElement.class)) {
            CSharpControlFlow flow = CSharpControlFlow.of(nested);
            if (flow == null) {
                // not built - nothing is known of what it reads
                return allOf(owner);
            }
            for (Instruction instruction : flow.getInstructions()) {
                if (instruction instanceof CSharpReadWriteInstruction readWrite && readWrite.getAccess() == CSharpReadWriteInstruction.Access.READ
                    && CSharpFlowVariables.ownerOf(readWrite.getVariable()) == owner) {
                    captured.add(readWrite.getVariable());
                }
            }
        }
        return captured;
    }

    @RequiredReadAction
    private static Set<DotNetVariable> allOf(PsiElement owner) {
        Set<DotNetVariable> variables = new HashSet<>();
        for (DotNetVariable variable : PsiTreeUtil.findChildrenOfType(owner, DotNetVariable.class)) {
            variables.add(variable);
        }
        return variables;
    }

    @RequiredReadAction
    private static boolean isChecked(DotNetVariable variable) {
        if ("_".equals(variable.getName())) {
            return false;
        }
        if (variable instanceof DotNetParameter parameter) {
            return !parameter.hasModifier(CSharpModifier.OUT) && !parameter.hasModifier(CSharpModifier.REF);
        }
        if (variable instanceof CSharpLocalVariable local) {
            PsiElement parent = local.getParent();
            if (parent instanceof CSharpUsingStatementImpl || parent instanceof CSharpFixedStatementImpl) {
                return false;
            }
            // using var d = ...
            return PsiUtilCore.getElementType(PsiTreeUtil.getDeepestFirst(parent)) != CSharpTokens.USING_KEYWORD;
        }
        return true;
    }
}
