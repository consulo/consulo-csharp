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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpCodeFragment;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpExpressionFragmentFileImpl extends PsiFileImpl implements CSharpCodeFragment
{
	@Nullable
	private final PsiElement myContext;

	public CSharpExpressionFragmentFileImpl(
			@NotNull IElementType elementType, IElementType contentElementType, @NotNull FileViewProvider provider, @Nullable PsiElement context)
	{
		super(elementType, contentElementType, provider);
		myContext = context;
	}

	@Nullable
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

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
}
