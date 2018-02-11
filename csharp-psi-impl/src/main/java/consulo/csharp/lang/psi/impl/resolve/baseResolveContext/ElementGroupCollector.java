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

package consulo.csharp.lang.psi.impl.resolve.baseResolveContext;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import consulo.csharp.lang.psi.impl.resolve.CSharpBaseResolveContext;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public abstract class ElementGroupCollector<E extends PsiElement>
{
	protected static final CSharpAdditionalMemberProvider[] ourAdditionalMemberProviders = CSharpAdditionalMemberProvider.EP_NAME.getExtensions();

	protected final CSharpAdditionalMemberProvider.Target myTarget;
	protected final CSharpBaseResolveContext<?> myResolveContext;

	public ElementGroupCollector(@Nonnull CSharpAdditionalMemberProvider.Target target, @Nonnull CSharpBaseResolveContext<?> context)
	{
		myTarget = target;
		myResolveContext = context;
	}

	@Nonnull
	protected abstract CSharpElementVisitor createVisitor(@Nonnull Consumer<E> consumer);

	@Nonnull
	public Project getProject()
	{
		return myResolveContext.getElement().getProject();
	}

	@Nonnull
	public DotNetGenericExtractor getExtractor()
	{
		return myResolveContext.getExtractor();
	}
}
