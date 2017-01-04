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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration>
{
	@RequiredReadAction
	public CSharpTypeResolveContext(@NotNull CSharpTypeDeclaration element,
			@NotNull DotNetGenericExtractor genericExtractor,
			@Nullable Set<PsiElement> recursiveGuardSet)
	{
		super(element, genericExtractor, recursiveGuardSet);
	}

	@Override
	public void acceptChildren(CSharpElementVisitor visitor)
	{
		for(DotNetNamedElement element : myElement.getMembers())
		{
			element.accept(visitor);
		}
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		DotNetTypeRef[] typeRefs = myElement.getExtendTypeRefs();
		List<DotNetTypeRef> extendTypeRefs = new ArrayList<DotNetTypeRef>(typeRefs.length);

		for(DotNetTypeRef typeRef : typeRefs)
		{
			extendTypeRefs.add(GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor, myElement));
		}
		return extendTypeRefs;
	}
}
