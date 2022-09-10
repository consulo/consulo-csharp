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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.stub.CSharpGenericParameterStub;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericParameterImpl extends CSharpStubMemberImpl<CSharpGenericParameterStub> implements CSharpGenericParameter
{
	public CSharpGenericParameterImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericParameterImpl(@Nonnull CSharpGenericParameterStub stub)
	{
		super(stub, CSharpStubElements.GENERIC_PARAMETER);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Override
	public int getIndex()
	{
		PsiElement parentByStub = getParentByStub();
		if(parentByStub instanceof DotNetGenericParameterList)
		{
			return ArrayUtil.find(((DotNetGenericParameterList) parentByStub).getParameters(), this);
		}
		return -1;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList != null)
		{
			return modifierList.getAttributes();
		}
		return DotNetAttribute.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result.create(CSharpGenericConstraintUtil.getExtendTypes(CSharpGenericParameterImpl.this), PsiModificationTracker
				.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
	}
}
