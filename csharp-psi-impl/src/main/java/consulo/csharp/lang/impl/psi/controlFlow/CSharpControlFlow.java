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
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.controlFlow.ControlFlow;
import consulo.language.controlFlow.Instruction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * The control flow graph of a flow owner - a method, constructor, operator or accessor body, a lambda, an anonymous
 * method, a local function, the top-level statements - with the reachability of its nodes (C# spec 13.2).
 * Nested owners are opaque: a lambda is one node, a local function none.
 *
 * @author VISTALL
 */
public final class CSharpControlFlow implements ControlFlow {
    /**
     * The end of the statements of a switch section - reachable means falling through to the next section (CS0163) or out
     * of the switch (CS8070)
     */
    public record SectionEnd(CSharpSwitchStatementImpl statement, CSharpSwitchLabelStatement label, Instruction instruction, boolean last) {
    }

    private final PsiElement myOwner;
    private final Instruction[] myInstructions;
    private final Instruction myEnd;
    private final boolean myIterator;
    private final List<SectionEnd> mySectionEnds;
    private final Map<Instruction, List<PsiElement>> myReturns;
    private final Map<PsiElement, List<Instruction>> myIndex = new HashMap<>();
    private final BitSet myReachable = new BitSet();

    private volatile @Nullable Set<PsiElement> myUnreachableStatements;
    private volatile @Nullable CSharpDefiniteAssignment myDefiniteAssignment;
    private volatile @Nullable List<CSharpUnusedValues.Write> myUnusedValues;

    CSharpControlFlow(PsiElement owner,
                      Instruction[] instructions,
                      Instruction end,
                      boolean iterator,
                      List<SectionEnd> sectionEnds,
                      Map<Instruction, List<PsiElement>> returns) {
        myOwner = owner;
        myInstructions = instructions;
        myEnd = end;
        myIterator = iterator;
        mySectionEnds = sectionEnds;
        myReturns = returns;

        for (Instruction instruction : instructions) {
            PsiElement element = instruction.getElement();
            if (element != null) {
                myIndex.computeIfAbsent(element, it -> new ArrayList<>(1)).add(instruction);
            }
        }

        Deque<Instruction> queue = new ArrayDeque<>();
        if (instructions.length > 0) {
            myReachable.set(0);
            queue.add(instructions[0]);
        }
        while (!queue.isEmpty()) {
            for (Instruction next : queue.poll().allSucc()) {
                if (!myReachable.get(next.num())) {
                    myReachable.set(next.num());
                    queue.add(next);
                }
            }
        }
    }

    /**
     * The graph of the owner - null if it has no body or does not parse
     */
    @RequiredReadAction
    public static @Nullable CSharpControlFlow of(PsiElement owner) {
        if (!(owner instanceof CSharpSimpleLikeMethodAsElement method)) {
            return null;
        }
        // a completion copy, a code fragment - not cached
        if (!owner.isPhysical()) {
            return compute(method);
        }
        return LanguageCachedValueUtil.getCachedValue(owner, () -> CachedValueProvider.Result.create(compute(method), PsiModificationTracker.MODIFICATION_COUNT));
    }

    /**
     * The graph of the innermost owner of the element - null if there is none
     */
    @RequiredReadAction
    public static @Nullable CSharpControlFlow ofElement(PsiElement element) {
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
        return owner == null ? null : of(owner);
    }

    @RequiredReadAction
    private static @Nullable CSharpControlFlow compute(CSharpSimpleLikeMethodAsElement owner) {
        PsiElement body = owner instanceof CSharpTopLevelStatementsImpl ? owner : owner.getCodeBlock().getElement();
        if (body == null || PsiTreeUtil.hasErrorElements(owner)) {
            return null;
        }
        return CSharpControlFlowBuilder.build(owner, body);
    }

    public PsiElement getOwner() {
        return myOwner;
    }

    @Override
    public Instruction[] getInstructions() {
        return myInstructions;
    }

    public List<Instruction> getInstructions(PsiElement element) {
        return myIndex.getOrDefault(element, List.of());
    }

    public boolean isReachable(Instruction instruction) {
        return myReachable.get(instruction.num());
    }

    /**
     * Any node of the element is reachable - true for an element without one (a block, an expression)
     */
    public boolean isReachable(PsiElement element) {
        List<Instruction> instructions = myIndex.get(element);
        if (instructions == null) {
            return true;
        }
        for (Instruction instruction : instructions) {
            if (isReachable(instruction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The end point of the body is reachable - the control falls off it without a return (C# spec 13.2)
     */
    public boolean isEndReachable() {
        return isReachable(myEnd);
    }

    /**
     * Has a yield return or a yield break - its end point is a yield break
     */
    public boolean isIterator() {
        return myIterator;
    }

    public List<SectionEnd> getSectionEnds() {
        return mySectionEnds;
    }

    /**
     * The end of the body
     */
    public Instruction getEnd() {
        return myEnd;
    }

    /**
     * The nodes a return leaves from to the exit, with its statements - several through one copy of a finally block;
     * the owner for an expression body
     */
    public Map<Instruction, List<PsiElement>> getReturns() {
        return myReturns;
    }

    /**
     * The assignments of variables of the owner whose value is never read
     */
    @RequiredReadAction
    public List<CSharpUnusedValues.Write> getUnusedValues() {
        List<CSharpUnusedValues.Write> result = myUnusedValues;
        if (result == null) {
            myUnusedValues = result = CSharpUnusedValues.compute(this);
        }
        return result;
    }

    @RequiredReadAction
    public CSharpDefiniteAssignment getDefiniteAssignment() {
        CSharpDefiniteAssignment result = myDefiniteAssignment;
        if (result == null) {
            myDefiniteAssignment = result = CSharpDefiniteAssignment.compute(this);
        }
        return result;
    }

    /**
     * The statements to report as unreachable (CS0162): the first one of each unreachable run, as Roslyn's
     * {@code ControlFlowPass} - not blocks, empty statements, throws, local functions; once reported, the next
     * unreachable ones are not until the flow becomes reachable again or joins one of an unreported branch
     */
    @RequiredReadAction
    public Set<PsiElement> getUnreachableStatements() {
        Set<PsiElement> result = myUnreachableStatements;
        if (result == null) {
            Set<PsiElement> statements = new HashSet<>();
            PsiElement body = myOwner instanceof CSharpTopLevelStatementsImpl ? myOwner : ((CSharpSimpleLikeMethodAsElement) myOwner).getCodeBlock().getElement();
            if (body != null) {
                children(body, false, statements);
            }
            myUnreachableStatements = result = statements;
        }
        return result;
    }

    /**
     * @return reported - the last unreachable statement is reported
     */
    @RequiredReadAction
    private boolean statement(@Nullable PsiElement statement, boolean reported, Set<PsiElement> result) {
        if (statement == null || statement instanceof CSharpEmptyStatementImpl || statement instanceof CSharpLocalMethodDeclarationStatementImpl) {
            return reported;
        }
        if (statement instanceof CSharpBlockStatementImpl) {
            return children(statement, reported, result);
        }
        if (statement instanceof CSharpThrowStatementImpl) {
            return !isReachable(statement) && reported;
        }

        boolean entry = check(statement, reported, result);
        if (statement instanceof CSharpIfStatementImpl ifStatement) {
            boolean trueEnd = statement(ifStatement.getTrueStatement(), entry, result);
            DotNetStatement falseStatement = ifStatement.getFalseStatement();
            boolean falseEnd = falseStatement == null ? entry : statement(falseStatement, entry, result);
            return trueEnd && falseEnd;
        }
        if (statement instanceof CSharpWhileStatementImpl || statement instanceof CSharpForStatementImpl || statement instanceof CSharpForeachStatementImpl) {
            children(statement, entry, result);
            return entry;
        }
        if (statement instanceof CSharpDoWhileStatementImpl) {
            return children(statement, entry, result);
        }
        if (statement instanceof CSharpSwitchStatementImpl switchStatement) {
            for (CSharpSwitchSection section : CSharpSwitchSection.of(switchStatement)) {
                boolean sectionReported = entry;
                for (DotNetStatement child : section.statements()) {
                    // its children are the next statements of the section
                    sectionReported = child instanceof CSharpLabeledStatementImpl ? check(child, sectionReported, result) : statement(child, sectionReported, result);
                }
            }
            return entry;
        }
        if (statement instanceof CSharpTryStatementImpl tryStatement) {
            boolean end = children(PsiTreeUtil.getChildOfType(tryStatement, CSharpBlockStatementImpl.class), entry, result);
            for (CSharpCatchStatementImpl catchStatement : tryStatement.getCatchStatements()) {
                end &= children(catchStatement, entry, result);
            }
            children(tryStatement.getFinallyStatement(), entry, result);
            return end;
        }
        // labeled, using, lock, fixed, checked, unsafe - their statements one after another
        return children(statement, entry, result);
    }

    @RequiredReadAction
    private boolean children(@Nullable PsiElement parent, boolean reported, Set<PsiElement> result) {
        if (parent == null) {
            return reported;
        }
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetStatement) {
                reported = statement(child, reported, result);
            }
        }
        return reported;
    }

    private boolean check(PsiElement statement, boolean reported, Set<PsiElement> result) {
        if (isReachable(statement)) {
            return false;
        }
        if (!reported) {
            result.add(statement);
        }
        return true;
    }
}
