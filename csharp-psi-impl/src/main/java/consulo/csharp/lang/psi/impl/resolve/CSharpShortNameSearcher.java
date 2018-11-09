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

package consulo.csharp.lang.psi.impl.resolve;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResultUtil;
import consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetShortNameSearcher;

/**
 * @author VISTALL
 * @since 08.07.2015
 */
public class CSharpShortNameSearcher extends DotNetShortNameSearcher
{
	@Inject
	public CSharpShortNameSearcher(Project project)
	{
		super(project);
	}

	@Override
	public void collectTypeNames(@Nonnull Processor<String> processor, @Nonnull GlobalSearchScope searchScope, @Nullable IdFilter filter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, processor, searchScope, filter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, processor, searchScope, filter);
	}

	@Override
	public void collectTypes(@Nonnull String s, @Nonnull GlobalSearchScope searchScope, @Nullable IdFilter filter, @Nonnull final Processor<DotNetTypeDeclaration> processor)
	{
		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, s, myProject, searchScope, filter, CSharpTypeDeclaration.class, processor);

		StubIndex.getInstance().processElements(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, s, myProject, searchScope, filter, CSharpMethodDeclaration.class, methodDeclaration ->
		{
			ProgressManager.checkCanceled();

			CSharpTypeDeclaration typeFromDelegate = CSharpLambdaResolveResultUtil.createTypeFromDelegate(methodDeclaration, DotNetGenericExtractor.EMPTY);
			return processor.process(typeFromDelegate);
		});
	}
}
