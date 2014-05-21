/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingNamespaceStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
public class CSharpImportOptimizer implements ImportOptimizer
{
	@Override
	public boolean supports(PsiFile psiFile)
	{
		return psiFile instanceof CSharpFileImpl;
	}

	@NotNull
	@Override
	public Runnable processFile(final PsiFile psiFile)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				psiFile.accept(new CSharpRecursiveElementVisitor()
				{
					@Override
					public void visitUsingNamespaceList(CSharpUsingListImpl list)
					{
						formatUsing(list);
					}
				});
			}
		};
	}

	private static void formatUsing(@NotNull CSharpUsingListImpl usingList)
	{
		Set<String> namespaceUse = new TreeSet<String>();
		List<Pair<String, String>> typeDef = new ArrayList<Pair<String, String>>();
		for(CSharpUsingListChild statement : usingList.getStatements())
		{
			if(statement instanceof CSharpUsingNamespaceStatementImpl)
			{
				DotNetReferenceExpression namespaceReference = ((CSharpUsingNamespaceStatementImpl) statement).getNamespaceReference();
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
		}

		StringBuilder builder = new StringBuilder();
		for(String qName : namespaceUse)
		{
			builder.append("using ").append(qName).append(";\n");
		}

		for(Pair<String, String> pair : typeDef)
		{
			builder.append("using ").append(pair.getFirst()).append(" = ").append(pair.getSecond()).append(";\n");
		}

		CSharpUsingListImpl usingListFromText = CSharpFileFactory.createUsingListFromText(usingList.getProject(), builder.toString());

		usingList.replace(usingListFromText);
	}
}
