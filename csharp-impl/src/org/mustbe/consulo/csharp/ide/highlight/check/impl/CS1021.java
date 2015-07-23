/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpConstantTypeRef;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class CS1021 extends CompilerCheck<CSharpConstantExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpConstantExpressionImpl element)
	{
		CSharpConstantTypeRef constantTypeRef = (CSharpConstantTypeRef) element.toTypeRef(false);
		PsiElement parent = element.getParent();
		if(parent instanceof CSharpCallArgument)
		{
			CSharpCallArgumentListOwner callArgumentListOwner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);
			if(callArgumentListOwner == null)
			{
				return null;
			}

			ResolveResult[] resolveResults = callArgumentListOwner.multiResolve(false);

			for(ResolveResult resolveResult : resolveResults)
			{
				if(resolveResult instanceof MethodResolveResult && resolveResult.isValidResult())
				{
					MethodCalcResult calcResult = ((MethodResolveResult) resolveResult).getCalcResult();
					for(NCallArgument nCallArgument : calcResult.getArguments())
					{
						CSharpCallArgument callArgument = nCallArgument.getCallArgument();
						if(callArgument == parent)
						{
							DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
							if(parameterTypeRef == null)
							{
								continue;
							}
							DotNetTypeRef mirror = constantTypeRef.doMirror(parameterTypeRef, element);
							if(mirror == null)
							{
								return newBuilder(element);
							}
						}
					}
				}
			}
		}
		else if(parent instanceof DotNetVariable)
		{
			DotNetTypeRef typeRef = ((DotNetVariable) parent).toTypeRef(false);
			if(typeRef == DotNetTypeRef.AUTO_TYPE)
			{
				DotNetTypeRef delegate = constantTypeRef.getDelegate();

				DotNetTypeRef mirror = constantTypeRef.doMirror(delegate, element);
				if(mirror == null)
				{
					return newBuilder(element);
				}
			}
			else
			{
				DotNetTypeRef mirror = constantTypeRef.doMirror(((DotNetVariable) parent).toTypeRef(false), element);
				if(mirror == null)
				{
					return newBuilder(element);
				}
			}
		}
		return null;
	}
}
