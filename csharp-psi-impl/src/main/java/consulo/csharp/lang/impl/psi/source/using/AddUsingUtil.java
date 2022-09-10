/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.impl.psi.source.using;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiParserFacade;
import consulo.util.collection.ArrayUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-07-21
 */
public class AddUsingUtil
{
	@Nonnull
	@RequiredReadAction
	private static PsiElement getElementForBeforeAdd(@Nonnull PsiFile file)
	{
		if(file instanceof CSharpFile)
		{
			CSharpUsingListChild[] usingStatements = ((CSharpFile) file).getUsingStatements();
			if(usingStatements.length > 0)
			{
				return ArrayUtil.getLastElement(usingStatements);
			}
		}
		return file;
	}

	@RequiredWriteAction
	public static void addUsingNoCaretMoving(@Nonnull PsiFile file, @Nonnull String qName)
	{
		PsiElement elementForBeforeAdd = getElementForBeforeAdd(file);

		CSharpUsingNamespaceStatement newStatement = CSharpFileFactory.createUsingNamespaceStatement(file.getProject(), qName);

		if(file instanceof CSharpCodeFragment)
		{
			((CSharpCodeFragment) file).addUsingChild(newStatement);
		}
		else if(elementForBeforeAdd instanceof CSharpUsingListChild)
		{
			addUsingStatementAfter((CSharpUsingListChild) elementForBeforeAdd, newStatement);
		}
		else if(elementForBeforeAdd instanceof CSharpFile)
		{
			DotNetQualifiedElement[] members = ((CSharpFile) elementForBeforeAdd).getMembers();

			PsiElement firstChild = members.length > 0 ? members[0] : elementForBeforeAdd.getFirstChild();

			assert firstChild != null;

			PsiElement usingStatementNew = elementForBeforeAdd.addBefore(newStatement, firstChild);

			PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(file.getProject()).createWhiteSpaceFromText("\n\n");

			elementForBeforeAdd.addAfter(whiteSpaceFromText, usingStatementNew);
		}
	}

	@RequiredReadAction
	private static void addUsingStatementAfter(@Nonnull CSharpUsingListChild afterElement, @Nonnull CSharpUsingNamespaceStatement newStatement)
	{
		CSharpUsingListOwner parent = (CSharpUsingListOwner) afterElement.getParent();

		if(isUsingListContainsNamespace(parent, newStatement.getReferenceText()))
		{
			return;
		}

		Project project = afterElement.getProject();

		PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");

		parent.addAfter(whiteSpaceFromText, afterElement);

		parent.addAfter(newStatement, afterElement.getNode().getTreeNext().getPsi());
	}

	@RequiredReadAction
	public static boolean isUsingListContainsNamespace(CSharpUsingListOwner listOwner, String namespace)
	{
		for(CSharpUsingListChild child : listOwner.getUsingStatements())
		{
			if(child instanceof CSharpUsingNamespaceStatement)
			{
				String ref = ((CSharpUsingNamespaceStatement) child).getReferenceText();
				if(StringUtil.equals(ref, namespace))
				{
					return true;
				}
			}
		}

		return false;
	}
}
