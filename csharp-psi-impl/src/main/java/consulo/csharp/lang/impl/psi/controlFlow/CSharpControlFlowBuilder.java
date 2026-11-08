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
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConstructorSuperCall;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.ast.IElementType;
import consulo.language.controlFlow.ControlFlow;
import consulo.language.controlFlow.ControlFlowBuilder;
import consulo.language.controlFlow.Instruction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Builds the {@link CSharpControlFlow} of one flow owner on the platform {@link ControlFlowBuilder}: a node per statement
 * (not for blocks and empty statements), conditional nodes for the branches of {@code if} and loops, constant conditions
 * folded (C# spec 13.2: the then branch of {@code if (false)} is unreachable, {@code while (true)} has no exit), jumps as
 * pending edges, {@code finally} built once per way out of the {@code try} (normal completion, each jump target, an
 * exception), an edge from every node of a try block to its catches. Reads and writes of the variables
 * ({@link CSharpFlowVariables}) are nodes; a condition has a when-true and a when-false exit ({@code &&}, {@code ||},
 * {@code !}, patterns assigning their variables when they match - C# spec 9.4.4), other expressions do not change the
 * reachability of statements, as in Roslyn. A lambda or an anonymous method is one node, its body another owner; a
 * local function declaration has none, a reference to it is a call node.
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
    private record JumpKey(@Nullable PsiElement scope, @Nullable PsiElement gotoTarget, boolean returns) {
    }

    /**
     * The nodes a condition ends in when it is true, and when it is false
     */
    private record Exits(List<Instruction> whenTrue, List<Instruction> whenFalse) {
        private Exits swap() {
            return new Exits(whenFalse, whenTrue);
        }
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
    private final Map<Instruction, List<PsiElement>> myReturns = new LinkedHashMap<>();

    private final PsiElement myOwner;
    private final @Nullable CSharpSimpleLikeMethodAsElement myTop;
    private final Set<String> myNames;
    // the out arguments of the call being visited - written after it
    private @Nullable List<Map.Entry<PsiElement, DotNetVariable>> myOutWrites;
    // the fields and auto-properties a constructor must assign - of a struct (C# 10 and lower, spec 9.4.1), the
    // non-nullable ones of a class (CS8618); static ones by a static constructor
    private final List<DotNetVariable> myStructMembers;
    private final @Nullable DotNetTypeDeclaration myStruct;
    // a struct - this may not be used before they all are assigned
    private final boolean myThisChecked;

    @RequiredReadAction
    private CSharpControlFlowBuilder(PsiElement owner) {
        myOwner = owner;
        myTop = CSharpFlowVariables.topOwner(owner);
        myNames = myTop == null ? Set.of() : CSharpFlowVariables.names(myTop);
        myStruct = owner instanceof CSharpConstructorDeclaration constructor ? constructedType(constructor) : null;
        boolean isStatic = myStruct != null && ((CSharpConstructorDeclaration) owner).hasModifier(CSharpModifier.STATIC);
        myStructMembers = myStruct == null ? List.of() : structMembers(myStruct, isStatic);
        myThisChecked = myStruct != null && myStruct.isStruct() && !isStatic;
    }

    /**
     * The type a constructor of it, not chained to another one, initializes
     */
    @RequiredReadAction
    private static @Nullable DotNetTypeDeclaration constructedType(CSharpConstructorDeclaration constructor) {
        if (constructor.isDeConstructor() || !(constructor.getParent() instanceof DotNetTypeDeclaration type) || type.hasModifier(CSharpModifier.PARTIAL)) {
            return null;
        }
        // : this(...) - assigns them all
        CSharpConstructorSuperCall superCall = constructor.getConstructorSuperCall();
        if (superCall != null && isThis(superCall.getExpression())) {
            return null;
        }
        return type;
    }

    /**
     * The fields, field-like events and auto-properties without an initializer - instance or static ones
     */
    @RequiredReadAction
    private static List<DotNetVariable> structMembers(DotNetTypeDeclaration type, boolean isStatic) {
        List<DotNetVariable> members = new ArrayList<>();
        for (PsiElement member : type.getMembers()) {
            if (member instanceof DotNetVariable variable && isConstructorMember(variable, isStatic)) {
                members.add(variable);
            }
        }
        return members;
    }

    /**
     * A field, a field-like event or an auto-property without an initializer, a constructor assigns
     */
    @RequiredReadAction
    public static boolean isConstructorMember(DotNetVariable variable, boolean isStatic) {
        if (variable.hasModifier(CSharpModifier.STATIC) != isStatic || variable.isConstant() || variable.getInitializer() != null) {
            return false;
        }
        return variable instanceof DotNetFieldDeclaration
            || variable instanceof DotNetEventDeclaration event && event.getAccessors().length == 0
            || variable instanceof CSharpPropertyDeclaration property && isAutoProperty(property);
    }

    @RequiredReadAction
    private static boolean isAutoProperty(CSharpPropertyDeclaration property) {
        DotNetXAccessor[] accessors = property.getAccessors();
        if (accessors.length == 0 || property.isAutoGet() || property.hasModifier(CSharpModifier.ABSTRACT) || property.hasModifier(CSharpModifier.EXTERN)) {
            return false;
        }
        for (DotNetXAccessor accessor : accessors) {
            if (!(accessor instanceof CSharpSimpleLikeMethodAsElement method) || !method.getCodeBlock().isSemicolonOrEmpty()) {
                return false;
            }
        }
        return true;
    }

    @RequiredReadAction
    private static boolean isThis(@Nullable PsiElement expression) {
        return expression instanceof CSharpReferenceExpression reference && reference.getQualifier() == null
            && PsiUtilCore.getElementType(reference.getReferenceElement()) == CSharpTokens.THIS_KEYWORD;
    }

    /**
     * x, this.x - a member of the struct constructed
     */
    @RequiredReadAction
    private @Nullable PsiElement structMember(@Nullable PsiElement expression) {
        if (myStruct == null || !(expression instanceof CSharpReferenceExpression reference) || isThis(reference)
            || reference.getQualifier() != null && !isThis(reference.getQualifier())) {
            return null;
        }
        PsiElement target = reference.resolve();
        return target != null && target.getParent() == myStruct ? target : null;
    }

    @RequiredReadAction
    public static CSharpControlFlow build(PsiElement owner, PsiElement body) {
        return new CSharpControlFlowBuilder(owner).buildImpl(owner, body);
    }

    @RequiredReadAction
    private CSharpControlFlow buildImpl(PsiElement owner, PsiElement body) {
        node(null, CSharpInstruction.ENTRY);

        // the parameters are assigned - not the out ones
        DotNetVariable[] parameters = owner instanceof CSharpLambdaExpressionImpl lambda ? lambda.getParameters()
            : owner instanceof DotNetParameterListOwner method ? method.getParameters() : new DotNetVariable[0];
        for (DotNetVariable parameter : parameters) {
            if (CSharpFlowVariables.isTracked(parameter) && !CSharpFlowVariables.isOut(parameter)) {
                readWrite(parameter, parameter, CSharpReadWriteInstruction.Access.WRITE);
            }
        }

        if (body instanceof DotNetExpression expression) {
            // => expression - returns it
            expression.accept(this);
            returnTo(owner);
        }
        else {
            visitChildren(body);
        }

        CSharpInstruction end = node(owner, CSharpInstruction.END);

        myGotoScope.resolve(myBuilder);

        node(null, CSharpInstruction.EXIT);

        ControlFlow flow = myBuilder.completeControlFlow();
        return new CSharpControlFlow(owner, flow.getInstructions(), end, myIterator, mySectionEnds, myReturns, myStructMembers);
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
     * A return - to the exit, the out parameters must be assigned there
     */
    private void returnTo(PsiElement statement) {
        Instruction instruction = myBuilder.prevInstruction;
        if (instruction != null) {
            myReturns.computeIfAbsent(instruction, it -> new ArrayList<>()).add(statement);
        }
        exitTo(null);
    }

    private void readWrite(PsiElement element, DotNetVariable variable, CSharpReadWriteInstruction.Access access) {
        myBuilder.addNodeAndCheckPending(new CSharpReadWriteInstruction(myBuilder, element, variable, access));
    }

    /**
     * The flow goes on from all the ends - joined in a node if more than one
     */
    private void join(List<Instruction> ends, PsiElement element) {
        Set<Instruction> distinct = new LinkedHashSet<>(ends);
        distinct.remove(null);
        myBuilder.flowAbrupted();
        if (distinct.size() == 1) {
            myBuilder.prevInstruction = distinct.iterator().next();
        }
        else if (!distinct.isEmpty()) {
            edges(List.copyOf(distinct), node(element, CSharpInstruction.JOIN));
        }
    }

    /**
     * A branch of a condition, from its exits
     */
    private void conditional(List<Instruction> exits, PsiElement element, PsiElement condition, boolean result) {
        myBuilder.flowAbrupted();
        CSharpConditionalInstruction instruction = new CSharpConditionalInstruction(myBuilder, element, condition, result);
        myBuilder.addNodeAndCheckPending(instruction);
        edges(exits, instruction);
    }

    private List<Instruction> current() {
        Instruction instruction = myBuilder.prevInstruction;
        return instruction == null ? List.of() : List.of(instruction);
    }

    /**
     * Visits a condition - a missing one is true
     */
    @RequiredReadAction
    private Exits condition(@Nullable DotNetExpression expression) {
        while (expression instanceof CSharpParenthesesExpressionImpl parentheses) {
            expression = parentheses.getInnerExpression();
        }
        if (expression == null) {
            return new Exits(current(), current());
        }

        if (expression instanceof CSharpPrefixExpressionImpl prefix && prefix.getOperatorElement().getOperatorElementType() == CSharpTokens.EXCL) {
            return condition(prefix.getExpression()).swap();
        }

        if (expression instanceof CSharpBinaryExpressionImpl binary) {
            IElementType operator = binary.getOperatorElement().getOperatorElementType();
            if (operator == CSharpTokens.ANDAND || operator == CSharpTokens.OROR) {
                Exits left = condition(binary.getLeftExpression());
                join(operator == CSharpTokens.ANDAND ? left.whenTrue() : left.whenFalse(), binary);
                Exits right = condition(binary.getRightExpression());
                return operator == CSharpTokens.ANDAND
                    ? new Exits(right.whenTrue(), concat(left.whenFalse(), right.whenFalse()))
                    : new Exits(concat(left.whenTrue(), right.whenTrue()), right.whenFalse());
            }
        }

        if (expression instanceof CSharpIsExpressionImpl isExpression) {
            accept(isExpression.getExpression());
            List<DotNetVariable> variables = patternVariables(isExpression);
            // unassigned when it does not match - also in the next iteration of a loop
            for (DotNetVariable variable : variables) {
                readWrite(variable, variable, CSharpReadWriteInstruction.Access.DECLARE);
            }
            Instruction unmatched = myBuilder.prevInstruction;
            for (DotNetVariable variable : variables) {
                readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
            }
            Exits exits = new Exits(current(), unmatched == null ? List.of() : List.of(unmatched));
            // o is not int i - assigned when false
            return isNegated(isExpression) ? exits.swap() : exits;
        }

        Boolean value = ConstantExpressionEvaluator.evaluateBoolean(expression);
        expression.accept(this);
        if (value == null || myBuilder.prevInstruction == null) {
            return new Exits(current(), current());
        }
        // false is definitely assigned when true - vacuously (C# spec 9.4.4.21)
        Instruction instruction = myBuilder.prevInstruction;
        Instruction vacuous = node(expression, CSharpInstruction.VACUOUS);
        return value ? new Exits(List.of(instruction), List.of(vacuous)) : new Exits(List.of(vacuous), List.of(instruction));
    }

    private static List<Instruction> concat(List<Instruction> first, List<Instruction> second) {
        List<Instruction> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    private static List<DotNetVariable> patternVariables(PsiElement pattern) {
        List<DotNetVariable> variables = new ArrayList<>();
        for (DotNetVariable variable : PsiTreeUtil.findChildrenOfAnyType(pattern, CSharpIsVariableImpl.class, CSharpCaseVariableImpl.class)) {
            variables.add(variable);
        }
        return variables;
    }

    private static boolean isNegated(CSharpIsExpressionImpl expression) {
        for (PsiElement child = expression.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpPatternImpl pattern) {
                return pattern.getNode().getElementType() == CSharpElements.NOT_PATTERN;
            }
        }
        return false;
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
        if (element instanceof CSharpSwitchExpressionImpl switchExpression) {
            visitSwitchExpression(switchExpression);
            return;
        }
        if (element instanceof DotNetStatement) {
            node(element);
        }
        visitChildren(element);
    }

    // region variables

    @Override
    @RequiredReadAction
    public void visitReferenceExpression(CSharpReferenceExpression expression) {
        if (myThisChecked) {
            if (isThis(expression)) {
                node(expression, CSharpInstruction.THIS_USE);
                return;
            }
            PsiElement member = structMember(expression);
            if (member != null) {
                // a field read - it must be assigned (CS9015), not a use of this; a method, a property with a body - a use
                if (member instanceof DotNetVariable variable && myStructMembers.contains(member)) {
                    readWrite(expression, variable, CSharpReadWriteInstruction.Access.READ);
                }
                else if (member instanceof DotNetModifierListOwner owner && !owner.hasModifier(CSharpModifier.STATIC)
                    && !(member instanceof DotNetVariable variable && variable.isConstant())) {
                    node(expression, CSharpInstruction.THIS_USE);
                }
                return;
            }
        }
        PsiElement target = resolveLocal(expression);
        if (target instanceof DotNetVariable variable) {
            readWrite(expression, variable, CSharpReadWriteInstruction.Access.READ);
            return;
        }
        if (target instanceof CSharpLocalMethodDeclarationImpl) {
            // the variables it reads must be assigned here
            node(expression, CSharpInstruction.CALL);
        }
        visitChildren(expression);
    }

    /**
     * A variable or a local function of the member the reference is to - only a reference by one of their names is resolved
     */
    @RequiredReadAction
    private @Nullable PsiElement resolveLocal(CSharpReferenceExpression expression) {
        if (expression.getQualifier() != null || !myNames.contains(expression.getReferenceName())) {
            return null;
        }
        PsiElement target = expression.resolve();
        if (target == null || myTop == null || !PsiTreeUtil.isAncestor(myTop, target, false)) {
            return null;
        }
        if (target instanceof CSharpLocalMethodDeclarationImpl) {
            return target;
        }
        return CSharpFlowVariables.isTracked(target) ? target : null;
    }

    @Override
    @RequiredReadAction
    public void visitLocalVariable(CSharpLocalVariable variable) {
        DotNetExpression initializer = variable.getInitializer();
        if (initializer == null) {
            if (CSharpFlowVariables.isTracked(variable)) {
                readWrite(variable, variable, CSharpReadWriteInstruction.Access.DECLARE);
            }
            return;
        }
        initializer.accept(this);
        if (CSharpFlowVariables.isTracked(variable)) {
            readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
        }
    }

    @Override
    @RequiredReadAction
    public void visitDeconstructionStatement(CSharpDeconstructionStatementImpl declaration) {
        node(declaration);
        for (PsiElement child = declaration.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetExpression) {
                child.accept(this);
            }
        }
        for (CSharpTupleVariable variable : declaration.getVariables()) {
            readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
        }
    }

    @Override
    @RequiredReadAction
    public void visitAssignmentExpression(CSharpAssignmentExpressionImpl expression) {
        DotNetExpression left = expression.getLeftExpression();
        DotNetExpression right = expression.getRightExpression();
        if (expression.getOperatorElement().getOperatorElementType() != CSharpTokens.EQ) {
            // x += 1 - read, then written
            accept(left);
            accept(right);
            write(left, false);
            return;
        }
        // the target is assigned after the value
        write(left, true);
        accept(right);
        write(left, false);
    }

    /**
     * The writes to the target of an assignment - or, before the value (prepare), the reads of the target which is
     * not a variable ({@code a.b = c} reads a)
     */
    @RequiredReadAction
    private void write(@Nullable DotNetExpression target, boolean prepare) {
        while (target instanceof CSharpParenthesesExpressionImpl parentheses) {
            target = parentheses.getInnerExpression();
        }
        if (myStruct != null) {
            // this = default - all of it
            if (isThis(target)) {
                if (!prepare) {
                    for (DotNetVariable member : myStructMembers) {
                        readWrite(target, member, CSharpReadWriteInstruction.Access.WRITE);
                    }
                }
                return;
            }
            if (structMember(target) instanceof DotNetVariable member && myStructMembers.contains(member)) {
                if (!prepare) {
                    readWrite(target, member, CSharpReadWriteInstruction.Access.WRITE);
                }
                return;
            }
        }
        if (target instanceof CSharpReferenceExpression reference && resolveLocal(reference) instanceof DotNetVariable variable) {
            if (!prepare) {
                readWrite(reference, variable, CSharpReadWriteInstruction.Access.WRITE);
            }
            return;
        }
        if (target instanceof CSharpTupleExpressionImpl tuple) {
            // (x, var y) = ...
            for (CSharpTupleElementImpl element : tuple.getElements()) {
                DotNetExpression expression = element.getExpression();
                if (expression instanceof CSharpOutRefVariableExpressionImpl declaration) {
                    if (!prepare && declaration.getVariable() instanceof DotNetVariable variable && CSharpFlowVariables.isTracked(variable)) {
                        readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
                    }
                }
                else {
                    write(expression, prepare);
                }
            }
            return;
        }
        if (prepare) {
            accept(target);
        }
    }

    @Override
    @RequiredReadAction
    public void visitPostfixExpression(CSharpPostfixExpressionImpl expression) {
        // x++ - read and written
        accept(expression.getExpression());
        write(expression.getExpression(), false);
    }

    @Override
    @RequiredReadAction
    public void visitPrefixExpression(CSharpPrefixExpressionImpl expression) {
        IElementType operator = expression.getOperatorElement().getOperatorElementType();
        if (operator == CSharpTokens.PLUSPLUS || operator == CSharpTokens.MINUSMINUS) {
            accept(expression.getExpression());
            write(expression.getExpression(), false);
        }
        else if (operator == CSharpTokens.AND) {
            // &x - its address, it need not be assigned
            write(expression.getExpression(), true);
            write(expression.getExpression(), false);
        }
        else if (operator == CSharpTokens.EXCL) {
            join(allExits(condition(expression)), expression);
        }
        else {
            visitChildren(expression);
        }
    }

    private static List<Instruction> allExits(Exits exits) {
        return concat(exits.whenTrue(), exits.whenFalse());
    }

    @Override
    @RequiredReadAction
    public void visitBinaryExpression(CSharpBinaryExpressionImpl expression) {
        IElementType operator = expression.getOperatorElement().getOperatorElementType();
        if (operator == CSharpTokens.ANDAND || operator == CSharpTokens.OROR) {
            join(allExits(condition(expression)), expression);
            return;
        }
        visitChildren(expression);
    }

    @Override
    @RequiredReadAction
    public void visitIsExpression(CSharpIsExpressionImpl expression) {
        join(allExits(condition(expression)), expression);
    }

    @Override
    @RequiredReadAction
    public void visitConditionalExpression(CSharpConditionalExpressionImpl expression) {
        Exits exits = condition(expression.getConditionExpression());
        join(exits.whenTrue(), expression);
        accept(expression.getTrueExpression());
        List<Instruction> trueEnd = current();
        join(exits.whenFalse(), expression);
        accept(expression.getFalseExpression());
        join(concat(trueEnd, current()), expression);
    }

    @Override
    @RequiredReadAction
    public void visitNullCoalescingExpression(CSharpNullCoalescingExpressionImpl expression) {
        // x ?? (y = 1) - y is not assigned when x is not null
        accept(expression.getCondition());
        List<Instruction> notNull = current();
        accept(expression.getResult());
        join(concat(notNull, current()), expression);
    }

    @RequiredReadAction
    private void visitSwitchExpression(CSharpSwitchExpressionImpl expression) {
        accept(expression.getExpression());
        List<Instruction> head = current();
        List<Instruction> ends = new ArrayList<>();
        for (CSharpSwitchExpressionArmImpl arm : expression.getArms()) {
            join(head, arm);
            PsiElement pattern = arm.getPattern();
            if (pattern != null) {
                for (DotNetVariable variable : pattern instanceof CSharpCaseVariableImpl caseVariable ? List.<DotNetVariable>of(caseVariable) : patternVariables(pattern)) {
                    readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
                }
            }
            if (arm.getWhenExpression() != null) {
                join(condition(arm.getWhenExpression()).whenTrue(), arm);
            }
            accept(arm.getResultExpression());
            ends.addAll(current());
        }
        // no arm matching - an exception
        join(ends, expression);
    }

    @Override
    public void visitNameOfExpression(CSharpNameOfExpressionImpl expression) {
        // nameof(x) - not a read
    }

    @Override
    @RequiredReadAction
    public void visitOurRefWrapExpression(CSharpOutRefWrapExpressionImpl expression) {
        DotNetExpression inner = expression.getInnerExpression();
        if (expression.getExpressionType() != CSharpRefTypeRef.Type.out) {
            // ref x - read
            accept(inner);
            return;
        }
        while (inner instanceof CSharpParenthesesExpressionImpl parentheses) {
            inner = parentheses.getInnerExpression();
        }
        if (inner instanceof CSharpReferenceExpression reference && resolveLocal(reference) instanceof DotNetVariable variable) {
            outWrite(reference, variable);
            return;
        }
        if (structMember(inner) instanceof DotNetVariable member && myStructMembers.contains(member)) {
            outWrite(inner, member);
            return;
        }
        accept(inner);
    }

    @Override
    @RequiredReadAction
    public void visitOutRefVariableExpression(CSharpOutRefVariableExpressionImpl expression) {
        // out var x - declared, assigned by the call
        if (expression.getVariable() instanceof DotNetVariable variable && CSharpFlowVariables.isTracked(variable)) {
            readWrite(variable, variable, CSharpReadWriteInstruction.Access.DECLARE);
            outWrite(variable, variable);
        }
    }

    private void outWrite(PsiElement element, DotNetVariable variable) {
        if (myOutWrites != null) {
            myOutWrites.add(Map.entry(element, variable));
        }
        else {
            readWrite(element, variable, CSharpReadWriteInstruction.Access.WRITE);
        }
    }

    @Override
    public void visitMethodCallExpression(CSharpMethodCallExpressionImpl expression) {
        call(expression);
    }

    @Override
    public void visitNewExpression(CSharpNewExpression expression) {
        call(expression);
    }

    @Override
    public void visitIndexAccessExpression(CSharpIndexAccessExpressionImpl expression) {
        call(expression);
    }

    /**
     * The arguments, then the out ones assigned (C# spec 9.4.4.6)
     */
    private void call(PsiElement expression) {
        List<Map.Entry<PsiElement, DotNetVariable>> outer = myOutWrites;
        List<Map.Entry<PsiElement, DotNetVariable>> outWrites = new ArrayList<>();
        myOutWrites = outWrites;
        try {
            visitChildren(expression);
        }
        finally {
            myOutWrites = outer;
        }
        for (Map.Entry<PsiElement, DotNetVariable> write : outWrites) {
            readWrite(write.getKey(), write.getValue(), CSharpReadWriteInstruction.Access.WRITE);
        }
        memberNotNull(expression);
    }

    /**
     * Init() with [MemberNotNull(nameof(x))] - assigns x of the type constructed
     */
    @RequiredReadAction
    private void memberNotNull(PsiElement expression) {
        if (myStructMembers.isEmpty() || !(expression instanceof CSharpMethodCallExpressionImpl call)
            || !(call.getCallExpression() instanceof CSharpReferenceExpression callee) || structMember(callee) == null
            || !(callee.resolve() instanceof DotNetModifierListOwner method)) {
            return;
        }
        DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(method, MEMBER_NOT_NULL);
        if (!(attribute instanceof CSharpAttribute csharpAttribute)) {
            return;
        }
        for (DotNetExpression argument : csharpAttribute.getParameterExpressions()) {
            String name = argument instanceof CSharpNameOfExpressionImpl nameOf ? nameOfName(nameOf) : new ConstantExpressionEvaluator(argument).getValueAs(String.class);
            for (DotNetVariable member : myStructMembers) {
                if (member.getName() != null && member.getName().equals(name)) {
                    readWrite(expression, member, CSharpReadWriteInstruction.Access.WRITE);
                }
            }
        }
    }

    private static final String MEMBER_NOT_NULL = "System.Diagnostics.CodeAnalysis.MemberNotNullAttribute";

    /**
     * nameof(x), nameof(this.x) - x
     */
    @RequiredReadAction
    private static @Nullable String nameOfName(CSharpNameOfExpressionImpl nameOf) {
        CSharpReferenceExpression reference = PsiTreeUtil.findChildOfType(nameOf, CSharpReferenceExpression.class);
        return reference == null ? null : reference.getReferenceName();
    }

    // endregion

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
        Exits exits = condition(condition);
        // a constant condition (C# spec 13.2) - not a constant part of it
        Boolean value = ConstantExpressionEvaluator.evaluateBoolean(condition);

        DotNetStatement trueStatement = statement.getTrueStatement();
        DotNetStatement falseStatement = statement.getFalseStatement();

        branch(exits.whenTrue(), value != Boolean.FALSE, trueStatement != null ? trueStatement : statement, condition, true);
        accept(trueStatement);
        myBuilder.addPendingEdge(statement, myBuilder.prevInstruction);

        branch(exits.whenFalse(), value != Boolean.TRUE, falseStatement != null ? falseStatement : statement, condition, false);
        accept(falseStatement);
    }

    /**
     * The start of a branch - none if the constant condition never takes it
     */
    private void branch(List<Instruction> exits, boolean taken, PsiElement element, @Nullable DotNetExpression condition, boolean result) {
        myBuilder.flowAbrupted();
        if (!taken) {
            return;
        }
        if (condition == null) {
            join(exits, element);
            return;
        }
        conditional(exits, element, condition, result);
    }

    @Override
    @RequiredReadAction
    public void visitWhileStatement(CSharpWhileStatementImpl statement) {
        Instruction head = node(statement);
        DotNetExpression condition = statement.getConditionExpression();
        loop(statement, head, condition, condition(condition), statement.getChildStatement(), List.of());
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
        loop(statement, head, condition, condition(condition), body, iterators);
    }

    /**
     * The condition is evaluated, then the body, the iterators of a for, and again the condition at the head
     */
    @RequiredReadAction
    private void loop(DotNetStatement loop,
                      Instruction head,
                      @Nullable DotNetExpression condition,
                      Exits exits,
                      @Nullable DotNetStatement body,
                      List<DotNetExpression> iterators) {
        // for (;;) - true
        Boolean value = condition == null ? Boolean.valueOf(loop instanceof CSharpForStatementImpl) : ConstantExpressionEvaluator.evaluateBoolean(condition);

        branch(exits.whenTrue(), value != Boolean.FALSE, body != null ? body : loop, condition, true);
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

        branch(exits.whenFalse(), value != Boolean.TRUE, loop, condition, false);
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
        Exits exits = condition(condition);
        Boolean value = ConstantExpressionEvaluator.evaluateBoolean(condition);

        DotNetStatement body = statement.getChildStatement();
        branch(exits.whenTrue(), value != Boolean.FALSE, body != null ? body : statement, condition, true);
        myBuilder.addEdge(myBuilder.prevInstruction, head);

        branch(exits.whenFalse(), value != Boolean.TRUE, statement, condition, false);
    }

    @Override
    @RequiredReadAction
    public void visitForeachStatement(CSharpForeachStatementImpl statement) {
        node(statement);
        accept(statement.getIterableExpression());

        // MoveNext - the next element to the variable, or the exit
        CSharpLocalVariable variable = statement.getVariable();
        Instruction head;
        if (variable != null) {
            readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
            head = myBuilder.prevInstruction;
        }
        else {
            head = node(statement, "next");
        }
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
                    for (DotNetVariable variable : patternVariables(label)) {
                        readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
                    }
                    if (label instanceof CSharpCasePatternStatementImpl pattern && pattern.getWhenExpression() != null) {
                        join(condition(pattern.getWhenExpression()).whenTrue(), label);
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
        // out of no loop (CS0139) - as Roslyn, the flow goes on
        PsiElement target = myBreakTargets.peek();
        if (target != null) {
            exitTo(target);
        }
    }

    @Override
    public void visitContinueStatement(CSharpContinueStatementImpl statement) {
        node(statement);
        PsiElement target = myContinueTargets.peek();
        if (target != null) {
            exitTo(target);
        }
    }

    @Override
    public void visitReturnStatement(CSharpReturnStatementImpl statement) {
        node(statement);
        visitChildren(statement);
        returnTo(statement);
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

        // to no label (CS0159) - as Roslyn, the flow goes on
        if (target != null) {
            jumpTo(instruction, target);
            myBuilder.flowAbrupted();
        }
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
        List<Instruction> throwing = new ArrayList<>();
        try {
            if (tryBlock != null) {
                tryBlock.accept(this);
                normalEnds.addAll(collectEnds(tryBlock));
            }
            // an exception from any node of the try block - with the state after it, or before its first one
            throwing.add(tryNode);
            throwing.addAll(myBuilder.instructions.subList(tryNode.num() + 1, myBuilder.instructions.size()));

            for (CSharpCatchStatementImpl catchStatement : statement.getCatchStatements()) {
                myBuilder.flowAbrupted();
                edges(throwing, node(catchStatement));
                CSharpLocalVariable variable = catchStatement.getVariable();
                if (variable != null) {
                    readWrite(variable, variable, CSharpReadWriteInstruction.Access.WRITE);
                }
                for (PsiElement child = catchStatement.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child != variable && child.getFirstChild() != null) {
                        child.accept(this);
                    }
                }
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
                JumpKey key = new JumpKey(pendingScope, myGotoJumps.remove(instruction), myReturns.containsKey(instruction));
                jumps.computeIfAbsent(key, it -> new ArrayList<>()).add(instruction);
            }
            else {
                myBuilder.addPendingEdge(pendingScope, instruction);
            }
        });

        // an exception from the try block or a catch - the finally block, then up to the exit
        List<Instruction> failing = new ArrayList<>(myBuilder.instructions.subList(tryNode.num(), myBuilder.instructions.size()));
        for (Instruction end : finallyCopy(finallyStatement, failing)) {
            myBuilder.addPendingEdge(null, end);
        }

        if (!normalEnds.isEmpty()) {
            for (Instruction end : finallyCopy(finallyStatement, normalEnds)) {
                myBuilder.addPendingEdge(statement, end);
            }
        }

        for (Map.Entry<JumpKey, List<Instruction>> jump : jumps.entrySet()) {
            JumpKey key = jump.getKey();
            List<PsiElement> returns = new ArrayList<>();
            if (key.returns()) {
                for (Instruction instruction : jump.getValue()) {
                    returns.addAll(myReturns.remove(instruction));
                }
            }
            for (Instruction end : finallyCopy(finallyStatement, jump.getValue())) {
                if (key.returns()) {
                    myReturns.computeIfAbsent(end, it -> new ArrayList<>()).addAll(returns);
                }
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
