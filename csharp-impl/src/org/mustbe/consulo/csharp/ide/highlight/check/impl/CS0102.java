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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
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
	public static <T extends DotNetMemberOwner & DotNetQualifiedElement> List<CompilerCheckResult> doCheck(@NotNull CompilerCheck<T> compilerCheck,
			@NotNull T element)
	{
		List<CompilerCheckResult> results = new SmartList<CompilerCheckResult>();

		final DotNetNamedElement[] members = element.getMembers();

		for(val namedElement : members)
		{
			DotNetNamedElement findTarget = ContainerUtil.find(members, new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					if(element == namedElement)
					{
						return false;
					}
					return CSharpElementCompareUtil.isEqualWithVirtualImpl(element, namedElement, element);
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
}
