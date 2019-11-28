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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration>
{
	@RequiredReadAction
	public CSharpTypeResolveContext(@Nonnull CSharpTypeDeclaration element, @Nonnull DotNetGenericExtractor genericExtractor, @Nullable Set<PsiElement> recursiveGuardSet)
	{
		super(element, genericExtractor, recursiveGuardSet);
	}

	@Override
	public void acceptChildren(CSharpElementVisitor visitor)
	{
		for(DotNetNamedElement element : myElement.getMembers())
		{
			ProgressManager.checkCanceled();

			element.accept(visitor);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		DotNetTypeRef[] typeRefs = myElement.getExtendTypeRefs();
		List<DotNetTypeRef> extendTypeRefs = new ArrayList<>(typeRefs.length);

		for(DotNetTypeRef typeRef : typeRefs)
		{
			DotNetTypeRef ref = RecursionManager.doPreventingRecursion(this, false, () -> GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor, myElement));
			if(ref == null)
			{
				continue;
			}
			extendTypeRefs.add(ref);
		}
		return extendTypeRefs;
	}
}
