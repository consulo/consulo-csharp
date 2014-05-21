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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaParameterImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0128 extends CompilerCheck<CSharpBlockStatementImpl>
{
	@NotNull
	@Override
	public List<CompilerCheckResult> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpBlockStatementImpl element)
	{
		PsiElement parent = element.getParent();
		if(!(parent instanceof DotNetModifierListOwner))
		{
			return Collections.emptyList();
		}
		val results = new ArrayList<CompilerCheckResult>();
		val names = new THashSet<String>();
		parent.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitLocalVariable(CSharpLocalVariable variable)
			{
				String name = variable.getName();
				if(names.contains(name))
				{
					results.add(result(variable.getNameIdentifier(), name));
				}
				else
				{
					names.add(name);
				}

				DotNetExpression initializer = variable.getInitializer();
				if(initializer != null)
				{
					initializer.accept(this);
				}
			}

			@Override
			public void visitParameter(DotNetParameter parameter)
			{
				String name = parameter.getName();
				names.add(name);
			}

			@Override
			public void visitLambdaParameter(CSharpLambdaParameterImpl parameter)
			{
				String name = parameter.getName();
				if(names.contains(name))
				{

					results.add(resultImpl(CS0136.class, parameter.getNameIdentifier(), name, name));
				}
				else
				{
					names.add(name);
				}
			}
		});
		return results;
	}
}
