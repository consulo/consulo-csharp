/*
 * Copyright 2013-2017 consulo.io
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
package consulo.csharp.impl.lang;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDefStatementImpl;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.refactoring.ImportOptimizer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Couple;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * @author VISTALL
 * @since 2014-01-01
 */
@ExtensionImpl
public class CSharpImportOptimizer implements ImportOptimizer {
    @Override
    public boolean supports(PsiFile psiFile) {
        return psiFile instanceof CSharpFile;
    }

    @Override
    public Runnable processFile(final PsiFile psiFile) {
        return new CollectingInfoRunnable() {
            private int myCount = 0;

            @Nullable
            @Override
            public String getUserNotificationInfo() {
                if (myCount > 0) {
                    return "removed " + myCount + " using statement" + (myCount != 1 ? "s" : "");
                }
                return null;
            }

            @Override
            @RequiredWriteAction
            public void run() {
                UnusedUsingVisitor unusedUsingVisitor = UnusedUsingVisitor.accept(psiFile);

                for (Map.Entry<CSharpUsingListChild, Boolean> entry : unusedUsingVisitor.getUsingContext().entrySet()) {
                    if (entry.getValue()) {
                        continue;
                    }

                    myCount++;
                    entry.getKey().delete();
                }

                final Set<CSharpUsingListChild> processedUsingStatements = new LinkedHashSet<>();
                final List<List<CSharpUsingListChild>> usingLists = new ArrayList<>();
                psiFile.accept(new CSharpRecursiveElementVisitor() {
                    @Override
                    @RequiredReadAction
                    public void visitUsingChild(CSharpUsingListChild child) {
                        super.visitUsingChild(child);

                        if (processedUsingStatements.contains(child)) {
                            return;
                        }

                        PsiElement referenceElement = child.getReferenceElement();
                        if (referenceElement == null) {
                            return;
                        }

                        List<CSharpUsingListChild> children = new ArrayList<>(5);

                        for (ASTNode node = child.getNode(); node != null; node = node.getTreeNext()) {
                            IElementType elementType = node.getElementType();
                            if (elementType == TokenType.WHITE_SPACE) {
                                CharSequence chars = node.getChars();
                                if (StringUtil.countNewLines(chars) > 2) {
                                    break;
                                }
                            }
                            else if (elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE) {
                                break;
                            }
                            else if (CSharpStubElementSets.USING_CHILDREN.contains(elementType)) {
                                children.add(node.getPsi(CSharpUsingListChild.class));
                            }
                        }

                        if (children.size() <= 1) {
                            return;
                        }

                        usingLists.add(children);

                        processedUsingStatements.addAll(children);
                    }
                });

                for (List<CSharpUsingListChild> usingList : usingLists) {
                    formatAndReplace(psiFile, usingList);
                }
            }
        };
    }

    @RequiredReadAction
    private static void formatAndReplace(PsiFile file, List<CSharpUsingListChild> children) {
        PsiElement parent = children.get(0).getParent();

        Comparator<String> namespaceComparator = (o1, o2) -> {
            boolean s1 = isSystem(o1);
            boolean s2 = isSystem(o2);

            if (s1 && s2 || !s1 && !s2) {
                return o1.compareToIgnoreCase(o2);
            }
            else {
                if (s1) {
                    return -1;
                }
                if (s2) {
                    return 1;
                }
                return 0;
            }
        };
        Set<String> namespaceUse = new TreeSet<>(namespaceComparator);
        Set<String> typeUse = new TreeSet<>(namespaceComparator);
        List<Pair<String, String>> typeDef = new ArrayList<>();
        for (CSharpUsingListChild statement : children) {
            if (statement instanceof CSharpUsingNamespaceStatement namespaceStatement) {
                DotNetReferenceExpression namespaceReference = namespaceStatement.getNamespaceReference();
                if (namespaceReference == null)  // if using don't have reference - don't format it
                {
                    return;
                }
                namespaceUse.add(namespaceReference.getText());
            }
            else if (statement instanceof CSharpTypeDefStatementImpl typeDefStmt) {
                DotNetType type = typeDefStmt.getType();
                if (type == null) {
                    return;
                }
                typeDef.add(Couple.of(typeDefStmt.getName(), type.getText()));
            }
            else if (statement instanceof CSharpUsingTypeStatement usingTypeStmt) {
                DotNetType type = usingTypeStmt.getType();
                if (type == null) {
                    return;
                }
                typeUse.add(type.getText());
            }
        }

        StringBuilder builder = new StringBuilder();

        if (!typeUse.isEmpty()) {
            for (String qName : typeUse) {
                builder.append("using static ").append(qName).append(";\n");
            }

            if (!namespaceUse.isEmpty() || !typeDef.isEmpty()) {
                builder.append("\n");
            }
        }

        if (!namespaceUse.isEmpty()) {
            for (String qName : namespaceUse) {
                builder.append("using ").append(qName).append(";\n");
            }

            if (!namespaceUse.isEmpty() && !typeDef.isEmpty()) {
                builder.append("\n");
            }
        }

        if (!typeDef.isEmpty()) {
            for (Pair<String, String> pair : typeDef) {
                builder.append("using ").append(pair.getFirst()).append(" = ").append(pair.getSecond()).append(";\n");
            }
        }

        CSharpFile newFile = CSharpFileFactory.createFile(file.getProject(), builder);

        CSharpUsingListChild[] usingStatements = newFile.getUsingStatements();

        PsiElement before = ContainerUtil.getLastItem(children).getNextSibling();

        parent.deleteChildRange(children.get(0), children.get(children.size() - 1));

        parent.addRangeBefore(usingStatements[0], usingStatements[usingStatements.length - 1], before);
    }

    private static boolean isSystem(String name) {
        return name.startsWith("System");
    }

    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
