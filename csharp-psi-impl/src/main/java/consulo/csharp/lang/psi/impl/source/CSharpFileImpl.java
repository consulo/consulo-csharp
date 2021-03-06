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

import javax.annotation.Nonnull;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpFileImpl extends PsiFileBase implements CSharpFile
{
	public CSharpFileImpl(@Nonnull FileViewProvider viewProvider)
	{
		super(viewProvider, CSharpLanguage.INSTANCE);
	}

	@Override
	@Nonnull
	@RequiredReadAction
	public CSharpUsingListChild[] getUsingStatements()
	{
		return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(getUsingStatementsInner(), PsiModificationTracker.MODIFICATION_COUNT)) ;
	}

	@Nonnull
	private CSharpUsingListChild[] getUsingStatementsInner()
	{
		StubElement<?> stub = getStub();
		if(stub != null)
		{
			return stub.getChildrenByType(CSharpStubElements.USING_CHILDREN, CSharpUsingListChild.ARRAY_FACTORY);
		}
		return findChildrenByClass(CSharpUsingListChild.class);
	}

	@Override
	public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException
	{
		DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElementNoNameCheck(this);
		if(singleElement != null && (singleElement == first || singleElement == last))
		{
			delete();
		}
		else
		{
			super.deleteChildRange(first, last);
		}
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitCSharpFile(this);
		}
		else
		{
			super.accept(visitor);
		}
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		StubElement<?> stub = getStub();
		if(stub != null)
		{
			return stub.getChildrenByType(CSharpStubElements.QUALIFIED_MEMBERS, DotNetQualifiedElement.ARRAY_FACTORY);
		}
		return findChildrenByClass(DotNetQualifiedElement.class);
	}
}
