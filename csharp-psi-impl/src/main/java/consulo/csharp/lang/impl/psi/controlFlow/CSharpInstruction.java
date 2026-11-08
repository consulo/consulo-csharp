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

import consulo.language.ast.ASTNode;
import consulo.language.controlFlow.ControlFlowBuilder;
import consulo.language.controlFlow.base.InstructionImpl;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * A node of a C# control flow graph - a statement, the creation of a lambda, or a marker (entry, end of the body,
 * exit, end of a switch section, a join of branches) named in the dump
 *
 * @author VISTALL
 */
public class CSharpInstruction extends InstructionImpl {
    public static final String ENTRY = "entry";
    public static final String END = "end";
    public static final String EXIT = "exit";
    public static final String SECTION_END = "section end";
    public static final String JOIN = "join";
    // the other exit of a constant part of a condition - definitely assigned, nothing is reported after it
    public static final String VACUOUS = "vacuous";
    // a reference to a local function - the variables it reads must be assigned
    public static final String CALL = "call";
    // a use of this in a struct constructor - all its fields must be assigned
    public static final String THIS_USE = "this";

    private final @Nullable String myMarker;

    public CSharpInstruction(ControlFlowBuilder builder, @Nullable PsiElement element, @Nullable String marker) {
        super(builder, element);
        myMarker = marker;
    }

    public boolean is(String marker) {
        return marker.equals(myMarker);
    }

    @Override
    public String getElementPresentation() {
        PsiElement element = getElement();
        if (myMarker == null) {
            return presentation(element);
        }
        // the end of the owner - its element
        return element == null || END.equals(myMarker) ? myMarker : myMarker + " " + presentation(element);
    }

    /**
     * {@code IF_STATEMENT if (b) return 1;} - the element type and the text on one line, cut after 50 characters
     */
    public static String presentation(@Nullable PsiElement element) {
        if (element == null) {
            return "null";
        }
        ASTNode node = element.getNode();
        String text = element.getText().replaceAll("\\s+", " ").trim();
        if (text.length() > 50) {
            text = text.substring(0, 50) + "...";
        }
        return (node == null ? element.getClass().getSimpleName() : node.getElementType().toString()) + " " + text;
    }
}
