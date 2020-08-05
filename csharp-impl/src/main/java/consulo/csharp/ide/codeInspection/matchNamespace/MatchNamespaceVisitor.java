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

package consulo.csharp.ide.codeInspection.matchNamespace;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.localize.CSharpInspectionLocalize;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.util.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 05-Nov-17
 */
class MatchNamespaceVisitor extends CSharpElementVisitor
{
	private ProblemsHolder myHolder;
	private final String myExpectedNamespace;

	private List<CSharpNamespaceDeclaration> myRootNamespaces = new ArrayList<>();

	@RequiredReadAction
	MatchNamespaceVisitor(ProblemsHolder holder, DotNetSimpleModuleExtension extension)
	{
		myHolder = holder;

		DotNetNamespaceGeneratePolicy namespaceGeneratePolicy = extension.getNamespaceGeneratePolicy();

		myExpectedNamespace = namespaceGeneratePolicy.calculateNamespace(holder.getFile().getContainingDirectory());
	}

	@Override
	@RequiredReadAction
	public void visitNamespaceDeclaration(CSharpNamespaceDeclaration declaration)
	{
		CSharpNamespaceDeclaration top = PsiTreeUtil.getParentOfType(declaration, CSharpNamespaceDeclaration.class);
		if(top != null)
		{
			return;
		}

		DotNetReferenceExpression namespaceReference = declaration.getNamespaceReference();
		if(namespaceReference == null)
		{
			return;
		}

		myRootNamespaces.add(declaration);
	}

	@RequiredReadAction
	public void report()
	{
		// we can't change to root namespace
		if(StringUtil.isEmpty(myExpectedNamespace))
		{
			return;
		}

		if(myRootNamespaces.isEmpty())
		{
			PsiFile file = myHolder.getFile();

			myHolder.registerProblem(file, CSharpInspectionLocalize.expectedNamespaceInspection(myExpectedNamespace).getValue());
		}
		else if(myRootNamespaces.size() == 1)
		{
			CSharpNamespaceDeclaration declaration = myRootNamespaces.get(0);

			String presentableQName = declaration.getPresentableQName();

			if(!Objects.equals(myExpectedNamespace, presentableQName))
			{
				DotNetReferenceExpression namespaceReference = declaration.getNamespaceReference();
				assert namespaceReference != null;
				myHolder.registerProblem(namespaceReference, CSharpInspectionLocalize.expectedNamespaceInspection(myExpectedNamespace).getValue(), new ChangeNamespaceFix(declaration,
						myExpectedNamespace));
			}
		}
	}
}
