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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpMethodUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPsiUtilImpl
{
	public static boolean isTypeLikeElement(@NotNull PsiElement element)
	{
		return element instanceof CSharpTypeDeclaration || CSharpMethodUtil.isDelegate(element);
	}

	@RequiredReadAction
	public static boolean isNullOrEmpty(@NotNull PsiNameIdentifierOwner owner)
	{
		PsiElement nameIdentifier = owner.getNameIdentifier();
		return nameIdentifier == null || nameIdentifier instanceof CSharpIdentifier && ((CSharpIdentifier) nameIdentifier).getValue() == null;
	}

	@Nullable
	@RequiredReadAction
	public static String getNameWithoutAt(@NotNull PsiNameIdentifierOwner element)
	{
		CSharpIdentifier nameIdentifier = (CSharpIdentifier) element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}
		String value = nameIdentifier.getValue();
		if(value == null)
		{
			return null;
		}
		return getNameWithoutAt(value);
	}

	@NotNull
	public static String getNameWithoutAt(@NotNull String oldName)
	{
		if(oldName.isEmpty())
		{
			return oldName;
		}

		if(oldName.charAt(0) == '@')
		{
			return oldName.substring(1, oldName.length());
		}
		return oldName;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetNamedElement findSingleElement(@NotNull CSharpFile file)
	{
		DotNetNamedElement member = findSingleElementNoNameCheck(file);
		if(member != null && Comparing.equal(FileUtil.getNameWithoutExtension(file.getName()), member.getName()))
		{
			return member;
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetNamedElement findSingleElementNoNameCheck(@NotNull CSharpFile file)
	{
		DotNetNamedElement[] members = file.getMembers();
		if(members.length != 1)
		{
			return null;
		}

		DotNetNamedElement member = members[0];
		if(member instanceof DotNetNamespaceDeclaration)
		{
			DotNetNamedElement[] namespacesDeclarations = ((DotNetNamespaceDeclaration) member).getMembers();
			if(namespacesDeclarations.length != 1)
			{
				return null;
			}

			DotNetNamedElement namespaceChildren = namespacesDeclarations[0];
			if(namespaceChildren instanceof DotNetNamespaceDeclaration)
			{
				return null;
			}
			return namespaceChildren;
		}
		else
		{
			return member;
		}
	}

	@Nullable
	public static CSharpFile findCSharpFile(@Nullable PsiFile psiFile)
	{
		if(psiFile == null)
		{
			return null;
		}
		FileViewProvider viewProvider = psiFile.getViewProvider();

		PsiFile psi = viewProvider.getPsi(CSharpLanguage.INSTANCE);
		if(psi == null)
		{
			return null;
		}
		return (CSharpFile) psi;
	}
}
