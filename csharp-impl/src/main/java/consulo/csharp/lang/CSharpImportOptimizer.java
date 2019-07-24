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

package consulo.csharp.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
public class CSharpImportOptimizer implements ImportOptimizer
{
	@Override
	public boolean supports(PsiFile psiFile)
	{
		return psiFile instanceof CSharpFile;
	}

	@Nonnull
	@Override
	public Runnable processFile(final PsiFile psiFile)
	{
		return new CollectingInfoRunnable()
		{
			private int myCount = 0;

			@Nullable
			@Override
			public String getUserNotificationInfo()
			{
				if(myCount > 0)
				{
					return "removed " + myCount + " using statement" + (myCount != 1 ? "s" : "");
				}
				return null;
			}

			@Override
			@RequiredReadAction
			public void run()
			{
				final UnusedUsingVisitor unusedUsingVisitor = UnusedUsingVisitor.accept(psiFile);

				for(Map.Entry<CSharpUsingListChild, Boolean> entry : unusedUsingVisitor.getUsingContext().entrySet())
				{
					if(entry.getValue())
					{
						continue;
					}

					myCount++;
					entry.getKey().delete();
				}

				final Set<CSharpUsingListChild> processedUsingStatements = new LinkedHashSet<CSharpUsingListChild>();
				final List<List<CSharpUsingListChild>> usingLists = new ArrayList<List<CSharpUsingListChild>>();
				psiFile.accept(new CSharpRecursiveElementVisitor()
				{
					@Override
					@RequiredReadAction
					public void visitUsingChild(@Nonnull CSharpUsingListChild child)
					{
						super.visitUsingChild(child);

						if(processedUsingStatements.contains(child))
						{
							return;
						}

						PsiElement referenceElement = child.getReferenceElement();
						if(referenceElement == null)
						{
							return;
						}

						List<CSharpUsingListChild> children = new ArrayList<CSharpUsingListChild>(5);

						for(ASTNode node = child.getNode(); node != null; node = node.getTreeNext())
						{
							IElementType elementType = node.getElementType();
							if(elementType == TokenType.WHITE_SPACE)
							{
								CharSequence chars = node.getChars();
								if(StringUtil.countNewLines(chars) > 2)
								{
									break;
								}
							}
							else if(elementType == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
							{
								break;
							}
							else if(CSharpStubElements.USING_CHILDREN.contains(elementType))
							{
								children.add(node.getPsi(CSharpUsingListChild.class));
							}
						}

						if(children.size() <= 1)
						{
							return;
						}

						usingLists.add(children);

						processedUsingStatements.addAll(children);
					}
				});

				for(List<CSharpUsingListChild> usingList : usingLists)
				{
					formatAndReplace(psiFile, usingList);
				}
			}
		};
	}

	@RequiredReadAction
	private static void formatAndReplace(@Nonnull PsiFile file, @Nonnull List<CSharpUsingListChild> children)
	{
		PsiElement parent = children.get(0).getParent();

		Comparator<String> namespaceComparator = new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				boolean s1 = isSystem(o1);
				boolean s2 = isSystem(o2);

				if(s1 && s2 || !s1 && !s2)
				{
					return o1.compareToIgnoreCase(o2);
				}
				else
				{
					if(s1)
					{
						return -1;
					}
					if(s2)
					{
						return 1;
					}
					return 0;
				}
			}
		};
		Set<String> namespaceUse = new TreeSet<String>(namespaceComparator);
		Set<String> typeUse = new TreeSet<String>(namespaceComparator);
		List<Pair<String, String>> typeDef = new ArrayList<Pair<String, String>>();
		for(CSharpUsingListChild statement : children)
		{
			if(statement instanceof CSharpUsingNamespaceStatement)
			{
				DotNetReferenceExpression namespaceReference = ((CSharpUsingNamespaceStatement) statement).getNamespaceReference();
				if(namespaceReference == null)  // if using dont have reference - dont format it
				{
					return;
				}
				namespaceUse.add(namespaceReference.getText());
			}
			else if(statement instanceof CSharpTypeDefStatementImpl)
			{
				DotNetType type = ((CSharpTypeDefStatementImpl) statement).getType();
				if(type == null)
				{
					return;
				}
				typeDef.add(new Pair<String, String>(((CSharpTypeDefStatementImpl) statement).getName(), type.getText()));
			}
			else if(statement instanceof CSharpUsingTypeStatement)
			{
				DotNetType type = ((CSharpUsingTypeStatement) statement).getType();
				if(type == null)
				{
					return;
				}
				typeUse.add(type.getText());
			}
		}

		StringBuilder builder = new StringBuilder();

		if(!typeUse.isEmpty())
		{
			for(String qName : typeUse)
			{
				builder.append("using static ").append(qName).append(";\n");
			}

			if(!namespaceUse.isEmpty() || namespaceUse.isEmpty() && !typeDef.isEmpty())
			{
				builder.append("\n");
			}
		}

		if(!namespaceUse.isEmpty())
		{
			for(String qName : namespaceUse)
			{
				builder.append("using ").append(qName).append(";\n");
			}

			if(!namespaceUse.isEmpty() && !typeDef.isEmpty())
			{
				builder.append("\n");
			}
		}

		if(!typeDef.isEmpty())
		{
			for(Pair<String, String> pair : typeDef)
			{
				builder.append("using ").append(pair.getFirst()).append(" = ").append(pair.getSecond()).append(";\n");
			}
		}

		CSharpFile newFile = CSharpFileFactory.createFile(file.getProject(), builder);

		CSharpUsingListChild[] usingStatements = newFile.getUsingStatements();

		PsiElement before = ContainerUtil.getLastItem(children).getNextSibling();

		parent.deleteChildRange(children.get(0), children.get(children.size() - 1));

		parent.addRangeBefore(usingStatements[0], usingStatements[usingStatements.length - 1], before);
	}

	private static boolean isSystem(String name)
	{
		return name.startsWith("System");
	}
}
