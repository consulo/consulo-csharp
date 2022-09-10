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

package consulo.csharp.lang.impl.psi.resolve;

import consulo.application.progress.ProgressManager;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResultUtil;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetShortNameSearcher;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.project.Project;
import consulo.project.content.scope.ProjectAwareSearchScope;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

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
	public void collectTypeNames(@Nonnull Predicate<String> processor, @Nonnull SearchScope searchScope, @Nullable IdFilter filter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, processor, (ProjectAwareSearchScope) searchScope, filter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, processor, (ProjectAwareSearchScope) searchScope, filter);
	}

	@Override
	public void collectTypes(@Nonnull String s, @Nonnull SearchScope searchScope, @Nullable IdFilter filter, @Nonnull final Predicate<DotNetTypeDeclaration> processor)
	{
		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, s, myProject, (ProjectAwareSearchScope) searchScope, filter, CSharpTypeDeclaration.class,
				processor);

		StubIndex.getInstance().processElements(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, s, myProject, (ProjectAwareSearchScope) searchScope, filter, CSharpMethodDeclaration.class,
				methodDeclaration ->
		{
			ProgressManager.checkCanceled();

			CSharpTypeDeclaration typeFromDelegate = CSharpLambdaResolveResultUtil.createTypeFromDelegate(methodDeclaration, DotNetGenericExtractor.EMPTY);
			return processor.test(typeFromDelegate);
		});
	}
}
