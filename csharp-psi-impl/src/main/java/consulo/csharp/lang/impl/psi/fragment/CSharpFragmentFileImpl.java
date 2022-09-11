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

package consulo.csharp.lang.impl.psi.fragment;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightUsingNamespaceStatementBuilder;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiCodeFragment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

		getManager().dropResolveCaches();

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
