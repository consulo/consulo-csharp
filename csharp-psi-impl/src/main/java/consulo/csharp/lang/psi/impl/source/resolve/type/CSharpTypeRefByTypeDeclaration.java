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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeRefByTypeDeclaration extends DotNetTypeRefWithCachedResult
{
	private DotNetTypeDeclaration myElement;
	@Nonnull
	private final DotNetGenericExtractor myExtractor;

	public CSharpTypeRefByTypeDeclaration(@Nonnull DotNetTypeDeclaration element)
	{
		this(element, DotNetGenericExtractor.EMPTY);
	}

	public CSharpTypeRefByTypeDeclaration(@Nonnull DotNetTypeDeclaration element, @Nonnull DotNetGenericExtractor extractor)
	{
		super(element.getProject(), element.getResolveScope());
		myElement = element;
		myExtractor = extractor;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		CSharpMethodDeclaration methodDeclaration = myElement.getUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE);
		if(methodDeclaration != null)
		{
			return new CSharpUserTypeRef.LambdaResult(getProject(), getResolveScope(), methodDeclaration, myExtractor);
		}
		return new CSharpUserTypeRef.Result<PsiElement>(myElement, myExtractor);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
	{
		return CSharpTypeRefPresentationUtil.buildText(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CSharpTypeRefByTypeDeclaration other)
		{
			return myElement.isEquivalentTo(other.myElement) && myExtractor.equals(other.myExtractor);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return getVmQName().hashCode() ^ myExtractor.hashCode();
	}
}
