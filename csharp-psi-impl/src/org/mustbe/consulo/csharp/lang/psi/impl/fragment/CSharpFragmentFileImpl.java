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

package org.mustbe.consulo.csharp.lang.psi.impl.fragment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpCodeFragment;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightUsingNamespaceStatementBuilder;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.CharFilter;
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

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpFragmentFileImpl extends PsiFileImpl implements CSharpCodeFragment, PsiCodeFragment, CSharpFile
{
	@Nullable
	private final PsiElement myContext;

	private Set<String> myUsingNamespaceChildren = new LinkedHashSet<String>();

	public CSharpFragmentFileImpl(@NotNull IElementType elementType, IElementType contentElementType, @NotNull FileViewProvider provider, @Nullable PsiElement context)
	{
		super(elementType, contentElementType, provider);
		myContext = context;
	}

	@RequiredReadAction
	@NotNull
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

	@NotNull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Override
	public void accept(@NotNull PsiElementVisitor psiElementVisitor)
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

	}

	@Override
	public GlobalSearchScope getForcedResolveScope()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return DotNetQualifiedElement.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Override
	public void addUsingChild(@NotNull CSharpUsingListChild child)
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
			String qName = StringUtil.strip(referenceText, CharFilter.NOT_WHITESPACE_FILTER);
			myUsingNamespaceChildren.add(qName);
		}
	}

	@RequiredReadAction
	@NotNull
	@Override
	public CSharpUsingListChild[] getUsingStatements()
	{
		List<CSharpUsingListChild> listChildren = new SmartList<CSharpUsingListChild>();
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
