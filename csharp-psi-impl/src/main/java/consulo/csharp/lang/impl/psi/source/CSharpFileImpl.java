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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.function.CommonProcessors;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpNamespaceStatement;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

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
		return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result.create(getUsingStatementsInner(), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	private CSharpUsingListChild[] getUsingStatementsInner()
	{
		StubElement<?> stub = getStub();
		if(stub != null)
		{
			return stub.getChildrenByType(CSharpStubElementSets.USING_CHILDREN, CSharpUsingListChild.ARRAY_FACTORY);
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

	@Nullable
	@Override
	public CSharpNamespaceStatement getNamespaceStatement()
	{
		StubElement stub = getStub();
		if(stub != null)
		{
			return (CSharpNamespaceStatement) stub.findChildStubByType(CSharpStubElementSets.NAMESPACE_STATEMENT);
		}
		return findChildByClass(CSharpNamespaceStatement.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<String> getGlobalUsings()
	{
		return CachedValuesManager.getManager(getProject()).createCachedValue(() -> CachedValueProvider.Result.create(getGlobalUsingsImpl(), PsiModificationTracker.MODIFICATION_COUNT)).getValue();
	}

	private List<String> getGlobalUsingsImpl()
	{
		Project project = getProject();
		GlobalSearchScope resolveScope = getResolveScope();

		CommonProcessors.CollectUniquesProcessor<String> uniquesProcessor = new CommonProcessors.CollectUniquesProcessor<>();
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.GLOBAL_USING_NAMESPACE, uniquesProcessor, resolveScope, IdFilter.getProjectIdFilter(project, true));
		return uniquesProcessor.getResults().stream().map(s -> StringUtil.trimStart(s, CSharpUsingNamespaceStatementImpl.GLOBAL_PREFIX)).toList();
	}

	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		StubElement<?> stub = getStub();
		if(stub != null)
		{
			return stub.getChildrenByType(CSharpStubElementSets.QUALIFIED_MEMBERS, DotNetQualifiedElement.ARRAY_FACTORY);
		}
		return findChildrenByClass(DotNetQualifiedElement.class);
	}
}
