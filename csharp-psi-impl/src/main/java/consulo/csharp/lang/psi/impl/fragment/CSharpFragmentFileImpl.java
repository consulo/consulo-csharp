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

package consulo.csharp.lang.psi.impl.fragment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpCodeFragment;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightUsingNamespaceStatementBuilder;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpFragmentFileImpl extends PsiFileImpl implements CSharpCodeFragment, PsiCodeFragment, CSharpFile
{
	@Nullable
	private final PsiElement myContext;

	private GlobalSearchScope mySearchScope;

	private Set<String> myUsingNamespaceChildren = new LinkedHashSet<>();

	public CSharpFragmentFileImpl(@Nonnull IElementType elementType, IElementType contentElementType, @Nonnull FileViewProvider provider, @Nullable PsiElement context)
	{
		super(elementType, contentElementType, provider);
		myContext = context;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Override
	public PsiElement getContext()
	{
		if(myContext != null)
		{
			return myContext;
		}
		return super.getContext();
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor psiElementVisitor)
	{
		psiElementVisitor.visitFile(this);
	}

	@Override
	@Nullable
	public PsiElement getScopeElement()
	{
		return myContext;
	}

	@Override
	public void forceResolveScope(GlobalSearchScope searchScope)
	{
		mySearchScope = searchScope;
	}

	@Override
	public GlobalSearchScope getForcedResolveScope()
	{
		return mySearchScope;
	}

	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return DotNetQualifiedElement.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Override
	public void addUsingChild(@Nonnull CSharpUsingListChild child)
	{
		if(myContext == null)
		{
			return;
		}

		myManager.beforeChange(false); // clear resolve cache
		if(child instanceof CSharpUsingNamespaceStatement)
		{
			String referenceText = ((CSharpUsingNamespaceStatement) child).getReferenceText();
			assert referenceText != null;
			String qName = StringUtil.strip(referenceText, CSharpReferenceExpression.DEFAULT_REF_FILTER);
			myUsingNamespaceChildren.add(qName);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpUsingListChild[] getUsingStatements()
	{
		List<CSharpUsingListChild> listChildren = new SmartList<>();
		for(String usingListChild : myUsingNamespaceChildren)
		{
			GlobalSearchScope resolveScope = myContext.getResolveScope();
			DotNetNamespaceAsElement namespace = DotNetPsiSearcher.getInstance(getProject()).findNamespace(usingListChild, resolveScope);
			if(namespace != null)
			{
				listChildren.add(new CSharpLightUsingNamespaceStatementBuilder(namespace, resolveScope));
			}
		}
		return ContainerUtil.toArray(listChildren, CSharpUsingListChild.ARRAY_FACTORY);
	}
}
