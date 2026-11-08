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
import consulo.application.util.RecursionManager;
import consulo.csharp.lang.impl.psi.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.controlFlow.Instruction;
import consulo.language.dataFlow.DFAEngine;
import consulo.language.dataFlow.DFALimitExceededException;
import consulo.language.dataFlow.DfaInstance;
import consulo.language.dataFlow.Semilattice;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Definite assignment (C# spec 9.4) on a {@link CSharpControlFlow}: the set of the variables assigned on every path to
 * a node, joined by intersection; everything is assigned at an unreachable node or after a vacuous one.
 * <ul>
 * <li>the reads of a variable of the owner not assigned there (CS0165, CS0269 for an out parameter)</li>
 * <li>the reads of a variable of an enclosing owner not assigned in this one before - its inputs, checked where a
 * lambda is created or a local function is referenced</li>
 * <li>the returns, and the end of the body, with an out parameter not assigned (CS0177)</li>
 * </ul>
 *
 * @author VISTALL
 */
public final class CSharpDefiniteAssignment {
    /**
     * A read of a variable not assigned before it - at its reference, or at the reference to a local function reading it
     */
    public record Read(PsiElement element, DotNetVariable variable) {
    }

    /**
     * An out parameter not assigned when the control leaves - at a return, or at the owner for the end of its body
     */
    public record Leave(PsiElement element, DotNetVariable parameter) {
    }

    private static final CSharpDefiniteAssignment EMPTY = new CSharpDefiniteAssignment(List.of(), List.of(), List.of());

    private final Map<PsiElement, List<DotNetVariable>> myUnassignedReads = new HashMap<>();
    private final List<Read> myInputs;
    private final List<Leave> myUnassignedLeaves;

    private CSharpDefiniteAssignment(Collection<Read> unassignedReads, List<Read> inputs, List<Leave> unassignedLeaves) {
        for (Read read : unassignedReads) {
            myUnassignedReads.computeIfAbsent(read.element(), it -> new ArrayList<>(1)).add(read.variable());
        }
        myInputs = inputs;
        myUnassignedLeaves = unassignedLeaves;
    }

    /**
     * The variables of this owner read at the element - a reference - not assigned before
     */
    public List<DotNetVariable> getUnassignedReads(PsiElement element) {
        return myUnassignedReads.getOrDefault(element, List.of());
    }

    public List<Read> getInputs() {
        return myInputs;
    }

    public List<Leave> getUnassignedLeaves() {
        return myUnassignedLeaves;
    }

    /**
     * Assigned variables by their index - null is all of them (an unreachable node)
     */
    private record State(@Nullable BitSet assigned) {
        boolean has(int index) {
            return assigned == null || assigned.get(index);
        }
    }

    private static final State ALL = new State(null);

    @RequiredReadAction
    static CSharpDefiniteAssignment compute(CSharpControlFlow flow) {
        Instruction[] instructions = flow.getInstructions();
        Map<DotNetVariable, Integer> indexes = new HashMap<>();
        for (Instruction instruction : instructions) {
            if (instruction instanceof CSharpReadWriteInstruction readWrite) {
                indexes.putIfAbsent(readWrite.getVariable(), indexes.size());
            }
        }
        List<DotNetVariable> outParameters = new ArrayList<>();
        if (flow.getOwner() instanceof DotNetParameterListOwner owner && !(owner instanceof CSharpLambdaExpressionImpl)) {
            for (DotNetVariable parameter : owner.getParameters()) {
                if (CSharpFlowVariables.isOut(parameter) && CSharpFlowVariables.isTracked(parameter)) {
                    indexes.putIfAbsent(parameter, indexes.size());
                    outParameters.add(parameter);
                }
            }
        }
        if (indexes.isEmpty() && !hasNested(instructions)) {
            return EMPTY;
        }

        List<State> states;
        try {
            states = new DFAEngine<>(instructions, new Transfer(flow, indexes), new Intersection()).performDFA();
        }
        catch (DFALimitExceededException e) {
            // too complex - nothing reported
            return EMPTY;
        }

        PsiElement owner = flow.getOwner();
        Set<Read> unassignedReads = new LinkedHashSet<>();
        Set<Read> inputs = new LinkedHashSet<>();
        for (Instruction instruction : instructions) {
            if (!flow.isReachable(instruction)) {
                continue;
            }
            State state = states.get(instruction.num());
            if (instruction instanceof CSharpReadWriteInstruction readWrite && readWrite.getAccess() == CSharpReadWriteInstruction.Access.READ) {
                if (!state.has(indexes.get(readWrite.getVariable()))) {
                    Read read = new Read(readWrite.getElement(), readWrite.getVariable());
                    (CSharpFlowVariables.ownerOf(read.variable()) == owner ? unassignedReads : inputs).add(read);
                }
                continue;
            }

            // a lambda created, a local function referenced - the variables it reads must be assigned here
            List<Read> nestedInputs = nestedInputs(instruction);
            for (Read input : nestedInputs) {
                Integer index = indexes.get(input.variable());
                if (index != null && state.has(index)) {
                    continue;
                }
                // a local function - at its reference
                Read read = instruction.getElement() instanceof CSharpReferenceExpression ? new Read(instruction.getElement(), input.variable()) : input;
                (CSharpFlowVariables.ownerOf(input.variable()) == owner ? unassignedReads : inputs).add(read);
            }
        }

        List<Leave> leaves = new ArrayList<>();
        if (!outParameters.isEmpty()) {
            for (Map.Entry<Instruction, List<PsiElement>> exit : flow.getReturns().entrySet()) {
                if (flow.isReachable(exit.getKey())) {
                    leaves(states.get(exit.getKey().num()), exit.getValue(), outParameters, indexes, leaves);
                }
            }
            Instruction end = flow.getEnd();
            if (flow.isReachable(end)) {
                leaves(states.get(end.num()), List.of(owner), outParameters, indexes, leaves);
            }
        }
        return new CSharpDefiniteAssignment(unassignedReads, List.copyOf(inputs), leaves);
    }

    private static void leaves(State state, List<PsiElement> elements, List<DotNetVariable> outParameters, Map<DotNetVariable, Integer> indexes, List<Leave> leaves) {
        for (DotNetVariable parameter : outParameters) {
            if (!state.has(indexes.get(parameter))) {
                for (PsiElement element : elements) {
                    leaves.add(new Leave(element, parameter));
                }
            }
        }
    }

    private static boolean hasNested(Instruction[] instructions) {
        for (Instruction instruction : instructions) {
            PsiElement element = instruction.getElement();
            if (element instanceof CSharpLambdaExpressionImpl || element instanceof CSharpDelegateExpressionImpl
                || instruction instanceof CSharpInstruction marked && marked.is(CSharpInstruction.CALL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The inputs of the lambda or the anonymous method created, of the local function referenced, at the node
     */
    @RequiredReadAction
    private static List<Read> nestedInputs(Instruction instruction) {
        PsiElement element = instruction.getElement();
        PsiElement nested = null;
        if (element instanceof CSharpLambdaExpressionImpl || element instanceof CSharpDelegateExpressionImpl) {
            nested = element;
        }
        else if (instruction instanceof CSharpInstruction marked && marked.is(CSharpInstruction.CALL) && element instanceof CSharpReferenceExpression reference) {
            nested = reference.resolve();
        }
        if (!(nested instanceof CSharpSimpleLikeMethodAsElement target)) {
            return List.of();
        }
        // a local function calling itself, or another one calling it back
        List<Read> inputs = RecursionManager.doPreventingRecursion(target, false, () -> {
            CSharpControlFlow flow = CSharpControlFlow.of(target);
            return flow == null ? List.<Read>of() : flow.getDefiniteAssignment().getInputs();
        });
        return inputs == null ? List.of() : inputs;
    }

    private record Transfer(CSharpControlFlow flow, Map<DotNetVariable, Integer> indexes) implements DfaInstance<State> {
        @Override
        public State fun(State state, Instruction instruction) {
            if (!flow.isReachable(instruction) || instruction instanceof CSharpInstruction marked && marked.is(CSharpInstruction.VACUOUS)) {
                return ALL;
            }
            if (!(instruction instanceof CSharpReadWriteInstruction readWrite) || readWrite.getAccess() == CSharpReadWriteInstruction.Access.READ) {
                return state;
            }
            int index = indexes.get(readWrite.getVariable());
            boolean assign = readWrite.getAccess() == CSharpReadWriteInstruction.Access.WRITE;
            if (state.has(index) == assign) {
                return state;
            }
            BitSet assigned = state.assigned() == null ? all(indexes.size()) : (BitSet) state.assigned().clone();
            assigned.set(index, assign);
            return new State(assigned);
        }

        @Override
        public State initial() {
            return ALL;
        }

        @Override
        public boolean isForward() {
            return true;
        }

        private static BitSet all(int size) {
            BitSet bitSet = new BitSet(size);
            bitSet.set(0, size);
            return bitSet;
        }
    }

    /**
     * Assigned on every path - the entry, with no predecessor, has nothing assigned
     */
    private static final class Intersection implements Semilattice<State> {
        @Override
        public State join(ArrayList<State> ins) {
            if (ins.isEmpty()) {
                return new State(new BitSet());
            }
            BitSet result = null;
            for (State state : ins) {
                if (state.assigned() == null) {
                    continue;
                }
                if (result == null) {
                    result = (BitSet) state.assigned().clone();
                }
                else {
                    result.and(state.assigned());
                }
            }
            return result == null ? ALL : new State(result);
        }

        @Override
        public boolean eq(State first, State second) {
            return Objects.equals(first.assigned(), second.assigned());
        }
    }
}
