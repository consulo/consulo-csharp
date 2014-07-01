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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class CS1620 extends CompilerCheck<CSharpMethodCallExpressionImpl>
{
	@NotNull
	@Override
	public List<CompilerCheckResult> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpMethodCallExpressionImpl element)
	{
		PsiElement psiElement = element.resolveToCallable();
		if(psiElement instanceof DotNetLikeMethodDeclaration)
		{
			DotNetExpression[] parameterExpressions = element.getParameterExpressions();
			DotNetParameter[] parameters = ((DotNetLikeMethodDeclaration) psiElement).getParameters();

			List<CompilerCheckResult> results = new SmartList<CompilerCheckResult>();
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetExpression dotNetExpression = ArrayUtil2.safeGet(parameterExpressions, i);
				if(dotNetExpression == null)
				{
					break;
				}

				DotNetParameter parameter = parameters[i];
				CSharpRefTypeRef.Type type = null;
				if(parameter.hasModifier(CSharpModifier.REF))
				{
					type = CSharpRefTypeRef.Type.ref;
				}
				else if(parameter.hasModifier(CSharpModifier.OUT))
				{
					type = CSharpRefTypeRef.Type.out;
				}
				else
				{
					continue;
				}
				DotNetTypeRef typeRef = dotNetExpression.toTypeRef(false);
				if(!(typeRef instanceof CSharpRefTypeRef) || ((CSharpRefTypeRef) typeRef).getType() != type)
				{
					results.add(result(dotNetExpression, String.valueOf(i + 1), type.name()));
				}
			}
			return results;
		}
		return Collections.emptyList();
	}
}
