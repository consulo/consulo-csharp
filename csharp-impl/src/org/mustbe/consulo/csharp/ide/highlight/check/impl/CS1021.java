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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class CS1021 extends CompilerCheck<CSharpConstantExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpConstantExpressionImpl element)
	{
		/*if(!CSharpConstantTypeRef.isNumberLiteral(element))
		{
			return null;
		}
		PsiElement parent = element.getParent();
		DotNetTypeRef tempTypeRef = element.toTypeRef(false);
		if(tempTypeRef instanceof CSharpConstantTypeRef)
		{
			CSharpConstantTypeRef constantTypeRef = (CSharpConstantTypeRef) tempTypeRef;
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
				if(typeRef != DotNetTypeRef.AUTO_TYPE)
				{
					DotNetTypeRef variableTypeRef = ((DotNetVariable) parent).toTypeRef(false);

					PsiElement typeRefElement = variableTypeRef.resolve(element).getElement();

					if(typeRefElement instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) typeRefElement).isEnum())
					{
						variableTypeRef = ((DotNetTypeDeclaration) typeRefElement).getTypeRefForEnumConstants();
					}

					DotNetTypeRef mirror = constantTypeRef.doMirror(variableTypeRef, element);
					if(mirror == null)
					{
						return newBuilder(element);
					}
				}
			}
			return null;
		}
		else
		{
			if(parent instanceof DotNetVariable)
			{
				DotNetTypeRef typeRef = ((DotNetVariable) parent).toTypeRef(false);
				if(typeRef == DotNetTypeRef.AUTO_TYPE)
				{
					if(tempTypeRef == DotNetTypeRef.ERROR_TYPE)
					{
						return newBuilder(element);
					}
				}
			}

			return null;
		} */
		return null;
	}
}
