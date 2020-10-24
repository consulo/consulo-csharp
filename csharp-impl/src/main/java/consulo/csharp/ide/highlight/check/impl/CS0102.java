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

package consulo.csharp.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0102 extends CompilerCheck<CSharpTypeDeclaration>
{
	@RequiredReadAction
	@Nonnull
	@Override
	public List<CompilerCheckBuilder> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpTypeDeclaration element)
	{
		return doCheck(this, element);
	}

	@Nonnull
	@RequiredReadAction
	public static List<CompilerCheckBuilder> doCheck(@Nonnull CompilerCheck<? extends DotNetMemberOwner> compilerCheck, @Nonnull DotNetMemberOwner t)
	{
		List<CompilerCheckBuilder> results = new SmartList<>();

		final DotNetNamedElement[] members = t.getMembers();

		List<DotNetNamedElement> duplicateMembers = new ArrayList<>();
		for(DotNetNamedElement searchElement : members)
		{
			if(!(searchElement instanceof PsiNameIdentifierOwner))
			{
				continue;
			}

			String searchName = searchElement.getName();
			if(searchName == null)
			{
				continue;
			}

			for(DotNetNamedElement mayDuplicate : members)
			{
				if(searchElement == mayDuplicate)
				{
					continue;
				}

				if(!(mayDuplicate instanceof PsiNameIdentifierOwner))
				{
					continue;
				}

				String targetName = mayDuplicate.getName();
				if(searchName.equals(targetName))
				{
					// skip type declarations if partial
					if(searchElement instanceof CSharpTypeDeclaration && mayDuplicate instanceof CSharpTypeDeclaration && CSharpElementCompareUtil.isEqualWithVirtualImpl(searchElement, mayDuplicate
					) && isPartial(searchElement) && isPartial(mayDuplicate))
					{
						continue;
					}

					if(searchElement instanceof CSharpTypeDeclaration && mayDuplicate instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) searchElement).getGenericParametersCount() !=
							((CSharpTypeDeclaration) mayDuplicate).getGenericParametersCount())
					{
						continue;
					}

					if(searchElement instanceof DotNetLikeMethodDeclaration && mayDuplicate instanceof DotNetLikeMethodDeclaration && !CSharpElementCompareUtil.isEqualWithVirtualImpl(searchElement,
							mayDuplicate))
					{
						continue;
					}

					if(searchElement instanceof CSharpPropertyDeclaration && mayDuplicate instanceof CSharpPropertyDeclaration && !CSharpElementCompareUtil.isEqualWithVirtualImpl(searchElement,
							mayDuplicate))
					{
						continue;
					}

					duplicateMembers.add(mayDuplicate);
				}
			}
		}

		String name = t instanceof PsiFile ? "<global namespace>" : ((DotNetQualifiedElement) t).getPresentableQName();
		for(DotNetNamedElement duplicateMember : duplicateMembers)
		{
			PsiElement toHighlight = duplicateMember;
			if(duplicateMember instanceof PsiNameIdentifierOwner)
			{
				PsiElement nameIdentifier = ((PsiNameIdentifierOwner) duplicateMember).getNameIdentifier();
				toHighlight = ObjectUtil.notNull(nameIdentifier, duplicateMember);
			}
			results.add(compilerCheck.newBuilder(toHighlight, name, duplicateMember.getName()));
		}
		return results;
	}

	private static boolean isPartial(DotNetNamedElement element)
	{
		return element instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) element).hasModifier(CSharpModifier.PARTIAL);
	}
}
