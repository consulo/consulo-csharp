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
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.ast.IElementType;
import consulo.language.controlFlow.ControlFlow;
import consulo.language.controlFlow.ControlFlowBuilder;
import consulo.language.controlFlow.Instruction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Builds the {@link CSharpControlFlow} of one flow owner on the platform {@link ControlFlowBuilder}: a node per statement
 * (not for blocks and empty statements), conditional nodes for the branches of {@code if} and loops, constant conditions
 * folded (C# spec 13.2: the then branch of {@code if (false)} is unreachable, {@code while (true)} has no exit), jumps as
 * pending edges, {@code finally} built once per way out of the {@code try} (normal completion, each jump target, an
 * exception). Expressions do not branch - as in Roslyn's reachability, only lambdas and anonymous methods get a node,
 * their bodies are other owners; a local function declaration has none.
 *
 * @author VISTALL
 */
public class CSharpControlFlowBuilder extends CSharpElementVisitor {
    /**
     * The labels a goto can reach - of the owner or of one copy of a finally block
     */
    private static class GotoScope {
        private final @Nullable GotoScope myParent;
        private final Map<PsiElement, Instruction> myLabels = new HashMap<>();
        private final List<Map.Entry<Instruction, PsiElement>> myJumps = new ArrayList<>();

        private GotoScope(@Nullable GotoScope parent) {
            myParent = parent;
        }

        private void resolve(ControlFlowBuilder builder) {
            for (Map.Entry<Instruction, PsiElement> jump : myJumps) {
                Instruction target = myLabels.get(jump.getValue());
                if (target != null) {
                    builder.addEdge(jump.getKey(), target);
                }
                else if (myParent != null) {
                    myParent.myJumps.add(jump);
                }
            }
        }
    }

    private record SwitchInfo(CSharpSwitchStatementImpl statement, List<CSharpSwitchSection> sections) {
    }

    /**
     * A jump out of a try with a finally - to the scope of a pending edge, or to a goto target
     */
    private record JumpKey(@Nullable PsiElement scope, @Nullable PsiElement gotoTarget) {
    }

    private final ControlFlowBuilder myBuilder = new ControlFlowBuilder();

    private final Deque<PsiElement> myBreakTargets = new ArrayDeque<>();
    private final Deque<PsiElement> myContinueTargets = new ArrayDeque<>();
    private final Deque<SwitchInfo> mySwitches = new ArrayDeque<>();
    private final Deque<CSharpTryStatementImpl> myFinallyTries = new ArrayDeque<>();
    private final Map<Instruction, PsiElement> myGotoJumps = new HashMap<>();
    private GotoScope myGotoScope = new GotoScope(null);

    private boolean myIterator;
    private final List<CSharpControlFlow.SectionEnd> mySectionEnds = new ArrayList<>();

    @RequiredReadAction
    public static CSharpControlFlow build(PsiElement owner, PsiElement body) {
        return new CSharpControlFlowBuilder().buildImpl(owner, body);
    }

    @RequiredReadAction
    private CSharpControlFlow buildImpl(PsiElement owner, PsiElement body) {
        node(null, CSharpInstruction.ENTRY);

        if (body instanceof DotNetExpression expression) {
            // => expression - returns it
            expression.accept(this);
            exitTo(null);
        }
        else {
            visitChildren(body);
        }

        CSharpInstruction end = node(owner, CSharpInstruction.END);

        myGotoScope.resolve(myBuilder);

        node(null, CSharpInstruction.EXIT);

        ControlFlow flow = myBuilder.completeControlFlow();
        return new CSharpControlFlow(owner, flow.getInstructions(), end, myIterator, mySectionEnds);
    }

    // region nodes

    private CSharpInstruction node(@Nullable PsiElement element) {
        return node(element, null);
    }

    private CSharpInstruction node(@Nullable PsiElement element, @Nullable String marker) {
        CSharpInstruction instruction = new CSharpInstruction(myBuilder, element, marker);
        myBuilder.addNodeAndCheckPending(instruction);
        return instruction;
    }

    private void conditional(PsiElement element, PsiElement condition, boolean result) {
        myBuilder.addNodeAndCheckPending(new CSharpConditionalInstruction(myBuilder, element, condition, result));
    }

    /**
     * The flow continues after the first node out of the scope (null - the exit), and stops here
     */
    private void exitTo(@Nullable PsiElement scope) {
        myBuilder.addPendingEdge(scope, myBuilder.prevInstruction);
        myBuilder.flowAbrupted();
    }

    /**
     * The ends of a construct in the scope: the current node and the pending edges out of a statement in the scope -
     * taken from the pending list, the flow stopped
     */
    private List<Instruction> collectEnds(PsiElement scope) {
        List<Instruction> ends = new ArrayList<>();
        if (myBuilder.prevInstruction != null) {
            ends.add(myBuilder.prevInstruction);
        }
        myBuilder.processPending((pendingScope, instruction) -> {
            if (pendingScope != null && PsiTreeUtil.isAncestor(scope, pendingScope, false)) {
                ends.add(instruction);
            }
            else {
                myBuilder.addPendingEdge(pendingScope, instruction);
            }
        });
        myBuilder.flowAbrupted();
        return ends;
    }

    private void edges(List<Instruction> from, @Nullable Instruction to) {
        for (Instruction instruction : from) {
            myBuilder.addEdge(instruction, to);
        }
    }

    private void accept(@Nullable PsiElement element) {
        if (element != null) {
            element.accept(this);
        }
    }

    private void visitChildren(PsiElement element) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            // tokens, whitespaces, comments
            if (child.getFirstChild() != null) {
                child.accept(this);
            }
        }
    }

    // endregion

    @Override
    public void visitElement(PsiElement element) {
        ProgressManager.checkCanceled();
        if (element instanceof DotNetStatement) {
            node(element);
        }
        visitChildren(element);
    }

    // region nested owners

    @Override
    public void visitLambdaExpression(CSharpLambdaExpressionImpl expression) {
        node(expression);
    }

    @Override
    public void visitAnonymousMethodExpression(CSharpDelegateExpressionImpl method) {
        node(method);
    }

    @Override
    public void visitLocalMethodDeclarationStatement(CSharpLocalMethodDeclarationStatementImpl statement) {
        // hoisted - no node
    }

    // endregion

    @Override
    public void visitBlockStatement(CSharpBlockStatementImpl statement) {
        visitChildren(statement);
    }

    @Override
    public void visitEmptyStatement(CSharpEmptyStatementImpl statement) {
    }

    @Override
    public void visitLabeledStatement(CSharpLabeledStatementImpl statement) {
        label(statement);
        visitChildren(statement);
    }

    private void label(PsiElement label) {
        myGotoScope.myLabels.put(label, node(label));
    }

    // region branches and loops

    @Override
    @RequiredReadAction
    public void visitIfStatement(CSharpIfStatementImpl statement) {
        node(statement);
        DotNetExpression condition = statement.getConditionExpression();
        accept(condition);
        Instruction head = myBuilder.prevInstruction;
        Boolean value = ConstantExpressionEvaluator.evaluateBoolean(condition);

        DotNetStatement trueStatement = statement.getTrueStatement();
        DotNetStatement falseStatement = statement.getFalseStatement();

        myBuilder.prevInstruction = value == Boolean.FALSE ? null : head;
        if (condition != null && trueStatement != null && value != Boolean.FALSE) {
            conditional(trueStatement, condition, true);
        }
        accept(trueStatement);
        myBuilder.addPendingEdge(statement, myBuilder.prevInstruction);

        myBuilder.prevInstruction = value == Boolean.TRUE ? null : head;
        if (condition != null && value != Boolean.TRUE) {
            conditional(falseStatement != null ? falseStatement : statement, condition, false);
        }
        accept(falseStatement);
    }

    @Override
    @RequiredReadAction
    public void visitWhileStatement(CSharpWhileStatementImpl statement) {
        Instruction head = node(statement);
        DotNetExpression condition = statement.getConditionExpression();
        accept(condition);
        loop(statement, head, condition, myBuilder.prevInstruction, statement.getChildStatement(), List.of());
    }

    @Override
    @RequiredReadAction
    public void visitForStatement(CSharpForStatementImpl statement) {
        node(statement);

        // for (initializers; condition; iterators) body
        int part = 0;
        DotNetExpression condition = null;
        List<DotNetExpression> iterators = new ArrayList<>();
        DotNetStatement body = null;
        for (PsiElement child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == CSharpTokens.SEMICOLON) {
                part++;
            }
            else if (type == CSharpTokens.RPAR) {
                part = 3;
            }
            else if (part == 0 && (child instanceof DotNetExpression || child instanceof DotNetVariable)) {
                child.accept(this);
            }
            else if (part == 1 && child instanceof DotNetExpression expression) {
                condition = expression;
            }
            else if (part == 2 && child instanceof DotNetExpression expression) {
                iterators.add(expression);
            }
            else if (part == 3 && child instanceof DotNetStatement childStatement) {
                body = childStatement;
            }
        }

        Instruction head = condition != null ? node(condition) : node(statement, "loop");
        accept(condition);
        loop(statement, head, condition, myBuilder.prevInstruction, body, iterators);
    }

    /**
     * The condition is evaluated, then the body, the iterators of a for, and again the condition at the head
     */
    @RequiredReadAction
    private void loop(DotNetStatement loop,
                      Instruction head,
                      @Nullable DotNetExpression condition,
                      @Nullable Instruction conditionEnd,
                      @Nullable DotNetStatement body,
                      List<DotNetExpression> iterators) {
        // for (;;) - true
        Boolean value = condition == null ? Boolean.valueOf(loop instanceof CSharpForStatementImpl) : ConstantExpressionEvaluator.evaluateBoolean(condition);

        myBuilder.prevInstruction = value == Boolean.FALSE ? null : conditionEnd;
        if (condition != null && body != null && value != Boolean.FALSE) {
            conditional(body, condition, true);
        }
        List<Instruction> ends = loopBody(loop, body);
        if (!iterators.isEmpty()) {
            Instruction iterator = node(iterators.get(0));
            edges(ends, iterator);
            for (DotNetExpression expression : iterators) {
                expression.accept(this);
            }
            ends = myBuilder.prevInstruction == null ? List.of() : List.of(myBuilder.prevInstruction);
        }
        edges(ends, head);

        myBuilder.prevInstruction = value == Boolean.TRUE ? null : conditionEnd;
        if (condition != null && value != Boolean.TRUE) {
            conditional(loop, condition, false);
        }
    }

    /**
     * The body with break and continue targets - its ends, continues included
     */
    private List<Instruction> loopBody(DotNetStatement loop, @Nullable DotNetStatement body) {
        PsiElement scope = body != null ? body : loop;
        myBreakTargets.push(loop);
        myContinueTargets.push(scope);
        try {
            accept(body);
        }
        finally {
            myBreakTargets.pop();
            myContinueTargets.pop();
        }
        return collectEnds(scope);
    }

    @Override
    @RequiredReadAction
    public void visitDoWhileStatement(CSharpDoWhileStatementImpl statement) {
        Instruction head = node(statement);
        DotNetExpression condition = statement.getConditionExpression();
        List<Instruction> ends = loopBody(statement, statement.getChildStatement());
        if (condition == null) {
            return;
        }

        edges(ends, node(condition));
        condition.accept(this);
        Instruction conditionEnd = myBuilder.prevInstruction;
        Boolean value = ConstantExpressionEvaluator.evaluateBoolean(condition);

        if (value != Boolean.FALSE) {
            DotNetStatement body = statement.getChildStatement();
            conditional(body != null ? body : statement, condition, true);
            myBuilder.addEdge(myBuilder.prevInstruction, head);
        }

        myBuilder.prevInstruction = value == Boolean.TRUE ? null : conditionEnd;
        if (value != Boolean.TRUE) {
            conditional(statement, condition, false);
        }
    }

    @Override
    @RequiredReadAction
    public void visitForeachStatement(CSharpForeachStatementImpl statement) {
        node(statement);
        accept(statement.getIterableExpression());

        // MoveNext - the next element to the variable, or the exit
        CSharpLocalVariable variable = statement.getVariable();
        Instruction head = node(variable != null ? variable : statement, "next");
        myBuilder.addPendingEdge(statement, head);

        edges(loopBody(statement, statement.getChildStatement()), head);
    }

    // endregion

    // region switch

    @Override
    @RequiredReadAction
    public void visitSwitchStatement(CSharpSwitchStatementImpl statement) {
        node(statement);
        DotNetExpression expression = statement.getExpression();
        accept(expression);
        Instruction head = myBuilder.prevInstruction;

        List<CSharpSwitchSection> sections = CSharpSwitchSection.of(statement);
        Object value = expression == null ? null : new ConstantExpressionEvaluator(expression, true).getValue();
        boolean matched = value != null && sections.stream().flatMap(it -> it.labels().stream()).anyMatch(it -> sameConstant(value, it));

        mySwitches.push(new SwitchInfo(statement, sections));
        myBreakTargets.push(statement);
        try {
            for (int i = 0; i < sections.size(); i++) {
                CSharpSwitchSection section = sections.get(i);

                myBuilder.flowAbrupted();
                for (CSharpSwitchLabelStatement label : section.labels()) {
                    // labels of a section follow each other
                    Instruction labelNode = node(label);
                    myGotoScope.myLabels.put(label, labelNode);
                    if (canMatch(label, value, matched)) {
                        myBuilder.addEdge(head, labelNode);
                    }
                    if (label instanceof CSharpCasePatternStatementImpl pattern) {
                        accept(pattern.getWhenExpression());
                    }
                }

                for (DotNetStatement child : section.statements()) {
                    if (child instanceof CSharpLabeledStatementImpl labeled) {
                        // its children are the next statements
                        label(labeled);
                    }
                    else {
                        child.accept(this);
                    }
                }

                CSharpInstruction end = node(section.lastLabel(), CSharpInstruction.SECTION_END);
                mySectionEnds.add(new CSharpControlFlow.SectionEnd(statement, section.lastLabel(), end, i == sections.size() - 1));
                myBuilder.flowAbrupted();
            }
        }
        finally {
            mySwitches.pop();
            myBreakTargets.pop();
        }

        if (!isExhaustive(expression, sections, value, matched)) {
            myBuilder.addPendingEdge(statement, head);
        }
        myBuilder.flowAbrupted();
    }

    @RequiredReadAction
    private static boolean canMatch(CSharpSwitchLabelStatement label, @Nullable Object value, boolean matched) {
        if (value == null) {
            return true;
        }
        if (isDefault(label)) {
            return !matched;
        }
        if (label instanceof CSharpCaseOrDefaultStatementImpl caseLabel) {
            Object labelValue = new ConstantExpressionEvaluator(caseLabel.getExpression(), true).getValue();
            return labelValue == null || sameConstant(value, labelValue);
        }
        // a pattern
        return true;
    }

    /**
     * No way past all the labels: a default, a constant switch matching a label, the labels true and false of a bool,
     * a var pattern, a type pattern of the non-nullable value type switched on
     */
    @RequiredReadAction
    private static boolean isExhaustive(@Nullable DotNetExpression expression, List<CSharpSwitchSection> sections, @Nullable Object value, boolean matched) {
        if (matched) {
            return true;
        }

        boolean trueLabel = false, falseLabel = false;
        List<CSharpCasePatternStatementImpl> patterns = new ArrayList<>();
        for (CSharpSwitchSection section : sections) {
            for (CSharpSwitchLabelStatement label : section.labels()) {
                if (isDefault(label)) {
                    return true;
                }
                if (label instanceof CSharpCaseOrDefaultStatementImpl caseLabel) {
                    Object labelValue = new ConstantExpressionEvaluator(caseLabel.getExpression(), true).getValue();
                    trueLabel |= Boolean.TRUE.equals(labelValue);
                    falseLabel |= Boolean.FALSE.equals(labelValue);
                }
                else if (label instanceof CSharpCasePatternStatementImpl pattern && pattern.getWhenExpression() == null) {
                    patterns.add(pattern);
                }
            }
        }

        if (value != null || expression == null) {
            return false;
        }

        DotNetTypeRef typeRef = null;
        if (trueLabel && falseLabel) {
            typeRef = expression.toTypeRef(true);
            if (DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.Boolean)) {
                return true;
            }
        }

        for (CSharpCasePatternStatementImpl pattern : patterns) {
            DotNetType type = pattern.getVariable().getType();
            if (type instanceof CSharpNativeType nativeType && nativeType.getTypeElementType() == CSharpSoftTokens.VAR_KEYWORD) {
                return true;
            }
            if (type == null) {
                continue;
            }
            if (typeRef == null) {
                typeRef = expression.toTypeRef(true);
            }
            if (CSharpTypeUtil.isTypeEqual(type.toTypeRef(), typeRef) && isNonNullableValueType(typeRef)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isNonNullableValueType(DotNetTypeRef typeRef) {
        if (!(typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type)) {
            return false;
        }
        return (type.isStruct() || type.isEnum()) && !DotNetTypes.System.Nullable$1.equals(type.getVmQName());
    }

    @RequiredReadAction
    private static boolean isDefault(CSharpSwitchLabelStatement label) {
        PsiElement first = label.getFirstChild();
        return label instanceof CSharpCaseOrDefaultStatementImpl && first != null && first.getNode().getElementType() == CSharpTokens.DEFAULT_KEYWORD;
    }

    @RequiredReadAction
    private static boolean sameConstant(Object value, CSharpSwitchLabelStatement label) {
        if (!(label instanceof CSharpCaseOrDefaultStatementImpl caseLabel) || isDefault(label)) {
            return false;
        }
        Object labelValue = new ConstantExpressionEvaluator(caseLabel.getExpression(), true).getValue();
        return labelValue != null && sameConstant(value, labelValue);
    }

    private static boolean sameConstant(Object first, Object second) {
        if (first instanceof Number firstNumber && second instanceof Number secondNumber
            && !(first instanceof Float || first instanceof Double || second instanceof Float || second instanceof Double)) {
            return firstNumber.longValue() == secondNumber.longValue();
        }
        return first.equals(second);
    }

    // endregion

    // region jumps

    @Override
    public void visitBreakStatement(CSharpBreakStatementImpl statement) {
        node(statement);
        PsiElement target = myBreakTargets.peek();
        if (target != null) {
            exitTo(target);
        }
        myBuilder.flowAbrupted();
    }

    @Override
    public void visitContinueStatement(CSharpContinueStatementImpl statement) {
        node(statement);
        PsiElement target = myContinueTargets.peek();
        if (target != null) {
            exitTo(target);
        }
        myBuilder.flowAbrupted();
    }

    @Override
    public void visitReturnStatement(CSharpReturnStatementImpl statement) {
        node(statement);
        visitChildren(statement);
        exitTo(null);
    }

    @Override
    public void visitThrowStatement(CSharpThrowStatementImpl statement) {
        node(statement);
        visitChildren(statement);
        exitTo(null);
    }

    @Override
    public void visitYieldStatement(CSharpYieldStatementImpl statement) {
        myIterator = true;
        node(statement);
        for (PsiElement child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpBreakStatementImpl) {
                // yield break
                exitTo(null);
                return;
            }
            if (child instanceof CSharpReturnStatementImpl yieldReturn) {
                visitChildren(yieldReturn);
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitGotoStatement(CSharpGotoStatementImpl statement) {
        Instruction instruction = node(statement);
        DotNetExpression expression = statement.getExpression();

        PsiElement target = null;
        if (statement.isCaseOrDefault()) {
            SwitchInfo info = mySwitches.peek();
            if (info != null) {
                Object value = expression == null ? null : new ConstantExpressionEvaluator(expression, true).getValue();
                target = findSwitchLabel(info, value);
            }
        }
        else if (expression instanceof CSharpReferenceExpression reference && reference.resolve() instanceof CSharpLabeledStatementImpl label) {
            target = label;
        }

        if (target != null) {
            jumpTo(instruction, target);
        }
        myBuilder.flowAbrupted();
    }

    /**
     * goto default - value null, goto case value
     */
    @RequiredReadAction
    private static @Nullable PsiElement findSwitchLabel(SwitchInfo info, @Nullable Object value) {
        for (CSharpSwitchSection section : info.sections()) {
            for (CSharpSwitchLabelStatement label : section.labels()) {
                if (value == null ? isDefault(label) : sameConstant(value, label)) {
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * A goto to a label - through the finally blocks it leaves
     */
    private void jumpTo(Instruction from, PsiElement target) {
        CSharpTryStatementImpl tryStatement = myFinallyTries.peek();
        if (tryStatement != null && !PsiTreeUtil.isAncestor(tryStatement, target, false)) {
            myGotoJumps.put(from, target);
            myBuilder.addPendingEdge(tryStatement, from);
            return;
        }
        myGotoScope.myJumps.add(Map.entry(from, target));
    }

    // endregion

    // region try

    @Override
    @RequiredReadAction
    public void visitTryStatement(CSharpTryStatementImpl statement) {
        Instruction tryNode = node(statement);

        CSharpBlockStatementImpl tryBlock = PsiTreeUtil.getChildOfType(statement, CSharpBlockStatementImpl.class);
        CSharpFinallyStatementImpl finallyStatement = statement.getFinallyStatement();

        if (finallyStatement != null) {
            myFinallyTries.push(statement);
        }
        List<Instruction> normalEnds = new ArrayList<>();
        try {
            if (tryBlock != null) {
                tryBlock.accept(this);
                normalEnds.addAll(collectEnds(tryBlock));
            }

            // an exception from anywhere in the try block
            for (CSharpCatchStatementImpl catchStatement : statement.getCatchStatements()) {
                myBuilder.prevInstruction = tryNode;
                node(catchStatement);
                visitChildren(catchStatement);
                normalEnds.addAll(collectEnds(catchStatement));
            }
        }
        finally {
            if (finallyStatement != null) {
                myFinallyTries.pop();
            }
        }

        if (finallyStatement == null) {
            for (Instruction end : normalEnds) {
                myBuilder.addPendingEdge(statement, end);
            }
            return;
        }

        // the jumps out of the try and the catches - each through its own copy of the finally block
        Map<JumpKey, List<Instruction>> jumps = new LinkedHashMap<>();
        int tryNumber = tryNode.num();
        myBuilder.processPending((pendingScope, instruction) -> {
            if (instruction.num() > tryNumber) {
                jumps.computeIfAbsent(new JumpKey(pendingScope, myGotoJumps.remove(instruction)), it -> new ArrayList<>()).add(instruction);
            }
            else {
                myBuilder.addPendingEdge(pendingScope, instruction);
            }
        });

        // an exception - the finally block, then up to the exit
        for (Instruction end : finallyCopy(finallyStatement, List.of(tryNode))) {
            myBuilder.addPendingEdge(null, end);
        }

        if (!normalEnds.isEmpty()) {
            for (Instruction end : finallyCopy(finallyStatement, normalEnds)) {
                myBuilder.addPendingEdge(statement, end);
            }
        }

        for (Map.Entry<JumpKey, List<Instruction>> jump : jumps.entrySet()) {
            JumpKey key = jump.getKey();
            for (Instruction end : finallyCopy(finallyStatement, jump.getValue())) {
                if (key.gotoTarget() != null) {
                    jumpTo(end, key.gotoTarget());
                }
                else {
                    myBuilder.addPendingEdge(key.scope(), end);
                }
            }
        }
        myBuilder.flowAbrupted();
    }

    private List<Instruction> finallyCopy(CSharpFinallyStatementImpl finallyStatement, List<Instruction> from) {
        myBuilder.flowAbrupted();
        edges(from, node(finallyStatement));

        GotoScope parent = myGotoScope;
        myGotoScope = new GotoScope(parent);
        try {
            visitChildren(finallyStatement);
            return collectEnds(finallyStatement);
        }
        finally {
            myGotoScope.resolve(myBuilder);
            myGotoScope = parent;
        }
    }

    // endregion
}
