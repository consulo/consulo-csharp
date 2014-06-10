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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0102 extends CompilerCheck<CSharpTypeDeclaration>
{
	@NotNull
	@Override
	public List<CompilerCheckResult> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpTypeDeclaration element)
	{
		return doCheck(this, element);
	}

	@NotNull
	public static <T extends DotNetMemberOwner & DotNetQualifiedElement> List<CompilerCheckResult> doCheck(
			@NotNull CompilerCheck<T> compilerCheck, @NotNull T element)
	{
		List<CompilerCheckResult> results = new SmartList<CompilerCheckResult>();

		final DotNetNamedElement[] members = element.getMembers();

		for(val namedElement : members)
		{
			val name = namedElement.getName();
			DotNetNamedElement findTarget = ContainerUtil.find(members, new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					if(element == namedElement)
					{
						return false;
					}
					if(element instanceof DotNetLikeMethodDeclaration && namedElement instanceof DotNetLikeMethodDeclaration)
					{
						return checkMethod((DotNetLikeMethodDeclaration) element, (DotNetLikeMethodDeclaration) namedElement);
					}
					else if(element instanceof DotNetTypeDeclaration && namedElement instanceof DotNetTypeDeclaration)
					{
						if(((DotNetTypeDeclaration) element).getGenericParametersCount() != ((DotNetTypeDeclaration) namedElement)
								.getGenericParametersCount())
						{
							return false;
						}
					}
					return Comparing.equal(element.getName(), name);
				}
			});

			if(findTarget != null)
			{
				PsiElement toHighlight = findTarget;
				if(findTarget instanceof PsiNameIdentifierOwner)
				{
					PsiElement nameIdentifier = ((PsiNameIdentifierOwner) findTarget).getNameIdentifier();
					toHighlight = ObjectUtils.notNull(nameIdentifier, findTarget);
				}

				results.add(compilerCheck.result(toHighlight, element.getPresentableQName(), findTarget.getName()));
			}
		}
		return results;
	}

	public static boolean checkMethod(DotNetLikeMethodDeclaration o1, DotNetLikeMethodDeclaration o2)
	{
		if(o1 == o2)
		{
			return true;
		}

		if(!Comparing.equal(o1.getName(), o2.getName()))
		{
			return false;
		}

		DotNetTypeRef[] pa1 = o1.getParameterTypeRefs();
		DotNetTypeRef[] pa2 = o2.getParameterTypeRefs();
		if(pa1.length != pa2.length)
		{
			return false;
		}


		for(int i = 0; i < pa2.length; i++)
		{
			DotNetTypeRef p1 = pa1[i];
			DotNetTypeRef p2 = pa2[i];

			if(!Comparing.equal(p1.getQualifiedText(), p2.getQualifiedText()))
			{
				return false;
			}
		}
		return true;
	}
}
